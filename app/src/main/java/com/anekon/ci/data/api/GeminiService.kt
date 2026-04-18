package com.anekon.ci.data.api

import com.anekon.ci.data.repository.GlobalStateManager
import com.anekon.ci.domain.model.AIProviderType
import com.anekon.ci.domain.model.AIAnalysisResult
import com.anekon.ci.domain.model.CodeGenerationRequest
import com.anekon.ci.domain.model.CodeGenerationResult
import com.anekon.ci.domain.model.GeneratedFile
import com.anekon.ci.domain.model.FileType
import com.anekon.ci.domain.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gemini Service - Versión Potente con las 4 capacidades estrictas:
 *
 * 1. LONG CONTEXT: Usa gemini-1.5-flash para soportar logs gigantes
 * 2. SYSTEM INSTRUCTIONS: Inyecta rol de experto CI/CD
 * 3. STRUCTURED JSON OUTPUT: Respuesta en JSON puro
 * 4. CHAIN OF THOUGHT: Schema obligatorio con razonamiento
 *
 * Schema de salida JSON:
 * {
 *   "analisis_del_error": "...",
 *   "plan_de_accion": ["..."],
 *   "codigo_final": "..."
 * }
 */
@Singleton
class GeminiService @Inject constructor(
    private val globalStateManager: GlobalStateManager
) : AIService {

    override val provider: AIProviderType = AIProviderType.GEMINI
    override val isFreeTier: Boolean = true  // Gemini tiene tier gratuito generoso

    // ============ CONFIGURACIÓN GEMINI ============
    private val baseUrl = "https://generativelanguage.googleapis.com/v1beta"
    private val client = OkHttpClient.Builder().build()

    // Modelo: gemini-1.5-flash (soporta hasta 1M tokens - ideal para logs de build)
    private val defaultModel = "models/gemini-1.5-flash"
    // Modelo alternative para máxima capacidad
    private val longContextModel = "models/gemini-1.5-flash-001"

    // ============ 1. LONG CONTEXT - Configuración ============
    // Gemini 1.5 flash soporta hasta 1M tokens de contexto
    // Esto permite analizar logs de compilación gigantes
    private val maxTokens = 65536  // ~64k tokens para respuesta
    private val maxInputTokens = 1000000  // 1M tokens de entrada

    // ============ 2. SYSTEM INSTRUCTIONS - Prompt del sistema ============
    private val systemInstruction = """
Eres un ingeniero experto en CI/CD y desarrollo Android con Kotlin.

Tu especialidad es:
- Analizar errores de compilación en GitHub Actions
- Diagnosticar problemas de dependencias y versiones
- Generar fixes precisos y funcionales
- Optimizar pipelines de CI/CD

COMPORTAMIENTO:
1. Analiza el error de forma sistemática
2. Identifica la causa raíz
3. Proporciona un plan de acción claro
4. Genera código listo para usar

IMPORTANTE: Responde EXCLUSIVAMENTE en formato JSON válido según el schema especificado.
No incluyas texto adicional fuera del JSON.
"""

    // ============ 4. CHAIN OF THOUGHT - Schema JSON obligatorio ============
    // El modelo DEBE seguir este schema, forzando razonamiento antes de código
    private val responseSchema = JSONObject().apply {
        put("type", "object")
        put("required", JSONArray().put("analisis_del_error").put("plan_de_accion").put("codigo_final"))
        put("properties", JSONObject().apply {
            put("analisis_del_error", JSONObject().apply {
                put("type", "object")
                put("description", "Análisis profundo del error con causa raíz")
                put("properties", JSONObject().apply {
                    put("tipo_error", JSONObject().put("type", "string"))
                    put("descripcion", JSONObject().put("type", "string"))
                    put("causa_raiz", JSONObject().put("type", "string"))
                    put("archivo_afectado", JSONObject().put("type", "string"))
                    put("linea_aproximada", JSONObject().put("type", "integer"))
                    put("confianza", JSONObject().apply {
                        put("type", "number")
                        put("minimum", 0.0)
                        put("maximum", 1.0)
                    })
                })
            })
            put("plan_de_accion", JSONObject().apply {
                put("type", "array")
                put("description", "Pasos secuenciales para resolver el error")
                put("items", JSONObject().put("type", "string"))
            })
            put("codigo_final", JSONObject().apply {
                put("type", "object")
                put("description", "Código generado listo para aplicar")
                put("properties", JSONObject().apply {
                    put("archivo", JSONObject().put("type", "string"))
                    put("contenido", JSONObject().put("type", "string"))
                    put("lenguaje", JSONObject().put("type", "string"))
                    put("explicacion", JSONObject().put("type", "string"))
                })
            })
        })
    }

    // ============ IMPLEMENTACIÓN DE MÉTODOS ============

    override suspend fun analyzeBuildError(
        logs: String,
        context: String?
    ): Result<AIAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val apiKey = globalStateManager.getGeminiApiKeySync()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Gemini API Key no configurada"))
            }

            // 1. LONG CONTEXT: Concatenar logs completos (soporta hasta 1M tokens)
            // 2. SYSTEM INSTRUCTIONS: Incluir rol en el prompt
            // 3. STRUCTURED OUTPUT: Usar response_schema para forzar JSON
            // 4. CHAIN OF THOUGHT: Schema incluye analisis -> plan -> codigo

            val prompt = buildBuildErrorPrompt(logs, context)

            val response = callGeminiApi(
                model = defaultModel,
                contents = listOf(
                    mapOf(
                        "role" to "user",
                        "parts" to listOf(mapOf("text" to prompt))
                    )
                ),
                systemInstruction = systemInstruction,
                responseSchema = responseSchema
            )

            Result.success(parseBuildErrorResponse(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateCode(
        request: CodeGenerationRequest
    ): Result<CodeGenerationResult> = withContext(Dispatchers.IO) {
        try {
            val apiKey = globalStateManager.getGeminiApiKeySync()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Gemini API Key no configurada"))
            }

            val prompt = buildCodeGenerationPrompt(request)

            val response = callGeminiApi(
                model = defaultModel,
                contents = listOf(
                    mapOf(
                        "role" to "user",
                        "parts" to listOf(mapOf("text" to prompt))
                    )
                ),
                systemInstruction = systemInstruction,
                responseSchema = null  // Code gen puede ser más flexible
            )

            Result.success(parseCodeGenerationResponse(response, request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(
        message: String,
        history: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = globalStateManager.getGeminiApiKeySync()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Gemini API Key no configurada"))
            }

            val contents = history.map { msg ->
                mapOf(
                    "role" to if (msg.isFromUser) "user" else "model",
                    "parts" to listOf(mapOf("text" to msg.content))
                )
            }.toMutableList()

            contents.add(mapOf(
                "role" to "user",
                "parts" to listOf(mapOf("text" to message))
            ))

            val response = callGeminiApi(
                model = defaultModel,
                contents = contents,
                systemInstruction = systemInstruction
            )

            Result.success(extractMessageContent(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reviewCode(
        code: String,
        language: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = globalStateManager.getGeminiApiKeySync()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("Gemini API Key no configurada"))
            }

            val prompt = """
Revisa el siguiente código $language y proporciona un análisis en JSON:

```$language
$code
```

Responde con:
{
  "problemas_encontrados": ["..."],
  "mejoras_sugeridas": ["..."],
  "seguridad": "...",
  "rendimiento": "..."
}
            """.trimIndent()

            val response = callGeminiApi(
                model = defaultModel,
                contents = listOf(
                    mapOf(
                        "role" to "user",
                        "parts" to listOf(mapOf("text" to prompt))
                    )
                ),
                systemInstruction = systemInstruction
            )

            Result.success(extractMessageContent(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun healthCheck(): Boolean = withContext(Dispatchers.IO) {
        try {
            val apiKey = globalStateManager.getGeminiApiKeySync()
            if (apiKey.isBlank()) return@withContext false

            val response = callGeminiApi(
                model = defaultModel,
                contents = listOf(
                    mapOf(
                        "role" to "user",
                        "parts" to listOf(mapOf("text" to "Hi"))
                    )
                ),
                maxTokens = 10
            )

            response.has("candidates")
        } catch (e: Exception) {
            false
        }
    }

    // ============ LLAMADA A LA API CON LAS 4 CAPACIDADES ============

    private fun callGeminiApi(
        model: String,
        contents: List<Map<String, Any>>,
        systemInstruction: String? = null,
        responseSchema: JSONObject? = null,
        maxTokens: Int = this.maxTokens
    ): JSONObject {
        val apiKey = globalStateManager.getGeminiApiKeySync()

        val requestBody = JSONObject().apply {
            put("contents", JSONArray(contents.map { JSONObject(it) }))

            // 1. LONG CONTEXT: Configuración para máxima capacidad
            put("generationConfig", JSONObject().apply {
                put("maxOutputTokens", maxTokens)
                put("temperature", 0.3f)  // Más deterministic para análisis
                put("topP", 0.8f)
                put("topK", 40)
            })

            // 2. SYSTEM INSTRUCTIONS: Rol de experto
            if (systemInstruction != null) {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().apply {
                        put("text", systemInstruction)
                    }))
                })
            }

            // 3. STRUCTURED OUTPUT: Forzar JSON schema
            if (responseSchema != null) {
                put("responseSchema", responseSchema)
                put("responseMimeType", "application/json")
            }
        }

        val request = Request.Builder()
            .url("$baseUrl/$model:generateContent?key=$apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("Gemini API Error: ${response.code} - $body")
        }

        return JSONObject(body)
    }

    // ============ PROMPTS ============

    private fun buildBuildErrorPrompt(logs: String, context: String?): String {
        return """
Analiza el siguiente log de error de build de Android/GitHub Actions.

LOG DE ERROR:
```
$logs
```

${context?.let { "CONTEXTO ADICIONAL:\n$it\n" } ?: ""}

Proporciona tu análisis en formato JSON siguiendo el schema obligatorio.
""".trimIndent()
    }

    private fun buildCodeGenerationPrompt(request: CodeGenerationRequest): String {
        return """
Genera código completo para una aplicación ${request.platform.name}.

APP: ${request.appName}
DESCRIPCIÓN: ${request.description}
PLATAFORMA: ${request.platform.name}
FEATURES: ${request.features.joinToString(", ")}
ARQUITECTURA: ${request.architecture}

Proporciona:
1. Estructura de archivos completa
2. Cada archivo con su path y contenido
3. Incluir build.gradle, AndroidManifest, y todos los archivos necesarios

Devuelve en JSON:
{
  "summary": "Descripción del proyecto",
  "warnings": ["advertencias"],
  "files": [{"path": "...", "content": "...", "language": "kotlin"}]
}
""".trimIndent()
    }

    // ============ PARSERS ============

    private fun parseBuildErrorResponse(json: JSONObject): AIAnalysisResult {
        return try {
            val content = extractMessageContent(json)

            // Intentar parsear como JSON estructurado
            val data = JSONObject(content)

            // Extraer según schema Chain of Thought
            val analisis = data.optJSONObject("analisis_del_error") ?: JSONObject()
            val planDeAccion = data.optJSONArray("plan_de_accion")?.let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            } ?: emptyList()
            val codigoFinal = data.optJSONObject("codigo_final") ?: JSONObject()

            AIAnalysisResult(
                success = true,
                errorType = analisis.optString("tipo_error", "UNKNOWN"),
                errorMessage = analisis.optString("descripcion", ""),
                causeRoot = analisis.optString("causa_raiz"),
                suggestedFix = planDeAccion.joinToString("\n"),
                codeSnippet = codigoFinal.optString("contenido"),
                confidence = analisis.optDouble("confianza", 0.5).toFloat(),
                provider = provider
            )
        } catch (e: Exception) {
            // Fallback si no es JSON válido
            AIAnalysisResult(
                success = true,
                errorType = "PARSING_ERROR",
                errorMessage = extractMessageContent(json).take(500),
                causeRoot = null,
                suggestedFix = "Revisar formato de respuesta",
                codeSnippet = null,
                confidence = 0.3f,
                provider = provider
            )
        }
    }

    private fun parseCodeGenerationResponse(
        json: JSONObject,
        request: CodeGenerationRequest
    ): CodeGenerationResult {
        val content = extractMessageContent(json)

        return try {
            val jsonMatch = Regex("```json\\s*([\\s\\S]*?)\\s*```").find(content)
            val data = if (jsonMatch != null) {
                JSONObject(jsonMatch.groupValues[1])
            } else {
                JSONObject(content)
            }

            val filesArray = data.getJSONArray("files")
            val files = (0 until filesArray.length()).map { i ->
                val file = filesArray.getJSONObject(i)
                GeneratedFile(
                    path = file.getString("path"),
                    content = file.getString("content"),
                    language = file.optString("language", "kotlin"),
                    type = FileType.FILE
                )
            }

            CodeGenerationResult(
                success = true,
                files = files,
                summary = data.optString("summary", ""),
                warnings = data.optJSONArray("warnings")?.let { arr ->
                    (0 until arr.length()).map { arr.getString(it) }
                } ?: emptyList(),
                provider = provider
            )
        } catch (e: Exception) {
            CodeGenerationResult(
                success = false,
                files = emptyList(),
                summary = "Error al parsear respuesta: ${e.message}",
                warnings = listOf(content.take(500)),
                provider = provider
            )
        }
    }

    private fun extractMessageContent(json: JSONObject): String {
        return try {
            val candidates = json.getJSONArray("candidates")
            if (candidates.length() > 0) {
                val content = candidates.getJSONObject(0).getJSONObject("content")
                val parts = content.getJSONArray("parts")
                if (parts.length() > 0) {
                    parts.getJSONObject(0).getString("text")
                } else {
                    throw Exception("No parts in response")
                }
            } else {
                throw Exception("No candidates in response")
            }
        } catch (e: Exception) {
            json.optString("text", json.toString())
        }
    }
}
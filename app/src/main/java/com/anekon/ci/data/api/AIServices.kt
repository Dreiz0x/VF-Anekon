package com.anekon.ci.data.api

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
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio de MiniMax API (Versión Pro/Pago)
 * Documentación: https://platform.minimaxi.com/document
 */
@Singleton
class MiniMaxService @Inject constructor(
    private val apiKey: String
) : AIService {

    override val provider: AIProviderType = AIProviderType.MINIMAX_PRO
    override val isFreeTier: Boolean = false

    private val baseUrl = "https://api.minimaxi.com"  // O usa "https://api.minimax.chat"
    private val client = OkHttpClient.Builder().build()

    // Modelos disponibles en MiniMax
    private val defaultModel = "abab6.5s"
    private val chatModel = "abab6.5-chat"

    override suspend fun analyzeBuildError(
        logs: String,
        context: String?
    ): Result<AIAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildErrorAnalysisPrompt(logs, context)

            val response = callMiniMaxApi(
                model = chatModel,
                messages = listOf(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    }
                ),
                temperature = 0.3f
            )

            Result.success(parseErrorAnalysis(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateCode(
        request: CodeGenerationRequest
    ): Result<CodeGenerationResult> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildCodeGenerationPrompt(request)

            val response = callMiniMaxApi(
                model = chatModel,
                messages = listOf(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    }
                ),
                temperature = 0.7f,
                maxTokens = 8000
            )

            Result.success(parseCodeGeneration(response, request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(
        message: String,
        history: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = history.map { msg ->
                JSONObject().apply {
                    put("role", if (msg.isFromUser) "user" else "assistant")
                    put("content", msg.content)
                }
            }.toMutableList()

            messages.add(JSONObject().apply {
                put("role", "user")
                put("content", message)
            })

            val response = callMiniMaxApi(
                model = chatModel,
                messages = messages,
                temperature = 0.8f
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
            val prompt = """
                |Eres un experto en código $language. Revisa el siguiente código y proporciona:
                |1. Problemas encontrados
                |2. Mejoras sugeridas
                |3. Seguridad y rendimiento
                |
                |Código a revisar:
                |```$language
                |$code
                |```
            """.trimMargin()

            val response = callMiniMaxApi(
                model = chatModel,
                messages = listOf(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", prompt)
                    }
                ),
                temperature = 0.5f
            )

            Result.success(extractMessageContent(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun healthCheck(): Boolean = withContext(Dispatchers.IO) {
        try {
            val response = callMiniMaxApi(
                model = defaultModel,
                messages = listOf(
                    JSONObject().apply {
                        put("role", "user")
                        put("content", "Hi")
                    }
                ),
                maxTokens = 5
            )
            return@withContext response.has("choices")
        } catch (e: Exception) {
            return@withContext false
        }
    }

    private fun callMiniMaxApi(
        model: String,
        messages: List<JSONObject>,
        temperature: Float = 0.7f,
        maxTokens: Int = 4000
    ): JSONObject {
        val requestBody = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray(messages))
            put("temperature", temperature)
            put("max_tokens", maxTokens)
        }

        val request = Request.Builder()
            .url("$baseUrl/v1/text/chatcompletion_v2")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("API Error: ${response.code} - $body")
        }

        return JSONObject(body)
    }

    private fun buildErrorAnalysisPrompt(logs: String, context: String?): String {
        return """
            |Analiza el siguiente log de error de build de Android/GitHub Actions:
            |
            |```
            |$logs
            |```
            |
            |${context?.let { "\nContexto adicional:\n$it\n" } ?: ""}
            |
            |Proporciona en formato JSON:
            |{
            |  "success": true/false,
            |  "errorType": "COMPILATION/DEPENDENCY/TEST/RUNTIME/PERMISSION",
            |  "errorMessage": "Descripción breve del error",
            |  "causeRoot": "Causa raíz del problema",
            |  "suggestedFix": "Pasos para resolver",
            |  "codeSnippet": "Código de ejemplo si aplica",
            |  "confidence": 0.0-1.0
            |}
        """.trimMargin()
    }

    private fun buildCodeGenerationPrompt(request: CodeGenerationRequest): String {
        return """
            |Genera código completo para una aplicación ${request.platform.name}.
            |
            |App: ${request.appName}
            |Descripción: ${request.description}
            |Plataforma: ${request.platform.name}
            |Features: ${request.features.joinToString(", ")}
            |Arquitectura: ${request.architecture}
            |
            |Proporciona:
            |1. Estructura de archivos completa
            |2. Cada archivo con su path y contenido
            |3. Incluir build.gradle, AndroidManifest, y todos los archivos necesarios
            |
            |Formato de respuesta:
            |```json
            |{
            |  "summary": "Descripción del proyecto generado",
            |  "warnings": ["advertencias"],
            |  "files": [
            |    {"path": "app/build.gradle", "content": "...", "language": "kotlin"}
            |  ]
            |}
            |```
        """.trimMargin()
    }

    private fun parseErrorAnalysis(json: JSONObject): AIAnalysisResult {
        val content = extractMessageContent(json)

        // Intentar parsear como JSON
        return try {
            val data = JSONObject(content)
            AIAnalysisResult(
                success = true,
                errorType = data.optString("errorType"),
                errorMessage = data.optString("errorMessage"),
                causeRoot = data.optString("causeRoot"),
                suggestedFix = data.optString("suggestedFix"),
                codeSnippet = data.optString("codeSnippet"),
                confidence = data.optDouble("confidence", 0.5).toFloat(),
                provider = provider
            )
        } catch (e: Exception) {
            // Si no es JSON válido, devolver como texto
            AIAnalysisResult(
                success = true,
                errorType = "UNKNOWN",
                errorMessage = content.take(200),
                causeRoot = null,
                suggestedFix = content,
                codeSnippet = null,
                confidence = 0.5f,
                provider = provider
            )
        }
    }

    private fun parseCodeGeneration(json: JSONObject, request: CodeGenerationRequest): CodeGenerationResult {
        val content = extractMessageContent(json)

        return try {
            // Intentar extraer el JSON de la respuesta
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
            val choices = json.getJSONArray("choices")
            if (choices.length() > 0) {
                val message = choices.getJSONObject(0).getJSONObject("message")
                message.getString("content")
            } else {
                throw Exception("No choices in response")
            }
        } catch (e: Exception) {
            // Fallback para otros formatos de respuesta
            json.optString("text", json.toString())
        }
    }
}

/**
 * Servicio OpenAI (GPT) - Alternativa gratuita
 */
@Singleton
class OpenAIService @Inject constructor(
    private val apiKey: String
) : AIService {

    override val provider: AIProviderType = AIProviderType.OPENAI
    override val isFreeTier: Boolean = true  // Con créditos gratuitos

    private val baseUrl = "https://api.openai.com/v1"
    private val client = OkHttpClient.Builder().build()

    override suspend fun analyzeBuildError(
        logs: String,
        context: String?
    ): Result<AIAnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildErrorAnalysisPrompt(logs, context)

            val response = callOpenAIApi(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    mapOf("role" to "system", "content" to "Eres un experto en CI/CD y Android"),
                    mapOf("role" to "user", "content" to prompt)
                )
            )

            Result.success(parseErrorAnalysis(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateCode(
        request: CodeGenerationRequest
    ): Result<CodeGenerationResult> = withContext(Dispatchers.IO) {
        try {
            val prompt = buildCodeGenerationPrompt(request)

            val response = callOpenAIApi(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    mapOf("role" to "system", "content" to "Eres un experto en desarrollo Android con Kotlin y Jetpack Compose"),
                    mapOf("role" to "user", "content" to prompt)
                )
            )

            Result.success(parseCodeGeneration(response, request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(
        message: String,
        history: List<ChatMessage>
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val messages = history.map { msg ->
                mapOf(
                    "role" to if (msg.isFromUser) "user" else "assistant",
                    "content" to msg.content
                )
            }.toMutableList()
            messages.add(mapOf("role" to "user", "content" to message))

            val response = callOpenAIApi(
                model = "gpt-3.5-turbo",
                messages = messages
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
            val response = callOpenAIApi(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    mapOf("role" to "system", "content" to "Eres un experto en revisión de código"),
                    mapOf("role" to "user", "content" to "Revisa este código $language:\n\n$code")
                )
            )

            Result.success(extractMessageContent(response))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun healthCheck(): Boolean = withContext(Dispatchers.IO) {
        try {
            callOpenAIApi("gpt-3.5-turbo", listOf(mapOf("role" to "user", "content" to "Hi")))
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun callOpenAIApi(model: String, messages: List<Map<String, String>>): JSONObject {
        val requestBody = JSONObject().apply {
            put("model", model)
            put("messages", JSONArray(messages))
            put("max_tokens", 4000)
            put("temperature", 0.7f)
        }

        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("OpenAI API Error: ${response.code} - $body")
        }

        return JSONObject(body)
    }

    private fun buildErrorAnalysisPrompt(logs: String, context: String?): String {
        return """
            |Analiza el siguiente log de error de build:
            |
            |```
            |$logs
            |```
            |
            |${context?.let { "Contexto: $it\n" } ?: ""}
            |
            |Proporciona en JSON con: success, errorType, errorMessage, causeRoot, suggestedFix, codeSnippet, confidence
        """.trimMargin()
    }

    private fun buildCodeGenerationPrompt(request: CodeGenerationRequest): String {
        return """
            |Genera código para: ${request.appName}
            |Platform: ${request.platform.name}
            |Features: ${request.features.joinToString(", ")}
            |
            |Devuelve JSON con: summary, warnings, files[{path, content, language}]
        """.trimMargin()
    }

    private fun parseErrorAnalysis(json: JSONObject): AIAnalysisResult {
        val content = extractMessageContent(json)
        return try {
            val data = JSONObject(content)
            AIAnalysisResult(
                success = true,
                errorType = data.optString("errorType"),
                errorMessage = data.optString("errorMessage"),
                causeRoot = data.optString("causeRoot"),
                suggestedFix = data.optString("suggestedFix"),
                codeSnippet = data.optString("codeSnippet"),
                confidence = data.optDouble("confidence", 0.5).toFloat(),
                provider = provider
            )
        } catch (e: Exception) {
            AIAnalysisResult(
                success = true,
                errorType = "UNKNOWN",
                errorMessage = content.take(200),
                causeRoot = null,
                suggestedFix = content,
                codeSnippet = null,
                confidence = 0.5f,
                provider = provider
            )
        }
    }

    private fun parseCodeGeneration(json: JSONObject, request: CodeGenerationRequest): CodeGenerationResult {
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
                summary = "Error parsing",
                warnings = listOf(content.take(500)),
                provider = provider
            )
        }
    }

    private fun extractMessageContent(json: JSONObject): String {
        return try {
            val choices = json.getJSONArray("choices")
            if (choices.length() > 0) {
                choices.getJSONObject(0).getJSONObject("message").getString("content")
            } else throw Exception("No content")
        } catch (e: Exception) {
            json.optString("text", json.toString())
        }
    }
}

/**
 * Servicio para APIs locales (Ollama, LM Studio) - Para pruebas de estrés
 */
@Singleton
class LocalAIService @Inject constructor(
    private val endpoint: String = "http://localhost:11434"
) : AIService {

    override val provider: AIProviderType = AIProviderType.LOCAL
    override val isFreeTier: Boolean = true

    private val client = OkHttpClient.Builder().build()

    override suspend fun analyzeBuildError(logs: String, context: String?): Result<AIAnalysisResult> {
        return try {
            val prompt = "Analiza este error y sugiere solución:\n\n$logs"
            val response = callLocalApi(prompt)
            Result.success(AIAnalysisResult(
                success = true,
                errorType = "LOCAL_ANALYSIS",
                errorMessage = response.take(200),
                causeRoot = null,
                suggestedFix = response,
                codeSnippet = null,
                confidence = 0.7f,
                provider = provider
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateCode(request: CodeGenerationRequest): Result<CodeGenerationResult> {
        return try {
            val prompt = "Genera código para ${request.appName}: ${request.description}"
            val response = callLocalApi(prompt)
            Result.success(CodeGenerationResult(
                success = true,
                files = listOf(GeneratedFile(
                    path = "generated/App.kt",
                    content = response,
                    language = "kotlin",
                    type = FileType.FILE
                )),
                summary = "Generated with local AI",
                warnings = emptyList(),
                provider = provider
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(message: String, history: List<ChatMessage>): Result<String> {
        return try {
            val response = callLocalApi(message)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reviewCode(code: String, language: String): Result<String> {
        return try {
            val response = callLocalApi("Revisa este código $language:\n$code")
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun healthCheck(): Boolean = withContext(Dispatchers.IO) {
        try {
            callLocalApi("test")
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun callLocalApi(prompt: String): String = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
            put("model", "llama2")  // Default model
            put("prompt", prompt)
            put("stream", false)
        }

        val request = Request.Builder()
            .url("$endpoint/api/generate")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("Local API Error: ${response.code}")
        }

        JSONObject(body).optString("response", body)
    }
}

/**
 * Servicio Anthropic (Claude) - Alternativa premium
 */
@Singleton
class AnthropicService @Inject constructor(
    private val apiKey: String
) : AIService {

    override val provider: AIProviderType = AIProviderType.ANTHROPIC
    override val isFreeTier: Boolean = false

    private val baseUrl = "https://api.anthropic.com/v1"
    private val client = OkHttpClient.Builder().build()

    override suspend fun analyzeBuildError(logs: String, context: String?): Result<AIAnalysisResult> {
        return try {
            val prompt = "Analiza este error de build:\n\n$logs"
            val response = callClaudeApi(prompt)
            Result.success(AIAnalysisResult(
                success = true,
                errorType = "CLAUDE_ANALYSIS",
                errorMessage = response.take(200),
                suggestedFix = response,
                causeRoot = null,
                codeSnippet = null,
                confidence = 0.85f,
                provider = provider
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateCode(request: CodeGenerationRequest): Result<CodeGenerationResult> {
        return try {
            val prompt = "Genera código para ${request.appName}: ${request.description}"
            val response = callClaudeApi(prompt)
            Result.success(CodeGenerationResult(
                success = true,
                files = listOf(GeneratedFile("generated/App.kt", response, "kotlin", FileType.FILE)),
                summary = "Generated by Claude",
                warnings = emptyList(),
                provider = provider
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMessage(message: String, history: List<ChatMessage>): Result<String> {
        return try {
            Result.success(callClaudeApi(message))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun reviewCode(code: String, language: String): Result<String> {
        return try {
            Result.success(callClaudeApi("Revisa: $code"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun healthCheck(): Boolean = try {
        callClaudeApi("test")
        true
    } catch (e: Exception) {
        false
    }

    private suspend fun callClaudeApi(prompt: String): String = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
            put("model", "claude-3-haiku-20240307")
            put("max_tokens", 4000)
            put("messages", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("content", prompt)
            }))
        }

        val request = Request.Builder()
            .url("$baseUrl/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: throw Exception("Empty response")

        if (!response.isSuccessful) {
            throw Exception("Anthropic API Error: ${response.code}")
        }

        val json = JSONObject(body)
        val content = json.getJSONArray("content")
        if (content.length() > 0) {
            content.getJSONObject(0).getString("text")
        } else throw Exception("No content")
    }
}
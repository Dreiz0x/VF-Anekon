package com.anekon.ci.data.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.anekon.ci.domain.model.AIProviderType
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Administrador seguro de API Keys usando EncryptedSharedPreferences
 * Las claves se almacenan encriptadas usando Android Keystore
 */
@Singleton
class SecureApiKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "anekon_secure_prefs"
        private const val KEY_PREFIX = "api_key_"
        private const val KEY_MINIMAX_PRO = "${KEY_PREFIX}minimax_pro"
        private const val KEY_MINIMAX_FREE = "${KEY_PREFIX}minimax_free"
        private const val KEY_OPENAI = "${KEY_PREFIX}openai"
        private const val KEY_ANTHROPIC = "${KEY_PREFIX}anthropic"
        private const val KEY_GEMINI = "${KEY_PREFIX}gemini"
        private const val KEY_LOCAL = "${KEY_PREFIX}local"
        private const val KEY_LOCAL_ENDPOINT = "local_endpoint"
        private const val KEY_GITHUB_TOKEN = "${KEY_PREFIX}github"
        private const val KEY_ACTIVE_PROVIDER = "active_provider"
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Guarda una API key de forma segura
     */
    fun saveApiKey(provider: AIProviderType, apiKey: String): Boolean {
        return try {
            val keyName = getKeyNameForProvider(provider)
            encryptedPrefs.edit().putString(keyName, apiKey).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene una API key almacenada
     */
    fun getApiKey(provider: AIProviderType): String? {
        return try {
            val keyName = getKeyNameForProvider(provider)
            encryptedPrefs.getString(keyName, null)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Elimina una API key
     */
    fun deleteApiKey(provider: AIProviderType): Boolean {
        return try {
            val keyName = getKeyNameForProvider(provider)
            encryptedPrefs.edit().remove(keyName).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Verifica si existe una API key para el proveedor
     */
    fun hasApiKey(provider: AIProviderType): Boolean {
        return !getApiKey(provider).isNullOrBlank()
    }

    /**
     * Guarda el endpoint para Ollama/Local
     */
    fun saveLocalEndpoint(endpoint: String): Boolean {
        return try {
            encryptedPrefs.edit().putString(KEY_LOCAL_ENDPOINT, endpoint).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene el endpoint local
     */
    fun getLocalEndpoint(): String {
        return encryptedPrefs.getString(KEY_LOCAL_ENDPOINT, "http://localhost:11434") ?: "http://localhost:11434"
    }

    /**
     * Guarda el token de GitHub
     */
    fun saveGitHubToken(token: String): Boolean {
        return try {
            encryptedPrefs.edit().putString(KEY_GITHUB_TOKEN, token).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene el token de GitHub
     */
    fun getGitHubToken(): String? {
        return try {
            encryptedPrefs.getString(KEY_GITHUB_TOKEN, null)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Elimina el token de GitHub
     */
    fun deleteGitHubToken(): Boolean {
        return try {
            encryptedPrefs.edit().remove(KEY_GITHUB_TOKEN).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Guarda el proveedor activo
     */
    fun saveActiveProvider(provider: AIProviderType): Boolean {
        return try {
            encryptedPrefs.edit().putString(KEY_ACTIVE_PROVIDER, provider.name).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene el proveedor activo
     */
    fun getActiveProvider(): AIProviderType {
        return try {
            val name = encryptedPrefs.getString(KEY_ACTIVE_PROVIDER, AIProviderType.MINIMAX_PRO.name)
            AIProviderType.valueOf(name ?: AIProviderType.MINIMAX_PRO.name)
        } catch (e: Exception) {
            AIProviderType.MINIMAX_PRO
        }
    }

    /**
     * Obtiene todos los proveedores configurados
     */
    fun getConfiguredProviders(): List<AIProviderType> {
        return AIProviderType.values().filter { hasApiKey(it) }
    }

    /**
     * Verifica si hay alguna API key configurada
     */
    fun hasAnyApiKey(): Boolean {
        return AIProviderType.values().any { hasApiKey(it) } || hasGitHubToken()
    }

    /**
     * Elimina todas las API keys
     */
    fun clearAllKeys(): Boolean {
        return try {
            val edit = encryptedPrefs.edit()
            AIProviderType.values().forEach { provider ->
                edit.remove(getKeyNameForProvider(provider))
            }
            edit.remove(KEY_GITHUB_TOKEN)
            edit.remove(KEY_ACTIVE_PROVIDER)
            edit.apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Valida una API key haciendo una llamada de prueba
     */
    suspend fun validateApiKey(provider: AIProviderType, apiKey: String): ValidationResult {
        return when (provider) {
            AIProviderType.MINIMAX_PRO, AIProviderType.MINIMAX_FREE -> validateMiniMaxKey(apiKey)
            AIProviderType.OPENAI -> validateOpenAIKey(apiKey)
            AIProviderType.ANTHROPIC -> validateAnthropicKey(apiKey)
            AIProviderType.GEMINI -> validateGeminiKey(apiKey)
            AIProviderType.LOCAL -> validateLocalEndpoint(apiKey)
        }
    }

    private suspend fun validateMiniMaxKey(apiKey: String): ValidationResult {
        return try {
            val client = OkHttpClient()
            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = """{"model":"abab6.5-chat","messages":[{"role":"user","content":"Hi"}]}""".toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url("https://api.minimaxi.com/v1/text/chatcompletion_v2")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                ValidationResult.Valid
            } else {
                ValidationResult.Invalid("API key inválida o expirada")
            }
        } catch (e: Exception) {
            ValidationResult.Error("Error de conexión: ${e.message}")
        }
    }

    private suspend fun validateOpenAIKey(apiKey: String): ValidationResult {
        return try {
            val client = OkHttpClient()
            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = """{"model":"gpt-3.5-turbo","messages":[{"role":"user","content":"Hi"}]}""".toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                ValidationResult.Valid
            } else {
                ValidationResult.Invalid("API key inválida o expirada")
            }
        } catch (e: Exception) {
            ValidationResult.Error("Error de conexión: ${e.message}")
        }
    }

    private suspend fun validateAnthropicKey(apiKey: String): ValidationResult {
        return try {
            val client = OkHttpClient()
            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = """{"model":"claude-3-haiku-20240307","max_tokens":10,"messages":[{"role":"user","content":"Hi"}]}""".toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url("https://api.anthropic.com/v1/messages")
                .addHeader("x-api-key", apiKey)
                .addHeader("anthropic-version", "2023-06-01")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                ValidationResult.Valid
            } else {
                ValidationResult.Invalid("API key inválida o expirada")
            }
        } catch (e: Exception) {
            ValidationResult.Error("Error de conexión: ${e.message}")
        }
    }

    private suspend fun validateGeminiKey(apiKey: String): ValidationResult {
        return try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1/models?key=$apiKey")
                .get()
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                ValidationResult.Valid
            } else {
                ValidationResult.Invalid("API key de Gemini inválida")
            }
        } catch (e: Exception) {
            ValidationResult.Error("Error de conexión: ${e.message}")
        }
    }

    private suspend fun validateLocalEndpoint(endpoint: String): ValidationResult {
        return try {
            val client = OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build()
            val mediaType = "application/json".toMediaTypeOrNull()
            val requestBody = """{"model":"llama2","prompt":"Hi","stream":false}""".toRequestBody(mediaType)
            
            val request = Request.Builder()
                .url("$endpoint/api/generate")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                ValidationResult.Valid
            } else {
                ValidationResult.Invalid("Endpoint no responde")
            }
        } catch (e: Exception) {
            ValidationResult.Error("No se pudo conectar: ${e.message}")
        }
    }

    private fun getKeyNameForProvider(provider: AIProviderType): String {
        return when (provider) {
            AIProviderType.MINIMAX_PRO -> KEY_MINIMAX_PRO
            AIProviderType.MINIMAX_FREE -> KEY_MINIMAX_FREE
            AIProviderType.OPENAI -> KEY_OPENAI
            AIProviderType.ANTHROPIC -> KEY_ANTHROPIC
            AIProviderType.GEMINI -> KEY_GEMINI
            AIProviderType.LOCAL -> KEY_LOCAL
        }
    }

    private fun hasGitHubToken(): Boolean {
        return !getGitHubToken().isNullOrBlank()
    }
}

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val reason: String) : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

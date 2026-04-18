package com.anekon.ci.data.api

import com.anekon.ci.domain.model.AIProviderType
import com.anekon.ci.domain.model.AIAnalysisResult
import com.anekon.ci.domain.model.CodeGenerationRequest
import com.anekon.ci.domain.model.CodeGenerationResult
import com.anekon.ci.domain.model.ChatMessage

/**
 * Interfaz unificada para múltiples proveedores de IA
 * Soporta: MiniMax Pro, OpenAI, Anthropic, y APIs gratuitas para pruebas
 */
interface AIService {
    val provider: AIProviderType
    val isFreeTier: Boolean

    /**
     * Analiza logs de error y devuelve sugerencia de fix
     */
    suspend fun analyzeBuildError(
        logs: String,
        context: String? = null
    ): Result<AIAnalysisResult>

    /**
     * Genera código para una app basada en descripción
     */
    suspend fun generateCode(
        request: CodeGenerationRequest
    ): Result<CodeGenerationResult>

    /**
     * Chat conversacional
     */
    suspend fun sendMessage(
        message: String,
        history: List<ChatMessage> = emptyList()
    ): Result<String>

    /**
     * Revisa código y sugiere mejoras
     */
    suspend fun reviewCode(
        code: String,
        language: String
    ): Result<String>

    /**
     * Test de conexión y salud del API
     */
    suspend fun healthCheck(): Boolean
}

/**
 * Factory para crear instancias de AIService según el tipo de proveedor
 */
object AIServiceFactory {

    fun create(provider: AIProviderType, apiKey: String?, customEndpoint: String? = null): AIService? {
        return when (provider) {
            AIProviderType.MINIMAX_PRO -> {
                if (apiKey.isNullOrBlank()) null
                else MiniMaxService(apiKey)
            }
            AIProviderType.MINIMAX_FREE -> {
                // Implementación gratuita de MiniMax si existe, o usar la misma con flag
                if (apiKey.isNullOrBlank()) null
                else MiniMaxService(apiKey) 
            }
            AIProviderType.OPENAI -> {
                if (apiKey.isNullOrBlank()) null
                else OpenAIService(apiKey)
            }
            AIProviderType.ANTHROPIC -> {
                if (apiKey.isNullOrBlank()) null
                else AnthropicService(apiKey)
            }
            AIProviderType.GEMINI -> {
                // Implementación de Gemini
                null // TODO: Implementar GeminiService
            }
            AIProviderType.LOCAL -> {
                // Para pruebas locales (Ollama, LM Studio, etc.)
                LocalAIService(customEndpoint ?: "http://localhost:11434")
            }
        }
    }

    /**
     * Proveedores disponibles para pruebas de estrés
     */
    fun getFreeProviders(): List<ProviderInfo> = listOf(
        ProviderInfo(
            type = AIProviderType.LOCAL,
            name = "Local / Ollama",
            description = "Para pruebas locales con Ollama, LM Studio, etc.",
            requiresEndpoint = true,
            requiresApiKey = false
        ),
        ProviderInfo(
            type = AIProviderType.MINIMAX_FREE,
            name = "MiniMax Free",
            description = "Versión gratuita de MiniMax",
            requiresEndpoint = false,
            requiresApiKey = true
        ),
        ProviderInfo(
            type = AIProviderType.OPENAI,
            name = "OpenAI Free Tier",
            description = "Pruebas con OpenAI (requiere API key)",
            requiresEndpoint = false,
            requiresApiKey = true,
            freeCreditsUrl = "https://platform.openai.com/docs/quickstart"
        )
    )

    /**
     * Proveedores premium (pago)
     */
    fun getPremiumProviders(): List<ProviderInfo> = listOf(
        ProviderInfo(
            type = AIProviderType.MINIMAX_PRO,
            name = "MiniMax Pro",
            description = "API oficial de MiniMax con modelos avanzados (abab6.5s, etc.)",
            requiresEndpoint = false,
            requiresApiKey = true,
            apiKeyUrl = "https://www.minimax.io/"
        ),
        ProviderInfo(
            type = AIProviderType.ANTHROPIC,
            name = "Anthropic Claude",
            description = "Claude API para análisis avanzados",
            requiresEndpoint = false,
            requiresApiKey = true,
            apiKeyUrl = "https://console.anthropic.com/"
        ),
        ProviderInfo(
            type = AIProviderType.GEMINI,
            name = "Google Gemini",
            description = "Google Gemini Pro/Flash",
            requiresEndpoint = false,
            requiresApiKey = true,
            apiKeyUrl = "https://aistudio.google.com/"
        )
    )
}

data class ProviderInfo(
    val type: AIProviderType,
    val name: String,
    val description: String,
    val requiresEndpoint: Boolean,
    val requiresApiKey: Boolean,
    val apiKeyUrl: String? = null,
    val freeCreditsUrl: String? = null
)

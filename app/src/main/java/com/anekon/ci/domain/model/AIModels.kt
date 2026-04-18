package com.anekon.ci.domain.model

data class AIProvider(
    val type: AIProviderType,
    val name: String,
    val iconUrl: String? = null,
    val isFree: Boolean = false,
    val requiresApiKey: Boolean = true
)

enum class AIProviderType {
    MINIMAX_PRO,    // MiniMax paid version
    MINIMAX_FREE,   // MiniMax free tier
    OPENAI,         // OpenAI GPT
    ANTHROPIC,      // Claude
    GEMINI,         // Google Gemini
    LOCAL           // Local/Ollama for testing
}

data class AIAnalysisResult(
    val success: Boolean,
    val errorType: String?,
    val errorMessage: String?,
    val causeRoot: String?,
    val suggestedFix: String?,
    val codeSnippet: String?,
    val confidence: Float,
    val provider: AIProviderType
)

data class CodeGenerationRequest(
    val appName: String,
    val description: String,
    val platform: Platform,
    val features: List<String>,
    val architecture: String,
    val additionalContext: String? = null
)

data class CodeGenerationResult(
    val success: Boolean,
    val files: List<GeneratedFile>,
    val summary: String,
    val warnings: List<String>,
    val provider: AIProviderType
)

data class GeneratedFile(
    val path: String,
    val content: String,
    val language: String,
    val type: FileType
)

enum class Platform {
    ANDROID, IOS, WEB, API, DESKTOP
}

data class ChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val provider: AIProviderType? = null,
    val isLoading: Boolean = false
)

data class AISettings(
    val selectedProvider: AIProviderType,
    val minimaxApiKey: String?,
    val openaiApiKey: String?,
    val anthropicApiKey: String?,
    val localEndpoint: String?,
    val model: String?,           // e.g., "abab6.5s" for MiniMax
    val maxTokens: Int = 4000,
    val temperature: Float = 0.7f
)
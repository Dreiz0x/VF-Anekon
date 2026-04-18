package com.anekon.ci.data.repository

import com.anekon.ci.data.api.AIService
import com.anekon.ci.data.api.AIServiceFactory
import com.anekon.ci.data.api.LocalAIService
import com.anekon.ci.data.api.MiniMaxService
import com.anekon.ci.data.api.OpenAIService
import com.anekon.ci.data.api.ProviderInfo
import com.anekon.ci.data.local.dao.AISettingsDao
import com.anekon.ci.data.local.dao.ChatMessageDao
import com.anekon.ci.data.local.dao.GeneratedAppDao
import com.anekon.ci.data.local.entity.AISettingsEntity
import com.anekon.ci.data.local.entity.ChatMessageEntity
import com.anekon.ci.data.local.entity.GeneratedAppEntity
import com.anekon.ci.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.ByteArrayOutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor(
    private val aiSettingsDao: AISettingsDao,
    private val chatMessageDao: ChatMessageDao,
    private val generatedAppDao: GeneratedAppDao
) {
    private var currentService: AIService? = null
    private val gson = Gson()

    /**
     * Inicializa el servicio de IA según la configuración guardada
     */
    suspend fun initializeService(): Result<AIService> {
        val settings = aiSettingsDao.getSettings()
            ?: return Result.failure(Exception("No AI settings found"))

        val service = when (AIProviderType.valueOf(settings.selectedProvider)) {
            AIProviderType.MINIMAX_PRO -> {
                settings.minimaxApiKey?.let { MiniMaxService(it) }
            }
            AIProviderType.MINIMAX_FREE -> {
                settings.minimaxApiKey?.let { MiniMaxService(it) }
            }
            AIProviderType.OPENAI -> {
                settings.openaiApiKey?.let { OpenAIService(it) }
            }
            AIProviderType.ANTHROPIC -> {
                settings.anthropicApiKey?.let { AnthropicService(it) }
            }
            AIProviderType.GEMINI -> {
                // TODO: Implementar GeminiService
                null
            }
            AIProviderType.LOCAL -> {
                LocalAIService(settings.localEndpoint ?: "http://localhost:11434")
            }
        } ?: return Result.failure(Exception("No API key configured for ${settings.selectedProvider}"))

        currentService = service
        return Result.success(service)
    }

    /**
     * Cambia el proveedor de IA
     */
    suspend fun switchProvider(provider: AIProviderType, apiKey: String?, endpoint: String? = null): Result<AIService> {
        val service = AIServiceFactory.create(provider, apiKey, endpoint)
            ?: return Result.failure(Exception("Failed to create AI service"))

        // Update settings
        val currentSettings = aiSettingsDao.getSettings()
        val newSettings = currentSettings?.copy(selectedProvider = provider.name) ?: AISettingsEntity(
            selectedProvider = provider.name,
            minimaxApiKey = if (provider == AIProviderType.MINIMAX_PRO || provider == AIProviderType.MINIMAX_FREE) apiKey else null,
            openaiApiKey = if (provider == AIProviderType.OPENAI) apiKey else null,
            anthropicApiKey = if (provider == AIProviderType.ANTHROPIC) apiKey else null,
            localEndpoint = if (provider == AIProviderType.LOCAL) endpoint else null,
            model = null,
            maxTokens = 4000,
            temperature = 0.7f
        )
        aiSettingsDao.insertSettings(newSettings)
        currentService = service

        return Result.success(service)
    }

    /**
     * Analiza un error de build usando IA
     */
    suspend fun analyzeBuildError(logs: String, context: String? = null): Result<AIAnalysisResult> {
        val service = currentService ?: initializeService().getOrNull()
            ?: return Result.failure(Exception("AI service not initialized"))

        return service.analyzeBuildError(logs, context)
    }

    /**
     * Genera código para una app
     */
    suspend fun generateCode(request: CodeGenerationRequest): Result<CodeGenerationResult> {
        val service = currentService ?: initializeService().getOrNull()
            ?: return Result.failure(Exception("AI service not initialized"))

        val result = service.generateCode(request)

        // Save generated app to database
        result.getOrNull()?.let { codeResult ->
            saveGeneratedApp(request, codeResult)
        }

        return result
    }

    /**
     * Chat con IA
     */
    suspend fun sendChatMessage(sessionId: String, message: String, history: List<ChatMessage>): Result<String> {
        val service = currentService ?: initializeService().getOrNull()
            ?: return Result.failure(Exception("AI service not initialized"))

        // Save user message
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                content = message,
                isFromUser = true,
                timestamp = System.currentTimeMillis(),
                provider = currentService?.provider?.name,
                isLoading = false
            )
        )

        val result = service.sendMessage(message, history)

        // Save AI response
        result.getOrNull()?.let { response ->
            chatMessageDao.insertMessage(
                ChatMessageEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    content = response,
                    isFromUser = false,
                    timestamp = System.currentTimeMillis(),
                    provider = currentService?.provider?.name,
                    isLoading = false
                )
            )
        }

        return result
    }

    /**
     * Revisa código
     */
    suspend fun reviewCode(code: String, language: String): Result<String> {
        val service = currentService ?: initializeService().getOrNull()
            ?: return Result.failure(Exception("AI service not initialized"))

        return service.reviewCode(code, language)
    }

    /**
     * Verifica salud de la API
     */
    suspend fun healthCheck(): Boolean {
        val service = currentService ?: return false
        return service.healthCheck()
    }

    /**
     * Obtiene mensajes de chat para una sesión
     */
    fun getChatMessages(sessionId: String): Flow<List<ChatMessage>> {
        return chatMessageDao.getMessagesForSession(sessionId).map { entities ->
            entities.map { entity ->
                ChatMessage(
                    id = entity.id,
                    content = entity.content,
                    isFromUser = entity.isFromUser,
                    timestamp = entity.timestamp,
                    provider = entity.provider?.let { AIProviderType.valueOf(it) },
                    isLoading = entity.isLoading
                )
            }
        }
    }

    /**
     * Obtiene apps generadas
     */
    fun getGeneratedApps(): Flow<List<GeneratedApp>> {
        return generatedAppDao.getAllGeneratedApps().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Exporta una app como ZIP
     */
    suspend fun exportAppAsZip(appId: String): Result<ByteArray> {
        val appEntity = generatedAppDao.getGeneratedAppById(appId)
            ?: return Result.failure(Exception("App not found"))

        val app = appEntity.toDomain()
        val outputStream = ByteArrayOutputStream()

        ZipOutputStream(outputStream).use { zipOut ->
            app.files.forEach { file ->
                zipOut.putNextEntry(ZipEntry(file.path))
                zipOut.write(file.content.toByteArray())
                zipOut.closeEntry()
            }
        }

        // Mark as exported
        generatedAppDao.markAsExported(appId, System.currentTimeMillis())

        return Result.success(outputStream.toByteArray())
    }

    /**
     * Obtiene información de proveedores disponibles
     */
    fun getAvailableProviders(): List<ProviderInfo> {
        return AIServiceFactory.getFreeProviders() + AIServiceFactory.getPremiumProviders()
    }

    /**
     * Obtiene proveedores gratuitos (para pruebas de estrés)
     */
    fun getFreeProviders(): List<ProviderInfo> {
        return AIServiceFactory.getFreeProviders()
    }

    /**
     * Obtiene proveedores premium
     */
    fun getPremiumProviders(): List<ProviderInfo> {
        return AIServiceFactory.getPremiumProviders()
    }

    // ============ PRIVATE HELPERS ============

    private suspend fun saveGeneratedApp(request: CodeGenerationRequest, result: CodeGenerationResult) {
        val appId = UUID.randomUUID().toString()
        val entity = GeneratedAppEntity(
            id = appId,
            name = request.appName,
            description = request.description,
            platform = request.platform.name,
            features = gson.toJson(request.features),
            filesJson = gson.toJson(result.files),
            createdAt = System.currentTimeMillis(),
            exportedAt = null
        )
        generatedAppDao.insertGeneratedApp(entity)
    }

    private fun GeneratedAppEntity.toDomain(): GeneratedApp {
        val featuresType = object : TypeToken<List<String>>() {}.type
        val filesType = object : TypeToken<List<GeneratedFile>>() {}.type

        return GeneratedApp(
            id = id,
            name = name,
            description = description,
            platform = Platform.valueOf(platform),
            features = gson.fromJson(features, featuresType),
            files = gson.fromJson(filesJson, filesType),
            createdAt = createdAt,
            exportedAt = exportedAt
        )
    }
}

// Alias for Anthropic service
private typealias AnthropicService = com.anekon.ci.data.api.AnthropicService

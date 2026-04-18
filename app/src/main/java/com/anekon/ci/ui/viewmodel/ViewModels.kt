package com.anekon.ci.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anekon.ci.data.repository.AIRepository
import com.anekon.ci.domain.model.*
import com.anekon.ci.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para el Constructor de Apps
 */
@HiltViewModel
class BuilderViewModel @Inject constructor(
    private val generateAppUseCase: GenerateAppUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BuilderUiState())
    val uiState: StateFlow<BuilderUiState> = _uiState.asStateFlow()

    fun updateAppName(name: String) {
        _uiState.update { it.copy(appName = name) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun togglePlatform(platform: Platform) {
        _uiState.update { state ->
            val platforms = if (state.selectedPlatforms.contains(platform)) {
                state.selectedPlatforms - platform
            } else {
                state.selectedPlatforms + platform
            }
            state.copy(selectedPlatforms = platforms)
        }
    }

    fun toggleFeature(feature: String) {
        _uiState.update { state ->
            val features = if (state.selectedFeatures.contains(feature)) {
                state.selectedFeatures - feature
            } else {
                state.selectedFeatures + feature
            }
            state.copy(selectedFeatures = features)
        }
    }

    fun nextStep() {
        _uiState.update { state ->
            if (state.currentStep < 4) {
                state.copy(currentStep = state.currentStep + 1)
            } else state
        }
    }

    fun previousStep() {
        _uiState.update { state ->
            if (state.currentStep > 1) {
                state.copy(currentStep = state.currentStep - 1)
            } else state
        }
    }

    fun generateApp() {
        val state = _uiState.value
        if (state.appName.isBlank() || state.selectedPlatforms.isEmpty()) {
            _uiState.update { it.copy(error = "Por favor completa los campos requeridos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }

            val result = generateAppUseCase(
                appName = state.appName,
                description = state.description,
                platform = state.selectedPlatforms.first(),  // Primary platform
                features = state.selectedFeatures.toList()
            )

            result.onSuccess { codeResult ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generatedFiles = codeResult.files,
                        generationSuccess = true
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        error = error.message ?: "Error al generar código"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun reset() {
        _uiState.value = BuilderUiState()
    }
}

data class BuilderUiState(
    val currentStep: Int = 1,
    val appName: String = "",
    val description: String = "",
    val selectedPlatforms: Set<Platform> = emptySet(),
    val selectedFeatures: Set<String> = emptySet(),
    val isGenerating: Boolean = false,
    val generatedFiles: List<GeneratedFile> = emptyList(),
    val generationSuccess: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para el Chat con IA
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatWithAIUseCase: ChatWithAIUseCase,
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var sessionId: String = java.util.UUID.randomUUID().toString()

    init {
        loadChatHistory()
        loadAvailableProviders()
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            chatWithAIUseCase.getChatHistory(sessionId).collect { messages ->
                _uiState.update { it.copy(messages = messages) }
            }
        }
    }

    private fun loadAvailableProviders() {
        val providers = aiRepository.getAvailableProviders()
        val freeProviders = aiRepository.getFreeProviders()
        val premiumProviders = aiRepository.getPremiumProviders()
        _uiState.update {
            it.copy(
                availableProviders = providers,
                freeProviders = freeProviders,
                premiumProviders = premiumProviders
            )
        }
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isTyping = true) }

            val result = chatWithAIUseCase(
                sessionId = sessionId,
                message = message,
                history = _uiState.value.messages
            )

            result.onSuccess {
                _uiState.update { it.copy(isTyping = false) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isTyping = false,
                        error = error.message
                    )
                }
            }
        }
    }

    fun switchProvider(providerType: AIProviderType, apiKey: String?, endpoint: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTyping = true) }

            val result = aiRepository.switchProvider(providerType, apiKey, endpoint)

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isTyping = false,
                        selectedProvider = providerType
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isTyping = false,
                        error = error.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearChat() {
        sessionId = java.util.UUID.randomUUID().toString()
        _uiState.update { it.copy(messages = emptyList()) }
        loadChatHistory()
    }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false,
    val error: String? = null,
    val selectedProvider: AIProviderType = AIProviderType.MINIMAX_PRO,
    val availableProviders: List<com.anekon.ci.data.api.ProviderInfo> = emptyList(),
    val freeProviders: List<com.anekon.ci.data.api.ProviderInfo> = emptyList(),
    val premiumProviders: List<com.anekon.ci.data.api.ProviderInfo> = emptyList()
)

/**
 * ViewModel para Análisis de Builds y Auto-Fix
 */
@HiltViewModel
class AutoFixViewModel @Inject constructor(
    private val analyzeBuildErrorUseCase: AnalyzeBuildErrorUseCase,
    private val applyFixUseCase: ApplyFixUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AutoFixUiState())
    val uiState: StateFlow<AutoFixUiState> = _uiState.asStateFlow()

    fun analyzeBuild(token: String, owner: String, repo: String, runId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true, error = null) }

            val result = analyzeBuildErrorUseCase(token, owner, repo, runId)

            result.onSuccess { autoFixResult ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        analysisResult = autoFixResult,
                        canApplyFix = autoFixResult.canApplyFix
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        error = error.message
                    )
                }
            }
        }
    }

    fun applyFix(
        token: String,
        owner: String,
        repo: String,
        filePath: String,
        newContent: String,
        commitMessage: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isApplyingFix = true, error = null) }

            val result = applyFixUseCase(token, owner, repo, filePath, newContent, commitMessage)

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isApplyingFix = false,
                        fixApplied = true
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isApplyingFix = false,
                        error = error.message
                    )
                }
            }
        }
    }

    fun clearResult() {
        _uiState.value = AutoFixUiState()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class AutoFixUiState(
    val isAnalyzing: Boolean = false,
    val isApplyingFix: Boolean = false,
    val analysisResult: AutoFixResult? = null,
    val canApplyFix: Boolean = false,
    val fixApplied: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel para Proyectos
 */
@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val getProjectsUseCase: GetProjectsUseCase,
    private val exportAppUseCase: ExportAppUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectsUiState())
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    init {
        loadProjects()
        loadGeneratedApps()
    }

    private fun loadProjects() {
        viewModelScope.launch {
            getProjectsUseCase.getLocalProjects().collect { projects ->
                _uiState.update { it.copy(localProjects = projects) }
            }
        }
    }

    private fun loadGeneratedApps() {
        viewModelScope.launch {
            exportAppUseCase.getGeneratedApps().collect { apps ->
                _uiState.update { it.copy(generatedApps = apps) }
            }
        }
    }

    fun syncFromGitHub(token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }

            val result = getProjectsUseCase.syncFromGitHub(token)

            result.onSuccess {
                _uiState.update { it.copy(isSyncing = false) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSyncing = false,
                        error = error.message
                    )
                }
            }
        }
    }

    fun exportApp(appId: String, onComplete: (ByteArray) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }

            val result = exportAppUseCase.exportAsZip(appId)

            result.onSuccess { zipData ->
                _uiState.update { it.copy(isExporting = false) }
                onComplete(zipData)
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        error = error.message
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ProjectsUiState(
    val localProjects: List<Project> = emptyList(),
    val generatedApps: List<GeneratedApp> = emptyList(),
    val isSyncing: Boolean = false,
    val isExporting: Boolean = false,
    val error: String? = null
)
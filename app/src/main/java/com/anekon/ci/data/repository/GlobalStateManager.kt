package com.anekon.ci.data.repository

import com.anekon.ci.data.local.PreferencesManager
import com.anekon.ci.domain.model.AIProviderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * GlobalStateManager - Resuelve "Amnesia de Navegación"
 *
 * Mantiene el estado de la aplicación en memoria RAM
 * pero también lo persiste en DataStore para sobrevivir
 * al ciclo de vida y cambios de configuración.
 *
 * PATRÓN: Singleton + Repository hybrid
 */
@Singleton
class GlobalStateManager @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    // ============ IN-MEMORY STATE (RAM) ============
    // Estos valores sobreviven a cambios de configuración
    // porque el Singleton vive en el Application context

    private val _minimaxApiKey = MutableStateFlow("")
    val minimaxApiKey: StateFlow<String> = _minimaxApiKey.asStateFlow()

    private val _openaiApiKey = MutableStateFlow("")
    val openaiApiKey: StateFlow<String> = _openaiApiKey.asStateFlow()

    private val _anthropicApiKey = MutableStateFlow("")
    val anthropicApiKey: StateFlow<String> = _anthropicApiKey.asStateFlow()

    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey.asStateFlow()

    private val _githubToken = MutableStateFlow("")
    val githubToken: StateFlow<String> = _githubToken.asStateFlow()

    private val _githubUsername = MutableStateFlow("")
    val githubUsername: StateFlow<String> = _githubUsername.asStateFlow()

    private val _selectedRepo = MutableStateFlow("")
    val selectedRepo: StateFlow<String> = _selectedRepo.asStateFlow()

    private val _selectedAIProvider = MutableStateFlow(AIProviderType.GEMINI)
    val selectedAIProvider: StateFlow<AIProviderType> = _selectedAIProvider.asStateFlow()

    private val _isFirstLaunch = MutableStateFlow(true)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch.asStateFlow()

    private val _autoFixEnabled = MutableStateFlow(false)
    val autoFixEnabled: StateFlow<Boolean> = _autoFixEnabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    // ============ INITIALIZATION ============
    // Se llama desde Application class al iniciar
    suspend fun loadAllFromDisk() {
        _minimaxApiKey.value = preferencesManager.minimaxApiKey.first()
    }

    // ============ API KEYS ============
    suspend fun saveMinimaxApiKey(key: String) {
        preferencesManager.saveMinimaxApiKey(key)
        _minimaxApiKey.value = key
    }

    suspend fun saveOpenaiApiKey(key: String) {
        preferencesManager.saveOpenaiApiKey(key)
        _openaiApiKey.value = key
    }

    suspend fun saveAnthropicApiKey(key: String) {
        preferencesManager.saveAnthropicApiKey(key)
        _anthropicApiKey.value = key
    }

    suspend fun saveGeminiApiKey(key: String) {
        preferencesManager.saveGeminiApiKey(key)
        _geminiApiKey.value = key
    }

    fun getApiKey(provider: AIProviderType): String {
        return when (provider) {
            AIProviderType.MINIMAX_PRO -> _minimaxApiKey.value
            AIProviderType.MINIMAX_FREE -> _minimaxApiKey.value
            AIProviderType.OPENAI -> _openaiApiKey.value
            AIProviderType.ANTHROPIC -> _anthropicApiKey.value
            AIProviderType.GEMINI -> _geminiApiKey.value
            AIProviderType.LOCAL -> ""
        }
    }

    // ============ GITHUB ============
    suspend fun saveGitHubToken(token: String) {
        preferencesManager.saveGitHubToken(token)
        _githubToken.value = token
    }

    suspend fun saveGitHubUsername(username: String) {
        preferencesManager.saveGitHubUsername(username)
        _githubUsername.value = username
    }

    suspend fun saveSelectedRepo(repo: String) {
        preferencesManager.saveSelectedRepo(repo)
        _selectedRepo.value = repo
    }

    // ============ AI PROVIDER ============
    suspend fun saveSelectedAIProvider(provider: AIProviderType) {
        preferencesManager.saveSelectedAIProvider(provider.name)
        _selectedAIProvider.value = provider
    }

    // ============ APP SETTINGS ============
    suspend fun setFirstLaunchComplete() {
        preferencesManager.setFirstLaunchComplete()
        _isFirstLaunch.value = false
    }

    suspend fun saveAutoFixEnabled(enabled: Boolean) {
        preferencesManager.saveAutoFixEnabled(enabled)
        _autoFixEnabled.value = enabled
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        preferencesManager.saveNotificationsEnabled(enabled)
        _notificationsEnabled.value = enabled
    }

    // ============ INIT FROM DISK (ASYNC) ============
    suspend fun initialize() {
        // Load API keys
        _minimaxApiKey.value = preferencesManager.minimaxApiKey.first()
    }

    suspend fun initializeAll() {
        // Load all values from disk into memory
        _minimaxApiKey.value = preferencesManager.minimaxApiKey.first()
        _openaiApiKey.value = preferencesManager.openaiApiKey.first()
        _anthropicApiKey.value = preferencesManager.anthropicApiKey.first()
        _geminiApiKey.value = preferencesManager.geminiApiKey.first()
        _githubToken.value = preferencesManager.githubToken.first()
        _githubUsername.value = preferencesManager.githubUsername.first()
        _selectedRepo.value = preferencesManager.selectedRepo.first()
        _selectedAIProvider.value = try {
            AIProviderType.valueOf(preferencesManager.selectedAIProvider.first())
        } catch (e: Exception) {
            AIProviderType.GEMINI
        }
        _isFirstLaunch.value = preferencesManager.isFirstLaunch.first()
        _autoFixEnabled.value = preferencesManager.autoFixEnabled.first()
        _notificationsEnabled.value = preferencesManager.notificationsEnabled.first()
    }

    // ============ GETTERS SYNC ============
    fun getMinimaxApiKeySync(): String = _minimaxApiKey.value
    fun getOpenaiApiKeySync(): String = _openaiApiKey.value
    fun getAnthropicApiKeySync(): String = _anthropicApiKey.value
    fun getGeminiApiKeySync(): String = _geminiApiKey.value
    fun getGitHubTokenSync(): String = _githubToken.value
    fun getGitHubUsernameSync(): String = _githubUsername.value
    fun getSelectedRepoSync(): String = _selectedRepo.value
    fun getSelectedAIProviderSync(): AIProviderType = _selectedAIProvider.value
    fun isFirstLaunchSync(): Boolean = _isFirstLaunch.value
    fun isAutoFixEnabledSync(): Boolean = _autoFixEnabled.value
    fun isNotificationsEnabledSync(): Boolean = _notificationsEnabled.value

    // ============ CLEAR ALL ============
    suspend fun clearAll() {
        preferencesManager.clearAll()
        _minimaxApiKey.value = ""
        _openaiApiKey.value = ""
        _anthropicApiKey.value = ""
        _geminiApiKey.value = ""
        _githubToken.value = ""
        _githubUsername.value = ""
        _selectedRepo.value = ""
        _selectedAIProvider.value = AIProviderType.GEMINI
        _isFirstLaunch.value = true
        _autoFixEnabled.value = false
        _notificationsEnabled.value = true
    }
}

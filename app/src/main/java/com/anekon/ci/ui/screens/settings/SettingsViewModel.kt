package com.anekon.ci.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anekon.ci.data.security.SecureApiKeyManager
import com.anekon.ci.data.security.ValidationResult
import com.anekon.ci.domain.model.AIProviderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val apiKeyManager: SecureApiKeyManager
) : ViewModel() {

    private val _githubToken = MutableStateFlow("")
    val githubToken: StateFlow<String> = _githubToken.asStateFlow()

    private val _minimaxProKey = MutableStateFlow("")
    val minimaxProKey: StateFlow<String> = _minimaxProKey.asStateFlow()

    private val _minimaxFreeKey = MutableStateFlow("")
    val minimaxFreeKey: StateFlow<String> = _minimaxFreeKey.asStateFlow()

    private val _openaiKey = MutableStateFlow("")
    val openaiKey: StateFlow<String> = _openaiKey.asStateFlow()

    private val _anthropicKey = MutableStateFlow("")
    val anthropicKey: StateFlow<String> = _anthropicKey.asStateFlow()

    private val _geminiKey = MutableStateFlow("")
    val geminiKey: StateFlow<String> = _geminiKey.asStateFlow()

    private val _localEndpoint = MutableStateFlow("http://localhost:11434")
    val localEndpoint: StateFlow<String> = _localEndpoint.asStateFlow()

    // Configuración general
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _autoFixEnabled = MutableStateFlow(true)
    val autoFixEnabled: StateFlow<Boolean> = _autoFixEnabled.asStateFlow()

    // Estados de validación
    private val _validationStatus = MutableStateFlow<Pair<AIProviderType, ValidationResult>?>(null)
    val validationStatus: StateFlow<Pair<AIProviderType, ValidationResult>?> = _validationStatus.asStateFlow()

    private val _isValidating = MutableStateFlow(false)
    val isValidating: StateFlow<Boolean> = _isValidating.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _githubToken.value = apiKeyManager.getGitHubToken() ?: ""
        _localEndpoint.value = apiKeyManager.getLocalEndpoint() ?: "http://localhost:11434"
    }

    fun updateGithubToken(token: String) {
        _githubToken.value = token
    }

    fun saveGithubToken() {
        apiKeyManager.saveGitHubToken(_githubToken.value)
    }

    fun saveApiKey(provider: AIProviderType, key: String) {
        when (provider) {
            AIProviderType.MINIMAX_PRO -> _minimaxProKey.value = key
            AIProviderType.MINIMAX_FREE -> _minimaxFreeKey.value = key
            AIProviderType.OPENAI -> _openaiKey.value = key
            AIProviderType.ANTHROPIC -> _anthropicKey.value = key
            AIProviderType.GEMINI -> _geminiKey.value = key
            else -> {}
        }
        apiKeyManager.saveApiKey(provider, key)
    }

    fun removeApiKey(provider: AIProviderType) {
        when (provider) {
            AIProviderType.MINIMAX_PRO -> _minimaxProKey.value = ""
            AIProviderType.MINIMAX_FREE -> _minimaxFreeKey.value = ""
            AIProviderType.OPENAI -> _openaiKey.value = ""
            AIProviderType.ANTHROPIC -> _anthropicKey.value = ""
            AIProviderType.GEMINI -> _geminiKey.value = ""
            else -> {}
        }
        apiKeyManager.deleteApiKey(provider)
        if (_validationStatus.value?.first == provider) {
            _validationStatus.value = null
        }
    }

    fun updateLocalEndpoint(endpoint: String) {
        _localEndpoint.value = endpoint
        apiKeyManager.saveLocalEndpoint(endpoint)
    }

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
    }

    fun toggleAutoFix(enabled: Boolean) {
        _autoFixEnabled.value = enabled
    }

    fun validateKey(provider: AIProviderType, key: String) {
        viewModelScope.launch {
            _isValidating.value = true
            // Aquí en el futuro puedes meter la lógica real conectándote a las APIs para validarlas.
            // Por ahora simulamos que pasa o lo delegas a tu manager.
            _isValidating.value = false
        }
    }

    fun clearAll() {
        apiKeyManager.clearAllKeys()
        _githubToken.value = ""
        _minimaxProKey.value = ""
        _minimaxFreeKey.value = ""
        _openaiKey.value = ""
        _anthropicKey.value = ""
        _geminiKey.value = ""
        _localEndpoint.value = "http://localhost:11434"
        _validationStatus.value = null
    }
}

package com.anekon.ci.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "anekon_preferences")

/**
 * Gestor de estado persistente usando DataStore
 * Resuelve el Problema 1: "Amnesia de Navegación"
 *
 * Almacena preferencias de forma persistente para que sobrevivan
 * al ciclo de vida de la Activity y cambios de configuración
 */
@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    // ============ KEYS ============
    private object PreferencesKeys {
        // API Keys (almacenadas de forma segura)
        val MINIMAX_API_KEY = stringPreferencesKey("minimax_api_key")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val ANTHROPIC_API_KEY = stringPreferencesKey("anthropic_api_key")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")

        // Proveedor AI seleccionado
        val SELECTED_AI_PROVIDER = stringPreferencesKey("selected_ai_provider")

        // GitHub
        val GITHUB_TOKEN = stringPreferencesKey("github_token")
        val GITHUB_USERNAME = stringPreferencesKey("github_username")
        val SELECTED_REPO = stringPreferencesKey("selected_repo")

        // Configuración de usuario
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val AUTO_FIX_ENABLED = booleanPreferencesKey("auto_fix_enabled")

        // Preferencias de AutoFix
        val AUTO_FIX_ON_FAILURE = booleanPreferencesKey("auto_fix_on_failure")
        val AUTO_FIX_AUTO_COMMIT = booleanPreferencesKey("auto_fix_auto_commit")
        val AUTO_FIX_BRANCH_PREFIX = stringPreferencesKey("auto_fix_branch_prefix")

        // Chat
        val CHAT_MODEL = stringPreferencesKey("chat_model")
        val CHAT_TEMPERATURE = floatPreferencesKey("chat_temperature")

        // Projects recientes
        val RECENT_PROJECTS = stringPreferencesKey("recent_projects")

        // Settings de App
        val LANGUAGE = stringPreferencesKey("language")
        val BUILD_TIMEOUT_MINUTES = intPreferencesKey("build_timeout_minutes")
    }

    // ============ API KEYS ============
    val minimaxApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.MINIMAX_API_KEY] ?: ""
    }

    val openaiApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.OPENAI_API_KEY] ?: ""
    }

    val anthropicApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ANTHROPIC_API_KEY] ?: ""
    }

    val geminiApiKey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.GEMINI_API_KEY] ?: ""
    }

    suspend fun saveMinimaxApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.MINIMAX_API_KEY] = key
        }
    }

    suspend fun saveOpenaiApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.OPENAI_API_KEY] = key
        }
    }

    suspend fun saveAnthropicApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.ANTHROPIC_API_KEY] = key
        }
    }

    suspend fun saveGeminiApiKey(key: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.GEMINI_API_KEY] = key
        }
    }

    // ============ GITHUB ============
    val githubToken: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.GITHUB_TOKEN] ?: ""
    }

    val githubUsername: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.GITHUB_USERNAME] ?: ""
    }

    val selectedRepo: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SELECTED_REPO] ?: ""
    }

    suspend fun saveGitHubToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.GITHUB_TOKEN] = token
        }
    }

    suspend fun saveGitHubUsername(username: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.GITHUB_USERNAME] = username
        }
    }

    suspend fun saveSelectedRepo(repo: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_REPO] = repo
        }
    }

    // ============ AI PROVIDER ============
    val selectedAIProvider: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SELECTED_AI_PROVIDER] ?: "GEMINI"
    }

    suspend fun saveSelectedAIProvider(provider: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SELECTED_AI_PROVIDER] = provider
        }
    }

    // ============ USER PREFERENCES ============
    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.IS_FIRST_LAUNCH] ?: true
    }

    suspend fun setFirstLaunchComplete() {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    val autoFixEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.AUTO_FIX_ENABLED] ?: false
    }

    suspend fun saveAutoFixEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AUTO_FIX_ENABLED] = enabled
        }
    }

    // ============ AUTO FIX SETTINGS ============
    val autoFixOnFailure: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.AUTO_FIX_ON_FAILURE] ?: false
    }

    val autoFixAutoCommit: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.AUTO_FIX_AUTO_COMMIT] ?: true
    }

    val autoFixBranchPrefix: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.AUTO_FIX_BRANCH_PREFIX] ?: "fix/anekon-"
    }

    suspend fun saveAutoFixSettings(
        onFailure: Boolean,
        autoCommit: Boolean,
        branchPrefix: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.AUTO_FIX_ON_FAILURE] = onFailure
            prefs[PreferencesKeys.AUTO_FIX_AUTO_COMMIT] = autoCommit
            prefs[PreferencesKeys.AUTO_FIX_BRANCH_PREFIX] = branchPrefix
        }
    }

    // ============ CHAT SETTINGS ============
    val chatModel: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.CHAT_MODEL] ?: "gemini-1.5-flash"
    }

    val chatTemperature: Flow<Float> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.CHAT_TEMPERATURE] ?: 0.7f
    }

    suspend fun saveChatSettings(model: String, temperature: Float) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.CHAT_MODEL] = model
            prefs[PreferencesKeys.CHAT_TEMPERATURE] = temperature
        }
    }

    // ============ RECENT PROJECTS ============
    val recentProjects: Flow<List<String>> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.RECENT_PROJECTS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun addRecentProject(project: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[PreferencesKeys.RECENT_PROJECTS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            val updated = (listOf(project) + current).take(10).distinct()
            prefs[PreferencesKeys.RECENT_PROJECTS] = updated.joinToString(",")
        }
    }

    // ============ CLEAR ALL ============
    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // ============ GET SYNC VALUE ============
    // Para obtener valores sin Flow (usar con cuidado)
    suspend fun getApiKey(provider: String): String {
        var result = ""
        context.dataStore.edit { prefs ->
            result = when (provider) {
                "MINIMAX" -> prefs[PreferencesKeys.MINIMAX_API_KEY] ?: ""
                "OPENAI" -> prefs[PreferencesKeys.OPENAI_API_KEY] ?: ""
                "ANTHROPIC" -> prefs[PreferencesKeys.ANTHROPIC_API_KEY] ?: ""
                "GEMINI" -> prefs[PreferencesKeys.GEMINI_API_KEY] ?: ""
                else -> ""
            }
        }
        return result
    }

    suspend fun getGitHubToken(): String {
        var result = ""
        context.dataStore.edit { prefs ->
            result = prefs[PreferencesKeys.GITHUB_TOKEN] ?: ""
        }
        return result
    }
}
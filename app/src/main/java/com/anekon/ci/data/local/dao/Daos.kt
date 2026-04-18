package com.anekon.ci.data.local.dao

import androidx.room.*
import com.anekon.ci.data.local.entity.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO para Proyectos
 */
@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE platform = :platform ORDER BY updatedAt DESC")
    fun getProjectsByPlatform(platform: String): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjects(projects: List<ProjectEntity>)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)

    @Query("UPDATE projects SET status = :status, lastBuildAt = :lastBuildAt, updatedAt = :updatedAt WHERE id = :projectId")
    suspend fun updateProjectStatus(projectId: String, status: String, lastBuildAt: Long, updatedAt: Long)
}

/**
 * DAO para Builds
 */
@Dao
interface BuildDao {
    @Query("SELECT * FROM builds WHERE projectId = :projectId ORDER BY startedAt DESC")
    fun getBuildsForProject(projectId: String): Flow<List<BuildEntity>>

    @Query("SELECT * FROM builds WHERE id = :id")
    suspend fun getBuildById(id: String): BuildEntity?

    @Query("SELECT * FROM builds WHERE status = :status ORDER BY startedAt DESC")
    fun getBuildsByStatus(status: String): Flow<List<BuildEntity>>

    @Query("SELECT * FROM builds ORDER BY startedAt DESC LIMIT :limit")
    fun getRecentBuilds(limit: Int = 20): Flow<List<BuildEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuild(build: BuildEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilds(builds: List<BuildEntity>)

    @Update
    suspend fun updateBuild(build: BuildEntity)

    @Delete
    suspend fun deleteBuild(build: BuildEntity)

    @Query("DELETE FROM builds WHERE projectId = :projectId")
    suspend fun deleteBuildsForProject(projectId: String)

    @Query("UPDATE builds SET conclusion = :conclusion, completedAt = :completedAt, errorMessage = :errorMessage WHERE id = :buildId")
    suspend fun updateBuildCompletion(buildId: String, conclusion: String?, completedAt: Long, errorMessage: String?)
}

/**
 * DAO para Mensajes de Chat
 */
@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(): ChatMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Delete
    suspend fun deleteMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)

    @Query("DELETE FROM chat_messages WHERE timestamp < :timestamp")
    suspend fun deleteOldMessages(timestamp: Long)
}

/**
 * DAO para Apps Generadas
 */
@Dao
interface GeneratedAppDao {
    @Query("SELECT * FROM generated_apps ORDER BY createdAt DESC")
    fun getAllGeneratedApps(): Flow<List<GeneratedAppEntity>>

    @Query("SELECT * FROM generated_apps WHERE id = :id")
    suspend fun getGeneratedAppById(id: String): GeneratedAppEntity?

    @Query("SELECT * FROM generated_apps WHERE platform = :platform ORDER BY createdAt DESC")
    fun getGeneratedAppsByPlatform(platform: String): Flow<List<GeneratedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneratedApp(app: GeneratedAppEntity)

    @Update
    suspend fun updateGeneratedApp(app: GeneratedAppEntity)

    @Delete
    suspend fun deleteGeneratedApp(app: GeneratedAppEntity)

    @Query("UPDATE generated_apps SET exportedAt = :exportedAt WHERE id = :appId")
    suspend fun markAsExported(appId: String, exportedAt: Long)
}

/**
 * DAO para Cuentas de GitHub
 */
@Dao
interface GitHubAccountDao {
    @Query("SELECT * FROM github_accounts ORDER BY lastUsedAt DESC")
    fun getAllAccounts(): Flow<List<GitHubAccountEntity>>

    @Query("SELECT * FROM github_accounts WHERE id = :id")
    suspend fun getAccountById(id: String): GitHubAccountEntity?

    @Query("SELECT * FROM github_accounts ORDER BY lastUsedAt DESC LIMIT 1")
    suspend fun getLastUsedAccount(): GitHubAccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: GitHubAccountEntity)

    @Update
    suspend fun updateAccount(account: GitHubAccountEntity)

    @Delete
    suspend fun deleteAccount(account: GitHubAccountEntity)

    @Query("UPDATE github_accounts SET lastUsedAt = :lastUsedAt WHERE id = :accountId")
    suspend fun updateLastUsed(accountId: String, lastUsedAt: Long)
}

/**
 * DAO para Configuración de IA
 */
@Dao
interface AISettingsDao {
    @Query("SELECT * FROM ai_settings WHERE id = 1")
    suspend fun getSettings(): AISettingsEntity?

    @Query("SELECT * FROM ai_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<AISettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AISettingsEntity)

    @Query("UPDATE ai_settings SET selectedProvider = :provider WHERE id = 1")
    suspend fun updateSelectedProvider(provider: String)

    @Query("UPDATE ai_settings SET minimaxApiKey = :apiKey WHERE id = 1")
    suspend fun updateMinimaxApiKey(apiKey: String?)

    @Query("UPDATE ai_settings SET openaiApiKey = :apiKey WHERE id = 1")
    suspend fun updateOpenaiApiKey(apiKey: String?)
}
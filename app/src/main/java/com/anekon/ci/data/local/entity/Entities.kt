package com.anekon.ci.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.anekon.ci.domain.model.Platform
import com.anekon.ci.domain.model.ProjectStatus

/**
 * Entidad de Proyecto guardado en la base de datos local
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String?,
    val platform: String,  // Platform enum as string
    val repositoryUrl: String?,
    val owner: String?,
    val repoName: String?,
    val status: String,     // ProjectStatus enum as string
    val buildCount: Int,
    val successRate: Float,
    val lastBuildAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val githubTokenId: String?
)

/**
 * Entidad de Build/Workflow Run
 */
@Entity(tableName = "builds")
data class BuildEntity(
    @PrimaryKey
    val id: String,
    val projectId: String,
    val workflowName: String,
    val branch: String,
    val commitSha: String,
    val status: String,
    val conclusion: String?,
    val duration: Long?,
    val startedAt: Long,
    val completedAt: Long?,
    val logsUrl: String?,
    val artifactsUrl: String?,
    val errorMessage: String?
)

/**
 * Entidad de Mensaje de Chat
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val sessionId: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val provider: String?,  // AIProviderType as string
    val isLoading: Boolean
)

/**
 * Entidad de App Generada
 */
@Entity(tableName = "generated_apps")
data class GeneratedAppEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val platform: String,
    val features: String,   // JSON array as string
    val filesJson: String, // JSON array of files as string
    val createdAt: Long,
    val exportedAt: Long?
)

/**
 * Entidad de Cuenta de GitHub
 */
@Entity(tableName = "github_accounts")
data class GitHubAccountEntity(
    @PrimaryKey
    val id: String,
    val username: String,
    val avatarUrl: String,
    val encryptedToken: String,
    val addedAt: Long,
    val lastUsedAt: Long?
)

/**
 * Entidad de Configuración de IA
 */
@Entity(tableName = "ai_settings")
data class AISettingsEntity(
    @PrimaryKey
    val id: Int = 1,  // Single row
    val selectedProvider: String,
    val minimaxApiKey: String?,
    val openaiApiKey: String?,
    val anthropicApiKey: String?,
    val localEndpoint: String?,
    val model: String?,
    val maxTokens: Int,
    val temperature: Float
)
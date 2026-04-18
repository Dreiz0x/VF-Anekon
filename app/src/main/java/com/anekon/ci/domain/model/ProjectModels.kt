package com.anekon.ci.domain.model

data class Project(
    val id: String,
    val name: String,
    val description: String?,
    val platform: Platform,
    val repositoryUrl: String?,
    val owner: String?,
    val repoName: String?,
    val status: ProjectStatus,
    val buildCount: Int,
    val successRate: Float,
    val lastBuildAt: Long?,
    val createdAt: Long,
    val updatedAt: Long,
    val githubTokenId: String?
)

enum class ProjectStatus {
    HEALTHY, BUILDING, ERROR, IDLE, SYNCING
}

data class Build(
    val id: String,
    val projectId: String,
    val workflowName: String,
    val branch: String,
    val commitSha: String,
    val status: BuildStatus,
    val conclusion: BuildConclusion?,
    val duration: Long?,  // in seconds
    val startedAt: Long,
    val completedAt: Long?,
    val logsUrl: String?,
    val artifactsUrl: String?,
    val errorMessage: String?,
    val aiAnalysis: AIAnalysisResult?
)

enum class BuildStatus {
    QUEUED, IN_PROGRESS, COMPLETED
}

enum class BuildConclusion {
    SUCCESS, FAILURE, CANCELLED, SKIPPED, NEUTRAL, TIMED_OUT
}

data class GeneratedApp(
    val id: String,
    val name: String,
    val description: String,
    val platform: Platform,
    val features: List<String>,
    val files: List<GeneratedFile>,
    val createdAt: Long,
    val exportedAt: Long? = null
)

data class GitHubAccount(
    val id: String,
    val username: String,
    val avatarUrl: String,
    val token: String,  // encrypted
    val addedAt: Long,
    val lastUsedAt: Long?
)
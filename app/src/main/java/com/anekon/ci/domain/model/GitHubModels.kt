package com.anekon.ci.domain.model

data class GitHubUser(
    val id: Long,
    val login: String,
    val avatarUrl: String,
    val name: String?,
    val email: String?,
    val bio: String?
)

data class Repository(
    val id: Long,
    val name: String,
    val fullName: String,
    val description: String?,
    val private: Boolean,
    val htmlUrl: String,
    val defaultBranch: String,
    val language: String?,
    val stargazersCount: Int,
    val forksCount: Int,
    val updatedAt: String
)

data class WorkflowRun(
    val id: Long,
    val name: String,
    val status: WorkflowStatus,
    val conclusion: WorkflowConclusion?,
    val branch: String,
    val commitSha: String,
    val commitMessage: String,
    val actor: String,
    val runStartedAt: String,
    val updatedAt: String,
    val htmlUrl: String,
    val workflowId: Long
)

enum class WorkflowStatus {
    QUEUED, IN_PROGRESS, COMPLETED
}

enum class WorkflowConclusion {
    SUCCESS, FAILURE, CANCELLED, SKIPPED, NEUTRAL, TIMED_OUT, ACTION_REQUIRED
}

data class WorkflowJob(
    val id: Long,
    val name: String,
    val status: WorkflowStatus,
    val conclusion: WorkflowConclusion?,
    val startedAt: String?,
    val completedAt: String?,
    val htmlUrl: String
)

data class FileContent(
    val name: String,
    val path: String,
    val type: FileType,
    val size: Long,
    val sha: String,
    val content: String?,
    val downloadUrl: String?
)

enum class FileType {
    FILE, DIR, SYMLINK, SUBMODULE, COMMIT
}

data class WorkflowLog(
    val jobId: Long,
    val jobName: String,
    val logs: String,
    val isComplete: Boolean
)

data class BuildArtifact(
    val id: Long,
    val name: String,
    val sizeInBytes: Long,
    val createdAt: String,
    val downloadUrl: String
)
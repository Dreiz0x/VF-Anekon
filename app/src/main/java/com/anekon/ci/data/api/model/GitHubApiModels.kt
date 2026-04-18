package com.anekon.ci.data.api.model

import com.google.gson.annotations.SerializedName

// ============ USER ============
data class GitHubUserDto(
    @SerializedName("id") val id: Long,
    @SerializedName("login") val login: String,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("name") val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("bio") val bio: String?
)

// ============ REPO ============
data class RepositoryDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("description") val description: String?,
    @SerializedName("private") val private: Boolean,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("default_branch") val defaultBranch: String,
    @SerializedName("language") val language: String?,
    @SerializedName("stargazers_count") val stargazersCount: Int,
    @SerializedName("forks_count") val forksCount: Int,
    @SerializedName("updated_at") val updatedAt: String
)

data class CreateRepoRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("private") val private: Boolean = false,
    @SerializedName("auto_init") val autoInit: Boolean = true
)

// ============ FILES ============
data class FileContentDto(
    @SerializedName("name") val name: String,
    @SerializedName("path") val path: String,
    @SerializedName("type") val type: String,
    @SerializedName("size") val size: Long,
    @SerializedName("sha") val sha: String,
    @SerializedName("content") val content: String?,
    @SerializedName("download_url") val downloadUrl: String?
)

data class UpdateFileRequest(
    @SerializedName("message") val message: String,
    @SerializedName("content") val content: String,  // Base64 encoded
    @SerializedName("sha") val sha: String?
)

data class CreateFileRequest(
    @SerializedName("message") val message: String,
    @SerializedName("content") val content: String,  // Base64 encoded
    @SerializedName("branch") val branch: String?
)

data class DeleteFileRequest(
    @SerializedName("message") val message: String,
    @SerializedName("sha") val sha: String
)

data class FileUpdateResultDto(
    @SerializedName("content") val content: FileContentDto?,
    @SerializedName("commit") val commit: CommitDto?
)

// ============ WORKFLOWS ============
data class WorkflowsDto(
    @SerializedName("workflows") val workflows: List<WorkflowDto>
)

data class WorkflowDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("path") val path: String,
    @SerializedName("state") val state: String
)

data class WorkflowRunsDto(
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("workflow_runs") val workflowRuns: List<WorkflowRunDto>
)

data class WorkflowRunDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("status") val status: String,
    @SerializedName("conclusion") val conclusion: String?,
    @SerializedName("head_branch") val headBranch: String,
    @SerializedName("head_sha") val headSha: String,
    @SerializedName("head_commit") val headCommit: HeadCommitDto?,
    @SerializedName("actor") val actor: ActorDto?,
    @SerializedName("run_started_at") val runStartedAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("workflow_id") val workflowId: Long,
    @SerializedName("run_number") val runNumber: Int
)

data class HeadCommitDto(
    @SerializedName("id") val id: String?,
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: String?
)

data class ActorDto(
    @SerializedName("login") val login: String,
    @SerializedName("avatar_url") val avatarUrl: String
)

data class WorkflowJobsDto(
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("jobs") val jobs: List<JobDto>
)

data class JobDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("status") val status: String,
    @SerializedName("conclusion") val conclusion: String?,
    @SerializedName("started_at") val startedAt: String?,
    @SerializedName("completed_at") val completedAt: String?,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("step_summary_url") val stepSummaryUrl: String?
)

data class LogsResponseDto(
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("workflow_run") val workflowRun: WorkflowRunDto?,
    @SerializedName("jobs") val jobs: List<JobDto>?
)

// ============ ARTIFACTS ============
data class ArtifactsDto(
    @SerializedName("total_count") val totalCount: Int,
    @SerializedName("artifacts") val artifacts: List<ArtifactDto>
)

data class ArtifactDto(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("size_in_bytes") val sizeInBytes: Long,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("expired") val expired: Boolean,
    @SerializedName("archive_download_url") val archiveDownloadUrl: String
)

// ============ BRANCHES ============
data class BranchDto(
    @SerializedName("name") val name: String,
    @SerializedName("commit") val commit: CommitInfoDto?,
    @SerializedName("protected") val protected: Boolean
)

data class CommitInfoDto(
    @SerializedName("sha") val sha: String,
    @SerializedName("url") val url: String
)

// ============ COMMITS ============
data class CommitDto(
    @SerializedName("sha") val sha: String,
    @SerializedName("commit") val commit: CommitInfo?,
    @SerializedName("html_url") val htmlUrl: String
)

data class CommitInfo(
    @SerializedName("message") val message: String,
    @SerializedName("author") val author: CommitAuthor?,
    @SerializedName("committer") val committer: CommitAuthor?
)

data class CommitAuthor(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("date") val date: String
)
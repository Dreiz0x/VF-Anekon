package com.anekon.ci.data.repository

import com.anekon.ci.data.api.GitHubApiService
import com.anekon.ci.data.api.model.*
import com.anekon.ci.data.local.dao.ProjectDao
import com.anekon.ci.data.local.dao.BuildDao
import com.anekon.ci.data.local.entity.ProjectEntity
import com.anekon.ci.data.local.entity.BuildEntity
import com.anekon.ci.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GitHubRepository @Inject constructor(
    private val apiService: GitHubApiService,
    private val projectDao: ProjectDao,
    private val buildDao: BuildDao
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // ============ USER ============
    suspend fun getCurrentUser(token: String): Result<GitHubUser> {
        return try {
            val response = apiService.getCurrentUser("Bearer $token")
            if (response.isSuccessful) {
                val dto = response.body()!!
                Result.success(
                    GitHubUser(
                        id = dto.id,
                        login = dto.login,
                        avatarUrl = dto.avatarUrl,
                        name = dto.name,
                        email = dto.email,
                        bio = dto.bio
                    )
                )
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============ REPOS ============
    suspend fun getUserRepos(token: String): Result<List<Repository>> {
        return try {
            val response = apiService.getUserRepos("Bearer $token")
            if (response.isSuccessful) {
                val repos = response.body()!!.map { it.toDomain() }
                // Save to local DB
                repos.forEach { repo ->
                    projectDao.insertProject(repo.toEntity())
                }
                Result.success(repos)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createRepository(token: String, name: String, description: String?, isPrivate: Boolean): Result<Repository> {
        return try {
            val response = apiService.createRepository(
                "Bearer $token",
                CreateRepoRequest(name, description, isPrivate)
            )
            if (response.isSuccessful) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============ FILES ============
    suspend fun getContents(token: String, owner: String, repo: String, path: String): Result<List<FileContent>> {
        return try {
            val response = apiService.getContents("Bearer $token", owner, repo, path)
            if (response.isSuccessful) {
                val items = response.body()!!.map { it.toDomain() }
                Result.success(items)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFileContent(token: String, owner: String, repo: String, path: String): Result<FileContent> {
        return try {
            val response = apiService.getFileContent("Bearer $token", owner, repo, path)
            if (response.isSuccessful) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createOrUpdateFile(
        token: String,
        owner: String,
        repo: String,
        path: String,
        content: String,
        message: String,
        sha: String? = null
    ): Result<Boolean> {
        return try {
            val encodedContent = Base64.getEncoder().encodeToString(content.toByteArray())
            val request = UpdateFileRequest(message, encodedContent, sha)
            val response = apiService.createOrUpdateFile("Bearer $token", owner, repo, path, request)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFile(token: String, owner: String, repo: String, path: String, sha: String): Result<Boolean> {
        return try {
            val response = apiService.deleteFile("Bearer $token", owner, repo, path, DeleteFileRequest("Delete $path", sha))
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============ WORKFLOWS ============
    suspend fun getWorkflowRuns(token: String, owner: String, repo: String): Result<List<WorkflowRun>> {
        return try {
            val response = apiService.getWorkflowRuns("Bearer $token", owner, repo)
            if (response.isSuccessful) {
                val runs = response.body()!!.workflowRuns.map { it.toDomain() }
                // Save builds to local DB
                runs.forEach { run ->
                    buildDao.insertBuild(run.toEntity(projectId = "$owner/$repo"))
                }
                Result.success(runs)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkflowRun(token: String, owner: String, repo: String, runId: Long): Result<WorkflowRun> {
        return try {
            val response = apiService.getWorkflowRun("Bearer $token", owner, repo, runId)
            if (response.isSuccessful) {
                Result.success(response.body()!!.toDomain())
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkflowJobs(token: String, owner: String, repo: String, runId: Long): Result<List<WorkflowJob>> {
        return try {
            val response = apiService.getWorkflowJobs("Bearer $token", owner, repo, runId)
            if (response.isSuccessful) {
                val jobs = response.body()!!.jobs.map { it.toDomain() }
                Result.success(jobs)
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getWorkflowLogs(token: String, owner: String, repo: String, runId: Long): Result<WorkflowLog> {
        return try {
            val response = apiService.getWorkflowLogs("Bearer $token", owner, repo, runId)
            if (response.isSuccessful) {
                val dto = response.body()!!
                val logText = dto.jobs?.joinToString("\n\n") { job ->
                    "Job: ${job.name}\nStatus: ${job.status} | ${job.conclusion ?: "N/A"}"
                } ?: "No logs available"

                Result.success(
                    WorkflowLog(
                        jobId = dto.jobs?.firstOrNull()?.id ?: 0,
                        jobName = dto.jobs?.firstOrNull()?.name ?: "Unknown",
                        logs = logText,
                        isComplete = true
                    )
                )
            } else {
                Result.failure(Exception("Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rerunWorkflow(token: String, owner: String, repo: String, runId: Long): Result<Boolean> {
        return try {
            val response = apiService.rerunWorkflow("Bearer $token", owner, repo, runId)
            Result.success(response.isSuccessful)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============ LOCAL DATA ============
    fun getLocalProjects(): Flow<List<Project>> {
        return projectDao.getAllProjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getLocalBuilds(projectId: String): Flow<List<Build>> {
        return buildDao.getBuildsForProject(projectId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getRecentBuilds(limit: Int = 20): Flow<List<Build>> {
        return buildDao.getRecentBuilds(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // ============ MAPPERS ============
    private fun RepositoryDto.toDomain() = Repository(
        id = id,
        name = name,
        fullName = fullName,
        description = description,
        private = private,
        htmlUrl = htmlUrl,
        defaultBranch = defaultBranch,
        language = language,
        stargazersCount = stargazersCount,
        forksCount = forksCount,
        updatedAt = updatedAt
    )

    private fun Repository.toEntity() = ProjectEntity(
        id = fullName,
        name = name,
        description = description,
        platform = "ANDROID",  // Puede determinarse por el lenguaje
        repositoryUrl = htmlUrl,
        owner = fullName.split("/").getOrNull(0) ?: "",
        repoName = name,
        status = "HEALTHY",
        buildCount = 0,
        successRate = 0f,
        lastBuildAt = null,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
        githubTokenId = null
    )

    private fun ProjectEntity.toDomain() = Project(
        id = id,
        name = name,
        description = description,
        platform = Platform.valueOf(platform),
        repositoryUrl = repositoryUrl,
        owner = owner,
        repoName = repoName,
        status = ProjectStatus.valueOf(status),
        buildCount = buildCount,
        successRate = successRate,
        lastBuildAt = lastBuildAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        githubTokenId = githubTokenId
    )

    private fun WorkflowRunDto.toDomain() = WorkflowRun(
        id = id,
        name = name,
        status = WorkflowStatus.valueOf(status.uppercase().replace("-", "_")),
        conclusion = conclusion?.let { WorkflowConclusion.valueOf(it.uppercase().replace("-", "_").replace(" ", "_")) },
        branch = headBranch,
        commitSha = headSha,
        commitMessage = headCommit?.message ?: "",
        actor = actor?.login ?: "Unknown",
        runStartedAt = runStartedAt,
        updatedAt = updatedAt,
        htmlUrl = htmlUrl,
        workflowId = workflowId
    )

    private fun WorkflowRun.toEntity(projectId: String) = BuildEntity(
        id = id.toString(),
        projectId = projectId,
        workflowName = name,
        branch = branch,
        commitSha = commitSha,
        status = status.name,
        conclusion = conclusion?.name,
        duration = null,
        startedAt = parseDate(runStartedAt),
        completedAt = null,
        logsUrl = htmlUrl,
        artifactsUrl = null,
        errorMessage = null
    )

    private fun BuildEntity.toDomain() = Build(
        id = id,
        projectId = projectId,
        workflowName = workflowName,
        branch = branch,
        commitSha = commitSha,
        status = BuildStatus.valueOf(status),
        conclusion = conclusion?.let { BuildConclusion.valueOf(it) },
        duration = duration,
        startedAt = startedAt,
        completedAt = completedAt,
        logsUrl = logsUrl,
        artifactsUrl = artifactsUrl,
        errorMessage = errorMessage,
        aiAnalysis = null
    )

    private fun FileContentDto.toDomain() = FileContent(
        name = name,
        path = path,
        type = FileType.valueOf(type.uppercase()),
        size = size,
        sha = sha,
        content = content,
        downloadUrl = downloadUrl
    )

    private fun JobDto.toDomain() = WorkflowJob(
        id = id,
        name = name,
        status = WorkflowStatus.valueOf(status.uppercase().replace("-", "_")),
        conclusion = conclusion?.let { WorkflowConclusion.valueOf(it.uppercase().replace("-", "_").replace(" ", "_")) },
        startedAt = startedAt,
        completedAt = completedAt,
        htmlUrl = htmlUrl
    )

    private fun parseDate(dateStr: String): Long {
        return try {
            dateFormat.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
package com.anekon.ci.domain.usecase

import com.anekon.ci.data.repository.GitHubRepository
import com.anekon.ci.data.repository.AIRepository
import com.anekon.ci.domain.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para analizar errores de build con IA y aplicar fixes
 */
class AnalyzeBuildErrorUseCase @Inject constructor(
    private val gitHubRepository: GitHubRepository,
    private val aiRepository: AIRepository
) {
    /**
     * Analiza los logs de un build fallido y devuelve sugerencias
     */
    suspend operator fun invoke(
        token: String,
        owner: String,
        repo: String,
        runId: Long
    ): Result<AutoFixResult> {
        // 1. Obtener los logs del workflow
        val logsResult = gitHubRepository.getWorkflowLogs(token, owner, repo, runId)
        if (logsResult.isFailure) {
            return Result.failure(logsResult.exceptionOrNull() ?: Exception("Failed to get logs"))
        }

        val logs = logsResult.getOrNull()?.logs ?: return Result.failure(Exception("No logs available"))

        // 2. Analizar con IA
        val analysisResult = aiRepository.analyzeBuildError(
            logs = logs,
            context = "Repository: $owner/$repo, Run ID: $runId"
        )

        return analysisResult.map { analysis ->
            AutoFixResult(
                buildId = runId.toString(),
                analysis = analysis,
                canApplyFix = analysis.codeSnippet != null && analysis.suggestedFix != null,
                suggestedAction = if (analysis.suggestedFix != null) {
                    SuggestedAction(
                        type = ActionType.APPLY_FIX,
                        description = analysis.suggestedFix,
                        codeSnippet = analysis.codeSnippet
                    )
                } else null
            )
        }
    }
}

/**
 * Caso de uso para aplicar un fix a un archivo
 */
class ApplyFixUseCase @Inject constructor(
    private val gitHubRepository: GitHubRepository,
    private val aiRepository: AIRepository
) {
    /**
     * Aplica el fix sugerido a un archivo específico
     */
    suspend operator fun invoke(
        token: String,
        owner: String,
        repo: String,
        filePath: String,
        newContent: String,
        commitMessage: String = "Auto-fix: Applied AI suggestion"
    ): Result<Boolean> {
        // 1. Obtener el archivo actual para obtener el SHA
        val fileResult = gitHubRepository.getFileContent(token, owner, repo, filePath)

        val sha = if (fileResult.isSuccess) {
            fileResult.getOrNull()?.sha
        } else null

        // 2. Actualizar el archivo con el fix
        return gitHubRepository.createOrUpdateFile(
            token = token,
            owner = owner,
            repo = repo,
            path = filePath,
            content = newContent,
            message = commitMessage,
            sha = sha
        )
    }

    /**
     * Crea un nuevo archivo con el fix
     */
    suspend fun createFileWithFix(
        token: String,
        owner: String,
        repo: String,
        filePath: String,
        content: String,
        commitMessage: String = "Auto-fix: Add AI suggested file"
    ): Result<Boolean> {
        return gitHubRepository.createOrUpdateFile(
            token = token,
            owner = owner,
            repo = repo,
            path = filePath,
            content = content,
            message = commitMessage,
            sha = null
        )
    }
}

/**
 * Caso de uso para generar código de app
 */
class GenerateAppUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(
        appName: String,
        description: String,
        platform: Platform,
        features: List<String>,
        architecture: String = "Clean Architecture"
    ): Result<CodeGenerationResult> {
        val request = CodeGenerationRequest(
            appName = appName,
            description = description,
            platform = platform,
            features = features,
            architecture = architecture
        )

        return aiRepository.generateCode(request)
    }
}

/**
 * Caso de uso para chat con IA
 */
class ChatWithAIUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(
        sessionId: String,
        message: String,
        history: List<ChatMessage> = emptyList()
    ): Result<String> {
        return aiRepository.sendChatMessage(sessionId, message, history)
    }

    fun getChatHistory(sessionId: String): Flow<List<ChatMessage>> {
        return aiRepository.getChatMessages(sessionId)
    }
}

/**
 * Caso de uso para obtener proyectos y builds
 */
class GetProjectsUseCase @Inject constructor(
    private val gitHubRepository: GitHubRepository
) {
    fun getLocalProjects(): Flow<List<Project>> {
        return gitHubRepository.getLocalProjects()
    }

    suspend fun syncFromGitHub(token: String): Result<List<Repository>> {
        return gitHubRepository.getUserRepos(token)
    }

    fun getRecentBuilds(limit: Int = 20): Flow<List<Build>> {
        return gitHubRepository.getRecentBuilds(limit)
    }

    suspend fun getWorkflowRuns(token: String, owner: String, repo: String): Result<List<WorkflowRun>> {
        return gitHubRepository.getWorkflowRuns(token, owner, repo)
    }
}

/**
 * Caso de uso para exportar apps como ZIP
 */
class ExportAppUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend fun exportAsZip(appId: String): Result<ByteArray> {
        return aiRepository.exportAppAsZip(appId)
    }

    fun getGeneratedApps(): Flow<List<GeneratedApp>> {
        return aiRepository.getGeneratedApps()
    }
}

/**
 * Caso de uso para revisar código
 */
class ReviewCodeUseCase @Inject constructor(
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(code: String, language: String): Result<String> {
        return aiRepository.reviewCode(code, language)
    }
}

// ============ SUPPORTING DATA CLASSES ============

data class AutoFixResult(
    val buildId: String,
    val analysis: AIAnalysisResult,
    val canApplyFix: Boolean,
    val suggestedAction: SuggestedAction?
)

data class SuggestedAction(
    val type: ActionType,
    val description: String,
    val codeSnippet: String?
)

enum class ActionType {
    APPLY_FIX,
    CREATE_FILE,
    UPDATE_CONFIG,
    RERUN_WORKFLOW,
    MANUAL_REVIEW
}

// ============ AUTO-FIX SPECIFIC USE CASES ============

/**
 * Caso de uso para obtener builds fallidos
 */
class GetFailedBuildsUseCase @Inject constructor(
    private val gitHubRepository: GitHubRepository
) {
    /**
     * Obtiene todos los builds fallidos de un repositorio
     */
    suspend fun getFailedBuilds(
        token: String,
        owner: String,
        repo: String
    ): Result<List<Build>> {
        val runsResult = gitHubRepository.getWorkflowRuns(token, owner, repo)

        return runsResult.map { runs ->
            runs.filter { run ->
                run.conclusion?.name == "FAILURE"
            }.map { run ->
                Build(
                    id = run.id.toString(),
                    projectId = "$owner/$repo",
                    workflowName = run.name,
                    branch = run.branch,
                    commitSha = run.commitSha,
                    status = BuildStatus.COMPLETED,
                    conclusion = BuildConclusion.FAILURE,
                    duration = null,
                    startedAt = run.runStartedAt?.let { parseDate(it) } ?: System.currentTimeMillis(),
                    completedAt = run.updatedAt?.let { parseDate(it) },
                    logsUrl = run.htmlUrl,
                    artifactsUrl = null,
                    errorMessage = null,
                    aiAnalysis = null
                )
            }
        }
    }

    private fun parseDate(dateStr: String): Long {
        return try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
            format.timeZone = java.util.TimeZone.getTimeZone("UTC")
            format.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}

/**
 * Caso de uso para crear settings de AutoFix por proyecto
 */
class AutoFixSettingsUseCase @Inject constructor() {

    fun getDefaultSettings(): AutoFixSettings {
        return AutoFixSettings(
            autoAnalyze = true,
            autoApplyFix = false,
            maxRetries = 3,
            notifyOnFailure = true,
            notifyOnFix = true,
            selectedProvider = AIProviderType.MINIMAX_PRO,
            preferredBranches = listOf("main", "develop"),
            excludedWorkflows = emptyList()
        )
    }

    fun shouldAutoFix(settings: AutoFixSettings, workflowName: String, branch: String): Boolean {
        // Check if workflow is excluded
        if (settings.excludedWorkflows.contains(workflowName)) {
            return false
        }

        // Check if branch is in preferred list
        if (settings.preferredBranches.isNotEmpty() && !settings.preferredBranches.contains(branch)) {
            return false
        }

        return settings.autoAnalyze || settings.autoApplyFix
    }
}

data class AutoFixSettings(
    val autoAnalyze: Boolean,
    val autoApplyFix: Boolean,
    val maxRetries: Int,
    val notifyOnFailure: Boolean,
    val notifyOnFix: Boolean,
    val selectedProvider: AIProviderType,
    val preferredBranches: List<String>,
    val excludedWorkflows: List<String>
)
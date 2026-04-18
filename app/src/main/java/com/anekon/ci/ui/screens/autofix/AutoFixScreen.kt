package com.anekon.ci.ui.screens.autofix

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anekon.ci.domain.model.AIAnalysisResult
import com.anekon.ci.domain.model.Build
import com.anekon.ci.domain.model.BuildStatus
import com.anekon.ci.ui.theme.AnekonColors

/**
 * Pantalla de AutoFix - Análisis de builds fallidos con IA
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoFixScreen(
    failedBuilds: List<Build>,
    isLoading: Boolean,
    onAnalyzeBuild: (Build) -> Unit,
    onApplyFix: (Build, AIAnalysisResult) -> Unit,
    onNavigateToDetail: (Build) -> Unit,
    onRefresh: () -> Unit
) {
    var selectedBuild by remember { mutableStateOf<Build?>(null) }
    var analysisResult by remember { mutableStateOf<AIAnalysisResult?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AnekonColors.BackgroundPrimary)
    ) {
        // Header
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "AutoFix",
                        style = MaterialTheme.typography.headlineMedium,
                        color = AnekonColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${failedBuilds.size} builds fallidos",
                        style = MaterialTheme.typography.bodySmall,
                        color = AnekonColors.TextMuted
                    )
                }
            },
            actions = {
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Actualizar",
                        tint = AnekonColors.Accent
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AnekonColors.BackgroundPrimary
            )
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AnekonColors.Accent)
            }
        } else if (failedBuilds.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AnekonColors.Success,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "¡Sin fallos!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AnekonColors.TextPrimary
                    )
                    Text(
                        text = "Todos tus builds están pasando",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AnekonColors.TextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(failedBuilds, key = { it.id }) { build ->
                    FailedBuildCard(
                        build = build,
                        isSelected = selectedBuild?.id == build.id,
                        analysisResult = if (selectedBuild?.id == build.id) analysisResult else null,
                        isAnalyzing = isAnalyzing && selectedBuild?.id == build.id,
                        onSelect = {
                            selectedBuild = build
                            analysisResult = null
                        },
                        onAnalyze = { onAnalyzeBuild(build) },
                        onViewLogs = { onNavigateToDetail(build) },
                        onApplyFix = { result ->
                            onApplyFix(build, result)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FailedBuildCard(
    build: Build,
    isSelected: Boolean,
    analysisResult: AIAnalysisResult?,
    isAnalyzing: Boolean,
    onSelect: () -> Unit,
    onAnalyze: () -> Unit,
    onViewLogs: () -> Unit,
    onApplyFix: (AIAnalysisResult) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                AnekonColors.BackgroundSecondary
            else
                AnekonColors.BackgroundSecondary.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AnekonColors.Error.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "FALLIDO",
                        color = AnekonColors.Error,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Time ago
                Text(
                    text = formatTimeAgo(build.startedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Build info
            Text(
                text = build.workflowName,
                style = MaterialTheme.typography.titleMedium,
                color = AnekonColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccountTree,
                    contentDescription = null,
                    tint = AnekonColors.TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = build.branch,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.Accent
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    Icons.Default.Commit,
                    contentDescription = null,
                    tint = AnekonColors.TextMuted,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = build.commitSha?.take(7) ?: "?",
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Error message preview
            build.errorMessage?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AnekonColors.BackgroundTertiary)
                        .padding(8.dp)
                ) {
                    Text(
                        text = error.take(100) + if (error.length > 100) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = AnekonColors.Error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Analysis result
            analysisResult?.let { result ->
                Spacer(modifier = Modifier.height(12.dp))
                AnalysisResultCard(result = result)
            }

            // Loading state
            if (isAnalyzing) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AnekonColors.Accent,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Analizando con IA...",
                        style = MaterialTheme.typography.bodySmall,
                        color = AnekonColors.Accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (analysisResult == null && !isAnalyzing) {
                    // Analyze button
                    Button(
                        onClick = onAnalyze,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AnekonColors.Accent,
                            contentColor = AnekonColors.BackgroundPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analizar", fontWeight = FontWeight.SemiBold)
                    }
                }

                // View logs button
                OutlinedButton(
                    onClick = onViewLogs,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AnekonColors.Accent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Description,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver Logs")
                }

                // Apply fix button
                if (analysisResult != null) {
                    Button(
                        onClick = { onApplyFix(analysisResult) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AnekonColors.Success,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.AutoFixHigh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Aplicar Fix")
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalysisResultCard(result: AIAnalysisResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundTertiary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AnekonColors.Accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Análisis de IA",
                    style = MaterialTheme.typography.titleSmall,
                    color = AnekonColors.Accent,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                // Confidence badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(getConfidenceColor(result.confidence).copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${(result.confidence * 100).toInt()}%",
                        color = getConfidenceColor(result.confidence),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Error type
            ResultRow(
                label = "Tipo de error",
                value = result.errorType ?: "",
                valueColor = AnekonColors.Error
            )

            // Error message
            ResultRow(
                label = "Mensaje",
                value = result.errorMessage ?: ""
            )

            // Root cause
            result.causeRoot?.let { cause ->
                ResultRow(
                    label = "Causa raíz",
                    value = cause,
                    valueColor = AnekonColors.Warning
                )
            }

            // Suggested fix
            ResultRow(
                label = "Solución sugerida",
                value = result.suggestedFix ?: "",
                valueColor = AnekonColors.Success
            )

            // Code snippet
            result.codeSnippet?.let { code ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Código:",
                    style = MaterialTheme.typography.labelSmall,
                    color = AnekonColors.TextMuted
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1A1A1A))
                        .padding(8.dp)
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = AnekonColors.Success,
                        fontSize = 11.sp
                    )
                }
            }

            // Provider badge
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    when (result.provider.name) {
                        "MINIMAX_PRO" -> Icons.Default.Star
                        "OPENAI" -> Icons.Default.Psychology
                        "ANTHROPIC" -> Icons.Default.Person
                        else -> Icons.Default.Computer
                    },
                    contentDescription = null,
                    tint = AnekonColors.TextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = result.provider.name.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = AnekonColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun ResultRow(
    label: String,
    value: String,
    valueColor: Color = AnekonColors.TextPrimary
) {
    Column(
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AnekonColors.TextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor
        )
    }
}

private fun getConfidenceColor(confidence: Float): Color {
    return when {
        confidence >= 0.8f -> AnekonColors.Success
        confidence >= 0.5f -> AnekonColors.Warning
        else -> AnekonColors.Error
    }
}

private fun formatTimeAgo(timestamp: Long?): String {
    if (timestamp == null) return "?"
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (1000 * 60)
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Hace un momento"
        minutes < 60 -> "Hace ${minutes}m"
        hours < 24 -> "Hace ${hours}h"
        days < 7 -> "Hace ${days}d"
        else -> "Hace ${days / 7}sem"
    }
}

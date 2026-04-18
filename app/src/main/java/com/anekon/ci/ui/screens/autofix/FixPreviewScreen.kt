package com.anekon.ci.ui.screens.autofix

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anekon.ci.domain.model.AIAnalysisResult
import com.anekon.ci.ui.theme.AnekonColors

/**
 * Pantalla de Vista Previa del Fix - Muestra el diff y permite aplicar cambios
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FixPreviewScreen(
    filePath: String,
    originalContent: String,
    analysisResult: AIAnalysisResult,
    onApplyFix: () -> Unit,
    onCancel: () -> Unit,
    isApplying: Boolean
) {
    var commitMessage by remember {
        mutableStateOf("fix: AutoFix - ${analysisResult.errorType ?: "build error"}")
    }
    var showConfirmDialog by remember { mutableStateOf(false) }

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
                        text = "Vista Previa del Fix",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AnekonColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = filePath,
                        style = MaterialTheme.typography.bodySmall,
                        color = AnekonColors.TextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onCancel) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = AnekonColors.TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = AnekonColors.BackgroundPrimary
            )
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Analysis summary card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AnekonColors.BackgroundSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = null,
                                tint = AnekonColors.Accent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Análisis de IA",
                                style = MaterialTheme.typography.titleMedium,
                                color = AnekonColors.Accent,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            // Confidence badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(getConfidenceColor(analysisResult.confidence).copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${(analysisResult.confidence * 100).toInt()}% confianza",
                                    color = getConfidenceColor(analysisResult.confidence),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Error type
                        InfoRow(
                            label = "Tipo de error",
                            value = analysisResult.errorType ?: "Desconocido",
                            valueColor = AnekonColors.Error
                        )

                        // Error message
                        analysisResult.errorMessage?.let { msg ->
                            InfoRow(label = "Mensaje", value = msg)
                        }

                        // Root cause
                        analysisResult.causeRoot?.let { cause ->
                            InfoRow(
                                label = "Causa raíz",
                                value = cause,
                                valueColor = AnekonColors.Warning
                            )
                        }
                    }
                }
            }

            // Diff view
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AnekonColors.BackgroundSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Difference,
                                contentDescription = null,
                                tint = AnekonColors.Accent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Cambios propuestos",
                                style = MaterialTheme.typography.titleMedium,
                                color = AnekonColors.Accent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Diff display
                        DiffView(
                            originalContent = originalContent,
                            fixContent = analysisResult.codeSnippet ?: "",
                            suggestedFix = analysisResult.suggestedFix ?: ""
                        )
                    }
                }
            }

            // Suggested fix instructions
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AnekonColors.BackgroundSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = AnekonColors.Success,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Pasos sugeridos",
                                style = MaterialTheme.typography.titleMedium,
                                color = AnekonColors.Success,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = analysisResult.suggestedFix ?: "No hay pasos sugeridos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AnekonColors.TextPrimary
                        )
                    }
                }
            }

            // Commit message input
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AnekonColors.BackgroundSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Mensaje de commit",
                            style = MaterialTheme.typography.titleSmall,
                            color = AnekonColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = commitMessage,
                            onValueChange = { commitMessage = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AnekonColors.Accent,
                                unfocusedBorderColor = AnekonColors.BackgroundTertiary,
                                focusedTextColor = AnekonColors.TextPrimary,
                                unfocusedTextColor = AnekonColors.TextPrimary,
                                cursorColor = AnekonColors.Accent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Bottom action bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AnekonColors.BackgroundSecondary,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AnekonColors.TextMuted
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar")
                }

                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier.weight(1f),
                    enabled = !isApplying && analysisResult.codeSnippet != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AnekonColors.Success,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isApplying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Aplicar Fix")
                }
            }
        }
    }

    // Confirmation dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = AnekonColors.BackgroundSecondary,
            title = {
                Text(
                    text = "¿Aplicar el fix?",
                    color = AnekonColors.TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Searán los siguientes cambios:",
                        color = AnekonColors.TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = filePath,
                        color = AnekonColors.Accent,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Commit: \"$commitMessage\"",
                        color = AnekonColors.TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onApplyFix()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AnekonColors.Success
                    )
                ) {
                    Text("Sí, aplicar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = AnekonColors.TextMuted
                    )
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun InfoRow(
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

@Composable
private fun DiffView(
    originalContent: String,
    fixContent: String,
    suggestedFix: String
) {
    val lines = fixContent.lines()
    val hasDiffMarkers = lines.any { it.startsWith("+") || it.startsWith("-") || it.startsWith(" ") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A1A))
            .horizontalScroll(rememberScrollState())
    ) {
        if (hasDiffMarkers) {
            // Show as unified diff
            lines.forEach { line ->
                val color = when {
                    line.startsWith("+") && !line.startsWith("+++") -> AnekonColors.Success
                    line.startsWith("-") && !line.startsWith("---") -> AnekonColors.Error
                    line.startsWith("@@") -> AnekonColors.Accent
                    else -> AnekonColors.TextMuted
                }
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = color,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        } else {
            // Show as code block
            Text(
                text = fixContent,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                ),
                color = AnekonColors.Success,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

private fun getConfidenceColor(confidence: Float): Color {
    return when {
        confidence >= 0.8f -> AnekonColors.Success
        confidence >= 0.5f -> AnekonColors.Warning
        else -> AnekonColors.Error
    }
}

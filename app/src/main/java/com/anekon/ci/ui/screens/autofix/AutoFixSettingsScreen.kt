@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.anekon.ci.ui.screens.autofix

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anekon.ci.domain.model.AIProviderType
import com.anekon.ci.domain.usecase.AutoFixSettings
import com.anekon.ci.ui.theme.AnekonColors

/**
 * Pantalla de configuración de AutoFix por proyecto
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoFixSettingsScreen(
    projectName: String,
    currentSettings: AutoFixSettings,
    onSave: (AutoFixSettings) -> Unit,
    onBack: () -> Unit
) {
    var autoAnalyze by remember { mutableStateOf(currentSettings.autoAnalyze) }
    var autoApplyFix by remember { mutableStateOf(currentSettings.autoApplyFix) }
    var maxRetries by remember { mutableStateOf(currentSettings.maxRetries) }
    var notifyOnFailure by remember { mutableStateOf(currentSettings.notifyOnFailure) }
    var notifyOnFix by remember { mutableStateOf(currentSettings.notifyOnFix) }
    var selectedProvider by remember { mutableStateOf(currentSettings.selectedProvider) }
    var preferredBranches by remember { mutableStateOf(currentSettings.preferredBranches.joinToString(", ")) }
    var showSaveDialog by remember { mutableStateOf(false) }

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
                        text = "AutoFix Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        color = AnekonColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = projectName,
                        style = MaterialTheme.typography.bodySmall,
                        color = AnekonColors.TextMuted
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
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
            // AI Provider Section
            item {
                SettingsSection(title = "Proveedor de IA")
            }

            item {
                ProviderSelector(
                    selectedProvider = selectedProvider,
                    onProviderSelected = { selectedProvider = it }
                )
            }

            // Automation Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSection(title = "Automatización")
            }

            item {
                SettingsToggleItem(
                    icon = Icons.Default.Psychology,
                    title = "Auto-Análisis",
                    subtitle = "Analizar automáticamente cuando falle un build",
                    checked = autoAnalyze,
                    onCheckedChange = { autoAnalyze = it }
                )
            }

            item {
                SettingsToggleItem(
                    icon = Icons.Default.AutoFixHigh,
                    title = "Auto-Fix",
                    subtitle = "Aplicar fixes automáticamente (requiere alta confianza)",
                    checked = autoApplyFix,
                    onCheckedChange = { autoApplyFix = it }
                )
            }

            item {
                SettingsSliderItem(
                    icon = Icons.Default.Replay,
                    title = "Reintentos máximos",
                    value = maxRetries,
                    onValueChange = { maxRetries = it },
                    valueRange = 1..5,
                    valueLabel = "$maxRetries"
                )
            }

            // Notifications Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSection(title = "Notificaciones")
            }

            item {
                SettingsToggleItem(
                    icon = Icons.Default.Error,
                    title = "Notificar fallos",
                    subtitle = "Recibir alerta cuando falle un build",
                    checked = notifyOnFailure,
                    onCheckedChange = { notifyOnFailure = it }
                )
            }

            item {
                SettingsToggleItem(
                    icon = Icons.Default.CheckCircle,
                    title = "Notificar fixes",
                    subtitle = "Recibir alerta cuando se aplique un fix",
                    checked = notifyOnFix,
                    onCheckedChange = { notifyOnFix = it }
                )
            }

            // Branch Filter Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsSection(title = "Filtro de ramas")
            }

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
                                Icons.Default.AccountTree,
                                contentDescription = null,
                                tint = AnekonColors.Accent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Ramas preferidas",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AnekonColors.TextPrimary
                                )
                                Text(
                                    text = "Separadas por coma (vacío = todas)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AnekonColors.TextMuted
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = preferredBranches,
                            onValueChange = { preferredBranches = it },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AnekonColors.Accent,
                                unfocusedBorderColor = AnekonColors.BackgroundTertiary,
                                focusedTextColor = AnekonColors.TextPrimary,
                                unfocusedTextColor = AnekonColors.TextPrimary,
                                cursorColor = AnekonColors.Accent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            placeholder = {
                                Text(
                                    text = "main, develop, feature/*",
                                    color = AnekonColors.TextMuted
                                )
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Save button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AnekonColors.BackgroundSecondary,
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = {
                    val newSettings = AutoFixSettings(
                        autoAnalyze = autoAnalyze,
                        autoApplyFix = autoApplyFix,
                        maxRetries = maxRetries,
                        notifyOnFailure = notifyOnFailure,
                        notifyOnFix = notifyOnFix,
                        selectedProvider = selectedProvider,
                        preferredBranches = preferredBranches.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        excludedWorkflows = currentSettings.excludedWorkflows
                    )
                    onSave(newSettings)
                    showSaveDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AnekonColors.Accent,
                    contentColor = AnekonColors.BackgroundPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Guardar Configuración", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // Success dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = AnekonColors.BackgroundSecondary,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AnekonColors.Success
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardado", color = AnekonColors.TextPrimary)
                }
            },
            text = {
                Text(
                    "Configuración de AutoFix guardada correctamente",
                    color = AnekonColors.TextMuted
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AnekonColors.Success
                    )
                ) {
                    Text("Aceptar")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = AnekonColors.Accent,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsToggleItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AnekonColors.Accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AnekonColors.TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.TextMuted
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AnekonColors.Accent,
                    checkedTrackColor = AnekonColors.Accent.copy(alpha = 0.3f),
                    uncheckedThumbColor = AnekonColors.TextMuted,
                    uncheckedTrackColor = AnekonColors.BackgroundTertiary
                )
            )
        }
    }
}

@Composable
private fun SettingsSliderItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    valueLabel: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AnekonColors.Accent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = AnekonColors.TextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AnekonColors.Accent.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = valueLabel,
                        color = AnekonColors.Accent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
                steps = valueRange.last - valueRange.first - 1,
                colors = SliderDefaults.colors(
                    thumbColor = AnekonColors.Accent,
                    activeTrackColor = AnekonColors.Accent,
                    inactiveTrackColor = AnekonColors.BackgroundTertiary
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProviderSelector(
    selectedProvider: AIProviderType,
    onProviderSelected: (AIProviderType) -> Unit
) {
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
                    text = "Proveedor de IA para análisis",
                    style = MaterialTheme.typography.titleMedium,
                    color = AnekonColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Provider options
            AIProviderType.values().forEach { provider ->
                ProviderOption(
                    provider = provider,
                    isSelected = selectedProvider == provider,
                    onClick = { onProviderSelected(provider) }
                )
                if (provider != AIProviderType.values().last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ProviderOption(
    provider: AIProviderType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AnekonColors.Accent.copy(alpha = 0.2f)
            else AnekonColors.BackgroundTertiary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (provider) {
                    AIProviderType.MINIMAX_PRO -> Icons.Default.Star
                    AIProviderType.MINIMAX_FREE -> Icons.Default.Star
                    AIProviderType.OPENAI -> Icons.Default.Psychology
                    AIProviderType.ANTHROPIC -> Icons.Default.Person
                    AIProviderType.GEMINI -> Icons.Default.AutoAwesome
                    AIProviderType.LOCAL -> Icons.Default.Computer
                },
                contentDescription = null,
                tint = if (isSelected) AnekonColors.Accent else AnekonColors.Accent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = when (provider) {
                            AIProviderType.MINIMAX_PRO -> "MiniMax Pro"
                            AIProviderType.MINIMAX_FREE -> "MiniMax Free"
                            AIProviderType.OPENAI -> "OpenAI"
                            AIProviderType.ANTHROPIC -> "Anthropic Claude"
                            AIProviderType.GEMINI -> "Google Gemini"
                            AIProviderType.LOCAL -> "Local / Ollama"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = AnekonColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    if (provider == AIProviderType.LOCAL || provider == AIProviderType.OPENAI || provider == AIProviderType.MINIMAX_FREE) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AnekonColors.Success.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "FREE",
                                color = AnekonColors.Success,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                Text(
                    text = when (provider) {
                        AIProviderType.MINIMAX_PRO -> "Modelos avanzados - Pago"
                        AIProviderType.MINIMAX_FREE -> "MiniMax - Versión gratuita"
                        AIProviderType.OPENAI -> "GPT-3.5/4 - Pruebas"
                        AIProviderType.ANTHROPIC -> "Claude - Pago"
                        AIProviderType.GEMINI -> "Gemini Pro/Flash"
                        AIProviderType.LOCAL -> "Para estrés"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.TextMuted
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Seleccionado",
                    tint = AnekonColors.Accent
                )
            }
        }
    }
}

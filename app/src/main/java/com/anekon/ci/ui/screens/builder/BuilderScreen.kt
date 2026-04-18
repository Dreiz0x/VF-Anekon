package com.anekon.ci.ui.screens.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.anekon.ci.ui.theme.AnekonColors

@Composable
fun BuilderScreen() {
    var currentStep by remember { mutableIntStateOf(0) }
    var appName by remember { mutableStateOf("") }
    var appDescription by remember { mutableStateOf("") }
    var selectedPlatforms by remember { mutableStateOf(setOf<String>()) }
    var selectedFeatures by remember { mutableStateOf(setOf<String>()) }
    var isGenerating by remember { mutableStateOf(false) }

    val steps = listOf("Nombre", "Plataforma", "Features", "Código")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AnekonColors.BackgroundPrimary)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Constructor",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AnekonColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Crea tu app desde cero con IA",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AnekonColors.TextMuted
                    )
                }
            }
        }

        item {
            // Progress Steps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { index, step ->
                    StepIndicator(
                        step = index + 1,
                        label = step,
                        isActive = currentStep >= index,
                        isCurrent = currentStep == index
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (currentStep) {
            0 -> {
                item {
                    Text(
                        text = "¿Cómo se llamará tu app?",
                        style = MaterialTheme.typography.titleLarge,
                        color = AnekonColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = appName,
                        onValueChange = { appName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nombre de la app") },
                        placeholder = { Text("Ej: MiApp, TaskMaster...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AnekonColors.Accent,
                            unfocusedBorderColor = AnekonColors.TextMuted,
                            focusedLabelColor = AnekonColors.Accent,
                            unfocusedLabelColor = AnekonColors.TextMuted,
                            cursorColor = AnekonColors.Accent,
                            focusedTextColor = AnekonColors.TextPrimary,
                            unfocusedTextColor = AnekonColors.TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Describe brevemente tu app",
                        style = MaterialTheme.typography.titleMedium,
                        color = AnekonColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = appDescription,
                        onValueChange = { appDescription = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = { Text("Ej: Una app para gestionar tareas diarias...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AnekonColors.Accent,
                            unfocusedBorderColor = AnekonColors.TextMuted,
                            cursorColor = AnekonColors.Accent,
                            focusedTextColor = AnekonColors.TextPrimary,
                            unfocusedTextColor = AnekonColors.TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            1 -> {
                item {
                    Text(
                        text = "¿Para qué plataforma?",
                        style = MaterialTheme.typography.titleLarge,
                        color = AnekonColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(platforms) { platform ->
                            PlatformCard(
                                platform = platform,
                                isSelected = selectedPlatforms.contains(platform.id),
                                onClick = {
                                    selectedPlatforms = if (selectedPlatforms.contains(platform.id)) {
                                        selectedPlatforms - platform.id
                                    } else {
                                        selectedPlatforms + platform.id
                                    }
                                }
                            )
                        }
                    }
                }
            }

            2 -> {
                item {
                    Text(
                        text = "¿Qué funcionalidades necesitas?",
                        style = MaterialTheme.typography.titleLarge,
                        color = AnekonColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                items(features.chunked(2)) { rowFeatures ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowFeatures.forEach { feature ->
                            FeatureChip(
                                modifier = Modifier.weight(1f),
                                feature = feature,
                                isSelected = selectedFeatures.contains(feature.id),
                                onClick = {
                                    selectedFeatures = if (selectedFeatures.contains(feature.id)) {
                                        selectedFeatures - feature.id
                                    } else {
                                        selectedFeatures + feature.id
                                    }
                                }
                            )
                        }
                        if (rowFeatures.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            3 -> {
                item {
                    Text(
                        text = "Generando código...",
                        style = MaterialTheme.typography.titleLarge,
                        color = AnekonColors.TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = AnekonColors.BackgroundSecondary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    color = AnekonColors.Accent,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Analizando requerimientos...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AnekonColors.TextSecondary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = AnekonColors.Accent,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Listo para generar",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AnekonColors.TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 0) {
                    TextButton(
                        onClick = { currentStep-- },
                        colors = ButtonDefaults.textButtonColors(contentColor = AnekonColors.TextMuted)
                    ) {
                        Text("Anterior")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < 3) {
                            currentStep++
                        } else {
                            isGenerating = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AnekonColors.Accent,
                        contentColor = AnekonColors.BackgroundPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = when (currentStep) {
                        0 -> appName.isNotBlank()
                        1 -> selectedPlatforms.isNotEmpty()
                        2 -> selectedFeatures.isNotEmpty()
                        else -> !isGenerating
                    }
                ) {
                    Text(if (currentStep < 3) "Siguiente" else "Generar App")
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun StepIndicator(
    step: Int,
    label: String,
    isActive: Boolean,
    isCurrent: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    when {
                        isCurrent -> AnekonColors.Accent
                        isActive -> AnekonColors.Accent
                        else -> AnekonColors.BackgroundSecondary
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isActive && !isCurrent) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = AnekonColors.BackgroundPrimary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrent) AnekonColors.BackgroundPrimary
                            else AnekonColors.TextMuted,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) AnekonColors.TextPrimary
                    else AnekonColors.TextMuted
        )
    }
}

@Composable
private fun PlatformCard(
    platform: Platform,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(
                    2.dp,
                    AnekonColors.Accent,
                    RoundedCornerShape(16.dp)
                ) else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AnekonColors.BackgroundTertiary
                            else AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = platform.icon,
                contentDescription = null,
                tint = if (isSelected) AnekonColors.Accent else AnekonColors.Accent,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = platform.name,
                style = MaterialTheme.typography.titleMedium,
                color = AnekonColors.TextPrimary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FeatureChip(
    modifier: Modifier = Modifier,
    feature: Feature,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AnekonColors.Accent.copy(alpha = 0.2f)
                            else AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle
                             else Icons.Default.Circle,
                contentDescription = null,
                tint = if (isSelected) AnekonColors.Accent
                       else AnekonColors.TextMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = feature.name,
                style = MaterialTheme.typography.bodyMedium,
                color = AnekonColors.TextPrimary
            )
        }
    }
}

data class Platform(
    val id: String,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

data class Feature(
    val id: String,
    val name: String
)

private val platforms = listOf(
    Platform("android", "Android", Icons.Default.Android),
    Platform("ios", "iOS", Icons.Default.PhoneIphone),
    Platform("web", "Web", Icons.Default.Web),
    Platform("api", "API", Icons.Default.Api)
)

private val features = listOf(
    Feature("auth", "Autenticación"),
    Feature("database", "Base de Datos"),
    Feature("api", "API REST"),
    Feature("push", "Notificaciones Push"),
    Feature("analytics", "Analíticas"),
    Feature("payments", "Pagos"),
    Feature("chat", "Chat en Vivo"),
    Feature("offline", "Modo Offline")
)

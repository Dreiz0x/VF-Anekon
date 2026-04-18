@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.anekon.ci.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.anekon.ci.ui.theme.AnekonColors

/**
 * HomeScreen - Premium Minimal Design
 * Actualizado con la paleta de colores Anekon:
 * - Fondo crema #F2F2EF
 * - Naranja #F4811F para acentos
 * - Cards blancas con bordes sutiles
 * - Tipografía Serif para branding
 */
@Composable
fun HomeScreen(
    onNavigateToProjectCreator: () -> Unit = {},
    onNavigateToRepoAnalyzer: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Hoy", "Esta Semana", "Este Mes")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AnekonColors.BackgroundPrimary),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ============ HEADER ============
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bienvenido",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AnekonColors.TextSecondary,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Anekon",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AnekonColors.TextPrimary
                    )
                }
                IconButton(onClick = { /* Notifications */ }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificaciones",
                        tint = AnekonColors.TextSecondary
                    )
                }
            }
        }

        // ============ TABS ============
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AnekonColors.BackgroundSecondary)
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    tabs.forEachIndexed { index, tab ->
                        TabButton(
                            text = tab,
                            selected = selectedTab == index,
                            onClick = { selectedTab = index }
                        )
                    }
                }
            }
        }

        // ============ STATS CARDS ============
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Builds",
                    value = "24",
                    change = "+12%",
                    isPositive = true,
                    icon = Icons.Default.Build
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "Errores",
                    value = "3",
                    change = "-25%",
                    isPositive = true,
                    icon = Icons.Default.Error
                )
            }
        }

        item {
            StatCard(
                modifier = Modifier.fillMaxWidth(),
                title = "Tiempo Promedio",
                value = "4m 32s",
                change = "-8s",
                isPositive = true,
                icon = Icons.Default.Schedule
            )
        }

        // ============ HERRAMIENTAS SECTION ============
        item {
            Text(
                text = "Herramientas",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AnekonColors.TextPrimary
            )
        }

        // Crear Nuevo Proyecto
        item {
            ActionCard(
                title = "Crear Nuevo Proyecto",
                subtitle = "Genera estructura Android con IA",
                icon = Icons.Default.Add,
                accentColor = AnekonColors.Accent,
                onClick = onNavigateToProjectCreator
            )
        }

        // Analizar Repositorio
        item {
            ActionCard(
                title = "Analizar Repositorio",
                subtitle = "Revisa y repara errores con IA",
                icon = Icons.Default.FolderOpen,
                accentColor = AnekonColors.Success,
                onClick = onNavigateToRepoAnalyzer
            )
        }

        // ============ ACTIVIDAD RECIENTE ============
        item {
            Text(
                text = "Actividad Reciente",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AnekonColors.TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(recentActivity) { activity ->
            ActivityCard(activity = activity)
        }

        // ============ BOTTOM SPACING ============
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) AnekonColors.Accent else Color.Transparent,
        label = "tabBackground"
    )

    TextButton(
        onClick = onClick,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
            ),
            color = if (selected) AnekonColors.BackgroundSecondary else AnekonColors.TextMuted
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    change: String,
    isPositive: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AnekonColors.Accent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.TextSecondary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = AnekonColors.TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (isPositive) AnekonColors.Success else AnekonColors.Error,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = change,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPositive) AnekonColors.Success else AnekonColors.Error
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = accentColor.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AnekonColors.TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.TextSecondary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AnekonColors.TextMuted
            )
        }
    }
}

@Composable
private fun ActivityCard(activity: Activity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (activity.status) {
                            ActivityStatus.SUCCESS -> AnekonColors.Success.copy(alpha = 0.15f)
                            ActivityStatus.FAILED -> AnekonColors.Error.copy(alpha = 0.15f)
                            ActivityStatus.RUNNING -> AnekonColors.Accent.copy(alpha = 0.15f)
                            ActivityStatus.PENDING -> AnekonColors.TextMuted.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (activity.status) {
                        ActivityStatus.SUCCESS -> Icons.Default.CheckCircle
                        ActivityStatus.FAILED -> Icons.Default.Cancel
                        ActivityStatus.RUNNING -> Icons.Default.PlayCircle
                        ActivityStatus.PENDING -> Icons.Default.Schedule
                    },
                    contentDescription = null,
                    tint = when (activity.status) {
                        ActivityStatus.SUCCESS -> AnekonColors.Success
                        ActivityStatus.FAILED -> AnekonColors.Error
                        ActivityStatus.RUNNING -> AnekonColors.Accent
                        ActivityStatus.PENDING -> AnekonColors.TextMuted
                    },
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = AnekonColors.TextPrimary
                )
                Text(
                    text = activity.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AnekonColors.TextMuted
                )
            }

            Text(
                text = activity.time,
                style = MaterialTheme.typography.bodySmall,
                color = AnekonColors.TextMuted
            )
        }
    }
}

enum class ActivityStatus {
    SUCCESS, FAILED, RUNNING, PENDING
}

data class Activity(
    val title: String,
    val subtitle: String,
    val time: String,
    val status: ActivityStatus
)

private val recentActivity = listOf(
    Activity(
        title = "Build MiApp-debug.apk",
        subtitle = "main • #142",
        time = "Hace 5m",
        status = ActivityStatus.SUCCESS
    ),
    Activity(
        title = "Lint Check",
        subtitle = "develop • #89",
        time = "Hace 23m",
        status = ActivityStatus.FAILED
    ),
    Activity(
        title = "Unit Tests",
        subtitle = "feature/new-ui • #34",
        time = "Hace 1h",
        status = ActivityStatus.SUCCESS
    ),
    Activity(
        title = "Deploy to Play Store",
        subtitle = "release/v1.2.0",
        time = "Hace 2h",
        status = ActivityStatus.RUNNING
    )
)
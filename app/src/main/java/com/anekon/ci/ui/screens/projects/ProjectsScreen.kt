@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.anekon.ci.ui.screens.projects

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anekon.ci.ui.theme.AnekonColors

@Composable
fun ProjectsScreen() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    val tabs = listOf("Todos", "Android", "Web", "API")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AnekonColors.BackgroundPrimary)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        text = "Proyectos",
                        style = MaterialTheme.typography.headlineLarge,
                        color = AnekonColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${projects.size} proyectos",
                        style = MaterialTheme.typography.bodySmall,
                        color = AnekonColors.TextMuted
                    )
                }
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = AnekonColors.Amber,
                    contentColor = AnekonColors.BackgroundPrimary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar proyecto"
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Filter tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AnekonColors.BackgroundSecondary)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = {
                            Text(
                                text = tab,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AnekonColors.Amber,
                            selectedLabelColor = AnekonColors.BackgroundPrimary,
                            containerColor = AnekonColors.BackgroundSecondary,
                            labelColor = AnekonColors.TextMuted
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        items(projects) { project ->
            ProjectCard(
                project = project,
                onClick = { /* Navigate to detail */ }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type ->
                // Add project logic
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = AnekonColors.BackgroundSecondary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(project.platformColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = project.platformIcon,
                            contentDescription = null,
                            tint = AnekonColors.TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = project.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = AnekonColors.TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = project.type,
                            style = MaterialTheme.typography.bodySmall,
                            color = AnekonColors.TextMuted
                        )
                    }
                }
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            when (project.status) {
                                ProjectStatus.HEALTHY -> AnekonColors.Success
                                ProjectStatus.BUILDING -> AnekonColors.Amber
                                ProjectStatus.ERROR -> AnekonColors.Error
                                ProjectStatus.IDLE -> AnekonColors.TextMuted
                            }
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ProjectStat(
                    icon = Icons.Default.PlayCircle,
                    value = project.buildCount.toString(),
                    label = "Builds"
                )
                ProjectStat(
                    icon = Icons.Default.CheckCircle,
                    value = project.successRate,
                    label = "Éxito"
                )
                ProjectStat(
                    icon = Icons.Default.Schedule,
                    value = project.lastBuild,
                    label = "Último"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Latest workflow
            if (project.latestWorkflow != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AnekonColors.BackgroundTertiary)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (project.latestWorkflow.status) {
                            WorkflowStatus.SUCCESS -> Icons.Default.CheckCircle
                            WorkflowStatus.FAILED -> Icons.Default.Cancel
                            WorkflowStatus.RUNNING -> Icons.Default.PlayCircle
                            else -> Icons.Default.Schedule
                        },
                        contentDescription = null,
                        tint = when (project.latestWorkflow.status) {
                            WorkflowStatus.SUCCESS -> AnekonColors.Success
                            WorkflowStatus.FAILED -> AnekonColors.Error
                            WorkflowStatus.RUNNING -> AnekonColors.Amber
                            else -> AnekonColors.TextMuted
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = project.latestWorkflow.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = AnekonColors.TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = project.latestWorkflow.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = AnekonColors.TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AnekonColors.Teal,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = AnekonColors.TextPrimary,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = AnekonColors.TextMuted
        )
    }
}

@Composable
private fun AddProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Android") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AnekonColors.BackgroundSecondary,
        title = {
            Text(
                text = "Nuevo Proyecto",
                color = AnekonColors.TextPrimary
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del proyecto") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AnekonColors.Amber,
                        unfocusedBorderColor = AnekonColors.TextMuted,
                        focusedLabelColor = AnekonColors.Amber,
                        unfocusedLabelColor = AnekonColors.TextMuted,
                        cursorColor = AnekonColors.Amber,
                        focusedTextColor = AnekonColors.TextPrimary,
                        unfocusedTextColor = AnekonColors.TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tipo de proyecto",
                    style = MaterialTheme.typography.bodyMedium,
                    color = AnekonColors.TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Android", "Web", "API").forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AnekonColors.Amber,
                                selectedLabelColor = AnekonColors.BackgroundPrimary,
                                containerColor = AnekonColors.BackgroundTertiary,
                                labelColor = AnekonColors.TextSecondary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedType) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = AnekonColors.Amber,
                    contentColor = AnekonColors.BackgroundPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = AnekonColors.TextMuted
                )
            ) {
                Text("Cancelar")
            }
        }
    )
}

enum class ProjectStatus {
    HEALTHY, BUILDING, ERROR, IDLE
}

enum class WorkflowStatus {
    SUCCESS, FAILED, RUNNING, PENDING
}

data class Project(
    val id: String,
    val name: String,
    val type: String,
    val platformIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val platformColor: androidx.compose.ui.graphics.Color,
    val status: ProjectStatus,
    val buildCount: Int,
    val successRate: String,
    val lastBuild: String,
    val latestWorkflow: Workflow? = null
)

data class Workflow(
    val id: String,
    val name: String,
    val status: WorkflowStatus,
    val time: String
)

private val projects = listOf(
    Project(
        id = "1",
        name = "MiApp",
        type = "Android",
        platformIcon = Icons.Default.Android,
        platformColor = AnekonColors.Success.copy(alpha = 0.2f),
        status = ProjectStatus.HEALTHY,
        buildCount = 142,
        successRate = "98%",
        lastBuild = "5m",
        latestWorkflow = Workflow("w1", "Build Debug APK", WorkflowStatus.SUCCESS, "Hace 5m")
    ),
    Project(
        id = "2",
        name = "TaskMaster",
        type = "Web",
        platformIcon = Icons.Default.Web,
        platformColor = AnekonColors.Info.copy(alpha = 0.2f),
        status = ProjectStatus.BUILDING,
        buildCount = 89,
        successRate = "95%",
        lastBuild = "En curso",
        latestWorkflow = Workflow("w2", "Deploy to Vercel", WorkflowStatus.RUNNING, "En curso")
    ),
    Project(
        id = "3",
        name = "BackendAPI",
        type = "API",
        platformIcon = Icons.Default.Api,
        platformColor = AnekonColors.Warning.copy(alpha = 0.2f),
        status = ProjectStatus.ERROR,
        buildCount = 56,
        successRate = "82%",
        lastBuild = "1h",
        latestWorkflow = Workflow("w3", "Run Tests", WorkflowStatus.FAILED, "Hace 1h")
    )
)

package com.anekon.ci.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Builder : Screen("builder")
    object Projects : Screen("projects")
    object Settings : Screen("settings")
    object AutoFix : Screen("autofix")
    object ProjectDetail : Screen("project/{projectId}") {
        fun createRoute(projectId: String) = "project/$projectId"
    }
    object WorkflowDetail : Screen("workflow/{workflowId}") {
        fun createRoute(workflowId: String) = "workflow/$workflowId"
    }
    object Logs : Screen("logs/{runId}") {
        fun createRoute(runId: String) = "logs/$runId"
    }
    object ProjectCreator : Screen("project-creator")
    object RepoAnalyzer : Screen("repo-analyzer")
}

data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(
        title = "Inicio",
        route = Screen.Home.route,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    BottomNavItem(
        title = "AutoFix",
        route = Screen.AutoFix.route,
        selectedIcon = Icons.Filled.AutoFixHigh,
        unselectedIcon = Icons.Outlined.AutoFixHigh
    ),
    BottomNavItem(
        title = "Constructor",
        route = Screen.Builder.route,
        selectedIcon = Icons.Filled.Build,
        unselectedIcon = Icons.Outlined.Build
    ),
    BottomNavItem(
        title = "Repos",
        route = Screen.RepoAnalyzer.route,
        selectedIcon = Icons.Filled.FolderOpen,
        unselectedIcon = Icons.Outlined.FolderOpen
    ),
    BottomNavItem(
        title = "Ajustes",
        route = Screen.Settings.route,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)

package com.anekon.ci.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.anekon.ci.ui.screens.autofix.AutoFixScreen
import com.anekon.ci.ui.screens.builder.BuilderScreen
import com.anekon.ci.ui.screens.home.HomeScreen
import com.anekon.ci.ui.screens.projectcreator.ProjectCreatorScreen
import com.anekon.ci.ui.screens.projects.ProjectsScreen
import com.anekon.ci.ui.screens.repoanalyzer.RepoAnalyzerScreen
import com.anekon.ci.ui.screens.settings.SettingsScreen
import com.anekon.ci.ui.screens.splash.SplashScreen
import com.anekon.ci.ui.theme.AnekonColors

@Composable
fun AnekonNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Rutas que NO muestran bottom nav
    val hideBottomNavRoutes = listOf(
        Screen.Splash.route,
        Screen.ProjectCreator.route,
        Screen.RepoAnalyzer.route
    )

    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route
        ) {
            // Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToProjectCreator = { navController.navigate(Screen.ProjectCreator.route) },
                    onNavigateToRepoAnalyzer = { navController.navigate(Screen.RepoAnalyzer.route) }
                )
            }

            // AutoFix
            composable(Screen.AutoFix.route) {
                AutoFixScreen(
                    failedBuilds = emptyList(),
                    isLoading = false,
                    onAnalyzeBuild = {},
                    onApplyFix = { _, _ -> },
                    onNavigateToDetail = {},
                    onRefresh = {}
                )
            }

            // Builder
            composable(Screen.Builder.route) {
                BuilderScreen()
            }

            // Projects
            composable(Screen.Projects.route) {
                ProjectsScreen()
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen()
            }

            // ProjectCreator
            composable(Screen.ProjectCreator.route) {
                ProjectCreatorScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // RepoAnalyzer
            composable(Screen.RepoAnalyzer.route) {
                RepoAnalyzerScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // Bottom Navigation Bar
        if (currentRoute !in hideBottomNavRoutes) {
            AnekonBottomNav(
                navController = navController,
                currentRoute = currentRoute
            )
        }
    }
}

@Composable
private fun AnekonBottomNav(
    navController: NavHostController,
    currentRoute: String?
) {
    NavigationBar(
        containerColor = AnekonColors.BackgroundSecondary,
        contentColor = AnekonColors.TextPrimary,
        tonalElevation = 8.dp
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AnekonColors.Accent,
                    selectedTextColor = AnekonColors.Accent,
                    unselectedIconColor = AnekonColors.TextMuted,
                    unselectedTextColor = AnekonColors.TextMuted,
                    indicatorColor = AnekonColors.Accent.copy(alpha = 0.15f)
                )
            )
        }
    }
}
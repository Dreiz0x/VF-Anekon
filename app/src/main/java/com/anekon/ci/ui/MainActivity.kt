package com.anekon.ci.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.anekon.ci.ui.navigation.AnekonNavGraph
import com.anekon.ci.ui.theme.AnekonTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnekonTheme {
                AnekonApp()
            }
        }
    }
}

@Composable
fun AnekonApp() {
    val navController = rememberNavController()

    // AnekonNavGraph ahora maneja la navegación interna
    // incluyendo el bottom nav para las rutas principales
    AnekonNavGraph(
        navController = navController,
        modifier = Modifier.fillMaxSize()
    )
}
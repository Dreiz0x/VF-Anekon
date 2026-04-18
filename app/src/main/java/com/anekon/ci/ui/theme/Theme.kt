package com.anekon.ci.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Anekon Theme - Premium Minimalist Light Design
 *
 * Paleta inspirada en la pantalla de bienvenida:
 * - Fondo crema claro #F2F2EF
 * - Naranja vibrante #F4811F para acentos
 * - Texto oscuro #1A1A1A
 * - Cards blancas con bordes sutiles
 */
private val AnekonColorScheme = lightColorScheme(
    // Primary: Naranja vibrante
    primary = AnekonColors.Accent,
    onPrimary = AnekonColors.BackgroundSecondary,
    primaryContainer = AnekonColors.AccentLight,
    onPrimaryContainer = AnekonColors.TextPrimary,

    // Secondary: Gris medio
    secondary = AnekonColors.TextSecondary,
    onSecondary = AnekonColors.BackgroundSecondary,
    secondaryContainer = AnekonColors.BackgroundTertiary,
    onSecondaryContainer = AnekonColors.TextPrimary,

    // Tertiary: Verde éxito
    tertiary = AnekonColors.Success,
    onTertiary = AnekonColors.BackgroundSecondary,
    tertiaryContainer = AnekonColors.SuccessLight,
    onTertiaryContainer = AnekonColors.TextPrimary,

    // Background: Crema claro
    background = AnekonColors.BackgroundPrimary,
    onBackground = AnekonColors.TextPrimary,

    // Surface: Blanco puro para cards
    surface = AnekonColors.BackgroundSecondary,
    onSurface = AnekonColors.TextPrimary,
    surfaceVariant = AnekonColors.BackgroundTertiary,
    onSurfaceVariant = AnekonColors.TextSecondary,

    // Error: Rojo
    error = AnekonColors.Error,
    onError = AnekonColors.BackgroundSecondary,
    errorContainer = AnekonColors.ErrorLight,
    onErrorContainer = AnekonColors.TextPrimary,

    // Outline/Borders
    outline = AnekonColors.Border,
    outlineVariant = AnekonColors.BorderLight,

    // Inverse (para elementos sobre fondos oscuros)
    inverseSurface = AnekonColors.TextPrimary,
    inverseOnSurface = AnekonColors.BackgroundSecondary,
    inversePrimary = AnekonColors.AccentLight
)

@Composable
fun AnekonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Anekon usa diseño claro por defecto (premium minimal)
    // Si el sistema está en dark mode, usamos un scheme adaptado
    val colorScheme = if (darkTheme) {
        AnekonColorScheme.copy(
            background = AnekonColors.TextPrimary,
            onBackground = AnekonColors.BackgroundSecondary,
            surface = AnekonColors.TextSecondary,
            onSurface = AnekonColors.BackgroundSecondary
        )
    } else {
        AnekonColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Configurar barras de sistema según el tema
            if (darkTheme) {
                window.statusBarColor = AnekonColors.TextPrimary.toArgb()
                window.navigationBarColor = AnekonColors.TextPrimary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            } else {
                window.statusBarColor = AnekonColors.BackgroundPrimary.toArgb()
                window.navigationBarColor = AnekonColors.BackgroundPrimary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AnekonTypography,
        content = content
    )
}
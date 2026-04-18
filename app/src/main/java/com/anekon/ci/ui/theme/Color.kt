package com.anekon.ci.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Anekon Color Palette - Premium Minimalist Design
 * Inspirado en la identidad visual con Fénix naranja sobre fondo crema
 */
object AnekonColors {
    // ============ COLORES DE FONDO ============
    // Fondo principal: Crema claro #F9F7F2
    val BackgroundPrimary = Color(0xFFF9F7F2)
    // Fondo secundario para cards: Blanco puro #FFFFFF
    val BackgroundSecondary = Color(0xFFFFFFFF)
    // Fondo terciario: Gris beige claro #F0EEE9
    val BackgroundTertiary = Color(0xFFF0EEE9)

    // ============ COLOR DE ACENTO PRINCIPAL - PHOENIX ORANGE ============
    // Naranja principal #FF5722
    val Accent = Color(0xFFFF5722)
    // Naranja claro #FF9D42
    val AccentLight = Color(0xFFFF9D42)
    // Naranja oscuro #E87B18
    val AccentDark = Color(0xFFE87B18)

    // ============ GRADIENTE PHOENIX ============
    val GradientStart = Color(0xFFFF9D42)
    val GradientEnd = Color(0xFFE87B18)

    // ============ COLOR SECUNDARIO ============
    // Verde éxito #4CAF50
    val Success = Color(0xFF4CAF50)
    val SuccessLight = Color(0xFF81C784)
    // Rojo error #E53935
    val Error = Color(0xFFE53935)
    val ErrorLight = Color(0xFFEF5350)
    // Azul info #2196F3
    val Info = Color(0xFF2196F3)
    // Naranja warning #FF9800
    val Warning = Color(0xFFFF9800)

    // ============ COLORES DE TEXTO ============
    // Texto principal oscuro #1A1A1A
    val TextPrimary = Color(0xFF1A1A1A)
    // Texto secundario gris medio #757575
    val TextSecondary = Color(0xFF757575)
    // Texto terciario gris oscuro #424242
    val TextMuted = Color(0xFF424242)
    // Placeholder gris suave #D1D1D1
    val TextPlaceholder = Color(0xFFD1D1D1)

    // ============ BORDES Y DIVIDERS ============
    val Border = Color(0xFFD1D1D1)
    val BorderLight = Color(0xFFE8E8E8)
    val Divider = Color(0xFFE0E0E0)

    // ============ SOMBRAS ============
    // Sombra suave para neomorfismo sutil
    val ShadowLight = Color(0x1A000000)
    val ShadowMedium = Color(0x33000000)
    // Sombra para botón CTA naranja
    val ButtonShadow = Color(0x4DE87B18)

    // ============ NAVEGACIÓN ============
    // Color de icono activo en bottom nav
    val NavActive = Accent
    // Color de icono inactivo
    val NavInactive = TextMuted

    // ============ STATUS DE BUILDS ============
    val BuildSuccess = Success
    val BuildFailed = Error
    val BuildRunning = Accent
    val BuildPending = TextMuted

    // ============ CARDS ============
    // Card con borde sutil
    val CardBorder = Color(0xFFE8E8E8)
    // Card elevation shadow
    val CardShadow = Color(0x0D000000)
}

/**
 * Gradientes predefinidos para la app
 */
object AnekonGradients {
    val ButtonGradient = listOf(
        AnekonColors.GradientStart,
        AnekonColors.GradientEnd
    )

    val BackgroundGradient = listOf(
        Color(0xFFF5F5F3),
        Color(0xFFEBEBE8)
    )

    val AccentGradient = listOf(
        AnekonColors.Accent,
        AnekonColors.AccentDark
    )
}

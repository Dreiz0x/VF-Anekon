package com.anekon.ci.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Anekon Color Palette - Premium Minimalist Design
 * Inspirado en la pantalla de bienvenida con estética refinada
 */
object AnekonColors {
    // ============ COLORES DE FONDO ============
    // Fondo principal: Crema claro #F2F2EF
    val BackgroundPrimary = Color(0xFFF2F2EF)
    // Fondo secundario para cards: Blanco puro #FFFFFF
    val BackgroundSecondary = Color(0xFFFFFFFF)
    // Fondo terciario: Crema más oscuro
    val BackgroundTertiary = Color(0xFFF7F3EE)

    // ============ COLOR DE ACENTO PRINCIPAL ============
    // Naranja vibrante #F4811F (logo, botones, highlights)
    val Accent = Color(0xFFF4811F)
    // Naranja claro para gradientes #FF9D42
    val AccentLight = Color(0xFFFF9D42)
    // Naranja oscuro para gradientes #E66B00
    val AccentDark = Color(0xFFE66B00)
    // Naranja suave para fondos de Cards
    val AccentSoft = Color(0xFFFF5C1A)

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
    // Texto principal oscuro (no negro puro) #1A1A1A
    val TextPrimary = Color(0xFF1A1A1A)
    // Texto secundario gris medio #8E8E93
    val TextSecondary = Color(0xFF8E8E93)
    // Texto terciario gris claro #BDBDBD
    val TextMuted = Color(0xFFBDBDBD)
    // Placeholder gris suave #D1D1D1
    val TextPlaceholder = Color(0xFFD1D1D1)

    // ============ BORDES Y DIVIDERS ============
    val Border = Color(0xFFD1D1D1)
    val BorderLight = Color(0xFFE8E8E8)
    val Divider = Color(0xFFE0E0E0)

    // ============ GRADIENTES ============
    // Gradiente para botón CTA (degradado naranja)
    val GradientStart = Color(0xFFFF9D42)
    val GradientEnd = Color(0xFFE66B00)
    // Gradiente suave de fondo
    val BackgroundGradientStart = Color(0xFFF7F3EE)
    val BackgroundGradientEnd = Color(0xFFF2F2EF)

    // ============ SOMBRAS ============
    // Sombra suave para neomorfismo sutil
    val ShadowLight = Color(0x1A000000)
    val ShadowMedium = Color(0x33000000)

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
        AnekonColors.BackgroundGradientStart,
        AnekonColors.BackgroundGradientEnd
    )

    val AccentGradient = listOf(
        AnekonColors.Accent,
        AnekonColors.AccentDark
    )
}
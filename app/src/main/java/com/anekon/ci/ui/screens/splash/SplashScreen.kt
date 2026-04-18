package com.anekon.ci.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anekon.ci.ui.theme.AnekonColors

/**
 * SplashScreen - Pantalla de bienvenida premium
 * Inspirada en la imagen de referencia con:
 * - Logo tipo Fénix estilizado
 * - Fondo crema #F2F2EF
 * - Naranja vibrante #F4811F
 * - Tipografía Serif para "Anekon" y Sans-serif para resto
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit = {}
) {
    // Animaciones
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val logoAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoAlpha"
    )

    val buttonScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonScale"
    )

    LaunchedEffect(Unit) {
        // Simular delay de carga
        kotlinx.coroutines.delay(2000)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AnekonColors.BackgroundPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            // Logo Fénix animado
            PhoenixLogo(
                modifier = Modifier.size(200.dp),
                alpha = logoAlpha
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Nombre de la app - Tipografía Serif elegante
            Text(
                text = "Anekon",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = AnekonColors.TextPrimary,
                    letterSpacing = 4.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo - Tipografía Sans-serif moderna
            Text(
                text = "Inteligente · Automatizada · DevOps",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    color = AnekonColors.TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Botón CTA con gradiente
            GradientButton(
                text = "Comenzar",
                scale = buttonScale,
                onClick = onNavigateToHome
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Características - Cards sutiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureChip("CI/CD")
                FeatureChip("AI")
                FeatureChip("AutoFix")
                FeatureChip("GitHub")
            }
        }
    }
}

@Composable
private fun PhoenixLogo(
    modifier: Modifier = Modifier,
    alpha: Float = 1f
) {
    // Logo estilizado tipo Fénix/Circuito
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val path = Path()
        val width = size.width
        val height = size.height
        val centerX = width / 2
        val centerY = height / 2

        // Dibujar líneas tipo fénix estilizado
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#F4811F")
            strokeWidth = 3f
            style = android.graphics.Paint.Style.STROKE
            isAntiAlias = true
        }

        // Líneas curvas que forman el fénix
        val pathAndroid = android.graphics.Path()

        // Ala superior izquierda
        pathAndroid.moveTo(centerX - 40, centerY - 20)
        pathAndroid.quadTo(centerX - 80, centerY - 60, centerX - 60, centerY - 80)
        pathAndroid.lineTo(centerX - 40, centerY - 60)

        // Ala superior derecha
        pathAndroid.moveTo(centerX + 40, centerY - 20)
        pathAndroid.quadTo(centerX + 80, centerY - 60, centerX + 60, centerY - 80)
        pathAndroid.lineTo(centerX + 40, centerY - 60)

        // Cuerpo central
        pathAndroid.moveTo(centerX, centerY - 40)
        pathAndroid.lineTo(centerX, centerY + 40)

        // Ala inferior izquierda
        pathAndroid.moveTo(centerX - 30, centerY + 20)
        pathAndroid.quadTo(centerX - 70, centerY + 50, centerX - 50, centerY + 70)

        // Ala inferior derecha
        pathAndroid.moveTo(centerX + 30, centerY + 20)
        pathAndroid.quadTo(centerX + 70, centerY + 50, centerX + 50, centerY + 70)

        // Nodos circulares
        val nodes = listOf(
            Offset(centerX - 60, centerY - 70),
            Offset(centerX + 60, centerY - 70),
            Offset(centerX, centerY - 40),
            Offset(centerX - 50, centerY + 60),
            Offset(centerX + 50, centerY + 60),
            Offset(centerX, centerY + 40)
        )

        nodes.forEach { node ->
            drawCircle(
                color = Color(0xFFF4811F).copy(alpha = alpha),
                radius = 8f,
                center = node
            )
            drawCircle(
                color = Color(0xFFF4811F).copy(alpha = alpha),
                radius = 12f,
                center = node,
                style = Stroke(width = 2f)
            )
        }

        // Líneas del fénix
        drawPath(
            path = pathAndroid,
            color = Color(0xFFF4811F).copy(alpha = alpha),
            style = Stroke(width = 3f)
        )

        // Círculo central
        drawCircle(
            color = Color(0xFFFF9D42).copy(alpha = alpha),
            radius = 20f,
            center = Offset(centerX, centerY)
        )
        drawCircle(
            color = Color(0xFFF4811F).copy(alpha = alpha),
            radius = 30f,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2f)
        )
    }
}

@Composable
private fun FeatureChip(text: String) {
    Surface(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium),
        color = AnekonColors.BackgroundSecondary.copy(alpha = 0.8f),
        shape = MaterialTheme.shapes.medium,
        shadowElevation = 2.dp
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                color = AnekonColors.TextSecondary
            )
        )
    }
}

@Composable
private fun GradientButton(
    text: String,
    scale: Float = 1f,
    onClick: () -> Unit
) {
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            AnekonColors.GradientStart,
            AnekonColors.GradientEnd
        )
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .height(56.dp)
            .width(200.dp),
        shape = MaterialTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.large)
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            )
        }
    }
}
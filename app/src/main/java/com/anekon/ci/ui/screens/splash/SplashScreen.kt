package com.anekon.ci.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anekon.ci.ui.theme.AnekonColors

/**
 * SplashScreen - Pantalla de bienvenida premium
 * Diseño exacto según imagen de referencia:
 * - Fondo crema con gradiente sutil
 * - Logo Fénix naranja con gradiente
 * - Tipografía Serif para "Anekon"
 * - Botón naranja con gradiente y sombra
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit = {}
) {
    // Animaciones
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val logoAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoAlpha"
    )

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2500)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF5F5F3),
                        Color(0xFFEBEBE8)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Fénix con gradiente
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                PhoenixLogoSvg(
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Nombre "Anekon" - Tipografía Serif elegante
            Text(
                text = "Anekon",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = AnekonColors.TextPrimary,
                    letterSpacing = (-0.5).sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtítulo
            Text(
                text = "Tu plataforma DevOps inteligente",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    color = AnekonColors.TextSecondary,
                    fontWeight = FontWeight.Normal
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Botón CTA con gradiente naranja
            PhoenixButton(
                text = "Comenzamos",
                onClick = onNavigateToHome
            )

            Spacer(modifier = Modifier.height(60.dp))

            // Feature chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureChip("CI/CD")
                FeatureChip("AutoFix")
                FeatureChip("DevOps")
            }
        }
    }
}

@Composable
private fun PhoenixLogoSvg(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = com.anekon.ci.R.drawable.ic_phoenix_logo),
            contentDescription = "Anekon Phoenix Logo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            alpha = 1f
        )
    }
}

@Composable
private fun PhoenixButton(
    text: String,
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
            .fillMaxWidth(0.8f)
            .height(56.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = AnekonColors.ButtonShadow,
                spotColor = AnekonColors.ButtonShadow
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(gradient),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            )
        }
    }
}

@Composable
private fun FeatureChip(text: String) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp)),
        color = Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = AnekonColors.Border
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Medium,
                color = AnekonColors.TextSecondary
            )
        )
    }
}

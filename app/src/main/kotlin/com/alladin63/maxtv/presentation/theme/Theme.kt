package com.alladin63.maxtv.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*

// ─── Couleurs ────────────────────────────────────────────────────────────────

val MaxBlue = Color(0xFF0D6EFD)
val MaxBlueDark = Color(0xFF0A58CA)
val MaxBlueLight = Color(0xFF6EA8FE)
val MaxBackground = Color(0xFF0A0A0F)
val MaxSurface = Color(0xFF12121A)
val MaxSurfaceVariant = Color(0xFF1E1E2E)
val MaxCard = Color(0xFF16162A)
val MaxCardFocused = Color(0xFF1E1E3E)
val MaxOnBackground = Color(0xFFE8E8FF)
val MaxOnSurface = Color(0xFFBBBBDD)
val MaxAccent = Color(0xFFFF6B35)
val MaxGold = Color(0xFFFFD700)
val MaxGreen = Color(0xFF4CAF50)
val MaxRed = Color(0xFFFF4444)
val MaxOverlay = Color(0xCC000000)

// ─── Typography ──────────────────────────────────────────────────────────────

val MaxTvTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        color = MaxOnBackground
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        color = MaxOnBackground
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        color = MaxOnBackground
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        color = MaxOnBackground
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        color = MaxOnBackground
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = MaxOnBackground
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        color = MaxOnSurface
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        color = MaxOnSurface
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = MaxOnSurface
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        color = MaxOnSurface
    )
)

// ─── Theme ───────────────────────────────────────────────────────────────────

@Composable
fun MaxTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = MaxBlue,
            onPrimary = Color.White,
            primaryContainer = MaxBlueDark,
            onPrimaryContainer = MaxBlueLight,
            secondary = MaxAccent,
            onSecondary = Color.White,
            background = MaxBackground,
            onBackground = MaxOnBackground,
            surface = MaxSurface,
            onSurface = MaxOnSurface,
            surfaceVariant = MaxSurfaceVariant,
            onSurfaceVariant = MaxOnSurface,
            error = MaxRed,
            onError = Color.White
        ),
        typography = MaxTvTypography,
        content = content
    )
}

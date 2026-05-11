package com.focustv.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ElectricBlue = Color(0xFF00D5FF)
val NeonViolet = Color(0xFF6D5BFF)
val DeepBg = Color(0xFF050714)
val PanelBg = Color(0xCC0B1026)
val TextSoft = Color(0xFFB9C3E6)

@Composable
fun FocusTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = ElectricBlue,
            secondary = NeonViolet,
            background = DeepBg,
            surface = PanelBg,
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

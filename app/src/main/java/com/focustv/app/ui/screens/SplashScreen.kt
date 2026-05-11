package com.focustv.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focustv.app.ui.DeepBg
import com.focustv.app.ui.ElectricBlue
import com.focustv.app.ui.NeonViolet

@Composable
fun SplashScreen() {
    val infinite = rememberInfiniteTransition(label = "splash")
    val scale by infinite.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "scale"
    )

    Box(
        Modifier.fillMaxSize().background(
            Brush.radialGradient(listOf(NeonViolet.copy(alpha = 0.32f), DeepBg), radius = 900f)
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(154.dp).scale(scale).background(
                    Brush.linearGradient(listOf(ElectricBlue, NeonViolet)),
                    RoundedCornerShape(42.dp)
                ),
                contentAlignment = Alignment.Center
            ) {
                Text("F", color = Color.Black, fontSize = 92.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(22.dp))
            Text("FocusTV", color = Color.White, fontSize = 42.sp, fontWeight = FontWeight.Black)
            Text("cinéma • live • séries", color = ElectricBlue, fontSize = 18.sp)
        }
    }
}

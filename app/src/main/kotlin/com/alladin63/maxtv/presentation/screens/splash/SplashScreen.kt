package com.alladin63.maxtv.presentation.screens.splash

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alladin63.maxtv.presentation.theme.*

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToPlaylist: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        when (state) {
            SplashState.HasPlaylist -> onNavigateToHome()
            SplashState.NoPlaylist -> onNavigateToPlaylist()
            SplashState.Loading -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(MaxSurfaceVariant, MaxBackground),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Logo / Titre
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + scaleIn()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "MaX",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Black,
                        color = MaxBlue
                    )
                    Text(
                        text = "TV",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Light,
                        color = MaxOnBackground,
                        letterSpacing = 12.sp
                    )
                }
            }

            if (state == SplashState.Loading) {
                CircularProgressIndicator(
                    color = MaxBlue,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Version en bas
        Text(
            text = "v1.0",
            color = MaxOnSurface.copy(alpha = 0.4f),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )
    }
}

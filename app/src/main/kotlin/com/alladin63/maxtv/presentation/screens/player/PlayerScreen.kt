package com.alladin63.maxtv.presentation.screens.player

import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import com.alladin63.maxtv.presentation.theme.*
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    url: String,
    title: String,
    type: String,
    contentId: Long,
    onBack: () -> Unit,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val playerState by viewModel.state.collectAsState()
    var showControls by remember { mutableStateOf(true) }
    var controlsTimer by remember { mutableStateOf(0) }

    LaunchedEffect(url) {
        viewModel.initPlayer(url, title, type, contentId)
    }

    LaunchedEffect(showControls) {
        if (showControls) {
            controlsTimer = 0
            while (controlsTimer < 5) {
                delay(1000)
                controlsTimer++
            }
            showControls = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusable()
            .onKeyEvent { event ->
                when {
                    event.type == KeyEventType.KeyDown -> {
                        showControls = true
                        when (event.key) {
                            Key.Back, Key.Escape -> { onBack(); true }
                            Key.MediaPlay, Key.MediaPause, Key.MediaPlayPause -> {
                                viewModel.togglePlayPause(); true
                            }
                            Key.DirectionCenter, Key.Enter -> {
                                if (!showControls) showControls = true
                                else viewModel.togglePlayPause()
                                true
                            }
                            Key.DirectionRight -> { viewModel.seekForward(); true }
                            Key.DirectionLeft -> { viewModel.seekBackward(); true }
                            Key.DirectionUp -> { viewModel.increaseVolume(); true }
                            Key.DirectionDown -> { viewModel.decreaseVolume(); true }
                            else -> false
                        }
                    }
                    else -> false
                }
            }
    ) {
        // ExoPlayer View
        val context = LocalContext.current
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    useController = false
                    player = viewModel.player
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Contrôles overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            PlayerControls(
                title = title,
                isPlaying = playerState.isPlaying,
                isBuffering = playerState.isBuffering,
                currentPosition = playerState.currentPosition,
                duration = playerState.duration,
                isLive = type == "LIVE",
                onPlayPause = { viewModel.togglePlayPause() },
                onSeekForward = { viewModel.seekForward() },
                onSeekBackward = { viewModel.seekBackward() },
                onSeek = { viewModel.seekTo(it) },
                onBack = onBack
            )
        }

        // Buffering indicator
        if (playerState.isBuffering) {
            CircularProgressIndicator(
                color = MaxBlue,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Error
        playerState.error?.let { error ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(MaxSurface.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, null, tint = MaxRed, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = MaxOnBackground)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text("Réessayer")
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerControls(
    title: String,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPosition: Long,
    duration: Long,
    isLive: Boolean,
    onPlayPause: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeek: (Long) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaxOverlay)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (isLive) {
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .background(MaxRed, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("EN DIRECT", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Centre controls
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSeekBackward, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Default.Replay10, null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            IconButton(
                onClick = onPlayPause,
                modifier = Modifier
                    .size(72.dp)
                    .background(MaxBlue.copy(alpha = 0.8f), RoundedCornerShape(50))
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
            IconButton(onClick = onSeekForward, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Default.Forward10, null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
        }

        // Bottom progress bar (VOD uniquement)
        if (!isLive && duration > 0) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(currentPosition), color = Color.White, fontSize = 12.sp)
                    Text(formatTime(duration), color = Color.White, fontSize = 12.sp)
                }
                Spacer(Modifier.height(4.dp))
                Slider(
                    value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                    onValueChange = { onSeek((it * duration).toLong()) },
                    colors = SliderDefaults.colors(
                        thumbColor = MaxBlue,
                        activeTrackColor = MaxBlue,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds)
    else "%02d:%02d".format(minutes, seconds)
}

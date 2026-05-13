package com.alladin63.maxtv.presentation.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.alladin63.maxtv.core.domain.model.*
import com.alladin63.maxtv.presentation.theme.*

@Composable
fun <T> ContentCard(item: T, onClick: () -> Unit) {
    when (item) {
        is Movie -> MovieCard(movie = item, onClick = onClick)
        is Serie -> SerieCard(serie = item, onClick = onClick)
        is Channel -> ChannelCard(channel = item, onClick = onClick)
        is WatchHistory -> HistoryCard(history = item, onClick = onClick)
        else -> {}
    }
}

@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(140.dp)
            .height(210.dp)
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
            .focusable()
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(10.dp))
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) MaxBlue else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
    ) {
        AsyncImage(
            model = movie.coverUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaxBackground.copy(alpha = 0.8f)),
                        startY = 120f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = movie.title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (movie.rating > 0f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(Icons.Default.Star, null, tint = MaxGold, modifier = Modifier.size(10.dp))
                    Text(
                        text = String.format("%.1f", movie.rating),
                        color = MaxGold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Progress bar si en cours
        if (movie.progress > 0L && movie.duration > 0) {
            val progress = (movie.progress.toFloat() / (movie.duration * 1000)).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter),
                color = MaxBlue,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }

        // Focus scale effect
        if (isFocused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, MaxBlue, RoundedCornerShape(10.dp))
            )
        }
    }
}

@Composable
fun SerieCard(serie: Serie, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(140.dp)
            .height(210.dp)
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
            .focusable()
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(10.dp))
    ) {
        AsyncImage(
            model = serie.coverUrl,
            contentDescription = serie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaxBackground.copy(alpha = 0.8f)),
                        startY = 120f
                    )
                )
        )
        Text(
            text = serie.title,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
        if (isFocused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, MaxBlue, RoundedCornerShape(10.dp))
            )
        }
    }
}

@Composable
fun ChannelCard(channel: Channel, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .width(140.dp)
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
            .focusable()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaxSurfaceVariant)
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = if (isFocused) MaxBlue else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (channel.logoUrl.isNotEmpty()) {
                AsyncImage(
                    model = channel.logoUrl,
                    contentDescription = channel.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.padding(8.dp).fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaxOnSurface,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = channel.name,
            color = if (isFocused) MaxOnBackground else MaxOnSurface,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        channel.currentProgram?.let { program ->
            Text(
                text = program.title,
                color = MaxOnSurface.copy(alpha = 0.6f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun HistoryCard(history: WatchHistory, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(140.dp)
            .height(210.dp)
            .onFocusChanged { isFocused = it.isFocused || it.hasFocus }
            .focusable()
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(10.dp))
    ) {
        AsyncImage(
            model = history.posterUrl,
            contentDescription = history.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, MaxBackground.copy(alpha = 0.85f)),
                        startY = 100f
                    )
                )
        )
        Text(
            text = history.title,
            color = Color.White,
            fontSize = 11.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        )
        if (history.duration > 0) {
            val progress = (history.progress.toFloat() / history.duration).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .align(Alignment.BottomCenter),
                color = MaxBlue,
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }
        if (isFocused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, MaxBlue, RoundedCornerShape(10.dp))
            )
        }
    }
}

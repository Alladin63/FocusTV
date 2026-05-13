package com.alladin63.maxtv.presentation.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.alladin63.maxtv.core.domain.model.*
import com.alladin63.maxtv.presentation.screens.home.RatingBadge
import com.alladin63.maxtv.presentation.theme.*

@Composable
fun MovieDetailsScreen(
    movieId: Long,
    onPlay: (String, String) -> Unit,
    onBack: () -> Unit,
    viewModel: MovieDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(movieId) { viewModel.loadMovie(movieId) }

    state.movie?.let { movie ->
        DetailsLayout(
            title = movie.title,
            overview = movie.overview,
            coverUrl = movie.coverUrl,
            backdropUrl = movie.backdropUrl,
            rating = movie.rating,
            year = movie.year,
            genres = movie.genres,
            isFavorite = state.isFavorite,
            onPlay = { onPlay(movie.streamUrl, movie.title) },
            onToggleFavorite = { viewModel.toggleFavorite() },
            onBack = onBack
        )
    } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (state.isLoading) CircularProgressIndicator(color = MaxBlue)
        else Text("Film introuvable", color = MaxOnSurface)
    }
}

@Composable
fun SerieDetailsScreen(
    serieId: Long,
    onPlayEpisode: (String, String, Long) -> Unit,
    onBack: () -> Unit,
    viewModel: SerieDetailsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedSeason by remember { mutableStateOf(1) }

    LaunchedEffect(serieId) { viewModel.loadSerie(serieId) }

    state.serie?.let { serie ->
        Row(Modifier.fillMaxSize()) {
            // Info panneau gauche
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .fillMaxHeight()
                    .background(MaxSurface)
                    .padding(24.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null, tint = MaxOnSurface)
                }
                Spacer(Modifier.height(16.dp))
                AsyncImage(
                    model = serie.coverUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.height(16.dp))
                Text(serie.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaxOnBackground)
                Spacer(Modifier.height(8.dp))
                if (serie.rating > 0) RatingBadge(serie.rating)
                Spacer(Modifier.height(8.dp))
                Text(serie.overview, fontSize = 12.sp, color = MaxOnSurface, maxLines = 6, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.toggleFavorite() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isFavorite) MaxRed.copy(0.2f) else MaxSurfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (state.isFavorite) MaxRed else MaxOnSurface
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (state.isFavorite) "Retirer des favoris" else "Ajouter aux favoris")
                }
            }

            // Saisons et épisodes
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Tabs saisons
                val seasons = serie.seasons.map { it.seasonNumber }.distinct().sorted()
                if (seasons.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = seasons.indexOf(selectedSeason).coerceAtLeast(0),
                        containerColor = MaxSurface,
                        contentColor = MaxBlue,
                        edgePadding = 0.dp
                    ) {
                        seasons.forEach { season ->
                            Tab(
                                selected = selectedSeason == season,
                                onClick = { selectedSeason = season },
                                text = { Text("Saison $season") }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                val episodes = serie.seasons
                    .find { it.seasonNumber == selectedSeason }
                    ?.episodes ?: emptyList()

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(episodes) { episode ->
                        EpisodeRow(episode = episode, onClick = {
                            onPlayEpisode(episode.streamUrl, episode.title, episode.id)
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailsLayout(
    title: String,
    overview: String,
    coverUrl: String,
    backdropUrl: String,
    rating: Float,
    year: String,
    genres: List<String>,
    isFavorite: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onBack: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        // Backdrop
        AsyncImage(
            model = backdropUrl.ifEmpty { coverUrl },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(MaxBackground.copy(0.95f), MaxBackground.copy(0.4f))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(480.dp)
                .padding(40.dp),
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = MaxOnSurface)
            }
            Spacer(Modifier.height(16.dp))
            Text(title, fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (rating > 0) RatingBadge(rating)
                if (year.isNotEmpty()) Text(year, color = MaxOnSurface, fontSize = 13.sp)
                if (genres.isNotEmpty()) Text(genres.take(3).joinToString(", "), color = MaxOnSurface, fontSize = 13.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text(overview, color = MaxOnSurface, fontSize = 14.sp, maxLines = 5, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onPlay,
                    colors = ButtonDefaults.buttonColors(containerColor = MaxBlue),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Regarder", fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(
                        if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (isFavorite) MaxRed else MaxOnSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun EpisodeRow(episode: Episode, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaxSurfaceVariant, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaxSurface, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("E${episode.episodeNumber}", color = MaxBlue, fontWeight = FontWeight.Bold)
        }
        Column(Modifier.weight(1f)) {
            Text(episode.title.ifEmpty { "Épisode ${episode.episodeNumber}" }, color = MaxOnBackground, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (episode.overview.isNotEmpty()) {
                Text(episode.overview, color = MaxOnSurface, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        if (episode.progress > 0 && episode.duration > 0) {
            val pct = (episode.progress.toFloat() / (episode.duration * 1000) * 100).toInt()
            Text("$pct%", color = MaxBlue, fontSize = 12.sp)
        }
        IconButton(onClick = onClick) {
            Icon(Icons.Default.PlayCircleFilled, null, tint = MaxBlue, modifier = Modifier.size(32.dp))
        }
    }
}

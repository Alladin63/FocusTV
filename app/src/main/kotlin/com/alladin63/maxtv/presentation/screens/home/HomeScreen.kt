package com.alladin63.maxtv.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.alladin63.maxtv.core.domain.model.*
import com.alladin63.maxtv.presentation.components.ContentCard
import com.alladin63.maxtv.presentation.components.SideMenu
import com.alladin63.maxtv.presentation.theme.*

@Composable
fun HomeScreen(
    onMovieClick: (Long) -> Unit,
    onSerieClick: (Long) -> Unit,
    onChannelClick: (String, String) -> Unit,
    onNavigateToEpg: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedSection by remember { mutableStateOf(HomeSection.MOVIES) }

    Row(modifier = Modifier.fillMaxSize()) {
        // Menu latéral gauche
        SideMenu(
            selectedSection = selectedSection,
            onSectionSelected = { section ->
                selectedSection = section
                when (section) {
                    HomeSection.EPG -> onNavigateToEpg()
                    HomeSection.SEARCH -> onNavigateToSearch()
                    HomeSection.SETTINGS -> onNavigateToSettings()
                    else -> Unit
                }
            }
        )

        // Contenu principal
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedSection) {
                HomeSection.MOVIES -> MoviesContent(
                    movies = state.movies,
                    continueWatching = state.continueWatching,
                    onMovieClick = onMovieClick
                )
                HomeSection.SERIES -> SeriesContent(
                    series = state.series,
                    onSerieClick = onSerieClick
                )
                HomeSection.CHANNELS -> ChannelsContent(
                    channels = state.channels,
                    onChannelClick = onChannelClick
                )
                HomeSection.FAVORITES -> FavoritesContent(
                    favoriteMovies = state.favoriteMovies,
                    favoriteSeries = state.favoriteSeries,
                    favoriteChannels = state.favoriteChannels,
                    onMovieClick = onMovieClick,
                    onSerieClick = onSerieClick,
                    onChannelClick = onChannelClick
                )
                HomeSection.HISTORY -> HistoryContent(
                    history = state.watchHistory,
                    onMovieClick = onMovieClick,
                    onSerieClick = onSerieClick
                )
                else -> Unit
            }
        }
    }
}

@Composable
private fun MoviesContent(
    movies: List<Movie>,
    continueWatching: List<WatchHistory>,
    onMovieClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        // Continue watching
        if (continueWatching.isNotEmpty()) {
            item {
                ContentRow(
                    title = "Continuer à regarder",
                    items = continueWatching,
                    onItemClick = { history ->
                        if (history.contentType == ContentType.MOVIE) onMovieClick(history.contentId)
                    }
                )
            }
        }

        // Featured / Hero
        if (movies.isNotEmpty()) {
            item {
                HeroBanner(movie = movies.first(), onClick = { onMovieClick(movies.first().id) })
            }
        }

        // Tous les films
        item {
            ContentRow(
                title = "Films",
                items = movies,
                onItemClick = { onMovieClick(it.id) }
            )
        }
    }
}

@Composable
private fun HeroBanner(movie: Movie, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .focusable()
    ) {
        AsyncImage(
            model = movie.backdropUrl.ifEmpty { movie.coverUrl },
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(MaxBackground.copy(alpha = 0.9f), Color.Transparent)
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 32.dp)
        ) {
            Text(
                text = movie.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (movie.rating > 0f) {
                    RatingBadge(rating = movie.rating)
                }
                if (movie.year.isNotEmpty()) {
                    Text(text = movie.year, color = MaxOnSurface, fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = movie.overview,
                color = MaxOnSurface,
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 400.dp)
            )
        }
    }
}

@Composable
private fun <T> ContentRow(title: String, items: List<T>, onItemClick: (T) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaxOnBackground,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items as List<Any>) { item ->
                @Suppress("UNCHECKED_CAST")
                ContentCard(
                    item = item as T,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

@Composable
private fun SeriesContent(series: List<Serie>, onSerieClick: (Long) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item {
            ContentRow(title = "Séries", items = series, onItemClick = { onSerieClick(it.id) })
        }
    }
}

@Composable
private fun ChannelsContent(
    channels: List<Channel>,
    onChannelClick: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item {
            ContentRow(
                title = "Chaînes TV",
                items = channels,
                onItemClick = { onChannelClick(it.streamUrl, it.name) }
            )
        }
    }
}

@Composable
private fun FavoritesContent(
    favoriteMovies: List<Movie>,
    favoriteSeries: List<Serie>,
    favoriteChannels: List<Channel>,
    onMovieClick: (Long) -> Unit,
    onSerieClick: (Long) -> Unit,
    onChannelClick: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        if (favoriteMovies.isNotEmpty()) {
            item {
                ContentRow("Films favoris", favoriteMovies, onItemClick = { onMovieClick(it.id) })
            }
        }
        if (favoriteSeries.isNotEmpty()) {
            item {
                ContentRow("Séries favorites", favoriteSeries, onItemClick = { onSerieClick(it.id) })
            }
        }
        if (favoriteChannels.isNotEmpty()) {
            item {
                ContentRow(
                    "Chaînes favorites",
                    favoriteChannels,
                    onItemClick = { onChannelClick(it.streamUrl, it.name) }
                )
            }
        }
        if (favoriteMovies.isEmpty() && favoriteSeries.isEmpty() && favoriteChannels.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun favori pour le moment", color = MaxOnSurface)
                }
            }
        }
    }
}

@Composable
private fun HistoryContent(
    history: List<WatchHistory>,
    onMovieClick: (Long) -> Unit,
    onSerieClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        item {
            ContentRow(
                "Historique",
                history,
                onItemClick = { h ->
                    when (h.contentType) {
                        ContentType.MOVIE -> onMovieClick(h.contentId)
                        ContentType.SERIE, ContentType.EPISODE -> onSerieClick(h.contentId)
                        else -> Unit
                    }
                }
            )
        }
    }
}

@Composable
fun RatingBadge(rating: Float) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .background(
                color = MaxGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaxGold,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = String.format("%.1f", rating),
            color = MaxGold,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

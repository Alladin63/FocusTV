package com.alladin63.maxtv.presentation.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alladin63.maxtv.core.data.local.dao.*
import com.alladin63.maxtv.core.domain.model.*
import com.alladin63.maxtv.core.util.EntityMapper
import com.alladin63.maxtv.presentation.components.ContentCard
import com.alladin63.maxtv.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

// ─── ViewModel ───────────────────────────────────────────────────────────────

data class SearchState(
    val query: String = "",
    val movies: List<Movie> = emptyList(),
    val series: List<Serie> = emptyList(),
    val channels: List<Channel> = emptyList(),
    val isSearching: Boolean = false
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val movieDao: MovieDao,
    private val serieDao: SerieDao,
    private val channelDao: ChannelDao
) : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> = _state.asStateFlow()
    private var searchJob: Job? = null

    fun search(query: String) {
        _state.update { it.copy(query = query) }
        if (query.length < 2) {
            _state.update { it.copy(movies = emptyList(), series = emptyList(), channels = emptyList(), isSearching = false) }
            return
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _state.update { it.copy(isSearching = true) }
            delay(300) // debounce
            val playlist = playlistDao.getActivePlaylistSync() ?: return@launch

            val movies = movieDao.searchMovies(query, playlist.id).first().map(EntityMapper::toMovie)
            val series = serieDao.searchSeries(query, playlist.id).first().map(EntityMapper::toSerie)
            val channels = channelDao.searchChannels(query, playlist.id).first().map(EntityMapper::toChannel)

            _state.update { it.copy(movies = movies, series = series, channels = channels, isSearching = false) }
        }
    }
}

// ─── Screen ──────────────────────────────────────────────────────────────────

@Composable
fun SearchScreen(
    onMovieClick: (Long) -> Unit,
    onSerieClick: (Long) -> Unit,
    onChannelClick: (String, String) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Barre de recherche
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = MaxOnSurface)
            }
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.search(it) },
                placeholder = { Text("Rechercher films, séries, chaînes...", color = MaxOnSurface.copy(0.5f)) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = MaxBlue) },
                trailingIcon = {
                    if (state.isSearching) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaxBlue)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaxBlue,
                    unfocusedBorderColor = MaxSurfaceVariant,
                    focusedTextColor = MaxOnBackground,
                    unfocusedTextColor = MaxOnBackground,
                    cursorColor = MaxBlue
                ),
                singleLine = true
            )
        }

        Spacer(Modifier.height(24.dp))

        if (state.query.length < 2) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Tapez au moins 2 caractères pour rechercher", color = MaxOnSurface)
            }
            return@Column
        }

        val totalResults = state.movies.size + state.series.size + state.channels.size
        if (!state.isSearching && totalResults == 0) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aucun résultat pour \"${state.query}\"", color = MaxOnSurface)
            }
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            if (state.movies.isNotEmpty()) {
                item {
                    SearchSection(title = "Films (${state.movies.size})") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.movies) { movie ->
                                ContentCard(item = movie, onClick = { onMovieClick(movie.id) })
                            }
                        }
                    }
                }
            }
            if (state.series.isNotEmpty()) {
                item {
                    SearchSection(title = "Séries (${state.series.size})") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.series) { serie ->
                                ContentCard(item = serie, onClick = { onSerieClick(serie.id) })
                            }
                        }
                    }
                }
            }
            if (state.channels.isNotEmpty()) {
                item {
                    SearchSection(title = "Chaînes (${state.channels.size})") {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(state.channels) { channel ->
                                ContentCard(item = channel, onClick = { onChannelClick(channel.streamUrl, channel.name) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaxOnBackground, modifier = Modifier.padding(bottom = 12.dp))
        content()
    }
}

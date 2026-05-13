package com.alladin63.maxtv.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alladin63.maxtv.core.domain.model.*
import com.alladin63.maxtv.core.data.local.dao.*
import com.alladin63.maxtv.core.util.EntityMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class HomeSection {
    MOVIES, SERIES, CHANNELS, EPG, FAVORITES, HISTORY, SEARCH, SETTINGS
}

data class HomeState(
    val movies: List<Movie> = emptyList(),
    val series: List<Serie> = emptyList(),
    val channels: List<Channel> = emptyList(),
    val favoriteMovies: List<Movie> = emptyList(),
    val favoriteSeries: List<Serie> = emptyList(),
    val favoriteChannels: List<Channel> = emptyList(),
    val watchHistory: List<WatchHistory> = emptyList(),
    val continueWatching: List<WatchHistory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val movieDao: MovieDao,
    private val serieDao: SerieDao,
    private val channelDao: ChannelDao,
    private val favoriteDao: FavoriteDao,
    private val watchHistoryDao: WatchHistoryDao
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        observeContent()
    }

    private fun observeContent() {
        viewModelScope.launch {
            playlistDao.getActivePlaylist().collectLatest { playlist ->
                if (playlist == null) return@collectLatest
                val playlistId = playlist.id

                // Movies
                launch {
                    movieDao.getAllMovies(playlistId).collect { entities ->
                        _state.update { it.copy(movies = entities.map(EntityMapper::toMovie)) }
                    }
                }
                // Series
                launch {
                    serieDao.getAllSeries(playlistId).collect { entities ->
                        _state.update { it.copy(series = entities.map(EntityMapper::toSerie)) }
                    }
                }
                // Channels
                launch {
                    channelDao.getAllChannels(playlistId).collect { entities ->
                        _state.update { it.copy(channels = entities.map(EntityMapper::toChannel)) }
                    }
                }
                // Favorite movies
                launch {
                    favoriteDao.getFavoriteMovies().collect { entities ->
                        _state.update { it.copy(favoriteMovies = entities.map(EntityMapper::toMovie)) }
                    }
                }
                // Favorite series
                launch {
                    favoriteDao.getFavoriteSeries().collect { entities ->
                        _state.update { it.copy(favoriteSeries = entities.map(EntityMapper::toSerie)) }
                    }
                }
                // Favorite channels
                launch {
                    favoriteDao.getFavoriteChannels().collect { entities ->
                        _state.update { it.copy(favoriteChannels = entities.map(EntityMapper::toChannel)) }
                    }
                }
                // History
                launch {
                    watchHistoryDao.getHistory().collect { entities ->
                        val history = entities.map(EntityMapper::toWatchHistory)
                        _state.update {
                            it.copy(
                                watchHistory = history,
                                continueWatching = history.filter { h -> h.progress > 0 && !isCompleted(h) }.take(20)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun isCompleted(history: WatchHistory): Boolean {
        if (history.duration <= 0) return false
        return (history.progress.toFloat() / history.duration) > 0.9f
    }
}

package com.alladin63.maxtv.presentation.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alladin63.maxtv.core.data.local.dao.*
import com.alladin63.maxtv.core.data.local.entities.*
import com.alladin63.maxtv.core.domain.model.*
import com.alladin63.maxtv.core.util.EntityMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Movie Details ────────────────────────────────────────────────────────────

data class MovieDetailsState(
    val movie: Movie? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val movieDao: MovieDao,
    private val favoriteDao: FavoriteDao,
    private val progressDao: ProgressDao
) : ViewModel() {

    private val _state = MutableStateFlow(MovieDetailsState())
    val state: StateFlow<MovieDetailsState> = _state.asStateFlow()
    private var currentMovieId: Long = 0L

    fun loadMovie(movieId: Long) {
        currentMovieId = movieId
        viewModelScope.launch {
            val entity = movieDao.getMovieById(movieId)
            if (entity != null) {
                val progress = progressDao.getMovieProgress(movieId)
                val movie = EntityMapper.toMovie(entity).copy(
                    progress = progress?.progress ?: 0L,
                    isWatched = progress?.isWatched ?: false
                )
                _state.update { it.copy(movie = movie, isLoading = false) }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
        viewModelScope.launch {
            favoriteDao.isMovieFavorite(movieId).collect { isFav ->
                _state.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            if (_state.value.isFavorite) {
                favoriteDao.removeMovieFavorite(MovieFavoriteEntity(currentMovieId))
            } else {
                favoriteDao.addMovieFavorite(MovieFavoriteEntity(currentMovieId))
            }
        }
    }
}

// ─── Serie Details ────────────────────────────────────────────────────────────

data class SerieDetailsState(
    val serie: Serie? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class SerieDetailsViewModel @Inject constructor(
    private val serieDao: SerieDao,
    private val episodeDao: EpisodeDao,
    private val favoriteDao: FavoriteDao,
    private val progressDao: ProgressDao
) : ViewModel() {

    private val _state = MutableStateFlow(SerieDetailsState())
    val state: StateFlow<SerieDetailsState> = _state.asStateFlow()
    private var currentSerieId: Long = 0L

    fun loadSerie(serieId: Long) {
        currentSerieId = serieId
        viewModelScope.launch {
            val entity = serieDao.getSerieById(serieId) ?: run {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            val serie = EntityMapper.toSerie(entity)

            // Charger les épisodes groupés par saison
            episodeDao.getEpisodesBySerie(serieId).collect { episodeEntities ->
                val episodes = episodeEntities.map { ep ->
                    val progress = progressDao.getEpisodeProgress(ep.episodeId)
                    EntityMapper.toEpisode(ep).copy(
                        progress = progress?.progress ?: 0L,
                        isWatched = progress?.isWatched ?: false
                    )
                }
                val seasons = episodes
                    .groupBy { it.seasonNumber }
                    .map { (seasonNum, eps) -> Season(seasonNum, eps.sortedBy { it.episodeNumber }) }
                    .sortedBy { it.seasonNumber }

                _state.update {
                    it.copy(serie = serie.copy(seasons = seasons), isLoading = false)
                }
            }
        }
        viewModelScope.launch {
            favoriteDao.isSerieFavorite(serieId).collect { isFav ->
                _state.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            if (_state.value.isFavorite) {
                favoriteDao.removeSerieFavorite(SerieFavoriteEntity(currentSerieId))
            } else {
                favoriteDao.addSerieFavorite(SerieFavoriteEntity(currentSerieId))
            }
        }
    }
}

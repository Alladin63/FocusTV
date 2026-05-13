package com.alladin63.maxtv.presentation.screens.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alladin63.maxtv.core.data.local.dao.*
import com.alladin63.maxtv.core.data.local.entities.*
import com.alladin63.maxtv.core.data.remote.api.XtreamApi
import com.alladin63.maxtv.core.util.M3UParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import javax.inject.Inject

data class PlaylistImportState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val importSuccess: Boolean = false,
    val progress: String = ""
)

@HiltViewModel
class PlaylistViewModel @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val movieDao: MovieDao,
    private val serieDao: SerieDao,
    private val channelDao: ChannelDao,
    private val groupDao: GroupDao,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _state = MutableStateFlow(PlaylistImportState())
    val state: StateFlow<PlaylistImportState> = _state.asStateFlow()

    fun importM3U(url: String, name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, progress = "Téléchargement...") }
            try {
                val content = downloadM3U(url)
                _state.update { it.copy(progress = "Analyse du fichier...") }
                val parsed = M3UParser.parse(content, url)

                _state.update { it.copy(progress = "Sauvegarde en base...") }
                savePlaylistData(name, url, parsed)

                _state.update { it.copy(isLoading = false, importSuccess = true) }
            } catch (e: Exception) {
                Timber.e(e, "M3U import error")
                _state.update { it.copy(isLoading = false, error = "Erreur d'import: ${e.message}") }
            }
        }
    }

    fun importXtream(serverUrl: String, username: String, password: String, name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, progress = "Connexion au serveur...") }
            try {
                val xtreamApi = createXtreamApi(serverUrl)
                val auth = xtreamApi.authenticate(username, password)

                if (auth.userInfo?.status != "Active") {
                    _state.update { it.copy(isLoading = false, error = "Compte inactif ou identifiants incorrects") }
                    return@launch
                }

                _state.update { it.copy(progress = "Chargement des chaînes...") }
                val liveStreams = try { xtreamApi.getLiveStreams(username, password) } catch (e: Exception) { emptyList() }

                _state.update { it.copy(progress = "Chargement des films...") }
                val vodStreams = try { xtreamApi.getVodStreams(username, password) } catch (e: Exception) { emptyList() }

                _state.update { it.copy(progress = "Chargement des séries...") }
                val series = try { xtreamApi.getSeries(username, password) } catch (e: Exception) { emptyList() }

                val liveCategories = try { xtreamApi.getLiveCategories(username, password) } catch (e: Exception) { emptyList() }
                val vodCategories = try { xtreamApi.getVodCategories(username, password) } catch (e: Exception) { emptyList() }
                val serieCategories = try { xtreamApi.getSeriesCategories(username, password) } catch (e: Exception) { emptyList() }

                _state.update { it.copy(progress = "Sauvegarde en base...") }

                // Créer la playlist
                val expiry = auth.userInfo?.expDate?.toLongOrNull()?.times(1000L) ?: 0L
                val playlist = PlaylistEntity(
                    name = name,
                    type = "XTREAM",
                    serverUrl = serverUrl,
                    username = username,
                    password = password,
                    isActive = true,
                    lastUpdated = System.currentTimeMillis(),
                    expiryDate = expiry,
                    movieCount = vodStreams.size,
                    serieCount = series.size,
                    channelCount = liveStreams.size
                )
                playlistDao.deactivateAll()
                val playlistId = playlistDao.insertPlaylist(playlist)

                // Groupes chaînes
                val channelGroups = liveCategories.mapIndexed { i, cat ->
                    GroupEntity(name = cat.categoryName, type = "CHANNEL", order = i, playlistId = playlistId)
                }
                groupDao.insertGroups(channelGroups)

                // Chaînes
                val baseUrl = "$serverUrl/live/$username/$password"
                val channelEntities = liveStreams.map { stream ->
                    val groupId = channelGroups.find { it.name == liveCategories.find { c -> c.categoryId == stream.categoryId }?.categoryName }?.id ?: 0L
                    ChannelEntity(
                        streamId = stream.streamId,
                        name = stream.name,
                        logoUrl = stream.streamIcon,
                        groupId = groupId,
                        groupName = liveCategories.find { c -> c.categoryId == stream.categoryId }?.categoryName ?: "",
                        streamUrl = "$baseUrl/${stream.streamId}.m3u8",
                        playlistId = playlistId,
                        order = stream.num
                    )
                }
                channelDao.insertChannels(channelEntities)

                // Films
                val movieGroups = vodCategories.mapIndexed { i, cat ->
                    GroupEntity(name = cat.categoryName, type = "MOVIE", order = i, playlistId = playlistId)
                }
                groupDao.insertGroups(movieGroups)

                val vodBaseUrl = "$serverUrl/movie/$username/$password"
                val movieEntities = vodStreams.map { stream ->
                    MovieEntity(
                        streamId = stream.streamId,
                        title = stream.name,
                        coverUrl = stream.streamIcon,
                        backdropUrl = stream.coverBig ?: "",
                        overview = stream.plot,
                        rating = stream.rating.toFloatOrNull() ?: 0f,
                        year = stream.releaseDate.take(4),
                        genres = stream.genre,
                        tmdbId = stream.tmdbId?.toIntOrNull(),
                        groupName = vodCategories.find { c -> c.categoryId == stream.categoryId }?.categoryName ?: "",
                        streamUrl = "$vodBaseUrl/${stream.streamId}.${stream.containerExtension}",
                        playlistId = playlistId
                    )
                }
                movieDao.insertMovies(movieEntities)

                // Séries
                val serieGroups = serieCategories.mapIndexed { i, cat ->
                    GroupEntity(name = cat.categoryName, type = "SERIE", order = i, playlistId = playlistId)
                }
                groupDao.insertGroups(serieGroups)

                val serieEntities = series.map { serie ->
                    SerieEntity(
                        seriesId = serie.seriesId,
                        title = serie.name,
                        coverUrl = serie.cover,
                        backdropUrl = serie.backdropPath?.firstOrNull() ?: "",
                        overview = serie.plot,
                        rating = serie.rating.toFloatOrNull() ?: 0f,
                        year = serie.releaseDate.take(4),
                        genres = serie.genre,
                        tmdbId = serie.tmdbId?.toIntOrNull(),
                        groupName = serieCategories.find { c -> c.categoryId == serie.categoryId }?.categoryName ?: "",
                        playlistId = playlistId
                    )
                }
                serieDao.insertSeries(serieEntities)

                _state.update { it.copy(isLoading = false, importSuccess = true) }

            } catch (e: Exception) {
                Timber.e(e, "Xtream import error")
                _state.update { it.copy(isLoading = false, error = "Erreur de connexion: ${e.message}") }
            }
        }
    }

    private suspend fun downloadM3U(url: String): String {
        val request = Request.Builder().url(url).build()
        return okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")
            response.body?.string() ?: throw Exception("Corps vide")
        }
    }

    private suspend fun savePlaylistData(name: String, url: String, parsed: M3UParser.ParseResult) {
        val playlist = PlaylistEntity(
            name = name,
            type = "M3U",
            url = url,
            isActive = true,
            lastUpdated = System.currentTimeMillis(),
            movieCount = parsed.movies.size,
            serieCount = 0,
            channelCount = parsed.channels.size
        )
        playlistDao.deactivateAll()
        val playlistId = playlistDao.insertPlaylist(playlist)

        val groups = parsed.groups.mapIndexed { i, g ->
            GroupEntity(name = g, type = "CHANNEL", order = i, playlistId = playlistId)
        }
        groupDao.insertGroups(groups)
        channelDao.insertChannels(parsed.channels.map { it.copy(playlistId = playlistId) })
        movieDao.insertMovies(parsed.movies.map { it.copy(playlistId = playlistId) })
    }

    private fun createXtreamApi(serverUrl: String): XtreamApi {
        val base = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        return Retrofit.Builder()
            .baseUrl(base)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XtreamApi::class.java)
    }
}

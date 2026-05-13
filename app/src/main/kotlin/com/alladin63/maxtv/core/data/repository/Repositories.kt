package com.alladin63.maxtv.core.data.repository

import com.alladin63.maxtv.core.data.local.AppDatabase
import com.alladin63.maxtv.core.data.local.entities.*
import com.alladin63.maxtv.core.data.remote.api.TmdbApi
import com.alladin63.maxtv.core.domain.model.*
import com.alladin63.maxtv.core.util.EntityMapper
import com.alladin63.maxtv.core.util.M3UParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Singleton

@Singleton
class PlaylistRepository(
    private val db: AppDatabase,
    private val okHttpClient: OkHttpClient,
    private val m3uParser: M3UParser,
) {
    fun getAllPlaylists(): Flow<List<Playlist>> =
        db.playlistDao().getAllPlaylists().map { list -> list.map(EntityMapper::toPlaylist) }

    suspend fun addM3UPlaylist(name: String, url: String): Result<Long> = runCatching {
        val request = Request.Builder().url(url).build()
        val body = okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}")
            response.body?.string().orEmpty()
        }
        val parsed = m3uParser.parse(body, url)

        db.playlistDao().deactivateAll()
        val playlistId = db.playlistDao().insertPlaylist(
            PlaylistEntity(
                name = name,
                url = url,
                type = "M3U",
                isActive = true,
                lastUpdated = System.currentTimeMillis(),
                movieCount = parsed.movies.size,
                channelCount = parsed.channels.size,
            )
        )

        db.channelDao().insertChannels(parsed.channels.map { it.copy(playlistId = playlistId) })
        db.movieDao().insertMovies(parsed.movies.map { it.copy(playlistId = playlistId) })
        playlistId
    }

    suspend fun addXtreamPlaylist(
        name: String,
        host: String,
        port: String,
        username: String,
        password: String,
    ): Result<Long> = runCatching {
        db.playlistDao().deactivateAll()
        db.playlistDao().insertPlaylist(
            PlaylistEntity(
                name = name,
                url = "$host:$port",
                serverUrl = "$host:$port",
                type = "XTREAM",
                username = username,
                password = password,
                isActive = true,
                lastUpdated = System.currentTimeMillis(),
            )
        )
    }

    suspend fun deletePlaylist(id: Long) {
        db.channelDao().deleteByPlaylist(id)
        db.movieDao().deleteByPlaylist(id)
        db.serieDao().deleteByPlaylist(id)
        db.groupDao().deleteByPlaylist(id)
        db.playlistDao().deletePlaylist(id)
    }

    suspend fun setActivePlaylist(id: Long) {
        db.playlistDao().deactivateAll()
        db.playlistDao().activatePlaylist(id)
    }
}

@Singleton
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ContentRepository(
    private val db: AppDatabase,
    @Suppress("UNUSED_PARAMETER") private val tmdbApi: TmdbApi,
) {
    private fun activePlaylistIdFlow(): Flow<Long?> =
        db.playlistDao().getActivePlaylist().map { it?.id }

    fun getMovies(playlistId: Long? = null): Flow<List<Movie>> =
        playlistId?.let { id -> db.movieDao().getAllMovies(id).map { it.map(EntityMapper::toMovie) } }
            ?: activePlaylistIdFlow().flatMapLatest { id ->
                if (id == null) flowOf(emptyList())
                else db.movieDao().getAllMovies(id).map { it.map(EntityMapper::toMovie) }
            }

    suspend fun getMovie(id: Long): Movie? =
        db.movieDao().getMovieById(id)?.let(EntityMapper::toMovie)

    fun getSeries(playlistId: Long? = null): Flow<List<Serie>> =
        playlistId?.let { id -> db.serieDao().getAllSeries(id).map { it.map(EntityMapper::toSerie) } }
            ?: activePlaylistIdFlow().flatMapLatest { id ->
                if (id == null) flowOf(emptyList())
                else db.serieDao().getAllSeries(id).map { it.map(EntityMapper::toSerie) }
            }

    suspend fun getSerie(id: Long): Serie? =
        db.serieDao().getSerieById(id)?.let(EntityMapper::toSerie)

    fun getEpisodesBySerie(serieId: Long): Flow<List<Episode>> =
        db.episodeDao().getEpisodesBySerie(serieId).map { it.map(EntityMapper::toEpisode) }

    fun getChannels(playlistId: Long? = null): Flow<List<Channel>> =
        playlistId?.let { id -> db.channelDao().getAllChannels(id).map { it.map(EntityMapper::toChannel) } }
            ?: activePlaylistIdFlow().flatMapLatest { id ->
                if (id == null) flowOf(emptyList())
                else db.channelDao().getAllChannels(id).map { it.map(EntityMapper::toChannel) }
            }

    fun getFavoriteMovies(): Flow<List<Movie>> =
        db.favoriteDao().getFavoriteMovies().map { it.map(EntityMapper::toMovie) }

    fun getFavoriteSeries(): Flow<List<Serie>> =
        db.favoriteDao().getFavoriteSeries().map { it.map(EntityMapper::toSerie) }

    fun getFavoriteChannels(): Flow<List<Channel>> =
        db.favoriteDao().getFavoriteChannels().map { it.map(EntityMapper::toChannel) }
}

data class SearchResult(
    val movies: List<Movie> = emptyList(),
    val series: List<Serie> = emptyList(),
    val channels: List<Channel> = emptyList(),
)

@Singleton
class HistoryRepository(
    private val db: AppDatabase,
) {
    fun getHistory(): Flow<List<WatchHistory>> =
        db.watchHistoryDao().getHistory().map { list -> list.map(EntityMapper::toWatchHistory) }

    suspend fun addToHistory(
        contentId: Long,
        contentType: String,
        title: String,
        posterUrl: String?,
        positionMs: Long = 0L,
        durationMs: Long = 0L,
    ) {
        db.watchHistoryDao().insert(
            WatchHistoryEntity(
                contentId = contentId,
                contentType = contentType,
                title = title,
                posterUrl = posterUrl.orEmpty(),
                progress = positionMs,
                duration = durationMs,
                watchedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun updateProgress(contentId: Long, contentType: String, positionMs: Long, durationMs: Long) {
        db.watchHistoryDao().insert(
            WatchHistoryEntity(
                contentId = contentId,
                contentType = contentType,
                progress = positionMs,
                duration = durationMs,
                watchedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun clearHistory() = db.watchHistoryDao().deleteAll()

    fun getProgress(@Suppress("UNUSED_PARAMETER") contentId: Long, @Suppress("UNUSED_PARAMETER") contentType: String): Flow<Long> =
        flowOf(0L)
}

@Singleton
class EpgRepository(
    private val db: AppDatabase,
    private val okHttpClient: OkHttpClient,
) {
    fun getEpgForChannel(tvgId: String): Flow<List<EpgProgram>> =
        db.epgDao().getByChannelFlow(tvgId).map { list -> list.map(EntityMapper::toEpgProgram) }

    fun getCurrentProgram(tvgId: String, nowMs: Long): Flow<EpgProgram?> =
        db.epgDao().getCurrentProgramFlow(tvgId, nowMs).map { entity -> entity?.let(EntityMapper::toEpgProgram) }

    suspend fun refreshEpg(epgUrl: String) = runCatching {
        val request = Request.Builder().url(epgUrl).build()
        val xmlContent = okHttpClient.newCall(request).execute().use { response -> response.body?.string().orEmpty() }
        val programs = parseXmltvEpg(xmlContent)
        db.epgDao().insertAll(programs)
    }

    private fun parseXmltvEpg(xml: String): List<EpgProgramEntity> {
        val result = mutableListOf<EpgProgramEntity>()
        val programRegex = Regex(
            """<programme\s+start="(\d+)[^"]*"\s+stop="(\d+)[^"]*"\s+channel="([^"]+)"[^>]*>""" +
                """.*?<title[^>]*>([^<]*)</title>""" +
                """(?:.*?<desc[^>]*>([^<]*)</desc>)?""" +
                """(?:.*?<icon\s+src="([^"]*)")?""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
        )
        val sdf = java.text.SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.US)
        programRegex.findAll(xml).forEach { match ->
            try {
                val start = sdf.parse(match.groupValues[1])?.time ?: return@forEach
                val stop = sdf.parse(match.groupValues[2])?.time ?: return@forEach
                result.add(
                    EpgProgramEntity(
                        epgChannelId = match.groupValues[3],
                        title = match.groupValues[4],
                        description = match.groupValues.getOrNull(5).orEmpty(),
                        posterUrl = match.groupValues.getOrNull(6).orEmpty(),
                        startTime = start,
                        endTime = stop,
                    )
                )
            } catch (_: Exception) {
            }
        }
        return result
    }
}

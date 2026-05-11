package com.focustv.app.services

import android.content.Context
import com.focustv.app.core.FocusTvState
import com.focustv.app.core.IptvSource
import com.focustv.app.core.MediaContent
import com.focustv.app.core.SourceDetector
import com.focustv.app.core.SourceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FocusRepository(context: Context) {
    private val storage = StorageService(context)
    private val m3u = M3uParser()
    private val xtream = XtreamService()
    private val stalker = StalkerService()
    private val tmdb = TmdbService()
    private val fanart = FanartService()

    suspend fun initialState(): FocusTvState {
        return FocusTvState(
            sources = storage.loadSources(),
            favorites = storage.loadFavorites(),
            history = storage.loadHistory(),
            profiles = storage.loadProfiles(),
            settings = storage.loadSettings()
        )
    }

    suspend fun saveSource(source: IptvSource): List<IptvSource> {
        val current = storage.loadSources().filterNot { it.id == source.id }
        val detected = if (source.type == SourceType.AUTO) {
            source.copy(type = SourceDetector.detect(source.url, source.username, source.password))
        } else source
        val all = current + detected
        storage.saveSources(all)
        return all
    }

    suspend fun deleteSource(id: String): List<IptvSource> {
        val all = storage.loadSources().filterNot { it.id == id }
        storage.saveSources(all)
        return all
    }

    suspend fun reloadAll(sources: List<IptvSource>): Triple<List<MediaContent>, List<MediaContent>, List<MediaContent>> =
        withContext(Dispatchers.IO) {
            val live = mutableListOf<MediaContent>()
            val movies = mutableListOf<MediaContent>()
            val series = mutableListOf<MediaContent>()

            sources.filter { it.enabled }.forEach { source ->
                try {
                    val type = if (source.type == SourceType.AUTO) {
                        SourceDetector.detect(source.url, source.username, source.password)
                    } else source.type

                    when (type) {
                        SourceType.M3U -> {
                            val items = m3u.load(source)
                            live += items.filter { it.type == "live" }
                            movies += items.filter { it.type == "movie" }
                        }
                        SourceType.XTREAM -> {
                            val data = xtream.load(source)
                            live += data.first
                            movies += data.second
                            series += data.third
                        }
                        SourceType.STALKER -> live += stalker.load(source)
                        SourceType.AUTO -> Unit
                    }
                } catch (_: Exception) {
                    // Un serveur ne doit jamais faire tomber l'application.
                }
            }

            val enrichedMovies = movies.take(80).map { fanart.enrich(tmdb.enrich(it)) } + movies.drop(80)
            val enrichedSeries = series.take(80).map { fanart.enrich(tmdb.enrich(it)) } + series.drop(80)
            Triple(live.distinctBy { it.id }, enrichedMovies.distinctBy { it.id }, enrichedSeries.distinctBy { it.id })
        }

    suspend fun toggleFavorite(id: String): Set<String> {
        val fav = storage.loadFavorites().toMutableSet()
        if (!fav.add(id)) fav.remove(id)
        storage.saveFavorites(fav)
        return fav
    }

    suspend fun addHistory(item: MediaContent) {
        val list = listOf(item) + storage.loadHistory().filterNot { it.id == item.id }
        storage.saveHistory(list)
    }
}

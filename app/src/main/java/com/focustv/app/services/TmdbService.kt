package com.focustv.app.services

import com.focustv.app.BuildConfig
import com.focustv.app.core.MediaContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder

class TmdbService {
    suspend fun enrich(content: MediaContent): MediaContent = withContext(Dispatchers.IO) {
        val token = BuildConfig.TMDB_TOKEN
        if (token.isBlank() || content.title.isBlank()) return@withContext content

        try {
            val type = if (content.type == "series") "tv" else "movie"
            val q = URLEncoder.encode(cleanTitle(content.title), "UTF-8")
            val url = "https://api.themoviedb.org/3/search/$type?language=fr-FR&query=$q"
            val req = HttpClient.request(url).newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            val response = HttpClient.client.newCall(req).execute()
            val root = JSONObject(response.body?.string().orEmpty())
            val first = root.optJSONArray("results")?.optJSONObject(0) ?: return@withContext content
            val poster = first.optString("poster_path").takeIf { it.isNotBlank() }?.let { "https://image.tmdb.org/t/p/w500$it" }.orEmpty()
            val backdrop = first.optString("backdrop_path").takeIf { it.isNotBlank() }?.let { "https://image.tmdb.org/t/p/w1280$it" }.orEmpty()

            content.copy(
                tmdbId = first.optString("id"),
                poster = content.poster.ifBlank { poster },
                backdrop = content.backdrop.ifBlank { backdrop },
                description = content.description.ifBlank { first.optString("overview") },
                year = content.year.ifBlank {
                    first.optString("release_date").ifBlank { first.optString("first_air_date") }.take(4)
                },
                rating = content.rating.ifBlank { first.optDouble("vote_average", 0.0).toString() }
            )
        } catch (_: Exception) {
            content
        }
    }

    private fun cleanTitle(v: String): String {
        return v.replace(Regex("\\([^)]*\\)|\\[[^]]*]"), "")
            .replace(Regex("\\b(1080p|720p|4k|uhd|multi|french|truefrench|vostfr)\\b", RegexOption.IGNORE_CASE), "")
            .trim()
    }
}

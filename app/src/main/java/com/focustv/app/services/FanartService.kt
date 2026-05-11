package com.focustv.app.services

import com.focustv.app.BuildConfig
import com.focustv.app.core.MediaContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class FanartService {
    suspend fun enrich(content: MediaContent): MediaContent = withContext(Dispatchers.IO) {
        val token = BuildConfig.FANART_TOKEN
        val tmdb = content.tmdbId
        if (token.isBlank() || tmdb.isBlank() || content.type == "live") return@withContext content

        try {
            val kind = if (content.type == "series") "tv" else "movies"
            val url = "https://webservice.fanart.tv/v3/$kind/$tmdb?api_key=$token"
            val response = HttpClient.client.newCall(HttpClient.request(url)).execute()
            val root = JSONObject(response.body?.string().orEmpty())
            val bg = root.optJSONArray("moviebackground")
                ?: root.optJSONArray("showbackground")
            val logo = root.optJSONArray("hdmovielogo")
                ?: root.optJSONArray("hdtvlogo")
                ?: root.optJSONArray("clearlogo")
            content.copy(
                backdrop = content.backdrop.ifBlank { bg?.optJSONObject(0)?.optString("url").orEmpty() },
                logo = content.logo.ifBlank { logo?.optJSONObject(0)?.optString("url").orEmpty() }
            )
        } catch (_: Exception) {
            content
        }
    }
}

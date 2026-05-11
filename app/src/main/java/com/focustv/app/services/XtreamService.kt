package com.focustv.app.services

import com.focustv.app.core.IptvSource
import com.focustv.app.core.MediaContent
import com.focustv.app.core.SourceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder

class XtreamService {
    suspend fun load(source: IptvSource): Triple<List<MediaContent>, List<MediaContent>, List<MediaContent>> =
        withContext(Dispatchers.IO) {
            val base = SourceDetector.xtreamBaseUrl(source.url)
            val u = enc(source.username)
            val p = enc(source.password)

            val liveCategories = jsonArray("$base/player_api.php?username=$u&password=$p&action=get_live_categories", source)
            val vodCategories = jsonArray("$base/player_api.php?username=$u&password=$p&action=get_vod_categories", source)
            val seriesCategories = jsonArray("$base/player_api.php?username=$u&password=$p&action=get_series_categories", source)

            val live = jsonArray("$base/player_api.php?username=$u&password=$p&action=get_live_streams", source)
                .map { o ->
                    val streamId = o.optString("stream_id")
                    MediaContent(
                        id = source.id + "_live_" + streamId,
                        sourceId = source.id,
                        title = o.optString("name", "Chaîne"),
                        streamUrl = "$base/live/$u/$p/$streamId.m3u8",
                        type = "live",
                        category = categoryName(liveCategories, o.optString("category_id")),
                        logo = o.optString("stream_icon"),
                        poster = o.optString("stream_icon")
                    )
                }

            val movies = jsonArray("$base/player_api.php?username=$u&password=$p&action=get_vod_streams", source)
                .map { o ->
                    val streamId = o.optString("stream_id")
                    val ext = o.optString("container_extension", "mp4").ifBlank { "mp4" }
                    MediaContent(
                        id = source.id + "_movie_" + streamId,
                        sourceId = source.id,
                        title = o.optString("name", "Film"),
                        streamUrl = "$base/movie/$u/$p/$streamId.$ext",
                        type = "movie",
                        category = categoryName(vodCategories, o.optString("category_id")),
                        poster = o.optString("stream_icon"),
                        rating = o.optString("rating"),
                        year = o.optString("year")
                    )
                }

            val series = jsonArray("$base/player_api.php?username=$u&password=$p&action=get_series", source)
                .map { o ->
                    val seriesId = o.optString("series_id")
                    MediaContent(
                        id = source.id + "_series_" + seriesId,
                        sourceId = source.id,
                        title = o.optString("name", "Série"),
                        streamUrl = "$base/player_api.php?username=$u&password=$p&action=get_series_info&series_id=$seriesId",
                        type = "series",
                        category = categoryName(seriesCategories, o.optString("category_id")),
                        poster = o.optString("cover"),
                        backdrop = o.optJSONArray("backdrop_path")?.optString(0).orEmpty(),
                        description = o.optString("plot"),
                        rating = o.optString("rating"),
                        year = o.optString("releaseDate").take(4)
                    )
                }

            Triple(live, movies, series)
        }

    private fun jsonArray(url: String, source: IptvSource): List<JSONObject> {
        return try {
            val response = HttpClient.client.newCall(HttpClient.request(url, source.userAgent)).execute()
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful || body.isBlank()) emptyList()
            else {
                val arr = JSONArray(body)
                List(arr.length()) { arr.getJSONObject(it) }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun categoryName(categories: List<JSONObject>, id: String): String {
        return categories.firstOrNull { it.optString("category_id") == id }
            ?.optString("category_name")
            ?.ifBlank { "Tous" } ?: "Tous"
    }

    private fun enc(v: String): String = URLEncoder.encode(v, "UTF-8")
}

package com.focustv.app.services

import com.focustv.app.core.IptvSource
import com.focustv.app.core.MediaContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class StalkerService {
    suspend fun load(source: IptvSource): List<MediaContent> = withContext(Dispatchers.IO) {
        try {
            val base = source.url.trim().removeSuffix("/")
            val mac = source.portalMac.ifBlank { "00:1A:79:00:00:00" }
            val handshake = "$base/portal.php?type=stb&action=handshake&JsHttpRequest=1-xml"
            val request = HttpClient.request(handshake, source.userAgent).newBuilder()
                .header("Cookie", "mac=$mac; stb_lang=fr; timezone=Europe/Paris")
                .build()

            val response = HttpClient.client.newCall(request).execute()
            val token = JSONObject(response.body?.string().orEmpty())
                .optJSONObject("js")
                ?.optString("token")
                .orEmpty()

            if (token.isBlank()) return@withContext emptyList()

            val channelsUrl = "$base/portal.php?type=itv&action=get_all_channels&JsHttpRequest=1-xml"
            val channelsReq = HttpClient.request(channelsUrl, source.userAgent).newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Cookie", "mac=$mac; stb_lang=fr; timezone=Europe/Paris")
                .build()

            val channelsRes = HttpClient.client.newCall(channelsReq).execute()
            val body = channelsRes.body?.string().orEmpty()
            val js = JSONObject(body).optJSONObject("js")
            val data = js?.optJSONArray("data") ?: return@withContext emptyList()

            List(data.length()) { idx ->
                val o = data.getJSONObject(idx)
                val cmd = o.optString("cmd").substringAfter("ffmpeg ", o.optString("cmd"))
                MediaContent(
                    id = source.id + "_stalker_" + o.optString("id", idx.toString()),
                    sourceId = source.id,
                    title = o.optString("name", "Chaîne"),
                    streamUrl = cmd,
                    type = "live",
                    category = o.optString("tv_genre_title", "Tous"),
                    logo = o.optString("logo"),
                    poster = o.optString("logo")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}

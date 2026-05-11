package com.focustv.app.services

import com.focustv.app.core.IptvSource
import com.focustv.app.core.MediaContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class M3uParser {
    suspend fun load(source: IptvSource): List<MediaContent> = withContext(Dispatchers.IO) {
        val response = HttpClient.client.newCall(HttpClient.request(source.url, source.userAgent)).execute()
        if (!response.isSuccessful) return@withContext emptyList()
        parse(source, response.body?.string().orEmpty())
    }

    fun parse(source: IptvSource, text: String): List<MediaContent> {
        val items = mutableListOf<MediaContent>()
        var title = ""
        var logo = ""
        var group = "Tous"
        var tvgId = ""

        text.lineSequence().forEach { raw ->
            val line = raw.trim()
            if (line.startsWith("#EXTINF", ignoreCase = true)) {
                title = line.substringAfter(",", "").trim().ifBlank { "Sans titre" }
                logo = attr(line, "tvg-logo")
                group = attr(line, "group-title").ifBlank { "Tous" }
                tvgId = attr(line, "tvg-id")
            } else if (line.startsWith("http", ignoreCase = true)) {
                val id = stableId(source.id + "|" + tvgId + "|" + title + "|" + line)
                val isVod = line.contains(".mp4", true) || line.contains(".mkv", true) || line.contains(".avi", true)
                items += MediaContent(
                    id = id,
                    sourceId = source.id,
                    title = title.ifBlank { line.substringAfterLast("/") },
                    streamUrl = line,
                    type = if (isVod) "movie" else "live",
                    category = group,
                    poster = logo,
                    logo = logo
                )
            }
        }
        return items
    }

    private fun attr(line: String, key: String): String {
        val marker = "$key=\""
        if (!line.contains(marker)) return ""
        return line.substringAfter(marker).substringBefore("\"").trim()
    }

    private fun stableId(value: String): String {
        val bytes = MessageDigest.getInstance("SHA-1").digest(value.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

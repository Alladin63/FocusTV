package com.alladin63.maxtv.core.util

import com.alladin63.maxtv.core.data.local.entities.ChannelEntity
import com.alladin63.maxtv.core.data.local.entities.MovieEntity
import timber.log.Timber

object M3UParser {

    data class ParseResult(
        val channels: List<ChannelEntity> = emptyList(),
        val movies: List<MovieEntity> = emptyList(),
        val groups: List<String> = emptyList()
    )

    fun parse(content: String, baseUrl: String): ParseResult {
        if (!content.startsWith("#EXTM3U")) {
            throw Exception("Format M3U invalide")
        }

        val lines = content.lines()
        val channels = mutableListOf<ChannelEntity>()
        val movies = mutableListOf<MovieEntity>()
        val groups = mutableSetOf<String>()

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()

            if (line.startsWith("#EXTINF:")) {
                val infoLine = line
                val urlLine = if (i + 1 < lines.size) lines[i + 1].trim() else ""

                if (urlLine.isNotEmpty() && !urlLine.startsWith("#")) {
                    try {
                        val name = extractAttribute(infoLine, "tvg-name")
                            ?: infoLine.substringAfterLast(",").trim()
                        val logo = extractAttribute(infoLine, "tvg-logo") ?: ""
                        val group = extractAttribute(infoLine, "group-title") ?: "Général"
                        val tvgId = extractAttribute(infoLine, "tvg-id") ?: ""

                        groups.add(group)

                        // Détecter VOD vs Live
                        val isVod = urlLine.contains("/movie/") ||
                            urlLine.endsWith(".mkv") || urlLine.endsWith(".mp4") ||
                            urlLine.endsWith(".avi") || urlLine.endsWith(".mov")

                        if (isVod) {
                            movies.add(
                                MovieEntity(
                                    streamId = channels.size.toLong() + movies.size.toLong(),
                                    title = name,
                                    coverUrl = logo,
                                    groupName = group,
                                    streamUrl = urlLine
                                )
                            )
                        } else {
                            channels.add(
                                ChannelEntity(
                                    streamId = channels.size.toLong(),
                                    name = name,
                                    logoUrl = logo,
                                    groupName = group,
                                    streamUrl = urlLine,
                                    order = channels.size
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Erreur parsing ligne M3U: $line")
                    }
                    i += 2
                    continue
                }
            }
            i++
        }

        Timber.d("M3U parsed: ${channels.size} chaînes, ${movies.size} films, ${groups.size} groupes")
        return ParseResult(channels = channels, movies = movies, groups = groups.toList())
    }

    private fun extractAttribute(line: String, attribute: String): String? {
        val regex = Regex("""$attribute="([^"]*)"""")
        return regex.find(line)?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
    }
}

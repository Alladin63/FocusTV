package com.focustv.app.core

import kotlinx.serialization.Serializable

@Serializable
enum class SourceType { M3U, XTREAM, STALKER, AUTO }

@Serializable
data class IptvSource(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val type: SourceType,
    val url: String,
    val username: String = "",
    val password: String = "",
    val portalMac: String = "",
    val userAgent: String = "FocusTV/1.0 AndroidTV",
    val enabled: Boolean = true
)

@Serializable
data class MediaContent(
    val id: String,
    val sourceId: String,
    val title: String,
    val streamUrl: String,
    val type: String,
    val category: String = "Tous",
    val poster: String = "",
    val backdrop: String = "",
    val logo: String = "",
    val description: String = "",
    val year: String = "",
    val genre: String = "",
    val duration: String = "",
    val cast: String = "",
    val rating: String = "",
    val tmdbId: String = "",
    val season: Int = 0,
    val episode: Int = 0,
    val resumePositionMs: Long = 0L
)

@Serializable
data class LiveProgram(
    val channelId: String,
    val title: String,
    val description: String = "",
    val start: Long = 0L,
    val end: Long = 0L
)

@Serializable
data class UserProfile(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val locked: Boolean = false,
    val pin: String = "",
    val avatar: String = "neon"
)

@Serializable
data class AppSettings(
    val language: String = "fr",
    val theme: String = "neon",
    val playerEngine: String = "media3",
    val enableAnimatedVodBackground: Boolean = true,
    val enableParentalLock: Boolean = false,
    val startupProfileId: String = "",
    val lastSection: String = "home"
)

@Serializable
data class FocusTvState(
    val sources: List<IptvSource> = emptyList(),
    val profiles: List<UserProfile> = listOf(UserProfile(name = "Principal")),
    val favorites: Set<String> = emptySet(),
    val history: List<MediaContent> = emptyList(),
    val live: List<MediaContent> = emptyList(),
    val movies: List<MediaContent> = emptyList(),
    val series: List<MediaContent> = emptyList(),
    val settings: AppSettings = AppSettings(),
    val loading: Boolean = false,
    val message: String = ""
)

package com.alladin63.maxtv.core.domain.model

data class Movie(
    val id: Long = 0,
    val streamId: Long = 0,
    val title: String = "",
    val coverUrl: String = "",
    val backdropUrl: String = "",
    val overview: String = "",
    val rating: Float = 0f,
    val year: String = "",
    val genres: List<String> = emptyList(),
    val duration: Int = 0,
    val tmdbId: Int? = null,
    val groupId: Long = 0,
    val groupName: String = "",
    val streamUrl: String = "",
    val isFavorite: Boolean = false,
    val isWatched: Boolean = false,
    val progress: Long = 0L
)

data class Serie(
    val id: Long = 0,
    val seriesId: Long = 0,
    val title: String = "",
    val coverUrl: String = "",
    val backdropUrl: String = "",
    val overview: String = "",
    val rating: Float = 0f,
    val year: String = "",
    val genres: List<String> = emptyList(),
    val tmdbId: Int? = null,
    val groupId: Long = 0,
    val groupName: String = "",
    val isFavorite: Boolean = false,
    val seasons: List<Season> = emptyList()
)

data class Season(
    val seasonNumber: Int = 0,
    val episodes: List<Episode> = emptyList()
)

data class Episode(
    val id: Long = 0,
    val episodeId: Long = 0,
    val title: String = "",
    val overview: String = "",
    val thumbnailUrl: String = "",
    val seasonNumber: Int = 0,
    val episodeNumber: Int = 0,
    val duration: Int = 0,
    val streamUrl: String = "",
    val progress: Long = 0L,
    val isWatched: Boolean = false
)

data class Channel(
    val id: Long = 0,
    val streamId: Long = 0,
    val name: String = "",
    val logoUrl: String = "",
    val groupId: Long = 0,
    val groupName: String = "",
    val streamUrl: String = "",
    val isFavorite: Boolean = false,
    val currentProgram: EpgProgram? = null,
    val nextProgram: EpgProgram? = null,
    val hasReplay: Boolean = false
)

data class EpgProgram(
    val id: Long = 0,
    val channelId: Long = 0,
    val title: String = "",
    val description: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val posterUrl: String = "",
    val category: String = ""
)

data class Group(
    val id: Long = 0,
    val name: String = "",
    val type: GroupType = GroupType.CHANNEL,
    val isVisible: Boolean = true,
    val order: Int = 0,
    val isCustom: Boolean = false
)

enum class GroupType { MOVIE, SERIE, CHANNEL }

data class Playlist(
    val id: Long = 0,
    val name: String = "",
    val type: PlaylistType = PlaylistType.M3U,
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val serverUrl: String = "",
    val isActive: Boolean = false,
    val lastUpdated: Long = 0L,
    val expiryDate: Long = 0L,
    val movieCount: Int = 0,
    val serieCount: Int = 0,
    val channelCount: Int = 0
)

enum class PlaylistType { M3U, XTREAM, STALKER }

data class Profile(
    val id: Long = 0,
    val name: String = "",
    val avatarUrl: String = "",
    val isActive: Boolean = false,
    val playlistId: Long = 0
)

data class WatchHistory(
    val id: Long = 0,
    val contentId: Long = 0,
    val contentType: ContentType = ContentType.MOVIE,
    val title: String = "",
    val posterUrl: String = "",
    val progress: Long = 0L,
    val duration: Long = 0L,
    val watchedAt: Long = 0L
)

enum class ContentType { MOVIE, SERIE, EPISODE, CHANNEL }

data class Recording(
    val id: Long = 0,
    val channelId: Long = 0,
    val channelName: String = "",
    val programTitle: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val filePath: String = "",
    val status: RecordingStatus = RecordingStatus.SCHEDULED,
    val smbServer: String = ""
)

enum class RecordingStatus { SCHEDULED, RECORDING, COMPLETED, FAILED }

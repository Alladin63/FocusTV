package com.alladin63.maxtv.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val streamId: Long = 0,
    val title: String = "",
    val coverUrl: String = "",
    val backdropUrl: String = "",
    val overview: String = "",
    val rating: Float = 0f,
    val year: String = "",
    val genres: String = "",
    val duration: Int = 0,
    val tmdbId: Int? = null,
    val groupId: Long = 0,
    val groupName: String = "",
    val streamUrl: String = "",
    val playlistId: Long = 0,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "series")
data class SerieEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seriesId: Long = 0,
    val title: String = "",
    val coverUrl: String = "",
    val backdropUrl: String = "",
    val overview: String = "",
    val rating: Float = 0f,
    val year: String = "",
    val genres: String = "",
    val tmdbId: Int? = null,
    val groupId: Long = 0,
    val groupName: String = "",
    val playlistId: Long = 0,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val episodeId: Long = 0,
    val serieId: Long = 0,
    val title: String = "",
    val overview: String = "",
    val thumbnailUrl: String = "",
    val seasonNumber: Int = 0,
    val episodeNumber: Int = 0,
    val duration: Int = 0,
    val streamUrl: String = ""
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val streamId: Long = 0,
    val name: String = "",
    val logoUrl: String = "",
    val groupId: Long = 0,
    val groupName: String = "",
    val streamUrl: String = "",
    val playlistId: Long = 0,
    val order: Int = 0
)

@Entity(tableName = "epg_programs")
data class EpgProgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: Long = 0,
    val epgChannelId: String = "",
    val title: String = "",
    val description: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val posterUrl: String = "",
    val category: String = ""
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val type: String = "CHANNEL",
    val isVisible: Boolean = true,
    val order: Int = 0,
    val isCustom: Boolean = false,
    val playlistId: Long = 0
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val type: String = "M3U",
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

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String = "",
    val avatarUrl: String = "",
    val isActive: Boolean = false,
    val playlistId: Long = 0
)

@Entity(tableName = "movie_favorites")
data class MovieFavoriteEntity(
    @PrimaryKey val movieId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "serie_favorites")
data class SerieFavoriteEntity(
    @PrimaryKey val serieId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "channel_favorites")
data class ChannelFavoriteEntity(
    @PrimaryKey val channelId: Long,
    val order: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contentId: Long = 0,
    val contentType: String = "MOVIE",
    val title: String = "",
    val posterUrl: String = "",
    val progress: Long = 0L,
    val duration: Long = 0L,
    val watchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "movie_progress")
data class MovieProgressEntity(
    @PrimaryKey val movieId: Long,
    val progress: Long = 0L,
    val duration: Long = 0L,
    val isWatched: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "episode_progress")
data class EpisodeProgressEntity(
    @PrimaryKey val episodeId: Long,
    val progress: Long = 0L,
    val duration: Long = 0L,
    val isWatched: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: Long = 0,
    val channelName: String = "",
    val programTitle: String = "",
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val filePath: String = "",
    val status: String = "SCHEDULED",
    val smbServer: String = ""
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val channelId: Long = 0,
    val programTitle: String = "",
    val startTime: Long = 0L,
    val notifyBefore: Int = 5
)

@Entity(tableName = "custom_group_entries")
data class CustomGroupEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long = 0,
    val contentId: Long = 0,
    val contentType: String = "MOVIE"
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String
)

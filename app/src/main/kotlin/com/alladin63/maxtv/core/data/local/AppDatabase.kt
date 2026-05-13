package com.alladin63.maxtv.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alladin63.maxtv.core.data.local.dao.*
import com.alladin63.maxtv.core.data.local.entities.*

@Database(
    entities = [
        MovieEntity::class,
        SerieEntity::class,
        EpisodeEntity::class,
        ChannelEntity::class,
        EpgProgramEntity::class,
        GroupEntity::class,
        PlaylistEntity::class,
        ProfileEntity::class,
        MovieFavoriteEntity::class,
        SerieFavoriteEntity::class,
        ChannelFavoriteEntity::class,
        WatchHistoryEntity::class,
        MovieProgressEntity::class,
        EpisodeProgressEntity::class,
        RecordingEntity::class,
        ReminderEntity::class,
        CustomGroupEntryEntity::class,
        SettingsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun serieDao(): SerieDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun channelDao(): ChannelDao
    abstract fun epgDao(): EpgDao
    abstract fun groupDao(): GroupDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun profileDao(): ProfileDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun progressDao(): ProgressDao
    abstract fun recordingDao(): RecordingDao
}

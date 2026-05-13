package com.alladin63.maxtv.core.data.local.dao

import androidx.room.*
import com.alladin63.maxtv.core.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies WHERE playlistId = :playlistId ORDER BY title ASC")
    fun getAllMovies(playlistId: Long): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE groupId = :groupId AND playlistId = :playlistId ORDER BY title ASC")
    fun getMoviesByGroup(groupId: Long, playlistId: Long): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovieById(id: Long): MovieEntity?

    @Query("SELECT * FROM movies WHERE tmdbId = :tmdbId LIMIT 1")
    suspend fun getMovieByTmdbId(tmdbId: Int): MovieEntity?

    @Query("SELECT * FROM movies WHERE title LIKE '%' || :query || '%' AND playlistId = :playlistId LIMIT 50")
    fun searchMovies(query: String, playlistId: Long): Flow<List<MovieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Query("DELETE FROM movies WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylist(playlistId: Long)

    @Query("SELECT COUNT(*) FROM movies WHERE playlistId = :playlistId")
    suspend fun getCount(playlistId: Long): Int
}

@Dao
interface SerieDao {
    @Query("SELECT * FROM series WHERE playlistId = :playlistId ORDER BY title ASC")
    fun getAllSeries(playlistId: Long): Flow<List<SerieEntity>>

    @Query("SELECT * FROM series WHERE groupId = :groupId AND playlistId = :playlistId ORDER BY title ASC")
    fun getSeriesByGroup(groupId: Long, playlistId: Long): Flow<List<SerieEntity>>

    @Query("SELECT * FROM series WHERE id = :id")
    suspend fun getSerieById(id: Long): SerieEntity?

    @Query("SELECT * FROM series WHERE title LIKE '%' || :query || '%' AND playlistId = :playlistId LIMIT 50")
    fun searchSeries(query: String, playlistId: Long): Flow<List<SerieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: List<SerieEntity>)

    @Query("DELETE FROM series WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylist(playlistId: Long)
}

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE serieId = :serieId ORDER BY seasonNumber ASC, episodeNumber ASC")
    fun getEpisodesBySerie(serieId: Long): Flow<List<EpisodeEntity>>

    @Query("SELECT * FROM episodes WHERE serieId = :serieId AND seasonNumber = :season ORDER BY episodeNumber ASC")
    fun getEpisodesBySeason(serieId: Long, season: Int): Flow<List<EpisodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodes(episodes: List<EpisodeEntity>)

    @Query("DELETE FROM episodes WHERE serieId = :serieId")
    suspend fun deleteBySerieId(serieId: Long)
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels WHERE playlistId = :playlistId ORDER BY `order` ASC, name ASC")
    fun getAllChannels(playlistId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE groupId = :groupId AND playlistId = :playlistId ORDER BY `order` ASC")
    fun getChannelsByGroup(groupId: Long, playlistId: Long): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE id = :id")
    suspend fun getChannelById(id: Long): ChannelEntity?

    @Query("SELECT * FROM channels WHERE name LIKE '%' || :query || '%' AND playlistId = :playlistId LIMIT 50")
    fun searchChannels(query: String, playlistId: Long): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<ChannelEntity>)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylist(playlistId: Long)
}

@Dao
interface EpgDao {
    @Query("SELECT * FROM epg_programs WHERE channelId = :channelId AND endTime > :now ORDER BY startTime ASC")
    fun getProgramsForChannel(channelId: Long, now: Long = System.currentTimeMillis()): Flow<List<EpgProgramEntity>>

    @Query("SELECT * FROM epg_programs WHERE epgChannelId = :tvgId AND endTime > :now ORDER BY startTime ASC")
    fun getByChannelFlow(tvgId: String, now: Long = System.currentTimeMillis()): Flow<List<EpgProgramEntity>>

    @Query("SELECT * FROM epg_programs WHERE channelId = :channelId AND startTime <= :now AND endTime >= :now LIMIT 1")
    suspend fun getCurrentProgram(channelId: Long, now: Long = System.currentTimeMillis()): EpgProgramEntity?

    @Query("SELECT * FROM epg_programs WHERE epgChannelId = :tvgId AND startTime <= :now AND endTime >= :now LIMIT 1")
    fun getCurrentProgramFlow(tvgId: String, now: Long = System.currentTimeMillis()): Flow<EpgProgramEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrograms(programs: List<EpgProgramEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(programs: List<EpgProgramEntity>)

    @Query("DELETE FROM epg_programs WHERE endTime < :before")
    suspend fun deleteOldPrograms(before: Long)

    @Query("DELETE FROM epg_programs")
    suspend fun deleteAll()
}

@Dao
interface GroupDao {
    @Query("SELECT * FROM groups WHERE playlistId = :playlistId AND type = :type ORDER BY `order` ASC, name ASC")
    fun getGroupsByType(playlistId: Long, type: String): Flow<List<GroupEntity>>

    @Query("SELECT * FROM groups WHERE id = :id")
    suspend fun getGroupById(id: Long): GroupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<GroupEntity>)

    @Update
    suspend fun updateGroup(group: GroupEntity)

    @Query("DELETE FROM groups WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylist(playlistId: Long)
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY id ASC")
    fun getAllPlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE isActive = 1 LIMIT 1")
    fun getActivePlaylist(): Flow<PlaylistEntity?>

    @Query("SELECT * FROM playlists WHERE isActive = 1 LIMIT 1")
    suspend fun getActivePlaylistSync(): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun deletePlaylist(id: Long)

    @Query("UPDATE playlists SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE playlists SET isActive = 1 WHERE id = :id")
    suspend fun activatePlaylist(id: Long)
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<ProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfile(id: Long)

    @Query("UPDATE profiles SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE profiles SET isActive = 1 WHERE id = :id")
    suspend fun activateProfile(id: Long)
}

@Dao
interface FavoriteDao {
    @Query("SELECT m.* FROM movies m INNER JOIN movie_favorites f ON m.id = f.movieId ORDER BY f.addedAt DESC")
    fun getFavoriteMovies(): Flow<List<MovieEntity>>

    @Query("SELECT s.* FROM series s INNER JOIN serie_favorites f ON s.id = f.serieId ORDER BY f.addedAt DESC")
    fun getFavoriteSeries(): Flow<List<SerieEntity>>

    @Query("SELECT c.* FROM channels c INNER JOIN channel_favorites f ON c.id = f.channelId ORDER BY f.`order` ASC, f.addedAt DESC")
    fun getFavoriteChannels(): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMovieFavorite(fav: MovieFavoriteEntity)

    @Delete
    suspend fun removeMovieFavorite(fav: MovieFavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addSerieFavorite(fav: SerieFavoriteEntity)

    @Delete
    suspend fun removeSerieFavorite(fav: SerieFavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addChannelFavorite(fav: ChannelFavoriteEntity)

    @Delete
    suspend fun removeChannelFavorite(fav: ChannelFavoriteEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM movie_favorites WHERE movieId = :id)")
    fun isMovieFavorite(id: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM serie_favorites WHERE serieId = :id)")
    fun isSerieFavorite(id: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM channel_favorites WHERE channelId = :id)")
    fun isChannelFavorite(id: Long): Flow<Boolean>
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY watchedAt DESC LIMIT 100")
    fun getHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM watch_history")
    suspend fun deleteAll()
}

@Dao
interface ProgressDao {
    @Query("SELECT * FROM movie_progress WHERE movieId = :movieId")
    suspend fun getMovieProgress(movieId: Long): MovieProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMovieProgress(progress: MovieProgressEntity)

    @Query("SELECT * FROM episode_progress WHERE episodeId = :episodeId")
    suspend fun getEpisodeProgress(episodeId: Long): EpisodeProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEpisodeProgress(progress: EpisodeProgressEntity)
}

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recordings ORDER BY startTime DESC")
    fun getAllRecordings(): Flow<List<RecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: RecordingEntity): Long

    @Update
    suspend fun updateRecording(recording: RecordingEntity)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteRecording(id: Long)
}

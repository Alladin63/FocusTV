package com.alladin63.maxtv.core.util

import com.alladin63.maxtv.core.data.local.entities.*
import com.alladin63.maxtv.core.domain.model.*

object EntityMapper {

    fun toMovie(entity: MovieEntity) = Movie(
        id = entity.id,
        streamId = entity.streamId,
        title = entity.title,
        coverUrl = entity.coverUrl,
        backdropUrl = entity.backdropUrl,
        overview = entity.overview,
        rating = entity.rating,
        year = entity.year,
        genres = entity.genres.split(",").filter { it.isNotBlank() },
        duration = entity.duration,
        tmdbId = entity.tmdbId,
        groupId = entity.groupId,
        groupName = entity.groupName,
        streamUrl = entity.streamUrl
    )

    fun toMovieEntity(movie: Movie, playlistId: Long) = MovieEntity(
        id = movie.id,
        streamId = movie.streamId,
        title = movie.title,
        coverUrl = movie.coverUrl,
        backdropUrl = movie.backdropUrl,
        overview = movie.overview,
        rating = movie.rating,
        year = movie.year,
        genres = movie.genres.joinToString(","),
        duration = movie.duration,
        tmdbId = movie.tmdbId,
        groupId = movie.groupId,
        groupName = movie.groupName,
        streamUrl = movie.streamUrl,
        playlistId = playlistId
    )

    fun toSerie(entity: SerieEntity) = Serie(
        id = entity.id,
        seriesId = entity.seriesId,
        title = entity.title,
        coverUrl = entity.coverUrl,
        backdropUrl = entity.backdropUrl,
        overview = entity.overview,
        rating = entity.rating,
        year = entity.year,
        genres = entity.genres.split(",").filter { it.isNotBlank() },
        tmdbId = entity.tmdbId,
        groupId = entity.groupId,
        groupName = entity.groupName
    )

    fun toSerieEntity(serie: Serie, playlistId: Long) = SerieEntity(
        id = serie.id,
        seriesId = serie.seriesId,
        title = serie.title,
        coverUrl = serie.coverUrl,
        backdropUrl = serie.backdropUrl,
        overview = serie.overview,
        rating = serie.rating,
        year = serie.year,
        genres = serie.genres.joinToString(","),
        tmdbId = serie.tmdbId,
        groupId = serie.groupId,
        groupName = serie.groupName,
        playlistId = playlistId
    )

    fun toChannel(entity: ChannelEntity) = Channel(
        id = entity.id,
        streamId = entity.streamId,
        name = entity.name,
        logoUrl = entity.logoUrl,
        groupId = entity.groupId,
        groupName = entity.groupName,
        streamUrl = entity.streamUrl
    )

    fun toChannelEntity(channel: Channel, playlistId: Long) = ChannelEntity(
        id = channel.id,
        streamId = channel.streamId,
        name = channel.name,
        logoUrl = channel.logoUrl,
        groupId = channel.groupId,
        groupName = channel.groupName,
        streamUrl = channel.streamUrl,
        playlistId = playlistId
    )

    fun toEpisode(entity: EpisodeEntity) = Episode(
        id = entity.id,
        episodeId = entity.episodeId,
        title = entity.title,
        overview = entity.overview,
        thumbnailUrl = entity.thumbnailUrl,
        seasonNumber = entity.seasonNumber,
        episodeNumber = entity.episodeNumber,
        duration = entity.duration,
        streamUrl = entity.streamUrl
    )

    fun toWatchHistory(entity: WatchHistoryEntity) = WatchHistory(
        id = entity.id,
        contentId = entity.contentId,
        contentType = ContentType.valueOf(entity.contentType),
        title = entity.title,
        posterUrl = entity.posterUrl,
        progress = entity.progress,
        duration = entity.duration,
        watchedAt = entity.watchedAt
    )

    fun toGroup(entity: GroupEntity) = Group(
        id = entity.id,
        name = entity.name,
        type = GroupType.valueOf(entity.type),
        isVisible = entity.isVisible,
        order = entity.order,
        isCustom = entity.isCustom
    )

    fun toPlaylist(entity: com.alladin63.maxtv.core.data.local.entities.PlaylistEntity) = Playlist(
        id = entity.id,
        name = entity.name,
        type = PlaylistType.valueOf(entity.type),
        url = entity.url,
        username = entity.username,
        password = entity.password,
        serverUrl = entity.serverUrl,
        isActive = entity.isActive,
        lastUpdated = entity.lastUpdated,
        expiryDate = entity.expiryDate,
        movieCount = entity.movieCount,
        serieCount = entity.serieCount,
        channelCount = entity.channelCount
    )

    fun toProfile(entity: ProfileEntity) = Profile(
        id = entity.id,
        name = entity.name,
        avatarUrl = entity.avatarUrl,
        isActive = entity.isActive,
        playlistId = entity.playlistId
    )

    fun toEpgProgram(entity: EpgProgramEntity) = EpgProgram(
        id = entity.id,
        channelId = entity.channelId,
        title = entity.title,
        description = entity.description,
        startTime = entity.startTime,
        endTime = entity.endTime,
        posterUrl = entity.posterUrl,
        category = entity.category
    )

    // Alias conservés pour les anciennes parties du code
    fun movieToDomain(entity: MovieEntity) = toMovie(entity)
    fun serieToDomain(entity: SerieEntity) = toSerie(entity)
    fun channelToDomain(entity: ChannelEntity) = toChannel(entity)
    fun episodeToDomain(entity: EpisodeEntity) = toEpisode(entity)
    fun historyToDomain(entity: WatchHistoryEntity) = toWatchHistory(entity)
    fun playlistToDomain(entity: PlaylistEntity) = toPlaylist(entity)
    fun epgToDomain(entity: EpgProgramEntity) = toEpgProgram(entity)

}

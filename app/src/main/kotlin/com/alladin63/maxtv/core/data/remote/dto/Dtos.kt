package com.alladin63.maxtv.core.data.remote.dto

import com.google.gson.annotations.SerializedName

// ─── TMDB ───────────────────────────────────────────────────────────────────

data class TmdbMovieDto(
    val id: Int = 0,
    val title: String = "",
    @SerializedName("original_title") val originalTitle: String = "",
    val overview: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("vote_average") val voteAverage: Float = 0f,
    @SerializedName("release_date") val releaseDate: String = "",
    val genres: List<TmdbGenreDto> = emptyList(),
    val runtime: Int = 0,
    val credits: TmdbCreditsDto? = null,
    val videos: TmdbVideosDto? = null,
    @SerializedName("imdb_id") val imdbId: String? = null
)

data class TmdbTvDto(
    val id: Int = 0,
    val name: String = "",
    val overview: String = "",
    @SerializedName("poster_path") val posterPath: String? = null,
    @SerializedName("backdrop_path") val backdropPath: String? = null,
    @SerializedName("vote_average") val voteAverage: Float = 0f,
    @SerializedName("first_air_date") val firstAirDate: String = "",
    val genres: List<TmdbGenreDto> = emptyList(),
    @SerializedName("number_of_seasons") val numberOfSeasons: Int = 0,
    @SerializedName("number_of_episodes") val numberOfEpisodes: Int = 0,
    val seasons: List<TmdbSeasonSummaryDto> = emptyList(),
    val credits: TmdbCreditsDto? = null
)

data class TmdbSeasonDto(
    val id: Int = 0,
    @SerializedName("season_number") val seasonNumber: Int = 0,
    val episodes: List<TmdbEpisodeDto> = emptyList()
)

data class TmdbEpisodeDto(
    val id: Int = 0,
    val name: String = "",
    val overview: String = "",
    @SerializedName("still_path") val stillPath: String? = null,
    @SerializedName("episode_number") val episodeNumber: Int = 0,
    @SerializedName("season_number") val seasonNumber: Int = 0,
    val runtime: Int = 0
)

data class TmdbSeasonSummaryDto(
    val id: Int = 0,
    @SerializedName("season_number") val seasonNumber: Int = 0,
    @SerializedName("episode_count") val episodeCount: Int = 0,
    @SerializedName("poster_path") val posterPath: String? = null
)

data class TmdbGenreDto(val id: Int = 0, val name: String = "")

data class TmdbCreditsDto(
    val cast: List<TmdbCastDto> = emptyList(),
    val crew: List<TmdbCrewDto> = emptyList()
)

data class TmdbCastDto(
    val id: Int = 0,
    val name: String = "",
    val character: String = "",
    @SerializedName("profile_path") val profilePath: String? = null
)

data class TmdbCrewDto(
    val id: Int = 0,
    val name: String = "",
    val job: String = "",
    @SerializedName("profile_path") val profilePath: String? = null
)

data class TmdbVideosDto(val results: List<TmdbVideoDto> = emptyList())

data class TmdbVideoDto(
    val key: String = "",
    val site: String = "",
    val type: String = "",
    val official: Boolean = false
)

data class TmdbSearchResponse<T>(
    val results: List<T> = emptyList(),
    val page: Int = 1,
    @SerializedName("total_results") val totalResults: Int = 0,
    @SerializedName("total_pages") val totalPages: Int = 0
)

data class TmdbExternalIdsDto(
    val id: Int = 0,
    @SerializedName("imdb_id") val imdbId: String? = null,
    @SerializedName("tvdb_id") val tvdbId: Int? = null
)

data class TmdbConfigDto(
    val images: TmdbImagesConfigDto = TmdbImagesConfigDto()
)

data class TmdbImagesConfigDto(
    @SerializedName("secure_base_url") val secureBaseUrl: String = "https://image.tmdb.org/t/p/",
    @SerializedName("poster_sizes") val posterSizes: List<String> = emptyList(),
    @SerializedName("backdrop_sizes") val backdropSizes: List<String> = emptyList()
)

// ─── FANART ──────────────────────────────────────────────────────────────────

data class FanartMovieDto(
    val name: String = "",
    val tmdbid: String = "",
    val hdmovielogo: List<FanartImageDto> = emptyList(),
    val moviebackground: List<FanartImageDto> = emptyList(),
    val movieposter: List<FanartImageDto> = emptyList(),
    val moviedisc: List<FanartImageDto> = emptyList(),
    val moviebanner: List<FanartImageDto> = emptyList()
)

data class FanartTvDto(
    val name: String = "",
    val thetvdb_id: String = "",
    val hdtvlogo: List<FanartImageDto> = emptyList(),
    val showbackground: List<FanartImageDto> = emptyList(),
    val tvposter: List<FanartImageDto> = emptyList(),
    val tvbanner: List<FanartImageDto> = emptyList(),
    val clearlogo: List<FanartImageDto> = emptyList()
)

data class FanartImageDto(
    val id: String = "",
    val url: String = "",
    val lang: String = "",
    val likes: String = "0"
)

// ─── XTREAM ──────────────────────────────────────────────────────────────────

data class XtreamAuthResponse(
    @SerializedName("user_info") val userInfo: XtreamUserInfo? = null,
    @SerializedName("server_info") val serverInfo: XtreamServerInfo? = null
)

data class XtreamUserInfo(
    val username: String = "",
    val password: String = "",
    val status: String = "",
    @SerializedName("exp_date") val expDate: String? = null,
    @SerializedName("max_connections") val maxConnections: String = "1",
    @SerializedName("active_cons") val activeCons: String = "0"
)

data class XtreamServerInfo(
    val url: String = "",
    val port: String = "",
    val https_port: String = "",
    val server_protocol: String = "http",
    val rtmp_port: String = "",
    val timezone: String = ""
)

data class XtreamStreamDto(
    @SerializedName("stream_id") val streamId: Long = 0,
    val name: String = "",
    @SerializedName("stream_icon") val streamIcon: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    val num: Int = 0,
    @SerializedName("stream_type") val streamType: String = "",
    @SerializedName("container_extension") val containerExtension: String = "",
    @SerializedName("cover_big") val coverBig: String? = null,
    val plot: String = "",
    val cast: String = "",
    val director: String = "",
    val genre: String = "",
    @SerializedName("release_date") val releaseDate: String = "",
    val rating: String = "0",
    @SerializedName("tmdb_id") val tmdbId: String? = null
)

data class XtreamSerieDto(
    @SerializedName("series_id") val seriesId: Long = 0,
    val name: String = "",
    val cover: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    val plot: String = "",
    val cast: String = "",
    val director: String = "",
    val genre: String = "",
    @SerializedName("release_date") val releaseDate: String = "",
    val rating: String = "0",
    @SerializedName("tmdb_id") val tmdbId: String? = null,
    @SerializedName("backdrop_path") val backdropPath: List<String>? = null
)

data class XtreamSerieInfoDto(
    val info: XtreamSerieInfoDetail? = null,
    val episodes: Map<String, List<XtreamEpisodeDto>>? = null
)

data class XtreamSerieInfoDetail(
    val name: String = "",
    val cover: String = "",
    val plot: String = "",
    val cast: String = "",
    val director: String = "",
    val genre: String = "",
    @SerializedName("tmdb_id") val tmdbId: String? = null,
    val rating: String = "0",
    @SerializedName("backdrop_path") val backdropPath: List<String>? = null
)

data class XtreamEpisodeDto(
    val id: String = "",
    @SerializedName("episode_num") val episodeNum: Int = 0,
    val title: String = "",
    @SerializedName("container_extension") val containerExtension: String = "mkv",
    val info: XtreamEpisodeInfo? = null,
    @SerializedName("season") val season: Int = 1
)

data class XtreamEpisodeInfo(
    val plot: String = "",
    @SerializedName("movie_image") val movieImage: String = "",
    val duration: String = "",
    val rating: String = ""
)

data class XtreamCategoryDto(
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("category_name") val categoryName: String = "",
    @SerializedName("parent_id") val parentId: Int = 0
)

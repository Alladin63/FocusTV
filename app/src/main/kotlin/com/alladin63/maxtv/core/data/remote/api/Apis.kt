package com.alladin63.maxtv.core.data.remote.api

import com.alladin63.maxtv.core.data.remote.dto.*
import retrofit2.http.*

interface TmdbApi {
    @GET("movie/{id}")
    suspend fun getMovieDetails(
        @Path("id") tmdbId: Int,
        @Query("language") language: String = "fr-FR",
        @Query("append_to_response") append: String = "credits,videos,images"
    ): TmdbMovieDto

    @GET("tv/{id}")
    suspend fun getTvDetails(
        @Path("id") tmdbId: Int,
        @Query("language") language: String = "fr-FR",
        @Query("append_to_response") append: String = "credits,videos,images"
    ): TmdbTvDto

    @GET("tv/{id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @Path("id") seriesId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("language") language: String = "fr-FR"
    ): TmdbSeasonDto

    @GET("search/movie")
    suspend fun searchMovie(
        @Query("query") query: String,
        @Query("language") language: String = "fr-FR",
        @Query("page") page: Int = 1
    ): TmdbSearchResponse<TmdbMovieDto>

    @GET("search/tv")
    suspend fun searchTv(
        @Query("query") query: String,
        @Query("language") language: String = "fr-FR",
        @Query("page") page: Int = 1
    ): TmdbSearchResponse<TmdbTvDto>

    @GET("movie/{id}/external_ids")
    suspend fun getMovieExternalIds(@Path("id") tmdbId: Int): TmdbExternalIdsDto

    @GET("configuration")
    suspend fun getConfiguration(): TmdbConfigDto
}

interface FanartApi {
    @GET("movies/{tmdb_id}")
    suspend fun getMovieArtwork(
        @Path("tmdb_id") tmdbId: Int,
        @Query("api_key") apiKey: String
    ): FanartMovieDto

    @GET("tv/{tmdb_id}")
    suspend fun getTvArtwork(
        @Path("tmdb_id") tmdbId: Int,
        @Query("api_key") apiKey: String
    ): FanartTvDto
}

interface XtreamApi {
    @GET("player_api.php")
    suspend fun authenticate(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series"
    ): XtreamAuthResponse

    @GET("player_api.php")
    suspend fun getLiveStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams"
    ): List<XtreamStreamDto>

    @GET("player_api.php")
    suspend fun getVodStreams(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams"
    ): List<XtreamStreamDto>

    @GET("player_api.php")
    suspend fun getSeries(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series"
    ): List<XtreamSerieDto>

    @GET("player_api.php")
    suspend fun getSeriesInfo(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_info",
        @Query("series_id") seriesId: Long
    ): XtreamSerieInfoDto

    @GET("player_api.php")
    suspend fun getLiveCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_categories"
    ): List<XtreamCategoryDto>

    @GET("player_api.php")
    suspend fun getVodCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_categories"
    ): List<XtreamCategoryDto>

    @GET("player_api.php")
    suspend fun getSeriesCategories(
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series_categories"
    ): List<XtreamCategoryDto>
}

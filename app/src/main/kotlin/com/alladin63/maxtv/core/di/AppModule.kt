package com.alladin63.maxtv.core.di

import android.content.Context
import androidx.room.Room
import com.alladin63.maxtv.BuildConfig
import com.alladin63.maxtv.core.data.local.AppDatabase
import com.alladin63.maxtv.core.data.local.dao.*
import com.alladin63.maxtv.core.data.remote.api.FanartApi
import com.alladin63.maxtv.core.data.remote.api.TmdbApi
import com.alladin63.maxtv.core.data.remote.api.XtreamApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "maxtv_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideMovieDao(db: AppDatabase) = db.movieDao()
    @Provides fun provideSerieDao(db: AppDatabase) = db.serieDao()
    @Provides fun provideEpisodeDao(db: AppDatabase) = db.episodeDao()
    @Provides fun provideChannelDao(db: AppDatabase) = db.channelDao()
    @Provides fun provideEpgDao(db: AppDatabase) = db.epgDao()
    @Provides fun provideGroupDao(db: AppDatabase) = db.groupDao()
    @Provides fun providePlaylistDao(db: AppDatabase) = db.playlistDao()
    @Provides fun provideProfileDao(db: AppDatabase) = db.profileDao()
    @Provides fun provideFavoriteDao(db: AppDatabase) = db.favoriteDao()
    @Provides fun provideWatchHistoryDao(db: AppDatabase) = db.watchHistoryDao()
    @Provides fun provideProgressDao(db: AppDatabase) = db.progressDao()
    @Provides fun provideRecordingDao(db: AppDatabase) = db.recordingDao()
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG)
                    HttpLoggingInterceptor.Level.BODY
                else
                    HttpLoggingInterceptor.Level.NONE
            }
        )
        .build()

    @Provides
    @Singleton
    @Named("tmdb")
    fun provideTmdbRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        val client = okHttpClient.newBuilder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${BuildConfig.TMDB_TOKEN}")
                    .addHeader("accept", "application/json")
                    .build()
                chain.proceed(request)
            }.build()

        return Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    @Named("fanart")
    fun provideFanartRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://webservice.fanart.tv/v3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    fun provideTmdbApi(@Named("tmdb") retrofit: Retrofit): TmdbApi =
        retrofit.create(TmdbApi::class.java)

    @Provides
    @Singleton
    fun provideFanartApi(@Named("fanart") retrofit: Retrofit): FanartApi =
        retrofit.create(FanartApi::class.java)

}

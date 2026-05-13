package com.alladin63.maxtv.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.alladin63.maxtv.core.data.local.AppDatabase
import com.alladin63.maxtv.core.data.remote.api.TmdbApi
import com.alladin63.maxtv.core.data.repository.*
import com.alladin63.maxtv.core.util.M3UParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences>
    by preferencesDataStore(name = "maxtv_prefs")

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.dataStore

    @Provides
    @Singleton
    fun provideM3UParser(): M3UParser = M3UParser

    @Provides
    @Singleton
    fun providePlaylistRepository(
        db: AppDatabase,
        okHttpClient: OkHttpClient,
        m3uParser: M3UParser,
    ): PlaylistRepository = PlaylistRepository(db, okHttpClient, m3uParser)

    @Provides
    @Singleton
    fun provideContentRepository(
        db: AppDatabase,
        tmdbApi: TmdbApi,
    ): ContentRepository = ContentRepository(db, tmdbApi)

    @Provides
    @Singleton
    fun provideHistoryRepository(
        db: AppDatabase,
    ): HistoryRepository = HistoryRepository(db)

    @Provides
    @Singleton
    fun provideEpgRepository(
        db: AppDatabase,
        okHttpClient: OkHttpClient,
    ): EpgRepository = EpgRepository(db, okHttpClient)
}

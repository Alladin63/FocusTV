package com.focustv.app.services

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.focustv.app.core.AppSettings
import com.focustv.app.core.IptvSource
import com.focustv.app.core.MediaContent
import com.focustv.app.core.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.focusTvStore by preferencesDataStore("focustv_store")

class StorageService(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val sourcesKey = stringPreferencesKey("sources")
    private val favoritesKey = stringPreferencesKey("favorites")
    private val historyKey = stringPreferencesKey("history")
    private val profilesKey = stringPreferencesKey("profiles")
    private val settingsKey = stringPreferencesKey("settings")

    suspend fun loadSources(): List<IptvSource> = read(sourcesKey, emptyList())
    suspend fun saveSources(v: List<IptvSource>) = write(sourcesKey, v)

    suspend fun loadFavorites(): Set<String> = read<List<String>>(favoritesKey, emptyList()).toSet()
    suspend fun saveFavorites(v: Set<String>) = write(favoritesKey, v.toList())

    suspend fun loadHistory(): List<MediaContent> = read(historyKey, emptyList())
    suspend fun saveHistory(v: List<MediaContent>) = write(historyKey, v.take(100))

    suspend fun loadProfiles(): List<UserProfile> = read(profilesKey, listOf(UserProfile(name = "Principal")))
    suspend fun saveProfiles(v: List<UserProfile>) = write(profilesKey, v)

    suspend fun loadSettings(): AppSettings = read(settingsKey, AppSettings())
    suspend fun saveSettings(v: AppSettings) = write(settingsKey, v)

    private suspend inline fun <reified T> read(key: androidx.datastore.preferences.core.Preferences.Key<String>, default: T): T {
        return try {
            val prefs = context.focusTvStore.data.first()
            prefs[key]?.let { json.decodeFromString<T>(it) } ?: default
        } catch (_: Exception) {
            default
        }
    }

    private suspend inline fun <reified T> write(key: androidx.datastore.preferences.core.Preferences.Key<String>, value: T) {
        context.focusTvStore.edit { prefs ->
            prefs[key] = json.encodeToString(value)
        }
    }
}

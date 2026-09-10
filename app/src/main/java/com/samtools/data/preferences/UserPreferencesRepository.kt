package com.samtools.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sam_tools_settings")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SUBFOLDER = stringPreferencesKey("downloads_subfolder")
        val INSTANT_SAVE = booleanPreferencesKey("instant_save")
        val OVERWRITE_DUPLICATES = booleanPreferencesKey("overwrite_duplicates")
        val ENABLE_TAILSCALE_CONNECT = booleanPreferencesKey("enable_tailscale_connect")
        val ENABLE_TAILSCALE_DISCONNECT = booleanPreferencesKey("enable_tailscale_disconnect")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            UserPreferences(
                subfolder = preferences[PreferencesKeys.SUBFOLDER] ?: "",
                instantSave = preferences[PreferencesKeys.INSTANT_SAVE] ?: false,
                overwriteDuplicates = preferences[PreferencesKeys.OVERWRITE_DUPLICATES] ?: false,
                enableTailscaleConnect = preferences[PreferencesKeys.ENABLE_TAILSCALE_CONNECT] ?: true,
                enableTailscaleDisconnect = preferences[PreferencesKeys.ENABLE_TAILSCALE_DISCONNECT] ?: true
            )
        }

    suspend fun updateSubfolder(subfolder: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SUBFOLDER] = subfolder.trim()
        }
    }

    suspend fun updateInstantSave(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.INSTANT_SAVE] = enabled
        }
    }

    suspend fun updateOverwriteDuplicates(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OVERWRITE_DUPLICATES] = enabled
        }
    }

    suspend fun updateEnableTailscaleConnect(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_TAILSCALE_CONNECT] = enabled
        }
    }

    suspend fun updateEnableTailscaleDisconnect(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_TAILSCALE_DISCONNECT] = enabled
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: UserPreferencesRepository? = null

        fun getInstance(context: Context): UserPreferencesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserPreferencesRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}

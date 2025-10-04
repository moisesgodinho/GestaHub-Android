// Local: app/src/main/java/br/com/gestahub/data/ThemeManager.kt
package br.com.gestahub.data // <-- Corrigido (sem .app)

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemeManager(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
    }

    val isDarkTheme = dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE_KEY] ?: false
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        dataStore.edit { settings ->
            settings[IS_DARK_MODE_KEY] = isDark
        }
    }
}
package com.bpo.gasapp.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.bpo.gasapp.domain.model.AppSettings
import com.bpo.gasapp.domain.model.FuelType
import com.bpo.gasapp.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[KEY_THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
            defaultFuel = prefs[KEY_FUEL]?.let { runCatching { FuelType.valueOf(it) }.getOrNull() }
                ?: FuelType.GASOLINA_95
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME] = mode.name }
    }

    suspend fun setDefaultFuel(fuel: FuelType) {
        dataStore.edit { it[KEY_FUEL] = fuel.name }
    }

    private companion object {
        val KEY_THEME = stringPreferencesKey("theme_mode")
        val KEY_FUEL = stringPreferencesKey("default_fuel")
    }
}

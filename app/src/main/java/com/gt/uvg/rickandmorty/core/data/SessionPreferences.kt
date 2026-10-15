package com.gt.uvg.rickandmorty.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// Instancia de DataStore. Va fuera de la clase para que exista una sola en toda la app
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

/**
 * Guarda los datos de la sesión del usuario en DataStore Preferences.
 */
class SessionPreferences(private val context: Context) {

    // Llaves con las que se guardan los valores
    private val nameKey = stringPreferencesKey("name")

    // Nombre del usuario. Si no se ha guardado nada, devuelve "" (no hay sesión)
    val name: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[nameKey] ?: ""
    }

    // Lee el nombre una sola vez (sin quedarse escuchando cambios)
    suspend fun getName(): String {
        return name.first()
    }

    suspend fun saveName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[nameKey] = name
        }
    }

    suspend fun clearName() {
        context.dataStore.edit { preferences ->
            preferences.remove(nameKey)
        }
    }
}

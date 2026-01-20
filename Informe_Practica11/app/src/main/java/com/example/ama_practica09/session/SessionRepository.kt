package com.example.ama_practica09.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio para gestionar la persistencia de la sesión de usuario
 * Utiliza DataStore para almacenamiento seguro y asíncrono
 *
 * SEGURIDAD:
 * - No guarda contraseñas ni tokens sensibles
 * - Solo almacena información básica del usuario para mantener sesión
 * - Los datos se guardan encriptados por el sistema Android
 */
class SessionRepository(private val context: Context) {

    companion object {
        // DataStore para preferencias de sesión
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

        // Claves para los datos de sesión
        private val KEY_FIREBASE_UID = stringPreferencesKey("firebase_uid")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_DISPLAY_NAME = stringPreferencesKey("display_name")
        private val KEY_LAST_LOGIN = longPreferencesKey("last_login_timestamp")
    }

    /**
     * Guarda los datos de sesión en DataStore
     * @param sessionData Datos de la sesión a guardar
     */
    suspend fun saveSessionData(sessionData: SessionData) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FIREBASE_UID] = sessionData.firebaseUid
            preferences[KEY_EMAIL] = sessionData.email ?: ""
            preferences[KEY_DISPLAY_NAME] = sessionData.displayName ?: ""
            preferences[KEY_LAST_LOGIN] = sessionData.lastLoginTimestamp
        }
    }

    /**
     * Obtiene los datos de sesión como Flow (observable)
     * @return Flow con los datos de sesión o null si no hay sesión guardada
     */
    fun getSessionData(): Flow<SessionData?> {
        return context.dataStore.data.map { preferences ->
            val uid = preferences[KEY_FIREBASE_UID]

            if (uid != null && uid.isNotEmpty()) {
                SessionData(
                    firebaseUid = uid,
                    email = preferences[KEY_EMAIL],
                    displayName = preferences[KEY_DISPLAY_NAME],
                    lastLoginTimestamp = preferences[KEY_LAST_LOGIN] ?: 0L
                )
            } else {
                null
            }
        }
    }

    /**
     * Limpia todos los datos de sesión del almacenamiento local
     * Se llama al cerrar sesión
     */
    suspend fun clearSessionData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Verifica si existe una sesión guardada localmente
     * @return true si hay datos de sesión, false en caso contrario
     */
    suspend fun hasSessionData(): Boolean {
        var hasData = false
        context.dataStore.data.map { preferences ->
            hasData = preferences[KEY_FIREBASE_UID]?.isNotEmpty() == true
        }
        return hasData
    }

    /**
     * Actualiza el timestamp del último inicio de sesión
     */
    suspend fun updateLastLogin() {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_LOGIN] = System.currentTimeMillis()
        }
    }
}

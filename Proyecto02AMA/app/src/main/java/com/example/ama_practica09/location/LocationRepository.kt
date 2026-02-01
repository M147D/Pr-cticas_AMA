package com.example.ama_practica09.location

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

// Extension para DataStore
private val Context.locationDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "location_preferences"
)

/**
 * Repositorio para persistencia de datos de ubicación
 *
 * Maneja:
 * - Persistencia local con DataStore
 * - Persistencia remota con Firestore
 * - Historial de ubicaciones
 * - Eventos de geovallas
 */
class LocationRepository(private val context: Context) {

    companion object {
        private const val TAG = "LocationRepository"

        // Colecciones de Firestore
        private const val COLLECTION_GEOFENCE_EVENTS = "geofence_events"
        private const val COLLECTION_LOCATION_HISTORY = "location_history"

        // Keys para DataStore
        private val KEY_LAST_LATITUDE = doublePreferencesKey("last_latitude")
        private val KEY_LAST_LONGITUDE = doublePreferencesKey("last_longitude")
        private val KEY_LAST_ACCURACY = floatPreferencesKey("last_accuracy")
        private val KEY_LAST_TIMESTAMP = longPreferencesKey("last_timestamp")
        private val KEY_IS_INSIDE_EDIFICIO_SECUNDARIO = booleanPreferencesKey("is_inside_edificio_secundario")
        private val KEY_IS_INSIDE_EDIFICIO_PRINCIPAL = booleanPreferencesKey("is_inside_edificio_principal")
        private val KEY_IS_INSIDE_CAMPUS = booleanPreferencesKey("is_inside_campus")
        private val KEY_ACTIVE_ZONE_NAME = stringPreferencesKey("active_zone_name")
    }

    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /**
     * Guarda la última ubicación conocida en DataStore
     */
    suspend fun saveLastLocation(geoPoint: GeoPoint, geofenceStatus: GeofenceStatus) {
        context.locationDataStore.edit { preferences ->
            preferences[KEY_LAST_LATITUDE] = geoPoint.latitude
            preferences[KEY_LAST_LONGITUDE] = geoPoint.longitude
            preferences[KEY_LAST_ACCURACY] = geoPoint.accuracy
            preferences[KEY_LAST_TIMESTAMP] = geoPoint.timestamp
            preferences[KEY_IS_INSIDE_EDIFICIO_SECUNDARIO] = geofenceStatus.isInsideEdificioSecundario
            preferences[KEY_IS_INSIDE_EDIFICIO_PRINCIPAL] = geofenceStatus.isInsideEdificioPrincipal
            preferences[KEY_IS_INSIDE_CAMPUS] = geofenceStatus.isInsideCampus
            preferences[KEY_ACTIVE_ZONE_NAME] = geofenceStatus.getActiveZoneName()
        }
        Log.d(TAG, "Ubicación guardada localmente")
    }

    /**
     * Obtiene la última ubicación guardada como Flow
     */
    fun getLastLocationFlow(): Flow<GeoPoint?> {
        return context.locationDataStore.data.map { preferences ->
            val latitude = preferences[KEY_LAST_LATITUDE]
            val longitude = preferences[KEY_LAST_LONGITUDE]

            if (latitude != null && longitude != null) {
                GeoPoint(
                    latitude = latitude,
                    longitude = longitude,
                    accuracy = preferences[KEY_LAST_ACCURACY] ?: 0f,
                    timestamp = preferences[KEY_LAST_TIMESTAMP] ?: 0L
                )
            } else {
                null
            }
        }
    }

    /**
     * Obtiene el último estado de geovallas como Flow
     */
    fun getLastGeofenceStatusFlow(): Flow<GeofenceStatus> {
        return context.locationDataStore.data.map { preferences ->
            GeofenceStatus(
                isInsideEdificioSecundario = preferences[KEY_IS_INSIDE_EDIFICIO_SECUNDARIO] ?: false,
                isInsideEdificioPrincipal = preferences[KEY_IS_INSIDE_EDIFICIO_PRINCIPAL] ?: false,
                isInsideCampus = preferences[KEY_IS_INSIDE_CAMPUS] ?: false
            )
        }
    }

    /**
     * Guarda un evento de geovalla en Firestore
     */
    suspend fun saveGeofenceEvent(
        userId: String,
        event: GeofenceEvent
    ): Result<String> {
        return try {
            val entry = when (event) {
                is GeofenceEvent.Enter -> GeofenceEventEntry(
                    geofenceId = event.zone.id,
                    zoneName = event.zone.name,
                    timestamp = event.timestamp,
                    eventType = "ENTER",
                    userId = userId
                )
                is GeofenceEvent.Exit -> GeofenceEventEntry(
                    geofenceId = event.zone.id,
                    zoneName = event.zone.name,
                    timestamp = event.timestamp,
                    eventType = "EXIT",
                    userId = userId
                )
                is GeofenceEvent.Dwell -> GeofenceEventEntry(
                    geofenceId = event.zone.id,
                    zoneName = event.zone.name,
                    timestamp = event.timestamp,
                    eventType = "DWELL",
                    userId = userId
                )
                is GeofenceEvent.None -> return Result.failure(
                    IllegalArgumentException("No se puede guardar evento None")
                )
            }

            val documentRef = firestore.collection(COLLECTION_GEOFENCE_EVENTS)
                .add(entry.toMap())
                .await()

            Log.d(TAG, "Evento de geovalla guardado: ${documentRef.id}")
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar evento de geovalla", e)
            Result.failure(e)
        }
    }

    /**
     * Guarda una entrada de historial de ubicación en Firestore
     */
    suspend fun saveLocationHistory(
        userId: String,
        geoPoint: GeoPoint,
        geofenceStatus: GeofenceStatus
    ): Result<String> {
        return try {
            val entry = LocationHistoryEntry(
                userId = userId,
                latitude = geoPoint.latitude,
                longitude = geoPoint.longitude,
                accuracy = geoPoint.accuracy,
                timestamp = geoPoint.timestamp,
                isInsideEdificioSecundario = geofenceStatus.isInsideEdificioSecundario,
                isInsideEdificioPrincipal = geofenceStatus.isInsideEdificioPrincipal,
                isInsideMecanica = geofenceStatus.isInsideMecanica,
                isInsideCampus = geofenceStatus.isInsideCampus,
                zoneName = geofenceStatus.getActiveZoneName()
            )

            val documentRef = firestore.collection(COLLECTION_LOCATION_HISTORY)
                .add(entry.toMap())
                .await()

            Log.d(TAG, "Historial de ubicación guardado: ${documentRef.id}")
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar historial de ubicación", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene el historial de eventos de geovalla de un usuario
     */
    suspend fun getGeofenceEventsForUser(
        userId: String,
        limit: Int = 50
    ): Result<List<GeofenceEventEntry>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_GEOFENCE_EVENTS)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val events = snapshot.documents.mapNotNull { doc ->
                doc.toGeofenceEventEntry()
            }

            Result.success(events)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener eventos de geovalla", e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene el historial de ubicaciones de un usuario
     */
    suspend fun getLocationHistoryForUser(
        userId: String,
        limit: Int = 100
    ): Result<List<LocationHistoryEntry>> {
        return try {
            val snapshot = firestore.collection(COLLECTION_LOCATION_HISTORY)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val history = snapshot.documents.mapNotNull { doc ->
                doc.toLocationHistoryEntry()
            }

            Result.success(history)
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener historial de ubicación", e)
            Result.failure(e)
        }
    }

    /**
     * Limpia el historial local
     */
    suspend fun clearLocalHistory() {
        context.locationDataStore.edit { preferences ->
            preferences.clear()
        }
        Log.d(TAG, "Historial local limpiado")
    }

    // Extensiones de conversión

    private fun GeofenceEventEntry.toMap(): Map<String, Any> = mapOf(
        "geofenceId" to geofenceId,
        "zoneName" to zoneName,
        "timestamp" to timestamp,
        "eventType" to eventType,
        "userId" to userId
    )

    private fun LocationHistoryEntry.toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "latitude" to latitude,
        "longitude" to longitude,
        "accuracy" to accuracy,
        "timestamp" to timestamp,
        "isInsideEdificioSecundario" to isInsideEdificioSecundario,
        "isInsideEdificioPrincipal" to isInsideEdificioPrincipal,
        "isInsideMecanica" to isInsideMecanica,
        "isInsideCampus" to isInsideCampus,
        "zoneName" to zoneName
    )

    private fun com.google.firebase.firestore.DocumentSnapshot.toGeofenceEventEntry(): GeofenceEventEntry? {
        return try {
            GeofenceEventEntry(
                geofenceId = getString("geofenceId") ?: return null,
                zoneName = getString("zoneName") ?: return null,
                timestamp = getLong("timestamp") ?: return null,
                eventType = getString("eventType") ?: return null,
                userId = getString("userId") ?: return null
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toLocationHistoryEntry(): LocationHistoryEntry? {
        return try {
            LocationHistoryEntry(
                userId = getString("userId") ?: return null,
                latitude = getDouble("latitude") ?: return null,
                longitude = getDouble("longitude") ?: return null,
                accuracy = getDouble("accuracy")?.toFloat() ?: 0f,
                timestamp = getLong("timestamp") ?: return null,
                isInsideEdificioSecundario = getBoolean("isInsideEdificioSecundario") ?: false,
                isInsideEdificioPrincipal = getBoolean("isInsideEdificioPrincipal") ?: false,
                isInsideMecanica = getBoolean("isInsideMecanica") ?: false,
                isInsideCampus = getBoolean("isInsideCampus") ?: false,
                zoneName = getString("zoneName") ?: "Desconocido"
            )
        } catch (e: Exception) {
            null
        }
    }
}

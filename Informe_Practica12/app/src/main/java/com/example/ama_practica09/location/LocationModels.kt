package com.example.ama_practica09.location

/**
 * Modelos de datos para el sistema de geolocalización y geovallas
 */

/**
 * Representa un punto geográfico con coordenadas
 */
data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        // Coordenadas de la EPN (Escuela Politécnica Nacional) - Quito, Ecuador
        // Centro del Campus y Facultad de Ciencias
        val EPN_CENTER = GeoPoint(
            latitude = -0.211108,
            longitude = -78.489904
        )

        // Centro de la Facultad de Ingeniería Eléctrica y Electrónica (FIEE)
        val EDIFICIO_SECUNDARIO_CENTER = GeoPoint(
            latitude = -0.209502,
            longitude = -78.489513
        )
    }
}

/**
 * Representa una zona de geovalla con diferentes niveles
 */
sealed class GeofenceZone(
    val id: String,
    val name: String,
    val center: GeoPoint,
    val radiusMeters: Float,
    val allowsAttendance: Boolean
) {
    /**
     * Facultad de Ingeniería Eléctrica y Electrónica - FIEE (20m)
     * Permite registro de asistencia
     * Centro: -0.209502, -78.489513
     */
    class EdificioSecundario(
        center: GeoPoint = GeoPoint.EDIFICIO_SECUNDARIO_CENTER,
        name: String = "Facultad de Ingeniería Eléctrica y Electrónica"
    ) : GeofenceZone(
        id = "fiee",
        name = name,
        center = center,
        radiusMeters = 20f,
        allowsAttendance = true
    )

    /**
     * Facultad de Ciencias (20m)
     * Permite registro de asistencia
     * Centro: -0.211108, -78.489904 (mismo que Campus)
     */
    class EdificioPrincipal(
        center: GeoPoint = GeoPoint.EPN_CENTER,
        name: String = "Facultad de Ciencias"
    ) : GeofenceZone(
        id = "facultad_ciencias",
        name = name,
        center = center,
        radiusMeters = 20f,
        allowsAttendance = true
    )

    /**
     * Campus - zona amplia (200m)
     * Solo monitoreo, no permite registro de asistencia
     * Centro: -0.211108, -78.489904
     */
    class Campus(
        center: GeoPoint = GeoPoint.EPN_CENTER,
        name: String = "Campus EPN"
    ) : GeofenceZone(
        id = "campus_epn",
        name = name,
        center = center,
        radiusMeters = 200f,
        allowsAttendance = false
    )
}

/**
 * Eventos de transición de geovallas
 */
sealed class GeofenceEvent {
    /**
     * Usuario entró en una geovalla
     */
    data class Enter(
        val zone: GeofenceZone,
        val timestamp: Long = System.currentTimeMillis()
    ) : GeofenceEvent()

    /**
     * Usuario salió de una geovalla
     */
    data class Exit(
        val zone: GeofenceZone,
        val timestamp: Long = System.currentTimeMillis()
    ) : GeofenceEvent()

    /**
     * Usuario permaneció en una geovalla (dwell)
     */
    data class Dwell(
        val zone: GeofenceZone,
        val timestamp: Long = System.currentTimeMillis(),
        val dwellTimeMs: Long = 0
    ) : GeofenceEvent()

    /**
     * Sin eventos recientes
     */
    data object None : GeofenceEvent()
}

/**
 * Estado actual de ubicación
 */
sealed class LocationState {
    /**
     * Ubicación disponible
     */
    data class Available(
        val geoPoint: GeoPoint,
        val geofenceStatus: GeofenceStatus
    ) : LocationState()

    /**
     * Buscando ubicación
     */
    data object Loading : LocationState()

    /**
     * Ubicación no disponible
     */
    data class Unavailable(
        val reason: String
    ) : LocationState()

    /**
     * Permisos no concedidos
     */
    data class PermissionRequired(
        val permissions: List<String>
    ) : LocationState()

    /**
     * Error al obtener ubicación
     */
    data class Error(
        val message: String
    ) : LocationState()
}

/**
 * Estado actual respecto a las geovallas
 */
data class GeofenceStatus(
    val isInsideEdificioSecundario: Boolean = false,
    val isInsideEdificioPrincipal: Boolean = false,
    val isInsideCampus: Boolean = false,
    val activeZone: GeofenceZone? = null,
    val lastEvent: GeofenceEvent = GeofenceEvent.None
) {
    /**
     * Determina si el usuario puede registrar asistencia
     * basado en su ubicación actual (dentro de cualquier edificio)
     */
    fun canRegisterAttendance(): Boolean {
        return isInsideEdificioSecundario || isInsideEdificioPrincipal
    }

    /**
     * Obtiene el nombre de la zona activa más específica
     */
    fun getActiveZoneName(): String {
        return when {
            isInsideEdificioSecundario && isInsideEdificioPrincipal -> "FIEE y Fac. Ciencias"
            isInsideEdificioSecundario -> "FIEE"
            isInsideEdificioPrincipal -> "Facultad de Ciencias"
            isInsideCampus -> "Campus EPN"
            else -> "Fuera del campus"
        }
    }

    /**
     * Verifica si está dentro de cualquier edificio
     */
    fun isInsideAnyEdificio(): Boolean {
        return isInsideEdificioSecundario || isInsideEdificioPrincipal
    }
}

/**
 * Punto de registro de asistencia con ubicación
 */
data class AttendancePoint(
    val userId: String,
    val geoPoint: GeoPoint,
    val zoneName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isValid: Boolean = true
)

/**
 * Historial de ubicación para persistencia
 */
data class LocationHistoryEntry(
    val userId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
    val isInsideEdificioSecundario: Boolean,
    val isInsideEdificioPrincipal: Boolean,
    val isInsideCampus: Boolean,
    val zoneName: String
)

/**
 * Evento de geovalla para persistencia en Firestore
 */
data class GeofenceEventEntry(
    val geofenceId: String,
    val zoneName: String,
    val timestamp: Long,
    val eventType: String, // "ENTER", "EXIT", "DWELL"
    val userId: String
)

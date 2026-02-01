package com.example.ama_practica09.flow

import com.example.ama_practica09.location.GeoPoint
import com.example.ama_practica09.location.GeofenceStatus
import com.example.ama_practica09.models.Ubicacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Publicador: LocationSource
 *
 * Source que publica la ubicación actual del usuario.
 *
 */
object LocationSource {

    /**
     * StateFlow privado mutable que mantiene la ubicación actual
     */
    private val _zoneFlow = MutableStateFlow<Ubicacion>(
        Ubicacion.DentroDelRango("Campus Principal")
    )

    /**
     * StateFlow público de solo lectura que expone la ubicación actual
     *
     * Este Flow emite un nuevo valor cada vez que:
     * - El usuario se mueve a una nueva ubicación
     * - Se detecta entrada/salida del rango permitido
     * - Se simula un cambio de ubicación para pruebas
     *
     * @return StateFlow<Ubicacion> que emite la ubicación actual
     */
    val zoneFlow: StateFlow<Ubicacion> = _zoneFlow.asStateFlow()

    /**
     * StateFlow privado que mantiene el GeoPoint actual del GPS
     */
    private val _gpsLocationFlow = MutableStateFlow<GeoPoint?>(null)

    /**
     * StateFlow público que expone la ubicación GPS actual
     */
    val gpsLocationFlow: StateFlow<GeoPoint?> = _gpsLocationFlow.asStateFlow()

    /**
     * StateFlow privado que mantiene el estado de geovallas
     */
    private val _geofenceStatusFlow = MutableStateFlow(GeofenceStatus())

    /**
     * StateFlow público que expone el estado de geovallas
     */
    val geofenceStatusFlow: StateFlow<GeofenceStatus> = _geofenceStatusFlow.asStateFlow()

    /**
     * Publica que el usuario está dentro del rango permitido
     *
     * @param descripcion Descripción de la ubicación (ej: "Campus Principal")
     */
    fun setDentroDelRango(descripcion: String = "Campus Principal") {
        _zoneFlow.value = Ubicacion.DentroDelRango(descripcion)
    }

    /**
     * Publica que el usuario está fuera del rango permitido
     *
     * @param descripcion Descripción de la ubicación (ej: "Fuera del área")
     */
    fun setFueraDelRango(descripcion: String = "Fuera del área permitida") {
        _zoneFlow.value = Ubicacion.FueraDelRango(descripcion)
    }

    /**
     * Publica una ubicación directamente
     *
     * @param ubicacion Nueva ubicación a publicar
     */
    fun setUbicacion(ubicacion: Ubicacion) {
        _zoneFlow.value = ubicacion
    }

    /**
     * Obtiene la ubicación actual de forma síncrona
     *
     * @return Ubicacion actual
     */
    fun getCurrentZone(): Ubicacion {
        return _zoneFlow.value
    }

    /**
     * Verifica si el usuario está dentro del rango permitido
     *
     * @return true si está dentro del rango, false en caso contrario
     */
    fun isDentroDelRango(): Boolean {
        return _zoneFlow.value.estaDentroDelRango()
    }

    /**
     * Actualiza la ubicación desde datos GPS reales
     *
     * Integra con el sistema de geolocalización real (LocationManager)
     * y actualiza tanto el GeoPoint como el estado de Ubicacion
     *
     * @param geoPoint Coordenadas GPS actuales
     * @param geofenceStatus Estado actual respecto a las geovallas
     */
    fun updateFromGPS(geoPoint: GeoPoint, geofenceStatus: GeofenceStatus) {
        // Actualizar GeoPoint
        _gpsLocationFlow.value = geoPoint

        // Actualizar estado de geovallas
        _geofenceStatusFlow.value = geofenceStatus

        // Actualizar Ubicacion según el estado de geovallas
        val zoneName = geofenceStatus.getActiveZoneName()

        if (geofenceStatus.canRegisterAttendance()) {
            // Dentro del rango permitido (Edificio Principal o Secundario)
            _zoneFlow.value = Ubicacion.DentroDelRango(zoneName)
        } else if (geofenceStatus.isInsideCampus) {
            // En el campus pero fuera del área de registro
            _zoneFlow.value = Ubicacion.DentroDelRango("$zoneName (solo monitoreo)")
        } else {
            // Completamente fuera del campus
            _zoneFlow.value = Ubicacion.FueraDelRango(zoneName)
        }
    }

    /**
     * Obtiene el último GeoPoint conocido
     *
     * @return GeoPoint actual o null si no hay ubicación
     */
    fun getCurrentGeoPoint(): GeoPoint? {
        return _gpsLocationFlow.value
    }

    /**
     * Obtiene el estado actual de geovallas
     *
     * @return GeofenceStatus actual
     */
    fun getCurrentGeofenceStatus(): GeofenceStatus {
        return _geofenceStatusFlow.value
    }

    /**
     * Verifica si el usuario puede registrar asistencia
     * basado en su ubicación GPS actual
     *
     * @return true si está en zona de registro (Edificio Principal o Secundario)
     */
    fun canRegisterAttendance(): Boolean {
        return _geofenceStatusFlow.value.canRegisterAttendance()
    }
}
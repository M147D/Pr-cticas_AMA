package com.example.ama_practica09.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Gestor de ubicación que utiliza FusedLocationProviderClient
 * para obtener actualizaciones de ubicación GPS
 */
class LocationManager(private val context: Context) {

    companion object {
        private const val TAG = "LocationManager"

        // Intervalos de actualización
        const val UPDATE_INTERVAL_MS = 10000L // 10 segundos
        const val FASTEST_UPDATE_INTERVAL_MS = 5000L // 5 segundos
        const val MIN_DISPLACEMENT_METERS = 5f // 5 metros
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Loading)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _currentLocation = MutableStateFlow<GeoPoint?>(null)
    val currentLocation: StateFlow<GeoPoint?> = _currentLocation.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var isReceivingUpdates = false

    // Geovallas predefinidas
    private val geofenceZones = listOf(
        GeofenceZone.EdificioSecundario(),
        GeofenceZone.EdificioPrincipal(),
        GeofenceZone.Campus()
    )

    /**
     * Verifica si los permisos de ubicación están concedidos
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Obtiene la última ubicación conocida
     */
    @SuppressLint("MissingPermission")
    fun getLastLocation(onResult: (GeoPoint?) -> Unit) {
        if (!hasLocationPermission()) {
            _locationState.value = LocationState.PermissionRequired(
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            onResult(null)
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val geoPoint = location.toGeoPoint()
                    _currentLocation.value = geoPoint
                    updateLocationState(geoPoint)
                    onResult(geoPoint)
                } else {
                    Log.w(TAG, "Última ubicación no disponible")
                    onResult(null)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener última ubicación", e)
                _locationState.value = LocationState.Error(e.message ?: "Error desconocido")
                onResult(null)
            }
    }

    /**
     * Inicia actualizaciones de ubicación continuas
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (!hasLocationPermission()) {
            _locationState.value = LocationState.PermissionRequired(
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return
        }

        if (isReceivingUpdates) {
            Log.d(TAG, "Ya se están recibiendo actualizaciones")
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MS
        ).apply {
            setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_MS)
            setMinUpdateDistanceMeters(MIN_DISPLACEMENT_METERS)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val geoPoint = location.toGeoPoint()
                    _currentLocation.value = geoPoint
                    updateLocationState(geoPoint)
                    Log.d(TAG, "Ubicación actualizada: ${geoPoint.latitude}, ${geoPoint.longitude}")
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback!!,
                Looper.getMainLooper()
            )
            isReceivingUpdates = true
            _locationState.value = LocationState.Loading
            Log.d(TAG, "Actualizaciones de ubicación iniciadas")
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar actualizaciones de ubicación", e)
            _locationState.value = LocationState.Error(e.message ?: "Error desconocido")
        }
    }

    /**
     * Detiene las actualizaciones de ubicación
     */
    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
            isReceivingUpdates = false
            Log.d(TAG, "Actualizaciones de ubicación detenidas")
        }
    }

    /**
     * Obtiene un Flow de actualizaciones de ubicación
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdatesFlow(): Flow<GeoPoint> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Permisos de ubicación no concedidos"))
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL_MS
        ).apply {
            setMinUpdateIntervalMillis(FASTEST_UPDATE_INTERVAL_MS)
            setMinUpdateDistanceMeters(MIN_DISPLACEMENT_METERS)
        }.build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location.toGeoPoint())
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            callback,
            Looper.getMainLooper()
        )

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }

    /**
     * Actualiza el estado de ubicación con información de geovallas
     */
    private fun updateLocationState(geoPoint: GeoPoint) {
        val geofenceStatus = calculateGeofenceStatus(geoPoint)
        _locationState.value = LocationState.Available(
            geoPoint = geoPoint,
            geofenceStatus = geofenceStatus
        )
    }

    /**
     * Calcula el estado respecto a las geovallas
     * Considera dos edificios con centros diferentes
     */
    fun calculateGeofenceStatus(geoPoint: GeoPoint): GeofenceStatus {
        // Distancia al centro principal (Campus y Edificio Principal)
        val distanceToCampusCenter = calculateDistance(
            geoPoint.latitude,
            geoPoint.longitude,
            GeoPoint.EPN_CENTER.latitude,
            GeoPoint.EPN_CENTER.longitude
        )

        // Distancia al Edificio Secundario
        val distanceToEdificioSecundario = calculateDistance(
            geoPoint.latitude,
            geoPoint.longitude,
            GeoPoint.EDIFICIO_SECUNDARIO_CENTER.latitude,
            GeoPoint.EDIFICIO_SECUNDARIO_CENTER.longitude
        )

        // Verificar si está dentro de cada zona (20m para edificios, 200m para campus)
        val isInsideEdificioSecundario = distanceToEdificioSecundario <= 20f
        val isInsideEdificioPrincipal = distanceToCampusCenter <= 20f
        val isInsideCampus = distanceToCampusCenter <= 200f

        // Determinar la zona activa (prioridad: edificio más cercano)
        val activeZone = when {
            isInsideEdificioSecundario && isInsideEdificioPrincipal -> {
                // Si está en ambos, usar el más cercano
                if (distanceToEdificioSecundario <= distanceToCampusCenter) {
                    geofenceZones.first { it is GeofenceZone.EdificioSecundario }
                } else {
                    geofenceZones.first { it is GeofenceZone.EdificioPrincipal }
                }
            }
            isInsideEdificioSecundario -> geofenceZones.first { it is GeofenceZone.EdificioSecundario }
            isInsideEdificioPrincipal -> geofenceZones.first { it is GeofenceZone.EdificioPrincipal }
            isInsideCampus -> geofenceZones.first { it is GeofenceZone.Campus }
            else -> null
        }

        return GeofenceStatus(
            isInsideEdificioSecundario = isInsideEdificioSecundario,
            isInsideEdificioPrincipal = isInsideEdificioPrincipal,
            isInsideCampus = isInsideCampus,
            activeZone = activeZone
        )
    }

    /**
     * Calcula la distancia entre dos puntos usando la fórmula Haversine
     */
    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    /**
     * Obtiene las zonas de geovallas predefinidas
     */
    fun getGeofenceZones(): List<GeofenceZone> = geofenceZones

    /**
     * Extensión para convertir Location a GeoPoint
     */
    private fun Location.toGeoPoint(): GeoPoint = GeoPoint(
        latitude = latitude,
        longitude = longitude,
        accuracy = accuracy,
        timestamp = time
    )
}

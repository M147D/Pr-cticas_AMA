package com.example.ama_practica09.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor de geovallas (Geofencing)
 *
 * Configura y monitorea geovallas para las zonas:
 * - FIEE (20m) - Centro: -0.209502, -78.489513
 * - Facultad de Ciencias (20m) - Centro: -0.211108, -78.489904
 * - Campus EPN (200m) - Centro: -0.211108, -78.489904
 */
class GeofenceManager(private val context: Context) {

    companion object {
        private const val TAG = "GeofenceManager"

        // Acción para el broadcast de geovallas
        const val ACTION_GEOFENCE_EVENT = "com.example.ama_practica09.ACTION_GEOFENCE_EVENT"

        // Tiempo de permanencia para eventos DWELL (5 minutos)
        const val LOITERING_DELAY_MS = 5 * 60 * 1000 // 5 minutos

        // Request code para PendingIntent
        private const val GEOFENCE_PENDING_INTENT_REQUEST_CODE = 1001
    }

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    private val _geofenceEvents = MutableSharedFlow<GeofenceEvent>(replay = 1)
    val geofenceEvents: SharedFlow<GeofenceEvent> = _geofenceEvents.asSharedFlow()

    private val _geofenceStatus = MutableStateFlow(GeofenceStatus())
    val geofenceStatus: StateFlow<GeofenceStatus> = _geofenceStatus.asStateFlow()

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    // Geovallas predefinidas
    private val predefinedZones = listOf(
        GeofenceZone.EdificioSecundario(),
        GeofenceZone.EdificioPrincipal(),
        GeofenceZone.Campus()
    )

    /**
     * PendingIntent para recibir eventos de geovallas
     */
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE_EVENT
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        PendingIntent.getBroadcast(context, GEOFENCE_PENDING_INTENT_REQUEST_CODE, intent, flags)
    }

    /**
     * Verifica si los permisos necesarios están concedidos
     */
    fun hasRequiredPermissions(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // Para Android 10+ se necesita ACCESS_BACKGROUND_LOCATION para geofencing
        val backgroundLocation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return fineLocation && backgroundLocation
    }

    /**
     * Registra todas las geovallas predefinidas
     */
    @SuppressLint("MissingPermission")
    fun registerAllGeofences(
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (!hasRequiredPermissions()) {
            onError(SecurityException("Permisos de ubicación no concedidos"))
            return
        }

        val geofences = predefinedZones.map { zone ->
            createGeofence(zone)
        }

        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofences)
        }.build()

        try {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                .addOnSuccessListener {
                    _isMonitoring.value = true
                    Log.d(TAG, "Geovallas registradas exitosamente")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al registrar geovallas", e)
                    onError(e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear request de geovallas", e)
            onError(e)
        }
    }

    /**
     * Crea una geovalla a partir de una zona
     */
    private fun createGeofence(zone: GeofenceZone): Geofence {
        return Geofence.Builder()
            .setRequestId(zone.id)
            .setCircularRegion(
                zone.center.latitude,
                zone.center.longitude,
                zone.radiusMeters
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                Geofence.GEOFENCE_TRANSITION_EXIT or
                Geofence.GEOFENCE_TRANSITION_DWELL
            )
            .setLoiteringDelay(LOITERING_DELAY_MS)
            .build()
    }

    /**
     * Elimina todas las geovallas registradas
     */
    fun removeAllGeofences(
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        geofencingClient.removeGeofences(geofencePendingIntent)
            .addOnSuccessListener {
                _isMonitoring.value = false
                Log.d(TAG, "Geovallas eliminadas exitosamente")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al eliminar geovallas", e)
                onError(e)
            }
    }

    /**
     * Registra una geovalla personalizada
     */
    @SuppressLint("MissingPermission")
    fun registerCustomGeofence(
        zone: GeofenceZone,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        if (!hasRequiredPermissions()) {
            onError(SecurityException("Permisos de ubicación no concedidos"))
            return
        }

        val geofence = createGeofence(zone)

        val geofencingRequest = GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofence(geofence)
        }.build()

        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "Geovalla personalizada registrada: ${zone.name}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al registrar geovalla personalizada", e)
                onError(e)
            }
    }

    /**
     * Procesa un evento de transición de geovalla
     * Llamado desde GeofenceBroadcastReceiver
     */
    fun handleGeofenceTransition(
        geofenceId: String,
        transitionType: Int
    ) {
        val zone = predefinedZones.find { it.id == geofenceId } ?: return

        val event = when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                Log.d(TAG, "Entrada detectada: ${zone.name}")
                GeofenceEvent.Enter(zone)
            }
            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                Log.d(TAG, "Salida detectada: ${zone.name}")
                GeofenceEvent.Exit(zone)
            }
            Geofence.GEOFENCE_TRANSITION_DWELL -> {
                Log.d(TAG, "Permanencia detectada: ${zone.name}")
                GeofenceEvent.Dwell(zone)
            }
            else -> {
                Log.w(TAG, "Tipo de transición desconocido: $transitionType")
                return
            }
        }

        // Emitir evento
        _geofenceEvents.tryEmit(event)

        // Actualizar estado
        updateGeofenceStatus(event)
    }

    /**
     * Actualiza el estado de geovallas basado en el evento
     */
    private fun updateGeofenceStatus(event: GeofenceEvent) {
        val currentStatus = _geofenceStatus.value

        val newStatus = when (event) {
            is GeofenceEvent.Enter -> {
                when (event.zone) {
                    is GeofenceZone.EdificioSecundario -> currentStatus.copy(
                        isInsideEdificioSecundario = true,
                        isInsideCampus = true,
                        activeZone = event.zone,
                        lastEvent = event
                    )
                    is GeofenceZone.EdificioPrincipal -> currentStatus.copy(
                        isInsideEdificioPrincipal = true,
                        isInsideCampus = true,
                        activeZone = if (currentStatus.isInsideEdificioSecundario) currentStatus.activeZone else event.zone,
                        lastEvent = event
                    )
                    is GeofenceZone.Campus -> currentStatus.copy(
                        isInsideCampus = true,
                        activeZone = if (currentStatus.isInsideAnyEdificio()) currentStatus.activeZone else event.zone,
                        lastEvent = event
                    )
                }
            }
            is GeofenceEvent.Exit -> {
                when (event.zone) {
                    is GeofenceZone.EdificioSecundario -> currentStatus.copy(
                        isInsideEdificioSecundario = false,
                        activeZone = when {
                            currentStatus.isInsideEdificioPrincipal -> GeofenceZone.EdificioPrincipal()
                            currentStatus.isInsideCampus -> GeofenceZone.Campus()
                            else -> null
                        },
                        lastEvent = event
                    )
                    is GeofenceZone.EdificioPrincipal -> currentStatus.copy(
                        isInsideEdificioPrincipal = false,
                        activeZone = when {
                            currentStatus.isInsideEdificioSecundario -> GeofenceZone.EdificioSecundario()
                            currentStatus.isInsideCampus -> GeofenceZone.Campus()
                            else -> null
                        },
                        lastEvent = event
                    )
                    is GeofenceZone.Campus -> currentStatus.copy(
                        isInsideCampus = false,
                        isInsideEdificioPrincipal = false,
                        isInsideEdificioSecundario = false,
                        activeZone = null,
                        lastEvent = event
                    )
                }
            }
            is GeofenceEvent.Dwell -> {
                currentStatus.copy(lastEvent = event)
            }
            is GeofenceEvent.None -> currentStatus
        }

        _geofenceStatus.value = newStatus
    }

    /**
     * Obtiene las zonas predefinidas
     */
    fun getPredefinedZones(): List<GeofenceZone> = predefinedZones

    /**
     * Actualiza el estado de geovallas manualmente (para pruebas o simulación)
     */
    fun updateStatusManually(status: GeofenceStatus) {
        _geofenceStatus.value = status
    }
}

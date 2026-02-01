package com.example.ama_practica09.location

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.ama_practica09.firebase.NotificationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor de notificaciones para geovallas
 *
 * Maneja la lógica de notificaciones:
 * - Campus: Notifica al ingresar (validación cada 10 minutos)
 * - Edificios: Notifica solo cuando se REGISTRA asistencia (no al entrar)
 */
class GeofenceNotificationManager(private val context: Context) {

    companion object {
        private const val TAG = "GeofenceNotifManager"
        private const val PREFS_NAME = "geofence_notifications"

        // Keys para SharedPreferences
        private const val KEY_LAST_CAMPUS_NOTIFICATION = "last_campus_notification"
        private const val KEY_LAST_CAMPUS_ENTRY_TIME = "last_campus_entry_time"
        private const val KEY_IS_INSIDE_CAMPUS = "is_inside_campus"

        // Intervalo de validación para Campus (10 minutos en milisegundos)
        const val CAMPUS_CHECK_INTERVAL_MS = 10 * 60 * 1000L // 10 minutos

        // IDs de notificación específicos para geovallas
        const val NOTIFICATION_ID_CAMPUS_ENTER = 2001
        const val NOTIFICATION_ID_ATTENDANCE_REGISTERED = 2002
        const val NOTIFICATION_ID_CAMPUS_EXIT = 2003
    }

    private val notificationService = NotificationService(context)
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Estado de si ya se notificó el ingreso al campus en esta sesión
    private val _campusNotified = MutableStateFlow(false)
    val campusNotified: StateFlow<Boolean> = _campusNotified.asStateFlow()

    // Estado del último edificio donde se registró asistencia
    private val _lastAttendanceBuilding = MutableStateFlow<String?>(null)
    val lastAttendanceBuilding: StateFlow<String?> = _lastAttendanceBuilding.asStateFlow()

    /**
     * Verifica si han pasado 10 minutos desde la última notificación de Campus
     */
    private fun shouldNotifyCampusEntry(): Boolean {
        val lastNotification = prefs.getLong(KEY_LAST_CAMPUS_NOTIFICATION, 0L)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastNotification = currentTime - lastNotification

        return timeSinceLastNotification >= CAMPUS_CHECK_INTERVAL_MS
    }

    /**
     * Maneja la entrada al Campus
     * Solo notifica si han pasado 10 minutos desde la última notificación
     */
    fun onCampusEnter() {
        if (shouldNotifyCampusEntry()) {
            Log.d(TAG, "Entrada al Campus detectada - Enviando notificación")

            notificationService.showNotification(
                title = "Bienvenido al Campus EPN",
                message = "Has ingresado al área del campus universitario",
                channelId = NotificationService.CHANNEL_ID_DEFAULT,
                notificationId = NOTIFICATION_ID_CAMPUS_ENTER
            )

            // Guardar timestamp de la notificación
            prefs.edit()
                .putLong(KEY_LAST_CAMPUS_NOTIFICATION, System.currentTimeMillis())
                .putBoolean(KEY_IS_INSIDE_CAMPUS, true)
                .apply()

            _campusNotified.value = true
        } else {
            Log.d(TAG, "Entrada al Campus detectada - Notificación omitida (menos de 10 minutos)")
        }
    }

    /**
     * Maneja la salida del Campus
     */
    fun onCampusExit() {
        Log.d(TAG, "Salida del Campus detectada")

        prefs.edit()
            .putBoolean(KEY_IS_INSIDE_CAMPUS, false)
            .apply()

        _campusNotified.value = false
    }

    /**
     * Notifica cuando se REGISTRA asistencia en un edificio
     * Esta función debe ser llamada SOLO cuando el usuario registra asistencia,
     * NO cuando simplemente entra a la geovalla del edificio
     *
     * @param buildingName Nombre del edificio (ej: "FIEE", "Facultad de Ciencias")
     * @param userName Nombre del usuario que registra
     * @param isEntry true si es entrada, false si es salida
     */
    fun onAttendanceRegistered(
        buildingName: String,
        userName: String,
        isEntry: Boolean
    ) {
        val action = if (isEntry) "Entrada registrada" else "Salida registrada"
        val title = "Registro de Asistencia"
        val message = "$action en $buildingName\nUsuario: $userName"

        Log.d(TAG, "Asistencia registrada en $buildingName - $action")

        notificationService.showNotification(
            title = title,
            message = message,
            channelId = NotificationService.CHANNEL_ID_ASISTENCIA,
            notificationId = NOTIFICATION_ID_ATTENDANCE_REGISTERED
        )

        _lastAttendanceBuilding.value = buildingName
    }

    /**
     * Procesa eventos de geovalla y envía notificaciones según la lógica definida
     *
     * IMPORTANTE: Esta función SOLO maneja notificaciones de CAMPUS
     * Las notificaciones de edificios se manejan en onAttendanceRegistered
     */
    fun processGeofenceEvent(event: GeofenceEvent) {
        when (event) {
            is GeofenceEvent.Enter -> {
                when (event.zone) {
                    is GeofenceZone.Campus -> onCampusEnter()
                    // Para edificios NO notificamos al entrar, solo al registrar asistencia
                    is GeofenceZone.EdificioPrincipal,
                    is GeofenceZone.EdificioSecundario,
                    is GeofenceZone.EdificioMecanica -> {
                        Log.d(TAG, "Entrada a ${event.zone.name} - Sin notificación (esperar registro)")
                    }
                }
            }
            is GeofenceEvent.Exit -> {
                when (event.zone) {
                    is GeofenceZone.Campus -> onCampusExit()
                    is GeofenceZone.EdificioPrincipal,
                    is GeofenceZone.EdificioSecundario,
                    is GeofenceZone.EdificioMecanica -> {
                        Log.d(TAG, "Salida de ${event.zone.name} - Sin notificación automática")
                    }
                }
            }
            is GeofenceEvent.Dwell -> {
                Log.d(TAG, "Permanencia en ${event.zone.name}")
            }
            is GeofenceEvent.None -> { /* No action */ }
        }
    }

    /**
     * Verifica periódicamente si el usuario sigue en el Campus
     * Debe ser llamado cada 10 minutos para mantener el estado actualizado
     *
     * @param isCurrentlyInsideCampus Estado actual de ubicación
     */
    fun periodicCampusCheck(isCurrentlyInsideCampus: Boolean) {
        val wasInsideCampus = prefs.getBoolean(KEY_IS_INSIDE_CAMPUS, false)

        if (isCurrentlyInsideCampus && !wasInsideCampus) {
            // Acaba de entrar al campus
            onCampusEnter()
        } else if (!isCurrentlyInsideCampus && wasInsideCampus) {
            // Acaba de salir del campus
            onCampusExit()
        } else if (isCurrentlyInsideCampus && shouldNotifyCampusEntry()) {
            // Sigue en el campus y han pasado 10 minutos - recordatorio
            Log.d(TAG, "Verificación periódica: Usuario aún en Campus")
            // Opcional: enviar recordatorio
            // onCampusEnter() // Descomentar si quieres recordatorios cada 10 min
        }
    }

    /**
     * Resetea el estado de notificaciones (útil para testing o logout)
     */
    fun resetState() {
        prefs.edit().clear().apply()
        _campusNotified.value = false
        _lastAttendanceBuilding.value = null
    }

    /**
     * Obtiene el tiempo restante hasta la próxima notificación permitida de Campus
     * @return Tiempo en milisegundos, o 0 si ya puede notificar
     */
    fun getTimeUntilNextCampusNotification(): Long {
        val lastNotification = prefs.getLong(KEY_LAST_CAMPUS_NOTIFICATION, 0L)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastNotification = currentTime - lastNotification
        val timeRemaining = CAMPUS_CHECK_INTERVAL_MS - timeSinceLastNotification

        return if (timeRemaining > 0) timeRemaining else 0L
    }
}

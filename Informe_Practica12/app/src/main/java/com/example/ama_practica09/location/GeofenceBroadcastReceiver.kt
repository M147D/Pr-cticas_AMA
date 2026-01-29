package com.example.ama_practica09.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

/**
 * BroadcastReceiver para manejar eventos de transición de geovallas
 *
 * Recibe notificaciones cuando el usuario:
 * - Entra en una geovalla (ENTER)
 * - Sale de una geovalla (EXIT)
 * - Permanece en una geovalla (DWELL)
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceBroadcastRcvr"

        // Instancia singleton del GeofenceManager para comunicar eventos
        @Volatile
        private var geofenceManagerInstance: GeofenceManager? = null

        /**
         * Registra la instancia del GeofenceManager para recibir eventos
         */
        fun setGeofenceManager(manager: GeofenceManager) {
            geofenceManagerInstance = manager
        }

        /**
         * Limpia la instancia del GeofenceManager
         */
        fun clearGeofenceManager() {
            geofenceManagerInstance = null
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Evento de geovalla recibido")

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent es null")
            return
        }

        if (geofencingEvent.hasError()) {
            val errorMessage = getErrorString(geofencingEvent.errorCode)
            Log.e(TAG, "Error en geofencing: $errorMessage")
            return
        }

        // Obtener tipo de transición
        val transitionType = geofencingEvent.geofenceTransition

        // Obtener geovallas que dispararon el evento
        val triggeringGeofences = geofencingEvent.triggeringGeofences

        if (triggeringGeofences.isNullOrEmpty()) {
            Log.w(TAG, "No hay geovallas en el evento")
            return
        }

        // Procesar cada geovalla que disparó el evento
        for (geofence in triggeringGeofences) {
            val geofenceId = geofence.requestId
            val transitionName = getTransitionString(transitionType)

            Log.d(TAG, "Transición: $transitionName en geovalla: $geofenceId")

            // Notificar al GeofenceManager
            geofenceManagerInstance?.handleGeofenceTransition(geofenceId, transitionType)

            // Opcionalmente, guardar en repositorio o mostrar notificación
            handleTransitionLocally(context, geofenceId, transitionType)
        }
    }

    /**
     * Maneja la transición localmente (persistencia, notificaciones, etc.)
     */
    private fun handleTransitionLocally(
        context: Context,
        geofenceId: String,
        transitionType: Int
    ) {
        // Aquí se puede agregar lógica adicional como:
        // - Guardar evento en base de datos local
        // - Mostrar notificación al usuario
        // - Enviar evento a Firestore

        val transitionName = getTransitionString(transitionType)
        Log.d(TAG, "Procesando transición local: $transitionName para $geofenceId")
    }

    /**
     * Convierte el código de transición a string legible
     */
    private fun getTransitionString(transitionType: Int): String {
        return when (transitionType) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> "ENTER"
            Geofence.GEOFENCE_TRANSITION_EXIT -> "EXIT"
            Geofence.GEOFENCE_TRANSITION_DWELL -> "DWELL"
            else -> "UNKNOWN"
        }
    }

    /**
     * Convierte el código de error a mensaje legible
     */
    private fun getErrorString(errorCode: Int): String {
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE ->
                "Geofencing no está disponible. Verifica que la ubicación esté activada."
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES ->
                "Se excedió el límite de geovallas registradas."
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS ->
                "Se excedió el límite de PendingIntents para geofencing."
            GeofenceStatusCodes.GEOFENCE_INSUFFICIENT_LOCATION_PERMISSION ->
                "Permisos de ubicación insuficientes para geofencing."
            else ->
                "Error desconocido de geofencing: $errorCode"
        }
    }
}

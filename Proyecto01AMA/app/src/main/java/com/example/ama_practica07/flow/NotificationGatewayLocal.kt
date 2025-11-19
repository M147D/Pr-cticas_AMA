package com.example.ama_practica07.flow

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.ama_practica07.MainActivity
import com.example.ama_practica07.R

/**
 * NotificationGatewayLocal
 *
 * Gateway que recibe eventos de la aplicación (AppEvent) y los convierte
 * en notificaciones locales de Android.
 *
 * Implementa el patrón Suscriptor del patrón publicador-suscriptor:
 * - Se SUSCRIBE a: SharedFlow<AppEvent> del ViewModel
 * - PUBLICA: Notificaciones locales de Android
 *
 * Responsabilidades:
 * - Crear y gestionar canales de notificación
 * - Transformar eventos en notificaciones nativas
 * - Aplicar estilos según el tipo de evento
 */
class NotificationGatewayLocal(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        // IDs de canales de notificación
        const val CHANNEL_ASISTENCIA = "asistencia_channel"
        const val CHANNEL_ALERTAS = "alertas_channel"

        // IDs de notificaciones (para poder actualizarlas/cancelarlas)
        private const val NOTIFICATION_ID_REGISTRO = 1001
        private const val NOTIFICATION_ID_ALERTA = 1002
        private const val NOTIFICATION_ID_INFO = 1003
    }

    init {
        crearCanalesDeNotificacion()
    }

    /**
     * Crea los canales de notificación requeridos
     *
     * Canales:
     * - "Asistencia" (IMPORTANCE_DEFAULT): para registros válidos
     * - "Alertas" (IMPORTANCE_HIGH): para fuera de horario o zona
     */
    private fun crearCanalesDeNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal: Asistencia
            val canalAsistencia = NotificationChannel(
                CHANNEL_ASISTENCIA,
                "Asistencia",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de registros de asistencia válidos"
                enableVibration(true)
                enableLights(true)
            }

            // Canal: Alertas
            val canalAlertas = NotificationChannel(
                CHANNEL_ALERTAS,
                "Alertas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas importantes: fuera de horario, zona no válida, etc."
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }

            // Registrar canales
            notificationManager.createNotificationChannel(canalAsistencia)
            notificationManager.createNotificationChannel(canalAlertas)
        }
    }

    /**
     * Procesa un evento y muestra la notificación correspondiente
     *
     * Este método actúa como suscriptor de AppEvent
     *
     * @param event Evento de la aplicación a procesar
     */
    fun procesarEvento(event: AppEvent) {
        when (event) {
            is AppEvent.Notify -> mostrarNotificacion(event)
            is AppEvent.RegistroExitoso -> mostrarRegistroExitoso(event)
            is AppEvent.RegistroRechazado -> mostrarRegistroRechazado(event)
            is AppEvent.CanRegisterChanged -> mostrarCambioDeEstado(event)
            else -> {
                // ShowToast y ShowSnackbar se manejan en la UI directamente
            }
        }
    }

    /**
     * Muestra una notificación genérica
     */
    private fun mostrarNotificacion(event: AppEvent.Notify) {
        val channelId = when (event.type) {
            NotificationType.SUCCESS -> CHANNEL_ASISTENCIA
            NotificationType.ERROR, NotificationType.WARNING -> CHANNEL_ALERTAS
            NotificationType.INFO -> CHANNEL_ASISTENCIA
        }

        val icon = when (event.type) {
            NotificationType.SUCCESS -> android.R.drawable.ic_dialog_info
            NotificationType.ERROR -> android.R.drawable.ic_dialog_alert
            NotificationType.WARNING -> android.R.drawable.stat_notify_error
            NotificationType.INFO -> android.R.drawable.ic_dialog_info
        }

        val priority = when (event.type) {
            NotificationType.ERROR, NotificationType.WARNING -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(event.title)
            .setContentText(event.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(event.body))
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(crearPendingIntent())
            .build()

        notificationManager.notify(NOTIFICATION_ID_INFO, notification)
    }

    /**
     * Muestra notificación de registro exitoso
     */
    private fun mostrarRegistroExitoso(event: AppEvent.RegistroExitoso) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ASISTENCIA)
            .setSmallIcon(android.R.drawable.checkbox_on_background)
            .setContentTitle("✓ Registro de Asistencia")
            .setContentText(event.mensaje)
            .setStyle(NotificationCompat.BigTextStyle().bigText(event.mensaje))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(crearPendingIntent())
            .build()

        notificationManager.notify(NOTIFICATION_ID_REGISTRO, notification)
    }

    /**
     * Muestra notificación de registro rechazado
     */
    private fun mostrarRegistroRechazado(event: AppEvent.RegistroRechazado) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTAS)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("✗ Registro Rechazado")
            .setContentText(event.razon)
            .setStyle(NotificationCompat.BigTextStyle().bigText(event.razon))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(crearPendingIntent())
            .build()

        notificationManager.notify(NOTIFICATION_ID_ALERTA, notification)
    }

    /**
     * Muestra notificación cuando cambia el estado canRegister
     */
    private fun mostrarCambioDeEstado(event: AppEvent.CanRegisterChanged) {
        // Solo notificar cuando pasa a NO poder registrar
        if (!event.canRegister) {
            val notification = NotificationCompat.Builder(context, CHANNEL_ALERTAS)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle("Registro deshabilitado")
                .setContentText(event.mensaje)
                .setStyle(NotificationCompat.BigTextStyle().bigText(event.mensaje))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(crearPendingIntent())
                .build()

            notificationManager.notify(NOTIFICATION_ID_ALERTA, notification)
        }
    }

    /**
     * Crea un PendingIntent para abrir la aplicación al tocar la notificación
     */
    private fun crearPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Cancela todas las notificaciones
     */
    fun cancelarTodasLasNotificaciones() {
        notificationManager.cancelAll()
    }

    /**
     * Cancela una notificación específica
     */
    fun cancelarNotificacion(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}

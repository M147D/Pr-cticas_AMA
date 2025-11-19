package com.example.ama_practica07.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.ama_practica07.MainActivity
import com.example.ama_practica07.R

/**
 * NotificationService
 *
 * Servicio encargado de gestionar las notificaciones push de Firebase Cloud Messaging.
 */
class NotificationService(private val context: Context) {

    companion object {
        private const val TAG = "NotificationService"

        // IDs de canales de notificación
        const val CHANNEL_ID_DEFAULT = "default_channel"
        const val CHANNEL_ID_ASISTENCIA = "asistencia_channel"
        const val CHANNEL_ID_ADMIN = "admin_channel"

        // Nombres de canales
        const val CHANNEL_NAME_DEFAULT = "Notificaciones Generales"
        const val CHANNEL_NAME_ASISTENCIA = "Asistencia"
        const val CHANNEL_NAME_ADMIN = "Alertas"

        // IDs de notificación
        const val NOTIFICATION_ID_DEFAULT = 1001
        const val NOTIFICATION_ID_ASISTENCIA = 1002
        const val NOTIFICATION_ID_ADMIN = 1003
    }

    val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    /**
     * Crea los canales de notificación necesarios para Android 8.0 (API 26) y superior
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal por defecto
            val defaultChannel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
                CHANNEL_NAME_DEFAULT,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones generales de la aplicación"
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
                enableVibration(true)
                setShowBadge(true)
            }

            // Canal de asistencia
            val asistenciaChannel = NotificationChannel(
                CHANNEL_ID_ASISTENCIA,
                CHANNEL_NAME_ASISTENCIA,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de control de asistencia"
                enableLights(true)
                lightColor = android.graphics.Color.GREEN
                enableVibration(true)
                setShowBadge(true)
            }

            // Canal de alertas (prioridad URGENTE)
            val adminChannel = NotificationChannel(
                CHANNEL_ID_ADMIN,
                CHANNEL_NAME_ADMIN,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas importantes y urgentes"
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setShowBadge(true)
            }

            // Registrar canales
            notificationManager.createNotificationChannel(defaultChannel)
            notificationManager.createNotificationChannel(asistenciaChannel)
            notificationManager.createNotificationChannel(adminChannel)

            Log.d(TAG, "✓ Canales de notificación creados correctamente")
        }
    }

    /**
     * Muestra una notificación simple
     */
    fun showNotification(
        title: String,
        message: String,
        channelId: String = CHANNEL_ID_DEFAULT,
        notificationId: Int = NOTIFICATION_ID_DEFAULT
    ) {
        Log.d(TAG, "Mostrando notificación: $title - Canal: $channelId")

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_type", channelId)
            putExtra("notification_title", title)
            putExtra("notification_body", message)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Determinar prioridad según canal
        val priority = when (channelId) {
            CHANNEL_ID_ADMIN -> NotificationCompat.PRIORITY_HIGH
            CHANNEL_ID_ASISTENCIA -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Usar ic_notification
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "✓ Notificación mostrada con ID: $notificationId")
    }

    /**
     * Muestra una notificación de asistencia
     */
    fun showAsistenciaNotification(
        userName: String,
        action: String,
        isValid: Boolean
    ) {
        val title = if (isValid) "✓ Registro Exitoso" else "✗ Registro Rechazado"
        val message = "$userName - $action"

        showNotification(
            title = title,
            message = message,
            channelId = CHANNEL_ID_ASISTENCIA,
            notificationId = NOTIFICATION_ID_ASISTENCIA
        )
    }

    /**
     * Muestra una notificación administrativa/alerta
     */
    fun showAdminNotification(title: String, message: String) {
        showNotification(
            title = "⚠ $title",
            message = message,
            channelId = CHANNEL_ID_ADMIN,
            notificationId = NOTIFICATION_ID_ADMIN
        )
    }

    /**
     * Cancela una notificación específica
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    /**
     * Cancela todas las notificaciones
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
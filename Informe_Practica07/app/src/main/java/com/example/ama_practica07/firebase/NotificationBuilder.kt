package com.example.ama_practica07.firebase

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.ama_practica07.MainActivity
import com.example.ama_practica07.R

/**
 * NotificationBuilder
 *
 * Clase helper que facilita la construcción de notificaciones
 * personalizadas con diferentes estilos y configuraciones.
 *
 * Proporciona un API fluido para crear notificaciones con:
 * - Diferentes estilos (BigText, BigPicture, InboxStyle)
 * - Acciones personalizadas
 * - Prioridades configurables
 * - Sonidos y vibraciones personalizadas
 *
 * Uso:
 * ```
 * val notification = NotificationBuilder(context)
 *     .setTitle("Título")
 *     .setMessage("Mensaje")
 *     .setChannelId(NotificationService.CHANNEL_ID_DEFAULT)
 *     .build()
 * ```
 */
class NotificationBuilder(private val context: Context) {

    private var title: String = ""
    private var message: String = ""
    private var channelId: String = NotificationService.CHANNEL_ID_DEFAULT
    private var priority: Int = NotificationCompat.PRIORITY_DEFAULT
    private var autoCancel: Boolean = true
    private var contentIntent: PendingIntent? = null
    private var smallIcon: Int = R.drawable.ic_launcher_foreground
    private var largeIcon: android.graphics.Bitmap? = null
    private var style: NotificationCompat.Style? = null
    private var actions: MutableList<NotificationCompat.Action> = mutableListOf()
    private var vibrationPattern: LongArray? = null
    private var soundEnabled: Boolean = true
    private var lightsEnabled: Boolean = true
    private var color: Int? = null

    /**
     * Establece el título de la notificación
     *
     * @param title Título
     * @return Esta instancia para encadenar métodos
     */
    fun setTitle(title: String): NotificationBuilder {
        this.title = title
        return this
    }

    /**
     * Establece el mensaje de la notificación
     *
     * @param message Mensaje
     * @return Esta instancia para encadenar métodos
     */
    fun setMessage(message: String): NotificationBuilder {
        this.message = message
        return this
    }

    /**
     * Establece el ID del canal de notificación
     *
     * @param channelId ID del canal
     * @return Esta instancia para encadenar métodos
     */
    fun setChannelId(channelId: String): NotificationBuilder {
        this.channelId = channelId
        return this
    }

    /**
     * Establece la prioridad de la notificación
     *
     * @param priority Prioridad (PRIORITY_MIN, PRIORITY_LOW, PRIORITY_DEFAULT, PRIORITY_HIGH, PRIORITY_MAX)
     * @return Esta instancia para encadenar métodos
     */
    fun setPriority(priority: Int): NotificationBuilder {
        this.priority = priority
        return this
    }

    /**
     * Establece si la notificación se cancela automáticamente al tocarla
     *
     * @param autoCancel true para auto-cancelar
     * @return Esta instancia para encadenar métodos
     */
    fun setAutoCancel(autoCancel: Boolean): NotificationBuilder {
        this.autoCancel = autoCancel
        return this
    }

    /**
     * Establece el icono pequeño de la notificación
     *
     * @param iconResId ID del recurso del icono
     * @return Esta instancia para encadenar métodos
     */
    fun setSmallIcon(iconResId: Int): NotificationBuilder {
        this.smallIcon = iconResId
        return this
    }

    /**
     * Establece el icono grande de la notificación
     *
     * @param bitmap Bitmap del icono grande
     * @return Esta instancia para encadenar métodos
     */
    fun setLargeIcon(bitmap: android.graphics.Bitmap): NotificationBuilder {
        this.largeIcon = bitmap
        return this
    }

    /**
     * Establece el color de la notificación
     *
     * @param color Color en formato ARGB
     * @return Esta instancia para encadenar métodos
     */
    fun setColor(color: Int): NotificationBuilder {
        this.color = color
        return this
    }

    /**
     * Establece el intent que se ejecuta al tocar la notificación
     *
     * @param intent Intent a ejecutar
     * @return Esta instancia para encadenar métodos
     */
    fun setContentIntent(intent: Intent): NotificationBuilder {
        this.contentIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return this
    }

    /**
     * Configura un intent por defecto que abre la actividad principal
     *
     * @return Esta instancia para encadenar métodos
     */
    fun setDefaultIntent(): NotificationBuilder {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        return setContentIntent(intent)
    }

    /**
     * Establece el estilo BigText para mensajes largos
     *
     * @param bigText Texto largo a mostrar
     * @return Esta instancia para encadenar métodos
     */
    fun setBigTextStyle(bigText: String): NotificationBuilder {
        this.style = NotificationCompat.BigTextStyle()
            .bigText(bigText)
            .setBigContentTitle(title)
        return this
    }

    /**
     * Establece el estilo Inbox para mostrar múltiples líneas
     *
     * @param lines Lista de líneas a mostrar
     * @return Esta instancia para encadenar métodos
     */
    fun setInboxStyle(lines: List<String>): NotificationBuilder {
        val inboxStyle = NotificationCompat.InboxStyle()
        lines.forEach { line ->
            inboxStyle.addLine(line)
        }
        inboxStyle.setBigContentTitle(title)
        this.style = inboxStyle
        return this
    }

    /**
     * Agrega una acción a la notificación
     *
     * @param icon Icono de la acción
     * @param title Título de la acción
     * @param intent Intent que se ejecuta al tocar la acción
     * @return Esta instancia para encadenar métodos
     */
    fun addAction(icon: Int, title: String, intent: Intent): NotificationBuilder {
        val pendingIntent = PendingIntent.getActivity(
            context,
            actions.size,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val action = NotificationCompat.Action.Builder(icon, title, pendingIntent).build()
        actions.add(action)
        return this
    }

    /**
     * Establece el patrón de vibración
     *
     * @param pattern Patrón de vibración en milisegundos
     * @return Esta instancia para encadenar métodos
     */
    fun setVibrationPattern(pattern: LongArray): NotificationBuilder {
        this.vibrationPattern = pattern
        return this
    }

    /**
     * Habilita o deshabilita el sonido de notificación
     *
     * @param enabled true para habilitar sonido
     * @return Esta instancia para encadenar métodos
     */
    fun setSoundEnabled(enabled: Boolean): NotificationBuilder {
        this.soundEnabled = enabled
        return this
    }

    /**
     * Habilita o deshabilita las luces LED
     *
     * @param enabled true para habilitar luces
     * @return Esta instancia para encadenar métodos
     */
    fun setLightsEnabled(enabled: Boolean): NotificationBuilder {
        this.lightsEnabled = enabled
        return this
    }

    /**
     * Construye la notificación con la configuración establecida
     *
     * @return Notificación construida
     */
    fun build(): Notification {
        // Si no se estableció un intent, usar el por defecto
        if (contentIntent == null) {
            setDefaultIntent()
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setContentIntent(contentIntent)
            .setAutoCancel(autoCancel)

        // Agregar icono grande si existe
        largeIcon?.let { builder.setLargeIcon(it) }

        // Agregar color si existe
        color?.let { builder.setColor(it) }

        // Agregar estilo si existe
        style?.let { builder.setStyle(it) }

        // Agregar acciones
        actions.forEach { action ->
            builder.addAction(action)
        }

        // Configurar vibración
        vibrationPattern?.let { builder.setVibrate(it) }

        // Configurar sonido
        if (soundEnabled) {
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
        }

        // Configurar luces
        if (lightsEnabled) {
            builder.setLights(
                FirebaseConfig.NotificationConfig.NOTIFICATION_COLOR,
                1000,
                3000
            )
        }

        return builder.build()
    }

    /**
     * Construye y muestra la notificación directamente
     *
     * @param notificationId ID único de la notificación
     */
    fun buildAndShow(notificationId: Int) {
        val notificationService = NotificationService(context)
        val notification = build()
        notificationService.notificationManager.notify(notificationId, notification)
    }

    /**
     * Companion object con métodos de conveniencia
     */
    companion object {
        /**
         * Crea una notificación simple rápidamente
         *
         * @param context Contexto de la aplicación
         * @param title Título de la notificación
         * @param message Mensaje de la notificación
         * @param channelId ID del canal
         * @return Notificación construida
         */
        fun createSimple(
            context: Context,
            title: String,
            message: String,
            channelId: String = NotificationService.CHANNEL_ID_DEFAULT
        ): Notification {
            return NotificationBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setChannelId(channelId)
                .setDefaultIntent()
                .build()
        }

        /**
         * Crea una notificación de alta prioridad
         *
         * @param context Contexto de la aplicación
         * @param title Título de la notificación
         * @param message Mensaje de la notificación
         * @return Notificación construida
         */
        fun createHighPriority(
            context: Context,
            title: String,
            message: String
        ): Notification {
            return NotificationBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrationPattern(FirebaseConfig.NotificationConfig.VIBRATION_PATTERN)
                .setDefaultIntent()
                .build()
        }
    }
}

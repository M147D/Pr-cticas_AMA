package com.example.ama_practica05.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * MyFirebaseMessagingService
 *
 * Servicio que maneja todos los mensajes de Firebase Cloud Messaging
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
    }

    /**
     * Se llama cuando se recibe un nuevo token de FCM
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "================================================")
        Log.d(TAG, "✓ Token de FCM actualizado: $token")
        Log.d(TAG, "================================================")

        // Actualizar en FirebaseConfig
        FirebaseConfig.updateToken(token)

        // Actualizar en MessagingHandler
        MessagingHandler.updateToken(token)

        // Suscribirse a tópicos por defecto
        subscribeToDefaultTopics()
    }

    /**
     * Se llama cuando se recibe un mensaje de FCM
     *
     * IMPORTANTE:
     * - App en FOREGROUND: Este método se ejecuta SIEMPRE
     * - App en BACKGROUND con mensaje NOTIFICATION: Sistema lo maneja automáticamente
     * - App en BACKGROUND con mensaje DATA: Este método se ejecuta
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "================================================")
        Log.d(TAG, "✓ Mensaje recibido de: ${remoteMessage.from}")
        Log.d(TAG, "Tiene notificación: ${remoteMessage.notification != null}")
        Log.d(TAG, "Tiene datos: ${remoteMessage.data.isNotEmpty()}")
        Log.d(TAG, "================================================")

        // 1. Verificar si tiene componente de NOTIFICACIÓN
        remoteMessage.notification?.let { notification ->
            Log.d(TAG, "Procesando mensaje de NOTIFICACIÓN")
            Log.d(TAG, "Título: ${notification.title}")
            Log.d(TAG, "Cuerpo: ${notification.body}")

            // Cuando la app está en FOREGROUND, debemos mostrar la notificación manualmente
            handleNotificationMessage(
                title = notification.title ?: "Notificación",
                body = notification.body ?: "",
                data = remoteMessage.data
            )
        }

        // 2. Verificar si tiene datos personalizados
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Procesando mensaje de DATOS")
            Log.d(TAG, "Datos: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // 3. Si no tiene ni notificación ni datos
        if (remoteMessage.notification == null && remoteMessage.data.isEmpty()) {
            Log.w(TAG, "⚠ Mensaje recibido sin contenido")
        }
    }

    /**
     * Maneja mensajes de tipo NOTIFICATION
     * Este método se ejecuta cuando la app está en FOREGROUND
     */
    private fun handleNotificationMessage(
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        Log.d(TAG, "→ Manejando notificación: $title")

        // Determinar el tipo/canal basándose en el título o datos
        val type = determineNotificationType(title, body, data)

        // Mostrar la notificación usando NotificationService
        val notificationService = NotificationService(applicationContext)

        when (type) {
            "alerta", "admin" -> {
                notificationService.showAdminNotification(title, body)
                Log.d(TAG, "✓ Notificación de ALERTA mostrada")
            }
            "asistencia" -> {
                notificationService.showNotification(
                    title = title,
                    message = body,
                    channelId = NotificationService.CHANNEL_ID_ASISTENCIA,
                    notificationId = NotificationService.NOTIFICATION_ID_ASISTENCIA
                )
                Log.d(TAG, "✓ Notificación de ASISTENCIA mostrada")
            }
            else -> {
                notificationService.showNotification(title, body)
                Log.d(TAG, "✓ Notificación GENERAL mostrada")
            }
        }

        // Registrar en MessagingHandler
        MessagingHandler.handleMessage(title, body, data, applicationContext)
    }

    /**
     * Maneja mensajes de tipo DATA
     * Este método se ejecuta siempre que llegue un mensaje con data payload
     */
    private fun handleDataMessage(data: Map<String, String>) {
        Log.d(TAG, "→ Manejando mensaje de datos")

        val type = data["type"] ?: "general"
        val title = data["title"] ?: "Nueva notificación"
        val body = data["body"] ?: "Tienes un nuevo mensaje"

        Log.d(TAG, "Tipo: $type")
        Log.d(TAG, "Título: $title")
        Log.d(TAG, "Cuerpo: $body")

        val notificationService = NotificationService(applicationContext)

        when (type.lowercase()) {
            "asistencia" -> {
                val userName = data["userName"] ?: "Usuario"
                val action = data["action"] ?: "Acción"
                val isValid = data["isValid"]?.toBoolean() ?: true

                notificationService.showAsistenciaNotification(userName, action, isValid)
                Log.d(TAG, "✓ Notificación de asistencia mostrada")
            }
            "alerta", "alert", "admin" -> {
                notificationService.showAdminNotification(title, body)
                Log.d(TAG, "✓ Alerta mostrada")
            }
            else -> {
                notificationService.showNotification(title, body)
                Log.d(TAG, "✓ Notificación general mostrada")
            }
        }

        // Registrar en MessagingHandler para historial
        MessagingHandler.handleDataMessage(data, applicationContext)
    }

    /**
     * Determina el tipo de notificación basándose en el contenido
     */
    private fun determineNotificationType(
        title: String,
        body: String,
        data: Map<String, String>
    ): String {
        // 1. Verificar si hay un tipo explícito en los datos
        data["type"]?.let { return it.lowercase() }

        // 2. Determinar por palabras clave en el título
        val titleLower = title.lowercase()
        return when {
            titleLower.contains("alerta") -> "alerta"
            titleLower.contains("alert") -> "alerta"
            titleLower.contains("urgente") -> "alerta"
            titleLower.contains("importante") -> "alerta"
            titleLower.contains("asistencia") -> "asistencia"
            titleLower.contains("registro") -> "asistencia"
            titleLower.contains("entrada") -> "asistencia"
            titleLower.contains("salida") -> "asistencia"
            else -> "general"
        }
    }

    /**
     * Suscribe automáticamente a tópicos por defecto
     */
    private fun subscribeToDefaultTopics() {
        Log.d(TAG, "→ Suscribiéndose a tópicos por defecto")

        FirebaseConfig.subscribeToTopic(
            topic = FirebaseConfig.Topics.ALL_USERS,
            onSuccess = {
                Log.d(TAG, "✓ Suscrito exitosamente a ${FirebaseConfig.Topics.ALL_USERS}")
            },
            onError = { error ->
                Log.e(TAG, "✗ Error al suscribirse a tópico: ${error.message}")
            }
        )
    }

    /**
     * Se llama cuando se eliminan mensajes de la cola
     */
    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.w(TAG, "⚠ Algunos mensajes fueron eliminados del servidor")
    }

    /**
     * Se llama cuando un mensaje se envía exitosamente
     */
    override fun onMessageSent(msgId: String) {
        super.onMessageSent(msgId)
        Log.d(TAG, "✓ Mensaje enviado: $msgId")
    }

    /**
     * Se llama cuando falla el envío de un mensaje
     */
    override fun onSendError(msgId: String, exception: Exception) {
        super.onSendError(msgId, exception)
        Log.e(TAG, "✗ Error al enviar mensaje $msgId: ${exception.message}", exception)
    }
}
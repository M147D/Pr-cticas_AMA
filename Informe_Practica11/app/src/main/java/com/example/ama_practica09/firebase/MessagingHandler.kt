package com.example.ama_practica09.firebase

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MessagingHandler
 *
 * Clase encargada de manejar los mensajes recibidos desde Firebase Cloud Messaging.
 * Implementa el patrón publicador-suscriptor para notificar a los observadores
 * sobre nuevos mensajes recibidos.
 *
 * Responsabilidades:
 * - Procesar mensajes entrantes de FCM
 * - Clasificar mensajes por tipo
 * - Publicar mensajes a los suscriptores
 * - Mantener historial de mensajes
 * - Gestionar el token de registro de FCM
 *
 * Patrón: Singleton para garantizar una única instancia
 */
object MessagingHandler {

    private const val TAG = "MessagingHandler"

    /**
     * Tipos de mensajes soportados
     */
    enum class MessageType {
        ASISTENCIA,      // Mensajes de registro de asistencia
        ADMIN,           // Mensajes administrativos
        GENERAL,         // Mensajes generales
        ALERT,           // Alertas importantes
        UNKNOWN          // Tipo desconocido
    }

    /**
     * Data class que representa un mensaje de FCM
     */
    data class FCMMessage(
        val messageId: String,
        val type: MessageType,
        val title: String,
        val body: String,
        val data: Map<String, String>,
        val timestamp: Long = System.currentTimeMillis()
    )

    // StateFlow para el token de FCM
    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    // StateFlow para mensajes recibidos
    private val _messages = MutableStateFlow<List<FCMMessage>>(emptyList())
    val messages: StateFlow<List<FCMMessage>> = _messages.asStateFlow()

    // StateFlow para el último mensaje recibido
    private val _lastMessage = MutableStateFlow<FCMMessage?>(null)
    val lastMessage: StateFlow<FCMMessage?> = _lastMessage.asStateFlow()

    // Lista interna de mensajes
    private val messageHistory = mutableListOf<FCMMessage>()

    /**
     * Actualiza el token de FCM
     *
     * Este método debe ser llamado cuando se recibe un nuevo token de registro
     * desde Firebase Cloud Messaging.
     *
     * @param token Nuevo token de FCM
     */
    fun updateToken(token: String) {
        Log.d(TAG, "Token FCM actualizado: $token")
        _fcmToken.value = token
        // Aquí se podría enviar el token al servidor backend
    }

    /**
     * Procesa un mensaje recibido desde Firebase
     *
     * @param title Título del mensaje
     * @param body Cuerpo del mensaje
     * @param data Datos adicionales del mensaje
     * @param context Contexto de la aplicación
     */
    fun handleMessage(
        title: String,
        body: String,
        data: Map<String, String>,
        context: Context
    ) {
        // Generar ID único para el mensaje
        val messageId = generateMessageId()

        // Determinar el tipo de mensaje
        val messageType = determineMessageType(data)

        // Crear objeto FCMMessage
        val message = FCMMessage(
            messageId = messageId,
            type = messageType,
            title = title,
            body = body,
            data = data
        )

        // Agregar al historial
        addToHistory(message)

        // Publicar el mensaje
        _lastMessage.value = message

        // Mostrar notificación según el tipo
        showNotificationForType(message, context)

        Log.d(TAG, "Mensaje procesado: $message")
    }

    /**
     * Determina el tipo de mensaje basándose en los datos
     *
     * @param data Mapa de datos del mensaje
     * @return Tipo de mensaje identificado
     */
    private fun determineMessageType(data: Map<String, String>): MessageType {
        return when (data["type"]) {
            "asistencia" -> MessageType.ASISTENCIA
            "admin" -> MessageType.ADMIN
            "alert" -> MessageType.ALERT
            "general" -> MessageType.GENERAL
            else -> MessageType.UNKNOWN
        }
    }

    /**
     * Agrega un mensaje al historial
     *
     * @param message Mensaje a agregar
     */
    private fun addToHistory(message: FCMMessage) {
        messageHistory.add(message)
        _messages.value = messageHistory.toList()

        // Limitar el historial a los últimos 100 mensajes
        if (messageHistory.size > 100) {
            messageHistory.removeAt(0)
        }
    }

    /**
     * Muestra una notificación según el tipo de mensaje
     *
     * @param message Mensaje a mostrar
     * @param context Contexto de la aplicación
     */
    private fun showNotificationForType(message: FCMMessage, context: Context) {
        val notificationService = NotificationService(context)

        when (message.type) {
            MessageType.ASISTENCIA -> {
                val userName = message.data["userName"] ?: "Usuario"
                val action = message.data["action"] ?: "Acción"
                val isValid = message.data["isValid"]?.toBoolean() ?: false

                notificationService.showAsistenciaNotification(
                    userName = userName,
                    action = action,
                    isValid = isValid
                )
            }
            MessageType.ADMIN, MessageType.ALERT -> {
                notificationService.showAdminNotification(
                    title = message.title,
                    message = message.body
                )
            }
            MessageType.GENERAL, MessageType.UNKNOWN -> {
                notificationService.showNotification(
                    title = message.title,
                    message = message.body
                )
            }
        }
    }

    /**
     * Genera un ID único para el mensaje
     *
     * @return ID único del mensaje
     */
    private fun generateMessageId(): String {
        return "msg_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    /**
     * Obtiene mensajes por tipo
     *
     * @param type Tipo de mensaje a filtrar
     * @return Lista de mensajes del tipo especificado
     */
    fun getMessagesByType(type: MessageType): List<FCMMessage> {
        return messageHistory.filter { it.type == type }
    }

    /**
     * Limpia el historial de mensajes
     */
    fun clearHistory() {
        messageHistory.clear()
        _messages.value = emptyList()
        _lastMessage.value = null
        Log.d(TAG, "Historial de mensajes limpiado")
    }

    /**
     * Obtiene estadísticas de mensajes
     *
     * @return Mapa con conteo de mensajes por tipo
     */
    fun getMessageStats(): Map<MessageType, Int> {
        return messageHistory.groupingBy { it.type }.eachCount()
    }

    /**
     * Suscribe a actualizaciones de token
     *
     * @param onTokenUpdate Callback que se ejecuta cuando se actualiza el token
     */
    fun subscribeToTokenUpdates(onTokenUpdate: (String?) -> Unit) {
        // Esta función permite que otros componentes se suscriban a cambios en el token
        // En una implementación real, usarías coroutines para observar el StateFlow
    }

    /**
     * Procesa mensajes de datos (sin notificación)
     *
     * Estos mensajes se reciben cuando la app está en primer plano
     *
     * @param data Mapa de datos del mensaje
     * @param context Contexto de la aplicación
     */
    fun handleDataMessage(data: Map<String, String>, context: Context) {
        val title = data["title"] ?: "Notificación"
        val body = data["body"] ?: "Nuevo mensaje recibido"

        handleMessage(
            title = title,
            body = body,
            data = data,
            context = context
        )
    }
}

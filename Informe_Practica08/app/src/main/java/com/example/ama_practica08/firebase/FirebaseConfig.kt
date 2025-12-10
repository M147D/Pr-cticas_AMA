package com.example.ama_practica08.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

/**
 * FirebaseConfig
 *
 * Objeto singleton que gestiona la configuración y estado de Firebase
 * en la aplicación.
 *
 * Responsabilidades:
 * - Mantener la configuración de Firebase Cloud Messaging
 * - Gestionar el estado de inicialización
 * - Proporcionar métodos de configuración centralizados
 * - Mantener constantes de configuración
 *
 * Patrón: Singleton
 */
object FirebaseConfig {

    private const val TAG = "FirebaseConfig"

    /**
     * Configuración de tópicos de FCM
     */
    object Topics {
        const val ALL_USERS = "all_users"
        const val ADMINS = "admins"
        const val ASISTENCIA = "asistencia"
        const val ALERTS = "alerts"
    }

    /**
     * Claves para datos de mensajes
     */
    object MessageKeys {
        const val TYPE = "type"
        const val TITLE = "title"
        const val BODY = "body"
        const val USER_NAME = "userName"
        const val ACTION = "action"
        const val IS_VALID = "isValid"
        const val TIMESTAMP = "timestamp"
        const val PRIORITY = "priority"
    }

    /**
     * Prioridades de mensajes
     */
    enum class MessagePriority {
        LOW,
        NORMAL,
        HIGH,
        URGENT
    }

    /**
     * Estado de Firebase
     */
    private var _isInitialized = false
    val isInitialized: Boolean
        get() = _isInitialized

    /**
     * Token actual de FCM
     */
    private var _currentToken: String? = null
    val currentToken: String?
        get() = _currentToken

    /**
     * Habilitar/deshabilitar logs de depuración
     */
    var debugMode: Boolean = true

    /**
     * Inicializa la configuración de Firebase
     *
     * @param context Contexto de la aplicación
     */
    fun initialize(context: Context) {
        if (_isInitialized) {
            logDebug("Firebase ya está inicializado")
            return
        }

        try {
            // Inicializar Firebase
            FirebaseApp.initializeApp(context)

            _isInitialized = true
            logDebug("Firebase inicializado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar Firebase: ${e.message}", e)
        }
    }

    /**
     * Actualiza el token de FCM almacenado
     *
     * @param token Nuevo token de FCM
     */
    fun updateToken(token: String) {
        _currentToken = token
        logDebug("Token actualizado: $token")

        // Aquí se podría guardar el token en SharedPreferences
        // o enviarlo al servidor backend
    }

    /**
     * Suscribe a un tópico de FCM
     *
     * @param topic Nombre del tópico
     * @param onSuccess Callback de éxito
     * @param onError Callback de error
     */
    fun subscribeToTopic(
        topic: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnSuccessListener {
                    logDebug("Suscrito al tópico: $topic")
                    onSuccess?.invoke()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al suscribirse al tópico $topic: ${e.message}", e)
                    onError?.invoke(e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error al suscribirse al tópico $topic: ${e.message}", e)
            onError?.invoke(e)
        }
    }

    /**
     * Cancela la suscripción a un tópico de FCM
     *
     * @param topic Nombre del tópico
     * @param onSuccess Callback de éxito
     * @param onError Callback de error
     */
    fun unsubscribeFromTopic(
        topic: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnSuccessListener {
                    logDebug("Desuscrito del tópico: $topic")
                    onSuccess?.invoke()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al desuscribirse del tópico $topic: ${e.message}", e)
                    onError?.invoke(e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error al desuscribirse del tópico $topic: ${e.message}", e)
            onError?.invoke(e)
        }
    }

    /**
     * Obtiene el token de FCM
     *
     * @param onTokenReceived Callback que recibe el token
     */
    fun getToken(onTokenReceived: (String?) -> Unit) {
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                updateToken(token)
                logDebug("Token obtenido: $token")
                onTokenReceived(token)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error al obtener token: ${e.message}", e)
                onTokenReceived(null)
            }
    }

    /**
     * Elimina el token actual de FCM
     *
     * @param onSuccess Callback de éxito
     * @param onError Callback de error
     */
    fun deleteToken(
        onSuccess: (() -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null
    ) {
        try {
            FirebaseMessaging.getInstance().deleteToken()
                .addOnSuccessListener {
                    _currentToken = null
                    logDebug("Token eliminado")
                    onSuccess?.invoke()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error al eliminar token: ${e.message}", e)
                    onError?.invoke(e)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar token: ${e.message}", e)
            onError?.invoke(e)
        }
    }

    /**
     * Habilita/deshabilita la entrega automática de mensajes
     *
     * @param enabled true para habilitar, false para deshabilitar
     */
    fun setAutoInitEnabled(enabled: Boolean) {
        FirebaseMessaging.getInstance().isAutoInitEnabled = enabled
        logDebug("Auto-inicialización ${if (enabled) "habilitada" else "deshabilitada"}")
    }

    /**
     * Verifica si Firebase está correctamente configurado
     *
     * @return true si está configurado, false en caso contrario
     */
    fun isConfigured(): Boolean {
        return _isInitialized && _currentToken != null
    }

    /**
     * Reinicia la configuración de Firebase
     */
    fun reset() {
        _isInitialized = false
        _currentToken = null
        logDebug("Configuración de Firebase reiniciada")
    }

    /**
     * Registra un mensaje de debug si el modo debug está habilitado
     *
     * @param message Mensaje a registrar
     */
    private fun logDebug(message: String) {
        if (debugMode) {
            Log.d(TAG, message)
        }
    }

    /**
     * Configuración para notificaciones locales
     */
    object NotificationConfig {
        const val DEFAULT_ICON = android.R.drawable.ic_dialog_info
        const val SOUND_ENABLED = true
        const val VIBRATION_ENABLED = true
        const val LIGHTS_ENABLED = true

        // Colores para LED de notificación
        const val NOTIFICATION_COLOR = 0xFF2196F3.toInt() // Azul Material

        // Patrones de vibración (ms)
        val VIBRATION_PATTERN = longArrayOf(0, 250, 250, 250)
    }

    /**
     * URLs y endpoints del servidor
     */
    object ServerConfig {
        // Aquí se configurarían las URLs del backend
        const val BASE_URL = "https://api.example.com"
        const val TOKEN_ENDPOINT = "/api/fcm/register"
        const val UNREGISTER_ENDPOINT = "/api/fcm/unregister"
    }
}

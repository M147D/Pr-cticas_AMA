package com.example.ama_practica07.session

import com.example.ama_practica07.models.Usuario

/**
 * Representa el estado de sesión de la aplicación
 *
 * Estados posibles:
 * - Loading: Verificando si existe una sesión activa
 * - Active: Usuario con sesión activa
 * - Inactive: Sin sesión activa, debe mostrar login
 * - Error: Error al verificar o gestionar la sesión
 */
sealed class SessionState {
    /**
     * Estado mientras se verifica si existe una sesión activa
     * Se muestra al iniciar la aplicación
     */
    data object Loading : SessionState()

    /**
     * Sesión activa con información del usuario autenticado
     * @param usuario Información completa del usuario de la sesión
     * @param firebaseUid UID de Firebase del usuario autenticado
     */
    data class Active(
        val usuario: Usuario,
        val firebaseUid: String
    ) : SessionState()

    /**
     * Sin sesión activa - usuario debe iniciar sesión
     */
    data object Inactive : SessionState()

    /**
     * Error durante la gestión de la sesión
     * @param message Mensaje descriptivo del error
     */
    data class Error(val message: String) : SessionState()
}

/**
 * Datos de sesión para persistencia local
 * @param firebaseUid UID del usuario en Firebase
 * @param email Correo electrónico del usuario
 * @param displayName Nombre para mostrar del usuario
 * @param lastLoginTimestamp Timestamp del último inicio de sesión
 */
data class SessionData(
    val firebaseUid: String,
    val email: String?,
    val displayName: String?,
    val lastLoginTimestamp: Long = System.currentTimeMillis()
)

package com.example.ama_practica07.auth

/**
 * Representa el estado de autenticación del usuario
 */
sealed class AuthState {
    /**
     * Estado inicial, esperando que el usuario inicie sesión
     */
    data object NotAuthenticated : AuthState()

    /**
     * Estado mientras se está procesando el inicio de sesión
     */
    data object Loading : AuthState()

    /**
     * Usuario autenticado exitosamente con su información
     */
    data class Authenticated(val user: GoogleUserInfo) : AuthState()

    /**
     * Error durante el proceso de autenticación
     */
    data class Error(val message: String) : AuthState()
}

/**
 * Información del usuario obtenida de Google Sign-In
 */
data class GoogleUserInfo(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
) {
    /**
     * Convierte GoogleUserInfo a Usuario temporal para el sistema de asistencia
     * Por defecto asigna rol USER, edad 18, y enabled true
     * En futuras versiones se validará contra la base de datos
     */
    fun toUsuario(): com.example.ama_practica07.models.Usuario {
        return com.example.ama_practica07.models.Usuario(
            id = uid.hashCode(), // Usar hash del UID como ID temporal
            nombre = displayName ?: email ?: "Usuario Google",
            correo = email ?: "no-email@gmail.com",
            edad = 18, // Edad por defecto
            rol = com.example.ama_practica07.models.Rol.USER, // Por defecto USER
            enabled = true
        )
    }
}

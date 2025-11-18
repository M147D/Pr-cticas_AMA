package com.example.ama_practica06.auth

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
)

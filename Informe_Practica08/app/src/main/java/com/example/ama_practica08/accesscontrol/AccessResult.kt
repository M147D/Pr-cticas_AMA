package com.example.ama_practica08.accesscontrol

/**
 * Resultado de validación de acceso
 * Sealed class que representa los posibles resultados al validar el acceso a una pantalla
 */
sealed class AccessResult {
    /**
     * Acceso concedido - el usuario puede acceder a la pantalla
     */
    object Granted : AccessResult()

    /**
     * Acceso denegado - el usuario no tiene permisos
     * @param reason Razón del rechazo (ej: "Solo administradores")
     * @param fallbackScreen Pantalla a la que redirigir (ej: "home")
     */
    data class Denied(
        val reason: String,
        val fallbackScreen: String
    ) : AccessResult()

    /**
     * Requiere autenticación - el usuario no ha iniciado sesión
     * @param message Mensaje a mostrar al usuario
     */
    data class RequiresAuth(
        val message: String = "Debes iniciar sesión para acceder"
    ) : AccessResult()
}

package com.example.ama_practica09.models

/**
 * Enumeración para el rol del usuario
 */
enum class Rol {
    ADMIN,
    USER
}

/**
 * Data class Usuario
 * Representa un usuario del sistema con sus atributos y estado
 */
data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val edad: Int,
    val rol: Rol,
    val enabled: Boolean
) {
    /**
     * Extension function que verifica si el usuario es administrador
     */
    fun esAdmin(): Boolean = rol == Rol.ADMIN

    /**
     * Extension function que verifica si el usuario está activo
     */
    fun estaActivo(): Boolean = enabled

    /**
     * Extension function que verifica si el usuario puede acceder
     */
    fun puedeAcceder(): Boolean = enabled && edad >= 18
}

/**
 * Extension function para formatear la información del usuario
 */
fun Usuario.formatearInfo(): String {
    val rolTexto = if (rol == Rol.ADMIN) "Administrador" else "Usuario"
    val estadoTexto = if (enabled) "Activo" else "Inactivo"
    return """
        ID: $id
        Nombre: $nombre
        Correo: $correo
        Edad: $edad años
        Rol: $rolTexto
        Estado: $estadoTexto
    """.trimIndent()
}

/**
 * Extension function para obtener el nombre completo con el rol
 */
fun Usuario.nombreConRol(): String {
    val prefijo = if (rol == Rol.ADMIN) "[ADMIN]" else "[USER]"
    return "$prefijo $nombre"
}

// ==================== EXTENSION FUNCTIONS REQUERIDAS ====================

/**
 * Extension function: displayName
 * Devuelve el nombre del usuario
 */
fun Usuario.displayName(): String {
    return nombre
}

/**
 * Extension function: isEnabled
 * Indica si el usuario está activo en el sistema
 */
fun Usuario.isEnabled(): Boolean {
    return enabled
}

/**
 * Extension function: isAdmin
 * Verifica si el usuario tiene rol de administrador
 */
fun Usuario.isAdmin(): Boolean {
    return rol == Rol.ADMIN
}

/**
 * Extension function: allowedAt
 * Evalúa si el usuario está dentro del horario permitido (06:00 a 20:00)
 * @param hora Hora actual en formato 24 horas (0-23)
 * @return true si está dentro del horario permitido
 */
fun Usuario.allowedAt(hora: Int): Boolean {
    return hora in 6..20
}
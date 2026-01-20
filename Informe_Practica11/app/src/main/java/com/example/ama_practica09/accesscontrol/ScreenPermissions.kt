package com.example.ama_practica09.accesscontrol

import com.example.ama_practica09.models.Rol

/**
 * Niveles de permiso para las pantallas
 */
enum class ScreenPermission {
    PUBLIC,              // Sin autenticación requerida (Login, Welcome)
    AUTHENTICATED,       // Solo sesión activa, cualquier rol (Home, Dashboard)
    USER_ROLE,          // Solo usuarios con rol USER (UserScreen)
    ADMIN_ROLE,         // Solo usuarios con rol ADMIN (AdminScreen)
    ANY_ROLE            // Cualquier rol autenticado
}

/**
 * Configuración de una pantalla
 * Define los requisitos de acceso para cada pantalla de la aplicación
 *
 * @param screenName Nombre identificador de la pantalla (ej: "login", "admin")
 * @param permission Nivel de permiso requerido
 * @param requiresSecureFlag Si la pantalla requiere FLAG_SECURE (bloqueo de screenshots)
 * @param allowedRoles Roles específicos permitidos (usado con USER_ROLE y ADMIN_ROLE)
 */
data class ScreenConfig(
    val screenName: String,
    val permission: ScreenPermission,
    val requiresSecureFlag: Boolean = false,
    val allowedRoles: Set<Rol> = emptySet()
)

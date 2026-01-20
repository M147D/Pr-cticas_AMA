package com.example.ama_practica09.accesscontrol

import com.example.ama_practica09.models.Rol
import com.example.ama_practica09.models.Usuario
import com.example.ama_practica09.session.SessionManager
import com.example.ama_practica09.session.SessionState
import kotlinx.coroutines.flow.StateFlow

/**
 * Gestor central de control de acceso
 *
 * Responsabilidades:
 * - Validar acceso a pantallas según rol y estado de sesión
 * - Configurar permisos y restricciones por pantalla
 * - Determinar si una pantalla requiere FLAG_SECURE
 *
 * @param sessionManager Gestor de sesión que proporciona el estado actual
 */
class AccessControlManager(
    private val sessionManager: SessionManager
) {
    private val sessionState: StateFlow<SessionState> = sessionManager.sessionState

    /**
     * Configuración de pantallas y sus permisos
     * Cada entrada define qué nivel de acceso se requiere para cada pantalla
     */
    private val screenConfigs = mapOf(
        // Pantallas públicas (sin sesión requerida)
        "welcome" to ScreenConfig(
            screenName = "welcome",
            permission = ScreenPermission.PUBLIC
        ),
        "login" to ScreenConfig(
            screenName = "login",
            permission = ScreenPermission.PUBLIC
        ),
        "register" to ScreenConfig(
            screenName = "register",
            permission = ScreenPermission.PUBLIC
        ),
        "menu" to ScreenConfig(
            screenName = "menu",
            permission = ScreenPermission.PUBLIC
        ),
        "practica02" to ScreenConfig(
            screenName = "practica02",
            permission = ScreenPermission.PUBLIC
        ),
        "googleSignIn" to ScreenConfig(
            screenName = "googleSignIn",
            permission = ScreenPermission.PUBLIC
        ),

        // Pantallas protegidas (requieren sesión activa)
        "home" to ScreenConfig(
            screenName = "home",
            permission = ScreenPermission.AUTHENTICATED
        ),

        // Pantallas con restricción de rol USER
        "user" to ScreenConfig(
            screenName = "user",
            permission = ScreenPermission.USER_ROLE,
            requiresSecureFlag = false,
            allowedRoles = setOf(Rol.USER)
        ),

        // Pantallas con restricción de rol ADMIN (con FLAG_SECURE)
        "admin" to ScreenConfig(
            screenName = "admin",
            permission = ScreenPermission.ADMIN_ROLE,
            requiresSecureFlag = true,  // Bloquea screenshots en AdminScreen
            allowedRoles = setOf(Rol.ADMIN)
        ),

        // NUEVO: Pantalla de calificación (cualquier usuario autenticado)
        "rating" to ScreenConfig(
            screenName = "rating",
            permission = ScreenPermission.AUTHENTICATED
        ),

        // NUEVO: Pantalla de estadísticas de rating (solo admin)
        "ratingStats" to ScreenConfig(
            screenName = "ratingStats",
            permission = ScreenPermission.ADMIN_ROLE,
            requiresSecureFlag = false,
            allowedRoles = setOf(Rol.ADMIN)
        ),

        // NUEVO: Pantalla de gráficos estadísticos (cualquier usuario autenticado)
        "charts" to ScreenConfig(
            screenName = "charts",
            permission = ScreenPermission.AUTHENTICATED
        ),

        // NUEVO: Pantalla de animaciones (cualquier usuario autenticado)
        "animations" to ScreenConfig(
            screenName = "animations",
            permission = ScreenPermission.AUTHENTICATED
        )
    )

    /**
     * Valida si el usuario actual puede acceder a la pantalla solicitada
     *
     * @param targetScreen Nombre de la pantalla a la que se intenta acceder
     * @return AccessResult con el resultado de la validación
     */
    fun validateAccess(targetScreen: String): AccessResult {
        val config = screenConfigs[targetScreen]
            ?: return AccessResult.Denied(
                "Pantalla no configurada",
                "home"
            )

        return when (val state = sessionState.value) {
            is SessionState.Active -> {
                // Usuario autenticado - validar según configuración
                validateAuthenticatedAccess(config, state.usuario)
            }

            is SessionState.Inactive -> {
                // Sin sesión activa
                if (config.permission == ScreenPermission.PUBLIC) {
                    AccessResult.Granted
                } else {
                    AccessResult.RequiresAuth()
                }
            }

            is SessionState.Loading -> {
                // Sesión cargando - denegar temporalmente
                AccessResult.Denied("Cargando sesión...", "welcome")
            }

            is SessionState.Error -> {
                // Error en sesión - redirigir a login
                AccessResult.Denied("Error en sesión", "login")
            }
        }
    }

    /**
     * Valida acceso para usuarios autenticados
     * Verifica que el usuario tenga el rol adecuado y esté habilitado
     *
     * @param config Configuración de la pantalla
     * @param usuario Usuario autenticado
     * @return AccessResult con el resultado
     */
    private fun validateAuthenticatedAccess(
        config: ScreenConfig,
        usuario: Usuario
    ): AccessResult {
        // Validar si el usuario está habilitado
        if (!usuario.enabled) {
            return AccessResult.Denied(
                "Tu cuenta está deshabilitada",
                "login"
            )
        }

        return when (config.permission) {
            ScreenPermission.PUBLIC -> {
                // Pantalla pública - siempre permitir
                AccessResult.Granted
            }

            ScreenPermission.AUTHENTICATED -> {
                // Solo requiere estar autenticado
                AccessResult.Granted
            }

            ScreenPermission.USER_ROLE -> {
                // Permitir USER y ADMIN (admin tiene acceso a todo)
                if (usuario.rol == Rol.USER || usuario.rol == Rol.ADMIN) {
                    AccessResult.Granted
                } else {
                    AccessResult.Denied(
                        "Esta pantalla es solo para usuarios",
                        "home"
                    )
                }
            }

            ScreenPermission.ADMIN_ROLE -> {
                // Requiere rol ADMIN
                if (usuario.rol == Rol.ADMIN) {
                    AccessResult.Granted
                } else {
                    AccessResult.Denied(
                        "Acceso denegado: Solo administradores",
                        "home"
                    )
                }
            }

            ScreenPermission.ANY_ROLE -> {
                // Cualquier rol autenticado
                AccessResult.Granted
            }
        }
    }

    /**
     * Verifica si una pantalla requiere FLAG_SECURE
     * FLAG_SECURE bloquea screenshots y grabación de pantalla
     *
     * @param screenName Nombre de la pantalla
     * @return true si requiere FLAG_SECURE, false en caso contrario
     */
    fun requiresSecureFlag(screenName: String): Boolean {
        return screenConfigs[screenName]?.requiresSecureFlag ?: false
    }

    /**
     * Obtiene la pantalla de fallback para logout
     * @return Nombre de la pantalla a la que redirigir después del logout
     */
    fun getLogoutScreen(): String = "welcome"

    /**
     * Verifica si la pantalla actual es pública
     * @param screenName Nombre de la pantalla
     * @return true si es pública, false si requiere autenticación
     */
    fun isPublicScreen(screenName: String): Boolean {
        return screenConfigs[screenName]?.permission == ScreenPermission.PUBLIC
    }
}

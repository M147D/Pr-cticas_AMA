package com.example.ama_practica09.accesscontrol

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

/**
 * Composable HOC (Higher-Order Component) para proteger rutas
 *
 * Valida automáticamente el acceso a una pantalla y ejecuta callbacks según el resultado.
 * Envuelve el contenido de la pantalla y solo lo muestra si el acceso es concedido.
 *
 * Ejemplo de uso:
 * ```
 * ProtectedRoute(
 *     targetScreen = "admin",
 *     accessControlManager = accessControlManager,
 *     onAccessDenied = { reason, fallback ->
 *         showDialog(reason)
 *         navigateTo(fallback)
 *     },
 *     onRequiresAuth = { navigateTo("login") }
 * ) {
 *     AdminScreen(...)
 * }
 * ```
 *
 * @param targetScreen Nombre de la pantalla que se está protegiendo
 * @param accessControlManager Gestor de control de acceso
 * @param onAccessDenied Callback cuando el acceso es denegado (reason, fallbackScreen)
 * @param onRequiresAuth Callback cuando se requiere autenticación
 * @param content Contenido de la pantalla a mostrar si el acceso es concedido
 */
@Composable
fun ProtectedRoute(
    targetScreen: String,
    accessControlManager: AccessControlManager,
    onAccessDenied: (String, String) -> Unit,
    onRequiresAuth: () -> Unit,
    content: @Composable () -> Unit
) {
    // Validar acceso al renderizar
    val accessResult = remember(targetScreen) {
        accessControlManager.validateAccess(targetScreen)
    }

    when (accessResult) {
        is AccessResult.Granted -> {
            // Acceso concedido - mostrar contenido
            content()
        }

        is AccessResult.Denied -> {
            // Acceso denegado - ejecutar callback y redirigir
            LaunchedEffect(Unit) {
                onAccessDenied(accessResult.reason, accessResult.fallbackScreen)
            }
        }

        is AccessResult.RequiresAuth -> {
            // Requiere autenticación - ejecutar callback
            LaunchedEffect(Unit) {
                onRequiresAuth()
            }
        }
    }
}

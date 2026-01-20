package com.example.ama_practica09.accesscontrol

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Composable para aplicar FLAG_SECURE a pantallas sensibles
 *
 * FLAG_SECURE bloquea screenshots y grabación de pantalla, protegiendo
 * información sensible que se muestra en la pantalla.
 *
 * Uso:
 * - Se activa automáticamente según configuración en AccessControlManager
 * - Se aplica en DisposableEffect al entrar a la pantalla
 * - Se remueve automáticamente al salir de la pantalla
 *
 * Ejemplo de uso:
 * ```
 * SecureScreen(
 *     screenName = "admin",
 *     accessControlManager = accessControlManager
 * ) {
 *     // Contenido de la pantalla (screenshots bloqueados si requiresSecureFlag=true)
 *     AdminScreenContent()
 * }
 * ```
 *
 * Comportamiento:
 * - AdminScreen: FLAG_SECURE activado (screenshots bloqueados)
 * - Otras pantallas: FLAG_SECURE desactivado (screenshots permitidos)
 *
 * @param screenName Nombre de la pantalla actual
 * @param accessControlManager Gestor de control de acceso
 * @param content Contenido de la pantalla
 */
@Composable
fun SecureScreen(
    screenName: String,
    accessControlManager: AccessControlManager,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context.findActivity()

    // Aplicar o remover FLAG_SECURE según configuración
    DisposableEffect(screenName) {
        val requiresSecure = accessControlManager.requiresSecureFlag(screenName)

        if (requiresSecure && activity != null) {
            // Activar FLAG_SECURE para esta pantalla
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
            Log.d("SecureScreen", "FLAG_SECURE activado para $screenName - Screenshots bloqueados")
        }

        onDispose {
            if (requiresSecure && activity != null) {
                // Remover FLAG_SECURE al salir de la pantalla
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                Log.d("SecureScreen", "FLAG_SECURE desactivado para $screenName - Screenshots permitidos")
            }
        }
    }

    // Renderizar contenido
    content()
}

/**
 * Extension function para obtener la Activity desde un Context
 * Navega por la jerarquía de ContextWrapper hasta encontrar una Activity
 *
 * @return Activity si se encuentra, null en caso contrario
 */
private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

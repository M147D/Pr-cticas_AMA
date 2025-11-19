package com.example.ama_practica07.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

/**
 * Navegación principal para el flujo de Google Sign-In
 * Maneja la transición entre la pantalla de login y la pantalla principal
 */
@Composable
fun GoogleSignInNavigation(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Crear instancia del auth manager
    val authManager = remember { GoogleAuthManager(context) }

    // Observar el estado de autenticación
    val authState by authManager.authState.collectAsState()

    // Launcher para manejar el resultado del sign-in (usando StartActivityForResult estándar)
    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            scope.launch {
                authManager.handleSignInResult(result.data)
            }
        } else {
            // El usuario canceló el inicio de sesión
            scope.launch {
                authManager.handleSignInResult(null)
            }
        }
    }

    // Mostrar la pantalla apropiada según el estado
    when (val state = authState) {
        is AuthState.Authenticated -> {
            // Usuario autenticado - mostrar pantalla principal
            HomeScreen(
                user = state.user,
                onSignOutClick = {
                    scope.launch {
                        authManager.signOut()
                    }
                }
            )
        }

        else -> {
            // Usuario no autenticado - mostrar pantalla de login
            LoginScreen(
                authState = state,
                onSignInClick = {
                    authManager.signIn(signInLauncher)
                },
                onBack = onBack
            )
        }
    }
}

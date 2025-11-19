package com.example.ama_practica07.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

/**
 * Gestor de autenticación con Google Sign-In
 * Maneja todo el flujo de autenticación usando Firebase Auth y Google Sign-In tradicional
 */
class GoogleAuthManager(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient

    // Estado de autenticación observable
    private val _authState = MutableStateFlow<AuthState>(AuthState.NotAuthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Web Client ID desde google-services.json
    private val webClientId = "287964275463-ppjkco9o3e5tluq3sq9kvttcr7e3vkg6.apps.googleusercontent.com"

    companion object {
        private const val TAG = "GoogleAuthManager"
    }

    init {
        // Configurar Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)

        // Verificar si ya hay un usuario autenticado
        checkCurrentUser()
    }

    /**
     * Verifica si hay un usuario actualmente autenticado
     */
    private fun checkCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(
                GoogleUserInfo(
                    uid = currentUser.uid,
                    email = currentUser.email,
                    displayName = currentUser.displayName,
                    photoUrl = currentUser.photoUrl?.toString()
                )
            )
        }
    }

    /**
     * Obtiene el Intent para iniciar el flujo de Google Sign-In
     */
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    /**
     * Inicia el flujo de inicio de sesión con Google
     */
    fun signIn(launcher: ActivityResultLauncher<Intent>) {
        try {
            _authState.value = AuthState.Loading
            launcher.launch(getSignInIntent())
        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar sign-in", e)
            _authState.value = AuthState.Error("Error al iniciar sesión: ${e.message}")
        }
    }

    /**
     * Procesa el resultado del inicio de sesión
     */
    suspend fun handleSignInResult(data: Intent?) {
        try {
            if (data == null) {
                _authState.value = AuthState.Error("No se recibieron datos de autenticación")
                return
            }

            // Obtener la cuenta de Google del Intent
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)

            if (account == null) {
                _authState.value = AuthState.Error("No se pudo obtener la cuenta de Google")
                return
            }

            // Obtener el token ID
            val idToken = account.idToken
            if (idToken == null) {
                _authState.value = AuthState.Error("No se recibió el token de Google")
                return
            }

            // Autenticar con Firebase usando el token de Google
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()

            val user = authResult.user
            if (user != null) {
                Log.d(TAG, "Usuario autenticado: ${user.email}")
                _authState.value = AuthState.Authenticated(
                    GoogleUserInfo(
                        uid = user.uid,
                        email = user.email,
                        displayName = user.displayName,
                        photoUrl = user.photoUrl?.toString()
                    )
                )
            } else {
                _authState.value = AuthState.Error("Error al obtener información del usuario")
            }

        } catch (e: ApiException) {
            Log.e(TAG, "Error de API de Google Sign-In", e)
            _authState.value = AuthState.Error("Error de Google Sign-In: ${e.statusCode}")
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar resultado de sign-in", e)
            _authState.value = AuthState.Error("Error de autenticación: ${e.message}")
        }
    }

    /**
     * Cierra la sesión del usuario
     */
    suspend fun signOut() {
        try {
            // Cerrar sesión en Firebase
            auth.signOut()

            // Cerrar sesión en Google
            googleSignInClient.signOut().await()

            Log.d(TAG, "Sesión cerrada exitosamente")
            _authState.value = AuthState.NotAuthenticated

        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión", e)
            // Aún así actualizar el estado a no autenticado
            _authState.value = AuthState.NotAuthenticated
        }
    }

    /**
     * Obtiene el usuario actualmente autenticado
     */
    fun getCurrentUser(): GoogleUserInfo? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.user
            else -> null
        }
    }
}

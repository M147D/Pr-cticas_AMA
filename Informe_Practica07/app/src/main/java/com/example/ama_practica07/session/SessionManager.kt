package com.example.ama_practica07.session

import android.content.Context
import android.util.Log
import com.example.ama_practica07.models.Rol
import com.example.ama_practica07.models.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/**
 * Gestor centralizado de control de sesión
 *
 * Responsabilidades:
 * - Verificar sesión activa con FirebaseAuth
 * - Gestionar persistencia de sesión con SessionRepository
 * - Proporcionar estado observable de la sesión
 * - Manejar inicio y cierre de sesión
 *
 * SEGURIDAD:
 * - Utiliza FirebaseAuth como fuente de verdad
 * - Limpia datos locales al cerrar sesión
 * - No almacena credenciales sensibles
 */
class SessionManager(context: Context) {

    companion object {
        private const val TAG = "SessionManager"
    }

    private val repository = SessionRepository(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Estado observable de la sesión
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Loading)
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    /**
     * Verifica si existe una sesión activa al iniciar la aplicación
     *
     * Proceso:
     * 1. Verifica si hay usuario autenticado en FirebaseAuth
     * 2. Si existe, restaura la sesión
     * 3. Si no existe, limpia datos locales y marca sesión como inactiva
     */
    suspend fun checkActiveSession() {
        try {
            Log.d(TAG, "Verificando sesión activa...")
            _sessionState.value = SessionState.Loading

            val currentUser = firebaseAuth.currentUser

            if (currentUser != null) {
                Log.d(TAG, "Usuario autenticado encontrado en Firebase: ${currentUser.uid}")

                // Verificar si hay datos guardados localmente
                val sessionData = repository.getSessionData().first()

                if (sessionData != null && sessionData.firebaseUid == currentUser.uid) {
                    // Sesión válida - restaurar
                    Log.d(TAG, "Restaurando sesión existente")
                    val usuario = firebaseUserToUsuario(currentUser)
                    _sessionState.value = SessionState.Active(usuario, currentUser.uid)

                    // Actualizar timestamp de último acceso
                    repository.updateLastLogin()
                } else {
                    // Usuario de Firebase pero sin datos locales - crear nueva sesión
                    Log.d(TAG, "Usuario de Firebase sin datos locales, creando sesión")
                    startSession(currentUser)
                }
            } else {
                Log.d(TAG, "No hay usuario autenticado en Firebase")
                // Limpiar cualquier dato local residual
                repository.clearSessionData()
                _sessionState.value = SessionState.Inactive
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar sesión: ${e.message}", e)
            _sessionState.value = SessionState.Error("Error al verificar sesión: ${e.message}")
        }
    }

    /**
     * Inicia una nueva sesión con el usuario de Firebase
     * @param firebaseUser Usuario autenticado de Firebase
     */
    suspend fun startSession(firebaseUser: FirebaseUser) {
        try {
            Log.d(TAG, "Iniciando nueva sesión para usuario: ${firebaseUser.uid}")

            // Guardar datos de sesión en almacenamiento local
            val sessionData = SessionData(
                firebaseUid = firebaseUser.uid,
                email = firebaseUser.email,
                displayName = firebaseUser.displayName,
                lastLoginTimestamp = System.currentTimeMillis()
            )
            repository.saveSessionData(sessionData)

            // Convertir a Usuario del sistema
            val usuario = firebaseUserToUsuario(firebaseUser)

            // Actualizar estado de sesión
            _sessionState.value = SessionState.Active(usuario, firebaseUser.uid)

            Log.d(TAG, "Sesión iniciada exitosamente")

        } catch (e: Exception) {
            Log.e(TAG, "Error al iniciar sesión: ${e.message}", e)
            _sessionState.value = SessionState.Error("Error al iniciar sesión: ${e.message}")
        }
    }

    /**
     * Cierra la sesión actual
     *
     * Proceso:
     * 1. Cierra sesión en FirebaseAuth
     * 2. Limpia datos del almacenamiento local
     * 3. Actualiza estado a Inactive
     */
    suspend fun signOut() {
        try {
            Log.d(TAG, "Cerrando sesión...")

            // Cerrar sesión en Firebase
            firebaseAuth.signOut()

            // Limpiar datos locales
            repository.clearSessionData()

            // Actualizar estado
            _sessionState.value = SessionState.Inactive

            Log.d(TAG, "Sesión cerrada exitosamente")

        } catch (e: Exception) {
            Log.e(TAG, "Error al cerrar sesión: ${e.message}", e)
            _sessionState.value = SessionState.Error("Error al cerrar sesión: ${e.message}")
        }
    }

    /**
     * Obtiene el usuario actual de la sesión
     * @return Usuario si hay sesión activa, null en caso contrario
     */
    fun getCurrentUser(): Usuario? {
        return when (val state = _sessionState.value) {
            is SessionState.Active -> state.usuario
            else -> null
        }
    }

    /**
     * Verifica si hay una sesión activa
     * @return true si la sesión está activa, false en caso contrario
     */
    fun isSessionActive(): Boolean {
        return _sessionState.value is SessionState.Active
    }

    /**
     * Convierte un FirebaseUser a Usuario del sistema
     * Por defecto asigna rol USER y enabled true
     * En futuras versiones se validará contra la base de datos
     */
    private fun firebaseUserToUsuario(firebaseUser: FirebaseUser): Usuario {
        return Usuario(
            id = firebaseUser.uid.hashCode(),
            nombre = firebaseUser.displayName ?: firebaseUser.email ?: "Usuario",
            correo = firebaseUser.email ?: "sin-email@gmail.com",
            edad = 18, // Edad por defecto
            rol = Rol.USER, // Por defecto USER
            enabled = true
        )
    }
}

package com.example.ama_practica08.session

import android.content.Context
import android.util.Log
import com.example.ama_practica08.models.Rol
import com.example.ama_practica08.models.Usuario
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
     * Proceso MODIFICADO para soportar login tradicional y Firebase:
     * 1. Primero verifica si hay datos guardados localmente en DataStore
     * 2. Si hay datos con UID que empieza con "local_" → es login tradicional
     * 3. Si hay datos con UID normal → es login de Firebase
     * 4. Solo limpia si NO hay ningún dato guardado
     */
    suspend fun checkActiveSession() {
        try {
            Log.d(TAG, "Verificando sesión activa...")
            _sessionState.value = SessionState.Loading

            // PASO 1: Verificar primero si hay datos guardados localmente
            val sessionData = repository.getSessionData().first()

            if (sessionData != null) {
                Log.d(TAG, "Datos de sesión encontrados: ${sessionData.firebaseUid}")

                // PASO 2: Verificar si es sesión tradicional o Firebase
                if (sessionData.firebaseUid.startsWith("local_")) {
                    // ✅ SESIÓN TRADICIONAL
                    Log.d(TAG, "Restaurando sesión tradicional")

                    val userId = sessionData.firebaseUid.removePrefix("local_").toIntOrNull()

                    if (userId != null) {
                        // Buscar usuario en UsuarioRepository
                        val usuario = com.example.ama_practica08.data.UsuarioRepository
                            .obtenerUsuarioPorId(userId)

                        if (usuario != null && usuario.enabled) {
                            // Sesión tradicional válida - restaurar
                            _sessionState.value = SessionState.Active(usuario, sessionData.firebaseUid)
                            repository.updateLastLogin()
                            Log.d(TAG, "Sesión tradicional restaurada para: ${usuario.nombre}")
                        } else {
                            Log.w(TAG, "Usuario tradicional no encontrado o deshabilitado")
                            repository.clearSessionData()
                            _sessionState.value = SessionState.Inactive
                        }
                    } else {
                        Log.e(TAG, "UID tradicional inválido")
                        repository.clearSessionData()
                        _sessionState.value = SessionState.Inactive
                    }
                } else {
                    // ✅ SESIÓN DE FIREBASE
                    Log.d(TAG, "Verificando sesión de Firebase")
                    val currentUser = firebaseAuth.currentUser

                    if (currentUser != null && sessionData.firebaseUid == currentUser.uid) {
                        // Sesión Firebase válida - restaurar
                        Log.d(TAG, "Restaurando sesión de Firebase")
                        val usuario = firebaseUserToUsuario(currentUser)
                        _sessionState.value = SessionState.Active(usuario, currentUser.uid)
                        repository.updateLastLogin()
                    } else {
                        // Usuario de Firebase cerró sesión - limpiar
                        Log.w(TAG, "Sesión de Firebase no coincide, limpiando")
                        repository.clearSessionData()
                        _sessionState.value = SessionState.Inactive
                    }
                }
            } else {
                // No hay datos guardados - sesión inactiva
                Log.d(TAG, "No hay datos de sesión guardados")
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

    /**
     * Guarda una sesión para login tradicional (sin Firebase)
     * Crea un UID sintético usando el userId y actualiza el estado de sesión
     *
     * @param userId ID del usuario del repositorio local
     * @param email Correo del usuario
     * @param displayName Nombre del usuario
     */
    suspend fun saveTraditionalLogin(
        userId: String,
        email: String,
        displayName: String
    ) {
        try {
            Log.d(TAG, "Guardando sesión de login tradicional para usuario: $userId")

            // Crear un UID sintético para login tradicional
            val syntheticUid = "local_$userId"

            // Guardar datos de sesión en almacenamiento local
            val sessionData = SessionData(
                firebaseUid = syntheticUid,
                email = email,
                displayName = displayName,
                lastLoginTimestamp = System.currentTimeMillis()
            )
            repository.saveSessionData(sessionData)

            // NUEVO: Buscar el usuario completo y actualizar el estado inmediatamente
            val userIdInt = userId.toIntOrNull()
            if (userIdInt != null) {
                val usuario = com.example.ama_practica08.data.UsuarioRepository
                    .obtenerUsuarioPorId(userIdInt)

                if (usuario != null && usuario.enabled) {
                    // Actualizar estado de sesión inmediatamente
                    _sessionState.value = SessionState.Active(usuario, syntheticUid)
                    Log.d(TAG, "Sesión tradicional iniciada para: ${usuario.nombre} (${usuario.rol})")
                } else {
                    Log.w(TAG, "Usuario no encontrado o deshabilitado")
                    _sessionState.value = SessionState.Inactive
                }
            }

            Log.d(TAG, "Sesión tradicional guardada exitosamente")

        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar sesión tradicional: ${e.message}", e)
            _sessionState.value = SessionState.Error("Error al iniciar sesión: ${e.message}")
        }
    }
}

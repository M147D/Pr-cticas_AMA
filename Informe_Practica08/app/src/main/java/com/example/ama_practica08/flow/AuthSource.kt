package com.example.ama_practica08.flow

import com.example.ama_practica08.models.Usuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Publicador: AuthSource
 *
 * Source que publica el usuario actualmente autenticado en el sistema.
 *
 * Los suscriptores pueden observar cambios en el usuario actual
 * sin necesidad de acoplamiento directo con la autenticación.
 */
object AuthSource {

    /**
     * StateFlow privado mutable que mantiene el usuario actual
     */
    private val _userFlow = MutableStateFlow<Usuario?>(null)

    /**
     * StateFlow público de solo lectura que expone el usuario actual
     *
     * Este Flow emite un nuevo valor cada vez que:
     * - Un usuario inicia sesión
     * - Un usuario cierra sesión
     * - Se actualiza la información del usuario
     *
     * @return StateFlow<Usuario?> que emite el usuario actual o null si no hay sesión
     */
    val userFlow: StateFlow<Usuario?> = _userFlow.asStateFlow()

    /**
     * Publica un nuevo usuario en el sistema
     *
     * @param usuario Usuario que ha iniciado sesión
     */
    fun login(usuario: Usuario) {
        _userFlow.value = usuario
    }

    /**
     * Publica que no hay usuario autenticado
     */
    fun logout() {
        _userFlow.value = null
    }

    /**
     * Obtiene el usuario actual de forma síncrona
     *
     * @return Usuario actual o null si no hay sesión
     */
    fun getCurrentUser(): Usuario? {
        return _userFlow.value
    }

    /**
     * Verifica si hay un usuario autenticado
     *
     * @return true si hay sesión activa, false en caso contrario
     */
    fun isAuthenticated(): Boolean {
        return _userFlow.value != null
    }
}
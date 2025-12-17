package com.example.ama_practica09.viewmodel

import com.example.ama_practica09.models.RegistroAcceso
import com.example.ama_practica09.models.Ubicacion
import com.example.ama_practica09.models.Usuario
import com.example.ama_practica09.rules.PolicyRules

/**
 * AppState
 *
 * Representa el estado completo de la aplicación en un momento dado.
 * Este estado es inmutable y cada cambio produce un nuevo AppState.
 *
 * El ViewModel expone un StateFlow<AppState> que emite nuevos estados
 * cada vez que cambia alguna parte de la aplicación.
 */
data class AppState(
    /**
     * Usuario actualmente autenticado
     * null si no hay sesión activa
     */
    val usuario: Usuario? = null,

    /**
     * Ubicación actual del usuario
     */
    val ubicacion: Ubicacion = Ubicacion.DentroDelRango("Campus Principal"),

    /**
     * Hora actual del sistema (0-23)
     */
    val hora: Int = 0,

    /**
     * Indica si el usuario puede registrar asistencia actualmente
     * Este valor se calcula reactivamente basado en PolicyRules.canRegister
     */
    val canRegister: Boolean = false,

    /**
     * Resultado detallado de la evaluación de la política
     * Contiene mensaje y razón de por qué puede o no puede registrar
     */
    val evaluacion: PolicyRules.ResultadoEvaluacion = PolicyRules.ResultadoEvaluacion(
        permitido = false,
        mensaje = "Evaluando...",
        razon = ""
    ),

    /**
     * Lista de todos los registros de acceso
     */
    val registros: List<RegistroAcceso> = emptyList(),

    /**
     * Indica si la aplicación está procesando alguna operación
     * Útil para mostrar indicadores de carga
     */
    val isLoading: Boolean = false,

    /**
     * Mensaje de error si ocurre algún problema
     * null si no hay errores
     */
    val error: String? = null
) {
    /**
     * Indica si hay un usuario autenticado
     */
    val isAuthenticated: Boolean
        get() = usuario != null

    /**
     * Registros del usuario actual
     * Lista vacía si no hay usuario autenticado
     */
    val misRegistros: List<RegistroAcceso>
        get() = usuario?.let { user ->
            registros.filter { it.usuario.id == user.id }
        } ?: emptyList()

    /**
     * Registros válidos
     */
    val registrosValidos: List<RegistroAcceso>
        get() = registros.filter { it.esAccesoValido() }

    /**
     * Registros inválidos
     */
    val registrosInvalidos: List<RegistroAcceso>
        get() = registros.filter { !it.esAccesoValido() }
}
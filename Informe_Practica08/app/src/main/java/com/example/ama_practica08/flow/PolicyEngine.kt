package com.example.ama_practica08.flow

import com.example.ama_practica08.rules.PolicyRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * PolicyEngine
 *
 * Motor de políticas que combina los diferentes sources (AuthSource, LocationSource, TimeSource)
 * y evalúa reactivamente la política canRegister.
 *
 * Este engine implementa el patrón publicador-suscriptor:
 * - Se SUSCRIBE a: userFlow, zoneFlow, timeFlow
 * - PUBLICA: canRegister Flow<Boolean>
 *
 * Cuando cualquiera de los sources emite un nuevo valor, el engine
 * automáticamente re-evalúa la política usando PolicyRules.canRegister
 */
object PolicyEngine {

    /**
     * Flow reactivo que publica si el usuario puede registrar asistencia
     *
     * Este Flow combina tres sources:
     * - AuthSource.userFlow (usuario actual)
     * - LocationSource.zoneFlow (ubicación actual)
     * - TimeSource.timeFlow (hora actual)
     *
     * Cada vez que cualquiera de estos valores cambia, se re-evalúa
     * automáticamente la política canRegister definida en PolicyRules.
     *
     * IMPORTANTE: Este Flow usa internamente PolicyRules.canRegister,
     * NO lo reemplaza. La lógica de negocio se mantiene en PolicyRules.
     *
     * @return Flow<Boolean> que emite true si el usuario puede registrar,
     *         false en caso contrario
     */
    val canRegister: Flow<Boolean> = combine(
        AuthSource.userFlow,
        LocationSource.zoneFlow,
        TimeSource.timeFlow
    ) { usuario, ubicacion, hora ->
        // Si no hay usuario autenticado, no puede registrar
        if (usuario == null) {
            return@combine false
        }

        // Crear contexto con los valores actuales
        val context = PolicyRules.RegistroContext(
            usuario = usuario,
            ubicacion = ubicacion,
            hora = hora
        )

        // Evaluar la política usando la regla existente
        PolicyRules.canRegister(context)
    }

    /**
     * Flow que publica el resultado detallado de la evaluación de la política
     *
     * Similar a canRegister, pero en lugar de solo emitir true/false,
     * emite un ResultadoEvaluacion completo con mensaje y razón.
     *
     * Útil para mostrar feedback detallado al usuario sobre por qué
     * puede o no puede registrar asistencia.
     *
     * @return Flow<PolicyRules.ResultadoEvaluacion> con evaluación completa
     */
    val evaluacionDetallada: Flow<PolicyRules.ResultadoEvaluacion> = combine(
        AuthSource.userFlow,
        LocationSource.zoneFlow,
        TimeSource.timeFlow
    ) { usuario, ubicacion, hora ->
        // Si no hay usuario, retornar evaluación negativa
        if (usuario == null) {
            return@combine PolicyRules.ResultadoEvaluacion(
                permitido = false,
                mensaje = "DESHABILITADO: No hay sesión activa",
                razon = "Debes iniciar sesión primero"
            )
        }

        // Crear contexto con los valores actuales
        val context = PolicyRules.RegistroContext(
            usuario = usuario,
            ubicacion = ubicacion,
            hora = hora
        )

        // Evaluar usando la función completa que retorna detalles
        PolicyRules.evaluarRegistro(context)
    }

    /**
     * Evalúa la política de forma síncrona con el estado actual
     *
     * Útil cuando se necesita saber inmediatamente si se puede registrar
     * sin necesidad de suscribirse al Flow.
     *
     * @return Boolean indicando si actualmente se puede registrar
     */
    fun canRegisterNow(): Boolean {
        val usuario = AuthSource.getCurrentUser() ?: return false
        val ubicacion = LocationSource.getCurrentZone()
        val hora = TimeSource.getCurrentHour()

        val context = PolicyRules.RegistroContext(
            usuario = usuario,
            ubicacion = ubicacion,
            hora = hora
        )

        return PolicyRules.canRegister(context)
    }

    /**
     * Evalúa la política de forma síncrona y retorna el resultado detallado
     *
     * @return ResultadoEvaluacion con detalles de la evaluación
     */
    fun evaluarNow(): PolicyRules.ResultadoEvaluacion {
        val usuario = AuthSource.getCurrentUser()
            ?: return PolicyRules.ResultadoEvaluacion(
                permitido = false,
                mensaje = "DESHABILITADO: No hay sesión activa",
                razon = "Debes iniciar sesión primero"
            )

        val ubicacion = LocationSource.getCurrentZone()
        val hora = TimeSource.getCurrentHour()

        val context = PolicyRules.RegistroContext(
            usuario = usuario,
            ubicacion = ubicacion,
            hora = hora
        )

        return PolicyRules.evaluarRegistro(context)
    }
}

package com.example.ama_practica09.rules

import com.example.ama_practica09.models.Usuario
import com.example.ama_practica09.models.Ubicacion
import com.example.ama_practica09.models.isEnabled
import com.example.ama_practica09.models.allowedAt
import com.example.ama_practica09.models.isAdmin
import com.example.ama_practica09.models.displayName
import java.util.Calendar

/**
 * Tipo de función para representar una regla
 * La regla es la función que toma un contexto y devuelve un booleano
 */
typealias Rule<T> = (T) -> Boolean

/**
 * Objeto que contiene las reglas atómicas y combinadores lógicos
 * para la política de registro de asistencia
 */
object PolicyRules {

    // ==================== COMBINADORES LÓGICOS ====================

    /**
     * Combinador AND: combina dos reglas con operador lógico AND
     * Ambas reglas deben cumplirse
     */
    fun <T> and(rule1: Rule<T>, rule2: Rule<T>): Rule<T> {
        return { context -> rule1(context) && rule2(context) }
    }

    /**
     * Combinador OR: combina dos reglas con operador lógico OR
     * Al menos una regla debe cumplirse
     */
    fun <T> or(rule1: Rule<T>, rule2: Rule<T>): Rule<T> {
        return { context -> rule1(context) || rule2(context) }
    }

    /**
     * Combinador NOT: niega una regla
     * La regla debe NO cumplirse
     */
    fun <T> not(rule: Rule<T>): Rule<T> {
        return { context -> !rule(context) }
    }

    /**
     * Combinador AND múltiple: combina múltiples reglas con AND
     * Todas las reglas deben cumplirse
     */
    fun <T> andAll(vararg rules: Rule<T>): Rule<T> {
        return { context -> rules.all { it(context) } }
    }

    /**
     * Combinador OR múltiple: combina múltiples reglas con OR
     * Al menos una regla debe cumplirse
     */
    fun <T> orAny(vararg rules: Rule<T>): Rule<T> {
        return { context -> rules.any { it(context) } }
    }

    // ==================== CONTEXTO DE REGISTRO ====================

    /**
     * Data class que representa el contexto para evaluar si un usuario puede registrar
     */
    data class RegistroContext(
        val usuario: Usuario,
        val ubicacion: Ubicacion,
        val hora: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    )

    // ==================== REGLAS ATÓMICAS ====================

    /**
     * Regla atómica: Usuario habilitado
     * Verifica que el usuario esté activo en el sistema
     */
    val usuarioHabilitado: Rule<RegistroContext> = { context ->
        context.usuario.isEnabled()
    }

    /**
     * Regla atómica: Horario válido
     * Verifica que esté dentro del horario permitido (06:00 a 20:00)
     */
    val horarioValido: Rule<RegistroContext> = { context ->
        context.usuario.allowedAt(context.hora)
    }

    /**
     * Regla atómica: Ubicación válida
     * Verifica que el usuario esté dentro del rango permitido
     */
    val ubicacionValida: Rule<RegistroContext> = { context ->
        context.ubicacion.estaDentroDelRango()
    }

    /**
     * Regla atómica: Usuario mayor de edad
     * Verifica que el usuario tenga 18 años o más
     */
    val esMayorDeEdad: Rule<RegistroContext> = { context ->
        context.usuario.edad >= 18
    }

    /**
     * Regla atómica: Usuario NO es administrador
     * Los administradores no registran asistencia
     */
    val noEsAdmin: Rule<RegistroContext> = { context ->
        !context.usuario.isAdmin()
    }

    // ==================== POLÍTICA CANREGISTER ====================

    /**
     * Política final: canRegister
     * Combina las tres reglas principales:
     * - Usuario habilitado AND
     * - Horario válido AND
     * - Ubicación válida AND
     * - Mayor de edad AND
     * - No es administrador (los admins no registran asistencia)
     */
    val canRegister: Rule<RegistroContext> = andAll(
        usuarioHabilitado,
        horarioValido,
        ubicacionValida,
        esMayorDeEdad,
        noEsAdmin
    )

    /**
     * Política alternativa: canRegister sin restricción de ubicación
     * Útil para registros remotos o especiales
     */
    val canRegisterRemote: Rule<RegistroContext> = andAll(
        usuarioHabilitado,
        horarioValido,
        esMayorDeEdad,
        noEsAdmin
    )

    /**
     * Política para administradores: pueden acceder sin restricciones
     * pero NO pueden registrar asistencia
     */
    val canAccessAsAdmin: Rule<RegistroContext> = { context ->
        context.usuario.isAdmin() && context.usuario.isEnabled()
    }

    // ==================== FUNCIONES DE EVALUACIÓN ====================

    /**
     * Evalúa si un usuario puede registrar asistencia y devuelve un mensaje descriptivo
     */
    fun evaluarRegistro(context: RegistroContext): ResultadoEvaluacion {
        // Verificar si es administrador
        if (context.usuario.isAdmin()) {
            return ResultadoEvaluacion(
                permitido = false,
                mensaje = "ACCESO DENEGADO: Los administradores no registran asistencia",
                razon = "Usuario con rol de administrador"
            )
        }

        // Verificar cada regla individualmente para dar feedback específico
        if (!usuarioHabilitado(context)) {
            return ResultadoEvaluacion(
                permitido = false,
                mensaje = "DESHABILITADO: Usuario no activo en el sistema",
                razon = "Usuario deshabilitado"
            )
        }

        if (!horarioValido(context)) {
            return ResultadoEvaluacion(
                permitido = false,
                mensaje = "DESHABILITADO: Fuera del horario permitido (06:00 - 20:00)",
                razon = "Horario inválido (hora actual: ${context.hora}:00)"
            )
        }

        if (!ubicacionValida(context)) {
            return ResultadoEvaluacion(
                permitido = false,
                mensaje = "DESHABILITADO: Ubicación fuera del rango permitido",
                razon = context.ubicacion.obtenerDescripcion()
            )
        }

        if (!esMayorDeEdad(context)) {
            return ResultadoEvaluacion(
                permitido = false,
                mensaje = "DESHABILITADO: Usuario menor de edad",
                razon = "Edad: ${context.usuario.edad} años (se requiere 18+)"
            )
        }

        // Si todas las reglas se cumplen
        return ResultadoEvaluacion(
            permitido = true,
            mensaje = "HABILITADO: Puede registrar asistencia",
            razon = "Todas las validaciones pasadas"
        )
    }

    /**
     * Data class que representa el resultado de evaluar la política
     */
    data class ResultadoEvaluacion(
        val permitido: Boolean,
        val mensaje: String,
        val razon: String
    )

    // ==================== FUNCIONES AUXILIARES ====================

    /**
     * Obtiene la hora actual del sistema
     */
    fun obtenerHoraActual(): Int {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }

    /**
     * Verifica si una hora específica está en horario válido
     */
    fun esHorarioValido(hora: Int): Boolean {
        return hora in 6..20
    }

    /**
     * Obtiene un mensaje descriptivo del horario
     */
    fun obtenerMensajeHorario(hora: Int): String {
        return when {
            hora < 6 -> "Demasiado temprano (antes de las 06:00)"
            hora > 20 -> "Demasiado tarde (después de las 20:00)"
            else -> "Horario válido"
        }
    }

    /**
     * Genera un reporte de evaluación detallado
     */
    fun generarReporteDetallado(context: RegistroContext): String {
        val resultado = evaluarRegistro(context)

        return """
            ══════════════════════════════════════════
            REPORTE DE EVALUACIÓN DE REGISTRO
            ══════════════════════════════════════════

            Usuario: ${context.usuario.displayName()}
            ID: ${context.usuario.id}
            Rol: ${if (context.usuario.isAdmin()) "ADMINISTRADOR" else "USUARIO"}

            ────────────────────────────────────────
            VALIDACIONES
            ────────────────────────────────────────

            ✓ Usuario habilitado: ${if (usuarioHabilitado(context)) "SÍ" else "NO"}
            ✓ Horario válido: ${if (horarioValido(context)) "SÍ (${context.hora}:00)" else "NO (${context.hora}:00)"}
            ✓ Ubicación válida: ${if (ubicacionValida(context)) "SÍ" else "NO"}
            ✓ Mayor de edad: ${if (esMayorDeEdad(context)) "SÍ (${context.usuario.edad} años)" else "NO (${context.usuario.edad} años)"}
            ✓ No es admin: ${if (noEsAdmin(context)) "SÍ" else "NO"}

            ────────────────────────────────────────
            RESULTADO
            ────────────────────────────────────────

            Estado: ${if (resultado.permitido) "✓ PERMITIDO" else "✗ DENEGADO"}
            Mensaje: ${resultado.mensaje}
            Razón: ${resultado.razon}

            ══════════════════════════════════════════
        """.trimIndent()
    }
}
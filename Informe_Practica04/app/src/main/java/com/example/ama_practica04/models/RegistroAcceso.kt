package com.example.ama_practica04.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class que representa un registro de acceso al sistema
 * Contiene información del usuario, la acción realizada, ubicación y timestamp
 */
data class RegistroAcceso(
    val usuario: Usuario,
    val accion: AccionAsistencia,
    val ubicacion: Ubicacion,
    val marcaTiempo: Long = System.currentTimeMillis()
) {
    /**
     * Función para obtener la fecha formateada
     */
    fun obtenerFechaFormateada(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(marcaTiempo))
    }

    /**
     * Función para verificar si el acceso es válido
     */
    fun esAccesoValido(): Boolean {
        return usuario.puedeAcceder() && ubicacion.estaDentroDelRango()
    }

    /**
     * Función para obtener el estado del registro
     */
    fun obtenerEstado(): String {
        return if (esAccesoValido()) "VÁLIDO" else "INVÁLIDO"
    }
}

/**
 * Extension function para formatear el registro completo
 */
fun RegistroAcceso.formatear(): String {
    val estado = if (esAccesoValido()) "✓" else "✗"
    return """
        $estado REGISTRO DE ACCESO
        ────────────────────────
        Usuario: ${usuario.nombreConRol()}
        Acción: ${accion.formatear()}
        Ubicación: ${ubicacion.formatear()}
        Fecha: ${obtenerFechaFormateada()}
        Estado: ${obtenerEstado()}
    """.trimIndent()
}

/**
 * Extension function para obtener un resumen corto
 */
fun RegistroAcceso.resumen(): String {
    val accionTexto = accion.obtenerDescripcion()
    return "${usuario.nombre} - $accionTexto - ${obtenerFechaFormateada()}"
}
package com.example.ama_practica04.models

/**
 * Enum class para las acciones de asistencia
 * Representa si es una entrada o salida del sistema
 */
enum class AccionAsistencia {
    ENTRADA,
    SALIDA;

    /**
     * Función para obtener el texto descriptivo de la acción
     */
    fun obtenerDescripcion(): String {
        return when (this) {
            ENTRADA -> "Registro de Entrada"
            SALIDA -> "Registro de Salida"
        }
    }

    /**
     * Función para obtener el emoji representativo
     */
    fun obtenerEmoji(): String {
        return when (this) {
            ENTRADA -> "→"
            SALIDA -> "←"
        }
    }
}

/**
 * Extension function para formatear la acción
 */
fun AccionAsistencia.formatear(): String {
    return "${obtenerEmoji()} ${obtenerDescripcion()}"
}

/**
 * Extension function para verificar si es entrada
 */
fun AccionAsistencia.esEntrada(): Boolean = this == AccionAsistencia.ENTRADA

/**
 * Extension function para verificar si es salida
 */
fun AccionAsistencia.esSalida(): Boolean = this == AccionAsistencia.SALIDA
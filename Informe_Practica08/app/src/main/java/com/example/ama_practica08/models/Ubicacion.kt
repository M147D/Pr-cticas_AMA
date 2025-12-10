package com.example.ama_practica08.models

/**
 * Sealed class para representar la ubicación
 * Puede ser "dentro del rango" o "fuera del rango"
 */
sealed class Ubicacion {
    /**
     * Usuario dentro del rango permitido
     */
    data class DentroDelRango(val descripcion: String = "Ubicación válida") : Ubicacion()

    /**
     * Usuario fuera del rango permitido
     */
    data class FueraDelRango(val razon: String = "Fuera del área permitida") : Ubicacion()

    /**
     * Función para verificar si está dentro del rango
     */
    fun estaDentroDelRango(): Boolean {
        return this is DentroDelRango
    }

    /**
     * Función para obtener descripción de la ubicación
     */
    fun obtenerDescripcion(): String {
        return when (this) {
            is DentroDelRango -> descripcion
            is FueraDelRango -> razon
        }
    }
}

/**
 * Extension function para formatear la ubicación
 */
fun Ubicacion.formatear(): String {
    return when (this) {
        is Ubicacion.DentroDelRango -> "✓ Dentro del rango: $descripcion"
        is Ubicacion.FueraDelRango -> "✗ Fuera del rango: $razon"
    }
}
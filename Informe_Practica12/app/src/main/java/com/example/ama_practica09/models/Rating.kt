package com.example.ama_practica09.models

import java.util.Date

/**
 * Modelo de datos para representar una calificación
 */
data class Rating(
    val id: String = "",
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val servicioId: String? = null,
    val servicioNombre: String = "Sistema de Asistencia",
    val puntuacion: Float = 0f,
    val comentario: String = "",
    val fecha: Date = Date(),
    val categoria: RatingCategory = RatingCategory.GENERAL
)

/**
 * Categorías de calificación
 */
enum class RatingCategory(val displayName: String) {
    GENERAL("General"),
    USABILIDAD("Usabilidad"),
    FUNCIONALIDAD("Funcionalidad"),
    RENDIMIENTO("Rendimiento"),
    DISENO("Diseño")
}

/**
 * Estadísticas de calificaciones
 */
data class RatingStats(
    val promedioGeneral: Float = 0f,
    val totalCalificaciones: Int = 0,
    val distribucion: Map<Int, Int> = mapOf(
        1 to 0,
        2 to 0,
        3 to 0,
        4 to 0,
        5 to 0
    ),
    val porCategoria: Map<RatingCategory, Float> = emptyMap()
)

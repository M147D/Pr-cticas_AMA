package com.example.ama_practica09.animations

/**
 * Configuración centralizada de animaciones
 * Define constantes para duraciones, escalas y otros valores de animación
 */
object AnimationConfig {
    // Duraciones de animación (en milisegundos)
    const val DURATION_SHORT = 300
    const val DURATION_MEDIUM = 600
    const val DURATION_LONG = 1000

    // Escalas de animación
    const val SCALE_START = 0f
    const val SCALE_NORMAL = 1f
    const val SCALE_BOUNCE = 1.2f
    const val SCALE_LARGE = 1.5f

    // Rotación
    const val ROTATION_FULL = 360f
    const val ROTATION_HALF = 180f

    // Alpha (transparencia)
    const val ALPHA_TRANSPARENT = 0f
    const val ALPHA_OPAQUE = 1f
    const val ALPHA_SEMI = 0.5f
}

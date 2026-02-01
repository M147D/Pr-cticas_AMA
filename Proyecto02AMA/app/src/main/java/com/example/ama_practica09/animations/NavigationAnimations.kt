package com.example.ama_practica09.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

/**
 * Configuraciones de animación para navegación entre pantallas
 *
 * Define transiciones específicas para diferentes tipos de navegación
 * y proporciona un sistema de animación consistente para toda la app
 */
object NavigationAnimations {

    // Duración de las animaciones de navegación
    private const val NAVIGATION_DURATION = 350

    /**
     * Tipos de transición disponibles
     */
    enum class TransitionType {
        SLIDE_HORIZONTAL,      // Deslizar horizontalmente (por defecto)
        SLIDE_VERTICAL,        // Deslizar verticalmente
        FADE,                  // Fade in/out
        SCALE_FADE,           // Escala + fade
        EXPAND_SHRINK,        // Expandir/contraer
        NONE                  // Sin animación
    }

    /**
     * Obtiene el tipo de transición recomendado para una navegación específica
     */
    fun getTransitionType(fromScreen: String, toScreen: String): TransitionType {
        return when {
            // Welcome -> Login: Slide horizontal
            fromScreen == "welcome" && toScreen == "login" -> TransitionType.SLIDE_HORIZONTAL
            fromScreen == "login" && toScreen == "welcome" -> TransitionType.SLIDE_HORIZONTAL

            // Login -> Home: Scale + Fade (transición importante)
            fromScreen == "login" && toScreen == "home" -> TransitionType.SCALE_FADE
            fromScreen == "register" && toScreen == "home" -> TransitionType.SCALE_FADE

            // Home -> Pantallas secundarias: Slide horizontal
            fromScreen == "home" && toScreen in listOf("user", "admin", "rating", "charts", "geolocation", "animations") ->
                TransitionType.SLIDE_HORIZONTAL

            // Volver a Home: Slide horizontal inverso
            toScreen == "home" -> TransitionType.SLIDE_HORIZONTAL

            // Pantallas modales: Slide vertical
            toScreen in listOf("googleSignIn") -> TransitionType.SLIDE_VERTICAL

            // Por defecto: Fade
            else -> TransitionType.FADE
        }
    }

    /**
     * Genera la especificación de entrada para AnimatedContent
     */
    fun getEnterTransition(transitionType: TransitionType, isForward: Boolean = true): EnterTransition {
        return when (transitionType) {
            TransitionType.SLIDE_HORIZONTAL -> {
                if (isForward) {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(NAVIGATION_DURATION)
                    ) + fadeIn(animationSpec = tween(NAVIGATION_DURATION))
                } else {
                    slideInHorizontally(
                        initialOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(NAVIGATION_DURATION)
                    ) + fadeIn(animationSpec = tween(NAVIGATION_DURATION))
                }
            }

            TransitionType.SLIDE_VERTICAL -> {
                slideInVertically(
                    initialOffsetY = { fullHeight -> if (isForward) fullHeight else -fullHeight },
                    animationSpec = tween(NAVIGATION_DURATION)
                ) + fadeIn(animationSpec = tween(NAVIGATION_DURATION))
            }

            TransitionType.FADE -> {
                fadeIn(animationSpec = tween(NAVIGATION_DURATION))
            }

            TransitionType.SCALE_FADE -> {
                scaleIn(
                    initialScale = 0.85f,
                    animationSpec = tween(NAVIGATION_DURATION)
                ) + fadeIn(animationSpec = tween(NAVIGATION_DURATION))
            }

            TransitionType.EXPAND_SHRINK -> {
                expandIn(
                    expandFrom = Alignment.Center,
                    animationSpec = tween(NAVIGATION_DURATION)
                ) + fadeIn(animationSpec = tween(NAVIGATION_DURATION))
            }

            TransitionType.NONE -> EnterTransition.None
        }
    }

    /**
     * Genera la especificación de salida para AnimatedContent
     */
    fun getExitTransition(transitionType: TransitionType, isForward: Boolean = true): ExitTransition {
        return when (transitionType) {
            TransitionType.SLIDE_HORIZONTAL -> {
                if (isForward) {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> -fullWidth },
                        animationSpec = tween(NAVIGATION_DURATION)
                    ) + fadeOut(animationSpec = tween(NAVIGATION_DURATION))
                } else {
                    slideOutHorizontally(
                        targetOffsetX = { fullWidth -> fullWidth },
                        animationSpec = tween(NAVIGATION_DURATION)
                    ) + fadeOut(animationSpec = tween(NAVIGATION_DURATION))
                }
            }

            TransitionType.SLIDE_VERTICAL -> {
                slideOutVertically(
                    targetOffsetY = { fullHeight -> if (isForward) -fullHeight else fullHeight },
                    animationSpec = tween(NAVIGATION_DURATION)
                ) + fadeOut(animationSpec = tween(NAVIGATION_DURATION))
            }

            TransitionType.FADE -> {
                fadeOut(animationSpec = tween(NAVIGATION_DURATION))
            }

            TransitionType.SCALE_FADE -> {
                scaleOut(
                    targetScale = 1.15f,
                    animationSpec = tween(NAVIGATION_DURATION)
                ) + fadeOut(animationSpec = tween(NAVIGATION_DURATION))
            }

            TransitionType.EXPAND_SHRINK -> {
                shrinkOut(
                    shrinkTowards = Alignment.Center,
                    animationSpec = tween(NAVIGATION_DURATION)
                ) + fadeOut(animationSpec = tween(NAVIGATION_DURATION))
            }

            TransitionType.NONE -> ExitTransition.None
        }
    }

    /**
     * Determina si la navegación es "hacia adelante" o "hacia atrás"
     * basándose en el orden lógico de las pantallas
     */
    fun isForwardNavigation(fromScreen: String, toScreen: String): Boolean {
        val screenOrder = listOf(
            "welcome",
            "menu",
            "login",
            "register",
            "googleSignIn",
            "home",
            "user",
            "admin",
            "rating",
            "charts",
            "geolocation",
            "animations",
            "practica02"
        )

        val fromIndex = screenOrder.indexOf(fromScreen)
        val toIndex = screenOrder.indexOf(toScreen)

        return if (fromIndex == -1 || toIndex == -1) true else toIndex >= fromIndex
    }
}

/**
 * Data class para mantener el estado de navegación con animación
 */
data class NavigationState(
    val currentScreen: String,
    val previousScreen: String = "",
    val transitionType: NavigationAnimations.TransitionType = NavigationAnimations.TransitionType.FADE,
    val isForward: Boolean = true
)

/**
 * Función helper para crear ContentTransform basado en el tipo de transición
 */
fun createContentTransform(
    transitionType: NavigationAnimations.TransitionType,
    isForward: Boolean
): ContentTransform {
    return ContentTransform(
        targetContentEnter = NavigationAnimations.getEnterTransition(transitionType, isForward),
        initialContentExit = NavigationAnimations.getExitTransition(transitionType, isForward),
        sizeTransform = null
    )
}

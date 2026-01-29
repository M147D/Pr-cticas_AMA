package com.example.ama_practica09.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.tween

/**
 * Configuraciones de transiciones entre pantallas
 * Define transiciones reutilizables para Fade, Slide y Scale
 */
object ScreenTransitions {

    // ======= FADE TRANSITIONS =======

    /**
     * Transición de entrada con fade in
     */
    val fadeInTransition: EnterTransition = fadeIn(
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    /**
     * Transición de salida con fade out
     */
    val fadeOutTransition: ExitTransition = fadeOut(
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    // ======= SLIDE TRANSITIONS =======

    /**
     * Transición de entrada deslizando desde la derecha
     */
    val slideInFromRightTransition: EnterTransition = slideInHorizontally(
        initialOffsetX = { it },  // Comienza fuera de la pantalla a la derecha
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    /**
     * Transición de entrada deslizando desde la izquierda
     */
    val slideInFromLeftTransition: EnterTransition = slideInHorizontally(
        initialOffsetX = { -it },  // Comienza fuera de la pantalla a la izquierda
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    /**
     * Transición de salida deslizando hacia la izquierda
     */
    val slideOutToLeftTransition: ExitTransition = slideOutHorizontally(
        targetOffsetX = { -it },  // Sale hacia la izquierda
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    /**
     * Transición de salida deslizando hacia la derecha
     */
    val slideOutToRightTransition: ExitTransition = slideOutHorizontally(
        targetOffsetX = { it },  // Sale hacia la derecha
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    /**
     * Transición de entrada deslizando desde arriba
     */
    val slideInFromTopTransition: EnterTransition = slideInVertically(
        initialOffsetY = { -it },  // Comienza fuera de la pantalla arriba
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    /**
     * Transición de entrada deslizando desde abajo
     */
    val slideInFromBottomTransition: EnterTransition = slideInVertically(
        initialOffsetY = { it },  // Comienza fuera de la pantalla abajo
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    // ======= SCALE TRANSITIONS =======

    /**
     * Transición de entrada con scale in (aumentando tamaño)
     */
    val scaleInTransition: EnterTransition = scaleIn(
        initialScale = 0.8f,
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    /**
     * Transición de salida con scale out (disminuyendo tamaño)
     */
    val scaleOutTransition: ExitTransition = scaleOut(
        targetScale = 0.8f,
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    /**
     * Transición de entrada con scale desde cero
     */
    val scaleInFromZeroTransition: EnterTransition = scaleIn(
        initialScale = AnimationConfig.SCALE_START,
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    // ======= COMBINED TRANSITIONS =======

    /**
     * Transición combinada: Fade + Scale In
     */
    val fadeScaleInTransition: EnterTransition = fadeInTransition + scaleInTransition

    /**
     * Transición combinada: Fade + Scale Out
     */
    val fadeScaleOutTransition: ExitTransition = fadeOutTransition + scaleOutTransition

    /**
     * Transición combinada: Fade + Slide desde derecha
     */
    val fadeSlideInFromRightTransition: EnterTransition = fadeInTransition + slideInFromRightTransition

    /**
     * Transición combinada: Fade + Slide hacia izquierda
     */
    val fadeSlideOutToLeftTransition: ExitTransition = fadeOutTransition + slideOutToLeftTransition

    // ======= EXPAND/COLLAPSE TRANSITIONS =======

    /**
     * Transición de expansión vertical
     */
    val expandVerticallyTransition: EnterTransition = expandVertically(
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    /**
     * Transición de colapso vertical
     */
    val shrinkVerticallyTransition: ExitTransition = shrinkVertically(
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    /**
     * Transición de expansión horizontal
     */
    val expandHorizontallyTransition: EnterTransition = expandHorizontally(
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )

    /**
     * Transición de colapso horizontal
     */
    val shrinkHorizontallyTransition: ExitTransition = shrinkHorizontally(
        animationSpec = tween(AnimationConfig.DURATION_MEDIUM)
    )
}

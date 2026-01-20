package com.example.ama_practica09.animations

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Utilidades de animación para Jetpack Compose
 * Funciones reutilizables para animaciones comunes
 */

/**
 * Animación de escala infinita que oscila entre dos valores
 */
@Composable
fun rememberScaleAnimation(
    initialScale: Float = AnimationConfig.SCALE_NORMAL,
    targetScale: Float = AnimationConfig.SCALE_BOUNCE,
    durationMillis: Int = AnimationConfig.DURATION_MEDIUM,
    easing: Easing = EaseInOut
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "scale_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = initialScale,
        targetValue = targetScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = easing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    return scale
}

/**
 * Animación de rotación infinita
 */
@Composable
fun rememberRotationAnimation(
    durationMillis: Int = AnimationConfig.DURATION_LONG * 2,
    easing: Easing = LinearEasing
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation_animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = AnimationConfig.ROTATION_FULL,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = easing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    return rotation
}

/**
 * Animación de fade in que se ejecuta al montar el composable
 */
@Composable
fun rememberFadeInAnimation(
    durationMillis: Int = AnimationConfig.DURATION_MEDIUM
): Float {
    var targetAlpha by remember { mutableFloatStateOf(AnimationConfig.ALPHA_TRANSPARENT) }

    val alpha by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(durationMillis),
        label = "fadeIn"
    )

    LaunchedEffect(Unit) {
        targetAlpha = AnimationConfig.ALPHA_OPAQUE
    }

    return alpha
}

/**
 * Animación de pulso de éxito
 * Aumenta y luego reduce la escala del elemento
 */
@Composable
fun rememberSuccessPulse(
    isActive: Boolean,
    targetScale: Float = AnimationConfig.SCALE_BOUNCE,
    durationMillis: Int = AnimationConfig.DURATION_SHORT
): Float {
    val scale by animateFloatAsState(
        targetValue = if (isActive) targetScale else AnimationConfig.SCALE_NORMAL,
        animationSpec = tween(durationMillis, easing = EaseInOutBack),
        label = "successPulse"
    )
    return scale
}

/**
 * Modifier que aplica una animación de pulso de éxito
 */
fun Modifier.successPulseAnimation(isActive: Boolean): Modifier = this.then(
    Modifier.graphicsLayer {
        val scale = if (isActive) AnimationConfig.SCALE_BOUNCE else AnimationConfig.SCALE_NORMAL
        scaleX = scale
        scaleY = scale
    }
)

/**
 * Animación de rebote (bounce effect)
 */
@Composable
fun rememberBounceAnimation(
    isActive: Boolean,
    bounceScale: Float = AnimationConfig.SCALE_BOUNCE,
    durationMillis: Int = AnimationConfig.DURATION_MEDIUM
): Float {
    val scale by animateFloatAsState(
        targetValue = if (isActive) bounceScale else AnimationConfig.SCALE_NORMAL,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounce"
    )
    return scale
}

/**
 * Animación de shake (sacudida horizontal)
 */
@Composable
fun rememberShakeAnimation(
    isActive: Boolean,
    shakeDistance: Float = 10f,
    durationMillis: Int = AnimationConfig.DURATION_SHORT
): Float {
    var animationCycle by remember { mutableIntStateOf(0) }

    LaunchedEffect(isActive) {
        if (isActive) {
            animationCycle++
        }
    }

    val offset by animateFloatAsState(
        targetValue = if (animationCycle % 2 == 0) 0f else shakeDistance,
        animationSpec = tween(durationMillis / 2, easing = EaseInOut),
        label = "shake"
    )

    return offset
}

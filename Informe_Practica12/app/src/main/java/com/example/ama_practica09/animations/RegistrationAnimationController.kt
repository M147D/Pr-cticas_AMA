package com.example.ama_practica09.animations

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.delay

/**
 * Controlador de la secuencia de animación para el registro de asistencia
 *
 * Gestiona los estados de la animación que ocurre cuando se registra exitosamente una asistencia:
 * 1. ButtonPulse: El botón aumenta de tamaño momentáneamente
 * 2. SuccessIcon: Aparece el icono de confirmación (✓)
 * 3. MessageFadeIn: El mensaje de éxito hace fade in
 * 4. Complete: La animación finaliza
 */
class RegistrationAnimationController {

    private val _animationState = mutableStateOf<AnimationState>(AnimationState.Idle)
    val animationState: State<AnimationState> = _animationState

    /**
     * Ejecuta la secuencia completa de animación de éxito
     * Esta función es suspendible para poder usar delays entre estados
     */
    suspend fun triggerSuccessSequence() {
        // Paso 1: Pulso del botón
        _animationState.value = AnimationState.ButtonPulse
        delay(300)

        // Paso 2: Icono de éxito aparece
        _animationState.value = AnimationState.SuccessIcon
        delay(500)

        // Paso 3: Mensaje hace fade in
        _animationState.value = AnimationState.MessageFadeIn
        delay(800)

        // Paso 4: Completado
        _animationState.value = AnimationState.Complete
    }

    /**
     * Ejecuta una secuencia de animación de error
     */
    suspend fun triggerErrorSequence() {
        // Shake del botón para indicar error
        _animationState.value = AnimationState.ButtonShake
        delay(400)

        // Icono de error aparece
        _animationState.value = AnimationState.ErrorIcon
        delay(600)

        // Vuelve a idle
        _animationState.value = AnimationState.Idle
    }

    /**
     * Resetea el controlador al estado inicial
     */
    fun reset() {
        _animationState.value = AnimationState.Idle
    }

    /**
     * Verifica si hay una animación en progreso
     */
    fun isAnimating(): Boolean {
        return _animationState.value != AnimationState.Idle && _animationState.value != AnimationState.Complete
    }

    /**
     * Estados posibles de la animación
     */
    sealed class AnimationState {
        // Estado inactivo (sin animación)
        data object Idle : AnimationState()

        // Secuencia de éxito
        data object ButtonPulse : AnimationState()
        data object SuccessIcon : AnimationState()
        data object MessageFadeIn : AnimationState()
        data object Complete : AnimationState()

        // Secuencia de error
        data object ButtonShake : AnimationState()
        data object ErrorIcon : AnimationState()
    }
}

package com.example.ama_practica09.animations

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator

/**
 * Utilidades de animación para Android Views tradicionales
 * Usa ObjectAnimator, AnimatorSet y ViewPropertyAnimator
 */
object ViewAnimations {

    /**
     * Animación de escala (scale) de una View
     */
    fun scaleAnimation(
        view: View,
        fromScale: Float = AnimationConfig.SCALE_START,
        toScale: Float = AnimationConfig.SCALE_NORMAL,
        duration: Long = AnimationConfig.DURATION_MEDIUM.toLong()
    ): AnimatorSet {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", fromScale, toScale).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }

        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", fromScale, toScale).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }

        return AnimatorSet().apply {
            playTogether(scaleX, scaleY)
        }
    }

    /**
     * Animación de rotación de una View
     */
    fun rotateAnimation(
        view: View,
        fromDegrees: Float = 0f,
        toDegrees: Float = AnimationConfig.ROTATION_FULL,
        duration: Long = AnimationConfig.DURATION_LONG.toLong()
    ): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "rotation", fromDegrees, toDegrees).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
        }
    }

    /**
     * Animación de fade in (aparición gradual)
     */
    fun fadeInAnimation(
        view: View,
        fromAlpha: Float = AnimationConfig.ALPHA_TRANSPARENT,
        toAlpha: Float = AnimationConfig.ALPHA_OPAQUE,
        duration: Long = AnimationConfig.DURATION_MEDIUM.toLong()
    ): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "alpha", fromAlpha, toAlpha).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    /**
     * Animación de fade out (desaparición gradual)
     */
    fun fadeOutAnimation(
        view: View,
        fromAlpha: Float = AnimationConfig.ALPHA_OPAQUE,
        toAlpha: Float = AnimationConfig.ALPHA_TRANSPARENT,
        duration: Long = AnimationConfig.DURATION_MEDIUM.toLong()
    ): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "alpha", fromAlpha, toAlpha).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    /**
     * Animación de traslación horizontal
     */
    fun slideHorizontalAnimation(
        view: View,
        fromX: Float,
        toX: Float,
        duration: Long = AnimationConfig.DURATION_MEDIUM.toLong()
    ): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "translationX", fromX, toX).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    /**
     * Animación de traslación vertical
     */
    fun slideVerticalAnimation(
        view: View,
        fromY: Float,
        toY: Float,
        duration: Long = AnimationConfig.DURATION_MEDIUM.toLong()
    ): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "translationY", fromY, toY).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    /**
     * Animación de rebote (bounce)
     */
    fun bounceAnimation(
        view: View,
        fromScale: Float = AnimationConfig.SCALE_START,
        toScale: Float = AnimationConfig.SCALE_NORMAL,
        duration: Long = AnimationConfig.DURATION_LONG.toLong()
    ): AnimatorSet {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", fromScale, toScale).apply {
            this.duration = duration
            interpolator = BounceInterpolator()
        }

        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", fromScale, toScale).apply {
            this.duration = duration
            interpolator = BounceInterpolator()
        }

        return AnimatorSet().apply {
            playTogether(scaleX, scaleY)
        }
    }

    /**
     * Animación de pulso (aumenta y luego vuelve al tamaño normal)
     */
    fun pulseAnimation(
        view: View,
        targetScale: Float = AnimationConfig.SCALE_BOUNCE,
        duration: Long = AnimationConfig.DURATION_SHORT.toLong()
    ): AnimatorSet {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", AnimationConfig.SCALE_NORMAL, targetScale)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", AnimationConfig.SCALE_NORMAL, targetScale)

        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", targetScale, AnimationConfig.SCALE_NORMAL)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", targetScale, AnimationConfig.SCALE_NORMAL)

        val scaleUp = AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY)
            this.duration = duration
            interpolator = OvershootInterpolator()
        }

        val scaleDown = AnimatorSet().apply {
            playTogether(scaleDownX, scaleDownY)
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
        }

        return AnimatorSet().apply {
            playSequentially(scaleUp, scaleDown)
        }
    }

    /**
     * Ejecuta múltiples animaciones secuencialmente
     */
    fun sequentialAnimation(vararg animators: ObjectAnimator): AnimatorSet {
        return AnimatorSet().apply {
            playSequentially(*animators)
        }
    }

    /**
     * Ejecuta múltiples animaciones en paralelo
     */
    fun parallelAnimation(vararg animators: ObjectAnimator): AnimatorSet {
        return AnimatorSet().apply {
            playTogether(*animators)
        }
    }

    /**
     * Animación usando ViewPropertyAnimator (API fluida)
     */
    fun viewPropertyAnimation(
        view: View,
        scale: Float = AnimationConfig.SCALE_BOUNCE,
        alpha: Float = AnimationConfig.ALPHA_OPAQUE,
        rotation: Float = 0f,
        duration: Long = AnimationConfig.DURATION_MEDIUM.toLong()
    ) {
        view.animate()
            .scaleX(scale)
            .scaleY(scale)
            .alpha(alpha)
            .rotation(rotation)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    /**
     * Animación de shake (sacudida)
     */
    fun shakeAnimation(
        view: View,
        shakeDistance: Float = 25f,
        duration: Long = AnimationConfig.DURATION_SHORT.toLong()
    ): AnimatorSet {
        val shake1 = ObjectAnimator.ofFloat(view, "translationX", 0f, shakeDistance)
        val shake2 = ObjectAnimator.ofFloat(view, "translationX", shakeDistance, -shakeDistance)
        val shake3 = ObjectAnimator.ofFloat(view, "translationX", -shakeDistance, shakeDistance)
        val shake4 = ObjectAnimator.ofFloat(view, "translationX", shakeDistance, 0f)

        return AnimatorSet().apply {
            playSequentially(shake1, shake2, shake3, shake4)
            this.duration = duration / 4
        }
    }

    /**
     * Animación infinita de rotación
     */
    fun infiniteRotationAnimation(
        view: View,
        duration: Long = AnimationConfig.DURATION_LONG.toLong() * 2
    ): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "rotation", 0f, AnimationConfig.ROTATION_FULL).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
        }
    }

    /**
     * Animación infinita de pulso
     */
    fun infinitePulseAnimation(
        view: View,
        minScale: Float = AnimationConfig.SCALE_NORMAL,
        maxScale: Float = AnimationConfig.SCALE_BOUNCE,
        duration: Long = AnimationConfig.DURATION_MEDIUM.toLong()
    ): AnimatorSet {
        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", minScale, maxScale).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", minScale, maxScale).apply {
            this.duration = duration
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
        }

        return AnimatorSet().apply {
            playTogether(scaleUpX, scaleUpY)
        }
    }
}

package com.lenaralabs.cardsreminder.ui.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition

object AppMotion {
    const val BASE_DURATION_MS = 450
    const val NAV_DURATION_MS = 350
    const val OVERLAY_DURATION_MS = 250
    const val STAGGER_STEP_MS = 50

    const val STANDARD_INITIAL_SCALE = 0.88f
    const val SECTION_INITIAL_SCALE = 0.98f
    const val CARD_INITIAL_SCALE = 0.95f
    const val EVENT_INITIAL_SCALE = 0.94f
    const val ICON_INITIAL_SCALE = 0.6f

    fun staggerDelay(index: Int): Int = index * STAGGER_STEP_MS

    val gentleSpring = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessMediumLow,
    )

    val splashSpring = spring<Float>(
        dampingRatio = 0.75f,
        stiffness = Spring.StiffnessMediumLow,
    )

    val signInSpring = spring<Float>(
        dampingRatio = 0.82f,
        stiffness = Spring.StiffnessLow,
    )

    fun navFadeIn(): EnterTransition = fadeIn(
        animationSpec = tween(NAV_DURATION_MS, easing = FastOutSlowInEasing),
    )

    fun navFadeOut(): ExitTransition = fadeOut(
        animationSpec = tween(NAV_DURATION_MS, easing = FastOutSlowInEasing),
    )

    fun standardEnter(index: Int = 0): EnterTransition {
        val delay = staggerDelay(index)
        return fadeIn(tween(BASE_DURATION_MS, delay, FastOutSlowInEasing)) +
            scaleIn(
                initialScale = STANDARD_INITIAL_SCALE,
                animationSpec = tween(BASE_DURATION_MS, delay, FastOutSlowInEasing),
            )
    }

    fun standardExit(): ExitTransition =
        fadeOut(tween(OVERLAY_DURATION_MS, easing = FastOutSlowInEasing)) +
            scaleOut(
                targetScale = STANDARD_INITIAL_SCALE,
                animationSpec = tween(OVERLAY_DURATION_MS, easing = FastOutSlowInEasing),
            )

    fun sectionEnter(index: Int = 0): EnterTransition {
        val delay = staggerDelay(index)
        return fadeIn(tween(BASE_DURATION_MS, delay, FastOutSlowInEasing)) +
            scaleIn(
                initialScale = SECTION_INITIAL_SCALE,
                animationSpec = tween(BASE_DURATION_MS, delay, FastOutSlowInEasing),
            ) +
            slideInVertically(
                animationSpec = tween(BASE_DURATION_MS, delay, FastOutSlowInEasing),
            ) { it / 8 }
    }

    fun cardEnter(index: Int = 0): EnterTransition {
        val delay = staggerDelay(index)
        return fadeIn(tween(BASE_DURATION_MS, delay, FastOutSlowInEasing)) +
            scaleIn(
                initialScale = CARD_INITIAL_SCALE,
                animationSpec = tween(BASE_DURATION_MS, delay, FastOutSlowInEasing),
            ) +
            slideInVertically(
                animationSpec = tween(BASE_DURATION_MS, delay, FastOutSlowInEasing),
            ) { it / 6 }
    }

    fun eventEnter(index: Int = 0): EnterTransition {
        val delay = staggerDelay(index)
        return fadeIn(tween(BASE_DURATION_MS, delay, FastOutSlowInEasing)) +
            scaleIn(
                initialScale = EVENT_INITIAL_SCALE,
                animationSpec = tween(BASE_DURATION_MS, delay, FastOutSlowInEasing),
            ) +
            slideInHorizontally(
                animationSpec = tween(BASE_DURATION_MS, delay, FastOutSlowInEasing),
            ) { it / 4 }
    }

    fun fromBottomEnter(index: Int = 0): EnterTransition {
        val delay = staggerDelay(index)
        return fadeIn(tween(BASE_DURATION_MS, delay, FastOutSlowInEasing)) +
            slideInVertically(
                animationSpec = tween(BASE_DURATION_MS, delay, FastOutSlowInEasing),
            ) { it / 3 }
    }

    fun slideOutEnd(): ExitTransition =
        fadeOut(tween(OVERLAY_DURATION_MS, easing = FastOutSlowInEasing)) +
            scaleOut(
                targetScale = STANDARD_INITIAL_SCALE,
                animationSpec = tween(OVERLAY_DURATION_MS, easing = FastOutSlowInEasing),
            ) +
            slideOutHorizontally(
                animationSpec = tween(OVERLAY_DURATION_MS, easing = FastOutSlowInEasing),
            ) { it / 3 }
}

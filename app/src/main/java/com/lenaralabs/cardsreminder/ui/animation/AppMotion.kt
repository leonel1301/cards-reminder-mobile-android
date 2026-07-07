package com.lenaralabs.cardsreminder.ui.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset

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

    val expressiveFastEffectsSpring = spring<Float>(
        dampingRatio = 1.0f,
        stiffness = 3800f,
    )

    val expressiveFastSpatialSpring = spring<Float>(
        dampingRatio = 0.6f,
        stiffness = 800f,
    )

    val expressiveSlowSpatialSpring = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = 200f,
    )

    @Composable
    fun spatialSpec(): FiniteAnimationSpec<Float> = MaterialTheme.motionScheme.fastSpatialSpec()

    @Composable
    fun spatialOffsetSpec(): FiniteAnimationSpec<IntOffset> = MaterialTheme.motionScheme.fastSpatialSpec()

    @Composable
    fun effectsSpec(): FiniteAnimationSpec<Float> = MaterialTheme.motionScheme.fastEffectsSpec()

    @Composable
    fun slowSpatialSpec(): FiniteAnimationSpec<Float> = MaterialTheme.motionScheme.slowSpatialSpec()

    fun navFadeIn(): EnterTransition = fadeIn(animationSpec = expressiveFastEffectsSpring)

    fun navFadeOut(): ExitTransition = fadeOut(animationSpec = expressiveFastEffectsSpring)

    @Composable
    fun standardEnter(index: Int = 0): EnterTransition {
        val delay = staggerDelay(index)
        return fadeIn(tween(delayMillis = delay, durationMillis = NAV_DURATION_MS)) +
            scaleIn(
                initialScale = STANDARD_INITIAL_SCALE,
                animationSpec = spatialSpec(),
            )
    }

    @Composable
    fun standardExit(): ExitTransition =
        fadeOut(animationSpec = effectsSpec()) +
            scaleOut(
                targetScale = STANDARD_INITIAL_SCALE,
                animationSpec = spatialSpec(),
            )

    @Composable
    fun sectionEnter(index: Int = 0): EnterTransition {
        val delay = staggerDelay(index)
        return fadeIn(tween(delayMillis = delay, durationMillis = NAV_DURATION_MS)) +
            scaleIn(
                initialScale = SECTION_INITIAL_SCALE,
                animationSpec = spatialSpec(),
            ) +
            slideInVertically(animationSpec = spatialOffsetSpec()) { it / 8 }
    }

    @Composable
    fun cardEnter(index: Int = 0): EnterTransition {
        val delay = staggerDelay(index)
        return fadeIn(tween(delayMillis = delay, durationMillis = NAV_DURATION_MS)) +
            scaleIn(
                initialScale = CARD_INITIAL_SCALE,
                animationSpec = spatialSpec(),
            ) +
            slideInVertically(animationSpec = spatialOffsetSpec()) { it / 6 }
    }

    @Composable
    fun eventEnter(index: Int = 0): EnterTransition {
        val delay = staggerDelay(index)
        return fadeIn(tween(delayMillis = delay, durationMillis = NAV_DURATION_MS)) +
            scaleIn(
                initialScale = EVENT_INITIAL_SCALE,
                animationSpec = spatialSpec(),
            ) +
            slideInHorizontally(animationSpec = spatialOffsetSpec()) { it / 4 }
    }

    @Composable
    fun fromBottomEnter(index: Int = 0): EnterTransition {
        val delay = staggerDelay(index)
        return fadeIn(tween(delayMillis = delay, durationMillis = NAV_DURATION_MS)) +
            slideInVertically(animationSpec = spatialOffsetSpec()) { it / 3 }
    }

    @Composable
    fun slideOutEnd(): ExitTransition =
        fadeOut(animationSpec = effectsSpec()) +
            scaleOut(
                targetScale = STANDARD_INITIAL_SCALE,
                animationSpec = spatialSpec(),
            ) +
            slideOutHorizontally(animationSpec = spatialOffsetSpec()) { it / 3 }
}

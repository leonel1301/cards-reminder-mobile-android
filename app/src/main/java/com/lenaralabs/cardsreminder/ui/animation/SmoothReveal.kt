package com.lenaralabs.cardsreminder.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class RevealStyle {
    Standard,
    Section,
    Card,
    Event,
    FromBottom,
}

@Composable
fun SmoothReveal(
    visible: Boolean,
    modifier: Modifier = Modifier,
    index: Int = 0,
    style: RevealStyle = RevealStyle.Standard,
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    val (enter, exit) = when (style) {
        RevealStyle.Standard -> AppMotion.standardEnter(index) to AppMotion.standardExit()
        RevealStyle.Section -> AppMotion.sectionEnter(index) to AppMotion.standardExit()
        RevealStyle.Card -> AppMotion.cardEnter(index) to AppMotion.standardExit()
        RevealStyle.Event -> AppMotion.eventEnter(index) to AppMotion.standardExit()
        RevealStyle.FromBottom -> AppMotion.fromBottomEnter(index) to AppMotion.standardExit()
    }

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = enter,
        exit = exit,
        content = content,
    )
}

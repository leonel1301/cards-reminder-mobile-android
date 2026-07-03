package com.lenaralabs.cardsreminder.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CardsReminderColors(
    val appBackground: Color,
    val authBackground: Color,
    val primaryAction: Color,
    val addActionButton: Color,
    val onAddActionButton: Color,
    val cardSurface: Color,
    val sheetItemSurface: Color,
    val secondaryText: Color,
    val defaultBorder: Color,
    val headerSurface: Color,
    val splashBackground: Color,
    val calendarTagSurface: Color,
    val optimalDay: Color,
    val amberStateForeground: Color,
    val amberStateBackground: Color,
    val emeraldStateForeground: Color,
    val emeraldStateBackground: Color,
    val redStateForeground: Color,
    val redStateBackground: Color,
    val violetStateForeground: Color,
    val violetStateBackground: Color,
    val onTrackStateForeground: Color,
    val onTrackStateBackground: Color,
    val switchUncheckedTrack: Color,
    val switchUncheckedThumb: Color,
    val switchDisabledTrack: Color,
    val switchDisabledThumb: Color,
)

val LightCardsReminderColors = CardsReminderColors(
    appBackground = AppBackgroundLight,
    authBackground = AuthBackgroundLight,
    primaryAction = BrandPrimaryLight,
    addActionButton = AddActionButton,
    onAddActionButton = OnAddActionButton,
    cardSurface = CardSurfaceLight,
    sheetItemSurface = SheetItemSurfaceLight,
    secondaryText = SecondaryTextLight,
    defaultBorder = DefaultBorderLight,
    headerSurface = HeaderSurfaceLight,
    splashBackground = AppBackgroundLight,
    calendarTagSurface = CalendarTagSurfaceLight,
    optimalDay = OptimalDayLight,
    amberStateForeground = AmberStateForegroundLight,
    amberStateBackground = AmberStateBackgroundLight,
    emeraldStateForeground = EmeraldStateForegroundLight,
    emeraldStateBackground = EmeraldStateBackgroundLight,
    redStateForeground = RedStateForegroundLight,
    redStateBackground = RedStateBackgroundLight,
    violetStateForeground = VioletStateForegroundLight,
    violetStateBackground = VioletStateBackgroundLight,
    onTrackStateForeground = OnTrackStateForegroundLight,
    onTrackStateBackground = OnTrackStateBackgroundLight,
    switchUncheckedTrack = SwitchUncheckedTrackLight,
    switchUncheckedThumb = SwitchUncheckedThumbLight,
    switchDisabledTrack = SwitchDisabledTrackLight,
    switchDisabledThumb = SwitchDisabledThumbLight,
)

val DarkCardsReminderColors = CardsReminderColors(
    appBackground = AppBackgroundDark,
    authBackground = AuthBackgroundDark,
    primaryAction = BrandPrimaryDark,
    addActionButton = AddActionButton,
    onAddActionButton = OnAddActionButton,
    cardSurface = CardSurfaceDark,
    sheetItemSurface = SheetItemSurfaceDark,
    secondaryText = SecondaryTextDark,
    defaultBorder = DefaultBorderDark,
    headerSurface = HeaderSurfaceDark,
    splashBackground = AuthBackgroundDark,
    calendarTagSurface = CalendarTagSurfaceDark,
    optimalDay = OptimalDayDark,
    amberStateForeground = AmberStateForegroundDark,
    amberStateBackground = AmberStateBackgroundDark,
    emeraldStateForeground = EmeraldStateForegroundDark,
    emeraldStateBackground = EmeraldStateBackgroundDark,
    redStateForeground = RedStateForegroundDark,
    redStateBackground = RedStateBackgroundDark,
    violetStateForeground = VioletStateForegroundDark,
    violetStateBackground = VioletStateBackgroundDark,
    onTrackStateForeground = OnTrackStateForegroundDark,
    onTrackStateBackground = OnTrackStateBackgroundDark,
    switchUncheckedTrack = SwitchUncheckedTrackDark,
    switchUncheckedThumb = SwitchUncheckedThumbDark,
    switchDisabledTrack = SwitchDisabledTrackDark,
    switchDisabledThumb = SwitchDisabledThumbDark,
)

val LocalCardsReminderColors = staticCompositionLocalOf { LightCardsReminderColors }

val MaterialTheme.cardsReminder: CardsReminderColors
    @Composable
    @ReadOnlyComposable
    get() = LocalCardsReminderColors.current

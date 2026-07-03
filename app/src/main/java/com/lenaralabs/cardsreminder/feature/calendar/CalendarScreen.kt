package com.lenaralabs.cardsreminder.feature.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lenaralabs.cardsreminder.CardsReminderApp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.ui.animation.RevealStyle
import com.lenaralabs.cardsreminder.ui.animation.SmoothReveal
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
) {
    val application = LocalContext.current.applicationContext as CardsReminderApp
    val viewModel: CalendarViewModel = viewModel(
        factory = CalendarViewModel.Factory(application.cardsRepository),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.cardsReminder

    val scrollState = rememberScrollState()

    PullToRefreshBox(
        isRefreshing = state.isPullRefreshing,
        onRefresh = viewModel::refresh,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
            ) {
                Text(
                    text = stringResource(R.string.screen_calendar_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 4.dp),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )

                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.redStateForeground,
                    )
                }

                MonthHeader(
                    title = state.monthYearTitle,
                    onPreviousMonth = { viewModel.changeMonth(-1) },
                    onNextMonth = { viewModel.changeMonth(1) },
                )

                WeekdayLabelsRow()

                HorizontalDivider(color = colors.defaultBorder)

                    if (state.activeCards.isEmpty() && !state.isInitialLoading) {
                        CalendarEmptyState()
                    } else {
                        AnimatedVisibility(visible = state.activeCards.isNotEmpty()) {
                            CalendarGrid(
                                state = state,
                                viewModel = viewModel,
                            )
                        }

                        HorizontalDivider(color = colors.defaultBorder)

                        AnimatedVisibility(visible = state.activeCards.isNotEmpty()) {
                            SmoothReveal(
                                visible = true,
                                style = RevealStyle.FromBottom,
                            ) {
                                CalendarLegendRows(
                                    cards = state.activeCards,
                                    billingPeriods = state.visibleBillingPeriods,
                                    payments = state.visiblePayments,
                                    selection = state.selection,
                                    onSelectionChange = viewModel::onSelectionChange,
                                )
                            }
                        }
                    }

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (state.isInitialLoading && state.activeCards.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}

@Composable
private fun MonthHeader(
    title: String,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun WeekdayLabelsRow() {
    val colors = MaterialTheme.cardsReminder
    val weekdaySymbols = CalendarBillingLogic.weekdaySymbols()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
    ) {
        weekdaySymbols.forEach { symbol ->
            Text(
                text = symbol,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.secondaryText,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    state: CalendarUiState,
    viewModel: CalendarViewModel,
) {
    val barsByDay = remember(
        state.year,
        state.month,
        state.activeCards,
        state.periodsByCardId,
        state.selection,
        state.daysInMonth,
    ) {
        viewModel.barDisplaysByDay(state)
    }

    val weeks = state.calendarDays
        .chunked(7)
        .map { week ->
            if (week.size < 7) week + List(7 - week.size) { null } else week
        }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .pointerInput(state.year, state.month) {
                var totalDrag = 0f
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            totalDrag < -40f -> viewModel.changeMonth(1)
                            totalDrag > 40f -> viewModel.changeMonth(-1)
                        }
                        totalDrag = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        totalDrag += dragAmount
                    },
                )
            },
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        weeks.forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                week.forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                    ) {
                        if (day != null) {
                            DayCell(
                                day = day,
                                isToday = viewModel.isToday(day, state),
                                bars = barsByDay[day].orEmpty(),
                            )
                        } else {
                            Spacer(modifier = Modifier.height(52.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarEmptyState() {
    val colors = MaterialTheme.cardsReminder

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.calendar_empty_title),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.calendar_empty_message),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
        )
    }
}

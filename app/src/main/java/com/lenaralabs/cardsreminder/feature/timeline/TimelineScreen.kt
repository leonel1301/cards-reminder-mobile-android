package com.lenaralabs.cardsreminder.feature.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lenaralabs.cardsreminder.CardsReminderApp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.feature.cards.CardPaymentsBottomSheet
import com.lenaralabs.cardsreminder.ui.animation.RevealStyle
import com.lenaralabs.cardsreminder.ui.animation.SmoothReveal
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    modifier: Modifier = Modifier,
) {
    val application = LocalContext.current.applicationContext as CardsReminderApp
    val viewModel: TimelineViewModel = viewModel(
        factory = TimelineViewModel.Factory(
            paymentsRepository = application.paymentsRepository,
            cardsRepository = application.cardsRepository,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MaterialTheme.cardsReminder
    val contentKey = remember(
        state.summary,
        state.sections,
        state.featuredEntry,
        state.showAllGood,
    ) {
        state.summary.hashCode() +
            state.sections.hashCode() +
            (state.featuredEntry?.card?.id?.hashCode() ?: 0) +
            state.showAllGood.hashCode()
    }
    var contentVisible by remember(contentKey) { mutableStateOf(false) }
    LaunchedEffect(contentKey) {
        contentVisible = true
    }

    state.pendingQuickPayCard?.let { card ->
        AlertDialog(
            onDismissRequest = viewModel::dismissQuickMarkPaid,
            title = { Text(stringResource(R.string.payments_quick_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.payments_quick_confirm_message_fallback,
                    ),
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmQuickMarkPaid) {
                    Text(stringResource(R.string.payments_mark_paid))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissQuickMarkPaid) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    state.paymentsCard?.let { card ->
        CardPaymentsBottomSheet(
            card = card,
            paymentsRepository = application.paymentsRepository,
            cardsRepository = application.cardsRepository,
            onDismissRequest = viewModel::closePayments,
            onEdit = viewModel::closePayments,
            onMarkPaidSuccess = viewModel::closePayments,
        )
    }

    val feeling = state.feeling

    if (state.showFeelingSheet && feeling != null) {
        FinanceFeelingSheet(
            feeling = feeling,
            onDismissRequest = viewModel::closeFeelingSheet,
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        TimelineHeader(
            headerDate = state.headerDate,
            feeling = state.feeling,
            showFeeling = !state.isInitialLoading && state.summary != null,
            onFeelingClick = viewModel::openFeelingSheet,
        )

        PullToRefreshBox(
            isRefreshing = state.isPullRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    if (state.showEmptyState) {
                        item {
                            TimelineEmptyState(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .fillMaxWidth(),
                            )
                        }
                    } else {
                        state.errorMessage?.takeIf { !state.hasCachedDashboard }
                            ?.let { message ->
                                item {
                                    Text(
                                        text = message,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = colors.redStateForeground,
                                    )
                                }
                            }

                        state.summary?.let { summary ->
                            item {
                                SmoothReveal(
                                    visible = contentVisible,
                                    style = RevealStyle.Section,
                                ) {
                                    TimelineSummaryStrip(summary = summary)
                                }
                            }
                        }

                        state.featuredEntry?.let { entry ->
                            item {
                                SmoothReveal(
                                    visible = contentVisible,
                                    style = RevealStyle.Card,
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        TimelineFeaturedCard(
                                            card = entry.card,
                                            status = entry.status,
                                            modifier = Modifier.clickable {
                                                viewModel.openPayments(entry.card)
                                            },
                                        )
                                        state.bestForPurchase?.why?.let { why ->
                                            TimelinePurchaseInsightRow(why = why)
                                        }
                                    }
                                }
                            }
                        }

                        if (state.showAllGood) {
                            item {
                                SmoothReveal(
                                    visible = contentVisible,
                                    index = 1,
                                    style = RevealStyle.Section,
                                ) {
                                    TimelineAllGoodState(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                    )
                                }
                            }
                        } else {
                            state.sections.forEachIndexed { sectionIndex, section ->
                                item {
                                    SmoothReveal(
                                        visible = contentVisible,
                                        index = sectionIndex,
                                        style = RevealStyle.Section,
                                    ) {
                                        Text(
                                            text = stringResource(section.titleRes),
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    }
                                }
                                itemsIndexed(
                                    items = section.events,
                                    key = { _, event -> event.id },
                                ) { eventIndex, event ->
                                    SmoothReveal(
                                        visible = contentVisible,
                                        index = eventIndex,
                                        style = RevealStyle.Event,
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .padding(bottom = 14.dp),
                                    ) {
                                        TimelineEventListItem(
                                            event = event,
                                            isMarkingPaid = state.markingPaidCardId == event.card.id,
                                            onOpenPayments = { viewModel.openPayments(event.card) },
                                            onMarkPaid = { viewModel.requestQuickMarkPaid(event.card) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.isInitialLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineHeader(
    headerDate: String,
    feeling: DashboardFeeling?,
    showFeeling: Boolean,
    onFeelingClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.screen_timeline_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = headerDate,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.cardsReminder.secondaryText,
            )
            if (showFeeling && feeling != null) {
                FinanceFeelingButton(
                    feeling = feeling,
                    onClick = onFeelingClick,
                )
            }
        }
    }
}

@Composable
private fun TimelineEmptyState(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.cardsReminder
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 10.dp),
            tint = colors.secondaryText,
        )
        Text(
            text = stringResource(R.string.timeline_empty_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(R.string.timeline_empty_message),
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun TimelineAllGoodState(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.cardsReminder
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                colors.emeraldStateBackground.copy(alpha = 0.45f),
                androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            )
            .padding(vertical = 32.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Verified,
            contentDescription = null,
            tint = colors.emeraldStateForeground,
        )
        Text(
            text = stringResource(R.string.timeline_all_good_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = stringResource(R.string.timeline_all_good_message),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
        )
    }
}

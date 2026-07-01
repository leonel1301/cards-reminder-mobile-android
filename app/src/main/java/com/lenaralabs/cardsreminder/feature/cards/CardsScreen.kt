package com.lenaralabs.cardsreminder.feature.cards

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lenaralabs.cardsreminder.CardsReminderApp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardsScreen(
    modifier: Modifier = Modifier,
) {
    val application = LocalContext.current.applicationContext as CardsReminderApp
    val context = LocalContext.current
    val viewModel: CardsViewModel = viewModel(
        factory = CardsViewModel.Factory(
            cardsRepository = application.cardsRepository,
            paymentsRepository = application.paymentsRepository,
            ownersRepository = application.ownersRepository,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val owners by viewModel.owners.collectAsStateWithLifecycle()
    val colors = MaterialTheme.cardsReminder
    val selfOwnerFormat = stringResource(R.string.owner_self_format)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.appBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.screen_cards_title),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 4.dp),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )

            PullToRefreshBox(
                isRefreshing = state.isSaving && state.cards.isNotEmpty(),
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 88.dp),
                    ) {
                    state.dashboardSummary?.takeIf { it.hasAttentionItems }?.let { summary ->
                        item {
                            DashboardSummaryBanner(
                                summary = summary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }

                    state.errorMessage?.let { message ->
                        item {
                            Text(
                                text = message,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.redStateForeground,
                            )
                        }
                    }

                    if (state.cards.isEmpty() && !state.isLoading) {
                        item {
                            CardsEmptyState()
                        }
                    } else {
                        items(state.cards, key = { it.id }) { card ->
                            val isDeleting = state.deletingCardId == card.id
                            val isMarkingPaid = state.markingPaidCardId == card.id
                            val scale by animateFloatAsState(
                                targetValue = if (isDeleting) 0.92f else 1f,
                                label = "deleteScale",
                            )
                            val alpha by animateFloatAsState(
                                targetValue = if (isDeleting) 0f else 1f,
                                label = "deleteAlpha",
                            )

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .scale(scale)
                                    .alpha(alpha)
                                    .then(if (isDeleting) Modifier.blur(6.dp) else Modifier),
                            ) {
                                CreditCardTile(
                                    card = card,
                                    status = state.cardStatuses[card.id],
                                    isMenuExpanded = state.expandedMenuCardId == card.id,
                                    onMenuExpandedChange = { expanded ->
                                        viewModel.setMenuExpanded(card.id, expanded)
                                    },
                                    onOpenPayments = { viewModel.openPayments(card) },
                                    onMarkPaid = if (card.isActive) {
                                        { viewModel.requestQuickPay(card) }
                                    } else {
                                        null
                                    },
                                    onEdit = { viewModel.openEdit(card) },
                                    onDelete = { viewModel.requestDelete(card) },
                                )

                                if (isDeleting) {
                                    Box(
                                        modifier = Modifier
                                            .matchParentSize()
                                            .padding(0.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                }

                                if (isMarkingPaid) {
                                    Box(
                                        modifier = Modifier.matchParentSize(),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
        }

        ExtendedFloatingActionButton(
            onClick = viewModel::openCreate,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text(stringResource(R.string.action_add_card)) },
        )
    }

    when (val sheet = state.activeSheet) {
        is CardsSheet.Create, is CardsSheet.Edit -> {
            CardFormBottomSheet(
                mode = sheet,
                formState = state.formState,
                owners = owners,
                isSaving = state.isSaving,
                onDismissRequest = viewModel::requestDismissSheet,
                onSave = viewModel::saveForm,
                onDelete = viewModel::deleteFromForm,
                onNameChange = { value -> viewModel.updateForm { it.copy(name = value) } },
                onLastFourChange = viewModel::sanitizeLastFour,
                onIssuerChange = { value -> viewModel.updateForm { it.copy(issuer = value) } },
                onBillingDayChange = { value ->
                    viewModel.updateForm { it.copy(billingCycleDay = value) }
                },
                onPaymentDayChange = { value ->
                    viewModel.updateForm { it.copy(paymentDueDay = value) }
                },
                onColorChange = { value ->
                    viewModel.updateForm { it.copy(selectedColorHex = value) }
                },
                onNotesChange = { value -> viewModel.updateForm { it.copy(notes = value) } },
                onActiveChange = { value -> viewModel.updateForm { it.copy(isActive = value) } },
                onOwnerChange = { value -> viewModel.updateForm { it.copy(selectedOwnerId = value) } },
                onShowLastFourHelp = viewModel::showLastFourHelp,
                ownerDisplayName = { owner ->
                    viewModel.ownerDisplayName(owner, selfOwnerFormat)
                },
            )
        }

        is CardsSheet.Payments -> {
            CardPaymentsBottomSheet(
                card = sheet.card,
                paymentsRepository = application.paymentsRepository,
                cardsRepository = application.cardsRepository,
                onDismissRequest = viewModel::closeSheetCompletely,
                onEdit = { viewModel.editFromPayments(sheet.card) },
                onMarkPaidSuccess = viewModel::closeSheetCompletely,
            )
        }

        CardsSheet.None -> Unit
    }

    state.pendingDeleteCard?.let {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteDialog,
            title = { Text(stringResource(R.string.delete_card_confirm_title)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text(stringResource(R.string.action_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteDialog) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    state.pendingQuickPayCard?.let { card ->
        val period = viewModel.quickPayConfirmationMessage(card)
        AlertDialog(
            onDismissRequest = viewModel::dismissQuickPayDialog,
            title = { Text(stringResource(R.string.payments_quick_confirm_title)) },
            text = {
                Text(
                    if (period.isNotBlank()) {
                        stringResource(R.string.payments_quick_confirm_message, period)
                    } else {
                        stringResource(R.string.payments_quick_confirm_message_fallback)
                    },
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmQuickPay) {
                    Text(stringResource(R.string.payments_mark_paid))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissQuickPayDialog) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (state.showDiscardFormDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDiscardDialog,
            title = { Text(stringResource(R.string.form_discard_title)) },
            text = { Text(stringResource(R.string.form_discard_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDiscardForm) {
                    Text(stringResource(R.string.form_discard_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDiscardDialog) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (state.showLastFourHelp) {
        AlertDialog(
            onDismissRequest = viewModel::dismissLastFourHelp,
            title = { Text(stringResource(R.string.field_last_four_digits_help_title)) },
            text = { Text(stringResource(R.string.field_last_four_digits_help_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissLastFourHelp) {
                    Text(stringResource(R.string.action_ok))
                }
            },
        )
    }

    if (state.showRemindersPrompt) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRemindersPrompt,
            title = { Text(stringResource(R.string.card_create_reminders_title)) },
            text = { Text(stringResource(R.string.card_create_reminders_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val granted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS,
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (!granted) {
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                context.startActivity(intent)
                            }
                        }
                        viewModel.completeCreateAfterReminders()
                    },
                ) {
                    Text(stringResource(R.string.card_create_reminders_enable))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRemindersPrompt) {
                    Text(stringResource(R.string.action_not_now))
                }
            },
        )
    }

    if (state.showRemindersLaterInfo) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRemindersLaterInfo,
            title = { Text(stringResource(R.string.card_create_reminders_later_title)) },
            text = { Text(stringResource(R.string.card_create_reminders_later_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::dismissRemindersLaterInfo) {
                    Text(stringResource(R.string.action_ok))
                }
            },
        )
    }
}

@Composable
private fun CardsEmptyState() {
    val colors = MaterialTheme.cardsReminder
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.cards_empty_title),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.cards_empty_message),
            style = MaterialTheme.typography.bodySmall,
            color = colors.secondaryText,
            textAlign = TextAlign.Center,
        )
    }
}

package com.lenaralabs.cardsreminder.feature.cards

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lenaralabs.cardsreminder.CardsReminderApp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.ui.animation.AppMotion
import com.lenaralabs.cardsreminder.ui.animation.RevealStyle
import com.lenaralabs.cardsreminder.ui.animation.SmoothReveal
import com.lenaralabs.cardsreminder.ui.components.AppExtendedFab
import com.lenaralabs.cardsreminder.ui.components.AppInlineLoadingIndicator
import com.lenaralabs.cardsreminder.ui.components.AppPullToRefreshBox
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
            pushNotificationManager = application.pushNotificationManager,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val owners by viewModel.owners.collectAsStateWithLifecycle()
    val colors = MaterialTheme.cardsReminder
    val selfOwnerFormat = stringResource(R.string.owner_self_format)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.enableRemindersFromPrompt()
        } else {
            viewModel.dismissRemindersPrompt()
        }
    }

    fun requestEnableReminders() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                viewModel.enableRemindersFromPrompt()
            } else {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            viewModel.enableRemindersFromPrompt()
        }
    }

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

            AppPullToRefreshBox(
                isRefreshing = state.isPullRefreshing,
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
                            SmoothReveal(
                                visible = true,
                                style = RevealStyle.Section,
                            ) {
                                DashboardSummaryBanner(
                                    summary = summary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }
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

                    if (state.cards.isEmpty() && !state.isInitialLoading) {
                        item {
                            CardsEmptyState()
                        }
                    } else {
                        itemsIndexed(
                            items = state.cards,
                            key = { _, card -> card.id },
                        ) { index, card ->
                            val isDeleting = state.deletingCardId == card.id
                            val isMarkingPaid = state.markingPaidCardId == card.id
                            val scale by animateFloatAsState(
                                targetValue = if (isDeleting) 0.92f else 1f,
                                animationSpec = tween(AppMotion.BASE_DURATION_MS),
                                label = "deleteScale",
                            )
                            val alpha by animateFloatAsState(
                                targetValue = if (isDeleting) 0f else 1f,
                                animationSpec = tween(AppMotion.BASE_DURATION_MS),
                                label = "deleteAlpha",
                            )

                            SmoothReveal(
                                visible = !isDeleting,
                                index = index,
                                style = RevealStyle.Card,
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
                                        AppInlineLoadingIndicator(size = 32.dp)
                                    }
                                }
                            }
                        }
                    }
                }

                if (state.isInitialLoading && state.cards.isEmpty()) {
                    AppInlineLoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
        }

        AppExtendedFab(
            onClick = viewModel::openCreate,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = colors.addActionButton,
            contentColor = colors.onAddActionButton,
            icon = Icons.Filled.Add,
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
                onSheetDismissed = viewModel::onFormSheetDismissed,
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
                TextButton(onClick = ::requestEnableReminders) {
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

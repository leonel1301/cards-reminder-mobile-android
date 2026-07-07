package com.lenaralabs.cardsreminder.feature.profile

import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lenaralabs.cardsreminder.BuildConfig
import com.lenaralabs.cardsreminder.CardsReminderApp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.ApiOwner
import com.lenaralabs.cardsreminder.core.util.AppLinks
import com.lenaralabs.cardsreminder.core.util.DateFormatUtils
import com.lenaralabs.cardsreminder.ui.components.PoweredByLenaraFooter
import com.lenaralabs.cardsreminder.ui.animation.RevealStyle
import com.lenaralabs.cardsreminder.ui.animation.SmoothReveal
import com.lenaralabs.cardsreminder.ui.components.AppDropdownMenu
import com.lenaralabs.cardsreminder.ui.components.AppDropdownMenuItem
import com.lenaralabs.cardsreminder.ui.components.AppInlineLoadingIndicator
import com.lenaralabs.cardsreminder.ui.components.AppSignOutOverlay
import com.lenaralabs.cardsreminder.ui.components.AppPullToRefreshBox
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
) {
    val application = LocalContext.current.applicationContext as CardsReminderApp
    val context = LocalContext.current
    val viewModel: ProfileUiState.ProfileViewModel = viewModel(
        factory = ProfileUiState.ProfileViewModel.Factory(
            userRepository = application.userRepository,
            ownersRepository = application.ownersRepository,
            feedbackRepository = application.feedbackRepository,
            sessionRepository = application.sessionRepository,
        ),
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val feedbacks by viewModel.feedbacks.collectAsStateWithLifecycle()
    val feedbackLoading by application.feedbackRepository.isLoading.collectAsStateWithLifecycle()
    val colors = MaterialTheme.cardsReminder
    val salaryNotSet = stringResource(R.string.salary_day_not_set)
    val salaryDayFormat = stringResource(R.string.owner_salary_day_value)

    Box(modifier = modifier.fillMaxSize()) {
    if (state.showSettings) {
        SettingsScreen(
            isDeletingAccount = state.isLoadingProfile,
            onBack = viewModel::closeSettings,
            onSignOut = viewModel::signOut,
            onDeleteAccount = { viewModel.deleteAccount {} },
        )
    } else {
    fun openUrl(url: String) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    fun rateApp() {
        val marketUri = Uri.parse(AppLinks.PLAY_STORE)
        val webUri = Uri.parse(
            "https://play.google.com/store/apps/details?id=${context.packageName}",
        )
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, marketUri))
        }.onFailure {
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    state.pendingDeleteOwner?.let { owner ->
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteOwner,
            title = { Text(stringResource(R.string.delete_owner_confirm_title)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeleteOwner) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteOwner) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    state.pendingDeleteFeedback?.let {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteFeedback,
            title = { Text(stringResource(R.string.delete_feedback_confirm_title)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeleteFeedback) {
                    Text(
                        text = stringResource(R.string.action_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteFeedback) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    if (state.showDiscardFeedbackDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDiscardFeedbackDialog,
            title = { Text(stringResource(R.string.form_discard_title)) },
            text = { Text(stringResource(R.string.form_discard_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDiscardFeedback) {
                    Text(stringResource(R.string.form_discard_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDiscardFeedbackDialog) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }

    when (val sheet = state.activeSheet) {
        is ProfileSheet.CreateOwner, is ProfileSheet.EditOwner -> {
            OwnerFormBottomSheet(
                mode = sheet,
                formState = state.ownerFormState,
                isSaving = state.isLoadingOwners,
                onDismissRequest = viewModel::requestDismissOwnerSheet,
                onSave = {
                    val owner = (sheet as? ProfileSheet.EditOwner)?.owner
                    viewModel.saveOwner(isSelfOwner = owner?.isSelf == true, editingOwner = owner)
                },
                onDelete = {
                    (sheet as? ProfileSheet.EditOwner)?.owner?.let(viewModel::requestDeleteOwner)
                },
                onNameChange = { name ->
                    viewModel.updateOwnerForm { it.copy(name = name) }
                },
                onSalaryDayChange = { day ->
                    viewModel.updateOwnerForm { it.copy(salaryDaySelection = day) }
                },
            )
        }

        ProfileSheet.FeedbackList -> {
            FeedbackBottomSheet(
                feedbacks = feedbacks,
                isLoading = feedbackLoading,
                onDismissRequest = viewModel::closeFeedbackList,
                onRefresh = { viewModel.openFeedbackList() },
                onAddFeedback = viewModel::openCreateFeedback,
                onFeedbackClick = viewModel::openEditFeedback,
            )
        }

        is ProfileSheet.CreateFeedback, is ProfileSheet.EditFeedback -> {
            FeedbackFormBottomSheet(
                mode = sheet,
                formState = state.feedbackFormState,
                isSaving = feedbackLoading,
                onDismissRequest = viewModel::requestDismissFeedbackForm,
                onSave = {
                    val feedback = (sheet as? ProfileSheet.EditFeedback)?.feedback
                    viewModel.saveFeedback(feedback)
                },
                onDelete = {
                    (sheet as? ProfileSheet.EditFeedback)?.feedback?.let(viewModel::requestDeleteFeedback)
                },
                onTitleChange = { title ->
                    viewModel.updateFeedbackForm { it.copy(title = title) }
                },
                onContentChange = { content ->
                    viewModel.updateFeedbackForm { it.copy(content = content) }
                },
            )
        }

        ProfileSheet.None -> Unit
    }

    AppPullToRefreshBox(
        isRefreshing = state.isPullRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.screen_profile_title),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Box {
                            IconButton(onClick = { viewModel.setMenuExpanded(true) }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = stringResource(R.string.action_more_options),
                                )
                            }
                            AppDropdownMenu(
                                expanded = state.showMenu,
                                onDismissRequest = { viewModel.setMenuExpanded(false) },
                            ) {
                                val menuActions = listOf(
                                    ProfileMenuAction(R.string.action_settings, viewModel::openSettings),
                                    ProfileMenuAction(R.string.action_sign_out, viewModel::signOut),
                                )
                                menuActions.forEachIndexed { index, action ->
                                    AppDropdownMenuItem(
                                        index = index,
                                        count = menuActions.size,
                                        text = { Text(stringResource(action.labelRes)) },
                                        onClick = action.onClick,
                                    )
                                }
                            }
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

                item {
                    ProfileSectionHeader(
                        title = stringResource(R.string.profile_section),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                    val memberSince = state.user?.createdAt?.let(DateFormatUtils::formatShortDate)
                        ?: stringResource(R.string.value_not_available)
                    ProfileDetailRow(
                        title = stringResource(R.string.field_member_since),
                        value = memberSince,
                        icon = Icons.Outlined.CalendarMonth,
                    )
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ProfileSectionHeader(
                            title = stringResource(R.string.owners_section),
                            modifier = Modifier.weight(1f),
                        )
                        if (state.owners.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Text(
                                    text = state.owners.size.toString(),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                        }
                    }
                    Button(
                        onClick = viewModel::openCreateOwner,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.addActionButton,
                            contentColor = colors.onAddActionButton,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(R.string.action_add_owner),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }

                if (state.isLoadingOwners && state.owners.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            AppInlineLoadingIndicator(size = 32.dp)
                        }
                    }
                } else if (state.owners.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.owners_empty_message),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.secondaryText,
                        )
                    }
                } else {
                    itemsIndexed(state.owners, key = { _, owner -> owner.id }) { index, owner ->
                        SmoothReveal(
                            visible = true,
                            index = index,
                            style = RevealStyle.Standard,
                        ) {
                            OwnerRow(
                                owner = owner,
                                displayName = owner.name,
                                salaryLabel = application.ownersRepository.salaryDayLabel(
                                    owner,
                                    salaryNotSet,
                                    salaryDayFormat,
                                ),
                                onClick = { viewModel.openEditOwner(owner) },
                            )
                        }
                    }
                }

                item {
                    ProfileSectionHeader(
                        title = stringResource(R.string.section_more_about_app),
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                    )
                    ProfileActionRow(
                        title = stringResource(R.string.action_rate_app),
                        icon = Icons.Outlined.Star,
                        onClick = ::rateApp,
                    )
                    ProfileActionRow(
                        title = stringResource(R.string.action_share_feedback),
                        icon = Icons.Outlined.Feedback,
                        onClick = viewModel::openFeedbackList,
                    )
                    ProfileActionRow(
                        title = stringResource(R.string.action_faq),
                        icon = Icons.Outlined.HelpOutline,
                        onClick = { openUrl(AppLinks.FAQ) },
                    )
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.app_description_short),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.secondaryText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        PoweredByLenaraFooter()
                        Text(
                            text = stringResource(
                                R.string.footer_version_build,
                                BuildConfig.VERSION_NAME,
                                BuildConfig.VERSION_CODE.toString(),
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.secondaryText,
                        )
                        state.user?.email?.let { email ->
                            Text(
                                text = stringResource(R.string.footer_user_email, email),
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.secondaryText,
                            )
                        }
                    }
                }
        }
    }
    }

    if (state.isSigningOut) {
        AppSignOutOverlay(
            indicatorColor = colors.primaryAction,
            trackColor = colors.defaultBorder,
        )
    }
    }
}

@Composable
private fun OwnerRow(
    owner: ApiOwner,
    displayName: String,
    salaryLabel: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.cardsReminder
    val accent = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = owner.name.firstOrNull()?.uppercaseChar()?.toString().orEmpty(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (owner.isSelf) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = stringResource(R.string.owner_self_badge),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
                Text(
                    text = salaryLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.secondaryText,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.size(18.dp),
        )
    }
}

private data class ProfileMenuAction(
    @param:StringRes val labelRes: Int,
    val onClick: () -> Unit,
)

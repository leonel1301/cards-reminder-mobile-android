package com.lenaralabs.cardsreminder.feature.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.ApiFeedback
import com.lenaralabs.cardsreminder.core.util.AppLinks
import com.lenaralabs.cardsreminder.core.util.DateFormatUtils
import com.lenaralabs.cardsreminder.ui.animation.RevealStyle
import com.lenaralabs.cardsreminder.ui.animation.SmoothReveal
import com.lenaralabs.cardsreminder.ui.components.AppInlineLoadingIndicator
import com.lenaralabs.cardsreminder.ui.components.AppPullToRefreshBox
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackBottomSheet(
    feedbacks: List<ApiFeedback>,
    isLoading: Boolean,
    onDismissRequest: () -> Unit,
    onRefresh: () -> Unit,
    onAddFeedback: () -> Unit,
    onFeedbackClick: (ApiFeedback) -> Unit,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.cardsReminder

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AppPullToRefreshBox(
                isRefreshing = isLoading && feedbacks.isNotEmpty(),
                onRefresh = onRefresh,
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 88.dp),
                ) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.feedback_sheet_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = stringResource(R.string.feedback_more_link),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clickable {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse(AppLinks.FEEDBACK_WEB),
                                        )
                                        context.startActivity(intent)
                                    },
                            )
                        }
                    }

                    if (feedbacks.isEmpty() && !isLoading) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.feedback_empty_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                )
                                Text(
                                    text = stringResource(R.string.feedback_empty_message),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.secondaryText,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    } else {
                        itemsIndexed(feedbacks, key = { _, feedback -> feedback.id }) { index, feedback ->
                            SmoothReveal(
                                visible = true,
                                index = index,
                                style = RevealStyle.Standard,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onFeedbackClick(feedback) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                ) {
                                    Text(
                                        text = feedback.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        text = DateFormatUtils.formatShortDateTime(feedback.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.secondaryText,
                                        modifier = Modifier.padding(top = 2.dp),
                                    )
                                    Text(
                                        text = feedback.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.secondaryText,
                                        modifier = Modifier.padding(top = 4.dp),
                                        maxLines = 2,
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }

                    if (isLoading && feedbacks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                AppInlineLoadingIndicator(size = 32.dp)
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = onAddFeedback,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.action_add_feedback))
            }
        }
    }
}

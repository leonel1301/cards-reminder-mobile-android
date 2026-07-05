package com.lenaralabs.cardsreminder.feature.cards

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.ApiCard
import com.lenaralabs.cardsreminder.core.model.ApiCardStatus
import com.lenaralabs.cardsreminder.core.util.isLightForegroundPreferred
import com.lenaralabs.cardsreminder.ui.components.AppDropdownMenu
import com.lenaralabs.cardsreminder.ui.components.AppDropdownMenuItem
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@Composable
fun CreditCardTile(
    card: ApiCard,
    status: ApiCardStatus?,
    isMenuExpanded: Boolean,
    onMenuExpandedChange: (Boolean) -> Unit,
    onOpenPayments: () -> Unit,
    onMarkPaid: (() -> Unit)?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardColor = card.color
    val useDarkText = cardColor.isLightForegroundPreferred()
    val contentColor = if (useDarkText) Color.Black.copy(alpha = 0.82f) else Color.White
    val secondaryColor = if (useDarkText) Color.Black.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.78f)
    val isPaidThisCycle = status?.isPaidThisCycle == true
    val colors = MaterialTheme.cardsReminder

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .shadow(
                elevation = if (card.isActive) 10.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = cardColor.copy(alpha = if (card.isActive) 0.35f else 0.15f),
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpenPayments)
            .alpha(if (card.isActive) 1f else 0.72f),
    ) {
        CardBackground(cardColor = cardColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ChipView()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp, end = 8.dp),
                ) {
                    Text(
                        text = card.name,
                        color = contentColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 2,
                    )
                    card.issuer?.takeIf { it.isNotBlank() }?.let { issuer ->
                        Text(
                            text = issuer,
                            color = secondaryColor,
                            fontSize = 12.sp,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (card.isActive && onMarkPaid != null) {
                        if (isPaidThisCycle) {
                            PaidIndicator()
                        } else {
                            PayActionButton(
                                onClick = onMarkPaid,
                            )
                        }
                    }

                    Box {
                        IconButton(
                            onClick = { onMenuExpandedChange(true) },
                            modifier = Modifier.size(28.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = stringResource(R.string.action_more_options),
                                tint = contentColor,
                                modifier = Modifier.size(18.dp),
                            )
                        }

                        AppDropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { onMenuExpandedChange(false) },
                        ) {
                            val menuActions = buildList {
                                add(
                                    MenuAction(R.string.payments_view_history) {
                                        onMenuExpandedChange(false)
                                        onOpenPayments()
                                    },
                                )
                                if (card.isActive && onMarkPaid != null && !isPaidThisCycle) {
                                    add(
                                        MenuAction(R.string.action_pay) {
                                            onMenuExpandedChange(false)
                                            onMarkPaid()
                                        },
                                    )
                                }
                                add(
                                    MenuAction(R.string.screen_edit_card_title) {
                                        onMenuExpandedChange(false)
                                        onEdit()
                                    },
                                )
                                add(
                                    MenuAction(R.string.action_delete_card) {
                                        onMenuExpandedChange(false)
                                        onDelete()
                                    },
                                )
                            }
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

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = card.maskedNumber,
                color = contentColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                letterSpacing = 2.sp,
                maxLines = 1,
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.card_billing_label),
                        color = secondaryColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(
                            R.string.billing_cut_payment,
                            card.billingCycleDay,
                            card.paymentDueDay,
                        ),
                        color = contentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (status != null && card.isActive) {
                    CardStatusBadge(status = status)
                }
            }
        }

        if (!card.isActive) {
            Text(
                text = stringResource(R.string.card_inactive_badge),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        RoundedCornerShape(50),
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CardBackground(cardColor: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(cardColor),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.22f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.18f),
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = 120.dp, y = (-60).dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = (-100).dp, y = 80.dp)
                .background(Color.Black.copy(alpha = 0.06f), CircleShape),
        )
    }
}

@Composable
private fun ChipView() {
    Box(
        modifier = Modifier
            .size(width = 38.dp, height = 28.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFEBD68C),
                        Color(0xFFC7A852),
                    ),
                ),
            )
            .border(0.5.dp, Color.Black.copy(alpha = 0.12f), RoundedCornerShape(5.dp)),
    )
}

@Composable
private fun PayActionButton(onClick: () -> Unit) {
    val colors = MaterialTheme.cardsReminder
    Row(
        modifier = Modifier
            .height(26.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.95f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Verified,
            contentDescription = null,
            tint = colors.emeraldStateForeground,
            modifier = Modifier.size(12.dp),
        )
        Text(
            text = stringResource(R.string.action_pay),
            color = colors.emeraldStateForeground,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 11.sp,
        )
    }
}

private data class MenuAction(
    @param:StringRes val labelRes: Int,
    val onClick: () -> Unit,
)

@Composable
private fun PaidIndicator() {
    val colors = MaterialTheme.cardsReminder
    Row(
        modifier = Modifier
            .height(26.dp)
            .clip(RoundedCornerShape(50))
            .background(colors.emeraldStateBackground.copy(alpha = 0.95f))
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = colors.emeraldStateForeground,
            modifier = Modifier.size(11.dp),
        )
        Text(
            text = stringResource(R.string.card_status_paid),
            color = colors.emeraldStateForeground,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 10.sp,
        )
    }
}

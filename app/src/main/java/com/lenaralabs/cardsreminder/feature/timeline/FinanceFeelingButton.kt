package com.lenaralabs.cardsreminder.feature.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@Composable
fun FinanceFeelingButton(
    feeling: DashboardFeeling,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder
    val accent = feeling.accentColor(colors)

    AssistChip(
        onClick = onClick,
        modifier = modifier,
        label = {
            Text(
                text = stringResource(feeling.wordRes),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = feeling.icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(16.dp),
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = colors.sheetItemSurface,
            labelColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceFeelingSheet(
    feeling: DashboardFeeling,
    onDismissRequest: () -> Unit,
) {
    val colors = MaterialTheme.cardsReminder
    val accent = feeling.accentColor(colors)

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = stringResource(R.string.finance_feeling_sheet_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
            )

            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(accent.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = feeling.icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(36.dp),
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(feeling.wordRes),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(feeling.headlineRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.secondaryText,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.sheetItemSurface, MaterialTheme.shapes.large)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.finance_feeling_why_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )

                feeling.reasonLineResIds().forEachIndexed { index, resId ->
                    val arg = feeling.reasonLineArg(index)
                    val line = if (resId == R.string.finance_feeling_why_blank) {
                        stringResource(resId)
                    } else {
                        stringResource(resId, arg)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(top = 6.dp)
                                .size(6.dp)
                                .background(colors.secondaryText, CircleShape),
                        )
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

private fun DashboardFeeling.accentColor(
    palette: com.lenaralabs.cardsreminder.ui.theme.CardsReminderColors,
): androidx.compose.ui.graphics.Color {
    return when (kind) {
        DashboardFeelingKind.RedZone -> palette.redStateForeground
        DashboardFeelingKind.CrunchTime -> palette.amberStateForeground
        DashboardFeelingKind.Countdown -> palette.violetStateForeground
        DashboardFeelingKind.PrimeWindow -> palette.violetStateForeground
        DashboardFeelingKind.Cruising -> palette.emeraldStateForeground
        DashboardFeelingKind.ClearBooks -> palette.emeraldStateForeground
        DashboardFeelingKind.BlankSlate -> palette.secondaryText
    }
}

private val DashboardFeeling.icon: ImageVector
    get() = when (kind) {
        DashboardFeelingKind.RedZone -> Icons.Outlined.Warning
        DashboardFeelingKind.CrunchTime -> Icons.Outlined.Whatshot
        DashboardFeelingKind.Countdown -> Icons.Outlined.AccountBalance
        DashboardFeelingKind.PrimeWindow -> Icons.Outlined.AutoAwesome
        DashboardFeelingKind.Cruising -> Icons.Outlined.ShowChart
        DashboardFeelingKind.ClearBooks -> Icons.Outlined.Lock
        DashboardFeelingKind.BlankSlate -> Icons.Outlined.CreditCard
    }

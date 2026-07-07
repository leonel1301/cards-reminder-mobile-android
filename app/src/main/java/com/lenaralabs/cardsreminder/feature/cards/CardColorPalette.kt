package com.lenaralabs.cardsreminder.feature.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.core.util.toComposeColor
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardColorPalette(
    selection: String,
    onSelectionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.cardsReminder

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.sheetItemSurface)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CardPaletteOption.all.forEach { option ->
            val isSelected = CardPaletteOption.normalize(selection) == option.hex
            val color = option.hex.toComposeColor()
            val label = stringResource(option.nameRes)
            val needsBorder = color.luminance() < 0.15f

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = when {
                            isSelected -> 2.dp
                            needsBorder -> 1.dp
                            else -> 0.dp
                        },
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onSurface
                            needsBorder -> colors.defaultBorder
                            else -> Color.Transparent
                        },
                        shape = CircleShape,
                    )
                    .clickable { onSelectionChange(option.hex) }
                    .semantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

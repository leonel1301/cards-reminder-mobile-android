package com.lenaralabs.cardsreminder.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.util.AppLinks
import com.lenaralabs.cardsreminder.ui.theme.CardsreminderTheme
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@Composable
fun PoweredByLenaraFooter(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = MaterialTheme.cardsReminder
    val accessibilityLabel = stringResource(R.string.footer_powered_by_lenara)

    TextButton(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(AppLinks.LENARA_HOMEPAGE))
            context.startActivity(intent)
        },
        modifier = modifier.semantics {
            contentDescription = accessibilityLabel
        },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.footer_powered_by),
                style = MaterialTheme.typography.labelMedium,
                color = colors.secondaryText,
            )

            Text(
                text = " ${stringResource(R.string.footer_lenara)}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.primaryAction,
            )

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp),
                tint = colors.primaryAction,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PoweredByLenaraFooterPreview() {
    CardsreminderTheme {
        PoweredByLenaraFooter()
    }
}

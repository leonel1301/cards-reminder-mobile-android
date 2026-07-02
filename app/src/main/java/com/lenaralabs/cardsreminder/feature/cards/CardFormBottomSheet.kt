package com.lenaralabs.cardsreminder.feature.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.ApiOwner
import com.lenaralabs.cardsreminder.ui.theme.cardsReminder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardFormBottomSheet(
    mode: CardsSheet,
    formState: CardFormState,
    owners: List<ApiOwner>,
    isSaving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onNameChange: (String) -> Unit,
    onLastFourChange: (String) -> Unit,
    onIssuerChange: (String) -> Unit,
    onBillingDayChange: (Int) -> Unit,
    onPaymentDayChange: (Int) -> Unit,
    onColorChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onOwnerChange: (String?) -> Unit,
    onShowLastFourHelp: () -> Unit,
    ownerDisplayName: (ApiOwner) -> String,
) {
    val colors = MaterialTheme.cardsReminder
    val isEditing = mode is CardsSheet.Edit
    val isCreating = mode is CardsSheet.Create
    val title = if (isEditing) {
        stringResource(R.string.screen_edit_card_title)
    } else {
        stringResource(R.string.screen_new_card_title)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = isCreating)

    if (isCreating) {
        LaunchedEffect(Unit) {
            sheetState.expand()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismissRequest) {
                    Text(stringResource(R.string.action_cancel))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                TextButton(
                    onClick = onSave,
                    enabled = formState.canSave && !isSaving,
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }

            FormSection(title = stringResource(R.string.section_card)) {
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.field_card_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = formState.lastFourDigits,
                        onValueChange = onLastFourChange,
                        label = { Text(stringResource(R.string.field_last_four_digits)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        isError = formState.showLastFourValidation,
                    )
                    IconButton(onClick = onShowLastFourHelp) {
                        Icon(
                            imageVector = Icons.Outlined.HelpOutline,
                            contentDescription = stringResource(R.string.field_last_four_digits_help_title),
                        )
                    }
                }
                if (formState.showLastFourValidation) {
                    Text(
                        text = stringResource(R.string.field_last_four_digits_validation),
                        color = colors.redStateForeground,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                OutlinedTextField(
                    value = formState.issuer,
                    onValueChange = onIssuerChange,
                    label = { Text(stringResource(R.string.field_issuer_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }

            FormSection(title = stringResource(R.string.section_billing)) {
                DayNumberPicker(
                    label = stringResource(R.string.picker_billing_cycle_day),
                    value = formState.billingCycleDay,
                    onValueChange = onBillingDayChange,
                )
                Text(
                    text = stringResource(R.string.period_start_preview, formState.periodStartPreview),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.secondaryText,
                )
                DayNumberPicker(
                    label = stringResource(R.string.picker_payment_due_day),
                    value = formState.paymentDueDay,
                    onValueChange = onPaymentDayChange,
                )
            }

            if (owners.isNotEmpty()) {
                FormSection(title = stringResource(R.string.section_owner)) {
                    OwnerPicker(
                        owners = owners,
                        selectedOwnerId = formState.selectedOwnerId,
                        onOwnerChange = onOwnerChange,
                        ownerDisplayName = ownerDisplayName,
                    )
                }
            }

            FormSection(title = stringResource(R.string.section_appearance)) {
                CardColorPalette(
                    selection = formState.selectedColorHex,
                    onSelectionChange = onColorChange,
                )
            }

            FormSection(title = stringResource(R.string.section_notes)) {
                OutlinedTextField(
                    value = formState.notes,
                    onValueChange = onNotesChange,
                    label = { Text(stringResource(R.string.field_notes_optional)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                )
            }

            if (isEditing) {
                FormSection(title = "") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(R.string.field_card_active))
                        Switch(
                            checked = formState.isActive,
                            onCheckedChange = onActiveChange,
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Button(
                        onClick = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.redStateBackground,
                            contentColor = colors.redStateForeground,
                        ),
                    ) {
                        Text(stringResource(R.string.action_delete_card))
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (title.isNotBlank()) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OwnerPicker(
    owners: List<ApiOwner>,
    selectedOwnerId: String?,
    onOwnerChange: (String?) -> Unit,
    ownerDisplayName: (ApiOwner) -> String,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedOwner = owners.firstOrNull { it.id == selectedOwnerId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedOwner?.let(ownerDisplayName).orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.field_card_owner)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            owners.forEach { owner ->
                DropdownMenuItem(
                    text = { Text(ownerDisplayName(owner)) },
                    onClick = {
                        onOwnerChange(owner.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

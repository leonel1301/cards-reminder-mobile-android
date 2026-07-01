package com.lenaralabs.cardsreminder.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.core.model.ApiOwner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerFormBottomSheet(
    mode: ProfileSheet,
    formState: OwnerFormState,
    isSaving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onNameChange: (String) -> Unit,
    onSalaryDayChange: (Int) -> Unit,
) {
    val isEditing = mode is ProfileSheet.EditOwner
    val owner = (mode as? ProfileSheet.EditOwner)?.owner
    val isSelf = owner?.isSelf == true

    val title = when {
        isSelf -> stringResource(R.string.screen_edit_self_salary_title)
        isEditing -> stringResource(R.string.screen_edit_owner_title)
        else -> stringResource(R.string.screen_new_owner_title)
    }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )

            if (isSelf) {
                OutlinedTextField(
                    value = owner?.name.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text(stringResource(R.string.field_owner_name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                OutlinedTextField(
                    value = formState.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.field_owner_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            SalaryDayPicker(
                selection = formState.salaryDaySelection,
                onSelectionChange = onSalaryDayChange,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.action_cancel))
                }
                Button(
                    onClick = onSave,
                    enabled = formState.canSave && !isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }

            if (isEditing && !isSelf) {
                HorizontalDivider()
                TextButton(
                    onClick = onDelete,
                    enabled = !isSaving,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_delete_owner))
                }
            }
        }
    }
}

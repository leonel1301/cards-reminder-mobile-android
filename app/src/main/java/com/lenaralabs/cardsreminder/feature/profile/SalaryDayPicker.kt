package com.lenaralabs.cardsreminder.feature.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lenaralabs.cardsreminder.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalaryDayPicker(
    selection: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = when (selection) {
        0 -> stringResource(R.string.salary_day_not_set)
        else -> stringResource(R.string.owner_salary_day_value, selection)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.field_salary_day)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.salary_day_not_set)) },
                onClick = {
                    onSelectionChange(0)
                    expanded = false
                },
            )
            (1..31).forEach { day ->
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.owner_salary_day_value, day)) },
                    onClick = {
                        onSelectionChange(day)
                        expanded = false
                    },
                )
            }
        }
    }
}

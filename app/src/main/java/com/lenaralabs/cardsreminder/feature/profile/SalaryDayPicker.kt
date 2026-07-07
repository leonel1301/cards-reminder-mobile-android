package com.lenaralabs.cardsreminder.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.lenaralabs.cardsreminder.R
import com.lenaralabs.cardsreminder.feature.cards.DayNumberPicker

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

    DayNumberPicker(
        label = stringResource(R.string.field_salary_day),
        value = selection,
        displayText = displayText,
        notSetLabel = stringResource(R.string.salary_day_not_set),
        onValueChange = onSelectionChange,
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    )
}

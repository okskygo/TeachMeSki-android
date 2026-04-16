package com.teachmeski.app.ui.wizard.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.wizard.WizardUiState
import java.util.Calendar
import java.util.Locale

private const val DATE_PLACEHOLDER_EM_DASH = "\u2014"

private fun parseIsoToMillis(iso: String): Long {
    val parts = iso.split("-")
    if (parts.size != 3) return System.currentTimeMillis()
    return try {
        val y = parts[0].toInt()
        val m = parts[1].toInt() - 1
        val d = parts[2].toInt()
        Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m)
            set(Calendar.DAY_OF_MONTH, d)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}

private fun millisToIso(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return String.format(
        Locale.US,
        "%04d-%02d-%02d",
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleStep(
    state: WizardUiState,
    onDatesFlexibleChange: (Boolean) -> Unit,
    onDateStartChange: (String?) -> Unit,
    onDateEndChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val startDatePickerCd = stringResource(R.string.wizard_schedule_heading)
    val endDatePickerCd = stringResource(R.string.wizard_schedule_subheading)

    if (showStartPicker) {
        val initialMillis = state.dateStart?.let { parseIsoToMillis(it) }
        val pickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = initialMillis,
            )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onDateStartChange(millisToIso(it)) }
                        showStartPicker = false
                    },
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        ) {
            Column {
                Text(
                    text = stringResource(R.string.wizard_confirm_label_date),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                )
                DatePicker(state = pickerState)
            }
        }
    }

    if (showEndPicker) {
        val initialMillis = state.dateEnd?.let { parseIsoToMillis(it) }
        val pickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = initialMillis,
            )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onDateEndChange(millisToIso(it)) }
                        showEndPicker = false
                    },
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        ) {
            Column {
                Text(
                    text = stringResource(R.string.wizard_confirm_label_date),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                )
                DatePicker(state = pickerState)
            }
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.wizard_schedule_heading),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.wizard_schedule_subheading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.wizard_dates_flexible_label),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f, fill = false),
            )
            Switch(
                checked = state.datesFlexible,
                onCheckedChange = onDatesFlexibleChange,
            )
        }
        if (state.datesFlexible) {
            Text(
                text = stringResource(R.string.wizard_confirm_dates_undecided),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = { showStartPicker = true },
                    modifier =
                        Modifier
                            .weight(1f)
                            .semantics {
                                contentDescription = startDatePickerCd
                            },
                ) {
                    Text(text = state.dateStart ?: DATE_PLACEHOLDER_EM_DASH)
                }
                OutlinedButton(
                    onClick = { showEndPicker = true },
                    modifier =
                        Modifier
                            .weight(1f)
                            .semantics {
                                contentDescription = endDatePickerCd
                            },
                ) {
                    Text(text = state.dateEnd ?: DATE_PLACEHOLDER_EM_DASH)
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

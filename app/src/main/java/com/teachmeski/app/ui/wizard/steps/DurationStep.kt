package com.teachmeski.app.ui.wizard.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.TmsTextField
import com.teachmeski.app.ui.wizard.WizardUiState
import kotlin.math.abs

private val PRESET_DAYS = listOf(0.5, 1.0, 1.5, 2.0, 3.0)

private fun isPresetValue(days: Double): Boolean =
    PRESET_DAYS.any { abs(it - days) < 1e-6 }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DurationStep(
    state: WizardUiState,
    onDurationChange: (Double) -> Unit,
    modifier: Modifier = Modifier,
) {
    val otherSelected = !isPresetValue(state.durationDays)
    var otherText by remember { mutableStateOf("") }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.wizard_duration_heading),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(R.string.wizard_duration_subheading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DurationPresetChip(
                label = stringResource(R.string.wizard_duration_half_day),
                selected = abs(state.durationDays - 0.5) < 1e-6,
                onClick = { onDurationChange(0.5) },
            )
            DurationPresetChip(
                label = stringResource(R.string.wizard_duration_1),
                selected = abs(state.durationDays - 1.0) < 1e-6,
                onClick = { onDurationChange(1.0) },
            )
            DurationPresetChip(
                label = stringResource(R.string.wizard_duration_1_5),
                selected = abs(state.durationDays - 1.5) < 1e-6,
                onClick = { onDurationChange(1.5) },
            )
            DurationPresetChip(
                label = stringResource(R.string.wizard_duration_2),
                selected = abs(state.durationDays - 2.0) < 1e-6,
                onClick = { onDurationChange(2.0) },
            )
            DurationPresetChip(
                label = stringResource(R.string.wizard_duration_3),
                selected = abs(state.durationDays - 3.0) < 1e-6,
                onClick = { onDurationChange(3.0) },
            )
            DurationPresetChip(
                label = stringResource(R.string.wizard_duration_other),
                selected = otherSelected,
                onClick = {
                    if (isPresetValue(state.durationDays)) {
                        val next = 2.5
                        otherText = formatDurationInput(next)
                        onDurationChange(next)
                    } else {
                        otherText = formatDurationInput(state.durationDays)
                        onDurationChange(state.durationDays)
                    }
                },
            )
        }
        if (otherSelected) {
            TmsTextField(
                value = otherText,
                onValueChange = { raw ->
                    otherText = raw
                    val parsed = raw.toDoubleOrNull()
                    if (parsed != null) {
                        onDurationChange(parsed)
                    }
                },
                label = stringResource(R.string.wizard_duration_other),
                placeholder = stringResource(R.string.wizard_duration_unit),
                keyboardOptions =
                    KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                    ),
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun DurationPresetChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        colors =
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            ),
    )
}

private fun formatDurationInput(days: Double): String {
    val rounded = (days * 2.0).toInt() / 2.0
    return if (abs(rounded - rounded.toLong().toDouble()) < 1e-6) {
        rounded.toLong().toString()
    } else {
        rounded.toString()
    }
}

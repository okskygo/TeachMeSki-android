package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.instructorwizard.InstructorWizardUiState
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun PricingStep(
    state: InstructorWizardUiState,
    onPriceHalfDayChange: (String) -> Unit,
    onPriceFullDayChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val h = state.priceHalfDay.trim().toIntOrNull()
    val f = state.priceFullDay.trim().toIntOrNull()
    val bothFilled = state.priceHalfDay.isNotBlank() && state.priceFullDay.isNotBlank()
    val warnFullLtHalf = bothFilled && h != null && f != null && f < h
    val anyInput = state.priceHalfDay.isNotBlank() || state.priceFullDay.isNotBlank()
    val invalid =
        anyInput &&
            when {
                state.priceHalfDay.isBlank() || state.priceFullDay.isBlank() -> true
                h == null || f == null || h <= 0 || f <= 0 -> true
                f < h -> true
                else -> false
            }

    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step7_heading),
                style = MaterialTheme.typography.headlineSmall,
                color = TmsColor.OnSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step7_subheading),
                style = MaterialTheme.typography.bodyMedium,
                color = TmsColor.OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step7_hint),
                style = MaterialTheme.typography.bodySmall,
                color = TmsColor.OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            OutlinedTextField(
                value = state.priceHalfDay,
                onValueChange = onPriceHalfDayChange,
                label = { Text(stringResource(R.string.instructor_wizard_step7_half_day_label)) },
                prefix = { Text(stringResource(R.string.instructor_wizard_step7_currency)) },
                isError = invalid,
                supportingText = {
                    if (invalid && state.priceHalfDay.isNotBlank() && state.priceFullDay.isNotBlank()) {
                        Text(stringResource(R.string.instructor_wizard_step7_error_invalid_price))
                    }
                },
                singleLine = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
            )
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            OutlinedTextField(
                value = state.priceFullDay,
                onValueChange = onPriceFullDayChange,
                label = { Text(stringResource(R.string.instructor_wizard_step7_full_day_label)) },
                prefix = { Text(stringResource(R.string.instructor_wizard_step7_currency)) },
                isError = invalid,
                singleLine = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
            )
        }
        item {
            if (warnFullLtHalf) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.instructor_wizard_step7_warning_full_lt_half),
                    style = MaterialTheme.typography.bodySmall,
                    color = TmsColor.Warning,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    val warnFullLtHalf = bothFilled && h != null && f != null && f in 1..<h

    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.instructor_wizard_step7_heading),
            style = MaterialTheme.typography.headlineSmall,
            color = TmsColor.OnSurface,
        )
        Text(
            text = stringResource(R.string.instructor_wizard_step7_subheading),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )

        Surface(
            color = TmsColor.SurfaceLow,
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                text = stringResource(R.string.instructor_wizard_step7_hint),
                style = MaterialTheme.typography.bodySmall,
                color = TmsColor.OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }

        PriceInputCard(
            label = stringResource(R.string.instructor_wizard_step7_half_day_label),
            value = state.priceHalfDay,
            currency = stringResource(R.string.instructor_wizard_step7_currency),
            notSetLabel = stringResource(R.string.instructor_wizard_step7_not_set),
            onValueChange = onPriceHalfDayChange,
        )

        PriceInputCard(
            label = stringResource(R.string.instructor_wizard_step7_full_day_label),
            value = state.priceFullDay,
            currency = stringResource(R.string.instructor_wizard_step7_currency),
            notSetLabel = stringResource(R.string.instructor_wizard_step7_not_set),
            onValueChange = onPriceFullDayChange,
        )

        if (warnFullLtHalf) {
            Text(
                text = stringResource(R.string.instructor_wizard_step7_warning_full_lt_half),
                style = MaterialTheme.typography.bodySmall,
                color = TmsColor.Warning,
            )
        }
    }
}

@Composable
private fun PriceInputCard(
    label: String,
    value: String,
    currency: String,
    notSetLabel: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val numericValue = value.trim().toIntOrNull()
    val displayPill =
        if (numericValue != null && numericValue > 0) {
            "$currency${"%,d".format(numericValue)}"
        } else {
            notSetLabel
        }
    val hasPill = numericValue != null && numericValue > 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TmsColor.SurfaceLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = TmsColor.OnSurface,
                )
                Surface(
                    color =
                        if (hasPill) {
                            TmsColor.Primary.copy(alpha = 0.10f)
                        } else {
                            TmsColor.SurfaceLow
                        },
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = displayPill,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasPill) TmsColor.Primary else TmsColor.Outline,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    )
                }
            }
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                prefix = { Text(currency) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

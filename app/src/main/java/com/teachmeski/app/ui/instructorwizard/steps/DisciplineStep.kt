package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.instructorwizard.InstructorWizardUiState
import com.teachmeski.app.ui.theme.TmsColor

private const val KEY_SKI = "ski"
private const val KEY_SNOWBOARD = "snowboard"

@Composable
fun DisciplineStep(
    state: InstructorWizardUiState,
    onToggleDiscipline: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step1_heading),
                style = MaterialTheme.typography.headlineSmall,
                color = TmsColor.OnSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step1_subheading),
                style = MaterialTheme.typography.bodyMedium,
                color = TmsColor.OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            DisciplineCard(
                title = stringResource(R.string.instructor_wizard_step1_discipline_ski),
                selected = KEY_SKI in state.selectedDisciplines,
                onToggle = { onToggleDiscipline(KEY_SKI) },
            )
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            DisciplineCard(
                title = stringResource(R.string.instructor_wizard_step1_discipline_snowboard),
                selected = KEY_SNOWBOARD in state.selectedDisciplines,
                onToggle = { onToggleDiscipline(KEY_SNOWBOARD) },
            )
        }
    }
}

@Composable
private fun DisciplineCard(
    title: String,
    selected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable(onClick = onToggle),
        shape = RoundedCornerShape(12.dp),
        color =
            if (selected) {
                TmsColor.Primary.copy(alpha = 0.08f)
            } else {
                TmsColor.SurfaceLowest
            },
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggle() },
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TmsColor.OnSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

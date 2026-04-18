package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.instructor_wizard_step1_heading),
            style = MaterialTheme.typography.headlineSmall,
            color = TmsColor.OnSurface,
        )
        Text(
            text = stringResource(R.string.instructor_wizard_step1_subheading),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DisciplineCard(
                selected = KEY_SKI in state.selectedDisciplines,
                onClick = { onToggleDiscipline(KEY_SKI) },
                iconRes = R.drawable.ic_ski,
                label = stringResource(R.string.instructor_wizard_step1_discipline_ski),
            )
            DisciplineCard(
                selected = KEY_SNOWBOARD in state.selectedDisciplines,
                onClick = { onToggleDiscipline(KEY_SNOWBOARD) },
                iconRes = R.drawable.ic_snowboard,
                label = stringResource(R.string.instructor_wizard_step1_discipline_snowboard),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisciplineCard(
    selected: Boolean,
    onClick: () -> Unit,
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = TmsColor.SurfaceLow,
        border =
            BorderStroke(
                width = 2.dp,
                color = if (selected) TmsColor.Primary else TmsColor.OutlineVariant,
            ),
        shadowElevation = if (selected) 4.dp else 0.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(40.dp),
                tint = TmsColor.Primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = TmsColor.OnSurface,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

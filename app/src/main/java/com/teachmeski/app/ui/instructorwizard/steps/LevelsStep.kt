package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.instructorwizard.InstructorWizardUiState
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun LevelsStep(
    state: InstructorWizardUiState,
    onToggleLevel: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val useSnowboardOnly =
        state.selectedDisciplines.contains("snowboard") &&
            !state.selectedDisciplines.contains("ski")

    val maxSelected = state.teachableLevels.maxOrNull() ?: -1

    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.instructor_wizard_step2_heading),
            style = MaterialTheme.typography.headlineSmall,
            color = TmsColor.OnSurface,
        )
        Text(
            text = stringResource(R.string.instructor_wizard_step2_subheading),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (lv in 0..4) {
                val isMax = lv == maxSelected
                val implied = lv < maxSelected && lv in state.teachableLevels
                LevelCard(
                    level = lv,
                    description = stringResource(levelDescriptionRes(useSnowboardOnly, lv)),
                    selected = isMax,
                    implied = implied,
                    onClick = { onToggleLevel(lv) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LevelCard(
    level: Int,
    description: String,
    selected: Boolean,
    implied: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = TmsColor.SurfaceLowest,
            ),
        border =
            when {
                selected -> BorderStroke(2.dp, TmsColor.Primary)
                implied -> BorderStroke(1.dp, TmsColor.Primary.copy(alpha = 0.40f))
                else -> null
            },
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = if (selected || implied) 0.dp else 1.dp,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.explore_card_skill_level_fmt, level.toString()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color =
                        if (implied) TmsColor.Primary.copy(alpha = 0.60f) else TmsColor.Primary,
                )
                if (selected || implied) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.bodySmall,
                        color = TmsColor.Outline,
                    )
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (implied) TmsColor.OnSurfaceVariant else TmsColor.OnSurface,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

private fun levelDescriptionRes(useSnowboard: Boolean, level: Int): Int =
    if (useSnowboard) {
        when (level) {
            0 -> R.string.instructor_wizard_step2_level_snowboard_0
            1 -> R.string.instructor_wizard_step2_level_snowboard_1
            2 -> R.string.instructor_wizard_step2_level_snowboard_2
            3 -> R.string.instructor_wizard_step2_level_snowboard_3
            4 -> R.string.instructor_wizard_step2_level_snowboard_4
            else -> R.string.instructor_wizard_step2_level_snowboard_0
        }
    } else {
        when (level) {
            0 -> R.string.instructor_wizard_step2_level_ski_0
            1 -> R.string.instructor_wizard_step2_level_ski_1
            2 -> R.string.instructor_wizard_step2_level_ski_2
            3 -> R.string.instructor_wizard_step2_level_ski_3
            4 -> R.string.instructor_wizard_step2_level_ski_4
            else -> R.string.instructor_wizard_step2_level_ski_0
        }
    }

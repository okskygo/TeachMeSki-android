package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.instructorwizard.InstructorWizardUiState
import com.teachmeski.app.ui.theme.TmsColor

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LevelsStep(
    state: InstructorWizardUiState,
    onToggleLevel: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val useSnowboardOnly =
        state.selectedDisciplines.contains("snowboard") &&
            !state.selectedDisciplines.contains("ski")

    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step2_heading),
                style = MaterialTheme.typography.headlineSmall,
                color = TmsColor.OnSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step2_subheading),
                style = MaterialTheme.typography.bodyMedium,
                color = TmsColor.OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
        item {
            FlowRow(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (lv in 0..4) {
                    val sel = lv in state.teachableLevels
                    FilterChip(
                        selected = sel,
                        onClick = { onToggleLevel(lv) },
                        label = {
                            Text(
                                text =
                                    stringResource(
                                        R.string.explore_card_skill_level_fmt,
                                        lv.toString(),
                                    ),
                            )
                        },
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                state.teachableLevels.sorted().forEach { lv ->
                    Text(
                        text = stringResource(levelDescriptionRes(useSnowboardOnly, lv)),
                        style = MaterialTheme.typography.bodySmall,
                        color = TmsColor.OnSurfaceVariant,
                    )
                }
            }
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

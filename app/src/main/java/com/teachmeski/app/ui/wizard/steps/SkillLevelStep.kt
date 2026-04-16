package com.teachmeski.app.ui.wizard.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.ui.wizard.WizardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillLevelStep(
    state: WizardUiState,
    onSkillLevelChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.wizard_level_heading),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.wizard_level_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        (0..4).forEach { level ->
            val selected = state.skillLevel == level
            val descriptionRes = skillLevelDescriptionRes(state.discipline, level)
            Card(
                onClick = { onSkillLevelChange(level) },
                modifier = Modifier.fillMaxWidth(),
                colors =
                    CardDefaults.cardColors(
                        containerColor =
                            if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                    ),
                border =
                    if (selected) {
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                    } else {
                        null
                    },
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.wizard_confirm_level_prefix) + level,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(descriptionRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }
}

private fun skillLevelDescriptionRes(discipline: Discipline, level: Int): Int {
    val safeLevel = level.coerceIn(0, 4)
    return when (discipline) {
        Discipline.Ski ->
            when (safeLevel) {
                0 -> R.string.wizard_level_ski_0
                1 -> R.string.wizard_level_ski_1
                2 -> R.string.wizard_level_ski_2
                3 -> R.string.wizard_level_ski_3
                else -> R.string.wizard_level_ski_4
            }
        Discipline.Snowboard ->
            when (safeLevel) {
                0 -> R.string.wizard_level_snowboard_0
                1 -> R.string.wizard_level_snowboard_1
                2 -> R.string.wizard_level_snowboard_2
                3 -> R.string.wizard_level_snowboard_3
                else -> R.string.wizard_level_snowboard_4
            }
    }
}

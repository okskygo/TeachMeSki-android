package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.TmsChip
import com.teachmeski.app.ui.instructorwizard.InstructorWizardUiState
import com.teachmeski.app.ui.theme.TmsColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LanguagesStep(
    state: InstructorWizardUiState,
    onToggleLanguage: (String) -> Unit,
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
            text = stringResource(R.string.instructor_wizard_step6_heading),
            style = MaterialTheme.typography.headlineSmall,
            color = TmsColor.OnSurface,
        )
        Text(
            text = stringResource(R.string.instructor_wizard_step6_subheading),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TmsChip(
                selected = "zh" in state.languages,
                onClick = { onToggleLanguage("zh") },
                label = stringResource(R.string.instructor_wizard_step6_lang_zh),
            )
            TmsChip(
                selected = "en" in state.languages,
                onClick = { onToggleLanguage("en") },
                label = stringResource(R.string.instructor_wizard_step6_lang_en),
            )
            TmsChip(
                selected = "ja" in state.languages,
                onClick = { onToggleLanguage("ja") },
                label = stringResource(R.string.instructor_wizard_step6_lang_ja),
            )
        }
    }
}

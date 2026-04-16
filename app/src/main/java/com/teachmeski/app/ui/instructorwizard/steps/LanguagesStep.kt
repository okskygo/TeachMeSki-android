package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.layout.Arrangement
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
fun LanguagesStep(
    state: InstructorWizardUiState,
    onToggleLanguage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step6_heading),
                style = MaterialTheme.typography.headlineSmall,
                color = TmsColor.OnSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step6_subheading),
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
                LanguageChip(
                    label = stringResource(R.string.instructor_wizard_step6_lang_zh),
                    selected = "zh" in state.languages,
                    onToggle = { onToggleLanguage("zh") },
                )
                LanguageChip(
                    label = stringResource(R.string.instructor_wizard_step6_lang_en),
                    selected = "en" in state.languages,
                    onToggle = { onToggleLanguage("en") },
                )
                LanguageChip(
                    label = stringResource(R.string.instructor_wizard_step6_lang_ja),
                    selected = "ja" in state.languages,
                    onToggle = { onToggleLanguage("ja") },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageChip(
    label: String,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onToggle,
        label = { Text(text = label) },
    )
}

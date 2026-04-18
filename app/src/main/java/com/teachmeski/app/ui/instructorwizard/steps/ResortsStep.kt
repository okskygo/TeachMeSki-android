package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.instructorwizard.InstructorWizardUiState
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.ui.wizard.ResortSelector

@Composable
fun ResortsStep(
    state: InstructorWizardUiState,
    onResortToggle: (String) -> Unit,
    onPrefectureToggle: (String) -> Unit,
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
            text = stringResource(R.string.instructor_wizard_step3_heading),
            style = MaterialTheme.typography.headlineSmall,
            color = TmsColor.OnSurface,
        )
        Text(
            text = stringResource(R.string.instructor_wizard_step3_subheading),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )
        when {
            state.isLoadingRegions -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = TmsColor.Primary)
                }
            }
            else -> {
                ResortSelector(
                    regions = state.regions,
                    selectedResortIds = state.selectedResortIds,
                    allRegionsSelected = false,
                    onResortToggle = onResortToggle,
                    onPrefectureToggle = onPrefectureToggle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

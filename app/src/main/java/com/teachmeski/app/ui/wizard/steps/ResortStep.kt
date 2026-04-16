package com.teachmeski.app.ui.wizard.steps

import androidx.compose.foundation.layout.fillMaxWidth
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
import com.teachmeski.app.ui.wizard.ResortSelector
import com.teachmeski.app.ui.wizard.WizardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResortStep(
    state: WizardUiState,
    onToggleAllRegions: () -> Unit,
    onResortToggle: (String) -> Unit,
    onPrefectureToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = stringResource(R.string.wizard_resort_heading),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            Text(
                text = stringResource(R.string.wizard_resort_subheading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        item {
            FilterChip(
                selected = state.allRegionsSelected,
                onClick = onToggleAllRegions,
                label = { Text(text = stringResource(R.string.wizard_resort_all_regions)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            Text(
                text = stringResource(R.string.wizard_resort_all_regions_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        item {
            ResortSelector(
                regions = state.regions,
                selectedResortIds = state.selectedResortIds,
                allRegionsSelected = state.allRegionsSelected,
                onResortToggle = onResortToggle,
                onPrefectureToggle = onPrefectureToggle,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

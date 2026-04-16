package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.instructorwizard.InstructorWizardUiState
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun ServicesStep(
    state: InstructorWizardUiState,
    onOffersTransportChange: (Boolean) -> Unit,
    onOffersPhotographyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step8_heading),
                style = MaterialTheme.typography.headlineSmall,
                color = TmsColor.OnSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step8_subheading),
                style = MaterialTheme.typography.bodyMedium,
                color = TmsColor.OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            ServiceSwitchRow(
                title = stringResource(R.string.instructor_wizard_step8_transport_label),
                description = stringResource(R.string.instructor_wizard_step8_transport_desc),
                checked = state.offersTransport,
                onCheckedChange = onOffersTransportChange,
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            ServiceSwitchRow(
                title = stringResource(R.string.instructor_wizard_step8_photography_label),
                description = stringResource(R.string.instructor_wizard_step8_photography_desc),
                checked = state.offersPhotography,
                onCheckedChange = onOffersPhotographyChange,
            )
        }
    }
}

@Composable
private fun ServiceSwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = TmsColor.OnSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TmsColor.OnSurfaceVariant,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

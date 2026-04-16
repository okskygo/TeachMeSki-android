package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.instructorwizard.InstructorWizardUiState
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun ProfileStep(
    state: InstructorWizardUiState,
    onDisplayNameChange: (String) -> Unit,
    onBioChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val nameTooLong = state.displayName.length > 50
    val bioTooLong = state.bio.length > 2000

    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step5_heading),
                style = MaterialTheme.typography.headlineSmall,
                color = TmsColor.OnSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            OutlinedTextField(
                value = state.displayName,
                onValueChange = onDisplayNameChange,
                label = { Text(stringResource(R.string.instructor_wizard_step5_name_label)) },
                placeholder = { Text(stringResource(R.string.instructor_wizard_step5_name_placeholder)) },
                isError = nameTooLong,
                supportingText =
                    if (nameTooLong) {
                        { Text(stringResource(R.string.instructor_wizard_step5_name_error_too_long)) }
                    } else {
                        null
                    },
                singleLine = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
            )
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            OutlinedTextField(
                value = state.bio,
                onValueChange = onBioChange,
                label = { Text(stringResource(R.string.instructor_wizard_step5_bio_label)) },
                placeholder = { Text(stringResource(R.string.instructor_wizard_step5_bio_placeholder)) },
                isError = bioTooLong,
                supportingText =
                    if (bioTooLong) {
                        { Text(stringResource(R.string.instructor_wizard_step5_bio_error_too_long)) }
                    } else {
                        { Text(stringResource(R.string.instructor_wizard_step5_bio_hint)) }
                    },
                minLines = 4,
                maxLines = 8,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
            )
        }
    }
}

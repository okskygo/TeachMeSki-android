package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.instructor_wizard_step5_heading),
            style = MaterialTheme.typography.headlineSmall,
            color = TmsColor.OnSurface,
        )
        if (state.isGuestMode) {
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
                modifier = Modifier.fillMaxWidth(),
            )
        }
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
            minLines = 5,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

package com.teachmeski.app.ui.wizard.steps

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.ui.wizard.WizardUiState

private const val NotesMaxLength = 1000

@Composable
fun NotesStep(
    state: WizardUiState,
    onNotesChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.wizard_notes_heading),
            style = MaterialTheme.typography.titleMedium,
            color = TmsColor.OnSurface,
        )
        Text(
            text = stringResource(R.string.wizard_notes_subheading),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )
        OutlinedTextField(
            value = state.additionalNotes,
            onValueChange = { if (it.length <= NotesMaxLength) onNotesChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = stringResource(R.string.wizard_notes_placeholder)) },
            minLines = 4,
            shape = RoundedCornerShape(12.dp),
            colors =
                OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = TmsColor.SurfaceLow,
                    focusedContainerColor = TmsColor.SurfaceLow,
                    unfocusedBorderColor = TmsColor.OutlineVariant,
                    focusedBorderColor = TmsColor.Primary,
                ),
        )
        Text(
            text = "${state.additionalNotes.length} / $NotesMaxLength",
            style = MaterialTheme.typography.bodySmall,
            color =
                if (state.additionalNotes.length >= 900) {
                    TmsColor.Warning
                } else {
                    TmsColor.OnSurfaceVariant
                },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            textAlign = TextAlign.End,
        )
    }
}

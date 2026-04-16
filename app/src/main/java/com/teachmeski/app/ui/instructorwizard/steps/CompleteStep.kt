package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun CompleteStep(
    profileAlreadyExists: Boolean,
    onStartExploring: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val titleRes =
            if (profileAlreadyExists) {
                R.string.instructor_wizard_errors_profile_exists
            } else {
                R.string.instructor_wizard_complete_celebration_title
            }
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TmsColor.OnSurface,
            textAlign = TextAlign.Center,
        )
        if (!profileAlreadyExists) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.instructor_wizard_complete_bonus_granted),
                style = MaterialTheme.typography.bodyLarge,
                color = TmsColor.OnSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onStartExploring,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.instructor_wizard_complete_start_exploring))
        }
    }
}

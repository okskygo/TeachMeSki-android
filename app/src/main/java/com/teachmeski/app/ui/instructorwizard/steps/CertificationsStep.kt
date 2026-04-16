package com.teachmeski.app.ui.instructorwizard.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.instructorwizard.InstructorWizardUiState
import com.teachmeski.app.ui.theme.TmsColor

internal val instructorWizardCertificationIds =
    listOf("CSIA", "CASI", "NZSIA", "PSIA", "SIA_Japan", "other")

private fun certificationLabelRes(id: String): Int =
    when (id) {
        "CSIA" -> R.string.instructor_wizard_step4_cert_CSIA
        "CASI" -> R.string.instructor_wizard_step4_cert_CASI
        "NZSIA" -> R.string.instructor_wizard_step4_cert_NZSIA
        "PSIA" -> R.string.instructor_wizard_step4_cert_PSIA
        "SIA_Japan" -> R.string.instructor_wizard_step4_cert_SIA_Japan
        "other" -> R.string.instructor_wizard_step4_cert_other
        else -> R.string.instructor_wizard_step4_cert_other
    }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CertificationsStep(
    state: InstructorWizardUiState,
    onToggleCertification: (String) -> Unit,
    onCertificationOtherChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step4_heading),
                style = MaterialTheme.typography.headlineSmall,
                color = TmsColor.OnSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            Text(
                text = stringResource(R.string.instructor_wizard_step4_subheading),
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
                instructorWizardCertificationIds.forEach { id ->
                    val sel = id in state.certifications
                    FilterChip(
                        selected = sel,
                        onClick = { onToggleCertification(id) },
                        label = { Text(text = stringResource(certificationLabelRes(id))) },
                    )
                }
            }
        }
        item {
            if ("other" in state.certifications) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    OutlinedTextField(
                        value = state.certificationOther,
                        onValueChange = onCertificationOtherChange,
                        label = { Text(text = stringResource(R.string.instructor_wizard_step4_other_placeholder)) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

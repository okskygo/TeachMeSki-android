package com.teachmeski.app.ui.profile.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailCertificationsSection(
    certifications: List<String>,
    certificationOther: String?,
) {
    val hasOther = !certificationOther.isNullOrBlank()
    val isEmpty = certifications.isEmpty() && !hasOther
    SectionCard {
        SectionLabel(text = stringResource(R.string.instructor_detail_certifications_label))
        if (isEmpty) {
            Text(
                text = stringResource(R.string.instructor_detail_no_certifications),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            return@SectionCard
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            certifications.forEach { cert -> CertChip(certDisplayLabel(cert)) }
            if (hasOther) {
                CertChip(stringResource(R.string.instructor_detail_cert_other_prefix) + certificationOther)
            }
        }
    }
}

@Composable
private fun certDisplayLabel(code: String): String = when (code) {
    "CSIA" -> stringResource(R.string.instructor_wizard_step4_cert_CSIA)
    "CASI" -> stringResource(R.string.instructor_wizard_step4_cert_CASI)
    "NZSIA" -> stringResource(R.string.instructor_wizard_step4_cert_NZSIA)
    "PSIA" -> stringResource(R.string.instructor_wizard_step4_cert_PSIA)
    "SIA_Japan", "SIA-Japan" -> stringResource(R.string.instructor_wizard_step4_cert_SIA_Japan)
    "other" -> stringResource(R.string.instructor_wizard_step4_cert_other)
    else -> code
}

@Composable
private fun CertChip(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

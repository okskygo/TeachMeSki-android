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
        Text(
            text = stringResource(R.string.instructor_detail_certifications_label),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
            certifications.forEach { cert -> CertChip(cert) }
            if (hasOther) {
                CertChip(stringResource(R.string.instructor_detail_cert_other_prefix) + certificationOther)
            }
        }
    }
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

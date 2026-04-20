package com.teachmeski.app.ui.profile.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.teachmeski.app.R

@Composable
fun DetailCertificatesSection(certificates: List<String>) {
    if (certificates.isEmpty()) return
    val shown = certificates.take(8)
    var lightboxIndex by remember { mutableStateOf<Int?>(null) }

    SectionCard {
        SectionLabel(text = stringResource(R.string.instructor_detail_certificates_title))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            itemsIndexed(shown) { idx, url ->
                Box(
                    modifier = Modifier
                        .width(176.dp)
                        .height(224.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { lightboxIndex = idx },
                ) {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.clip(RoundedCornerShape(14.dp)),
                    )
                }
            }
        }
    }

    lightboxIndex?.let { startIdx ->
        CertificateLightboxDialog(
            images = shown,
            initialIndex = startIdx,
            onDismiss = { lightboxIndex = null },
        )
    }
}

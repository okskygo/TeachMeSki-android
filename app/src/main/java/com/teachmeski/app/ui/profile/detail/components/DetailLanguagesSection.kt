package com.teachmeski.app.ui.profile.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R

@Composable
fun DetailLanguagesSection(languages: List<String>) {
    val labels = buildList {
        for (code in languages) {
            add(
                when (code) {
                    "zh" -> stringResource(R.string.instructor_detail_language_zh)
                    "en" -> stringResource(R.string.instructor_detail_language_en)
                    "ja" -> stringResource(R.string.instructor_detail_language_ja)
                    else -> code
                },
            )
        }
    }
    val separator = stringResource(R.string.instructor_detail_language_separator)
    SectionCard {
        Text(
            text = stringResource(R.string.instructor_detail_languages_label),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = labels.joinToString(separator),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

package com.teachmeski.app.ui.profile.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline

@Composable
fun DetailLevelsSection(
    discipline: Discipline,
    levels: List<Int>,
) {
    val sorted = levels.sorted()
    SectionCard {
        SectionLabel(text = stringResource(R.string.instructor_detail_levels_label))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            sorted.forEach { lv ->
                LevelRow(lv, discipline)
            }
        }
    }
}

@Composable
private fun LevelRow(level: Int, discipline: Discipline) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.instructor_detail_level_line_fmt, level),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .widthIn(min = 56.dp)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        )
        val descRes = descriptionRes(level, discipline)
        if (descRes != null) {
            Text(
                text = stringResource(descRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun descriptionRes(level: Int, discipline: Discipline): Int? {
    val isSnowboardOnly = discipline == Discipline.Snowboard
    return when {
        isSnowboardOnly -> when (level) {
            0 -> R.string.instructor_detail_level_desc_snowboard_0
            1 -> R.string.instructor_detail_level_desc_snowboard_1
            2 -> R.string.instructor_detail_level_desc_snowboard_2
            3 -> R.string.instructor_detail_level_desc_snowboard_3
            4 -> R.string.instructor_detail_level_desc_snowboard_4
            else -> null
        }
        else -> when (level) {
            0 -> R.string.instructor_detail_level_desc_ski_0
            1 -> R.string.instructor_detail_level_desc_ski_1
            2 -> R.string.instructor_detail_level_desc_ski_2
            3 -> R.string.instructor_detail_level_desc_ski_3
            4 -> R.string.instructor_detail_level_desc_ski_4
            else -> null
        }
    }
}

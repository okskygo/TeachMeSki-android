package com.teachmeski.app.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.LessonRequestDisplay
import com.teachmeski.app.ui.component.ExpandableText
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun LessonRequestFields(
    request: LessonRequestDisplay,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FieldRow(
            label = stringResource(R.string.chat_panel_discipline),
            value = stringResource(
                if (request.discipline.equals("snowboard", ignoreCase = true)) {
                    R.string.chat_badge_snowboard
                } else {
                    R.string.chat_badge_ski
                },
            ),
        )

        request.skillLevel?.let { level ->
            val levelRes = when (level) {
                0 -> R.string.skill_level_0
                1 -> R.string.skill_level_1
                2 -> R.string.skill_level_2
                3 -> R.string.skill_level_3
                4 -> R.string.skill_level_4
                else -> R.string.skill_level_4
            }
            FieldRow(
                label = stringResource(R.string.chat_panel_skill_level),
                value = stringResource(levelRes),
            )
        }

        val groupValue = if (request.hasChildren) {
            stringResource(R.string.chat_panel_group_with_children_fmt, request.groupSize)
        } else {
            stringResource(R.string.chat_panel_group_fmt, request.groupSize)
        }
        FieldRow(
            label = stringResource(R.string.chat_panel_group),
            value = groupValue,
        )

        val dateValue = when {
            request.datesFlexible -> stringResource(R.string.chat_panel_dates_flexible)
            request.dateStart != null && request.dateEnd != null ->
                "${request.dateStart} ~ ${request.dateEnd}"
            request.dateStart != null -> request.dateStart
            else -> null
        }
        dateValue?.let {
            FieldRow(label = stringResource(R.string.chat_panel_dates), value = it)
        }

        request.durationDays?.let { days ->
            val asString = if (days % 1.0 == 0.0) days.toInt().toString() else days.toString()
            FieldRow(
                label = stringResource(R.string.chat_panel_duration),
                value = stringResource(R.string.chat_panel_duration_fmt, asString),
            )
        }

        val resortsValue = when {
            request.allRegionsSelected -> stringResource(R.string.chat_panel_all_regions)
            request.resortNames.isNotEmpty() -> request.resortNames.joinToString("、")
            else -> null
        }
        resortsValue?.let {
            FieldRow(label = stringResource(R.string.chat_panel_resorts), value = it)
        }

        if (request.languages.isNotEmpty()) {
            FieldRow(
                label = stringResource(R.string.chat_panel_languages),
                value = request.languages.joinToString("、"),
            )
        }

        request.equipmentRental?.let { eq ->
            val res = when (eq.lowercase()) {
                "all" -> R.string.chat_panel_equipment_all
                "partial" -> R.string.chat_panel_equipment_partial
                "none" -> R.string.chat_panel_equipment_none
                else -> null
            }
            res?.let {
                FieldRow(
                    label = stringResource(R.string.chat_panel_equipment),
                    value = stringResource(it),
                )
            }
        }

        FieldRow(
            label = stringResource(R.string.chat_panel_transport),
            value = stringResource(
                if (request.needsTransport) R.string.chat_panel_transport_yes
                else R.string.chat_panel_transport_no,
            ),
        )

        if (request.certPrefs.isNotEmpty()) {
            FieldRow(
                label = stringResource(R.string.chat_panel_certs),
                value = request.certPrefs.joinToString("、"),
            )
        }

        if (!request.additionalNotes.isNullOrBlank()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.chat_panel_notes),
                    style = MaterialTheme.typography.labelMedium,
                    color = TmsColor.Outline,
                    modifier = Modifier.width(96.dp),
                )
                ExpandableText(
                    text = request.additionalNotes,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun FieldRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TmsColor.Outline,
            modifier = Modifier.width(96.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurface,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

package com.teachmeski.app.ui.chat

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // 1. Discipline
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

        // 2. Resorts — pill chips; collapse when > threshold
        when {
            request.allRegionsSelected -> {
                FieldRowBadge(
                    label = stringResource(R.string.chat_panel_resorts),
                    badgeText = stringResource(R.string.chat_panel_all_regions),
                )
            }
            request.resortNames.isNotEmpty() -> {
                ResortsRow(
                    label = stringResource(R.string.chat_panel_resorts),
                    names = request.resortNames,
                )
            }
        }

        // 3. Skill Level (short + desc below, both right-aligned)
        request.skillLevel?.let { level ->
            val short = stringResource(R.string.chat_panel_skill_level_short_fmt, level)
            val isSnowboard = request.discipline.equals("snowboard", ignoreCase = true)
            val descRes = when (level) {
                0 -> if (isSnowboard) R.string.wizard_level_snowboard_0 else R.string.wizard_level_ski_0
                1 -> if (isSnowboard) R.string.wizard_level_snowboard_1 else R.string.wizard_level_ski_1
                2 -> if (isSnowboard) R.string.wizard_level_snowboard_2 else R.string.wizard_level_ski_2
                3 -> if (isSnowboard) R.string.wizard_level_snowboard_3 else R.string.wizard_level_ski_3
                else -> if (isSnowboard) R.string.wizard_level_snowboard_4 else R.string.wizard_level_ski_4
            }
            SkillLevelRow(
                label = stringResource(R.string.chat_panel_skill_level),
                shortValue = short,
                desc = stringResource(descRes),
            )
        }

        // 4. Group Size
        val groupValue = if (request.hasChildren) {
            stringResource(R.string.chat_panel_group_with_children_fmt, request.groupSize)
        } else {
            stringResource(R.string.chat_panel_group_fmt, request.groupSize)
        }
        FieldRow(
            label = stringResource(R.string.chat_panel_group),
            value = groupValue,
        )

        // 5. Lesson Dates
        val dateValue = when {
            request.datesFlexible && request.dateStart == null ->
                stringResource(R.string.chat_panel_dates_undecided)
            request.datesFlexible && request.dateStart != null ->
                "${request.dateStart} " + stringResource(R.string.chat_panel_dates_flexible_suffix)
            request.dateStart != null && request.dateEnd != null && request.dateStart != request.dateEnd ->
                "${request.dateStart} – ${request.dateEnd}"
            request.dateStart != null -> request.dateStart
            request.dateEnd != null -> request.dateEnd
            else -> stringResource(R.string.chat_panel_dates_undecided)
        }
        FieldRow(label = stringResource(R.string.chat_panel_dates), value = dateValue)

        // 6. Duration
        request.durationDays?.let { days ->
            FieldRow(
                label = stringResource(R.string.chat_panel_duration),
                value = formatDuration(days),
            )
        }

        // 7. Language
        if (request.languages.isNotEmpty()) {
            val zhLabel = stringResource(R.string.wizard_lang_zh)
            val enLabel = stringResource(R.string.wizard_lang_en)
            val jaLabel = stringResource(R.string.wizard_lang_ja)
            val languageText = request.languages.joinToString("、") { code ->
                when (code.lowercase()) {
                    "zh" -> zhLabel
                    "en" -> enLabel
                    "ja" -> jaLabel
                    else -> code
                }
            }
            FieldRow(
                label = stringResource(R.string.chat_panel_languages),
                value = languageText,
            )
        }

        // 8. Equipment (only if value present; `none` still shown as "Bring my own" per Web)
        request.equipmentRental?.let { eq ->
            val res = when (eq.lowercase()) {
                "all" -> R.string.chat_panel_equipment_all
                "partial" -> R.string.chat_panel_equipment_partial
                "none" -> R.string.wizard_equipment_none
                else -> null
            }
            res?.let {
                FieldRow(
                    label = stringResource(R.string.chat_panel_equipment),
                    value = stringResource(it),
                )
            }
        }

        // 9. Transportation — only when needed (matches Web)
        if (request.needsTransport) {
            val base = stringResource(R.string.chat_panel_transport_yes)
            val note = request.transportNote?.trim().orEmpty()
            FieldRow(
                label = stringResource(R.string.chat_panel_transport),
                value = if (note.isNotEmpty()) "$base（$note）" else base,
            )
        }

        // 10. Cert prefs — only when non-empty (matches Web)
        if (request.certPrefs.isNotEmpty()) {
            FieldRow(
                label = stringResource(R.string.chat_panel_certs),
                value = request.certPrefs.joinToString("、"),
            )
        }

        // 11. Notes — divider above, stacked (label on top, content below full-width)
        if (!request.additionalNotes.isNullOrBlank() && request.additionalNotes != "—") {
            Spacer(Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(TmsColor.SurfaceVariant.copy(alpha = 0.3f)),
            )
            Spacer(Modifier.height(10.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.chat_panel_notes),
                    color = TmsColor.Outline,
                    fontSize = LabelFontSize,
                )
                Spacer(Modifier.height(4.dp))
                ExpandableText(
                    text = request.additionalNotes,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun formatDuration(days: Double): String {
    return when (days) {
        0.5 -> stringResource(R.string.chat_panel_duration_half_day)
        1.0 -> stringResource(R.string.chat_panel_duration_fmt, "1")
        1.5 -> stringResource(R.string.chat_panel_duration_fmt, "1.5")
        2.0 -> stringResource(R.string.chat_panel_duration_fmt, "2")
        3.0 -> stringResource(R.string.chat_panel_duration_fmt, "3")
        else -> {
            val asString = if (days % 1.0 == 0.0) days.toInt().toString() else days.toString()
            stringResource(R.string.chat_panel_duration_fmt, asString)
        }
    }
}

private val LabelFontSize = 13.sp
private val ValueFontSize = 15.sp
private const val RESORTS_COLLAPSE_THRESHOLD = 3

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = TmsColor.Outline,
        fontSize = LabelFontSize,
        modifier = Modifier.widthIn(min = 96.dp),
    )
}

@Composable
private fun FieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FieldLabel(label)
        Text(
            text = value,
            color = TmsColor.OnSurface,
            fontSize = ValueFontSize,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
        )
    }
}

@Composable
private fun FieldRowBadge(label: String, badgeText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FieldLabel(label)
        Box(modifier = Modifier.weight(1f, fill = true)) {
            ResortPill(
                text = badgeText,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

@Composable
private fun SkillLevelRow(label: String, shortValue: String, desc: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        FieldRow(label = label, value = shortValue)
        if (desc.isNotBlank()) {
            Text(
                text = desc,
                color = TmsColor.OnSurfaceVariant,
                fontSize = 13.sp,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun ResortsRow(label: String, names: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    val shouldCollapse = names.size > RESORTS_COLLAPSE_THRESHOLD

    Column(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        if (!shouldCollapse) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FieldLabel(label)
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    names.forEach { ResortPill(text = it) }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FieldLabel(label)
                Row(
                    modifier = Modifier.weight(1f, fill = true),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = stringResource(R.string.chat_panel_resort_count_fmt, names.size),
                        color = TmsColor.Primary,
                        fontSize = ValueFontSize,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.widthIn(min = 2.dp))
                    Icon(
                        imageVector = Icons.Filled.ExpandMore,
                        contentDescription = null,
                        tint = TmsColor.Primary,
                        modifier = Modifier
                            .height(16.dp)
                            .rotate(if (expanded) 180f else 0f),
                    )
                }
            }
            if (expanded) {
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    names.forEach { ResortPill(text = it) }
                }
            }
        }
    }
}

@Composable
private fun ResortPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(TmsColor.Primary.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            color = TmsColor.Primary,
            fontSize = 13.sp,
        )
    }
}

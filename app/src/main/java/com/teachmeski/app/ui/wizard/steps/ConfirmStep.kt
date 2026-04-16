package com.teachmeski.app.ui.wizard.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.EquipmentRental
import com.teachmeski.app.ui.wizard.WizardUiState
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ConfirmStep(
    state: WizardUiState,
    onEditStep: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val resortValue = resortSummaryText(state)
    val groupValue = groupSummaryText(state)
    val levelValue = skillLevelSummaryText(state)
    val datesValue = datesSummaryText(state)
    val durationValue = durationSummaryText(state)
    val languagesValue = languagesSummaryText(state)
    val preferencesValue = preferencesSummaryText(state)
    val notesValue =
        state.additionalNotes.trim().ifEmpty {
            stringResource(R.string.common_empty_value)
        }

    LazyColumn(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            ConfirmSummaryRow(
                label = stringResource(R.string.wizard_confirm_label_resort),
                value = resortValue,
                step = 1,
                onEditStep = onEditStep,
            )
        }
        item {
            ConfirmSummaryRow(
                label = stringResource(R.string.wizard_confirm_label_group),
                value = groupValue,
                step = 2,
                onEditStep = onEditStep,
            )
        }
        item {
            ConfirmSummaryRow(
                label = stringResource(R.string.wizard_confirm_label_level),
                value = levelValue,
                step = 3,
                onEditStep = onEditStep,
            )
        }
        item {
            ConfirmSummaryRow(
                label = stringResource(R.string.wizard_confirm_label_date),
                value = datesValue,
                step = 4,
                onEditStep = onEditStep,
            )
        }
        item {
            ConfirmSummaryRow(
                label = stringResource(R.string.wizard_confirm_label_duration),
                value = durationValue,
                step = 5,
                onEditStep = onEditStep,
            )
        }
        item {
            ConfirmSummaryRow(
                label = stringResource(R.string.wizard_confirm_label_language),
                value = languagesValue,
                step = 6,
                onEditStep = onEditStep,
            )
        }
        item {
            ConfirmSummaryRow(
                label = stringResource(R.string.wizard_confirm_label_preferences),
                value = preferencesValue,
                step = 7,
                onEditStep = onEditStep,
            )
        }
        item {
            ConfirmSummaryRow(
                label = stringResource(R.string.wizard_confirm_label_notes),
                value = notesValue,
                step = 8,
                onEditStep = onEditStep,
            )
        }
    }
}

@Composable
private fun ConfirmSummaryRow(
    label: String,
    value: String,
    step: Int,
    onEditStep: (Int) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        IconButton(onClick = { onEditStep(step) }) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.common_edit),
            )
        }
    }
}

@Composable
private fun resortSummaryText(state: WizardUiState): String {
    if (state.allRegionsSelected) {
        return stringResource(R.string.wizard_resort_all_regions)
    }
    val names =
        state.regions
            .flatMap { it.resorts }
            .filter { it.id in state.selectedResortIds }
            .map { "${it.nameZh} (${it.nameEn})" }
    return if (names.isEmpty()) {
        stringResource(R.string.common_empty_value)
    } else {
        names.joinToString("\n")
    }
}

@Composable
private fun groupSummaryText(state: WizardUiState): String {
    val discipline =
        when (state.discipline) {
            Discipline.Ski -> stringResource(R.string.wizard_discipline_ski)
            Discipline.Snowboard -> stringResource(R.string.wizard_discipline_snowboard)
            Discipline.Both -> stringResource(R.string.wizard_discipline_both)
        }
    val people = stringResource(R.string.wizard_confirm_people)
    val children =
        if (state.hasChildren) {
            stringResource(R.string.wizard_confirm_with_children)
        } else {
            ""
        }
    return "$discipline, ${state.groupSize} $people$children"
}

@Composable
private fun skillLevelSummaryText(state: WizardUiState): String {
    val prefix = stringResource(R.string.wizard_confirm_level_prefix)
    val descRes = skillLevelDescriptionRes(state.discipline, state.skillLevel)
    val desc = stringResource(descRes)
    return "$prefix${state.skillLevel} — $desc"
}

private fun skillLevelDescriptionRes(
    discipline: Discipline,
    level: Int,
): Int {
    val n = level.coerceIn(0, 4)
    return when (discipline) {
        Discipline.Ski,
        Discipline.Both,
        ->
            when (n) {
                0 -> R.string.wizard_level_ski_0
                1 -> R.string.wizard_level_ski_1
                2 -> R.string.wizard_level_ski_2
                3 -> R.string.wizard_level_ski_3
                else -> R.string.wizard_level_ski_4
            }
        Discipline.Snowboard ->
            when (n) {
                0 -> R.string.wizard_level_snowboard_0
                1 -> R.string.wizard_level_snowboard_1
                2 -> R.string.wizard_level_snowboard_2
                3 -> R.string.wizard_level_snowboard_3
                else -> R.string.wizard_level_snowboard_4
            }
    }
}

@Composable
private fun datesSummaryText(state: WizardUiState): String {
    if (state.datesFlexible) {
        return stringResource(R.string.wizard_confirm_dates_undecided)
    }
    val start = state.dateStart
    if (start.isNullOrBlank()) {
        return stringResource(R.string.wizard_confirm_dates_undecided)
    }
    val locale = LocalConfiguration.current.locales[0]
    val fmtIn = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val fmtOut =
        remember(locale) {
            SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, locale)
        }
    return try {
        val startDate = fmtIn.parse(start) ?: return start
        val startLabel = fmtOut.format(startDate)
        val endIso = state.dateEnd?.takeIf { it.isNotBlank() } ?: start
        if (endIso == start) {
            startLabel
        } else {
            val endDate = fmtIn.parse(endIso) ?: return "$startLabel – $endIso"
            val endLabel = fmtOut.format(endDate)
            "$startLabel – $endLabel"
        }
    } catch (_: Exception) {
        val endIso = state.dateEnd?.takeIf { it.isNotBlank() }
        if (endIso != null && endIso != start) {
            "$start – $endIso"
        } else {
            start
        }
    }
}

@Composable
private fun durationSummaryText(state: WizardUiState): String {
    if (state.durationDays == 0.5) {
        return stringResource(R.string.wizard_confirm_half_day)
    }
    val daysWord = stringResource(R.string.wizard_confirm_days)
    val n = state.durationDays
    val numStr =
        if (kotlin.math.abs(n - n.toInt().toDouble()) < 1e-6) {
            n.toInt().toString()
        } else {
            n.toString()
        }
    return "$numStr $daysWord"
}

@Composable
private fun languagesSummaryText(state: WizardUiState): String {
    val parts =
        state.languages.sorted().map { code ->
            stringResource(
                when (code) {
                    "zh" -> R.string.wizard_lang_zh
                    "en" -> R.string.wizard_lang_en
                    "ja" -> R.string.wizard_lang_ja
                    else -> R.string.wizard_lang_en
                },
            )
        }
    return if (parts.isEmpty()) {
        stringResource(R.string.common_empty_value)
    } else {
        parts.joinToString(", ")
    }
}

@Composable
private fun preferencesSummaryText(state: WizardUiState): String {
    val equipment =
        stringResource(
            when (state.equipmentRental) {
                EquipmentRental.All -> R.string.wizard_equipment_all
                EquipmentRental.Partial -> R.string.wizard_equipment_partial
                EquipmentRental.None -> R.string.wizard_equipment_none
            },
        )
    val transport =
        when (state.needsTransport) {
            true -> stringResource(R.string.wizard_transport_yes)
            false -> stringResource(R.string.wizard_transport_no)
            null -> stringResource(R.string.common_empty_value)
        }
    val note = state.transportNote.trim()
    val transportPart =
        if (state.needsTransport == true && note.isNotEmpty()) {
            "$transport ($note)"
        } else {
            transport
        }
    val certs =
        state.certPreferences.sorted().joinToString(", ").ifEmpty {
            stringResource(R.string.common_empty_value)
        }
    return listOf(equipment, transportPart, certs).joinToString("\n")
}

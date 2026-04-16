package com.teachmeski.app.ui.myrequests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.EquipmentRental
import com.teachmeski.app.domain.model.InstructorPreview
import com.teachmeski.app.domain.model.LessonRequest
import com.teachmeski.app.domain.model.LessonRequestStatus
import com.teachmeski.app.ui.component.EmptyState
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.util.UiText
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RequestDetailScreen(
    onBack: () -> Unit,
    onChatClick: (String) -> Unit,
    onInstructorClick: (String) -> Unit,
    viewModel: RequestDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showCloseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.closeSuccess) {
        if (state.closeSuccess) {
            snackbarHostState.showSnackbar(context.getString(R.string.request_detail_close_success))
            viewModel.consumeCloseSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TmsTopBar(
                title = stringResource(R.string.request_detail_screen_title),
                onBack = onBack,
            )
        },
    ) { padding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            when {
                state.isLoading && state.detail == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null && state.detail == null -> {
                    val err = state.error
                    EmptyState(
                        title = stringResource(R.string.error_load_request_detail),
                        description = err?.asString().orEmpty(),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                state.detail != null -> {
                    RequestDetailContent(
                        detail = state.detail!!,
                        unlocked = state.unlockedInstructors,
                        recommended = state.recommendedInstructors,
                        isClosing = state.isClosing,
                        loadError = state.error,
                        onChatClick = onChatClick,
                        onInstructorClick = onInstructorClick,
                        onCloseClick = { showCloseDialog = true },
                    )
                }
            }
        }
    }

    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text(text = stringResource(R.string.request_detail_close_dialog_title)) },
            text = { Text(text = stringResource(R.string.request_detail_close_dialog_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCloseDialog = false
                        viewModel.close()
                    },
                    enabled = !state.isClosing,
                ) {
                    Text(text = stringResource(R.string.request_detail_close_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) {
                    Text(text = stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun RequestDetailContent(
    detail: LessonRequest,
    unlocked: List<InstructorPreview>,
    recommended: List<InstructorPreview>,
    isClosing: Boolean,
    loadError: UiText?,
    onChatClick: (String) -> Unit,
    onInstructorClick: (String) -> Unit,
    onCloseClick: () -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RequestInfoCard(detail = detail)
        }
        item {
            StatusAndCloseSection(
                detail = detail,
                isClosing = isClosing,
                onCloseClick = onCloseClick,
            )
        }
        if (loadError != null) {
            item {
                Text(
                    text = loadError.asString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.request_detail_section_user_initiated),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (unlocked.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.request_detail_no_user_initiated_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        } else {
            items(unlocked, key = { it.instructorId }) { preview ->
                UnlockedInstructorCard(
                    preview = preview,
                    onChatClick = onChatClick,
                    onInstructorClick = onInstructorClick,
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.request_detail_recommended_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (recommended.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.request_detail_recommended_none),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        } else {
            items(recommended, key = { it.instructorId }) { preview ->
                RecommendedInstructorCard(
                    preview = preview,
                    onInstructorClick = onInstructorClick,
                )
            }
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun RequestInfoCard(detail: LessonRequest) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DetailRow(
                label = stringResource(R.string.request_detail_discipline_label),
                value = disciplineLabel(detail.discipline),
            )
            DetailRow(
                label = stringResource(R.string.request_detail_level_label),
                value = skillLevelSummary(detail.discipline, detail.skillLevel),
            )
            DetailRow(
                label = stringResource(R.string.request_detail_group_size_label),
                value = groupSummary(detail),
            )
            DetailRow(
                label = stringResource(R.string.request_detail_lesson_date_label),
                value = datesSummary(detail),
            )
            DetailRow(
                label = stringResource(R.string.request_detail_duration_label),
                value = durationSummary(detail.durationDays),
            )
            DetailRow(
                label = stringResource(R.string.request_detail_resort_label),
                value = resortSummary(detail),
            )
            DetailRow(
                label = stringResource(R.string.request_detail_language_label),
                value = languagesSummary(detail.languages),
            )
            DetailRow(
                label = stringResource(R.string.wizard_equipment_label),
                value = equipmentSummary(detail.equipmentRental),
            )
            DetailRow(
                label = stringResource(R.string.wizard_transport_label),
                value = transportSummary(detail),
            )
            DetailRow(
                label = stringResource(R.string.wizard_cert_label),
                value = certsSummary(detail.certPreferences),
            )
            DetailRow(
                label = stringResource(R.string.request_detail_description_label),
                value =
                    detail.additionalNotes?.trim().takeUnless { it.isNullOrEmpty() }
                        ?: stringResource(R.string.common_empty_value),
            )
            DetailRow(
                label = stringResource(R.string.request_detail_created_at_label),
                value = formatCreatedAt(detail.createdAt),
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
}

@Composable
private fun disciplineLabel(discipline: Discipline): String =
    stringResource(
        when (discipline) {
            Discipline.Ski -> R.string.wizard_discipline_ski
            Discipline.Snowboard -> R.string.wizard_discipline_snowboard
        },
    )

@Composable
private fun skillLevelSummary(
    discipline: Discipline,
    skillLevel: Int,
): String {
    val lvl = skillLevel.coerceIn(0, 4)
    val prefix = stringResource(R.string.wizard_confirm_level_prefix)
    val descRes = skillLevelDescriptionRes(discipline, lvl)
    val desc = stringResource(descRes)
    return "$prefix$lvl — $desc"
}

private fun skillLevelDescriptionRes(
    discipline: Discipline,
    level: Int,
): Int {
    val n = level.coerceIn(0, 4)
    return when (discipline) {
        Discipline.Ski ->
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
private fun groupSummary(detail: LessonRequest): String {
    val discipline = disciplineLabel(detail.discipline)
    val people = stringResource(R.string.wizard_confirm_people)
    val children =
        if (detail.hasChildren) {
            stringResource(R.string.wizard_confirm_with_children)
        } else {
            ""
        }
    return "$discipline, ${detail.groupSize} $people$children"
}

@Composable
private fun datesSummary(detail: LessonRequest): String {
    if (detail.datesFlexible) {
        return stringResource(R.string.request_detail_dates_undecided) +
            stringResource(R.string.request_detail_dates_flexible_suffix)
    }
    val start = detail.dateStart
    if (start.isNullOrBlank()) {
        return stringResource(R.string.request_detail_dates_undecided)
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
        val endIso = detail.dateEnd?.takeIf { it.isNotBlank() } ?: start
        if (endIso == start) {
            startLabel
        } else {
            val endDate = fmtIn.parse(endIso) ?: return "$startLabel – $endIso"
            val endLabel = fmtOut.format(endDate)
            "$startLabel – $endLabel"
        }
    } catch (_: Exception) {
        val endIso = detail.dateEnd?.takeIf { it.isNotBlank() }
        if (endIso != null && endIso != start) {
            "$start – $endIso"
        } else {
            start
        }
    }
}

@Composable
private fun durationSummary(durationDays: Double): String {
    if (durationDays == 0.5) {
        return stringResource(R.string.wizard_confirm_half_day)
    }
    val daysWord = stringResource(R.string.wizard_confirm_days)
    val n = durationDays
    val numStr =
        if (kotlin.math.abs(n - n.toInt().toDouble()) < 1e-6) {
            n.toInt().toString()
        } else {
            n.toString()
        }
    return "$numStr $daysWord"
}

@Composable
private fun resortSummary(detail: LessonRequest): String {
    if (detail.allRegionsSelected) {
        return stringResource(R.string.wizard_resort_all_regions)
    }
    return if (detail.resortNames.isEmpty()) {
        stringResource(R.string.common_empty_value)
    } else {
        detail.resortNames.joinToString("\n")
    }
}

@Composable
private fun languagesSummary(codes: List<String>): String {
    val parts =
        codes.sorted().map { code ->
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
private fun equipmentSummary(rental: EquipmentRental): String =
    stringResource(
        when (rental) {
            EquipmentRental.All -> R.string.wizard_equipment_all
            EquipmentRental.Partial -> R.string.wizard_equipment_partial
            EquipmentRental.None -> R.string.wizard_equipment_none
        },
    )

@Composable
private fun transportSummary(detail: LessonRequest): String {
    val transport =
        if (detail.needsTransport) {
            stringResource(R.string.wizard_transport_yes)
        } else {
            stringResource(R.string.wizard_transport_no)
        }
    val note = detail.transportNote?.trim().orEmpty()
    return if (detail.needsTransport && note.isNotEmpty()) {
        "$transport ($note)"
    } else {
        transport
    }
}

@Composable
private fun certsSummary(prefs: List<String>): String =
    prefs.sorted().joinToString(", ").ifEmpty {
        stringResource(R.string.common_empty_value)
    }

@Composable
private fun formatCreatedAt(iso: String): String {
    if (iso.isBlank()) return stringResource(R.string.common_empty_value)
    val locale = LocalConfiguration.current.locales[0]
    val parsers =
        listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
            SimpleDateFormat("yyyy-MM-dd", Locale.US),
        )
    for (p in parsers) {
        try {
            val d = p.parse(iso) ?: continue
            val fmtOut = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.SHORT, locale)
            return fmtOut.format(d)
        } catch (_: Exception) {
            continue
        }
    }
    return iso
}

@Composable
private fun StatusAndCloseSection(
    detail: LessonRequest,
    isClosing: Boolean,
    onCloseClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val statusText =
            stringResource(
                when (detail.status) {
                    LessonRequestStatus.Active -> R.string.my_requests_status_active
                    LessonRequestStatus.Expired -> R.string.my_requests_status_expired
                    LessonRequestStatus.ClosedByUser -> R.string.my_requests_status_closed
                },
            )
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (detail.status == LessonRequestStatus.Active) {
            OutlinedButton(
                onClick = onCloseClick,
                enabled = !isClosing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    if (isClosing) {
                        CircularProgressIndicator(
                            modifier =
                                Modifier
                                    .size(18.dp)
                                    .padding(end = 8.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                    Text(text = stringResource(R.string.request_detail_close_button))
                }
            }
        }
    }
}

@Composable
private fun UnlockedInstructorCard(
    preview: InstructorPreview,
    onChatClick: (String) -> Unit,
    onInstructorClick: (String) -> Unit,
) {
    val shortId = preview.shortId.orEmpty()
    val subtitle =
        if (preview.hasUnread) {
            stringResource(R.string.request_detail_go_to_chat)
        } else {
            stringResource(R.string.request_detail_last_message_empty)
        }
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (shortId.isNotEmpty()) {
                        Modifier.clickable { onInstructorClick(shortId) }
                    } else {
                        Modifier
                    },
                ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            UserAvatar(
                displayName = preview.displayName,
                avatarUrl = preview.avatarUrl,
                size = 48.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preview.displayName ?: stringResource(R.string.common_empty_value),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val roomId = preview.roomId
            if (!roomId.isNullOrBlank()) {
                TextButton(onClick = { onChatClick(roomId) }) {
                    Text(text = stringResource(R.string.request_detail_go_to_chat))
                }
            }
        }
    }
}

@Composable
private fun RecommendedInstructorCard(
    preview: InstructorPreview,
    onInstructorClick: (String) -> Unit,
) {
    val shortId = preview.shortId.orEmpty()
    val ratingLabel = stringResource(R.string.request_detail_recommended_rating)
    val ratingPart =
        preview.ratingAvg?.let { avg ->
            val c = preview.ratingCount
            "$ratingLabel: ${"%.1f".format(avg)} ($c)"
        }
    val phoneLine =
        stringResource(
            if (preview.phoneVerifiedAt != null) {
                R.string.request_detail_phone_verified
            } else {
                R.string.request_detail_phone_unverified
            },
        )
    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (shortId.isNotEmpty()) {
                        Modifier.clickable { onInstructorClick(shortId) }
                    } else {
                        Modifier
                    },
                ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            UserAvatar(
                displayName = preview.displayName,
                avatarUrl = preview.avatarUrl,
                size = 48.dp,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = preview.displayName ?: stringResource(R.string.common_empty_value),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = phoneLine,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (ratingPart != null) {
                    Text(
                        text = ratingPart,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (shortId.isNotEmpty()) {
                Button(
                    onClick = { onInstructorClick(shortId) },
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Text(text = stringResource(R.string.request_detail_recommended_contact))
                }
            }
        }
    }
}

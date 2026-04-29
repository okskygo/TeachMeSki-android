package com.teachmeski.app.ui.myrequests

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.EquipmentRental
import com.teachmeski.app.domain.model.InstructorPreview
import com.teachmeski.app.domain.model.LessonRequest
import com.teachmeski.app.domain.model.LessonRequestStatus
import com.teachmeski.app.ui.component.EmptyState
import com.teachmeski.app.ui.component.IdentityVerifiedBadge
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor
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
    var showExpandConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.closeSuccess) {
        if (state.closeSuccess) {
            snackbarHostState.showSnackbar(context.getString(R.string.request_detail_close_success))
            viewModel.consumeCloseSuccess()
        }
    }

    LaunchedEffect(state.firstMessageRoomId) {
        val roomId = state.firstMessageRoomId
        if (roomId != null) {
            onChatClick(roomId)
            viewModel.consumeFirstMessageRoomId()
        }
    }

    val expandToast = state.expandQuotaToast
    LaunchedEffect(expandToast) {
        if (expandToast != null) {
            val message = when (expandToast) {
                is UiText.StringResource -> context.getString(
                    expandToast.resId,
                    *expandToast.args.toTypedArray(),
                )
                is UiText.DynamicString -> expandToast.value
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.consumeExpandQuotaToast()
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                state.isLoading && state.detail == null -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = TmsColor.Primary,
                    )
                }
                state.error != null && state.detail == null -> {
                    EmptyState(
                        title = stringResource(R.string.error_load_request_detail),
                        description = state.error?.asString().orEmpty(),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                state.detail != null -> {
                    RequestDetailContent(
                        detail = state.detail!!,
                        userInitiated = state.userInitiatedInstructors,
                        expertInitiated = state.expertInitiatedInstructors,
                        recommended = state.recommendedInstructors,
                        isClosing = state.isClosing,
                        loadError = state.error,
                        isExpandingQuota = state.isExpandingQuota,
                        onChatClick = onChatClick,
                        onInstructorClick = onInstructorClick,
                        onCloseClick = { showCloseDialog = true },
                        onContactInstructor = viewModel::openFirstMessageDialog,
                        onFindMoreClick = { showExpandConfirm = true },
                    )
                }
            }
        }
    }

    val firstMessageTarget = state.firstMessageTarget
    if (firstMessageTarget != null) {
        com.teachmeski.app.ui.chat.FirstMessageDialog(
            instructorName = firstMessageTarget.displayName.orEmpty(),
            instructorAvatarUrl = firstMessageTarget.avatarUrl,
            messageDraft = state.firstMessageDraft,
            isSending = state.isSendingFirstMessage,
            onDraftChange = viewModel::updateFirstMessageDraft,
            onDismiss = viewModel::dismissFirstMessageDialog,
            onSend = viewModel::sendFirstMessage,
        )
    }

    if (showCloseDialog) {
        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text(stringResource(R.string.request_detail_close_dialog_title)) },
            text = { Text(stringResource(R.string.request_detail_close_dialog_desc)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCloseDialog = false
                        viewModel.close()
                    },
                    enabled = !state.isClosing,
                ) {
                    Text(stringResource(R.string.request_detail_close_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }

    if (showExpandConfirm) {
        AlertDialog(
            onDismissRequest = { showExpandConfirm = false },
            title = { Text(stringResource(R.string.my_requests_find_more_confirm_title)) },
            text = { Text(stringResource(R.string.my_requests_find_more_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExpandConfirm = false
                        viewModel.expandQuota()
                    },
                    enabled = !state.isExpandingQuota,
                ) {
                    Text(stringResource(R.string.my_requests_find_more_confirm_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpandConfirm = false }) {
                    Text(stringResource(R.string.my_requests_find_more_confirm_no))
                }
            },
        )
    }
}

private const val RECOMMENDED_PAGE_SIZE = 6

@Composable
private fun RequestDetailContent(
    detail: LessonRequest,
    userInitiated: List<InstructorPreview>,
    expertInitiated: List<InstructorPreview>,
    recommended: List<InstructorPreview>,
    isClosing: Boolean,
    loadError: UiText?,
    isExpandingQuota: Boolean,
    onChatClick: (String) -> Unit,
    onInstructorClick: (String) -> Unit,
    onCloseClick: () -> Unit,
    onContactInstructor: (InstructorPreview) -> Unit,
    onFindMoreClick: () -> Unit,
) {
    var visibleRecommendedCount by remember { mutableStateOf(RECOMMENDED_PAGE_SIZE) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // F-109 v1.1: quota expansion guidance card — placed at the very top
        // so the student sees it immediately when entering the detail page.
        // Renders nothing unless conditions are met (no disabled state).
        // F-109 §7.4: card visible only when unlock_count has caught up to
        // quota_limit. Note that students never see status='locked' — the
        // Android `LessonRequestStatus.fromString` decoder folds unknown
        // values to .Active (F-110 §1.1 dual-semantics rule), so a single
        // `== Active` check here is correct.
        if (
            detail.status == LessonRequestStatus.Active &&
            detail.unlockCount >= detail.quotaLimit
        ) {
            item {
                FindMoreInstructorsCard(
                    unlockCount = detail.unlockCount,
                    isLoading = isExpandingQuota,
                    onClick = onFindMoreClick,
                )
            }
        }

        item {
            OrderInfoCard(
                detail = detail,
                isClosing = isClosing,
                onCloseClick = onCloseClick,
            )
        }

        if (loadError != null) {
            item {
                Text(
                    text = loadError.asString(),
                    color = TmsColor.Error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        item {
            SectionLabel(text = stringResource(R.string.request_detail_section_user_initiated))
        }
        if (userInitiated.isEmpty()) {
            item {
                EmptySection(text = stringResource(R.string.request_detail_no_user_initiated_yet))
            }
        } else {
            items(userInitiated, key = { "u_" + it.instructorId }) { preview ->
                UnlockedInstructorCard(
                    preview = preview,
                    onChatClick = onChatClick,
                    onInstructorClick = onInstructorClick,
                )
            }
        }

        item {
            SectionLabel(text = stringResource(R.string.request_detail_section_expert_initiated))
        }
        if (expertInitiated.isEmpty()) {
            item {
                EmptySection(text = stringResource(R.string.request_detail_no_expert_initiated))
            }
        } else {
            items(expertInitiated, key = { "e_" + it.instructorId }) { preview ->
                UnlockedInstructorCard(
                    preview = preview,
                    onChatClick = onChatClick,
                    onInstructorClick = onInstructorClick,
                )
            }
        }

        item {
            SectionLabel(text = stringResource(R.string.request_detail_recommended_title))
        }
        if (recommended.isEmpty()) {
            item {
                EmptySection(text = stringResource(R.string.request_detail_recommended_none))
            }
        } else {
            val visible = recommended.take(visibleRecommendedCount)
            items(visible, key = { "r_" + it.instructorId }) { preview ->
                RecommendedInstructorCard(
                    preview = preview,
                    onInstructorClick = onInstructorClick,
                    onContactClick = { onContactInstructor(preview) },
                )
            }
            if (visibleRecommendedCount < recommended.size) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        OutlinedButton(
                            onClick = {
                                visibleRecommendedCount =
                                    (visibleRecommendedCount + RECOMMENDED_PAGE_SIZE)
                                        .coerceAtMost(recommended.size)
                            },
                            shape = CircleShape,
                            border = BorderStroke(1.dp, TmsColor.Primary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TmsColor.Primary,
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.request_detail_load_more),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

/**
 * F-109 v1.1 quota expansion guidance card.
 *
 * Layout: rounded surface with a Search icon container on the left,
 * followed by title + dynamic subtitle, then a full-width primary CTA button
 * underneath. Rendered at the top of [RequestDetailScreen] only when
 * `unlock_count >= quota_limit && status='active'`. Per F-109 §1.1, no
 * "quota / 名額" wording leaks to the student — the subtitle phrases the
 * count as "已收到 N 位教練的回覆".
 */
@Composable
private fun FindMoreInstructorsCard(
    unlockCount: Int,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = TmsColor.PrimaryFixed.copy(alpha = 0.30f),
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(TmsColor.PrimaryFixed),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = TmsColor.Primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = stringResource(R.string.my_requests_find_more_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TmsColor.OnSurface,
                    )
                    Text(
                        text = stringResource(
                            R.string.my_requests_find_more_card_subtitle,
                            unlockCount,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TmsColor.OnSurfaceVariant,
                    )
                }
            }

            Button(
                onClick = onClick,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TmsColor.Primary,
                    contentColor = TmsColor.OnPrimary,
                ),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TmsColor.OnPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.my_requests_find_more_cta),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = TmsColor.Outline,
        letterSpacing = 2.sp,
    )
}

@Composable
private fun EmptySection(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TmsColor.SurfaceLow, RoundedCornerShape(16.dp))
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OrderInfoCard(
    detail: LessonRequest,
    isClosing: Boolean,
    onCloseClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = TmsColor.SurfaceLowest,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = disciplineLabel(detail.discipline),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TmsColor.OnSurface,
                    )
                    DetailStatusPill(status = detail.status)
                }

                if (detail.status == LessonRequestStatus.Active) {
                    OutlinedButton(
                        onClick = onCloseClick,
                        enabled = !isClosing,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, TmsColor.Primary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TmsColor.Primary,
                        ),
                        contentPadding = ButtonDefaults.ContentPadding,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.request_detail_close_button),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = TmsColor.SurfaceVariant.copy(alpha = 0.4f),
            )

            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Landscape,
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                        .padding(top = 2.dp),
                    tint = TmsColor.Primary,
                )
                Column {
                    Text(
                        text = stringResource(R.string.request_detail_resort_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TmsColor.Outline,
                        letterSpacing = 1.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (detail.allRegionsSelected) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = TmsColor.Primary.copy(alpha = 0.1f),
                        ) {
                            Text(
                                text = stringResource(R.string.wizard_resort_all_regions),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TmsColor.Primary,
                            )
                        }
                    } else if (detail.resortNames.isNotEmpty()) {
                        ResortTagsCollapsible(names = detail.resortNames)
                    }
                }
            }

            InfoRow(
                icon = Icons.Outlined.CalendarMonth,
                label = stringResource(R.string.request_detail_lesson_date_label),
                value = datesSummary(detail),
            )
            InfoRow(
                iconRes = when (detail.discipline) {
                    Discipline.Snowboard -> R.drawable.ic_snowboard
                    Discipline.Ski,
                    Discipline.Both,
                    -> R.drawable.ic_ski
                },
                label = stringResource(R.string.request_detail_discipline_label),
                value = disciplineLabel(detail.discipline),
            )
            InfoRow(
                icon = Icons.Outlined.TrendingUp,
                label = stringResource(R.string.request_detail_level_label),
                value = skillLevelSummary(detail.discipline, detail.skillLevel),
            )
            InfoRow(
                icon = Icons.Outlined.Groups,
                label = stringResource(R.string.request_detail_group_size_label),
                value = groupSummary(detail),
            )
            InfoRow(
                icon = Icons.Outlined.Schedule,
                label = stringResource(R.string.request_detail_duration_label),
                value = durationSummary(detail.durationDays),
            )
            InfoRow(
                icon = Icons.Outlined.Translate,
                label = stringResource(R.string.request_detail_language_label),
                value = languagesSummary(detail.languages),
            )
            InfoRow(
                icon = Icons.Outlined.Inventory2,
                label = stringResource(R.string.request_detail_equipment_label),
                value = equipmentSummary(detail.equipmentRental),
            )
            InfoRow(
                icon = Icons.Outlined.DirectionsCar,
                label = stringResource(R.string.request_detail_transport_label),
                value = transportSummary(detail.needsTransport, detail.transportNote),
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = TmsColor.SurfaceVariant.copy(alpha = 0.4f),
            )

            CertPrefsRow(certPreferences = detail.certPreferences)

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = TmsColor.SurfaceVariant.copy(alpha = 0.4f),
            )

            InfoRow(
                icon = Icons.Outlined.Description,
                label = stringResource(R.string.request_detail_description_label),
                value = detail.additionalNotes?.trim().takeUnless { it.isNullOrEmpty() }
                    ?: stringResource(R.string.common_empty_value),
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CertPrefsRow(certPreferences: List<String>) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.EmojiEvents,
            contentDescription = null,
            modifier = Modifier
                .size(14.dp)
                .padding(top = 2.dp),
            tint = TmsColor.Primary,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.request_detail_cert_label).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TmsColor.Outline,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(6.dp))
            if (certPreferences.isEmpty()) {
                Text(
                    text = stringResource(R.string.request_detail_cert_none_value),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TmsColor.OnSurface,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    certPreferences.forEach { code ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = TmsColor.PrimaryFixed.copy(alpha = 0.4f),
                        ) {
                            Text(
                                text = certLabel(code),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = TmsColor.Primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun certLabel(code: String): String = when (code) {
    "CSIA" -> stringResource(R.string.wizard_cert_csia)
    "CASI" -> stringResource(R.string.wizard_cert_casi)
    "NZSIA" -> stringResource(R.string.wizard_cert_nzsia)
    "PSIA" -> stringResource(R.string.wizard_cert_psia)
    "SIA_Japan", "SIA-Japan" -> stringResource(R.string.wizard_cert_sia_japan)
    "other" -> stringResource(R.string.wizard_cert_other)
    else -> code
}

@Composable
private fun equipmentSummary(rental: EquipmentRental): String = stringResource(
    when (rental) {
        EquipmentRental.All -> R.string.wizard_equipment_all
        EquipmentRental.Partial -> R.string.wizard_equipment_partial
        EquipmentRental.None -> R.string.wizard_equipment_none
    },
)

@Composable
private fun transportSummary(needs: Boolean, note: String?): String {
    val trimmed = note?.trim().orEmpty()
    return if (needs) {
        if (trimmed.isNotEmpty()) {
            "${stringResource(R.string.wizard_transport_yes)}（$trimmed）"
        } else {
            stringResource(R.string.wizard_transport_yes)
        }
    } else {
        stringResource(R.string.wizard_transport_no)
    }
}

private const val RESORT_VISIBLE_THRESHOLD = 5

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResortTagsCollapsible(names: List<String>) {
    var expanded by remember { mutableStateOf(false) }
    val showToggle = names.size > RESORT_VISIBLE_THRESHOLD
    val visible = if (expanded || !showToggle) names else names.take(RESORT_VISIBLE_THRESHOLD)
    val remaining = names.size - RESORT_VISIBLE_THRESHOLD

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        visible.forEach { name ->
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = TmsColor.SurfaceLow,
            ) {
                Text(
                    text = name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TmsColor.OnSurface,
                )
            }
        }
        if (showToggle) {
            Surface(
                modifier = Modifier.clickable { expanded = !expanded },
                shape = RoundedCornerShape(4.dp),
                color = TmsColor.PrimaryFixed.copy(alpha = 0.3f),
            ) {
                Text(
                    text = if (expanded) {
                        stringResource(R.string.request_detail_resort_collapse)
                    } else {
                        "+$remaining ${stringResource(R.string.request_detail_resort_more_suffix)}"
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = TmsColor.Primary,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .size(14.dp)
                .padding(top = 2.dp),
            tint = TmsColor.Primary,
        )
        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TmsColor.Outline,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = TmsColor.OnSurface,
            )
        }
    }
}

@Composable
private fun InfoRow(
    iconRes: Int,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier
                .size(14.dp)
                .padding(top = 2.dp),
            tint = TmsColor.Primary,
        )
        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = TmsColor.Outline,
                letterSpacing = 1.sp,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = TmsColor.OnSurface,
            )
        }
    }
}

@Composable
private fun DetailStatusPill(status: LessonRequestStatus) {
    val (label, bgColor, textColor) = statusPillColors(status)
    Surface(
        shape = CircleShape,
        color = bgColor,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(textColor.copy(alpha = 0.8f), CircleShape),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = textColor,
            )
        }
    }
}

@Composable
private fun statusPillColors(status: LessonRequestStatus): Triple<String, Color, Color> {
    val label = when (status) {
        LessonRequestStatus.Active -> stringResource(R.string.my_requests_status_active)
        LessonRequestStatus.Expired -> stringResource(R.string.my_requests_status_expired)
        LessonRequestStatus.ClosedByUser -> stringResource(R.string.my_requests_status_closed)
        LessonRequestStatus.PendingEmailVerification ->
            stringResource(R.string.my_requests_status_pending_email_verification)
        LessonRequestStatus.ExpiredUnverified ->
            stringResource(R.string.my_requests_status_expired_unverified)
    }
    return when (status) {
        LessonRequestStatus.Active -> Triple(
            label,
            TmsColor.Primary.copy(alpha = 0.1f),
            TmsColor.Primary,
        )
        LessonRequestStatus.Expired -> Triple(
            label,
            TmsColor.Error.copy(alpha = 0.1f),
            TmsColor.Error,
        )
        LessonRequestStatus.ClosedByUser -> Triple(
            label,
            TmsColor.OnSurfaceVariant.copy(alpha = 0.1f),
            TmsColor.OnSurfaceVariant,
        )
        LessonRequestStatus.PendingEmailVerification -> Triple(
            label,
            TmsColor.Warning.copy(alpha = 0.12f),
            TmsColor.Warning,
        )
        LessonRequestStatus.ExpiredUnverified -> Triple(
            label,
            TmsColor.Outline.copy(alpha = 0.1f),
            TmsColor.Outline,
        )
    }
}

@Composable
private fun disciplineLabel(discipline: Discipline): String =
    stringResource(
        when (discipline) {
            Discipline.Ski -> R.string.wizard_discipline_ski
            Discipline.Snowboard -> R.string.wizard_discipline_snowboard
            Discipline.Both -> R.string.wizard_discipline_both
        },
    )

@Composable
private fun skillLevelSummary(discipline: Discipline, skillLevel: Int): String {
    val lvl = skillLevel.coerceIn(0, 4)
    val descRes = when (discipline) {
        Discipline.Ski,
        Discipline.Both,
        -> when (lvl) {
            0 -> R.string.wizard_level_ski_0
            1 -> R.string.wizard_level_ski_1
            2 -> R.string.wizard_level_ski_2
            3 -> R.string.wizard_level_ski_3
            else -> R.string.wizard_level_ski_4
        }
        Discipline.Snowboard -> when (lvl) {
            0 -> R.string.wizard_level_snowboard_0
            1 -> R.string.wizard_level_snowboard_1
            2 -> R.string.wizard_level_snowboard_2
            3 -> R.string.wizard_level_snowboard_3
            else -> R.string.wizard_level_snowboard_4
        }
    }
    return stringResource(descRes)
}

@Composable
private fun groupSummary(detail: LessonRequest): String {
    val people = stringResource(R.string.wizard_confirm_people)
    val children = if (detail.hasChildren) stringResource(R.string.wizard_confirm_with_children) else ""
    return "${detail.groupSize} $people$children"
}

@Composable
private fun datesSummary(detail: LessonRequest): String {
    val undecided = stringResource(R.string.request_detail_dates_undecided)
    val flexSuffix = stringResource(R.string.request_detail_dates_flexible_suffix)
    val locale = LocalConfiguration.current.locales[0]

    if (detail.datesFlexible && detail.dateStart.isNullOrBlank()) return undecided
    val yearMonthPattern = stringResource(R.string.date_format_year_month)
    if (detail.datesFlexible && !detail.dateStart.isNullOrBlank()) {
        val formatted = try {
            val dt = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(detail.dateStart!!)!!
            SimpleDateFormat(yearMonthPattern, locale).format(dt)
        } catch (_: Exception) {
            detail.dateStart
        }
        return "$formatted $flexSuffix"
    }
    val start = detail.dateStart?.takeIf { it.isNotBlank() } ?: return undecided
    val fmtIn = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val fmtOut = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, locale)
    return try {
        val startDate = fmtIn.parse(start)!!
        val startLabel = fmtOut.format(startDate)
        val endIso = detail.dateEnd?.takeIf { it.isNotBlank() } ?: start
        if (endIso == start) startLabel
        else "$startLabel – ${fmtOut.format(fmtIn.parse(endIso)!!)}"
    } catch (_: Exception) {
        start
    }
}

@Composable
private fun durationSummary(durationDays: Double): String {
    if (durationDays == 0.5) return stringResource(R.string.wizard_confirm_half_day)
    val daysWord = stringResource(R.string.wizard_confirm_days)
    val numStr = if (kotlin.math.abs(durationDays - durationDays.toInt().toDouble()) < 1e-6)
        durationDays.toInt().toString() else durationDays.toString()
    return "$numStr $daysWord"
}

@Composable
private fun languagesSummary(codes: List<String>): String {
    val parts = codes.sorted().map { code ->
        stringResource(
            when (code) {
                "zh" -> R.string.wizard_lang_zh
                "en" -> R.string.wizard_lang_en
                "ja" -> R.string.wizard_lang_ja
                else -> R.string.wizard_lang_en
            },
        )
    }
    return parts.joinToString(", ").ifEmpty { stringResource(R.string.common_empty_value) }
}

@Composable
private fun UnlockedInstructorCard(
    preview: InstructorPreview,
    onChatClick: (String) -> Unit,
    onInstructorClick: (String) -> Unit,
) {
    val shortId = preview.shortId.orEmpty()

    val infiniteTransition = rememberInfiniteTransition(label = "unread_pulse")
    val unreadPulse by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "unread_pulse_alpha",
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (shortId.isNotEmpty()) Modifier.clickable { onInstructorClick(shortId) }
                else Modifier,
            ),
        shape = RoundedCornerShape(16.dp),
        color = TmsColor.SurfaceLowest,
        shadowElevation = 1.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box {
                    UserAvatar(
                        displayName = preview.displayName,
                        avatarUrl = preview.avatarUrl,
                        size = 56.dp,
                    )
                    if (preview.hasUnread) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-2).dp)
                                .size(14.dp)
                                .graphicsLayer { alpha = unreadPulse }
                                .clip(CircleShape)
                                .background(TmsColor.Error)
                                .border(2.dp, TmsColor.SurfaceLowest, CircleShape),
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = preview.displayName ?: stringResource(R.string.common_empty_value),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TmsColor.OnSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    IdentityVerifiedBadge(
                        verified = preview.lineUserId != null,
                        verifiedLabel = stringResource(R.string.identity_verified_label),
                        unverifiedLabel = stringResource(R.string.identity_unverified_label),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    StarRatingRow(
                        avg = preview.ratingAvg ?: 0.0,
                        count = preview.ratingCount,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val roomId = preview.roomId
            if (!roomId.isNullOrBlank()) {
                Button(
                    onClick = { onChatClick(roomId) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TmsColor.Primary,
                        contentColor = TmsColor.OnPrimary,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.request_detail_go_to_chat),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendedInstructorCard(
    preview: InstructorPreview,
    onInstructorClick: (String) -> Unit,
    onContactClick: () -> Unit,
) {
    val shortId = preview.shortId.orEmpty()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (shortId.isNotEmpty()) Modifier.clickable { onInstructorClick(shortId) }
                else Modifier,
            ),
        shape = RoundedCornerShape(16.dp),
        color = TmsColor.SurfaceLowest,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            UserAvatar(
                displayName = preview.displayName,
                avatarUrl = preview.avatarUrl,
                size = 48.dp,
            )
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = preview.displayName ?: stringResource(R.string.common_empty_value),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TmsColor.OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(4.dp))
                IdentityVerifiedBadge(
                    verified = preview.lineUserId != null,
                    verifiedLabel = stringResource(R.string.identity_verified_label),
                    unverifiedLabel = stringResource(R.string.identity_unverified_label),
                )
                Spacer(modifier = Modifier.height(2.dp))
                StarRatingRow(
                    avg = preview.ratingAvg ?: 0.0,
                    count = preview.ratingCount,
                )
            }
            Button(
                onClick = onContactClick,
                modifier = Modifier.align(Alignment.CenterVertically),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TmsColor.Primary,
                    contentColor = TmsColor.OnPrimary,
                ),
            ) {
                Text(
                    text = stringResource(R.string.request_detail_contact_instructor),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun StarRatingRow(avg: Double, count: Int) {
    val filled = kotlin.math.round(avg).toInt().coerceIn(0, 5)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        repeat(5) { i ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(11.dp),
                tint = if (i < filled) TmsColor.Primary else TmsColor.OnSurfaceVariant.copy(alpha = 0.3f),
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "%.1f".format(avg),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = TmsColor.Primary,
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.labelSmall,
            color = TmsColor.OnSurfaceVariant,
        )
    }
}

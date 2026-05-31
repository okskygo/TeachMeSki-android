package com.teachmeski.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Landscape
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.EquipmentRental
import com.teachmeski.app.domain.model.ExploreLessonRequest
import com.teachmeski.app.ui.component.EmptyState
import com.teachmeski.app.ui.component.IdentityRequiredDialog
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ExploreDetailScreen(
    requestId: String,
    viewModel: ExploreViewModel,
    onBack: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    onNavigateToAccountSettings: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val request = uiState.requests.firstOrNull { it.id == requestId }
        ?: uiState.detailFallback?.takeIf { it.id == requestId }

    LaunchedEffect(requestId) {
        viewModel.ensureRequestLoaded(requestId)
    }

    LaunchedEffect(uiState.unlockSuccessChatRoomId) {
        val roomId = uiState.unlockSuccessChatRoomId ?: return@LaunchedEffect
        onNavigateToChat(roomId)
        viewModel.consumeUnlockSuccess()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = TmsColor.Background,
        topBar = {
            // ExploreDetail is a fullscreen route, so MainActivity's outer
            // Scaffold zeroes content insets and TmsTopBar zeroes its own —
            // apply the status-bar inset here so the bar sits below the status bar.
            TmsTopBar(
                modifier = Modifier.statusBarsPadding(),
                title = stringResource(R.string.explore_detail_screen_title),
                onBack = onBack,
            )
        },
        bottomBar = {
            if (request != null) {
                ExploreDetailBottomBar(
                    request = request,
                    onUnlockClick = { viewModel.openUnlockDialog(request) },
                    onViewChatClick = onNavigateToChat,
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when {
                request != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                    ) {
                        ExploreDetailInfoCard(request = request)
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                uiState.detailLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = TmsColor.Primary,
                    )
                }

                else -> {
                    EmptyState(
                        title = stringResource(R.string.explore_detail_not_found_title),
                        description = stringResource(R.string.explore_detail_not_found_description),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
    }

    uiState.unlockDialogRequest?.let { req ->
        UnlockDialog(
            request = req,
            tokenBalance = uiState.tokenBalance,
            message = uiState.unlockMessage,
            onMessageChange = viewModel::setUnlockMessage,
            isUnlocking = uiState.isUnlocking,
            error = uiState.unlockError,
            onConfirm = { viewModel.confirmUnlock() },
            onDismiss = { viewModel.closeUnlockDialog() },
        )
    }

    if (uiState.showIdentityRequired) {
        IdentityRequiredDialog(
            onDismiss = { viewModel.dismissIdentityRequired() },
            onBindNow = {
                viewModel.dismissIdentityRequired()
                onNavigateToAccountSettings()
            },
        )
    }
}

@Composable
private fun ExploreDetailBottomBar(
    request: ExploreLessonRequest,
    onUnlockClick: () -> Unit,
    onViewChatClick: (String) -> Unit,
) {
    Surface(
        color = TmsColor.SurfaceLowest,
        shadowElevation = 8.dp,
    ) {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            when (val state = exploreCtaState(request)) {
                is ExploreCtaState.ViewChat -> {
                    Button(
                        onClick = { onViewChatClick(state.roomId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TmsColor.Primary,
                            contentColor = TmsColor.OnPrimary,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.explore_card_view_chat),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                ExploreCtaState.AlreadyUnlocked -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(TmsColor.SurfaceLow)
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LockOpen,
                            contentDescription = null,
                            tint = TmsColor.OnSurfaceVariant,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.explore_card_already_unlocked),
                            style = MaterialTheme.typography.labelLarge,
                            color = TmsColor.OnSurfaceVariant,
                        )
                    }
                }

                is ExploreCtaState.Unlock -> {
                    val brush = Brush.linearGradient(
                        colors = listOf(TmsColor.Primary, TmsColor.PrimaryContainer),
                    )
                    Surface(
                        onClick = onUnlockClick,
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(brush, RoundedCornerShape(10.dp))
                                .padding(vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = TmsColor.OnPrimary,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stringResource(R.string.explore_unlock_button, state.tokenCost),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TmsColor.OnPrimary,
                            )
                        }
                    }
                }

                ExploreCtaState.SlotsFull -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(TmsColor.SurfaceContainer)
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = TmsColor.Outline,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.explore_card_slots_full),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TmsColor.Outline,
                        )
                    }
                }

                ExploreCtaState.Unavailable -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(TmsColor.SurfaceContainer)
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = TmsColor.Outline,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.explore_cta_unavailable),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TmsColor.Outline,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExploreDetailInfoCard(request: ExploreLessonRequest) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = TmsColor.SurfaceLowest,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = disciplineLabel(request.discipline),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TmsColor.OnSurface,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                    displayName = request.userDisplayName,
                    avatarUrl = request.userAvatarUrl,
                    size = 36.dp,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = request.userDisplayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TmsColor.OnSurface,
                )
                Text(
                    text = " · " + formatRelativeTime(request.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = TmsColor.Outline,
                )
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
                    modifier = Modifier.size(14.dp).padding(top = 2.dp),
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
                    if (request.allRegionsSelected) {
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
                    } else if (request.resortNames.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            request.resortNames.forEach { name ->
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
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.common_empty_value),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TmsColor.OnSurface,
                        )
                    }
                }
            }

            DetailInfoRow(
                icon = Icons.Outlined.CalendarMonth,
                label = stringResource(R.string.request_detail_lesson_date_label),
                value = datesSummary(request),
            )
            DetailInfoRow(
                iconRes = when (request.discipline) {
                    Discipline.Snowboard -> R.drawable.ic_snowboard
                    Discipline.Ski, Discipline.Both -> R.drawable.ic_ski
                },
                label = stringResource(R.string.request_detail_discipline_label),
                value = disciplineLabel(request.discipline),
            )
            DetailInfoRow(
                icon = Icons.Outlined.TrendingUp,
                label = stringResource(R.string.request_detail_level_label),
                value = skillLevelSummary(request.discipline, request.skillLevel),
            )
            DetailInfoRow(
                icon = Icons.Outlined.Groups,
                label = stringResource(R.string.request_detail_group_size_label),
                value = groupSummary(request),
            )
            DetailInfoRow(
                icon = Icons.Outlined.Schedule,
                label = stringResource(R.string.request_detail_duration_label),
                value = durationSummary(request.durationDays),
            )
            DetailInfoRow(
                icon = Icons.Outlined.Translate,
                label = stringResource(R.string.request_detail_language_label),
                value = languagesSummary(request.preferredLanguages),
            )
            DetailInfoRow(
                icon = Icons.Outlined.Inventory2,
                label = stringResource(R.string.request_detail_equipment_label),
                value = equipmentSummary(request.equipmentRental),
            )
            DetailInfoRow(
                icon = Icons.Outlined.DirectionsCar,
                label = stringResource(R.string.request_detail_transport_label),
                value = transportSummary(request.needsTransport, request.transportNote),
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = TmsColor.SurfaceVariant.copy(alpha = 0.4f),
            )

            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp).padding(top = 2.dp),
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
                    if (request.certPreferences.isEmpty()) {
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
                            request.certPreferences.forEach { code ->
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

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = TmsColor.SurfaceVariant.copy(alpha = 0.4f),
            )

            DetailInfoRow(
                icon = Icons.Outlined.Description,
                label = stringResource(R.string.request_detail_description_label),
                value = request.additionalNotes?.trim().takeUnless { it.isNullOrEmpty() }
                    ?: stringResource(R.string.common_empty_value),
            )
        }
    }
}

@Composable
private fun DetailInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp).padding(top = 2.dp),
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
private fun DetailInfoRow(iconRes: Int, label: String, value: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(14.dp).padding(top = 2.dp),
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
private fun disciplineLabel(discipline: Discipline): String =
    stringResource(
        when (discipline) {
            Discipline.Ski -> R.string.wizard_discipline_ski
            Discipline.Snowboard -> R.string.wizard_discipline_snowboard
            Discipline.Both -> R.string.wizard_discipline_both
        },
    )

@Composable
private fun skillLevelSummary(discipline: Discipline, skillLevel: Int?): String {
    if (skillLevel == null) return stringResource(R.string.common_empty_value)
    val lvl = skillLevel.coerceIn(0, 4)
    val descRes = when (discipline) {
        Discipline.Ski, Discipline.Both -> when (lvl) {
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
private fun groupSummary(request: ExploreLessonRequest): String {
    val people = stringResource(R.string.wizard_confirm_people)
    val children = if (request.hasChildren) stringResource(R.string.wizard_confirm_with_children) else ""
    return "${request.groupSize} $people$children"
}

@Composable
private fun durationSummary(durationDays: Double?): String {
    if (durationDays == null) return stringResource(R.string.common_empty_value)
    if (durationDays == 0.5) return stringResource(R.string.wizard_confirm_half_day)
    val daysWord = stringResource(R.string.wizard_confirm_days)
    val numStr = if (kotlin.math.abs(durationDays - durationDays.toInt().toDouble()) < 1e-6)
        durationDays.toInt().toString() else durationDays.toString()
    return "$numStr $daysWord"
}

@Composable
private fun datesSummary(request: ExploreLessonRequest): String {
    val undecided = stringResource(R.string.request_detail_dates_undecided)
    val flexSuffix = stringResource(R.string.request_detail_dates_flexible_suffix)
    val locale = LocalConfiguration.current.locales[0]

    if (request.datesFlexible && request.startDate.isNullOrBlank()) return undecided
    val yearMonthPattern = stringResource(R.string.date_format_year_month)
    if (request.datesFlexible && !request.startDate.isNullOrBlank()) {
        val formatted = try {
            val dt = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(request.startDate!!)!!
            SimpleDateFormat(yearMonthPattern, locale).format(dt)
        } catch (_: Exception) {
            request.startDate
        }
        return "$formatted $flexSuffix"
    }
    val start = request.startDate?.takeIf { it.isNotBlank() } ?: return undecided
    val fmtIn = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val fmtOut = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, locale)
    return try {
        val startDate = fmtIn.parse(start)!!
        val startLabel = fmtOut.format(startDate)
        val endIso = request.endDate?.takeIf { it.isNotBlank() } ?: start
        if (endIso == start) startLabel
        else "$startLabel – ${fmtOut.format(fmtIn.parse(endIso)!!)}"
    } catch (_: Exception) {
        start
    }
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
private fun equipmentSummary(rental: EquipmentRental?): String {
    if (rental == null) return stringResource(R.string.common_empty_value)
    return stringResource(
        when (rental) {
            EquipmentRental.All -> R.string.wizard_equipment_all
            EquipmentRental.Partial -> R.string.wizard_equipment_partial
            EquipmentRental.None -> R.string.wizard_equipment_none
        },
    )
}

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

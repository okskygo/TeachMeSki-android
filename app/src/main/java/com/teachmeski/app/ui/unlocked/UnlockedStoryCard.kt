package com.teachmeski.app.ui.unlocked

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.DownhillSkiing
import androidx.compose.material.icons.filled.Snowboarding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import androidx.compose.ui.draw.alpha
import com.teachmeski.app.domain.model.UnlockStatus
import com.teachmeski.app.domain.model.UnlockedRoom
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun parseIsoMillis(raw: String): Long? {
    val s = raw.trim()
    if (s.isEmpty()) return null
    // Only take the date-only fast path for strings that are exactly a plain
    // `yyyy-MM-dd` (10 chars). SimpleDateFormat silently accepts trailing
    // input, so without this guard `2026-04-20T16:04:49+00:00` would be
    // parsed as local-midnight 2026-04-20 and render as "1 day ago".
    if (s.length == 10) {
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(s)?.time?.let { return it }
        } catch (_: Exception) { /* fall through */ }
    }

    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy-MM-dd'T'HH:mm:ss",
    )
    for (p in patterns) {
        try {
            val sdf = SimpleDateFormat(p, Locale.US)
            if (p.endsWith("'Z'") || p == "yyyy-MM-dd'T'HH:mm:ss.SSSZ") {
                sdf.timeZone = TimeZone.getTimeZone("UTC")
            }
            sdf.parse(s)?.time?.let { return it }
        } catch (_: Exception) { /* try next */ }
    }
    return null
}

private fun formatDisplayDate(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    val ms = parseIsoMillis(iso) ?: return iso.trim()
    return DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault()).format(Date(ms))
}

private fun dateStringsEqual(a: String?, b: String?): Boolean {
    if (a.isNullOrBlank() || b.isNullOrBlank()) return false
    return normalizeDateKey(a) == normalizeDateKey(b)
}

private fun normalizeDateKey(iso: String): String {
    val ms = parseIsoMillis(iso) ?: return iso.trim()
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(ms))
}

private fun languageLabelRes(code: String): Int? =
    when (code.lowercase(Locale.US)) {
        "zh", "zh-tw", "zh-hant", "zh-hk" -> R.string.explore_card_language_zh
        "en" -> R.string.explore_card_language_en
        "ja" -> R.string.explore_card_language_ja
        else -> null
    }

@Composable
private fun formatRelativeTime(iso: String): String {
    val parsed = parseIsoMillis(iso) ?: return stringResource(R.string.explore_time_just_now)
    val now = System.currentTimeMillis()
    val diffMs = (now - parsed).coerceAtLeast(0L)
    return when {
        diffMs < 60_000L -> stringResource(R.string.explore_time_just_now)
        diffMs < 3_600_000L -> stringResource(R.string.explore_time_minutes_ago, (diffMs / 60_000L).toInt().coerceAtLeast(1))
        diffMs < 86_400_000L -> stringResource(R.string.explore_time_hours_ago, (diffMs / 3_600_000L).toInt().coerceAtLeast(1))
        else -> {
            val days = (diffMs / 86_400_000L).toInt().coerceAtLeast(1)
            when {
                days < 7 -> stringResource(R.string.explore_time_days_ago, days)
                days < 30 -> stringResource(R.string.explore_time_weeks_ago, (days / 7).coerceAtLeast(1))
                days < 365 -> stringResource(R.string.explore_time_months_ago, (days / 30).coerceAtLeast(1))
                else -> SimpleDateFormat("yyyy/MM/dd", Locale.US).format(Date(parsed))
            }
        }
    }
}

@Composable
private fun UnlockedSectionLabel(text: String) {
    Text(
        text = text.uppercase(Locale.getDefault()),
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp,
        ),
        color = TmsColor.OnSurfaceVariant,
    )
}

@Composable
private fun UnlockedLocationsSection(
    allRegionsSelected: Boolean,
    resortNames: List<String>,
) {
    if (!allRegionsSelected && resortNames.isEmpty()) return

    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        UnlockedSectionLabel(stringResource(R.string.explore_card_label_locations))
        Spacer(modifier = Modifier.height(8.dp))
        if (allRegionsSelected) {
            Text(
                text = stringResource(R.string.explore_card_all_resorts),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TmsColor.Primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(TmsColor.Primary.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        } else {
            var expanded by remember(resortNames) { mutableStateOf(false) }
            val showCollapseControl = resortNames.size > 5
            val visible = if (expanded || !showCollapseControl) resortNames else resortNames.take(5)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                visible.forEach { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodySmall,
                        color = TmsColor.OnSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TmsColor.SurfaceLow)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                if (showCollapseControl) {
                    val more = resortNames.size - 5
                    Text(
                        text = if (expanded) {
                            stringResource(R.string.explore_card_resort_collapse)
                        } else {
                            stringResource(R.string.explore_card_resort_more_suffix, more)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = TmsColor.Primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TmsColor.Primary.copy(alpha = 0.1f))
                            .clickable { expanded = !expanded }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun UnlockedStoryCard(
    room: UnlockedRoom,
    onNavigateToChat: () -> Unit,
) {
    // F-008 P3: refunded unlocks (48hr no-reply auto-refund) are visually
    // closed AND greyscaled; lesson_request closed/expired is visually closed
    // but the card itself is NOT dimmed (consistent with web P2).
    val isRefunded = room.unlockStatus == UnlockStatus.Refunded
    // FR-008-053 (2026-05-02 "我的案件" rename): a chat_room without
    // any matching `request_unlocks` row is shown as Pending — visuals
    // stay live (NOT greyed) and a "待解鎖 / Pending unlock" badge
    // replaces the "unlocked X ago" timestamp.
    val isPending = room.unlockStatus == UnlockStatus.Pending
    val isRequestActive = room.requestStatus.equals("active", ignoreCase = true)
    val isClosed = isRefunded || !isRequestActive
    val isoHalf = room.durationDays != null && kotlin.math.abs(room.durationDays - 0.5) < 1e-6
    val hasDates = !room.dateStart.isNullOrBlank() || !room.dateEnd.isNullOrBlank()
    val showLangDateRow = room.preferredLanguages.isNotEmpty() || hasDates

    val badgeText = when (room.discipline) {
        Discipline.Ski -> stringResource(R.string.explore_card_discipline_badge_ski)
        Discipline.Snowboard -> stringResource(R.string.explore_card_discipline_badge_snowboard)
        Discipline.Both -> stringResource(R.string.explore_card_discipline_badge_both)
    }

    val datePrimaryText = run {
        val s = room.dateStart
        val e = room.dateEnd
        when {
            !s.isNullOrBlank() && (e.isNullOrBlank() || dateStringsEqual(s, e)) -> formatDisplayDate(s)
            !s.isNullOrBlank() && !e.isNullOrBlank() ->
                stringResource(
                    R.string.explore_card_date_range,
                    formatDisplayDate(s),
                    formatDisplayDate(e),
                )

            !e.isNullOrBlank() -> formatDisplayDate(e)
            else -> ""
        }
    }.let { base ->
        if (base.isEmpty()) {
            base
        } else if (room.datesFlexible) {
            base + " · " + stringResource(R.string.explore_card_dates_flexible)
        } else {
            base
        }
    }

    val languageLine = room.preferredLanguages.map { code ->
        languageLabelRes(code)?.let { stringResource(it) }
            ?: stringResource(R.string.explore_card_language_other, code)
    }.joinToString(", ")

    val unlockedRelative = when {
        room.unlockedAt.isBlank() -> stringResource(R.string.explore_time_just_now)
        else -> formatRelativeTime(room.unlockedAt)
    }
    val unlockedLabel = stringResource(R.string.unlocked_unlocked_ago_fmt, unlockedRelative)

    // AC-008-MYCASES-010 (2026-05-02 v2): footer pill reflects ONLY
    // the lesson_request lifecycle (refunded > closed > active). The
    // "pending unlock" signal is communicated solely by the top-right
    // badge — mixing unlock-state and lifecycle-state into one pill
    // confused users.
    val statusLabel = when {
        isRefunded -> stringResource(R.string.unlocked_status_refunded)
        !isRequestActive -> stringResource(R.string.unlocked_status_closed)
        else -> stringResource(R.string.unlocked_status_active)
    }

    val cardShape = RoundedCornerShape(12.dp)
    val canOpenChat = room.roomId.isNotBlank()

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
        color = TmsColor.SurfaceLowest,
        shape = cardShape,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isRefunded) Modifier.alpha(0.6f) else Modifier),
    ) {
        Column(modifier = Modifier.fillMaxWidth().clip(cardShape)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier
                            .border(1.dp, TmsColor.Primary.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(TmsColor.Primary.copy(alpha = 0.05f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        when (room.discipline) {
                            Discipline.Snowboard -> Icon(
                                Icons.Filled.Snowboarding,
                                contentDescription = null,
                                tint = TmsColor.Primary,
                                modifier = Modifier.size(18.dp),
                            )

                            Discipline.Ski, Discipline.Both -> Icon(
                                Icons.Filled.DownhillSkiing,
                                contentDescription = null,
                                tint = TmsColor.Primary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        Text(
                            text = badgeText.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TmsColor.Primary,
                        )
                    }
                    if (isPending) {
                        Text(
                            text = stringResource(R.string.unlocked_badge_pending)
                                .uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TmsColor.Warning,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(TmsColor.Warning.copy(alpha = 0.1f))
                                .border(
                                    1.dp,
                                    TmsColor.Warning.copy(alpha = 0.2f),
                                    RoundedCornerShape(20.dp),
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                        )
                    } else {
                        Text(
                            text = unlockedLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = TmsColor.OnSurfaceVariant,
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UserAvatar(
                        displayName = room.studentDisplayName,
                        avatarUrl = room.studentAvatarUrl,
                        size = 48.dp,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = room.studentDisplayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TmsColor.OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (room.skillLevel != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(
                                    R.string.explore_card_skill_level_fmt,
                                    room.skillLevel.toString(),
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TmsColor.OnPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TmsColor.SecondaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                if (showLangDateRow) {
                    val langBlock: @Composable () -> Unit = {
                        UnlockedSectionLabel(stringResource(R.string.explore_card_label_language))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = languageLine,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TmsColor.OnSurface,
                        )
                    }
                    val datesBlock: @Composable () -> Unit = {
                        UnlockedSectionLabel(stringResource(R.string.explore_card_label_dates))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = datePrimaryText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TmsColor.Primary,
                        )
                    }
                    when {
                        room.preferredLanguages.isNotEmpty() && hasDates -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                Column(modifier = Modifier.weight(1f)) { langBlock() }
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .width(1.dp)
                                        .height(32.dp)
                                        .background(TmsColor.OutlineVariant.copy(alpha = 0.3f)),
                                )
                                Column(modifier = Modifier.weight(1f)) { datesBlock() }
                            }
                        }

                        room.preferredLanguages.isNotEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                            ) {
                                langBlock()
                            }
                        }

                        else -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp),
                            ) {
                                datesBlock()
                            }
                        }
                    }
                }

                UnlockedLocationsSection(
                    allRegionsSelected = room.allRegionsSelected,
                    resortNames = room.resortNames,
                )

                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    UnlockedSectionLabel(stringResource(R.string.explore_card_label_group_duration))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val groupSuffix = if (room.hasChildren) {
                            " · " + stringResource(R.string.explore_card_group_has_children)
                        } else {
                            ""
                        }
                        Text(
                            text = stringResource(R.string.explore_card_group_size, room.groupSize) + groupSuffix,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TmsColor.Secondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TmsColor.Secondary.copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                        if (room.durationDays != null) {
                            val durationLabel = if (isoHalf) {
                                stringResource(R.string.explore_card_half_day)
                            } else {
                                stringResource(
                                    R.string.explore_card_days,
                                    kotlin.math.round(room.durationDays).toInt().coerceAtLeast(1),
                                )
                            }
                            Text(
                                text = durationLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TmsColor.Secondary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TmsColor.Secondary.copy(alpha = 0.1f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TmsColor.SurfaceLow),
                    ) {
                        if (room.hasUnread) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .fillMaxHeight()
                                    .background(TmsColor.Error),
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            val lastMsg = room.lastMessageContent?.trim().orEmpty()
                            if (lastMsg.isNotEmpty()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        text = lastMsg,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (room.hasUnread) FontWeight.Medium else FontWeight.Normal,
                                        color = if (room.hasUnread) TmsColor.OnSurface else TmsColor.OnSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    val lmAt = room.lastMessageAt
                                    if (!lmAt.isNullOrBlank()) {
                                        Text(
                                            text = formatRelativeTime(lmAt),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = TmsColor.OnSurfaceVariant.copy(alpha = 0.6f),
                                        )
                                    }
                                }
                            } else {
                                Text(
                                    text = stringResource(R.string.unlocked_no_message_yet),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontStyle = FontStyle.Italic,
                                    color = TmsColor.OnSurfaceVariant.copy(alpha = 0.5f),
                                )
                            }
                        }
                    }
                    if (room.hasUnread) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 6.dp, y = (-6).dp)
                                .size(14.dp)
                                .graphicsLayer { alpha = unreadPulse }
                                .clip(CircleShape)
                                .background(TmsColor.Error)
                                .border(2.dp, TmsColor.SurfaceLowest, CircleShape),
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TmsColor.SurfaceLow)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                val statusShape = RoundedCornerShape(50)
                val statusFg = if (isClosed) TmsColor.OnSurfaceVariant else TmsColor.Success
                val statusBg = if (isClosed) {
                    TmsColor.OnSurfaceVariant.copy(alpha = 0.1f)
                } else {
                    TmsColor.Success.copy(alpha = 0.1f)
                }
                val statusBorder = if (isClosed) {
                    Modifier
                } else {
                    Modifier.border(1.dp, TmsColor.Success.copy(alpha = 0.2f), statusShape)
                }
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusFg,
                    modifier = Modifier
                        .clip(statusShape)
                        .background(statusBg)
                        .then(statusBorder)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                )
                if (canOpenChat) {
                    Button(
                        onClick = onNavigateToChat,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TmsColor.Primary,
                            contentColor = TmsColor.OnPrimary,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 2.dp,
                        ),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Message,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.unlocked_go_to_conversation),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.unlocked_go_to_conversation),
                        style = MaterialTheme.typography.bodySmall,
                        color = TmsColor.Outline,
                    )
                }
            }
        }
    }
}

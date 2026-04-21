package com.teachmeski.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.DownhillSkiing
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.EquipmentRental
import com.teachmeski.app.domain.model.ExploreLessonRequest
import com.teachmeski.app.domain.model.LessonRequestStatus
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
private fun formatRelativeTime(createdAt: String): String {
    val parsed = parseIsoMillis(createdAt) ?: return stringResource(R.string.explore_time_just_now)
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
private fun ExploreCardSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.6.sp,
        ),
        color = TmsColor.OnSurfaceVariant,
    )
}

@Composable
private fun equipmentRentalLabel(rental: EquipmentRental): String =
    when (rental) {
        EquipmentRental.All -> stringResource(R.string.explore_card_equipment_all)
        EquipmentRental.Partial -> stringResource(R.string.explore_card_equipment_partial)
        EquipmentRental.None -> stringResource(R.string.explore_card_equipment_none)
    }

@Composable
private fun ExploreCardPreferencesSection(
    equipmentRental: EquipmentRental?,
    needsTransport: Boolean,
    transportNote: String?,
    certPreferences: List<String>,
) {
    if (equipmentRental == null && !needsTransport && certPreferences.isEmpty()) return

    Column(
        modifier = Modifier.padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (equipmentRental != null) {
            Column {
                ExploreCardSectionLabel(stringResource(R.string.explore_card_label_equipment))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = equipmentRentalLabel(equipmentRental),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TmsColor.OnSurface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TmsColor.SurfaceLow)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }

        if (needsTransport) {
            Column {
                ExploreCardSectionLabel(stringResource(R.string.explore_card_label_transport))
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.explore_card_transport_needed),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TmsColor.Primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TmsColor.Primary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
                if (!transportNote.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(
                            R.string.explore_card_transport_note_prefix,
                            transportNote,
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        fontStyle = FontStyle.Italic,
                        color = TmsColor.OnSurfaceVariant,
                    )
                }
            }
        }

        if (certPreferences.isNotEmpty()) {
            Column {
                ExploreCardSectionLabel(stringResource(R.string.explore_card_label_cert_preferences))
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    certPreferences.forEach { code ->
                        Text(
                            text = code,
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
        }
    }
}

@Composable
private fun ExploreCardNotesSection(notes: String) {
    val trimmed = notes.trim()
    if (trimmed.isEmpty()) return
    var expanded by remember(notes) { mutableStateOf(false) }
    var isOverflowing by remember(notes) { mutableStateOf(false) }
    val quoted = "\u201c$trimmed\u201d"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TmsColor.SurfaceLow)
            .padding(20.dp),
    ) {
        Text(
            text = quoted,
            style = MaterialTheme.typography.bodySmall,
            fontStyle = FontStyle.Italic,
            color = TmsColor.OnSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result -> isOverflowing = result.hasVisualOverflow },
        )
        if (isOverflowing || expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(if (expanded) R.string.explore_card_show_less else R.string.explore_card_show_more),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TmsColor.Primary,
                modifier = Modifier.clickable { expanded = !expanded },
            )
        }
    }
}

@Composable
private fun ExploreCardLocationsSection(
    allRegionsSelected: Boolean,
    resortNames: List<String>,
) {
    if (!allRegionsSelected && resortNames.isEmpty()) return

    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        ExploreCardSectionLabel(stringResource(R.string.explore_card_label_locations))
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
private fun ExploreCardFooter(
    remaining: Int,
    quotaLimit: Int,
    isUnlockedByMe: Boolean,
    isActive: Boolean,
    myChatRoomId: String?,
    baseTokenCost: Int,
    onUnlockClick: () -> Unit,
    onViewChatClick: (String) -> Unit,
) {
    val quotaLine = if (remaining <= 0 && quotaLimit > 0) {
        stringResource(R.string.explore_card_remaining_slots_zero)
    } else {
        stringResource(R.string.explore_card_quota_remaining, remaining)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TmsColor.SurfaceLow)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(50))
                .background(TmsColor.SecondaryFixed)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.People,
                contentDescription = null,
                tint = TmsColor.Secondary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = quotaLine,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = TmsColor.Secondary,
                maxLines = 2,
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        when {
            isUnlockedByMe && myChatRoomId != null -> {
                Button(
                    onClick = { onViewChatClick(myChatRoomId) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TmsColor.Primary,
                        contentColor = TmsColor.OnPrimary,
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.explore_card_view_chat))
                }
            }

            isUnlockedByMe -> {
                Text(
                    text = stringResource(R.string.explore_card_already_unlocked),
                    style = MaterialTheme.typography.labelSmall,
                    color = TmsColor.Outline,
                )
            }

            !isUnlockedByMe && isActive && remaining > 0 -> {
                val brush = Brush.horizontalGradient(listOf(TmsColor.Primary, TmsColor.PrimaryContainer))
                Surface(
                    onClick = onUnlockClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    shadowElevation = 0.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .background(brush, RoundedCornerShape(8.dp))
                            .padding(horizontal = 32.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = TmsColor.OnPrimary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(R.string.explore_unlock_button, baseTokenCost),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = TmsColor.OnPrimary,
                        )
                    }
                }
            }

            else -> {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TmsColor.SurfaceContainer,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = TmsColor.Outline,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = stringResource(R.string.explore_card_slots_full),
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

@Composable
internal fun ExploreRequestCard(
    request: ExploreLessonRequest,
    onUnlockClick: () -> Unit,
    onViewChatClick: (String) -> Unit,
) {
    val remaining = kotlin.math.max(0, request.quotaLimit - request.unlockCount)
    val slotsFull = request.status != LessonRequestStatus.Active || remaining <= 0
    val dimCard = slotsFull && !request.isUnlockedByMe
    val isoHalf = request.durationDays != null && kotlin.math.abs(request.durationDays - 0.5) < 1e-6

    val preferredLanguages = request.preferredLanguages
    val hasDates = !request.startDate.isNullOrBlank() || !request.endDate.isNullOrBlank()
    val showLangDateRow = preferredLanguages.isNotEmpty() || hasDates

    val badgeText = when (request.discipline) {
        Discipline.Ski -> stringResource(R.string.explore_card_discipline_badge_ski)
        Discipline.Snowboard -> stringResource(R.string.explore_card_discipline_badge_snowboard)
        Discipline.Both -> stringResource(R.string.explore_card_discipline_badge_both)
    }

    val datePrimaryText = run {
        val s = request.startDate
        val e = request.endDate
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
        } else if (request.datesFlexible) {
            base + " · " + stringResource(R.string.explore_card_dates_flexible)
        } else {
            base
        }
    }

    val languageLine = preferredLanguages.map { code ->
        languageLabelRes(code)?.let { stringResource(it) }
            ?: stringResource(R.string.explore_card_language_other, code)
    }.joinToString(", ")

    val cardShape = RoundedCornerShape(12.dp)

    Surface(
        color = TmsColor.SurfaceLowest,
        shape = cardShape,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (dimCard) Modifier.alpha(0.6f) else Modifier),
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
                            .clip(RoundedCornerShape(20.dp))
                            .background(TmsColor.Primary.copy(alpha = 0.05f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        when (request.discipline) {
                            Discipline.Ski -> Icon(
                                Icons.Filled.DownhillSkiing,
                                contentDescription = null,
                                tint = TmsColor.Primary,
                                modifier = Modifier.size(18.dp),
                            )

                            Discipline.Snowboard -> Icon(
                                Icons.Filled.Snowboarding,
                                contentDescription = null,
                                tint = TmsColor.Primary,
                                modifier = Modifier.size(18.dp),
                            )

                            Discipline.Both -> Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(
                                    Icons.Filled.DownhillSkiing,
                                    contentDescription = null,
                                    tint = TmsColor.Primary,
                                    modifier = Modifier.size(16.dp),
                                )
                                Icon(
                                    Icons.Filled.Snowboarding,
                                    contentDescription = null,
                                    tint = TmsColor.Primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        Text(
                            text = badgeText.uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TmsColor.Primary,
                        )
                    }
                    Text(
                        text = formatRelativeTime(request.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = TmsColor.OnSurfaceVariant,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    UserAvatar(
                        displayName = request.userDisplayName,
                        avatarUrl = request.userAvatarUrl,
                        size = 48.dp,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = request.userDisplayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TmsColor.OnSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (request.skillLevel != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = stringResource(
                                    R.string.explore_card_skill_level_fmt,
                                    request.skillLevel.toString(),
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
                        ExploreCardSectionLabel(stringResource(R.string.explore_card_label_language))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = languageLine,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TmsColor.OnSurface,
                        )
                    }
                    val datesBlock: @Composable () -> Unit = {
                        ExploreCardSectionLabel(stringResource(R.string.explore_card_label_dates))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = datePrimaryText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = TmsColor.Primary,
                        )
                    }
                    when {
                        preferredLanguages.isNotEmpty() && hasDates -> {
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

                        preferredLanguages.isNotEmpty() -> {
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

                ExploreCardLocationsSection(
                    allRegionsSelected = request.allRegionsSelected,
                    resortNames = request.resortNames,
                )

                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    ExploreCardSectionLabel(stringResource(R.string.explore_card_label_group_duration))
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val groupSuffix = if (request.hasChildren) {
                            " · " + stringResource(R.string.explore_card_group_has_children)
                        } else {
                            ""
                        }
                        Text(
                            text = stringResource(R.string.explore_card_group_size, request.groupSize) + groupSuffix,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = TmsColor.Secondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TmsColor.Secondary.copy(alpha = 0.1f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                        if (request.durationDays != null) {
                            val durationLabel = if (isoHalf) {
                                stringResource(R.string.explore_card_half_day)
                            } else {
                                stringResource(
                                    R.string.explore_card_days,
                                    kotlin.math.round(request.durationDays).toInt().coerceAtLeast(1),
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

                ExploreCardPreferencesSection(
                    equipmentRental = request.equipmentRental,
                    needsTransport = request.needsTransport,
                    transportNote = request.transportNote,
                    certPreferences = request.certPreferences,
                )

                request.additionalNotes?.let { ExploreCardNotesSection(it) }
            }

            ExploreCardFooter(
                remaining = remaining,
                quotaLimit = request.quotaLimit,
                isUnlockedByMe = request.isUnlockedByMe,
                isActive = request.status == LessonRequestStatus.Active,
                myChatRoomId = request.myChatRoomId,
                baseTokenCost = request.baseTokenCost,
                onUnlockClick = onUnlockClick,
                onViewChatClick = onViewChatClick,
            )
        }
    }
}

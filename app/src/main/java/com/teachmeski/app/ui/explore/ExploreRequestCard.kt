package com.teachmeski.app.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DownhillSkiing
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Snowboarding
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

// Skill level long description res by discipline.
// `Both` falls back to ski per product decision (a request's discipline is
// ski OR snowboard at creation; Both is theoretical but handled defensively).
private fun skillLevelDescriptionRes(discipline: Discipline, level: Int): Int? {
    val clamped = level.coerceIn(0, 4)
    return when (discipline) {
        Discipline.Snowboard -> when (clamped) {
            0 -> R.string.skill_level_snowboard_0_desc
            1 -> R.string.skill_level_snowboard_1_desc
            2 -> R.string.skill_level_snowboard_2_desc
            3 -> R.string.skill_level_snowboard_3_desc
            4 -> R.string.skill_level_snowboard_4_desc
            else -> null
        }
        Discipline.Ski, Discipline.Both -> when (clamped) {
            0 -> R.string.skill_level_ski_0_desc
            1 -> R.string.skill_level_ski_1_desc
            2 -> R.string.skill_level_ski_2_desc
            3 -> R.string.skill_level_ski_3_desc
            4 -> R.string.skill_level_ski_4_desc
            else -> null
        }
    }
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
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        ),
        color = TmsColor.Outline,
        modifier = modifier,
    )
}

@Composable
private fun equipmentRentalLabel(rental: EquipmentRental): String =
    when (rental) {
        EquipmentRental.All -> stringResource(R.string.explore_card_equipment_all)
        EquipmentRental.Partial -> stringResource(R.string.explore_card_equipment_partial)
        EquipmentRental.None -> stringResource(R.string.explore_card_equipment_none)
    }

/**
 * Hero block: discipline label + posted time on top, date as hero (22sp
 * bold), duration · group · flexible inline meta below. No color rail here
 * — the rail lives at the outer card Row so it spans full height.
 */
@Composable
private fun HeroBlock(
    request: ExploreLessonRequest,
    isoHalf: Boolean,
) {
    val disciplineLabel = when (request.discipline) {
        Discipline.Ski -> stringResource(R.string.explore_card_discipline_badge_ski)
        Discipline.Snowboard -> stringResource(R.string.explore_card_discipline_badge_snowboard)
        Discipline.Both -> stringResource(R.string.explore_card_discipline_badge_both)
    }
    val disciplineTextColor = TmsColor.OnSurface

    val hasDates = !request.startDate.isNullOrBlank() || !request.endDate.isNullOrBlank()
    val dateHero = run {
        val s = request.startDate
        val e = request.endDate
        when {
            !s.isNullOrBlank() && (e.isNullOrBlank() || dateStringsEqual(s, e)) -> formatDisplayDate(s)
            !s.isNullOrBlank() && !e.isNullOrBlank() ->
                stringResource(R.string.explore_card_date_range, formatDisplayDate(s), formatDisplayDate(e))
            !e.isNullOrBlank() -> formatDisplayDate(e)
            else -> ""
        }
    }

    Column(modifier = Modifier.padding(top = 14.dp, bottom = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DisciplineIcon(request.discipline, tint = disciplineTextColor, size = 14.dp)
                Text(
                    text = disciplineLabel.uppercase(Locale.getDefault()),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp,
                    ),
                    color = disciplineTextColor,
                )
            }
            if (request.skillLevel != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(
                            R.string.explore_card_skill_level_fmt,
                            request.skillLevel.toString(),
                        ),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.4.sp,
                        ),
                        color = TmsColor.OnPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TmsColor.Secondary)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                    val descRes = skillLevelDescriptionRes(request.discipline, request.skillLevel)
                    if (descRes != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(descRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = TmsColor.OnSurfaceVariant,
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }
        }

        if (hasDates && dateHero.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = dateHero,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                ),
                color = TmsColor.OnSurface,
            )
        } else {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.explore_card_dates_any),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp,
                ),
                color = TmsColor.OnSurface,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        InlineMetaRow(request = request, isoHalf = isoHalf, hasDates = hasDates)
    }
}

@Composable
private fun DisciplineIcon(discipline: Discipline, tint: Color, size: androidx.compose.ui.unit.Dp) {
    when (discipline) {
        Discipline.Ski -> Icon(
            Icons.Filled.DownhillSkiing,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size),
        )
        Discipline.Snowboard -> Icon(
            Icons.Filled.Snowboarding,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(size),
        )
        Discipline.Both -> Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            Icon(
                Icons.Filled.DownhillSkiing,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size),
            )
            Icon(
                Icons.Filled.Snowboarding,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size),
            )
        }
    }
}

@Composable
private fun InlineMetaRow(request: ExploreLessonRequest, isoHalf: Boolean, hasDates: Boolean) {
    val parts = mutableListOf<@Composable () -> Unit>()

    if (request.durationDays != null) {
        val durationLabel = if (isoHalf) {
            stringResource(R.string.explore_card_half_day)
        } else {
            stringResource(
                R.string.explore_card_days,
                kotlin.math.round(request.durationDays).toInt().coerceAtLeast(1),
            )
        }
        parts.add {
            Text(
                text = durationLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = TmsColor.OnSurface,
            )
        }
    }

    val groupSuffix = if (request.hasChildren) {
        " · " + stringResource(R.string.explore_card_group_has_children)
    } else ""
    parts.add {
        Text(
            text = stringResource(R.string.explore_card_group_size, request.groupSize) + groupSuffix,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = TmsColor.OnSurface,
        )
    }

    if (request.datesFlexible && hasDates) {
        parts.add {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(
                    Icons.Filled.SwapHoriz,
                    contentDescription = null,
                    tint = TmsColor.Secondary,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = stringResource(R.string.explore_card_dates_flexible),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TmsColor.Secondary,
                )
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        parts.forEachIndexed { idx, block ->
            if (idx > 0) {
                Text(
                    text = " · ",
                    style = MaterialTheme.typography.labelMedium,
                    color = TmsColor.OutlineVariant,
                )
            }
            block()
        }
    }
}

/**
 * Requester block: avatar 36dp on the left; right column has name · posted-time
 * on the first line and (optional) note on the second line.
 */
@Composable
private fun RequesterBlock(request: ExploreLessonRequest, note: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        UserAvatar(
            displayName = request.userDisplayName,
            avatarUrl = request.userAvatarUrl,
            size = 36.dp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = request.userDisplayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TmsColor.OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Text(
                    text = " · " + formatRelativeTime(request.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = TmsColor.Outline,
                )
            }
            if (!note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                NotesBody(note = note)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResortsBlock(
    allRegionsSelected: Boolean,
    resortNames: List<String>,
) {
    if (!allRegionsSelected && resortNames.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(stringResource(R.string.explore_card_label_locations))
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

/**
 * Preferences inset: surface-low panel with icon + label + value rows.
 * Only renders rows that have data; hides whole block if nothing present.
 */
@Composable
private fun PreferencesInset(
    request: ExploreLessonRequest,
    preferredLanguages: List<String>,
) {
    val hasLanguage = preferredLanguages.isNotEmpty()
    val hasEquipment = request.equipmentRental != null
    val hasTransport = request.needsTransport
    val hasCert = request.certPreferences.isNotEmpty()
    if (!hasLanguage && !hasEquipment && !hasTransport && !hasCert) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(TmsColor.SurfaceLow)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (hasLanguage) {
            val resolvedLangs: List<String> = preferredLanguages.map { code ->
                val res = languageLabelRes(code)
                if (res != null) stringResource(res)
                else stringResource(R.string.explore_card_language_other, code)
            }
            PrefRow(
                icon = Icons.Filled.Language,
                label = stringResource(R.string.explore_card_pref_label_language),
                value = resolvedLangs.joinToString(" · "),
            )
        }
        if (hasEquipment) {
            PrefRow(
                icon = Icons.AutoMirrored.Filled.DirectionsRun,
                label = stringResource(R.string.explore_card_pref_label_equipment),
                value = equipmentRentalLabel(request.equipmentRental!!),
            )
        }
        if (hasTransport) {
            PrefRow(
                icon = Icons.Filled.DirectionsCar,
                label = stringResource(R.string.explore_card_pref_label_transport),
                value = stringResource(R.string.explore_card_transport_needed),
            )
            if (!request.transportNote.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.explore_card_transport_note_prefix, request.transportNote),
                    style = MaterialTheme.typography.labelSmall,
                    fontStyle = FontStyle.Italic,
                    color = TmsColor.OnSurfaceVariant,
                    modifier = Modifier.padding(start = 30.dp),
                )
            }
        }
        if (hasCert) {
            PrefRow(
                icon = Icons.Filled.WorkspacePremium,
                label = stringResource(R.string.explore_card_pref_label_cert),
                value = request.certPreferences.joinToString(" · "),
            )
        }
    }
}

@Composable
private fun PrefRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = TmsColor.OnSurface,
) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TmsColor.Outline,
            modifier = Modifier.size(14.dp).padding(top = 2.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TmsColor.Outline,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.widthIn(min = 64.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NotesBody(note: String) {
    val trimmed = note.trim()
    if (trimmed.isEmpty()) return
    var expanded by remember(note) { mutableStateOf(false) }
    var hasOverflow by remember(note) { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = trimmed,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
            color = TmsColor.OnSurfaceVariant,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!expanded && result.hasVisualOverflow && !hasOverflow) {
                    hasOverflow = true
                }
            },
        )
        if (hasOverflow) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(if (expanded) R.string.explore_card_show_less else R.string.explore_card_show_more),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TmsColor.Primary,
                modifier = Modifier.clickable { expanded = !expanded },
            )
        }
    }
}

/**
 * Footer: full-width CTA on top, slots pill below.
 */
@Composable
private fun FooterBlock(
    remaining: Int,
    quotaLimit: Int,
    isUnlockedByMe: Boolean,
    isActive: Boolean,
    myChatRoomId: String?,
    baseTokenCost: Int,
    onUnlockClick: () -> Unit,
    onViewChatClick: (String) -> Unit,
) {
    val showSlotsPill = quotaLimit > 0 || remaining > 0
    Column(modifier = Modifier.fillMaxWidth()) {
        when {
            isUnlockedByMe && myChatRoomId != null -> {
                Button(
                    onClick = { onViewChatClick(myChatRoomId) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TmsColor.Primary,
                        contentColor = TmsColor.OnPrimary,
                    ),
                    shape = RoundedCornerShape(10.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 1.dp,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 13.dp),
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

            isUnlockedByMe -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TmsColor.SurfaceLow)
                        .padding(vertical = 12.dp),
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
                        style = MaterialTheme.typography.labelMedium,
                        color = TmsColor.OnSurfaceVariant,
                    )
                }
            }

            !isUnlockedByMe && isActive && remaining > 0 -> {
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
                            .padding(vertical = 13.dp),
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
                            text = stringResource(R.string.explore_unlock_button, baseTokenCost),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TmsColor.OnPrimary,
                        )
                    }
                }
            }

            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TmsColor.SurfaceContainer)
                        .padding(vertical = 13.dp),
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
        }

        if (showSlotsPill) {
            Spacer(modifier = Modifier.height(10.dp))
            SlotsPill(remaining = remaining, quotaLimit = quotaLimit)
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SlotsPill(remaining: Int, quotaLimit: Int) {
    if (quotaLimit <= 0 && remaining <= 0) return
    val isEmpty = remaining <= 0 && quotaLimit > 0
    val dotColor = when {
        isEmpty -> TmsColor.Error
        remaining == 1 -> TmsColor.Secondary
        else -> TmsColor.Success
    }
    val text = if (isEmpty) {
        stringResource(R.string.explore_card_remaining_slots_zero)
    } else {
        stringResource(R.string.explore_card_quota_remaining, remaining)
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(50))
                .background(dotColor),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (isEmpty) TmsColor.Error else TmsColor.OnSurfaceVariant,
        )
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

    val cardShape = RoundedCornerShape(12.dp)

    val railColor = when (request.discipline) {
        Discipline.Snowboard -> TmsColor.Secondary
        else -> TmsColor.Primary
    }

    Surface(
        color = TmsColor.SurfaceLowest,
        shape = cardShape,
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (dimCard) Modifier.alpha(0.6f) else Modifier),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .drawBehind {
                    drawRect(
                        color = railColor,
                        size = Size(4.dp.toPx(), size.height),
                    )
                },
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 24.dp, end = 20.dp),
            ) {
                HeroBlock(request = request, isoHalf = isoHalf)

                Spacer(modifier = Modifier.height(16.dp))

                val hasResorts = request.allRegionsSelected || request.resortNames.isNotEmpty()
                if (hasResorts) {
                    ResortsBlock(
                        allRegionsSelected = request.allRegionsSelected,
                        resortNames = request.resortNames,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                val hasPrefs = request.preferredLanguages.isNotEmpty() ||
                    request.equipmentRental != null ||
                    request.needsTransport ||
                    request.certPreferences.isNotEmpty()
                if (hasPrefs) {
                    PreferencesInset(
                        request = request,
                        preferredLanguages = request.preferredLanguages,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                RequesterBlock(
                    request = request,
                    note = request.additionalNotes?.takeIf { it.isNotBlank() },
                )

                Spacer(modifier = Modifier.height(16.dp))

                FooterBlock(
                    remaining = remaining,
                    quotaLimit = request.quotaLimit,
                    isUnlockedByMe = request.isUnlockedByMe,
                    isActive = request.status == LessonRequestStatus.Active,
                    myChatRoomId = request.myChatRoomId,
                    baseTokenCost = request.baseTokenCost,
                    onUnlockClick = onUnlockClick,
                    onViewChatClick = onViewChatClick,
                )

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

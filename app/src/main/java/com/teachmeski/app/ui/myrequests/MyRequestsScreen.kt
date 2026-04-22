package com.teachmeski.app.ui.myrequests

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.LessonRequestListItem
import com.teachmeski.app.domain.model.LessonRequestStatus
import com.teachmeski.app.ui.component.EmptyState
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val DIMMED_STATUSES = setOf(
    LessonRequestStatus.ClosedByUser,
    LessonRequestStatus.Expired,
    LessonRequestStatus.ExpiredUnverified,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRequestsScreen(
    onRequestClick: (String) -> Unit,
    onNewRequest: () -> Unit,
    viewModel: MyRequestsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loadError = uiState.error

    // Refresh when returning to this tab (e.g. after posting a new request via the wizard
    // or switching back from another bottom-nav tab). Skip the very first ON_START since
    // the ViewModel already loads in init.
    var skipFirstStart by remember { mutableStateOf(true) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                if (skipFirstStart) {
                    skipFirstStart = false
                } else {
                    viewModel.refreshOnResume()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewRequest,
                containerColor = TmsColor.Primary,
                contentColor = TmsColor.OnPrimary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.my_requests_new_button),
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Text(
                text = stringResource(R.string.nav_my_requests),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TmsColor.OnSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                uiState.isLoading && uiState.requests.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = TmsColor.Primary)
                    }
                }

                uiState.requests.isEmpty() && loadError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = loadError.asString(),
                            color = TmsColor.Error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.refresh() }) {
                            Text(text = stringResource(R.string.common_retry))
                        }
                    }
                }

                uiState.requests.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.my_requests_empty_title),
                        description = stringResource(R.string.my_requests_empty_desc),
                        modifier = Modifier.fillMaxSize(),
                        action = {
                            TextButton(onClick = onNewRequest) {
                                Text(text = stringResource(R.string.my_requests_empty_cta))
                            }
                        },
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(
                            start = 20.dp,
                            end = 20.dp,
                            top = 8.dp,
                            bottom = 88.dp,
                        ),
                    ) {
                        uiState.error?.let { err ->
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = err.asString(),
                                        color = TmsColor.Error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.weight(1f),
                                    )
                                    TextButton(onClick = { viewModel.consumeError() }) {
                                        Text(text = stringResource(R.string.common_close))
                                    }
                                }
                            }
                        }
                        items(uiState.requests, key = { it.id }) { request ->
                            OrderCard(
                                request = request,
                                onClick = { onRequestClick(request.id) },
                            )
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun OrderCard(
    request: LessonRequestListItem,
    onClick: () -> Unit,
) {
    val isDimmed = request.status in DIMMED_STATUSES
    val isGhostCta = request.status in DIMMED_STATUSES

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
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = TmsColor.SurfaceLowest,
        shadowElevation = 4.dp,
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .alpha(if (isDimmed) 0.6f else 1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                StatusPill(status = request.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(
                        id = when (request.discipline) {
                            Discipline.Snowboard -> R.drawable.ic_snowboard
                            Discipline.Ski,
                            Discipline.Both,
                            -> R.drawable.ic_ski
                        },
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = TmsColor.Primary,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = disciplineLabel(request.discipline),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TmsColor.OnSurface,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = formatLessonDates(request),
                style = MaterialTheme.typography.bodyMedium,
                color = TmsColor.OnSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (request.instructorPreviews.isEmpty()) {
                Text(
                    text = stringResource(R.string.my_requests_chat_count_zero),
                    style = MaterialTheme.typography.bodySmall,
                    color = TmsColor.Outline,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val maxVisible = 4
                    request.instructorPreviews.take(maxVisible).forEach { preview ->
                        Box {
                            UserAvatar(
                                displayName = preview.displayName,
                                avatarUrl = preview.avatarUrl,
                                size = 36.dp,
                            )
                            if (preview.hasUnread) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .size(20.dp)
                                        .graphicsLayer { alpha = unreadPulse }
                                        .clip(CircleShape)
                                        .background(TmsColor.Error)
                                        .border(2.dp, TmsColor.SurfaceLowest, CircleShape),
                                )
                            }
                        }
                    }
                    val overflow = request.instructorPreviews.size - maxVisible
                    if (overflow > 0) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = CircleShape,
                            color = TmsColor.Background,
                            border = androidx.compose.foundation.BorderStroke(
                                1.5.dp,
                                TmsColor.Outline,
                            ),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "+$overflow",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TmsColor.OnSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                shape = CircleShape,
                color = if (isGhostCta) Color.Transparent else TmsColor.Primary,
                border = if (isGhostCta) {
                    androidx.compose.foundation.BorderStroke(1.dp, TmsColor.OutlineVariant)
                } else {
                    null
                },
            ) {
                Text(
                    text = stringResource(R.string.my_requests_view_request),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isGhostCta) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (isGhostCta) TmsColor.OnSurfaceVariant else TmsColor.OnPrimary,
                )
            }
        }
    }
}

@Composable
private fun StatusPill(status: LessonRequestStatus) {
    val (label, color) = statusPillStyle(status)
    Surface(
        shape = CircleShape,
        color = color.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

@Composable
private fun statusPillStyle(status: LessonRequestStatus): Pair<String, Color> {
    val label = when (status) {
        LessonRequestStatus.Active -> stringResource(R.string.my_requests_status_active)
        LessonRequestStatus.Expired -> stringResource(R.string.my_requests_status_expired)
        LessonRequestStatus.ClosedByUser -> stringResource(R.string.my_requests_status_closed)
        LessonRequestStatus.PendingEmailVerification ->
            stringResource(R.string.my_requests_status_pending_email_verification)
        LessonRequestStatus.ExpiredUnverified ->
            stringResource(R.string.my_requests_status_expired_unverified)
    }
    val color = when (status) {
        LessonRequestStatus.Active -> TmsColor.Primary
        LessonRequestStatus.PendingEmailVerification -> TmsColor.Warning
        LessonRequestStatus.Expired,
        LessonRequestStatus.ClosedByUser,
        LessonRequestStatus.ExpiredUnverified,
        -> TmsColor.Outline
    }
    return label to color
}

@Composable
private fun disciplineLabel(discipline: Discipline): String =
    when (discipline) {
        Discipline.Ski -> stringResource(R.string.wizard_discipline_ski)
        Discipline.Snowboard -> stringResource(R.string.wizard_discipline_snowboard)
        Discipline.Both -> stringResource(R.string.wizard_discipline_both)
    }

@Composable
private fun formatLessonDates(request: LessonRequestListItem): String {
    val undecided = stringResource(R.string.my_requests_dates_undecided)
    val flexSuffix = stringResource(R.string.my_requests_dates_flexible_suffix)
    val locale = LocalConfiguration.current.locales[0]
    val start = request.dateStart?.takeIf { it.isNotBlank() }
    val end = request.dateEnd?.takeIf { it.isNotBlank() }

    return when {
        request.datesFlexible && start == null -> undecided
        request.datesFlexible && start != null -> {
            val dt = parseDate(start)
            if (dt != null) {
                val pattern = stringResource(R.string.date_format_year_month)
                val fmt = SimpleDateFormat(pattern, locale)
                "${fmt.format(dt)} $flexSuffix"
            } else {
                "$start $flexSuffix"
            }
        }
        start == null -> undecided
        end == null || start == end -> formatDate(start, locale) ?: start
        else -> "${formatDate(start, locale) ?: start} – ${formatDate(end, locale) ?: end}"
    }
}

private fun parseDate(dateStr: String): java.util.Date? {
    return try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(dateStr)
    } catch (_: Exception) {
        null
    }
}

private fun formatDate(dateStr: String, locale: Locale): String? {
    val date = parseDate(dateStr) ?: return null
    return SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, locale).format(date)
}

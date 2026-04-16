package com.teachmeski.app.ui.myrequests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.LessonRequestListItem
import com.teachmeski.app.domain.model.LessonRequestStatus
import com.teachmeski.app.ui.component.EmptyState
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRequestsScreen(
    onRequestClick: (String) -> Unit,
    onNewRequest: () -> Unit,
    viewModel: MyRequestsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loadError = uiState.error

    Scaffold(
        topBar = {
            TmsTopBar(title = stringResource(R.string.my_requests_title))
        },
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
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(16.dp),
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
                            LessonRequestCard(
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

@Composable
private fun LessonRequestCard(
    request: LessonRequestListItem,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = disciplineLabel(request.discipline),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                StatusBadge(status = request.status)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = lessonRequestDateSummary(request),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lessonRequestDurationText(request.durationDays),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = chatCountLabel(request.chatCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.my_requests_created_at_label,
                ) + " " + formatCreatedAt(request.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = TmsColor.Outline,
            )
            if (request.instructorPreviews.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    request.instructorPreviews.take(3).forEachIndexed { index, preview ->
                        if (index > 0) {
                            Spacer(modifier = Modifier.width((-8).dp))
                        }
                        UserAvatar(
                            displayName = preview.displayName,
                            avatarUrl = preview.avatarUrl,
                            size = 32.dp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun disciplineLabel(discipline: Discipline): String =
    when (discipline) {
        Discipline.Ski -> stringResource(R.string.wizard_discipline_ski)
        Discipline.Snowboard -> stringResource(R.string.wizard_discipline_snowboard)
    }

@Composable
private fun lessonRequestDateSummary(request: LessonRequestListItem): String {
    val undecided = stringResource(R.string.my_requests_dates_undecided)
    val flexSuffix = stringResource(R.string.my_requests_dates_flexible_suffix)
    val start = request.dateStart?.takeIf { it.isNotBlank() }
    val end = request.dateEnd?.takeIf { it.isNotBlank() }
    return when {
        request.datesFlexible && start == null -> undecided
        request.datesFlexible && start != null -> {
            val endPart = (end ?: start)
            "$start ~ $endPart $flexSuffix"
        }
        start == null -> undecided
        end == null || start == end -> start
        else -> "$start ~ $end"
    }
}

@Composable
private fun lessonRequestDurationText(durationDays: Double): String {
    if (kotlin.math.abs(durationDays - 0.5) < 1e-6) {
        return stringResource(R.string.wizard_duration_half_day)
    }
    val daysWord = stringResource(R.string.wizard_confirm_days)
    val numStr =
        if (kotlin.math.abs(durationDays - durationDays.toInt().toDouble()) < 1e-6) {
            durationDays.toInt().toString()
        } else {
            durationDays.toString()
        }
    return "$numStr $daysWord"
}

@Composable
private fun chatCountLabel(chatCount: Int): String =
    if (chatCount == 0) {
        stringResource(R.string.my_requests_chat_count_zero)
    } else {
        stringResource(R.string.my_requests_chat_count_fmt, chatCount)
    }

@Composable
private fun StatusBadge(status: LessonRequestStatus) {
    val (label, color) = statusBadgeStyle(status)
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

@Composable
private fun statusBadgeStyle(status: LessonRequestStatus): Pair<String, Color> {
    val label =
        when (status) {
            LessonRequestStatus.Active ->
                stringResource(R.string.my_requests_status_active)
            LessonRequestStatus.Expired ->
                stringResource(R.string.my_requests_status_expired)
            LessonRequestStatus.ClosedByUser ->
                stringResource(R.string.my_requests_status_closed)
        }
    val color =
        when (status) {
            LessonRequestStatus.Active -> TmsColor.Success
            LessonRequestStatus.Expired,
            LessonRequestStatus.ClosedByUser,
            -> TmsColor.Outline
        }
    return label to color
}

@Composable
private fun formatCreatedAt(iso: String): String {
    val locale = LocalConfiguration.current.locales[0]
    val utcParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val date =
        try {
            val normalized = if (iso.length >= 19) iso.take(19) else iso
            utcParser.parse(normalized)
        } catch (_: Exception) {
            null
        }
    if (date == null) return iso
    val out =
        SimpleDateFormat.getDateTimeInstance(
            SimpleDateFormat.MEDIUM,
            SimpleDateFormat.SHORT,
            locale,
        )
    return out.format(date)
}

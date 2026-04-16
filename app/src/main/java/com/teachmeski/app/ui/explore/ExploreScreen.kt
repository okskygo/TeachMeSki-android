package com.teachmeski.app.ui.explore

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.ExploreLessonRequest
import com.teachmeski.app.ui.component.EmptyState
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel = hiltViewModel(),
    onNavigateToChat: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(listState, viewModel) {
        snapshotFlow {
            val info = listState.layoutInfo
            val last = info.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = info.totalItemsCount
            val nearEnd = total > 0 && last >= total - 2
            val s = viewModel.uiState.value
            nearEnd to (s.hasMore && !s.isLoadingMore && !s.isLoading)
        }
            .distinctUntilChanged()
            .collect { (nearEnd, canLoad) ->
                if (nearEnd && canLoad) {
                    viewModel.loadMore()
                }
            }
    }

    LaunchedEffect(uiState.unlockSuccessChatRoomId) {
        val roomId = uiState.unlockSuccessChatRoomId ?: return@LaunchedEffect
        onNavigateToChat(roomId)
        viewModel.consumeUnlockSuccess()
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TmsTopBar(
                title = stringResource(R.string.explore_title),
                actions = {
                    Surface(
                        color = TmsColor.PrimaryFixed,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.wallet_tokens_fmt, uiState.tokenBalance),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TmsColor.Primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TmsColor.Background),
            ) {
                DisciplineFilterRow(
                    selected = uiState.disciplineFilter,
                    onSelect = viewModel::setDisciplineFilter,
                )

                when {
                    uiState.isLoading && uiState.requests.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = TmsColor.Primary)
                        }
                    }

                    uiState.requests.isEmpty() && uiState.error != null -> {
                        val err = uiState.error
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = err?.asString().orEmpty(),
                                color = TmsColor.Error,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(onClick = {
                                viewModel.consumeError()
                                viewModel.loadPage(1)
                            }) {
                                Text(text = stringResource(R.string.common_retry))
                            }
                        }
                    }

                    uiState.requests.isEmpty() -> {
                        EmptyState(
                            title = stringResource(R.string.explore_empty_title),
                            description = stringResource(R.string.explore_empty_description),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            uiState.error?.let { err ->
                                item(key = "error_banner") {
                                    Surface(
                                        color = TmsColor.ErrorContainer,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                        ) {
                                            Text(
                                                text = err.asString(),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TmsColor.Error,
                                                modifier = Modifier.weight(1f),
                                            )
                                            TextButton(onClick = {
                                                viewModel.consumeError()
                                                viewModel.loadPage(1)
                                            }) {
                                                Text(stringResource(R.string.common_retry))
                                            }
                                        }
                                    }
                                }
                            }
                            items(
                                items = uiState.requests,
                                key = { it.id },
                            ) { request ->
                                ExploreRequestCard(
                                    request = request,
                                    onUnlockClick = { viewModel.openUnlockDialog(request) },
                                    onViewChatClick = { roomId -> onNavigateToChat(roomId) },
                                )
                            }
                            if (uiState.isLoadingMore) {
                                item(key = "loading_more") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = TmsColor.Primary,
                                            strokeWidth = 2.dp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            uiState.unlockDialogRequest?.let { req ->
                UnlockDialog(
                    request = req,
                    message = uiState.unlockMessage,
                    onMessageChange = viewModel::setUnlockMessage,
                    isUnlocking = uiState.isUnlocking,
                    error = uiState.unlockError,
                    onConfirm = { viewModel.confirmUnlock() },
                    onDismiss = { viewModel.closeUnlockDialog() },
                )
            }
        }
    }
}

@Composable
private fun DisciplineFilterRow(
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(R.string.explore_filter_all_disciplines)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TmsColor.Primary,
                selectedLabelColor = TmsColor.OnPrimary,
            ),
        )
        FilterChip(
            selected = selected == "ski",
            onClick = { onSelect("ski") },
            label = { Text(stringResource(R.string.explore_filter_ski)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TmsColor.Primary,
                selectedLabelColor = TmsColor.OnPrimary,
            ),
        )
        FilterChip(
            selected = selected == "snowboard",
            onClick = { onSelect("snowboard") },
            label = { Text(stringResource(R.string.explore_filter_snowboard)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TmsColor.Primary,
                selectedLabelColor = TmsColor.OnPrimary,
            ),
        )
    }
}

@Composable
private fun ExploreRequestCard(
    request: ExploreLessonRequest,
    onUnlockClick: () -> Unit,
    onViewChatClick: (String) -> Unit,
) {
    val disciplineLabel = when (request.discipline) {
        Discipline.Ski -> stringResource(R.string.common_discipline_ski)
        Discipline.Snowboard -> stringResource(R.string.common_discipline_snowboard)
        Discipline.Both -> stringResource(R.string.common_discipline_both)
    }
    val skillLabel = stringResource(
        R.string.explore_card_skill_level_fmt,
        request.skillLevel?.toString()
            ?: stringResource(R.string.common_empty_value),
    )
    val resortLine = if (request.allRegionsSelected && request.resortNames.isEmpty()) {
        stringResource(R.string.wizard_resort_all_regions)
    } else {
        request.resortNames.joinToString(stringResource(R.string.common_list_separator))
    }
    val quotaRemaining = kotlin.math.max(0, request.quotaLimit - request.unlockCount)

    Surface(
        color = TmsColor.SurfaceLowest,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                UserAvatar(
                    displayName = request.userDisplayName,
                    avatarUrl = request.userAvatarUrl,
                    size = 48.dp,
                )
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.userDisplayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TmsColor.OnSurface,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = TmsColor.SurfaceLow,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = disciplineLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = TmsColor.OnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
                Surface(
                    color = TmsColor.PrimaryFixed,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.explore_card_cost_tokens, request.baseTokenCost),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TmsColor.Primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = skillLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = TmsColor.OnSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.explore_card_group_size, request.groupSize),
                style = MaterialTheme.typography.bodyMedium,
                color = TmsColor.OnSurfaceVariant,
            )
            if (resortLine.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = resortLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TmsColor.OnSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.explore_card_quota_remaining, quotaRemaining),
                style = MaterialTheme.typography.labelMedium,
                color = TmsColor.Secondary,
            )
            Spacer(modifier = Modifier.height(12.dp))
            when {
                !request.isUnlockedByMe -> {
                    Button(
                        onClick = onUnlockClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TmsColor.Primary,
                            contentColor = TmsColor.OnPrimary,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.explore_unlock_button, request.baseTokenCost))
                    }
                }

                request.myChatRoomId != null -> {
                    Button(
                        onClick = { onViewChatClick(request.myChatRoomId!!) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TmsColor.Primary,
                            contentColor = TmsColor.OnPrimary,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.explore_card_view_chat))
                    }
                }

                else -> {
                    Text(
                        text = stringResource(R.string.explore_card_already_unlocked),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TmsColor.OnSurfaceVariant,
                    )
                }
            }
        }
    }
}

package com.teachmeski.app.ui.unlocked

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.UnlockedRoom
import com.teachmeski.app.ui.component.EmptyState
import com.teachmeski.app.ui.component.TmsTopBar
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockedScreen(
    viewModel: UnlockedViewModel = hiltViewModel(),
    onNavigateToChat: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val loadError = uiState.error

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TmsTopBar(title = stringResource(R.string.unlocked_title))
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.load(isRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading && uiState.rooms.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TmsColor.Background),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = TmsColor.Primary)
                    }
                }

                uiState.rooms.isEmpty() && loadError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TmsColor.Background)
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
                        TextButton(onClick = {
                            viewModel.consumeError()
                            viewModel.load(isRefresh = false)
                        }) {
                            Text(text = stringResource(R.string.common_retry))
                        }
                    }
                }

                uiState.rooms.isEmpty() -> {
                    EmptyState(
                        title = stringResource(R.string.unlocked_empty_title),
                        description = stringResource(R.string.unlocked_empty_description),
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TmsColor.Background),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(TmsColor.Background),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        loadError?.let { err ->
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
                                            viewModel.load(isRefresh = false)
                                        }) {
                                            Text(stringResource(R.string.common_retry))
                                        }
                                    }
                                }
                            }
                        }
                        items(
                            items = uiState.rooms,
                            key = { it.roomId },
                        ) { room ->
                            UnlockedRoomCard(
                                room = room,
                                onClick = { onNavigateToChat(room.roomId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnlockedRoomCard(
    room: UnlockedRoom,
    onClick: () -> Unit,
) {
    val disciplineLabel = when (room.discipline) {
        Discipline.Ski -> stringResource(R.string.common_discipline_ski)
        Discipline.Snowboard -> stringResource(R.string.common_discipline_snowboard)
        Discipline.Both -> stringResource(R.string.common_discipline_both)
    }
    val previewText = room.lastMessageContent?.takeIf { it.isNotBlank() }
        ?: stringResource(R.string.unlocked_card_last_message_none)

    Surface(
        color = TmsColor.SurfaceLowest,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                UserAvatar(
                    displayName = room.studentDisplayName,
                    avatarUrl = room.studentAvatarUrl,
                    size = 48.dp,
                )
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = room.studentDisplayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TmsColor.OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
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
                if (room.hasUnread) {
                    Surface(
                        color = TmsColor.PrimaryFixed,
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.unlocked_card_unread),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TmsColor.Primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = previewText,
                style = MaterialTheme.typography.bodyMedium,
                color = TmsColor.OnSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

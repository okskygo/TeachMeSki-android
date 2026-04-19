package com.teachmeski.app.ui.chat

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.ChatRoom
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.util.RelativeTime
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomListScreen(
    isInstructorView: Boolean,
    onRoomClick: (roomId: String) -> Unit,
    onEmptyCtaClick: () -> Unit,
    viewModel: ChatRoomListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    val displayRooms = remember(uiState.rooms, uiState.selectedTab) {
        when (uiState.selectedTab) {
            ChatRoomListUiState.Tab.All -> uiState.rooms
            ChatRoomListUiState.Tab.Unread ->
                uiState.rooms.filter { it.unreadCount > 0 }
        }
    }

    // TODO(phase5): realtime inbox subscription (broadcast room_updated → refresh rows / unread)

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.refreshOnResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(listState, displayRooms.size, viewModel) {
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

    val titleRes = if (isInstructorView) {
        R.string.chat_title_instructor
    } else {
        R.string.chat_title_seeker
    }

    Scaffold(
        containerColor = TmsColor.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TmsColor.Background),
        ) {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TmsColor.OnSurface,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            val tabIndex = when (uiState.selectedTab) {
                ChatRoomListUiState.Tab.All -> 0
                ChatRoomListUiState.Tab.Unread -> 1
            }
            PrimaryTabRow(
                selectedTabIndex = tabIndex,
                containerColor = TmsColor.SurfaceLowest,
                contentColor = TmsColor.Primary,
            ) {
                Tab(
                    selected = uiState.selectedTab == ChatRoomListUiState.Tab.All,
                    onClick = { viewModel.setTab(ChatRoomListUiState.Tab.All) },
                    text = {
                        Text(text = stringResource(R.string.chat_tab_all))
                    },
                )
                Tab(
                    selected = uiState.selectedTab == ChatRoomListUiState.Tab.Unread,
                    onClick = { viewModel.setTab(ChatRoomListUiState.Tab.Unread) },
                    text = {
                        Text(text = stringResource(R.string.chat_tab_unread))
                    },
                )
            }

            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = { viewModel.pullToRefresh() },
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    uiState.rooms.isEmpty() && (uiState.isLoading || uiState.isRefreshing) -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(TmsColor.Background),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = TmsColor.Primary)
                        }
                    }

                    uiState.rooms.isEmpty() && uiState.error != null -> {
                        val err = uiState.error
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(TmsColor.Background)
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
                            TextButton(
                                onClick = {
                                    viewModel.consumeError()
                                    viewModel.loadPage(reset = true)
                                },
                            ) {
                                Text(text = stringResource(R.string.common_retry))
                            }
                        }
                    }

                    displayRooms.isEmpty() &&
                        uiState.rooms.isEmpty() &&
                        !uiState.isLoading &&
                        !uiState.isRefreshing -> {
                        ChatListEmptyState(
                            isInstructorView = isInstructorView,
                            onEmptyCtaClick = onEmptyCtaClick,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(TmsColor.Background),
                        )
                    }

                    displayRooms.isEmpty() &&
                        uiState.rooms.isNotEmpty() &&
                        !uiState.isLoading -> {
                        // Unread tab filtered to zero — still allow pull; show subtle empty
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(TmsColor.Background),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = stringResource(R.string.chat_empty_unread_title),
                                style = MaterialTheme.typography.bodyMedium,
                                color = TmsColor.OnSurfaceVariant,
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(TmsColor.Background),
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
                                            TextButton(
                                                onClick = {
                                                    viewModel.consumeError()
                                                    viewModel.loadPage(reset = true)
                                                },
                                            ) {
                                                Text(stringResource(R.string.common_retry))
                                            }
                                        }
                                    }
                                }
                            }
                            items(
                                items = displayRooms,
                                key = { it.id },
                            ) { room ->
                                ChatRoomCard(
                                    room = room,
                                    onClick = { onRoomClick(room.id) },
                                    relativeTime = RelativeTime.format(room.lastMessageAt, context),
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
        }
    }
}

@Composable
private fun ChatListEmptyState(
    isInstructorView: Boolean,
    onEmptyCtaClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleRes = if (isInstructorView) {
        R.string.chat_empty_instructor_title
    } else {
        R.string.chat_empty_seeker_title
    }
    val ctaRes = if (isInstructorView) {
        R.string.chat_empty_instructor_cta
    } else {
        R.string.chat_empty_seeker_cta
    }

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = TmsColor.OnSurface,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onEmptyCtaClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = TmsColor.Primary,
                contentColor = TmsColor.OnPrimary,
            ),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(text = stringResource(ctaRes))
        }
    }
}

@Composable
private fun ChatRoomCard(
    room: ChatRoom,
    onClick: () -> Unit,
    relativeTime: String,
) {
    Surface(
        color = TmsColor.SurfaceLowest,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            UserAvatar(
                displayName = room.otherPartyName,
                avatarUrl = room.otherPartyAvatarUrl,
                size = 48.dp,
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = room.otherPartyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TmsColor.OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    room.discipline?.let { d ->
                        DisciplineChip(disciplineRaw = d)
                    }
                    if (relativeTime.isNotBlank()) {
                        Text(
                            text = relativeTime,
                            style = MaterialTheme.typography.bodySmall,
                            color = TmsColor.Outline,
                            maxLines = 1,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val preview = room.lastMessage?.takeIf { it.isNotBlank() }
                    Text(
                        text = preview
                            ?: stringResource(R.string.chat_last_message_none),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TmsColor.OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (room.unreadCount > 0) {
                        UnreadBadge(count = room.unreadCount)
                    }
                }
            }
        }
    }
}

@Composable
private fun DisciplineChip(disciplineRaw: String) {
    val d = disciplineRaw.lowercase(Locale.US)
    val labelRes = when {
        d.contains("snow") -> R.string.chat_badge_snowboard
        else -> R.string.chat_badge_ski
    }
    Surface(
        color = TmsColor.PrimaryFixed,
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = TmsColor.Primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    val label = if (count > 9) {
        stringResource(R.string.chat_unread_overflow)
    } else {
        count.toString()
    }
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(TmsColor.Error, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = TmsColor.OnPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            maxLines = 1,
        )
    }
}

package com.teachmeski.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.InfoPanelData
import com.teachmeski.app.ui.theme.TmsColor
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    onNavigateToInstructor: (String) -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val detail = uiState.roomDetail
    val otherParty = detail?.otherParty

    val isAtBottom by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisible == null || lastVisible.index >= layoutInfo.totalItemsCount - 1
        }
    }

    var hasInitialScrolled by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading, uiState.messages.size) {
        val count = uiState.messages.size
        if (!hasInitialScrolled && !uiState.isLoading && count > 0) {
            listState.scrollToItem(count - 1)
            hasInitialScrolled = true
        }
    }

    LaunchedEffect(uiState.messages.size) {
        val count = uiState.messages.size
        if (!hasInitialScrolled || count == 0) return@LaunchedEffect
        // Skip auto-scroll-to-bottom when the size grew because of an
        // older-message prepend; in that case we let the dedicated
        // prepend-anchor effect (below) restore the user's reading
        // position to the row they were on before pagination.
        if (uiState.prependAnchorMessageId != null) return@LaunchedEffect
        // Always scroll when the new tail row is sent by the current
        // user (matches user spec: "發送訊息時，自動 scroll to
        // bottom"). For incoming messages, only scroll when the user
        // is parked at the bottom — otherwise keep the viewport pinned
        // so they can keep reading older messages.
        val tail = uiState.messages.lastOrNull()
        val ownTail = tail != null && tail.senderId == uiState.currentUserId
        if (ownTail || isAtBottom) {
            listState.animateScrollToItem(count - 1)
        }
    }

    // After an older-message page is prepended, restore the scroll so
    // the row that used to be first-visible stays in roughly the same
    // place (anchor at offset 0). Without this, LazyColumn keeps
    // `firstVisibleItemIndex == 0` and the viewport jumps to the
    // newly-loaded oldest message at the top of the list.
    LaunchedEffect(uiState.prependAnchorMessageId) {
        val anchorId = uiState.prependAnchorMessageId ?: return@LaunchedEffect
        val index = uiState.messages.indexOfFirst { it.id == anchorId }
        if (index >= 0) {
            listState.scrollToItem(index, scrollOffset = 0)
        }
        viewModel.consumePrependAnchor()
    }

    LaunchedEffect(listState, hasInitialScrolled) {
        if (!hasInitialScrolled) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { index ->
                if (index == 0) {
                    val s = viewModel.uiState.value
                    if (s.hasMoreOlder && !s.isLoadingOlder && s.messages.isNotEmpty()) {
                        viewModel.loadOlderMessages()
                    }
                }
            }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TmsColor.SurfaceLowest,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ChatHeader(
                otherPartyName = otherParty?.name,
                otherPartyAvatarUrl = otherParty?.avatarUrl,
                isLoaded = otherParty != null,
                infoPanelExpanded = uiState.infoPanelExpanded,
                onBack = onNavigateBack,
                onToggleInfo = viewModel::toggleInfoPanel,
            )
        },
        bottomBar = {
            val needsUnlockPlaceholder =
                detail?.needsUnlock == true && detail.unlockInfo != null
            val blocked =
                uiState.isBlockedByMe ||
                    uiState.isBlockedByOther ||
                    detail?.isBlocked == true

            when {
                needsUnlockPlaceholder -> {
                    ChatUnlockBar(
                        unlockInfo = detail!!.unlockInfo!!,
                        onUnlockClick = viewModel::showUnlockDialog,
                    )
                }
                blocked -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = TmsColor.SurfaceLowest,
                    ) {
                        Text(
                            text = stringResource(R.string.chat_blocked_notice),
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TmsColor.OnSurfaceVariant,
                        )
                    }
                }
                else -> {
                    ChatInput(
                        onSend = viewModel::sendMessage,
                        isSending = uiState.isSending,
                    )
                }
            }
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TmsColor.SurfaceLow),
        ) {
            val infoPanelMaxHeight = maxHeight * 0.6f

            when {
                uiState.isLoading && uiState.messages.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = TmsColor.Primary)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        uiState.error?.let { err ->
                            item(key = "error_banner") {
                                Surface(
                                    color = TmsColor.ErrorContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
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
                                                viewModel.refresh()
                                            },
                                        ) {
                                            Text(stringResource(R.string.common_retry))
                                        }
                                    }
                                }
                            }
                        }

                        if (uiState.isLoadingOlder) {
                            item(key = "loading_older") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(28.dp),
                                        color = TmsColor.Primary,
                                        strokeWidth = 2.dp,
                                    )
                                }
                            }
                        }

                        if (uiState.messages.isEmpty() && !uiState.isLoading) {
                            item(key = "empty") {
                                Text(
                                    text = stringResource(R.string.chat_no_messages),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TmsColor.OnSurfaceVariant,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp),
                                )
                            }
                        }

                        items(
                            items = uiState.messages,
                            key = { it.id },
                        ) { message ->
                            MessageBubble(
                                message = message,
                                isOwn = message.senderId == uiState.currentUserId,
                                otherPartyAvatarUrl = otherParty?.avatarUrl,
                                otherPartyName = otherParty?.name ?: "",
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = uiState.infoPanelExpanded,
                modifier = Modifier.align(Alignment.TopCenter),
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
            ) {
                val info = detail?.infoPanelData
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = infoPanelMaxHeight)
                        .background(TmsColor.SurfaceLow)
                        .verticalScroll(rememberScrollState()),
                ) {
                    when (info) {
                        is InfoPanelData.StudentPanel -> StudentInfoPanel(
                            data = info,
                            isBlockedByMe = uiState.isBlockedByMe,
                            hasSentMessage = detail?.hasSentMessage == true,
                            onReviewClick = viewModel::showReviewDialog,
                            onNavigateToInstructor = onNavigateToInstructor,
                            onBlockToggle = viewModel::toggleBlock,
                            onReportClick = viewModel::showReportDialog,
                        )
                        is InfoPanelData.InstructorPanel -> InstructorInfoPanel(
                            data = info,
                            isBlockedByMe = uiState.isBlockedByMe,
                            onBlockToggle = viewModel::toggleBlock,
                            onReportClick = viewModel::showReportDialog,
                        )
                        null -> Unit
                    }
                }
            }

        }
    }

    if (uiState.showReviewDialog) {
        ReviewDialog(
            isSubmitting = uiState.isSubmittingReview,
            onDismiss = viewModel::dismissReviewDialog,
            onSubmit = viewModel::submitReview,
        )
    }

    if (uiState.showReportDialog) {
        ReportDialog(
            isSubmitting = uiState.isSubmittingReport,
            onDismiss = viewModel::dismissReportDialog,
            onSubmit = viewModel::submitReport,
        )
    }

    if (uiState.showUnlockDialog) {
        ChatUnlockDialog(
            messageDraft = uiState.unlockMessageDraft,
            isUnlocking = uiState.isUnlocking,
            onDraftChange = viewModel::updateUnlockMessage,
            onDismiss = viewModel::dismissUnlockDialog,
            onConfirm = viewModel::confirmUnlock,
        )
    }
}

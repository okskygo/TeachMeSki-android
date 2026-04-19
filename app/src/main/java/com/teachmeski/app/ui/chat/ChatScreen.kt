package com.teachmeski.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.animation.AnimatedVisibility
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

    LaunchedEffect(uiState.messages.size) {
        val count = uiState.messages.size
        if (count > 0) {
            listState.animateScrollToItem(count - 1)
        }
    }

    LaunchedEffect(listState) {
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
        containerColor = TmsColor.Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            ChatHeader(
                otherPartyName = otherParty?.name ?: "",
                otherPartyAvatarUrl = otherParty?.avatarUrl,
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
                        color = TmsColor.SurfaceLow,
                    ) {
                        Text(
                            text = stringResource(R.string.chat_blocked_notice),
                            modifier = Modifier.padding(16.dp),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TmsColor.SurfaceLow),
        ) {
            AnimatedVisibility(visible = uiState.infoPanelExpanded) {
                val info = detail?.infoPanelData
                when (info) {
                    is InfoPanelData.StudentPanel -> StudentInfoPanel(
                        data = info,
                        isBlockedByMe = uiState.isBlockedByMe,
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

            when {
                uiState.isLoading && uiState.messages.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = TmsColor.Primary)
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
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

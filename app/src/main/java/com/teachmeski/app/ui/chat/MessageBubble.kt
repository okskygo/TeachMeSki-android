package com.teachmeski.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.ChatMessage
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.util.RelativeTime

@Composable
fun MessageBubble(
    message: ChatMessage,
    isOwn: Boolean,
    otherPartyAvatarUrl: String?,
    otherPartyName: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bubbleAlpha = if (message.isOptimistic) 0.5f else 1f
    val maxBubbleWidth = LocalConfiguration.current.screenWidthDp.dp * 0.75f

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top,
    ) {
        if (!isOwn) {
            UserAvatar(
                displayName = otherPartyName,
                avatarUrl = otherPartyAvatarUrl,
                size = 32.dp,
                modifier = Modifier.padding(end = 8.dp),
            )
        }

        val bubble: @Composable () -> Unit = {
            Box(
                modifier = Modifier
                    .widthIn(max = maxBubbleWidth)
                    .then(
                        if (isOwn) {
                            Modifier.background(
                                color = TmsColor.Primary,
                                shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                            )
                        } else {
                            Modifier.background(
                                color = TmsColor.SurfaceLowest,
                                shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp),
                            )
                        },
                    )
                    .alpha(bubbleAlpha)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isOwn) TmsColor.OnPrimary else TmsColor.OnSurface,
                )
            }
        }

        val timestamp: @Composable () -> Unit = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 6.dp),
            ) {
                Text(
                    text = RelativeTime.format(message.sentAt, context),
                    style = MaterialTheme.typography.bodySmall,
                    color = TmsColor.Outline,
                )
                if (message.isFailed) {
                    Text(
                        text = "●",
                        style = MaterialTheme.typography.labelSmall,
                        color = TmsColor.Error,
                    )
                    Text(
                        text = stringResource(R.string.chat_send_error),
                        style = MaterialTheme.typography.labelSmall,
                        color = TmsColor.Error,
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.Bottom) {
            if (isOwn) {
                timestamp()
                bubble()
            } else {
                bubble()
                timestamp()
            }
        }
    }
}

package com.teachmeski.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.InfoPanelData
import com.teachmeski.app.ui.component.ExpandableText
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun StudentInfoPanel(
    data: InfoPanelData.StudentPanel,
    isBlockedByMe: Boolean,
    onReviewClick: () -> Unit,
    onNavigateToInstructor: (String) -> Unit,
    onBlockToggle: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TmsColor.SurfaceLowest)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(
                displayName = data.instructorName,
                avatarUrl = data.instructorAvatarUrl,
                size = 72.dp,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = data.instructorName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TmsColor.OnSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                val ratingCount = data.instructorRatingCount
                val ratingAvg = data.instructorRatingAvg
                if (ratingCount > 0 && ratingAvg != null) {
                    Text(
                        text = stringResource(
                            R.string.chat_panel_rating_fmt,
                            ratingAvg,
                            ratingCount,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = TmsColor.OnSurfaceVariant,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.chat_panel_new_instructor),
                        style = MaterialTheme.typography.bodySmall,
                        color = TmsColor.OnSurfaceVariant,
                    )
                }
            }
        }

        if (data.isReviewed) {
            Text(
                text = stringResource(R.string.chat_panel_reviewed),
                style = MaterialTheme.typography.labelMedium,
                color = TmsColor.Success,
            )
        } else {
            OutlinedButton(
                onClick = onReviewClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.chat_panel_review_btn))
            }
        }

        if (!data.instructorBio.isNullOrBlank()) {
            ExpandableText(
                text = data.instructorBio,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        TextButton(onClick = { onNavigateToInstructor(data.instructorId) }) {
            Text(stringResource(R.string.chat_panel_view_profile))
        }

        TonalDivider()

        Text(
            text = stringResource(R.string.chat_panel_section_lesson_request),
            style = MaterialTheme.typography.labelMedium,
            color = TmsColor.OnSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
        )
        LessonRequestFields(request = data.lessonRequest)

        TonalDivider()

        BlockReportActionsPlaceholder(
            isBlockedByMe = isBlockedByMe,
            onBlockToggle = onBlockToggle,
            onReportClick = onReportClick,
        )
    }
}

@Composable
internal fun TonalDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(TmsColor.SurfaceHigh),
    )
}

@Composable
internal fun BlockReportActionsPlaceholder(
    isBlockedByMe: Boolean,
    onBlockToggle: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(onClick = onBlockToggle) {
            Text(
                text = stringResource(
                    if (isBlockedByMe) R.string.unblock_user else R.string.block_user,
                ),
                color = TmsColor.Error,
            )
        }
        TextButton(onClick = onReportClick) {
            Text(
                text = stringResource(R.string.report_user),
                color = TmsColor.Error,
            )
        }
    }
}

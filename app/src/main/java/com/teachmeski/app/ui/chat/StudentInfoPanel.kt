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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.InfoPanelData
import com.teachmeski.app.ui.component.ExpandableText
import com.teachmeski.app.ui.component.IdentityVerifiedBadge
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun StudentInfoPanel(
    data: InfoPanelData.StudentPanel,
    isBlockedByMe: Boolean,
    hasSentMessage: Boolean,
    onReviewClick: () -> Unit,
    onNavigateToInstructor: (String) -> Unit,
    onBlockToggle: () -> Unit,
    onReportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        InstructorHeaderCard(
            data = data,
            hasSentMessage = hasSentMessage,
            onReviewClick = onReviewClick,
            onNavigateToInstructor = onNavigateToInstructor,
        )

        LessonRequestCard(request = data.lessonRequest)

        BlockReportCard(
            isBlockedByMe = isBlockedByMe,
            onBlockToggle = onBlockToggle,
            onReportClick = onReportClick,
        )
    }
}

@Composable
private fun InstructorHeaderCard(
    data: InfoPanelData.StudentPanel,
    hasSentMessage: Boolean,
    onReviewClick: () -> Unit,
    onNavigateToInstructor: (String) -> Unit,
) {
    PanelCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(
                displayName = data.instructorName,
                avatarUrl = data.instructorAvatarUrl,
                size = 72.dp,
            )
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = data.instructorName,
                    fontSize = 20.sp,
                    color = TmsColor.OnSurface,
                    fontWeight = FontWeight.SemiBold,
                )
                IdentityVerifiedBadge(
                    verified = data.instructorPhoneVerifiedAt != null,
                    verifiedLabel = stringResource(R.string.identity_verified_label),
                    unverifiedLabel = stringResource(R.string.identity_unverified_label),
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            val ratingCount = data.instructorRatingCount
            val ratingAvg = data.instructorRatingAvg
            if (ratingCount > 0 && ratingAvg != null) {
                Text(
                    text = stringResource(R.string.chat_panel_rating_short_fmt, ratingAvg),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TmsColor.OnSurfaceVariant,
                )
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TmsColor.Success.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.chat_panel_new_instructor),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = TmsColor.Success,
                    )
                }
            }

            Spacer(Modifier.width(12.dp))
            Text(
                text = "|",
                color = TmsColor.OutlineVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.width(12.dp))

            if (data.isReviewed) {
                Text(
                    text = stringResource(R.string.chat_panel_reviewed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TmsColor.OnSurfaceVariant,
                )
            } else {
                val reviewEnabled = hasSentMessage
                TextButton(
                    onClick = onReviewClick,
                    enabled = reviewEnabled,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 0.dp,
                        vertical = 0.dp,
                    ),
                ) {
                    Text(
                        text = stringResource(
                            if (reviewEnabled) R.string.chat_panel_review_btn
                            else R.string.chat_panel_review_disabled_hint,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (reviewEnabled) TmsColor.Primary else TmsColor.OnSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }
        }

        if (!data.instructorBio.isNullOrBlank()) {
            Spacer(Modifier.height(12.dp))
            ExpandableText(
                text = data.instructorBio,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = { onNavigateToInstructor(data.instructorId) },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, SolidColor(TmsColor.Primary)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TmsColor.Primary),
        ) {
            Text(
                text = stringResource(R.string.chat_panel_view_profile),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun LessonRequestCard(
    request: com.teachmeski.app.domain.model.LessonRequestDisplay,
) {
    PanelCard {
        Text(
            text = stringResource(R.string.chat_panel_my_request_title),
            style = MaterialTheme.typography.labelSmall,
            color = TmsColor.OnSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(12.dp))
        LessonRequestFields(request = request)
    }
}

@Composable
private fun BlockReportCard(
    isBlockedByMe: Boolean,
    onBlockToggle: () -> Unit,
    onReportClick: () -> Unit,
) {
    PanelCard(verticalPadding = 8.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
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
}

@Composable
private fun PanelCard(
    verticalPadding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TmsColor.SurfaceLowest)
            .padding(horizontal = 16.dp, vertical = verticalPadding),
    ) {
        content()
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

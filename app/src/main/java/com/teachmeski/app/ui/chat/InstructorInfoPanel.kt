package com.teachmeski.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.InfoPanelData
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun InstructorInfoPanel(
    data: InfoPanelData.InstructorPanel,
    isBlockedByMe: Boolean,
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
                displayName = data.seekerName,
                avatarUrl = data.seekerAvatarUrl,
                size = 72.dp,
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = data.seekerName,
                style = MaterialTheme.typography.titleMedium,
                color = TmsColor.OnSurface,
                fontWeight = FontWeight.SemiBold,
            )
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

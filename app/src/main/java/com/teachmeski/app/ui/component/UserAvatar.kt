package com.teachmeski.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun UserAvatar(
    displayName: String?,
    avatarUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    if (!avatarUrl.isNullOrBlank()) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape),
        )
    } else {
        val initial = displayName?.firstOrNull()?.uppercase() ?: "?"
        val fontSize = when {
            size >= 96.dp -> 36.sp
            size >= 48.dp -> 20.sp
            else -> 14.sp
        }
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(TmsColor.PrimaryFixed),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                color = TmsColor.Primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = fontSize,
            )
        }
    }
}

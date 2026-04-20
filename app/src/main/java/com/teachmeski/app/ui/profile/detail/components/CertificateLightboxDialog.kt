package com.teachmeski.app.ui.profile.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.teachmeski.app.R
import com.teachmeski.app.ui.theme.TmsColor

private const val SWIPE_THRESHOLD_DP = 48

@Composable
fun CertificateLightboxDialog(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit,
) {
    if (images.isEmpty()) {
        onDismiss()
        return
    }
    var idx by remember { mutableIntStateOf(initialIndex.coerceIn(0, images.lastIndex)) }
    val currentIdx by rememberUpdatedState(idx)
    val lastIndex = images.lastIndex

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        // Scrim layer: clicking the empty background dismisses the lightbox.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TmsColor.InverseSurface.copy(alpha = 0.92f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Image layer: swallows taps (tapping the image does NOT dismiss)
            // and handles horizontal swipe to navigate between images.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .pointerInput(lastIndex) {
                        val thresholdPx = SWIPE_THRESHOLD_DP.dp.toPx()
                        var dragAccum = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragAccum = 0f },
                            onDragEnd = {
                                if (dragAccum <= -thresholdPx && currentIdx < lastIndex) {
                                    idx = currentIdx + 1
                                } else if (dragAccum >= thresholdPx && currentIdx > 0) {
                                    idx = currentIdx - 1
                                }
                                dragAccum = 0f
                            },
                            onDragCancel = { dragAccum = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                dragAccum += dragAmount
                            },
                        )
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    ),
                contentAlignment = Alignment.Center,
            ) {
                AsyncImage(
                    model = images[idx],
                    contentDescription = stringResource(R.string.instructor_detail_certificates_title),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (images.size > 1) {
                Text(
                    text = "${idx + 1} / ${images.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TmsColor.InverseOnSurface.copy(alpha = 0.75f),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 24.dp),
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(TmsColor.InverseOnSurface.copy(alpha = 0.12f))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.instructor_detail_lightbox_close),
                    tint = TmsColor.InverseOnSurface,
                    modifier = Modifier.size(22.dp),
                )
            }

            if (idx > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(12.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TmsColor.InverseOnSurface.copy(alpha = 0.12f))
                        .clickable { idx-- },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = stringResource(R.string.instructor_detail_lightbox_prev),
                        tint = TmsColor.InverseOnSurface,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }

            if (idx < lastIndex) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(12.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(TmsColor.InverseOnSurface.copy(alpha = 0.12f))
                        .clickable { idx++ },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.instructor_detail_lightbox_next),
                        tint = TmsColor.InverseOnSurface,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
}

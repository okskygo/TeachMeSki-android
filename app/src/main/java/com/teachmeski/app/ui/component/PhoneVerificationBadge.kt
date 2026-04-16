package com.teachmeski.app.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teachmeski.app.R
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun PhoneVerificationBadge(
    verified: Boolean,
    modifier: Modifier = Modifier,
    verifiedLabel: String = stringResource(R.string.request_detail_phone_verified),
    unverifiedLabel: String = stringResource(R.string.request_detail_phone_unverified),
) {
    val color = if (verified) TmsColor.Success else TmsColor.Outline
    val bgColor = if (verified) TmsColor.Success.copy(alpha = 0.1f) else Color.Transparent
    val borderColor = if (verified) TmsColor.Success.copy(alpha = 0.4f) else TmsColor.OutlineVariant.copy(alpha = 0.6f)

    val shape = RoundedCornerShape(4.dp)
    val borderModifier = if (verified) {
        Modifier.border(1.dp, borderColor, shape)
    } else {
        Modifier.dashedBorder(1.dp, borderColor, shape)
    }

    Surface(
        modifier = modifier.then(borderModifier),
        shape = shape,
        color = bgColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            CheckCircleIcon(
                verified = verified,
                tint = color,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = if (verified) verifiedLabel else unverifiedLabel,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = color,
            )
        }
    }
}

@Composable
private fun CheckCircleIcon(
    verified: Boolean,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width * 6f / 14f

        val circleStroke = if (verified) {
            Stroke(width = size.width * 1.2f / 14f)
        } else {
            Stroke(
                width = size.width * 1.2f / 14f,
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(size.width * 2.5f / 14f, size.width * 2f / 14f),
                    0f,
                ),
            )
        }

        if (verified) {
            drawCircle(
                color = tint.copy(alpha = 0.15f),
                radius = r,
                center = Offset(cx, cy),
            )
        }

        drawCircle(
            color = tint,
            radius = r,
            center = Offset(cx, cy),
            style = circleStroke,
        )

        val checkStroke = Stroke(
            width = size.width * 1.4f / 14f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(size.width * 4.5f / 14f, size.height * 7f / 14f)
            lineTo(size.width * 6.2f / 14f, size.height * 8.7f / 14f)
            lineTo(size.width * 9.5f / 14f, size.height * 5.5f / 14f)
        }
        drawPath(path, color = tint, style = checkStroke)
    }
}

private fun Modifier.dashedBorder(
    width: androidx.compose.ui.unit.Dp,
    color: Color,
    shape: RoundedCornerShape,
): Modifier = this.drawBehind {
    val strokePx = width.toPx()
    val dashPx = 4.dp.toPx()
    val gapPx = 3.dp.toPx()
    val radiusPx = 4.dp.toPx()
    drawRoundRect(
        color = color,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusPx),
        style = Stroke(
            width = strokePx,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), 0f),
        ),
    )
}

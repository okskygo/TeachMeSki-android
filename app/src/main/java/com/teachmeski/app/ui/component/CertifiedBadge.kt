package com.teachmeski.app.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teachmeski.app.ui.theme.TmsColor

/**
 * F-117 "certified ski instructor" TAG — Android counterpart of web
 * `components/CertifiedBadge.tsx` and iOS `UI/Component/CertifiedBadge.swift`.
 *
 * Positive-only: composes nothing when [visible] is false. Primary blue
 * by design, never the identity badge's success green (FR-117-021).
 */
@Composable
fun CertifiedBadge(
    visible: Boolean,
    label: String,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    val shape = RoundedCornerShape(4.dp)
    Surface(
        modifier = modifier.border(1.dp, TmsColor.Primary.copy(alpha = 0.4f), shape),
        shape = shape,
        color = TmsColor.Primary.copy(alpha = 0.1f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = TmsColor.Primary,
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TmsColor.Primary,
            )
        }
    }
}

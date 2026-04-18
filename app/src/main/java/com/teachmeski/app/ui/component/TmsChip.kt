package com.teachmeski.app.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachmeski.app.ui.theme.TmsColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TmsChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) TmsColor.Primary else TmsColor.SurfaceLow
    val textColor = if (selected) TmsColor.OnPrimary else TmsColor.OnSurface
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (selected) 4.dp else 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = textColor,
        )
    }
}

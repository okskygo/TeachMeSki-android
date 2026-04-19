package com.teachmeski.app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.teachmeski.app.R
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = 3,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = TmsColor.OnSurfaceVariant,
) {
    var expanded by remember { mutableStateOf(false) }
    var hasOverflow by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            style = style,
            color = color,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { layout ->
                if (!expanded) {
                    hasOverflow = layout.hasVisualOverflow
                }
            },
        )
        if (hasOverflow || expanded) {
            Text(
                text = stringResource(
                    if (expanded) R.string.chat_panel_collapse else R.string.chat_panel_expand,
                ),
                style = MaterialTheme.typography.labelMedium,
                color = TmsColor.Primary,
                modifier = Modifier.clickable { expanded = !expanded },
            )
        }
    }
}

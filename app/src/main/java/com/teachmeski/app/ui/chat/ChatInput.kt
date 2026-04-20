package com.teachmeski.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun ChatInput(
    onSend: (String) -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    val max = 2000
    val canSend = text.isNotBlank() && !isSending && text.length <= max

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(TmsColor.SurfaceLowest)
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(TmsColor.SurfaceLow)
                .border(
                    width = 1.dp,
                    color = TmsColor.OutlineVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            BasicTextField(
                value = text,
                onValueChange = { if (it.length <= max) text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 20.dp, max = 124.dp),
                textStyle = LocalTextStyle.current.merge(
                    MaterialTheme.typography.bodyMedium.copy(color = TmsColor.OnSurface),
                ),
                cursorBrush = SolidColor(TmsColor.Primary),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                keyboardActions = KeyboardActions(),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(R.string.chat_input_placeholder),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TmsColor.Outline,
                        )
                    }
                    innerTextField()
                },
            )
        }

        Button(
            onClick = {
                val t = text.trim()
                if (t.isNotEmpty() && !isSending && t.length <= max) {
                    onSend(t)
                    text = ""
                }
            },
            enabled = canSend,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TmsColor.Primary,
                contentColor = TmsColor.OnPrimary,
                disabledContainerColor = TmsColor.Primary.copy(alpha = 0.4f),
                disabledContentColor = TmsColor.OnPrimary.copy(alpha = 0.8f),
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 20.dp,
                vertical = 12.dp,
            ),
            modifier = Modifier
                .padding(start = 12.dp)
                .heightIn(min = 44.dp),
        ) {
            Text(
                text = stringResource(R.string.chat_send),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

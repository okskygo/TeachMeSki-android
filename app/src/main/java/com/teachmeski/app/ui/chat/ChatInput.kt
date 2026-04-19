package com.teachmeski.app.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val showCounter = text.length > max * 0.8

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TmsColor.SurfaceLowest)
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= max) text = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = stringResource(R.string.chat_input_placeholder),
                        color = TmsColor.Outline,
                    )
                },
                minLines = 1,
                maxLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TmsColor.OnSurface,
                    unfocusedTextColor = TmsColor.OnSurface,
                    focusedContainerColor = TmsColor.SurfaceLowest,
                    unfocusedContainerColor = TmsColor.SurfaceLowest,
                    focusedBorderColor = TmsColor.OutlineVariant,
                    unfocusedBorderColor = TmsColor.OutlineVariant,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                keyboardActions = KeyboardActions(),
            )
            IconButton(
                onClick = {
                    val t = text.trim()
                    if (t.isNotEmpty() && !isSending && t.length <= max) {
                        onSend(t)
                        text = ""
                    }
                },
                enabled = text.isNotBlank() && !isSending && text.length <= max,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.chat_send),
                    tint = TmsColor.Primary,
                )
            }
        }
        if (showCounter) {
            Text(
                text = stringResource(R.string.chat_char_count_fmt, text.length),
                style = MaterialTheme.typography.labelSmall,
                color = TmsColor.Outline,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}

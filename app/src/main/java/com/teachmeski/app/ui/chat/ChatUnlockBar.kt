package com.teachmeski.app.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.UnlockInfo
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun ChatUnlockBar(
    unlockInfo: UnlockInfo,
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val insufficient = unlockInfo.balance < unlockInfo.cost
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = TmsColor.SurfaceLowest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = TmsColor.Primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.chat_unlock_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = TmsColor.OnSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Text(
                text = stringResource(R.string.chat_unlock_description),
                style = MaterialTheme.typography.bodySmall,
                color = TmsColor.OnSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.chat_unlock_cost_fmt, unlockInfo.cost),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TmsColor.OnSurface,
                )
                Text(
                    text = stringResource(R.string.chat_unlock_balance_fmt, unlockInfo.balance),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (insufficient) TmsColor.Error else TmsColor.OnSurfaceVariant,
                )
            }
            if (insufficient) {
                Text(
                    text = stringResource(R.string.chat_unlock_insufficient),
                    style = MaterialTheme.typography.bodySmall,
                    color = TmsColor.Error,
                )
            }
            Button(
                onClick = onUnlockClick,
                enabled = !insufficient,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.chat_unlock_btn))
            }
        }
    }
}

@Composable
fun ChatUnlockDialog(
    messageDraft: String,
    isUnlocking: Boolean,
    onDraftChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isUnlocking) onDismiss() },
        title = { Text(stringResource(R.string.chat_unlock_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.chat_unlock_description),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = messageDraft,
                    onValueChange = onDraftChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.chat_input_placeholder)) },
                    minLines = 3,
                    maxLines = 6,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isUnlocking && messageDraft.trim().isNotEmpty(),
            ) {
                if (isUnlocking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.chat_unlock_btn))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUnlocking) {
                Text(stringResource(R.string.review_cancel))
            }
        },
    )
}

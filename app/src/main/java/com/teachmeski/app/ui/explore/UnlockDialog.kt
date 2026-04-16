package com.teachmeski.app.ui.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.ExploreLessonRequest
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.util.UiText

@Composable
fun UnlockDialog(
    request: ExploreLessonRequest,
    message: String,
    onMessageChange: (String) -> Unit,
    isUnlocking: Boolean,
    error: UiText?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isUnlocking) onDismiss() },
        title = { Text(text = stringResource(R.string.explore_unlock_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    UserAvatar(
                        displayName = request.userDisplayName,
                        avatarUrl = request.userAvatarUrl,
                        size = 48.dp,
                    )
                    Spacer(modifier = Modifier.size(12.dp))
                    Text(
                        text = request.userDisplayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TmsColor.OnSurface,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.explore_unlock_dialog_cost_label, request.baseTokenCost),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TmsColor.OnSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = message,
                    onValueChange = onMessageChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.explore_unlock_dialog_hint)) },
                    minLines = 3,
                    maxLines = 8,
                    enabled = !isUnlocking,
                    shape = RoundedCornerShape(12.dp),
                )
                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TmsColor.Error,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isUnlocking,
            ) {
                if (isUnlocking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = TmsColor.Primary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.explore_unlock_dialog_confirm,
                            request.baseTokenCost,
                        ),
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isUnlocking) {
                Text(text = stringResource(R.string.explore_unlock_dialog_cancel))
            }
        },
    )
}

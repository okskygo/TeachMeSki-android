package com.teachmeski.app.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.UserAvatar
import com.teachmeski.app.ui.theme.TmsColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(
    otherPartyName: String?,
    otherPartyAvatarUrl: String?,
    isLoaded: Boolean,
    infoPanelExpanded: Boolean,
    onBack: () -> Unit,
    onToggleInfo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = TmsColor.SurfaceLowest,
            scrolledContainerColor = TmsColor.SurfaceLowest,
        ),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                    tint = TmsColor.OnSurface,
                )
            }
        },
        title = {
            if (isLoaded) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable(onClick = onToggleInfo)
                        .padding(horizontal = 8.dp),
                ) {
                    UserAvatar(
                        displayName = otherPartyName,
                        avatarUrl = otherPartyAvatarUrl,
                        size = 32.dp,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = otherPartyName.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = TmsColor.OnSurface,
                    )
                }
            }
        },
        actions = {
            if (isLoaded) {
                IconButton(onClick = onToggleInfo) {
                    Icon(
                        imageVector = if (infoPanelExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = stringResource(
                            if (infoPanelExpanded) R.string.chat_panel_collapse else R.string.chat_panel_expand,
                        ),
                        tint = TmsColor.OnSurface,
                    )
                }
            }
        },
    )
}

package com.teachmeski.app.ui.profile.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.InstructorProfile
import com.teachmeski.app.ui.component.IdentityVerifiedBadge
import com.teachmeski.app.ui.component.UserAvatar
import java.util.Locale

@Composable
fun DetailHeaderSection(profile: InstructorProfile) {
    SectionCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarCircle(profile)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                IdentityVerifiedBadge(
                    verified = profile.phoneVerifiedAt != null,
                    verifiedLabel = stringResource(R.string.identity_verified_label),
                    unverifiedLabel = stringResource(R.string.identity_unverified_label),
                )
                RatingRow(profile)
            }
        }
    }
}

@Composable
private fun AvatarCircle(profile: InstructorProfile) {
    UserAvatar(
        displayName = profile.displayName,
        avatarUrl = profile.avatarUrl,
        size = 96.dp,
    )
}

@Composable
private fun RatingRow(profile: InstructorProfile) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(20.dp),
        )
        val avg = profile.ratingAvg?.let { String.format(Locale.US, "%.1f", it) }
            ?: stringResource(R.string.instructor_detail_rating_default)
        Text(
            text = avg,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "(${stringResource(R.string.instructor_detail_rating_count_fmt, profile.ratingCount)})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

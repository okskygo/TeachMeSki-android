package com.teachmeski.app.ui.profile.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Payments
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
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DetailPricingSection(
    priceHalfDay: Int?,
    priceFullDay: Int?,
) {
    val noPrice = priceHalfDay == null && priceFullDay == null
    SectionCard {
        Text(
            text = stringResource(R.string.instructor_detail_pricing_label),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (noPrice) {
            Text(
                text = stringResource(R.string.instructor_detail_price_not_set),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            return@SectionCard
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PriceRow(R.string.instructor_detail_price_half_day_fmt, priceHalfDay)
            PriceRow(R.string.instructor_detail_price_full_day_fmt, priceFullDay)
        }
    }
}

@Composable
private fun PriceRow(labelRes: Int, amount: Int?) {
    val formatted = amount?.let { NumberFormat.getInstance(Locale.US).format(it) }
        ?: stringResource(R.string.instructor_detail_price_not_set)
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Payments,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = if (amount == null) formatted else stringResource(labelRes, formatted),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

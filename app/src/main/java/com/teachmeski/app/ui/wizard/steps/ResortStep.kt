package com.teachmeski.app.ui.wizard.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teachmeski.app.R
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.ui.wizard.ResortSelector
import com.teachmeski.app.ui.wizard.WizardUiState

@Composable
fun ResortStep(
    state: WizardUiState,
    onToggleAllRegions: () -> Unit,
    onResortToggle: (String) -> Unit,
    onPrefectureToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasSpecificSelection = state.selectedResortIds.isNotEmpty()
    val allRegionsSelected = state.allRegionsSelected
    val cardShape = RoundedCornerShape(6.dp)
    val indicatorShape = RoundedCornerShape(4.dp)

    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = stringResource(R.string.wizard_resort_heading),
                style = MaterialTheme.typography.headlineSmall,
                color = TmsColor.OnSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        item {
            Text(
                text = stringResource(R.string.wizard_resort_subheading),
                style = MaterialTheme.typography.bodySmall,
                color = TmsColor.OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        item {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .alpha(if (hasSpecificSelection) 0.4f else 1f)
                        .clip(cardShape)
                        .background(
                            if (allRegionsSelected) {
                                TmsColor.Primary.copy(alpha = 0.05f)
                            } else {
                                TmsColor.SurfaceLow
                            },
                        )
                        .clickable(enabled = !hasSpecificSelection) {
                            onToggleAllRegions()
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(TmsColor.Primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TmsColor.OnPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = stringResource(R.string.wizard_resort_all_regions),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TmsColor.OnSurface,
                    )
                    Text(
                        text = stringResource(R.string.wizard_resort_all_regions_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = TmsColor.OnSurfaceVariant,
                        lineHeight = 18.sp,
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .size(20.dp)
                            .clip(indicatorShape)
                            .background(
                                if (allRegionsSelected) TmsColor.Primary else TmsColor.SurfaceLowest,
                            )
                            .border(
                                width = 1.dp,
                                color =
                                    if (allRegionsSelected) {
                                        TmsColor.Primary
                                    } else {
                                        TmsColor.OutlineVariant
                                    },
                                shape = indicatorShape,
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (allRegionsSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = TmsColor.OnPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
        item {
            ResortSelector(
                regions = state.regions,
                selectedResortIds = state.selectedResortIds,
                allRegionsSelected = state.allRegionsSelected,
                onResortToggle = onResortToggle,
                onPrefectureToggle = onPrefectureToggle,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}

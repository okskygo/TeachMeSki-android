package com.teachmeski.app.ui.wizard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Region
import com.teachmeski.app.domain.model.SkiResort
import com.teachmeski.app.ui.theme.TmsColor

@Composable
fun ResortSelector(
    regions: List<Region>,
    selectedResortIds: Set<String>,
    allRegionsSelected: Boolean,
    onResortToggle: (String) -> Unit,
    onPrefectureToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val grouped =
        remember(regions) {
            regions
                .groupBy { it.prefectureEn ?: "" }
                .entries
                .sortedBy { (_, list) -> list.minOfOrNull { it.sortOrder } ?: 0 }
                .map { it.key to it.value }
        }

    var expandedKeys by remember { mutableStateOf(setOf<String>()) }

    Column(
        modifier =
            modifier.then(
                if (allRegionsSelected) {
                    Modifier.alpha(0.4f)
                } else {
                    Modifier
                },
            ),
    ) {
        grouped.forEach { (prefectureKey, regionList) ->
            val title =
                if (prefectureKey.isEmpty()) {
                    stringResource(R.string.wizard_resort_other)
                } else {
                    val sample = regionList.firstOrNull()
                    val zh = sample?.prefectureZh
                    if (!zh.isNullOrBlank() && zh != prefectureKey) {
                        "$zh · $prefectureKey"
                    } else {
                        prefectureKey
                    }
                }

            val resortIdsInPref =
                remember(regionList) { regionList.flatMap { it.resorts.map(SkiResort::id) } }
            val expanded = prefectureKey in expandedKeys
            val disabled = allRegionsSelected

            val selectedCountInPref =
                remember(selectedResortIds, resortIdsInPref) {
                    resortIdsInPref.count { it in selectedResortIds }
                }

            val sectionShape = RoundedCornerShape(6.dp)
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(sectionShape)
                        .background(TmsColor.SurfaceLow),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier =
                            Modifier
                                .weight(1f)
                                .clickable(enabled = !disabled) {
                                    expandedKeys =
                                        if (expanded) {
                                            expandedKeys - prefectureKey
                                        } else {
                                            expandedKeys + prefectureKey
                                        }
                                },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = TmsColor.Primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Box(
                            modifier =
                                Modifier
                                    .size(24.dp)
                                    .alpha(if (selectedCountInPref > 0) 1f else 0f)
                                    .clip(CircleShape)
                                    .background(TmsColor.Primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = selectedCountInPref.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TmsColor.OnPrimary,
                            )
                        }
                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription =
                                stringResource(
                                    if (expanded) {
                                        R.string.wizard_resort_section_collapse_cd
                                    } else {
                                        R.string.wizard_resort_section_expand_cd
                                    },
                                ),
                            modifier = Modifier.size(16.dp),
                            tint = TmsColor.OnSurfaceVariant,
                        )
                    }
                    TextButton(
                        onClick = { onPrefectureToggle(prefectureKey) },
                        enabled = !disabled,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors =
                            ButtonDefaults.textButtonColors(
                                contentColor = TmsColor.Primary,
                                disabledContentColor = TmsColor.Primary.copy(alpha = 0.38f),
                            ),
                    ) {
                        Text(
                            text = stringResource(R.string.wizard_resort_select_all),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val sortedResorts =
                            remember(regionList) {
                                regionList
                                    .flatMap { it.resorts }
                                    .sortedWith(
                                        compareBy<SkiResort>({ it.sortOrder }, { it.nameEn }, { it.id }),
                                    )
                            }
                        sortedResorts.forEach { resort ->
                            val selected = resort.id in selectedResortIds
                            val rowShape = RoundedCornerShape(6.dp)
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 40.dp)
                                        .shadow(
                                            elevation = if (selected) 2.dp else 0.dp,
                                            shape = rowShape,
                                            spotColor = TmsColor.Primary.copy(alpha = 0.2f),
                                            ambientColor = TmsColor.Primary.copy(alpha = 0.08f),
                                        )
                                        .clip(rowShape)
                                        .border(
                                            width = if (selected) 2.dp else 1.dp,
                                            color =
                                                if (selected) {
                                                    TmsColor.Primary
                                                } else {
                                                    TmsColor.OutlineVariant.copy(alpha = 0.6f)
                                                },
                                            shape = rowShape,
                                        )
                                        .background(
                                            if (selected) {
                                                TmsColor.Primary.copy(alpha = 0.05f)
                                            } else {
                                                TmsColor.SurfaceLowest.copy(alpha = 0.6f)
                                            },
                                        )
                                        .clickable(enabled = !disabled) {
                                            onResortToggle(resort.id)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                ResortRowCheckIndicator(selected = selected)
                                Text(
                                    text = "${resort.nameZh} · ${resort.nameEn}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TmsColor.OnSurface,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ResortRowCheckIndicator(selected: Boolean) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier =
            Modifier
                .size(20.dp)
                .clip(shape)
                .background(if (selected) TmsColor.Primary else TmsColor.SurfaceLowest)
                .border(
                    width = 1.dp,
                    color = if (selected) TmsColor.Primary else TmsColor.OutlineVariant,
                    shape = shape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = TmsColor.OnPrimary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

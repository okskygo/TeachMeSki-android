package com.teachmeski.app.ui.wizard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Region
import com.teachmeski.app.domain.model.SkiResort

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

    val prefectureKeyOrder = remember(grouped) { grouped.map { it.first } }

    var expandedKeys by rememberSaveable { mutableStateOf(setOf<String>()) }

    LaunchedEffect(prefectureKeyOrder) {
        expandedKeys = prefectureKeyOrder.toSet()
    }

    Column(modifier = modifier) {
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
            val allSelected =
                resortIdsInPref.isNotEmpty() &&
                    resortIdsInPref.all { it in selectedResortIds }

            val expanded = prefectureKey in expandedKeys
            val disabled = allRegionsSelected

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp)
                        .alpha(if (disabled) 0.38f else 1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = {
                        expandedKeys =
                            if (expanded) {
                                expandedKeys - prefectureKey
                            } else {
                                expandedKeys + prefectureKey
                            }
                    },
                    enabled = !disabled,
                ) {
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
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
                            }
                            .padding(vertical = 8.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { onPrefectureToggle(prefectureKey) },
                        enabled = !disabled,
                    )
                    Text(
                        text = stringResource(R.string.wizard_resort_select_all),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier =
                            Modifier.clickable(enabled = !disabled) {
                                onPrefectureToggle(prefectureKey)
                            },
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(start = 48.dp, end = 4.dp)) {
                    val sortedResorts =
                        remember(regionList) {
                            regionList
                                .flatMap { it.resorts }
                                .sortedWith(
                                    compareBy<SkiResort>({ it.sortOrder }, { it.nameEn }, { it.id })
                                )
                        }
                    sortedResorts.forEach { resort ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .alpha(if (disabled) 0.38f else 1f),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(
                                checked = resort.id in selectedResortIds,
                                onCheckedChange = { onResortToggle(resort.id) },
                                enabled = !disabled,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${resort.nameZh} · ${resort.nameEn}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

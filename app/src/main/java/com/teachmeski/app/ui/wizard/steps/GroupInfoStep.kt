package com.teachmeski.app.ui.wizard.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.ui.wizard.WizardUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoStep(
    state: WizardUiState,
    onDisciplineChange: (Discipline) -> Unit,
    onGroupSizeChange: (Int) -> Unit,
    onHasChildrenChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.wizard_group_heading),
            style = MaterialTheme.typography.headlineSmall,
            color = TmsColor.OnSurface,
        )
        Text(
            text = stringResource(R.string.wizard_group_subheading),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DisciplineCard(
                selected = state.discipline == Discipline.Ski,
                onClick = { onDisciplineChange(Discipline.Ski) },
                iconRes = R.drawable.ic_ski,
                label = stringResource(R.string.wizard_discipline_ski),
                modifier = Modifier.weight(1f),
            )
            DisciplineCard(
                selected = state.discipline == Discipline.Snowboard,
                onClick = { onDisciplineChange(Discipline.Snowboard) },
                iconRes = R.drawable.ic_snowboard,
                label = stringResource(R.string.wizard_discipline_snowboard),
                modifier = Modifier.weight(1f),
            )
        }

        Text(
            text = stringResource(R.string.wizard_group_size_label),
            style = MaterialTheme.typography.titleSmall,
            color = TmsColor.OnSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                IconButton(
                    onClick = { onGroupSizeChange(state.groupSize - 1) },
                    enabled = state.groupSize > 1,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = stringResource(R.string.wizard_group_size_decrease_cd),
                    )
                }
                Text(
                    text = state.groupSize.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = TmsColor.OnSurface,
                )
                IconButton(
                    onClick = { onGroupSizeChange(state.groupSize + 1) },
                    enabled = state.groupSize < 12,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(R.string.wizard_group_size_increase_cd),
                    )
                }
            }
        }

        Surface(
            onClick = { onHasChildrenChange(!state.hasChildren) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color =
                if (state.hasChildren) {
                    TmsColor.Primary.copy(alpha = 0.05f)
                } else {
                    TmsColor.SurfaceLow
                },
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.wizard_has_children_label),
                        style = MaterialTheme.typography.bodyLarge,
                        color = TmsColor.OnSurface,
                    )
                    Text(
                        text = stringResource(R.string.wizard_has_children_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = TmsColor.OnSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                HasChildrenCheckboxIndicator(checked = state.hasChildren)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisciplineCard(
    selected: Boolean,
    onClick: () -> Unit,
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = shape,
        color = TmsColor.SurfaceLow,
        border =
            BorderStroke(
                width = 2.dp,
                color = if (selected) TmsColor.Primary else TmsColor.OutlineVariant,
            ),
        shadowElevation = if (selected) 4.dp else 0.dp,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                modifier = Modifier.size(40.dp),
                tint = TmsColor.Primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = TmsColor.OnSurface,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun HasChildrenCheckboxIndicator(
    checked: Boolean,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(6.dp)
    val borderColor = if (checked) TmsColor.Primary else TmsColor.OutlineVariant
    Box(
        modifier =
            modifier
                .size(24.dp)
                .clip(shape)
                .border(width = 2.dp, color = borderColor, shape = shape)
                .background(if (checked) TmsColor.Primary else Color.Transparent, shape),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = TmsColor.OnPrimary,
            )
        }
    }
}

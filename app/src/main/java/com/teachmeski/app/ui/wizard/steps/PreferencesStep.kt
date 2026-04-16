package com.teachmeski.app.ui.wizard.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.EquipmentRental
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.ui.wizard.WizardUiState

private val CertKeys = listOf("CSIA", "CASI", "NZSIA", "PSIA", "SIA_Japan", "other")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PreferencesStep(
    state: WizardUiState,
    onEquipmentRentalChange: (EquipmentRental) -> Unit,
    onNeedsTransportChange: (Boolean?) -> Unit,
    onTransportNoteChange: (String) -> Unit,
    onToggleCertPreference: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.wizard_preferences_optional_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )

        Text(
            text = stringResource(R.string.wizard_equipment_label),
            style = MaterialTheme.typography.titleSmall,
            color = TmsColor.OnSurface,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            EquipmentRental.entries.forEach { option ->
                val selected = state.equipmentRental == option
                TmsChip(
                    selected = selected,
                    onClick = {
                        onEquipmentRentalChange(
                            if (state.equipmentRental == option) {
                                EquipmentRental.None
                            } else {
                                option
                            },
                        )
                    },
                    label =
                        stringResource(
                            when (option) {
                                EquipmentRental.All -> R.string.wizard_equipment_all
                                EquipmentRental.Partial -> R.string.wizard_equipment_partial
                                EquipmentRental.None -> R.string.wizard_equipment_none
                            },
                        ),
                )
            }
        }

        Text(
            text = stringResource(R.string.wizard_transport_label),
            style = MaterialTheme.typography.titleSmall,
            color = TmsColor.OnSurface,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TmsChip(
                selected = state.needsTransport == true,
                onClick = {
                    onNeedsTransportChange(
                        if (state.needsTransport == true) null else true,
                    )
                },
                label = stringResource(R.string.wizard_transport_yes),
            )
            TmsChip(
                selected = state.needsTransport == false,
                onClick = {
                    onNeedsTransportChange(
                        if (state.needsTransport == false) null else false,
                    )
                },
                label = stringResource(R.string.wizard_transport_no),
            )
        }
        if (state.needsTransport == true) {
            OutlinedTextField(
                value = state.transportNote,
                onValueChange = onTransportNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(text = stringResource(R.string.wizard_transport_note_placeholder))
                },
                singleLine = false,
                minLines = 2,
            )
        }

        Text(
            text = stringResource(R.string.wizard_cert_label),
            style = MaterialTheme.typography.titleSmall,
            color = TmsColor.OnSurface,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CertKeys.forEach { key ->
                val selected = key in state.certPreferences
                TmsChip(
                    selected = selected,
                    onClick = { onToggleCertPreference(key) },
                    label = key,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TmsChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) TmsColor.Primary else TmsColor.SurfaceLow
    val textColor = if (selected) TmsColor.OnPrimary else TmsColor.OnSurface
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(8.dp),
        shadowElevation = if (selected) 4.dp else 0.dp,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = textColor,
        )
    }
}

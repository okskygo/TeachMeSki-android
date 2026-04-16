package com.teachmeski.app.ui.wizard.steps

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachmeski.app.R
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.ui.wizard.WizardUiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val DATE_PLACEHOLDER_EM_DASH = "\u2014"

private enum class DateStrategy {
    SPECIFIC,
    FLEXIBLE_MONTH,
    NOT_FIXED,
}

private data class UpcomingMonth(
    val key: String,
    val label: String,
    val firstDayIso: String,
    val lastDayIso: String,
)

private fun parseIsoToMillis(iso: String): Long {
    val parts = iso.split("-")
    if (parts.size != 3) return System.currentTimeMillis()
    return try {
        val y = parts[0].toInt()
        val m = parts[1].toInt() - 1
        val d = parts[2].toInt()
        Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m)
            set(Calendar.DAY_OF_MONTH, d)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    } catch (_: Exception) {
        System.currentTimeMillis()
    }
}

private fun millisToIso(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return String.format(
        Locale.US,
        "%04d-%02d-%02d",
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH) + 1,
        cal.get(Calendar.DAY_OF_MONTH),
    )
}

private fun buildUpcomingMonths(locale: Locale): List<UpcomingMonth> {
    val labelFmt = SimpleDateFormat("LLLL yyyy", locale)
    val cal =
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    return buildList {
        repeat(12) {
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val key = String.format(Locale.US, "%04d-%02d", y, m)
            val firstDayIso = millisToIso(cal.timeInMillis)
            val last = cal.clone() as Calendar
            last.set(Calendar.DAY_OF_MONTH, last.getActualMaximum(Calendar.DAY_OF_MONTH))
            val lastDayIso = millisToIso(last.timeInMillis)
            add(
                UpcomingMonth(
                    key = key,
                    label = labelFmt.format(cal.time),
                    firstDayIso = firstDayIso,
                    lastDayIso = lastDayIso,
                ),
            )
            cal.add(Calendar.MONTH, 1)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleStep(
    state: WizardUiState,
    onDatesFlexibleChange: (Boolean) -> Unit,
    onDateStartChange: (String?) -> Unit,
    onDateEndChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var strategyName by rememberSaveable { mutableStateOf(DateStrategy.NOT_FIXED.name) }
    var selectedMonthKey by rememberSaveable { mutableStateOf<String?>(null) }
    val dateStrategy =
        try {
            DateStrategy.valueOf(strategyName)
        } catch (_: IllegalArgumentException) {
            DateStrategy.NOT_FIXED
        }

    LaunchedEffect(state.currentStep) {
        if (state.currentStep != 4) return@LaunchedEffect
        val next =
            when {
                !state.datesFlexible -> DateStrategy.SPECIFIC
                state.datesFlexible && !state.dateStart.isNullOrBlank() -> DateStrategy.FLEXIBLE_MONTH
                else -> DateStrategy.NOT_FIXED
            }
        strategyName = next.name
        selectedMonthKey =
            if (next == DateStrategy.FLEXIBLE_MONTH) state.dateStart?.take(7) else null
    }

    var showStartPicker by rememberSaveable { mutableStateOf(false) }
    var showEndPicker by rememberSaveable { mutableStateOf(false) }
    val startDatePickerCd = stringResource(R.string.wizard_schedule_heading)
    val endDatePickerCd = stringResource(R.string.wizard_schedule_subheading)

    if (showStartPicker) {
        val initialMillis = state.dateStart?.let { parseIsoToMillis(it) }
        val pickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = initialMillis,
            )
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onDateStartChange(millisToIso(it)) }
                        showStartPicker = false
                    },
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        ) {
            Column {
                Text(
                    text = stringResource(R.string.wizard_confirm_label_date),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                )
                DatePicker(state = pickerState)
            }
        }
    }

    if (showEndPicker) {
        val initialMillis = state.dateEnd?.let { parseIsoToMillis(it) }
        val pickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = initialMillis,
            )
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { onDateEndChange(millisToIso(it)) }
                        showEndPicker = false
                    },
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
        ) {
            Column {
                Text(
                    text = stringResource(R.string.wizard_confirm_label_date),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp),
                )
                DatePicker(state = pickerState)
            }
        }
    }

    val locale = Locale.getDefault()
    val upcomingMonths = buildUpcomingMonths(locale)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.wizard_schedule_heading),
            style = MaterialTheme.typography.headlineSmall,
            color = TmsColor.OnSurface,
        )
        Text(
            text = stringResource(R.string.wizard_schedule_subheading),
            style = MaterialTheme.typography.bodyMedium,
            color = TmsColor.OnSurfaceVariant,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DateStrategyCard(
                modifier = Modifier.weight(1f),
                active = dateStrategy == DateStrategy.SPECIFIC,
                icon = Icons.Default.CalendarToday,
                label = stringResource(R.string.wizard_strategy_specific),
                onClick = {
                    strategyName = DateStrategy.SPECIFIC.name
                    onDatesFlexibleChange(false)
                    onDateStartChange(null)
                    onDateEndChange(null)
                },
            )
            DateStrategyCard(
                modifier = Modifier.weight(1f),
                active = dateStrategy == DateStrategy.FLEXIBLE_MONTH,
                icon = Icons.Default.CalendarViewMonth,
                label = stringResource(R.string.wizard_strategy_flexible_month),
                onClick = {
                    strategyName = DateStrategy.FLEXIBLE_MONTH.name
                    onDatesFlexibleChange(true)
                    onDateStartChange(null)
                    onDateEndChange(null)
                    selectedMonthKey = null
                },
            )
            DateStrategyCard(
                modifier = Modifier.weight(1f),
                active = dateStrategy == DateStrategy.NOT_FIXED,
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                label = stringResource(R.string.wizard_strategy_not_fixed),
                onClick = {
                    strategyName = DateStrategy.NOT_FIXED.name
                    onDatesFlexibleChange(true)
                    onDateStartChange(null)
                    onDateEndChange(null)
                },
            )
        }

        when (dateStrategy) {
            DateStrategy.SPECIFIC -> {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = TmsColor.SurfaceLowest,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { showStartPicker = true },
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .semantics {
                                        contentDescription = startDatePickerCd
                                    },
                        ) {
                            Text(text = state.dateStart ?: DATE_PLACEHOLDER_EM_DASH)
                        }
                        OutlinedButton(
                            onClick = { showEndPicker = true },
                            modifier =
                                Modifier
                                    .weight(1f)
                                    .semantics {
                                        contentDescription = endDatePickerCd
                                    },
                        ) {
                            Text(text = state.dateEnd ?: DATE_PLACEHOLDER_EM_DASH)
                        }
                    }
                }
            }
            DateStrategy.FLEXIBLE_MONTH -> {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    color = TmsColor.SurfaceLowest,
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.wizard_select_month_heading),
                            style = MaterialTheme.typography.titleSmall,
                            color = TmsColor.OnSurface,
                        )
                        upcomingMonths.chunked(3).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                row.forEach { month ->
                                    val selected = month.key == selectedMonthKey
                                    Box(
                                        modifier =
                                            Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .then(
                                                    if (selected) {
                                                        Modifier.shadow(
                                                            elevation = 4.dp,
                                                            shape = RoundedCornerShape(8.dp),
                                                            spotColor = TmsColor.Primary.copy(alpha = 0.2f),
                                                            ambientColor = TmsColor.Primary.copy(alpha = 0.08f),
                                                        )
                                                    } else {
                                                        Modifier
                                                    },
                                                )
                                                .background(
                                                    if (selected) TmsColor.Primary else TmsColor.SurfaceLow,
                                                )
                                                .clickable {
                                                    selectedMonthKey = month.key
                                                    onDateStartChange(month.firstDayIso)
                                                    onDateEndChange(month.lastDayIso)
                                                }
                                                .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = month.label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color =
                                                if (selected) {
                                                    TmsColor.OnPrimary
                                                } else {
                                                    TmsColor.OnSurface
                                                },
                                        )
                                    }
                                }
                                if (row.size < 3) {
                                    repeat(3 - row.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            DateStrategy.NOT_FIXED -> Unit
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateStrategyCard(
    modifier: Modifier = Modifier,
    active: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Surface(
        onClick = onClick,
        modifier =
            modifier
                .shadow(2.dp, shape)
                .then(
                    if (active) {
                        Modifier.border(2.dp, TmsColor.Primary, shape)
                    } else {
                        Modifier
                    },
                ),
        shape = shape,
        color = if (active) TmsColor.Primary.copy(alpha = 0.05f) else TmsColor.SurfaceLowest,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (active) TmsColor.Primary else TmsColor.SurfaceLow),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = if (active) TmsColor.OnPrimary else TmsColor.Outline,
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TmsColor.OnSurface,
            )
        }
    }
}

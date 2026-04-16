package com.teachmeski.app.ui.wizard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.teachmeski.app.R
import com.teachmeski.app.ui.component.WizardStepProgress
import com.teachmeski.app.ui.theme.TmsColor
import com.teachmeski.app.ui.wizard.steps.ConfirmStep
import com.teachmeski.app.ui.wizard.steps.DurationStep
import com.teachmeski.app.ui.wizard.steps.GroupInfoStep
import com.teachmeski.app.ui.wizard.steps.LanguageStep
import com.teachmeski.app.ui.wizard.steps.NotesStep
import com.teachmeski.app.ui.wizard.steps.PreferencesStep
import com.teachmeski.app.ui.wizard.steps.ResortStep
import com.teachmeski.app.ui.wizard.steps.ScheduleStep
import com.teachmeski.app.ui.wizard.steps.SkillLevelStep
import kotlinx.coroutines.delay

private const val SUCCESS_AUTO_DISMISS_MS = 2_800L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonRequestWizardScreen(
    onClose: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: LessonRequestWizardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCloseConfirm by rememberSaveable { mutableStateOf(false) }
    var successNavigated by remember { mutableStateOf(false) }

    LaunchedEffect(state.phase) {
        if (state.phase == WizardPhase.Success && !successNavigated) {
            delay(SUCCESS_AUTO_DISMISS_MS)
            if (!successNavigated) {
                successNavigated = true
                onSuccess()
            }
        }
    }

    if (showCloseConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseConfirm = false },
            title = {
                Text(text = stringResource(R.string.wizard_close_confirm_title))
            },
            text = {
                Text(text = stringResource(R.string.wizard_close_confirm_desc))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCloseConfirm = false
                        onClose()
                    },
                ) {
                    Text(text = stringResource(R.string.wizard_close_confirm_leave))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirm = false }) {
                    Text(text = stringResource(R.string.wizard_close_confirm_stay))
                }
            },
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Scaffold(
            topBar = {
                if (state.phase == WizardPhase.Steps) {
                    CenterAlignedTopAppBar(
                        title = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text =
                                        stringResource(
                                            R.string.wizard_step_progress_fmt,
                                            state.currentStep,
                                            9,
                                        ),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TmsColor.OnSurface,
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = { showCloseConfirm = true },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.wizard_nav_close),
                                )
                            }
                        },
                    )
                }
            },
            bottomBar = {
                if (state.phase == WizardPhase.Steps) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (state.currentStep > 1) {
                            OutlinedButton(onClick = viewModel::prevStep) {
                                Text(text = stringResource(R.string.wizard_nav_prev))
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (state.currentStep in 7..8) {
                            TextButton(onClick = viewModel::nextStep) {
                                Text(text = stringResource(R.string.wizard_nav_skip))
                            }
                        }
                        val primaryLabel =
                            if (state.currentStep == 9) {
                                if (state.isSubmitting) {
                                    R.string.wizard_submit_loading
                                } else {
                                    R.string.wizard_submit
                                }
                            } else {
                                R.string.wizard_nav_next
                            }
                        val primaryEnabled =
                            when {
                                state.currentStep == 9 -> !state.isSubmitting
                                else -> state.canAdvanceFromCurrentStep
                            }
                        Button(
                            onClick = {
                                if (state.currentStep == 9) {
                                    viewModel.submit()
                                } else {
                                    viewModel.nextStep()
                                }
                            },
                            enabled = primaryEnabled,
                        ) {
                            Text(text = stringResource(primaryLabel))
                        }
                    }
                }
            },
        ) { innerPadding ->
            when (state.phase) {
                WizardPhase.Steps -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                    ) {
                        WizardStepProgress(
                            currentStep = state.currentStep,
                            totalSteps = 9,
                            labels = (1..9).map { stringResource(stepLabelRes(it)) },
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                        state.submitError?.let { err ->
                            Surface(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text(
                                    text = err.asString(),
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                )
                            }
                        }
                        AnimatedContent(
                            targetState = state.currentStep,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(220)) togetherWith
                                    fadeOut(animationSpec = tween(220))
                            },
                            label = "wizard_step",
                            modifier = Modifier.weight(1f),
                        ) { step ->
                            when (step) {
                                1 -> ResortStep(
                                    state = state,
                                    onToggleAllRegions = viewModel::toggleAllRegions,
                                    onResortToggle = viewModel::toggleResort,
                                    onPrefectureToggle = viewModel::togglePrefecture,
                                )
                                2 -> GroupInfoStep(
                                    state = state,
                                    onDisciplineChange = viewModel::setDiscipline,
                                    onGroupSizeChange = viewModel::setGroupSize,
                                    onHasChildrenChange = viewModel::setHasChildren,
                                )
                                3 -> SkillLevelStep(
                                    state = state,
                                    onSkillLevelChange = viewModel::setSkillLevel,
                                )
                                4 -> ScheduleStep(
                                    state = state,
                                    onDatesFlexibleChange = viewModel::setDatesFlexible,
                                    onDateStartChange = viewModel::setDateStart,
                                    onDateEndChange = viewModel::setDateEnd,
                                )
                                5 -> DurationStep(
                                    state = state,
                                    onDurationChange = viewModel::setDurationDays,
                                )
                                6 -> LanguageStep(
                                    state = state,
                                    onToggleLanguage = viewModel::toggleLanguage,
                                )
                                7 -> PreferencesStep(
                                    state = state,
                                    onEquipmentRentalChange = viewModel::setEquipmentRental,
                                    onNeedsTransportChange = viewModel::setNeedsTransport,
                                    onTransportNoteChange = viewModel::setTransportNote,
                                    onToggleCertPreference = viewModel::toggleCertPreference,
                                )
                                8 -> NotesStep(
                                    state = state,
                                    onNotesChange = viewModel::setAdditionalNotes,
                                )
                                9 -> ConfirmStep(
                                    state = state,
                                    onEditStep = viewModel::goToStep,
                                )
                                else -> Box(Modifier.fillMaxSize())
                            }
                        }
                    }
                }
                WizardPhase.Success -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.wizard_success_title),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.wizard_success_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (!successNavigated) {
                                    successNavigated = true
                                    onSuccess()
                                }
                            },
                        ) {
                            Text(text = stringResource(R.string.wizard_success_continue))
                        }
                    }
                }
            }
        }
    }
}

private fun stepLabelRes(step: Int): Int =
    when (step) {
        1 -> R.string.wizard_step_resort
        2 -> R.string.wizard_step_activity
        3 -> R.string.wizard_step_level
        4 -> R.string.wizard_step_dates
        5 -> R.string.wizard_step_duration
        6 -> R.string.wizard_step_language
        7 -> R.string.wizard_step_preferences
        8 -> R.string.wizard_step_notes
        9 -> R.string.wizard_step_confirm
        else -> R.string.wizard_step_resort
    }

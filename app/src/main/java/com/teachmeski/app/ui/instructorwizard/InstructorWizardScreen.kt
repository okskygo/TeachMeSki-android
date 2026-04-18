package com.teachmeski.app.ui.instructorwizard

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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.teachmeski.app.ui.instructorwizard.steps.AccountStep
import com.teachmeski.app.ui.instructorwizard.steps.CertificationsStep
import com.teachmeski.app.ui.instructorwizard.steps.CompleteStep
import com.teachmeski.app.ui.instructorwizard.steps.DisciplineStep
import com.teachmeski.app.ui.instructorwizard.steps.LanguagesStep
import com.teachmeski.app.ui.instructorwizard.steps.LevelsStep
import com.teachmeski.app.ui.instructorwizard.steps.OtpStep
import com.teachmeski.app.ui.instructorwizard.steps.PricingStep
import com.teachmeski.app.ui.instructorwizard.steps.ProfileStep
import com.teachmeski.app.ui.instructorwizard.steps.ResortsStep
import com.teachmeski.app.ui.instructorwizard.steps.ServicesStep
import com.teachmeski.app.ui.theme.TmsColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstructorWizardScreen(
    isGuestMode: Boolean = false,
    onClose: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: InstructorWizardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showCloseConfirm by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isGuestMode) {
        if (isGuestMode) viewModel.setGuestMode(true)
    }

    if (showCloseConfirm) {
        AlertDialog(
            onDismissRequest = { showCloseConfirm = false },
            title = { Text(text = stringResource(R.string.instructor_wizard_close_confirm_title)) },
            text = { Text(text = stringResource(R.string.instructor_wizard_close_confirm_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCloseConfirm = false
                        onClose()
                    },
                ) {
                    Text(text = stringResource(R.string.instructor_wizard_close_confirm_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCloseConfirm = false }) {
                    Text(text = stringResource(R.string.instructor_wizard_close_confirm_cancel))
                }
            },
        )
    }

    when {
        state.isCheckingProfile -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = TmsColor.Primary)
                    Spacer(modifier = Modifier.padding(16.dp))
                    Text(
                        text = stringResource(R.string.instructor_wizard_checking_profile),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TmsColor.OnSurfaceVariant,
                    )
                }
            }
        }
        state.phase == InstructorWizardPhase.Success -> {
            CompleteStep(
                profileAlreadyExists = state.profileAlreadyExists,
                onStartExploring = {
                    if (state.profileAlreadyExists) {
                        onClose()
                    } else {
                        onSuccess()
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
        else -> {
            InstructorWizardStepsScaffold(
                state = state,
                onCloseClick = { showCloseConfirm = true },
                onBack = viewModel::goBack,
                onPrimary = viewModel::goNext,
                viewModel = viewModel,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstructorWizardStepsScaffold(
    state: InstructorWizardUiState,
    onCloseClick: () -> Unit,
    onBack: () -> Unit,
    onPrimary: () -> Unit,
    viewModel: InstructorWizardViewModel,
) {
    Scaffold(
        containerColor = TmsColor.SurfaceLowest,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(0, 0, 0, 0),
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = TmsColor.SurfaceLowest,
                        scrolledContainerColor = TmsColor.SurfaceLowest,
                    ),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text =
                                stringResource(
                                    R.string.wizard_step_progress_fmt,
                                    state.currentStep,
                                    state.totalSteps,
                                ),
                            style = MaterialTheme.typography.titleMedium,
                            color = TmsColor.OnSurface,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onCloseClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.instructor_wizard_nav_close),
                            tint = TmsColor.OnSurface,
                        )
                    }
                },
            )
        },
        bottomBar = {
            if (state.currentStep != 10 || !state.isGuestMode) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.currentStep > 1) {
                        OutlinedButton(onClick = onBack) {
                            Text(text = stringResource(R.string.instructor_wizard_nav_prev))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    val primaryLabel =
                        when {
                            state.currentStep == 9 && state.isGuestMode -> R.string.auth_signup_submit
                            state.currentStep == state.totalSteps -> {
                                if (state.isSubmitting) {
                                    R.string.wizard_submit_loading
                                } else {
                                    R.string.instructor_wizard_nav_submit
                                }
                            }
                            else -> R.string.instructor_wizard_nav_next
                        }
                    val primaryEnabled =
                        when {
                            state.currentStep == 9 && state.isGuestMode ->
                                !state.isSigningUp && state.canAdvanceFromCurrentStep()
                            state.currentStep == state.totalSteps ->
                                !state.isSubmitting && state.canAdvanceFromCurrentStep()
                            else -> state.canAdvanceFromCurrentStep()
                        }
                    Button(
                        onClick = onPrimary,
                        enabled = primaryEnabled,
                    ) {
                        if (state.currentStep == 9 && state.isGuestMode && state.isSigningUp) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        } else {
                            Text(text = stringResource(primaryLabel))
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            WizardStepProgress(
                currentStep = state.currentStep,
                totalSteps = state.totalSteps,
                labels = (1..state.totalSteps).map { stringResource(instructorWizardStepLabelRes(it)) },
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            (state.submitError ?: if (state.currentStep == 9) state.signupError else null)?.let { err ->
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
                label = "instructor_wizard_step",
                modifier = Modifier.weight(1f),
            ) { step ->
                when (step) {
                    1 ->
                        DisciplineStep(
                            state = state,
                            onToggleDiscipline = viewModel::toggleDiscipline,
                        )
                    2 ->
                        LevelsStep(
                            state = state,
                            onToggleLevel = viewModel::toggleTeachableLevel,
                        )
                    3 ->
                        ResortsStep(
                            state = state,
                            onToggleAllRegions = viewModel::toggleAllRegions,
                            onResortToggle = viewModel::toggleResort,
                            onPrefectureToggle = viewModel::togglePrefecture,
                        )
                    4 ->
                        CertificationsStep(
                            state = state,
                            onToggleCertification = viewModel::toggleCertification,
                            onCertificationOtherChange = viewModel::setCertificationOther,
                        )
                    5 ->
                        ProfileStep(
                            state = state,
                            onDisplayNameChange = viewModel::setDisplayName,
                            onBioChange = viewModel::setBio,
                        )
                    6 ->
                        LanguagesStep(
                            state = state,
                            onToggleLanguage = viewModel::toggleLanguage,
                        )
                    7 ->
                        PricingStep(
                            state = state,
                            onPriceHalfDayChange = viewModel::setPriceHalfDay,
                            onPriceFullDayChange = viewModel::setPriceFullDay,
                        )
                    8 ->
                        ServicesStep(
                            state = state,
                            onOffersTransportChange = viewModel::setOffersTransport,
                            onOffersPhotographyChange = viewModel::setOffersPhotography,
                        )
                    9 ->
                        AccountStep(
                            state = state,
                            onEmailChange = viewModel::onEmailChange,
                            onPasswordChange = viewModel::onPasswordChange,
                            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                            onTermsCheckedChange = viewModel::onTermsCheckedChange,
                        )
                    10 ->
                        OtpStep(
                            state = state,
                            onOtpChange = viewModel::onOtpChange,
                            onVerify = viewModel::verifyOtp,
                            onResend = viewModel::resendOtp,
                        )
                    else -> Box(Modifier.fillMaxSize())
                }
            }
        }
    }
}

private fun instructorWizardStepLabelRes(step: Int): Int =
    when (step) {
        1 -> R.string.instructor_wizard_step_labels_step1
        2 -> R.string.instructor_wizard_step_labels_step2
        3 -> R.string.instructor_wizard_step_labels_step3
        4 -> R.string.instructor_wizard_step_labels_step4
        5 -> R.string.instructor_wizard_step_labels_step5
        6 -> R.string.instructor_wizard_step_labels_step6
        7 -> R.string.instructor_wizard_step_labels_step7
        8 -> R.string.instructor_wizard_step_labels_step8
        9 -> R.string.instructor_wizard_step_labels_step9
        10 -> R.string.instructor_wizard_step_labels_step10
        else -> R.string.instructor_wizard_step_labels_step1
    }

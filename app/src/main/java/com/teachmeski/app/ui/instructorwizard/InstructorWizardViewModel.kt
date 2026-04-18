package com.teachmeski.app.ui.instructorwizard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.InstructorProfileData
import com.teachmeski.app.domain.model.PendingInstructorProfile
import com.teachmeski.app.domain.model.Region
import com.teachmeski.app.domain.repository.AuthRepository
import com.teachmeski.app.domain.repository.InstructorRepository
import com.teachmeski.app.domain.repository.ResortRepository
import com.teachmeski.app.ui.component.PasswordRules
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class InstructorWizardPhase {
    Steps,
    Success,
}

data class InstructorWizardUiState(
    val currentStep: Int = 1,
    val phase: InstructorWizardPhase = InstructorWizardPhase.Steps,
    val selectedDisciplines: Set<String> = emptySet(),
    val teachableLevels: Set<Int> = emptySet(),
    val allRegionsSelected: Boolean = false,
    val selectedResortIds: Set<String> = emptySet(),
    val regions: List<Region> = emptyList(),
    val isLoadingRegions: Boolean = false,
    val certifications: Set<String> = emptySet(),
    val certificationOther: String = "",
    val displayName: String = "",
    val bio: String = "",
    val languages: Set<String> = setOf("zh"),
    val priceHalfDay: String = "",
    val priceFullDay: String = "",
    val offersTransport: Boolean = false,
    val offersPhotography: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitError: UiText? = null,
    val isCheckingProfile: Boolean = true,
    val profileAlreadyExists: Boolean = false,
    val isGuestMode: Boolean = false,
    val totalSteps: Int = 8,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val termsChecked: Boolean = false,
    val passwordRules: PasswordRules = PasswordRules(),
    val isSigningUp: Boolean = false,
    val signupError: UiText? = null,
    val otp: String = "",
    val isVerifyingOtp: Boolean = false,
    val otpError: UiText? = null,
    val resendCooldown: Int = 0,
    val resendMessage: UiText? = null,
    val isResendingOtp: Boolean = false,
) {
    fun canAdvanceFromCurrentStep(): Boolean =
        when (currentStep) {
            1 -> selectedDisciplines.isNotEmpty()
            2 -> teachableLevels.isNotEmpty()
            3 -> selectedResortIds.isNotEmpty()
            4 -> ("other" !in certifications) || certificationOther.isNotBlank()
            5 -> {
                val name = displayName.trim()
                name.isNotEmpty() && name.length <= 50 && bio.length <= 2000
            }
            6 -> languages.isNotEmpty()
            7 -> pricingStepValid()
            8 -> true
            9 ->
                isGuestMode &&
                    passwordRules.allPassed &&
                    password == confirmPassword &&
                    confirmPassword.isNotEmpty() &&
                    termsChecked &&
                    email.contains("@") &&
                    email.isNotBlank()
            10 -> isGuestMode && otp.length == 6
            else -> false
        }

    private fun pricingStepValid(): Boolean {
        val half = priceHalfDay.trim().toIntOrNull() ?: 0
        val full = priceFullDay.trim().toIntOrNull() ?: 0
        if (half > 0 && full > 0) return full >= half
        return true
    }
}

@HiltViewModel
class InstructorWizardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val resortRepository: ResortRepository,
    private val instructorRepository: InstructorRepository,
    private val authRepository: AuthRepository,
    private val pendingProfile: PendingInstructorProfile,
) : ViewModel() {

    private val initialGuestMode: Boolean = savedStateHandle.get<Boolean>("isGuestMode") == true

    private val _uiState =
        MutableStateFlow(
            InstructorWizardUiState(
                isGuestMode = initialGuestMode,
                totalSteps = if (initialGuestMode) 10 else 8,
                isCheckingProfile = !initialGuestMode,
            ),
        )
    val uiState: StateFlow<InstructorWizardUiState> = _uiState.asStateFlow()

    private var cooldownJob: Job? = null

    init {
        viewModelScope.launch {
            if (_uiState.value.isGuestMode) {
                _uiState.update { it.copy(isCheckingProfile = false) }
                loadRegions()
                return@launch
            }
            when (val existsResult = instructorRepository.checkProfileExists()) {
                is Resource.Success -> {
                    if (existsResult.data) {
                        _uiState.update {
                            it.copy(
                                isCheckingProfile = false,
                                phase = InstructorWizardPhase.Success,
                                profileAlreadyExists = true,
                            )
                        }
                        return@launch
                    }
                }
                is Resource.Error -> Unit
                is Resource.Loading -> Unit
            }
            _uiState.update { it.copy(isCheckingProfile = false) }
            loadRegions()
        }
    }

    private fun loadRegions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingRegions = true) }
            when (val result = resortRepository.getResortsWithRegions()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            regions = result.data,
                            isLoadingRegions = false,
                        )
                    }
                }
                is Resource.Error,
                is Resource.Loading,
                -> {
                    _uiState.update { it.copy(regions = emptyList(), isLoadingRegions = false) }
                }
            }
        }
    }

    fun canNext(): Boolean {
        val s = _uiState.value
        return s.phase == InstructorWizardPhase.Steps && s.canAdvanceFromCurrentStep()
    }

    fun setGuestMode(isGuest: Boolean) {
        if (!isGuest) return
        _uiState.update {
            it.copy(isGuestMode = true, totalSteps = 10, isCheckingProfile = false)
        }
        loadRegions()
    }

    fun goNext() {
        val s = _uiState.value
        if (s.phase != InstructorWizardPhase.Steps) return
        if (!s.canAdvanceFromCurrentStep()) return
        val total = s.totalSteps
        if (s.currentStep < total) {
            if (s.currentStep == 9 && s.isGuestMode) {
                signUp()
            } else {
                _uiState.update { it.copy(currentStep = it.currentStep + 1, submitError = null) }
            }
        } else if (!s.isGuestMode) {
            submit()
        }
    }

    fun goBack() {
        _uiState.update { st ->
            if (st.phase != InstructorWizardPhase.Steps) return@update st
            if (st.currentStep <= 1) return@update st
            st.copy(currentStep = st.currentStep - 1)
        }
    }

    fun toggleDiscipline(key: String) {
        _uiState.update { s ->
            val next =
                if (key in s.selectedDisciplines) {
                    s.selectedDisciplines - key
                } else {
                    s.selectedDisciplines + key
                }
            s.copy(selectedDisciplines = next)
        }
    }

    fun toggleTeachableLevel(level: Int) {
        val lv = level.coerceIn(0, 4)
        _uiState.update { s ->
            val maxSelected = s.teachableLevels.maxOrNull() ?: -1
            val next =
                if (lv in s.teachableLevels && lv == maxSelected) {
                    (0 until lv).toSet()
                } else {
                    (0..lv).toSet()
                }
            s.copy(teachableLevels = next)
        }
    }

    fun toggleAllRegions() {
        _uiState.update { s ->
            val newAll = !s.allRegionsSelected
            if (newAll) {
                s.copy(allRegionsSelected = true, selectedResortIds = emptySet())
            } else {
                s.copy(allRegionsSelected = false)
            }
        }
    }

    fun toggleResort(resortId: String) {
        _uiState.update { s ->
            val newIds =
                if (resortId in s.selectedResortIds) {
                    s.selectedResortIds - resortId
                } else {
                    s.selectedResortIds + resortId
                }
            s.copy(selectedResortIds = newIds, allRegionsSelected = false)
        }
    }

    fun togglePrefecture(prefectureEn: String) {
        _uiState.update { s ->
            val idsInPref =
                s.regions
                    .filter { (it.prefectureEn ?: "") == prefectureEn }
                    .flatMap { it.resorts.map { r -> r.id } }
                    .toSet()
            if (idsInPref.isEmpty()) return@update s
            val allSelected = idsInPref.all { it in s.selectedResortIds }
            val newSelected =
                if (allSelected) {
                    s.selectedResortIds - idsInPref
                } else {
                    s.selectedResortIds + idsInPref
                }
            s.copy(selectedResortIds = newSelected, allRegionsSelected = false)
        }
    }

    fun toggleCertification(id: String) {
        _uiState.update { s ->
            val next =
                if (id in s.certifications) {
                    s.certifications - id
                } else {
                    s.certifications + id
                }
            s.copy(certifications = next)
        }
    }

    fun setCertificationOther(value: String) {
        _uiState.update { it.copy(certificationOther = value) }
    }

    fun setDisplayName(value: String) {
        _uiState.update { it.copy(displayName = value) }
    }

    fun setBio(value: String) {
        _uiState.update { it.copy(bio = value) }
    }

    fun toggleLanguage(code: String) {
        _uiState.update { s ->
            if (code in s.languages) {
                if (s.languages.size <= 1) return@update s
                s.copy(languages = s.languages - code)
            } else {
                s.copy(languages = s.languages + code)
            }
        }
    }

    fun setPriceHalfDay(value: String) {
        _uiState.update { it.copy(priceHalfDay = value.filter { ch -> ch.isDigit() }) }
    }

    fun setPriceFullDay(value: String) {
        _uiState.update { it.copy(priceFullDay = value.filter { ch -> ch.isDigit() }) }
    }

    fun setOffersTransport(value: Boolean) {
        _uiState.update { it.copy(offersTransport = value) }
    }

    fun setOffersPhotography(value: Boolean) {
        _uiState.update { it.copy(offersPhotography = value) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, signupError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordRules = PasswordRules.check(value),
                signupError = null,
            )
        }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, signupError = null) }
    }

    fun onTermsCheckedChange(checked: Boolean) {
        _uiState.update { it.copy(termsChecked = checked) }
    }

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otp = value, otpError = null) }
    }

    private fun savePendingProfile() {
        val s = _uiState.value
        val discipline = disciplineForApi(s.selectedDisciplines)
        val resortIds =
            if (s.allRegionsSelected) {
                s.regions.flatMap { r -> r.resorts.map { it.id } }
            } else {
                s.selectedResortIds.toList()
            }
        val certList = certificationsForApi(s)
        val other =
            if ("other" in s.certifications && s.certificationOther.isNotBlank()) {
                s.certificationOther.trim()
            } else {
                null
            }

        pendingProfile.set(
            InstructorProfileData(
                discipline = discipline,
                teachableLevels = s.teachableLevels.toList().sorted(),
                resortIds = resortIds,
                certifications = certList,
                certificationOther = other,
                displayName = s.displayName.trim(),
                bio = s.bio.trim().ifBlank { null },
                languages = s.languages.toList().sorted(),
                priceHalfDay = s.priceHalfDay.trim().toIntOrNull(),
                priceFullDay = s.priceFullDay.trim().toIntOrNull(),
                offersTransport = s.offersTransport,
                offersPhotography = s.offersPhotography,
            ),
        )
    }

    private fun signUp() {
        val s = _uiState.value
        if (s.isSigningUp) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSigningUp = true, signupError = null) }
            savePendingProfile()
            when (
                val result =
                    authRepository.signUp(
                        email = s.email.trim(),
                        password = s.password,
                        displayName = s.displayName.trim(),
                    )
            ) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(isSigningUp = false, currentStep = 10)
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isSigningUp = false, signupError = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun verifyOtp() {
        val s = _uiState.value
        if (s.otp.length != 6 || s.isVerifyingOtp) return

        viewModelScope.launch {
            _uiState.update { it.copy(isVerifyingOtp = true, otpError = null) }
            when (val result = authRepository.verifyEmailOtp(s.email.trim(), s.otp)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isVerifyingOtp = false) }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isVerifyingOtp = false, otpError = result.message, otp = "")
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun resendOtp() {
        val s = _uiState.value
        if (s.resendCooldown > 0 || s.isResendingOtp) return

        viewModelScope.launch {
            _uiState.update { it.copy(isResendingOtp = true, otpError = null) }
            when (authRepository.resendEmailOtp(s.email.trim())) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isResendingOtp = false,
                            resendMessage = UiText.StringResource(R.string.auth_verify_email_resend_sent),
                        )
                    }
                    startCooldown(60)
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isResendingOtp = false,
                            otpError = UiText.StringResource(R.string.auth_error_generic),
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun startCooldown(seconds: Int) {
        cooldownJob?.cancel()
        cooldownJob =
            viewModelScope.launch {
                for (i in seconds downTo 1) {
                    _uiState.update { it.copy(resendCooldown = i) }
                    delay(1_000L)
                }
                _uiState.update { it.copy(resendCooldown = 0, resendMessage = null) }
            }
    }

    fun submit() {
        val snapshot = _uiState.value
        if (snapshot.isGuestMode) return
        if (snapshot.phase != InstructorWizardPhase.Steps ||
            snapshot.currentStep != snapshot.totalSteps ||
            snapshot.isSubmitting
        ) {
            return
        }
        if (!snapshot.canAdvanceFromCurrentStep()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            val s = _uiState.value
            val discipline = disciplineForApi(s.selectedDisciplines)
            val resortIds =
                if (s.allRegionsSelected) {
                    s.regions.flatMap { r -> r.resorts.map { it.id } }
                } else {
                    s.selectedResortIds.toList()
                }
            val half = s.priceHalfDay.trim().toIntOrNull()
            val full = s.priceFullDay.trim().toIntOrNull()
            val certList = certificationsForApi(s)
            val other =
                if ("other" in s.certifications && s.certificationOther.isNotBlank()) {
                    s.certificationOther.trim()
                } else {
                    null
                }
            val bioOut = s.bio.trim().ifBlank { null }

            when (
                val result =
                    instructorRepository.createProfile(
                        discipline = discipline,
                        teachableLevels = s.teachableLevels.toList().sorted(),
                        resortIds = resortIds,
                        certifications = certList,
                        certificationOther = other,
                        displayName = s.displayName.trim(),
                        bio = bioOut,
                        languages = s.languages.toList().sorted(),
                        priceHalfDay = half,
                        priceFullDay = full,
                        offersTransport = s.offersTransport,
                        offersPhotography = s.offersPhotography,
                    )
            ) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            phase = InstructorWizardPhase.Success,
                            submitError = null,
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            submitError = result.message,
                        )
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                }
            }
        }
    }

    override fun onCleared() {
        cooldownJob?.cancel()
        super.onCleared()
    }
}

private fun disciplineForApi(selected: Set<String>): String =
    when {
        "ski" in selected && "snowboard" in selected -> "both"
        "snowboard" in selected -> "snowboard"
        else -> "ski"
    }

private fun certificationsForApi(s: InstructorWizardUiState): List<String> {
    val withoutOther = s.certifications.filter { it != "other" }
    val withOther =
        if ("other" in s.certifications && s.certificationOther.isNotBlank()) {
            withoutOther + "other"
        } else {
            withoutOther
        }
    return withOther.sorted()
}

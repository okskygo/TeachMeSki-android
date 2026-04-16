package com.teachmeski.app.ui.wizard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.Discipline
import com.teachmeski.app.domain.model.EquipmentRental
import com.teachmeski.app.domain.model.Region
import com.teachmeski.app.domain.repository.LessonRequestRepository
import com.teachmeski.app.domain.repository.ResortRepository
import com.teachmeski.app.util.Resource
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TOTAL_STEPS = 9

enum class WizardPhase {
    Steps,
    Success,
}

data class WizardUiState(
    val currentStep: Int = 1,
    val phase: WizardPhase = WizardPhase.Steps,
    val allRegionsSelected: Boolean = false,
    val selectedResortIds: Set<String> = emptySet(),
    val regions: List<Region> = emptyList(),
    val isLoadingRegions: Boolean = false,
    val discipline: Discipline = Discipline.Ski,
    val groupSize: Int = 1,
    val hasChildren: Boolean = false,
    val skillLevel: Int = 0,
    val datesFlexible: Boolean = true,
    val dateStart: String? = null,
    val dateEnd: String? = null,
    val durationDays: Double = 1.0,
    val languages: Set<String> = setOf("zh"),
    val equipmentRental: EquipmentRental = EquipmentRental.None,
    val needsTransport: Boolean? = null,
    val transportNote: String = "",
    val certPreferences: Set<String> = emptySet(),
    val additionalNotes: String = "",
    val isSubmitting: Boolean = false,
    val submitError: UiText? = null,
) {
    val canAdvanceFromCurrentStep: Boolean
        get() =
            when (currentStep) {
                1 -> allRegionsSelected || selectedResortIds.isNotEmpty()
                2, 3, 4, 7, 8 -> true
                5 -> durationDays >= 0.5
                6 -> languages.isNotEmpty()
                9 -> false
                else -> true
            }
}

@HiltViewModel
class LessonRequestWizardViewModel @Inject constructor(
    private val resortRepository: ResortRepository,
    private val lessonRequestRepository: LessonRequestRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(WizardUiState())
    val uiState: StateFlow<WizardUiState> = _uiState.asStateFlow()

    init {
        loadRegions()
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
                is Resource.Error -> {
                    _uiState.update { it.copy(regions = emptyList(), isLoadingRegions = false) }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoadingRegions = false) }
                }
            }
        }
    }

    fun nextStep() {
        _uiState.update { s ->
            if (s.phase != WizardPhase.Steps) return@update s
            if (s.currentStep >= TOTAL_STEPS) return@update s
            if (!s.canAdvanceFromCurrentStep) return@update s
            s.copy(currentStep = s.currentStep + 1, submitError = null)
        }
    }

    fun prevStep() {
        _uiState.update { s ->
            if (s.phase != WizardPhase.Steps) return@update s
            if (s.currentStep <= 1) return@update s
            s.copy(currentStep = s.currentStep - 1)
        }
    }

    fun goToStep(step: Int) {
        val clamped = step.coerceIn(1, TOTAL_STEPS)
        _uiState.update {
            it.copy(
                currentStep = clamped,
                phase = WizardPhase.Steps,
                submitError = null,
            )
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

    fun setDiscipline(d: Discipline) {
        _uiState.update { s ->
            val newLevel = if (s.discipline != d) 0 else s.skillLevel
            s.copy(discipline = d, skillLevel = newLevel)
        }
    }

    fun setGroupSize(n: Int) {
        _uiState.update { s ->
            s.copy(groupSize = n.coerceIn(1, 12))
        }
    }

    fun setHasChildren(b: Boolean) {
        _uiState.update { it.copy(hasChildren = b) }
    }

    fun setSkillLevel(n: Int) {
        _uiState.update { it.copy(skillLevel = n.coerceIn(0, 4)) }
    }

    fun setDatesFlexible(b: Boolean) {
        _uiState.update { s ->
            s.copy(datesFlexible = b).withClampedDuration()
        }
    }

    fun setDateStart(date: String?) {
        _uiState.update { s ->
            s.copy(dateStart = date).withClampedDuration()
        }
    }

    fun setDateEnd(date: String?) {
        _uiState.update { s ->
            s.copy(dateEnd = date).withClampedDuration()
        }
    }

    fun setDurationDays(d: Double) {
        _uiState.update { s ->
            val max = s.durationUpperBoundInclusive()
            val snapped = (d * 2.0).roundToInt() / 2.0
            s.copy(durationDays = snapped.coerceIn(0.5, max))
        }
    }

    fun toggleLanguage(lang: String) {
        _uiState.update { s ->
            if (lang in s.languages) {
                if (s.languages.size <= 1) return@update s
                s.copy(languages = s.languages - lang)
            } else {
                s.copy(languages = s.languages + lang)
            }
        }
    }

    fun setEquipmentRental(e: EquipmentRental) {
        _uiState.update { it.copy(equipmentRental = e) }
    }

    fun setNeedsTransport(b: Boolean?) {
        _uiState.update { it.copy(needsTransport = b) }
    }

    fun setTransportNote(note: String) {
        _uiState.update { it.copy(transportNote = note) }
    }

    fun toggleCertPreference(cert: String) {
        _uiState.update { s ->
            val next =
                if (cert in s.certPreferences) {
                    s.certPreferences - cert
                } else {
                    s.certPreferences + cert
                }
            s.copy(certPreferences = next)
        }
    }

    fun setAdditionalNotes(notes: String) {
        _uiState.update { it.copy(additionalNotes = notes) }
    }

    fun submit() {
        val snapshot = _uiState.value
        if (snapshot.phase != WizardPhase.Steps ||
            snapshot.currentStep != TOTAL_STEPS ||
            snapshot.isSubmitting
        ) {
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null) }
            val s = _uiState.value
            val dateEndForApi = if (!s.datesFlexible) s.dateEnd ?: s.dateStart else s.dateEnd
            when (
                val result =
                    lessonRequestRepository.submitLessonRequest(
                        discipline = s.discipline.value,
                        skillLevel = s.skillLevel,
                        groupSize = s.groupSize,
                        hasChildren = s.hasChildren,
                        dateStart = s.dateStart,
                        dateEnd = dateEndForApi,
                        datesFlexible = s.datesFlexible,
                        durationDays = s.durationDays,
                        equipmentRental = s.equipmentRental.value,
                        needsTransport = s.needsTransport == true,
                        transportNote = s.transportNote,
                        languages = s.languages.toList().sorted(),
                        additionalNotes = s.additionalNotes,
                        allRegionsSelected = s.allRegionsSelected,
                        resortIds = s.selectedResortIds.toList(),
                        certPreferences = s.certPreferences.toList(),
                    )
            ) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            phase = WizardPhase.Success,
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
}

private fun inclusiveDaySpanBetweenIsoDates(dateStart: String, dateEnd: String?): Double {
    val end = dateEnd ?: dateStart
    val fmt =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    return try {
        val s = fmt.parse(dateStart) ?: return 1.0
        val e = fmt.parse(end) ?: return 1.0
        val diffMs = e.time - s.time
        val diff = (diffMs / 86_400_000L).toInt() + 1
        maxOf(1, diff).toDouble()
    } catch (_: Exception) {
        1.0
    }
}

private fun WizardUiState.computeDateBoundedMaxDays(): Double? {
    if (datesFlexible || dateStart == null) return null
    return inclusiveDaySpanBetweenIsoDates(dateStart, dateEnd)
}

private fun WizardUiState.durationUpperBoundInclusive(): Double {
    return computeDateBoundedMaxDays() ?: 14.0
}

private fun WizardUiState.clampedDurationDays(): Double {
    val max = durationUpperBoundInclusive()
    val snapped = (durationDays * 2.0).roundToInt() / 2.0
    return snapped.coerceIn(0.5, max)
}

private fun WizardUiState.withClampedDuration(): WizardUiState =
    copy(durationDays = clampedDurationDays())

package com.teachmeski.app.ui.profile.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teachmeski.app.R
import com.teachmeski.app.domain.model.DetailError
import com.teachmeski.app.domain.usecase.instructor.DetailLoadResult
import com.teachmeski.app.domain.usecase.instructor.GetInstructorDetailUseCase
import com.teachmeski.app.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InstructorDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getInstructorDetail: GetInstructorDetailUseCase,
) : ViewModel() {

    private val shortId: String = savedStateHandle.get<String>("shortId").orEmpty()

    private val _uiState = MutableStateFlow<InstructorDetailUiState>(InstructorDetailUiState.Loading)
    val uiState: StateFlow<InstructorDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        if (shortId.isBlank()) {
            _uiState.value = InstructorDetailUiState.NotFound
            return
        }
        _uiState.value = InstructorDetailUiState.Loading
        viewModelScope.launch {
            _uiState.value = when (val r = getInstructorDetail(shortId)) {
                is DetailLoadResult.Ok  -> InstructorDetailUiState.Success(r.value)
                is DetailLoadResult.Err -> when (r.error) {
                    is DetailError.NotFound -> InstructorDetailUiState.NotFound
                    is DetailError.Generic  -> InstructorDetailUiState.Error(
                        UiText.StringResource(R.string.instructor_detail_error_generic)
                    )
                }
            }
        }
    }
}

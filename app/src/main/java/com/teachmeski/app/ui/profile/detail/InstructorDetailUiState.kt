package com.teachmeski.app.ui.profile.detail

import com.teachmeski.app.domain.model.InstructorDetailBundle
import com.teachmeski.app.util.UiText

sealed interface InstructorDetailUiState {
    data object Loading : InstructorDetailUiState
    data class Success(val bundle: InstructorDetailBundle) : InstructorDetailUiState
    data class Error(val message: UiText) : InstructorDetailUiState
    data object NotFound : InstructorDetailUiState
}

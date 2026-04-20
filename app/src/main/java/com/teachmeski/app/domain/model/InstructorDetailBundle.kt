package com.teachmeski.app.domain.model

data class InstructorDetailBundle(
    val profile: InstructorProfile,
    val resortsByRegion: List<Region>,
)

sealed interface DetailError {
    data object NotFound : DetailError
    data class Generic(val throwable: Throwable? = null) : DetailError
}

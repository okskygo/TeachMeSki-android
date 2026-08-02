package com.teachmeski.app.domain.model

data class InstructorDetailBundle(
    val profile: InstructorProfile,
    val resortsByRegion: List<Region>,
    /** F-117: approved certificates power the public detail page's CertificatesSection. */
    val approvedCertificates: List<InstructorCertificate> = emptyList(),
)

sealed interface DetailError {
    data object NotFound : DetailError
    data class Generic(val throwable: Throwable? = null) : DetailError
}

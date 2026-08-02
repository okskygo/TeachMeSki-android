package com.teachmeski.app.domain.model

/** F-117 per-image certificate with manual review status. */
enum class CertificateStatus { PENDING, APPROVED, REJECTED;

    companion object {
        fun fromString(raw: String): CertificateStatus = when (raw) {
            "approved" -> APPROVED
            "rejected" -> REJECTED
            else -> PENDING
        }
    }
}

data class InstructorCertificate(
    val id: String,
    val imageUrl: String,
    val status: CertificateStatus,
)

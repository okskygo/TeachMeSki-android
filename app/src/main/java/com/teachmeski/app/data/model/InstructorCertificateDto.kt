package com.teachmeski.app.data.model

import com.teachmeski.app.domain.model.CertificateStatus
import com.teachmeski.app.domain.model.InstructorCertificate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstructorCertificateDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("image_url") val imageUrl: String = "",
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String = "",
)

fun InstructorCertificateDto.toDomain() = InstructorCertificate(
    id = id,
    imageUrl = imageUrl,
    status = CertificateStatus.fromString(status),
)

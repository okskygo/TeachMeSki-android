package com.teachmeski.app.domain.model

import javax.inject.Inject
import javax.inject.Singleton

data class InstructorProfileData(
    val discipline: String,
    val teachableLevels: List<Int>,
    val resortIds: List<String>,
    val certifications: List<String>,
    val certificationOther: String?,
    val displayName: String,
    val bio: String?,
    val languages: List<String>,
    val priceHalfDay: Int?,
    val priceFullDay: Int?,
    val offersTransport: Boolean,
    val offersPhotography: Boolean,
)

@Singleton
class PendingInstructorProfile @Inject constructor() {
    @Volatile
    private var data: InstructorProfileData? = null

    fun set(profile: InstructorProfileData) {
        data = profile
    }

    fun get(): InstructorProfileData? = data

    fun clear() {
        data = null
    }
}

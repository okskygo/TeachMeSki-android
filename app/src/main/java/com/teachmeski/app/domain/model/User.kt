package com.teachmeski.app.domain.model

data class User(
    val id: String,
    val displayName: String?,
    val avatarUrl: String?,
    val role: UserRole,
    val deletedAt: String?,
)

enum class UserRole(val value: String) {
    Student("student"),
    Instructor("instructor"),
    Both("both");

    companion object {
        fun fromString(value: String): UserRole = when (value) {
            "instructor" -> Instructor
            "both" -> Both
            else -> Student
        }
    }
}

package com.teachmeski.app.navigation

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable data object Login : Route
    @Serializable data object Register : Route
    @Serializable data object Signup : Route
    @Serializable data object ForgotPassword : Route
    @Serializable data class VerifyEmail(val email: String) : Route

    @Serializable data object MyRequests : Route
    @Serializable data class RequestDetail(val id: String) : Route
    @Serializable data object LessonRequestWizard : Route

    @Serializable data object Explore : Route
    @Serializable data object Unlocked : Route

    @Serializable data object ChatRoomList : Route
    @Serializable data class Chat(val roomId: String) : Route

    @Serializable data object Account : Route
    @Serializable data object AccountSettings : Route
    @Serializable data object InstructorAccount : Route
    @Serializable data object InstructorProfile : Route
    @Serializable data class InstructorWizard(val isGuestMode: Boolean = false) : Route

    @Serializable data object Wallet : Route
    @Serializable data object CreditHistory : Route

    @Serializable data object Contact : Route
    @Serializable data class Legal(val type: String) : Route
    @Serializable data class InstructorDetail(val shortId: String) : Route

    @Serializable data object AuthGraph : Route
    @Serializable data object StudentGraph : Route
    @Serializable data object InstructorGraph : Route
}

package com.teachmeski.app.ui.explore

import com.teachmeski.app.domain.model.ExploreLessonRequest
import com.teachmeski.app.domain.model.LessonRequestStatus

/**
 * Bottom-bar CTA state for the Explore detail screen. Mirrors the card footer
 * (`FooterBlock`) state machine so the detail page shows the same action.
 */
sealed interface ExploreCtaState {
    data class ViewChat(val roomId: String) : ExploreCtaState
    data object AlreadyUnlocked : ExploreCtaState
    data class Unlock(val tokenCost: Int) : ExploreCtaState

    /** Active but quota is full — instructor cannot unlock right now. */
    data object SlotsFull : ExploreCtaState

    /** Request is closed/expired — no longer unlockable. */
    data object Unavailable : ExploreCtaState
}

fun exploreCtaState(request: ExploreLessonRequest): ExploreCtaState {
    val remaining = (request.quotaLimit - request.unlockCount).coerceAtLeast(0)
    val isActive = request.status == LessonRequestStatus.Active
    return when {
        request.isUnlockedByMe && request.myChatRoomId != null ->
            ExploreCtaState.ViewChat(request.myChatRoomId)
        request.isUnlockedByMe -> ExploreCtaState.AlreadyUnlocked
        isActive && remaining > 0 -> ExploreCtaState.Unlock(request.baseTokenCost)
        isActive -> ExploreCtaState.SlotsFull
        else -> ExploreCtaState.Unavailable
    }
}

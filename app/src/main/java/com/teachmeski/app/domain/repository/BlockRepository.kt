package com.teachmeski.app.domain.repository

import com.teachmeski.app.util.Resource

interface BlockRepository {
    suspend fun blockUser(blockedUserId: String): Resource<Unit>
    suspend fun unblockUser(blockedUserId: String): Resource<Unit>

    /** Returns true iff the current user has blocked [otherUserId]. Best-effort; false on failure. */
    suspend fun haveIBlocked(otherUserId: String): Boolean

    /**
     * F-110: returns true iff the signed-in user has been blocked by
     * [otherUserId]. Wraps the SECURITY DEFINER RPC `am_i_blocked_by` —
     * the `blocks` SELECT policy only allows the blocker to read their
     * own row, so the blocked party cannot see the relationship directly.
     * Returns false on any failure (best-effort).
     */
    suspend fun amIBlockedBy(otherUserId: String): Boolean
}

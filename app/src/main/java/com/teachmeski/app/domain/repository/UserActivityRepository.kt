package com.teachmeski.app.domain.repository

/** Fire-and-forget. Swallows errors. Server-side 1h cooldown. */
interface UserActivityRepository {
    suspend fun touch()
}

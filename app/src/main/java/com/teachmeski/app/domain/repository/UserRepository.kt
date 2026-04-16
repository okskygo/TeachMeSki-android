package com.teachmeski.app.domain.repository

import com.teachmeski.app.domain.model.User
import com.teachmeski.app.util.Resource

interface UserRepository {
    suspend fun getUserById(userId: String): Resource<User>
    suspend fun updateDisplayName(userId: String, displayName: String): Resource<Unit>
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray, extension: String): Resource<String>
}

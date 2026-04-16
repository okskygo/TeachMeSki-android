package com.teachmeski.app.domain.repository

import com.teachmeski.app.util.Resource

interface ContactRepository {
    suspend fun submitContact(name: String, email: String, message: String): Resource<Unit>
}

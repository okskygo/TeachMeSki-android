package com.teachmeski.app.data.remote

import com.teachmeski.app.data.model.ContactSubmissionDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun submitContact(name: String, email: String, message: String, userId: String?) {
        supabaseClient.postgrest
            .from("contact_submissions")
            .insert(
                ContactSubmissionDto(
                    name = name,
                    email = email,
                    message = message,
                    userId = userId,
                )
            )
    }
}

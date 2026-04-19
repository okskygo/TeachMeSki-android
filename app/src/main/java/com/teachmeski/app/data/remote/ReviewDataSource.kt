package com.teachmeski.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    @Serializable
    data class ReviewIdRow(@SerialName("instructor_id") val instructorId: String)

    suspend fun submitReview(reviewerId: String, instructorId: String, rating: Int, comment: String?) {
        supabaseClient.postgrest.from("reviews")
            .insert(
                buildJsonObject {
                    put("reviewer_id", reviewerId)
                    put("instructor_id", instructorId)
                    put("rating", rating)
                    if (!comment.isNullOrBlank()) put("comment", comment)
                },
            )
    }

    suspend fun getReviewedInstructorIds(reviewerId: String): List<String> =
        supabaseClient.postgrest.from("reviews")
            .select(columns = Columns.list("instructor_id")) {
                filter { eq("reviewer_id", reviewerId) }
            }
            .decodeList<ReviewIdRow>()
            .map { it.instructorId }
}

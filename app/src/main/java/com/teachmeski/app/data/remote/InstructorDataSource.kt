package com.teachmeski.app.data.remote

import com.teachmeski.app.data.model.InstructorCertificateDto
import com.teachmeski.app.data.model.InstructorProfileDto
import com.teachmeski.app.data.model.SkiResortDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstructorDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {
    suspend fun getProfileByUserId(userId: String): InstructorProfileDto? =
        supabaseClient.postgrest.from("instructor_profiles")
            .select {
                filter { eq("user_id", userId) }
            }
            .decodeSingleOrNull<InstructorProfileDto>()

    suspend fun getProfileByShortId(shortId: String): InstructorProfileDto? =
        supabaseClient.postgrest.from("instructor_profiles")
            .select {
                filter { eq("short_id", shortId) }
            }
            .decodeSingleOrNull<InstructorProfileDto>()

    suspend fun getResortNamesByIds(resortIds: List<String>): List<SkiResortDto> {
        if (resortIds.isEmpty()) return emptyList()
        return supabaseClient.postgrest.from("ski_resorts")
            .select {
                filter { isIn("id", resortIds) }
            }
            .decodeList<SkiResortDto>()
    }

    suspend fun createProfile(
        userId: String,
        discipline: String,
        teachableLevels: List<Int>,
        resortIds: List<String>,
        certifications: List<String>,
        certificationOther: String?,
        displayName: String,
        bio: String?,
        languages: List<String>,
        priceHalfDay: Int?,
        priceFullDay: Int?,
        offersTransport: Boolean,
        offersPhotography: Boolean,
    ): String {
        val shortId = UUID.randomUUID().toString().replace("-", "").take(8)
        val payload = buildJsonObject {
            put("user_id", userId)
            put("short_id", shortId)
            put("display_name", displayName)
            if (bio != null) put("bio", bio)
            put("discipline", discipline)
            putJsonArray("teachable_levels") { teachableLevels.forEach { add(JsonPrimitive(it)) } }
            putJsonArray("resort_ids") { resortIds.forEach { add(JsonPrimitive(it)) } }
            putJsonArray("certifications") {
                certifications.filter { it != "other" }.forEach { add(JsonPrimitive(it)) }
            }
            if (certifications.contains("other") && certificationOther != null) {
                put("certification_other", certificationOther)
            }
            putJsonArray("languages") { languages.forEach { add(JsonPrimitive(it)) } }
            if (priceHalfDay != null) put("price_half_day", priceHalfDay)
            if (priceFullDay != null) put("price_full_day", priceFullDay)
            put("offers_transport", offersTransport)
            put("offers_photography", offersPhotography)
        }
        val result = supabaseClient.postgrest.from("instructor_profiles")
            .insert(payload) { select(Columns.list("id")) }
            .decodeSingle<InstructorProfileDto>()
        return result.id
    }

    suspend fun updateProfile(
        profileId: String,
        updates: Map<String, Any?>,
    ) {
        val payload = buildJsonObject {
            updates.forEach { (key, value) ->
                when (value) {
                    is String -> put(key, value)
                    is Int -> put(key, value)
                    is Boolean -> put(key, value)
                    is List<*> -> {
                        putJsonArray(key) {
                            @Suppress("UNCHECKED_CAST")
                            when {
                                value.firstOrNull() is Int -> (value as List<Int>).forEach { add(JsonPrimitive(it)) }
                                else -> (value as List<String>).forEach { add(JsonPrimitive(it)) }
                            }
                        }
                    }
                    null -> put(key, JsonNull)
                    else -> error("Unsupported value type for key '$key': ${value::class.simpleName}")
                }
            }
        }
        supabaseClient.postgrest.from("instructor_profiles")
            .update(payload) { filter { eq("id", profileId) } }
    }

    suspend fun toggleAcceptingRequests(profileId: String, isAccepting: Boolean) {
        supabaseClient.postgrest.from("instructor_profiles")
            .update({ set("is_accepting_requests", isAccepting) }) {
                filter { eq("id", profileId) }
            }
    }

    /** Shared upload for portfolio (`folder = "portfolio"`) and certificate (`folder = "certificates"`) images. */
    suspend fun uploadImage(userId: String, bytes: ByteArray, contentType: String, folder: String): String {
        val ext = if (contentType == "image/png") "png" else "jpg"
        val filename = "${System.currentTimeMillis()}.$ext"
        val path = "$userId/$folder/$filename"
        val bucket = supabaseClient.storage.from("avatars")
        val parsedType = ContentType.parse(contentType)
        bucket.upload(path, bytes) {
            this.contentType = parsedType
            upsert = false
        }
        return bucket.publicUrl(path)
    }

    suspend fun deletePortfolioImage(profileId: String, imageUrl: String, currentUrls: List<String>) {
        val updated = currentUrls.filter { it != imageUrl }
        supabaseClient.postgrest.from("instructor_profiles")
            .update({ set("portfolio_urls", updated) }) {
                filter { eq("id", profileId) }
            }
        // Best-effort cleanup AFTER the DB update — the array is the
        // source of truth (PRD §5).
        deleteStorageObject(imageUrl)
    }

    // F-117: Certificate review rows (instructor_certificates)

    suspend fun getCertificates(userId: String, approvedOnly: Boolean): List<InstructorCertificateDto> =
        supabaseClient.postgrest.from("instructor_certificates")
            .select {
                filter {
                    eq("user_id", userId)
                    if (approvedOnly) eq("status", "approved")
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<InstructorCertificateDto>()

    suspend fun insertCertificate(userId: String, imageUrl: String): InstructorCertificateDto =
        supabaseClient.postgrest.from("instructor_certificates")
            .insert(
                buildJsonObject {
                    put("user_id", userId)
                    put("image_url", imageUrl)
                },
            ) { select() }
            .decodeSingle<InstructorCertificateDto>()

    suspend fun deleteCertificateRow(certificateId: String, imageUrl: String) {
        supabaseClient.postgrest.from("instructor_certificates")
            .delete { filter { eq("id", certificateId) } }
        // Best-effort cleanup AFTER the row delete — the row is the
        // source of truth (PRD §5), and deleting it first lifts the
        // approved-file storage freeze.
        deleteStorageObject(imageUrl)
    }

    /**
     * Best-effort storage removal shared by [deletePortfolioImage] and
     * [deleteCertificateRow], and by the upload-rollback path in
     * `InstructorRepositoryImpl` when an `instructor_certificates` INSERT
     * fails after the file has already landed in storage.
     */
    suspend fun deleteStorageObject(imageUrl: String) {
        val prefix = "/storage/v1/object/public/avatars/"
        val idx = imageUrl.indexOf(prefix)
        if (idx != -1) {
            val storagePath = imageUrl.substring(idx + prefix.length)
            try {
                supabaseClient.storage.from("avatars").delete(storagePath)
            } catch (_: Exception) {
            }
        }
    }

    suspend fun updateDisplayName(userId: String, displayName: String) {
        supabaseClient.postgrest.from("users")
            .update({ set("display_name", displayName) }) {
                filter { eq("id", userId) }
            }
    }

    suspend fun updateUserRole(userId: String, role: String) {
        supabaseClient.postgrest.from("users")
            .update({ set("role", role) }) {
                filter { eq("id", userId) }
            }
    }

    suspend fun grantWelcomeBonus() {
        supabaseClient.functions.invoke("grant-welcome-bonus").bodyAsText()
    }

    suspend fun checkProfileExists(userId: String): Boolean {
        @Serializable
        data class IdRow(val id: String)
        val result = supabaseClient.postgrest.from("instructor_profiles")
            .select(columns = Columns.list("id")) {
                filter { eq("user_id", userId) }
            }
            .decodeSingleOrNull<IdRow>()
        return result != null
    }
}

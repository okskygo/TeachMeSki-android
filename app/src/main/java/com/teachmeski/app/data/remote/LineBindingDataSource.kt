package com.teachmeski.app.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Singleton
class LineBindingDataSource @Inject constructor(
    private val supabase: SupabaseClient,
) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    suspend fun callback(
        code: String,
        codeVerifier: String,
        nonce: String,
    ): LineCallbackResponse {
        val response = supabase.functions.invoke(
            function = "line-oauth-callback",
            body = LineCallbackBody(code = code, codeVerifier = codeVerifier, nonce = nonce),
        )
        val text = response.bodyAsText()
        // Edge Function returns JSON whether success or error (see
        // supabase/functions/line-oauth-callback/index.ts: jsonResponse).
        // We accept any decodable body and let the caller branch on
        // `success` / `error`. Non-2xx status with non-JSON body is mapped
        // to a generic error.
        return runCatching {
            json.decodeFromString(LineCallbackResponse.serializer(), text)
        }.getOrElse {
            LineCallbackResponse(
                success = null,
                error = if (response.status.isSuccess()) "binding_failed" else "binding_failed",
            )
        }
    }

    @Serializable
    private data class LineCallbackBody(
        val code: String,
        @SerialName("code_verifier") val codeVerifier: String,
        val nonce: String,
    )

    @Serializable
    data class LineCallbackResponse(
        val success: Boolean? = null,
        val error: String? = null,
    )
}

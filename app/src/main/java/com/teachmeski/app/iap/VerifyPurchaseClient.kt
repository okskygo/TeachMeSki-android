package com.teachmeski.app.iap

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.functions.functions
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Posts `{ product_id, purchase_token }` to the `verify-purchase` Edge Function
 * and returns the Edge Function's response, which includes the credited amount
 * and the user's new token balance.
 *
 * The Edge Function is idempotent (keyed by Google orderId in
 * `token_transactions.external_id`). Callers can safely retry on any non-2xx
 * response without double-crediting.
 */
@Singleton
class VerifyPurchaseClient @Inject constructor(
    private val supabase: SupabaseClient,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun verify(
        productId: String,
        purchaseToken: String,
    ): Result<VerifyPurchaseResponse> = runCatching {
        val body = json.encodeToString(VerifyPurchaseBody(productId, purchaseToken))
        val response = supabase.functions.invoke(
            function = "verify-purchase",
            body = body,
        )
        val text = response.bodyAsText()
        val status: HttpStatusCode = response.status
        if (!status.isSuccess()) {
            val code = runCatching { json.decodeFromString<ErrorBody>(text).code }.getOrNull()
            throw VerifyPurchaseException(
                IapError.VerifyFailed(httpStatus = status.value, code = code),
            )
        }
        json.decodeFromString(VerifyPurchaseResponse.serializer(), text)
    }.recoverCatching { e ->
        when (e) {
            is VerifyPurchaseException -> throw e
            else -> throw VerifyPurchaseException(
                IapError.VerifyFailed(httpStatus = -1, code = null),
                cause = e,
            )
        }
    }

    @Serializable
    private data class VerifyPurchaseBody(
        @SerialName("product_id") val productId: String,
        @SerialName("purchase_token") val purchaseToken: String,
    )

    @Serializable
    private data class ErrorBody(val code: String? = null, val message: String? = null)

    class VerifyPurchaseException(
        val iapError: IapError.VerifyFailed,
        cause: Throwable? = null,
    ) : RuntimeException("verify-purchase failed: $iapError", cause)
}

@Serializable
data class VerifyPurchaseResponse(
    @SerialName("credited") val credited: Boolean,
    @SerialName("new_balance") val newBalance: Int,
    @SerialName("transaction_id") val transactionId: String? = null,
    @SerialName("delta") val delta: Int = 0,
)

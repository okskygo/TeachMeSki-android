package com.teachmeski.app.util

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

data class LineOAuthInit(
    val state: String,
    val nonce: String,
    val codeVerifier: String,
    val codeChallenge: String,
)

@Singleton
class LineOAuthState @Inject constructor(@ApplicationContext private val context: Context) {

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "line_oauth",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    private val random = SecureRandom()

    private fun b64url(bytes: ByteArray): String =
        android.util.Base64.encodeToString(
            bytes,
            android.util.Base64.URL_SAFE or
                android.util.Base64.NO_PADDING or
                android.util.Base64.NO_WRAP,
        )

    fun init(): LineOAuthInit {
        val state = b64url(ByteArray(16).also(random::nextBytes))
        val nonce = b64url(ByteArray(16).also(random::nextBytes))
        val verifier = b64url(ByteArray(64).also(random::nextBytes))
        val challenge = b64url(
            MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray()),
        )
        prefs.edit {
            putString(KEY_STATE, state)
            putString(KEY_NONCE, nonce)
            putString(KEY_VERIFIER, verifier)
            putLong(KEY_CREATED_AT, System.currentTimeMillis())
        }
        return LineOAuthInit(state, nonce, verifier, challenge)
    }

    data class Persisted(
        val state: String,
        val nonce: String,
        val verifier: String,
    )

    fun consume(expectedState: String): Persisted? {
        val state = prefs.getString(KEY_STATE, null) ?: return null
        val nonce = prefs.getString(KEY_NONCE, null) ?: return null
        val verifier = prefs.getString(KEY_VERIFIER, null) ?: return null
        val createdAt = prefs.getLong(KEY_CREATED_AT, 0L)
        prefs.edit { clear() }
        if (state != expectedState) return null
        if (System.currentTimeMillis() - createdAt > TTL_MS) return null
        return Persisted(state, nonce, verifier)
    }

    private companion object {
        const val KEY_STATE = "state"
        const val KEY_NONCE = "nonce"
        const val KEY_VERIFIER = "verifier"
        const val KEY_CREATED_AT = "created_at"
        const val TTL_MS = 10L * 60L * 1000L
    }
}

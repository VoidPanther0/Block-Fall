package com.example.security

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

enum class IntegrityStatusCode(val wireValue: String) {
    NOT_CHECKED("NOT_CHECKED"),
    PENDING_SERVER_VERIFICATION("PENDING_SERVER_VERIFICATION"),
    LOCAL_UNAVAILABLE("LOCAL_UNAVAILABLE"),
    VERIFIED_SERVER("VERIFIED_SERVER"),
    REJECTED_SERVER("REJECTED_SERVER")
}

data class IntegrityCheckResult(
    val status: IntegrityStatusCode,
    val nonce: String,
    val tokenDigest: String,
    val tokenLength: Int,
    val appVersionCode: Int,
    val rawToken: String?,
    val summaryMessage: String
)

/**
 * Integrates Google Play Integrity Standard API for runtime app/device attestation.
 * Note: Token cryptographic decryption and verdict evaluation must occur on a trusted backend
 * (e.g., verifyPlayIntegrityAndSync Cloud Function); the client only requests and submits the
 * opaque token and never self-asserts VERIFIED_SERVER.
 */
class PlayIntegrityManager(private val context: Context) {

    @Volatile
    private var integrityTokenProvider: StandardIntegrityManager.StandardIntegrityTokenProvider? = null

    suspend fun requestAttestation(): IntegrityCheckResult {
        val nonce = generateNonce()
        val versionCode = resolveVersionCode()

        val rawToken = withTimeoutOrNull(ATTESTATION_TIMEOUT_MS) {
            fetchStandardIntegrityToken(nonce)
        }

        if (rawToken.isNullOrBlank()) {
            return IntegrityCheckResult(
                status = IntegrityStatusCode.LOCAL_UNAVAILABLE,
                nonce = nonce,
                tokenDigest = sha256Hex("UNAVAILABLE:$nonce"),
                tokenLength = 1,
                appVersionCode = versionCode,
                rawToken = null,
                summaryMessage = "Play Integrity token unavailable on this device/emulator"
            )
        }

        return IntegrityCheckResult(
            status = IntegrityStatusCode.PENDING_SERVER_VERIFICATION,
            nonce = nonce,
            tokenDigest = sha256Hex(rawToken),
            tokenLength = rawToken.length.coerceIn(1, 8192),
            appVersionCode = versionCode,
            rawToken = rawToken,
            summaryMessage = "Attestation token captured (${rawToken.length} B); queued for server check"
        )
    }

    private suspend fun fetchStandardIntegrityToken(requestHash: String): String? {
        return try {
            val standardManager = IntegrityManagerFactory.createStandard(context.applicationContext)
            val provider = integrityTokenProvider ?: prepareProvider(standardManager)?.also {
                integrityTokenProvider = it
            } ?: return null

            suspendCancellableCoroutine { cont: CancellableContinuation<String?> ->
                val request = StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                    .setRequestHash(requestHash)
                    .build()
                provider.request(request)
                    .addOnSuccessListener { response ->
                        if (cont.isActive) cont.resume(response.token())
                    }
                    .addOnFailureListener { error ->
                        Log.w(TAG, "Play Integrity token request failed: ${error.message}")
                        if (cont.isActive) cont.resume(null)
                    }
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Play Integrity unavailable: ${t.message}")
            null
        }
    }

    private suspend fun prepareProvider(
        manager: StandardIntegrityManager
    ): StandardIntegrityManager.StandardIntegrityTokenProvider? {
        return suspendCancellableCoroutine { cont ->
            val builder = StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
            val cloudProjectNumber = resolveCloudProjectNumber()
            if (cloudProjectNumber > 0L) {
                builder.setCloudProjectNumber(cloudProjectNumber)
            }
            manager.prepareIntegrityToken(builder.build())
                .addOnSuccessListener { provider ->
                    if (cont.isActive) cont.resume(provider)
                }
                .addOnFailureListener { error ->
                    Log.w(TAG, "Prepare Play Integrity failed: ${error.message}")
                    if (cont.isActive) cont.resume(null)
                }
        }
    }

    private fun resolveCloudProjectNumber(): Long {
        return try {
            val resId = context.resources.getIdentifier(
                "gcm_defaultSenderId",
                "string",
                context.packageName
            )
            if (resId != 0) context.getString(resId).toLongOrNull() ?: 0L else 0L
        } catch (_: Exception) {
            0L
        }
    }

    private fun resolveVersionCode(): Int {
        return try {
            BuildConfig.VERSION_CODE.coerceAtLeast(1)
        } catch (_: Exception) {
            1
        }
    }

    companion object {
        private const val TAG = "PlayIntegrity"
        private const val ATTESTATION_TIMEOUT_MS = 6_000L

        fun generateNonce(): String {
            val bytes = ByteArray(24)
            SecureRandom().nextBytes(bytes)
            return Base64.encodeToString(
                bytes,
                Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
            )
        }

        fun sha256Hex(value: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(value.toByteArray(Charsets.UTF_8))
            return digest.joinToString(separator = "") { "%02x".format(it) }
        }
    }
}

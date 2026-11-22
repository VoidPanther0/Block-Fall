package com.example.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

sealed interface DecryptionResult {
    data class Verified(val plaintext: String) : DecryptionResult
    data object Missing : DecryptionResult
    data class TamperedOrCorrupted(val reason: String) : DecryptionResult
}

interface SecretKeyProvider {
    fun getOrCreateKey(): SecretKey
}

/**
 * Provides a 256-bit AES-GCM key from AndroidKeyStore on Android devices.
 * In local JVM/Robolectric environments where AndroidKeyStore is absent, falls back
 * to an ephemeral process-scoped AES key so tests can exercise real AES-GCM encryption
 * and tamper detection without hardware Keystore support.
 */
class AndroidKeyStoreSecretKeyProvider(
    private val keyAlias: String = DEFAULT_KEY_ALIAS
) : SecretKeyProvider {

    override fun getOrCreateKey(): SecretKey {
        return try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            val existing = keyStore.getKey(keyAlias, null) as? SecretKey
            if (existing != null) return existing

            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val spec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setRandomizedEncryptionRequired(true)
                .build()
            keyGen.init(spec)
            keyGen.generateKey()
        } catch (_: Exception) {
            jvmTestKeys.computeIfAbsent(keyAlias) {
                val raw = ByteArray(32)
                SecureRandom().nextBytes(raw)
                SecretKeySpec(raw, "AES")
            }
        }
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val DEFAULT_KEY_ALIAS = "blockfalls_entitlement_aes_v1"
        private val jvmTestKeys = ConcurrentHashMap<String, SecretKey>()
    }
}

/**
 * Hardware-backed AES-256-GCM envelope protector for sensitive local progress and
 * synced entitlement cache entries.
 *
 * Security model:
 * - Protects against casual XML/DataStore tampering on unrooted devices and binds each
 *   record to its package name and record key via GCM Additional Authenticated Data (AAD).
 * - Client-side cryptography alone is defense-in-depth, not 100% unhackable; authoritative
 *   currency, purchases, and rewarded ad entitlements must still be enforced server-side.
 */
class EncryptedEntitlementCache(
    private val packageName: String,
    private val keyProvider: SecretKeyProvider = AndroidKeyStoreSecretKeyProvider()
) {
    constructor(context: Context) : this(packageName = context.packageName)

    fun encrypt(recordKey: String, plaintext: String): String {
        val secretKey = keyProvider.getOrCreateKey()
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        cipher.updateAAD(buildAad(recordKey))

        val iv = cipher.iv
        val ciphertext = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        val ivB64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val cipherB64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        return "$ENVELOPE_PREFIX:$ivB64:$cipherB64"
    }

    fun decrypt(recordKey: String, envelope: String?): DecryptionResult {
        if (envelope.isNullOrBlank()) return DecryptionResult.Missing

        val parts = envelope.split(':')
        if (parts.size != 3 || parts[0] != ENVELOPE_PREFIX) {
            return DecryptionResult.TamperedOrCorrupted("invalid_envelope_format")
        }

        return try {
            val iv = Base64.decode(parts[1], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[2], Base64.NO_WRAP)
            if (iv.size != GCM_IV_LENGTH_BYTES || ciphertext.size <= GCM_TAG_LENGTH_BYTES) {
                return DecryptionResult.TamperedOrCorrupted("invalid_iv_or_ciphertext_length")
            }

            val secretKey = keyProvider.getOrCreateKey()
            val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                secretKey,
                GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)
            )
            cipher.updateAAD(buildAad(recordKey))

            val plaintextBytes = cipher.doFinal(ciphertext)
            DecryptionResult.Verified(String(plaintextBytes, StandardCharsets.UTF_8))
        } catch (e: Exception) {
            DecryptionResult.TamperedOrCorrupted(e.javaClass.simpleName)
        }
    }

    private fun buildAad(recordKey: String): ByteArray =
        "blockfalls|$ENVELOPE_PREFIX|$packageName|$recordKey".toByteArray(StandardCharsets.UTF_8)

    companion object {
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val ENVELOPE_PREFIX = "v1"
        private const val GCM_IV_LENGTH_BYTES = 12
        private const val GCM_TAG_LENGTH_BITS = 128
        private const val GCM_TAG_LENGTH_BYTES = GCM_TAG_LENGTH_BITS / 8
    }
}

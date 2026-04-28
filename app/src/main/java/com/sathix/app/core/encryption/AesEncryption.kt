package com.sathix.app.core.encryption

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AesEncryption @Inject constructor() {

    fun generateKey(): String {
        val key = ByteArray(32)
        SecureRandom().nextBytes(key)
        return Base64.encodeToString(key, Base64.NO_WRAP)
    }

    fun encrypt(plain: String, base64Key: String): String {
        val key = secretKey(base64Key)
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        val cipherText = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + cipherText.size).apply {
            System.arraycopy(iv, 0, this, 0, iv.size)
            System.arraycopy(cipherText, 0, this, iv.size, cipherText.size)
        }
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(payload: String, base64Key: String): String {
        val data = Base64.decode(payload, Base64.NO_WRAP)
        val iv = data.copyOfRange(0, 12)
        val cipherText = data.copyOfRange(12, data.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey(base64Key), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(cipherText), Charsets.UTF_8)
    }

    private fun secretKey(base64: String): SecretKey =
        SecretKeySpec(Base64.decode(base64, Base64.NO_WRAP), "AES")

    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}

package com.example.data.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

object SecurityUtils {
    private val secureRandom = SecureRandom()

    fun generateSalt(length: Int = 16): String {
        val bytes = ByteArray(length)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val input = "$salt:$password:$salt"
        val digest = MessageDigest.getInstance("SHA-256")
        var hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        // Multiple iterations to strengthen hash resistance
        for (i in 0 until 1000) {
            digest.reset()
            hash = digest.digest(hash)
        }
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val computedHash = hashPassword(password, salt)
        return MessageDigest.isEqual(computedHash.toByteArray(Charsets.UTF_8), expectedHash.toByteArray(Charsets.UTF_8))
    }

    fun generateSessionToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return "fpsess_" + bytes.joinToString("") { "%02x".format(it) }
    }

    fun generateOtpToken(): String {
        val code = 100000 + secureRandom.nextInt(900000)
        return code.toString()
    }
}

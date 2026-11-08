package com.example.data.models

data class UserAccount(
    val employeeId: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val branch: String,
    val passwordSalt: String,
    val passwordHash: String,
    val failedLoginAttempts: Int = 0,
    val isLocked: Boolean = false,
    val lastLoginAt: String? = null
)

data class AuthSession(
    val token: String,
    val user: UserAccount,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (8 * 60 * 60 * 1000) // 8-hour enterprise session
) {
    val isExpired: Boolean get() = System.currentTimeMillis() > expiresAt
}

data class PasswordResetRequest(
    val email: String,
    val otpToken: String,
    val expiresAt: Long,
    val isVerified: Boolean = false
)

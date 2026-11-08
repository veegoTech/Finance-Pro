package com.example.data.remote

import com.example.data.models.AuthSession
import com.example.data.models.PasswordResetRequest
import com.example.data.models.UserAccount
import com.example.data.models.UserRole
import com.example.data.security.SecurityUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

interface AuthService {
    suspend fun login(identifier: String, password: String): Result<AuthSession>
    suspend fun logout(token: String): Boolean
    suspend fun requestPasswordReset(email: String): Result<String>
    suspend fun verifyResetOtp(email: String, otp: String): Result<Boolean>
    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Boolean>
    suspend fun validateSession(token: String): AuthSession?
    suspend fun registerEmployee(
        adminToken: String,
        fullName: String,
        email: String,
        phone: String,
        role: UserRole,
        branch: String,
        initialPassword: String
    ): Result<UserAccount>
    suspend fun getRegisteredDemoAccounts(): List<Pair<String, String>>
}

class MockAuthBackendService : AuthService {

    // In-memory PostgreSQL User Table (Pass-hashes only, never plain-text)
    private val usersTable = mutableListOf<UserAccount>()
    private val sessionsTable = mutableMapOf<String, AuthSession>()
    private val resetRequests = mutableListOf<PasswordResetRequest>()

    init {
        // Pre-seed the 5 enterprise employee roles with secure salted hashes
        seedUser("EMP-SA-001", "Arthur Pendelton", "superadmin@financepro.com", "+1 (555) 001-9901", UserRole.SUPER_ADMIN, "Global HQ", "Password@123")
        seedUser("EMP-AD-002", "James Sterling", "admin@financepro.com", "+1 (555) 001-9902", UserRole.ADMIN, "Metro Headquarters", "Password@123")
        seedUser("EMP-FM-003", "Victoria Bennett", "finance@financepro.com", "+1 (555) 001-9903", UserRole.FINANCE_MANAGER, "Central Treasury", "Password@123")
        seedUser("EMP-CS-004", "Robert Chen", "supervisor@financepro.com", "+1 (555) 001-9904", UserRole.COLLECTION_SUPERVISOR, "Metro North Branch", "Password@123")
        seedUser("EMP-CE-005", "David Miller", "collector@financepro.com", "+1 (555) 001-9905", UserRole.COLLECTION_EMPLOYEE, "Metro North Branch", "Password@123")
    }

    private fun seedUser(id: String, name: String, email: String, phone: String, role: UserRole, branch: String, plainPassword: String) {
        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(plainPassword, salt)
        usersTable.add(
            UserAccount(
                employeeId = id,
                fullName = name,
                email = email,
                phone = phone,
                role = role,
                branch = branch,
                passwordSalt = salt,
                passwordHash = hash
            )
        )
    }

    override suspend fun login(identifier: String, password: String): Result<AuthSession> {
        delay(120) // simulate crypto verification latency
        val cleanId = identifier.trim()
        val user = usersTable.find {
            it.email.equals(cleanId, ignoreCase = true) || it.employeeId.equals(cleanId, ignoreCase = true)
        } ?: return Result.failure(Exception("Invalid Employee ID or Email address."))

        if (user.isLocked) {
            return Result.failure(Exception("Account is locked due to multiple failed login attempts. Contact Super Admin."))
        }

        val isValid = SecurityUtils.verifyPassword(password, user.passwordSalt, user.passwordHash)
        if (!isValid) {
            val updatedUser = user.copy(failedLoginAttempts = user.failedLoginAttempts + 1)
            val index = usersTable.indexOf(user)
            if (updatedUser.failedLoginAttempts >= 5) {
                usersTable[index] = updatedUser.copy(isLocked = true)
                return Result.failure(Exception("Account locked: 5 consecutive failed login attempts."))
            } else {
                usersTable[index] = updatedUser
                val attemptsLeft = 5 - updatedUser.failedLoginAttempts
                return Result.failure(Exception("Invalid password. $attemptsLeft attempts remaining before lock."))
            }
        }

        // Reset failed login attempts and update lastLogin
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val nowStr = sdf.format(Date())
        val loggedInUser = user.copy(failedLoginAttempts = 0, lastLoginAt = nowStr)
        val index = usersTable.indexOf(user)
        usersTable[index] = loggedInUser

        val token = SecurityUtils.generateSessionToken()
        val session = AuthSession(token = token, user = loggedInUser)
        sessionsTable[token] = session

        return Result.success(session)
    }

    override suspend fun logout(token: String): Boolean {
        delay(50)
        return sessionsTable.remove(token) != null
    }

    override suspend fun requestPasswordReset(email: String): Result<String> {
        delay(100)
        val user = usersTable.find { it.email.equals(email.trim(), ignoreCase = true) }
            ?: return Result.failure(Exception("No account registered with this email address."))

        val otp = SecurityUtils.generateOtpToken()
        val expiry = System.currentTimeMillis() + (15 * 60 * 1000) // 15-minute OTP
        resetRequests.removeAll { it.email.equals(email, ignoreCase = true) }
        resetRequests.add(PasswordResetRequest(email = user.email, otpToken = otp, expiresAt = expiry))

        return Result.success(otp)
    }

    override suspend fun verifyResetOtp(email: String, otp: String): Result<Boolean> {
        delay(60)
        val req = resetRequests.find { it.email.equals(email.trim(), ignoreCase = true) }
            ?: return Result.failure(Exception("No reset request found for this email."))

        if (System.currentTimeMillis() > req.expiresAt) {
            resetRequests.remove(req)
            return Result.failure(Exception("Reset OTP has expired. Please request a new one."))
        }

        if (req.otpToken != otp.trim()) {
            return Result.failure(Exception("Invalid 6-digit OTP code."))
        }

        val idx = resetRequests.indexOf(req)
        resetRequests[idx] = req.copy(isVerified = true)
        return Result.success(true)
    }

    override suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Boolean> {
        delay(120)
        if (newPassword.length < 8) {
            return Result.failure(Exception("Password must be at least 8 characters long."))
        }

        val req = resetRequests.find { it.email.equals(email.trim(), ignoreCase = true) }
            ?: return Result.failure(Exception("Invalid reset session."))

        if (!req.isVerified && req.otpToken != otp.trim()) {
            return Result.failure(Exception("Unverified reset attempt."))
        }

        val user = usersTable.find { it.email.equals(email.trim(), ignoreCase = true) }
            ?: return Result.failure(Exception("User not found."))

        val newSalt = SecurityUtils.generateSalt()
        val newHash = SecurityUtils.hashPassword(newPassword, newSalt)
        val updatedUser = user.copy(
            passwordSalt = newSalt,
            passwordHash = newHash,
            failedLoginAttempts = 0,
            isLocked = false
        )

        val idx = usersTable.indexOf(user)
        usersTable[idx] = updatedUser
        resetRequests.remove(req)

        return Result.success(true)
    }

    override suspend fun validateSession(token: String): AuthSession? {
        val session = sessionsTable[token] ?: return null
        if (session.isExpired) {
            sessionsTable.remove(token)
            return null
        }
        return session
    }

    override suspend fun registerEmployee(
        adminToken: String,
        fullName: String,
        email: String,
        phone: String,
        role: UserRole,
        branch: String,
        initialPassword: String
    ): Result<UserAccount> {
        val session = validateSession(adminToken)
            ?: return Result.failure(Exception("Unauthorized: Valid admin session token required."))

        if (session.user.role != UserRole.SUPER_ADMIN && session.user.role != UserRole.ADMIN) {
            return Result.failure(Exception("Forbidden: Only Super Admin and Admin can register new employees."))
        }

        if (usersTable.any { it.email.equals(email.trim(), ignoreCase = true) }) {
            return Result.failure(Exception("An employee is already registered with email $email."))
        }

        val prefix = when (role) {
            UserRole.SUPER_ADMIN -> "EMP-SA"
            UserRole.ADMIN -> "EMP-AD"
            UserRole.FINANCE_MANAGER -> "EMP-FM"
            UserRole.COLLECTION_SUPERVISOR -> "EMP-CS"
            UserRole.COLLECTION_EMPLOYEE -> "EMP-CE"
        }
        val count = usersTable.count { it.role == role } + 1
        val employeeId = "%s-%03d".format(prefix, count)

        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(initialPassword, salt)
        val newAccount = UserAccount(
            employeeId = employeeId,
            fullName = fullName.trim(),
            email = email.trim(),
            phone = phone.trim(),
            role = role,
            branch = branch.trim(),
            passwordSalt = salt,
            passwordHash = hash
        )

        usersTable.add(newAccount)
        return Result.success(newAccount)
    }

    override suspend fun getRegisteredDemoAccounts(): List<Pair<String, String>> {
        return listOf(
            "EMP-SA-001 (Super Admin)" to "superadmin@financepro.com",
            "EMP-AD-002 (Administrator)" to "admin@financepro.com",
            "EMP-FM-003 (Finance Manager)" to "finance@financepro.com",
            "EMP-CS-004 (Collection Supervisor)" to "supervisor@financepro.com",
            "EMP-CE-005 (Collection Employee)" to "collector@financepro.com"
        )
    }
}

package com.example

import com.example.data.models.*
import com.example.data.remote.MockAuthBackendService
import com.example.data.remote.MockFinanceBackendService
import com.example.data.security.SecurityUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testCurrencyFormatting() {
        val amount = 25000.0
        val formatted = amount.toCurrencyString()
        assertTrue(formatted.contains("25,000"))
    }

    @Test
    fun testPasswordHashingSecurity() {
        val password = "MySecretPassword123!"
        val salt1 = SecurityUtils.generateSalt()
        val salt2 = SecurityUtils.generateSalt()

        // Salts must be unique
        assertNotEquals(salt1, salt2)

        val hash1 = SecurityUtils.hashPassword(password, salt1)
        val hash2 = SecurityUtils.hashPassword(password, salt2)

        // Same password with different salts produces completely different hashes
        assertNotEquals(hash1, hash2)

        // Plain text must NEVER equal hash
        assertNotEquals(password, hash1)

        // Verification must succeed with correct password & salt
        assertTrue(SecurityUtils.verifyPassword(password, salt1, hash1))

        // Verification must fail with wrong password
        assertFalse(SecurityUtils.verifyPassword("WrongPassword", salt1, hash1))
    }

    @Test
    fun testAuthenticationWorkflow() = runBlocking {
        val authService = MockAuthBackendService()

        // Test login with valid employee ID
        val loginResult = authService.login("EMP-SA-001", "Password@123")
        assertTrue(loginResult.isSuccess)
        val session = loginResult.getOrThrow()
        assertEquals(UserRole.SUPER_ADMIN, session.user.role)
        assertEquals("Arthur Pendelton", session.user.fullName)
        assertTrue(session.token.startsWith("fpsess_"))

        // Test login with valid corporate email
        val loginEmailResult = authService.login("collector@financepro.com", "Password@123")
        assertTrue(loginEmailResult.isSuccess)
        val collectorSession = loginEmailResult.getOrThrow()
        assertEquals(UserRole.COLLECTION_EMPLOYEE, collectorSession.user.role)
        assertEquals("EMP-CE-005", collectorSession.user.employeeId)

        // Test login with invalid password
        val failResult = authService.login("EMP-SA-001", "WrongPassword")
        assertTrue(failResult.isFailure)

        // Test logout
        val logoutSuccess = authService.logout(session.token)
        assertTrue(logoutSuccess)
        assertNull(authService.validateSession(session.token))
    }

    @Test
    fun testForgotPasswordResetWorkflow() = runBlocking {
        val authService = MockAuthBackendService()
        val email = "finance@financepro.com"

        // 1. Request OTP
        val otpResult = authService.requestPasswordReset(email)
        assertTrue(otpResult.isSuccess)
        val otp = otpResult.getOrThrow()
        assertEquals(6, otp.length)

        // 2. Verify wrong OTP
        val wrongOtpResult = authService.verifyResetOtp(email, "000000")
        assertTrue(wrongOtpResult.isFailure)

        // 3. Verify correct OTP
        val verifyResult = authService.verifyResetOtp(email, otp)
        assertTrue(verifyResult.isSuccess)

        // 4. Reset password
        val resetResult = authService.resetPassword(email, otp, "NewSecurePass#99")
        assertTrue(resetResult.isSuccess)

        // 5. Old password must fail now
        val oldLogin = authService.login(email, "Password@123")
        assertTrue(oldLogin.isFailure)

        // 6. New password must succeed
        val newLogin = authService.login(email, "NewSecurePass#99")
        assertTrue(newLogin.isSuccess)
    }

    @Test
    fun testGranularRoleBasedPermissionsMatrix() {
        val matrix = DefaultPermissions.getInitialPermissionMatrix()

        // 1. Super Admin: Full application control (ALL 5 actions across ALL modules)
        AppModule.values().forEach { module ->
            val actions = matrix[UserRole.SUPER_ADMIN to module] ?: emptySet()
            assertEquals(
                setOf(
                    PermissionAction.VIEW,
                    PermissionAction.CREATE,
                    PermissionAction.UPDATE,
                    PermissionAction.APPROVE,
                    PermissionAction.REVERSE
                ),
                actions
            )
        }

        // 2. Admin: Manage customers, loans, employees, assignments and reports
        val adminCustomerActions = matrix[UserRole.ADMIN to AppModule.CUSTOMER_MANAGEMENT] ?: emptySet()
        assertTrue(adminCustomerActions.contains(PermissionAction.VIEW))
        assertTrue(adminCustomerActions.contains(PermissionAction.CREATE))
        assertTrue(adminCustomerActions.contains(PermissionAction.UPDATE))

        val adminLoanActions = matrix[UserRole.ADMIN to AppModule.LOAN_MANAGEMENT] ?: emptySet()
        assertTrue(adminLoanActions.contains(PermissionAction.VIEW))
        assertTrue(adminLoanActions.contains(PermissionAction.CREATE))
        assertTrue(adminLoanActions.contains(PermissionAction.UPDATE))

        val adminAssignmentActions = matrix[UserRole.ADMIN to AppModule.EMPLOYEE_ASSIGNMENTS] ?: emptySet()
        assertTrue(adminAssignmentActions.contains(PermissionAction.CREATE))
        assertTrue(adminAssignmentActions.contains(PermissionAction.UPDATE))

        // 3. Finance Manager: Manage loan schedules, payments, due amounts and financial verification
        val fmVerificationActions = matrix[UserRole.FINANCE_MANAGER to AppModule.PAYMENT_VERIFICATION] ?: emptySet()
        assertTrue(fmVerificationActions.contains(PermissionAction.APPROVE))
        assertTrue(fmVerificationActions.contains(PermissionAction.REVERSE))

        val fmEmiActions = matrix[UserRole.FINANCE_MANAGER to AppModule.EMI_MANAGEMENT] ?: emptySet()
        assertTrue(fmEmiActions.contains(PermissionAction.VIEW))
        assertTrue(fmEmiActions.contains(PermissionAction.CREATE))
        assertTrue(fmEmiActions.contains(PermissionAction.UPDATE))

        // 4. Collection Supervisor: View assigned team, allocate collection tasks and monitor employees
        val supAssignmentActions = matrix[UserRole.COLLECTION_SUPERVISOR to AppModule.EMPLOYEE_ASSIGNMENTS] ?: emptySet()
        assertTrue(supAssignmentActions.contains(PermissionAction.CREATE))
        assertTrue(supAssignmentActions.contains(PermissionAction.UPDATE))

        // 5. Collection Employee: View only assigned customers and submit collection transactions
        val collectorCollectionActions = matrix[UserRole.COLLECTION_EMPLOYEE to AppModule.LOAN_COLLECTION] ?: emptySet()
        assertTrue(collectorCollectionActions.contains(PermissionAction.VIEW))
        assertTrue(collectorCollectionActions.contains(PermissionAction.CREATE))

        // Collector must NOT have APPROVE, REVERSE, or loan origination CREATE
        assertFalse(collectorCollectionActions.contains(PermissionAction.APPROVE))
        assertFalse(collectorCollectionActions.contains(PermissionAction.REVERSE))
        val collectorLoanActions = matrix[UserRole.COLLECTION_EMPLOYEE to AppModule.LOAN_MANAGEMENT] ?: emptySet()
        assertFalse(collectorLoanActions.contains(PermissionAction.CREATE))
    }

    @Test
    fun testBackendApiPermissionEnforcementAndAuditLogs() = runBlocking {
        val authService = MockAuthBackendService()
        val backendService = MockFinanceBackendService()

        // 1. Log in as Collection Employee
        val collectorSession = authService.login("EMP-CE-005", "Password@123").getOrThrow()

        // 2. Collector attempts to verify/approve payment -> Must be REJECTED at Backend API
        val verifyAttempt = backendService.verifyPayment(
            receiptId = "COL-703",
            approved = true,
            verifiedBy = collectorSession.user.fullName,
            note = "Unauthorized approval attempt",
            callerSession = collectorSession
        )
        assertTrue(verifyAttempt.isFailure)
        assertTrue(verifyAttempt.exceptionOrNull() is SecurityException)

        // 3. Verify that an Access Denied audit log entry was generated
        val auditLogsAfterBlock = backendService.getAuditLogs()
        val blockedEntry = auditLogsAfterBlock.find { it.status == AuditStatus.DENIED && it.actorId == "EMP-CE-005" }
        assertNotNull(blockedEntry)
        assertEquals(PermissionAction.APPROVE, blockedEntry?.action)

        // 4. Log in as Finance Manager
        val fmSession = authService.login("EMP-FM-003", "Password@123").getOrThrow()

        // 5. Finance Manager reverses payment -> Must SUCCEED and restore balance
        val reverseAttempt = backendService.reversePayment(
            receiptId = "COL-701",
            reason = "Duplicate bank statement deposit detected",
            callerSession = fmSession
        )
        assertTrue(reverseAttempt.isSuccess)
        val reversedRecord = reverseAttempt.getOrThrow()
        assertEquals(ReceiptStatus.REVERSED, reversedRecord.status)
        assertTrue(reversedRecord.auditNote.contains("REVERSED"))

        // 6. Verify reversal audit log entry
        val auditLogsAfterReverse = backendService.getAuditLogs()
        val reversalEntry = auditLogsAfterReverse.find { it.status == AuditStatus.REVERSED && it.action == PermissionAction.REVERSE }
        assertNotNull(reversalEntry)
        assertEquals("EMP-FM-003", reversalEntry?.actorId)
    }

    @Test
    fun testCollectionEmployeeCustomerPortfolioFiltering() = runBlocking {
        val backendService = MockFinanceBackendService()

        // All customers count for Admin
        val allCustomers = backendService.getCustomers(callerRole = UserRole.ADMIN)
        assertTrue(allCustomers.size >= 5)

        // Collection Employee EMP-CE-005 must only receive assigned customers (Alexander Wright, Sarah Jenkins, Marcus Vance)
        val collectorCustomers = backendService.getCustomers(
            callerRole = UserRole.COLLECTION_EMPLOYEE,
            callerEmployeeId = "EMP-CE-005"
        )
        assertTrue(collectorCustomers.isNotEmpty())
        assertTrue(collectorCustomers.size < allCustomers.size)
        assertTrue(collectorCustomers.all { it.id in setOf("CUST-1001", "CUST-1002", "CUST-1003") })
    }

    @Test
    fun testDynamicPermissionModificationAndSuperAdminAudit() = runBlocking {
        val authService = MockAuthBackendService()
        val backendService = MockFinanceBackendService()

        val superAdminSession = authService.login("EMP-SA-001", "Password@123").getOrThrow()
        val collectorSession = authService.login("EMP-CE-005", "Password@123").getOrThrow()

        // Non-admin attempting to alter permission policy must be rejected
        val unauthorizedUpdate = backendService.updatePermission(
            role = UserRole.COLLECTION_EMPLOYEE,
            module = AppModule.PAYMENT_VERIFICATION,
            action = PermissionAction.APPROVE,
            enabled = true,
            callerSession = collectorSession
        )
        assertTrue(unauthorizedUpdate.isFailure)

        // Super Admin altering permission succeeds
        val authorizedUpdate = backendService.updatePermission(
            role = UserRole.COLLECTION_EMPLOYEE,
            module = AppModule.REPORTS,
            action = PermissionAction.VIEW,
            enabled = true,
            callerSession = superAdminSession
        )
        assertTrue(authorizedUpdate.isSuccess)

        // Verify updated permission
        val matrix = backendService.getPermissionsMatrix()
        val collectorReportActions = matrix[UserRole.COLLECTION_EMPLOYEE to AppModule.REPORTS] ?: emptySet()
        assertTrue(collectorReportActions.contains(PermissionAction.VIEW))

        // Reset to defaults
        val resetResult = backendService.resetPermissionsToDefaults(superAdminSession)
        assertTrue(resetResult.isSuccess)
    }
}

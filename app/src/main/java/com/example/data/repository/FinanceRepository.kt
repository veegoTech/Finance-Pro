package com.example.data.repository

import com.example.data.models.*
import com.example.data.remote.AuthService
import com.example.data.remote.FinanceApiService
import com.example.data.remote.MockAuthBackendService
import com.example.data.remote.MockFinanceBackendService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FinanceRepository(
    private val apiService: FinanceApiService = MockFinanceBackendService(),
    private val authService: AuthService = MockAuthBackendService()
) {
    private val _currentSession = MutableStateFlow<AuthSession?>(null)
    val currentSession: StateFlow<AuthSession?> = _currentSession.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _currentModule = MutableStateFlow(AppModule.DASHBOARD)
    val currentModule: StateFlow<AppModule> = _currentModule.asStateFlow()

    private val _dashboardMetrics = MutableStateFlow<DashboardMetrics?>(null)
    val dashboardMetrics: StateFlow<DashboardMetrics?> = _dashboardMetrics.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(emptyList())
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _loans = MutableStateFlow<List<Loan>>(emptyList())
    val loans: StateFlow<List<Loan>> = _loans.asStateFlow()

    private val _emis = MutableStateFlow<List<EMIItem>>(emptyList())
    val emis: StateFlow<List<EMIItem>> = _emis.asStateFlow()

    private val _pendingDues = MutableStateFlow<List<Assignment>>(emptyList())
    val pendingDues: StateFlow<List<Assignment>> = _pendingDues.asStateFlow()

    private val _agingBuckets = MutableStateFlow<List<AgingBucket>>(emptyList())
    val agingBuckets: StateFlow<List<AgingBucket>> = _agingBuckets.asStateFlow()

    private val _employees = MutableStateFlow<List<Employee>>(emptyList())
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    private val _assignments = MutableStateFlow<List<Assignment>>(emptyList())
    val assignments: StateFlow<List<Assignment>> = _assignments.asStateFlow()

    private val _collections = MutableStateFlow<List<CollectionRecord>>(emptyList())
    val collections: StateFlow<List<CollectionRecord>> = _collections.asStateFlow()

    private val _branchPerformances = MutableStateFlow<List<BranchPerformance>>(emptyList())
    val branchPerformances: StateFlow<List<BranchPerformance>> = _branchPerformances.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _permissionsMatrix = MutableStateFlow<Map<Pair<UserRole, AppModule>, Set<PermissionAction>>>(emptyMap())
    val permissionsMatrix: StateFlow<Map<Pair<UserRole, AppModule>, Set<PermissionAction>>> = _permissionsMatrix.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    private val _architectureStatus = MutableStateFlow<Map<String, String>>(emptyMap())
    val architectureStatus: StateFlow<Map<String, String>> = _architectureStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun login(identifier: String, password: String): Result<AuthSession> {
        _isLoading.value = true
        return try {
            val res = authService.login(identifier, password)
            res.onSuccess { session ->
                _currentSession.value = session
                _currentRole.value = session.user.role
                _currentModule.value = AppModule.DASHBOARD

                // Log authentication audit event
                apiService.logAuditEvent(
                    AuditLogEntry(
                        id = "AUD-${System.currentTimeMillis()}",
                        actorId = session.user.employeeId,
                        actorName = session.user.fullName,
                        actorRole = session.user.role,
                        action = PermissionAction.VIEW,
                        module = AppModule.DASHBOARD,
                        entityType = "UserSession",
                        entityId = session.token.take(12),
                        details = "User signed in successfully with role '${session.user.role.badgeLabel}'",
                        status = AuditStatus.SUCCESS
                    )
                )

                refreshAll()
            }
            res
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun logout(): Boolean {
        val session = _currentSession.value
        val token = session?.token
        if (token != null) {
            authService.logout(token)
            apiService.logAuditEvent(
                AuditLogEntry(
                    id = "AUD-${System.currentTimeMillis()}",
                    actorId = session.user.employeeId,
                    actorName = session.user.fullName,
                    actorRole = session.user.role,
                    action = PermissionAction.VIEW,
                    module = AppModule.DASHBOARD,
                    entityType = "UserSession",
                    entityId = session.token.take(12),
                    details = "User logged out. Session invalidated.",
                    status = AuditStatus.SUCCESS
                )
            )
        }
        _currentSession.value = null
        _currentModule.value = AppModule.DASHBOARD
        return true
    }

    suspend fun requestPasswordReset(email: String): Result<String> {
        return authService.requestPasswordReset(email)
    }

    suspend fun verifyResetOtp(email: String, otp: String): Result<Boolean> {
        return authService.verifyResetOtp(email, otp)
    }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Boolean> {
        return authService.resetPassword(email, otp, newPassword)
    }

    suspend fun getDemoCredentials(): List<Pair<String, String>> {
        return authService.getRegisteredDemoAccounts()
    }

    /**
     * Checks if current user can access the given module (at least VIEW permission).
     */
    fun canAccess(module: AppModule): Boolean {
        val role = _currentSession.value?.user?.role ?: return false
        val actions = _permissionsMatrix.value[role to module] ?: emptySet()
        return actions.contains(PermissionAction.VIEW)
    }

    /**
     * Granular action permission check for the logged-in user.
     */
    fun canPerform(module: AppModule, action: PermissionAction): Boolean {
        val role = _currentSession.value?.user?.role ?: return false
        val actions = _permissionsMatrix.value[role to module] ?: emptySet()
        return actions.contains(action)
    }

    /**
     * Granular action permission check for any specific role.
     */
    fun canPerformForRole(role: UserRole, module: AppModule, action: PermissionAction): Boolean {
        val actions = _permissionsMatrix.value[role to module] ?: emptySet()
        return actions.contains(action)
    }

    fun setRole(role: UserRole) {
        _currentRole.value = role
        if (!canAccess(_currentModule.value)) {
            _currentModule.value = AppModule.DASHBOARD
        }
    }

    fun setModule(module: AppModule) {
        if (canAccess(module)) {
            _currentModule.value = module
        } else {
            _currentModule.value = AppModule.DASHBOARD
        }
    }

    suspend fun refreshAll() {
        val session = _currentSession.value ?: return
        _isLoading.value = true
        try {
            _dashboardMetrics.value = apiService.getDashboardMetrics(session.user.role)
            _customers.value = apiService.getCustomers(
                searchQuery = "",
                callerRole = session.user.role,
                callerEmployeeId = session.user.employeeId
            )
            _loans.value = apiService.getLoans()
            _emis.value = apiService.getEMISchedule()
            _pendingDues.value = apiService.getPendingDues()
            _agingBuckets.value = apiService.getAgingBuckets()
            _employees.value = apiService.getEmployees()
            _assignments.value = apiService.getAssignments(
                employeeId = if (session.user.role == UserRole.COLLECTION_EMPLOYEE) session.user.employeeId else null
            )
            _collections.value = apiService.getCollections()
            _branchPerformances.value = apiService.getBranchPerformances()
            _notifications.value = apiService.getNotifications()
            _permissionsMatrix.value = apiService.getPermissionsMatrix()
            _auditLogs.value = apiService.getAuditLogs()
            _architectureStatus.value = apiService.getArchitectureStatus()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun searchCustomers(query: String) {
        val session = _currentSession.value ?: return
        _customers.value = apiService.getCustomers(
            searchQuery = query,
            callerRole = session.user.role,
            callerEmployeeId = session.user.employeeId
        )
    }

    suspend fun recordCollection(
        customerId: String,
        loanId: String,
        amount: Double,
        mode: PaymentMode,
        collectedBy: String,
        note: String
    ): Result<CollectionRecord> {
        val session = _currentSession.value
            ?: return Result.failure(SecurityException("Unauthorized: Please log in to record collections."))

        val result = apiService.recordCollection(
            customerId = customerId,
            loanId = loanId,
            amount = amount,
            mode = mode,
            collectedBy = collectedBy,
            note = note,
            callerSession = session
        )
        if (result.isSuccess) {
            refreshAll()
        }
        return result
    }

    suspend fun verifyPayment(
        receiptId: String,
        approved: Boolean,
        verifiedBy: String,
        note: String
    ): Result<CollectionRecord> {
        val session = _currentSession.value
            ?: return Result.failure(SecurityException("Unauthorized: Please log in."))

        val result = apiService.verifyPayment(
            receiptId = receiptId,
            approved = approved,
            verifiedBy = verifiedBy,
            note = note,
            callerSession = session
        )
        if (result.isSuccess) {
            refreshAll()
        }
        return result
    }

    suspend fun reversePayment(
        receiptId: String,
        reason: String
    ): Result<CollectionRecord> {
        val session = _currentSession.value
            ?: return Result.failure(SecurityException("Unauthorized: Please log in to reverse transactions."))

        val result = apiService.reversePayment(
            receiptId = receiptId,
            reason = reason,
            callerSession = session
        )
        if (result.isSuccess) {
            refreshAll()
        }
        return result
    }

    suspend fun updatePermission(
        role: UserRole,
        module: AppModule,
        action: PermissionAction,
        enabled: Boolean
    ): Result<Boolean> {
        val session = _currentSession.value
            ?: return Result.failure(SecurityException("Unauthorized: Please log in."))

        val result = apiService.updatePermission(
            role = role,
            module = module,
            action = action,
            enabled = enabled,
            callerSession = session
        )
        if (result.isSuccess) {
            _permissionsMatrix.value = apiService.getPermissionsMatrix()
            _auditLogs.value = apiService.getAuditLogs()
        }
        return result
    }

    suspend fun resetPermissionsToDefaults(): Result<Boolean> {
        val session = _currentSession.value
            ?: return Result.failure(SecurityException("Unauthorized: Please log in."))

        val result = apiService.resetPermissionsToDefaults(session)
        if (result.isSuccess) {
            _permissionsMatrix.value = apiService.getPermissionsMatrix()
            _auditLogs.value = apiService.getAuditLogs()
        }
        return result
    }

    suspend fun markNotificationAsRead(id: String) {
        if (apiService.markNotificationRead(id)) {
            _notifications.value = apiService.getNotifications()
        }
    }
}

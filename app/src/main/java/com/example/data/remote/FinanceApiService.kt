package com.example.data.remote

import com.example.data.models.*

interface FinanceApiService {
    suspend fun getDashboardMetrics(role: UserRole): DashboardMetrics
    suspend fun getCustomers(
        searchQuery: String = "",
        callerRole: UserRole? = null,
        callerEmployeeId: String? = null
    ): List<Customer>
    suspend fun getLoans(filterStatus: LoanStatus? = null): List<Loan>
    suspend fun getEMISchedule(loanId: String? = null): List<EMIItem>
    suspend fun getPendingDues(): List<Assignment>
    suspend fun getAgingBuckets(): List<AgingBucket>
    suspend fun getEmployees(): List<Employee>
    suspend fun getAssignments(employeeId: String? = null): List<Assignment>
    suspend fun getCollections(): List<CollectionRecord>
    suspend fun recordCollection(
        customerId: String,
        loanId: String,
        amount: Double,
        mode: PaymentMode,
        collectedBy: String,
        note: String,
        callerSession: AuthSession? = null
    ): Result<CollectionRecord>
    suspend fun verifyPayment(
        receiptId: String,
        approved: Boolean,
        verifiedBy: String,
        note: String,
        callerSession: AuthSession? = null
    ): Result<CollectionRecord>
    suspend fun reversePayment(
        receiptId: String,
        reason: String,
        callerSession: AuthSession? = null
    ): Result<CollectionRecord>
    suspend fun allocateAssignment(
        customerId: String,
        loanId: String,
        employeeId: String,
        dueAmount: Double,
        callerSession: AuthSession? = null
    ): Result<Assignment>
    suspend fun getPermissionsMatrix(): Map<Pair<UserRole, AppModule>, Set<PermissionAction>>
    suspend fun updatePermission(
        role: UserRole,
        module: AppModule,
        action: PermissionAction,
        enabled: Boolean,
        callerSession: AuthSession? = null
    ): Result<Boolean>
    suspend fun resetPermissionsToDefaults(callerSession: AuthSession? = null): Result<Boolean>
    suspend fun getAuditLogs(): List<AuditLogEntry>
    suspend fun logAuditEvent(entry: AuditLogEntry): Boolean
    suspend fun getBranchPerformances(): List<BranchPerformance>
    suspend fun getNotifications(): List<NotificationItem>
    suspend fun markNotificationRead(id: String): Boolean
    suspend fun getArchitectureStatus(): Map<String, String>
}

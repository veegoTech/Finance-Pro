package com.example.data.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Granular action permissions for role-based access control.
 */
enum class PermissionAction(
    val key: String,
    val label: String,
    val description: String,
    val iconName: String
) {
    VIEW(
        key = "view",
        label = "View & Read",
        description = "Read records, view details, dashboards, and audit summaries",
        iconName = "visibility"
    ),
    CREATE(
        key = "create",
        label = "Create & Add",
        description = "Originate loans, register customers, allocate tasks, or record collections",
        iconName = "add_circle"
    ),
    UPDATE(
        key = "update",
        label = "Edit & Update",
        description = "Modify customer profiles, reschedule EMIs, reassign tasks, or edit staff",
        iconName = "edit"
    ),
    APPROVE(
        key = "approve",
        label = "Authorize & Approve",
        description = "Verify field collection receipts, sign off on disbursements, and clear audits",
        iconName = "verified"
    ),
    REVERSE(
        key = "reverse",
        label = "Void & Reverse",
        description = "Reverse verified payments, void erroneous collection receipts, and undo allocations",
        iconName = "undo"
    );

    companion object {
        fun fromKey(key: String): PermissionAction? = values().find { it.key.equals(key, ignoreCase = true) }
    }
}

enum class AuditStatus(val label: String) {
    SUCCESS("Success"),
    DENIED("Access Denied"),
    REVERSED("Reversed"),
    FLAGGED("Security Flag")
}

data class AuditLogEntry(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val formattedTime: String = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US).format(Date(timestamp)),
    val actorId: String,
    val actorName: String,
    val actorRole: UserRole,
    val action: PermissionAction,
    val module: AppModule,
    val entityType: String,
    val entityId: String,
    val details: String,
    val status: AuditStatus = AuditStatus.SUCCESS,
    val ipAddress: String = "10.0.2.16 (Secure Enterprise Gateway)"
)

/**
 * Default granular role-based permissions matrix tailored to user roles.
 */
object DefaultPermissions {

    fun getInitialPermissionMatrix(): MutableMap<Pair<UserRole, AppModule>, MutableSet<PermissionAction>> {
        val matrix = mutableMapOf<Pair<UserRole, AppModule>, MutableSet<PermissionAction>>()

        // Super Admin: Full application control across all modules
        for (module in AppModule.values()) {
            matrix[UserRole.SUPER_ADMIN to module] = mutableSetOf(
                PermissionAction.VIEW,
                PermissionAction.CREATE,
                PermissionAction.UPDATE,
                PermissionAction.APPROVE,
                PermissionAction.REVERSE
            )
        }

        // Admin: Manage customers, loans, employees, assignments and reports
        matrix[UserRole.ADMIN to AppModule.DASHBOARD] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.ADMIN to AppModule.CUSTOMER_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW, PermissionAction.CREATE, PermissionAction.UPDATE)
        matrix[UserRole.ADMIN to AppModule.LOAN_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW, PermissionAction.CREATE, PermissionAction.UPDATE)
        matrix[UserRole.ADMIN to AppModule.EMI_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.ADMIN to AppModule.PENDING_DUES] = mutableSetOf(PermissionAction.VIEW, PermissionAction.UPDATE)
        matrix[UserRole.ADMIN to AppModule.EMPLOYEE_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW, PermissionAction.CREATE, PermissionAction.UPDATE)
        matrix[UserRole.ADMIN to AppModule.EMPLOYEE_ASSIGNMENTS] = mutableSetOf(PermissionAction.VIEW, PermissionAction.CREATE, PermissionAction.UPDATE)
        matrix[UserRole.ADMIN to AppModule.LOAN_COLLECTION] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.ADMIN to AppModule.PAYMENT_VERIFICATION] = mutableSetOf(PermissionAction.VIEW, PermissionAction.APPROVE)
        matrix[UserRole.ADMIN to AppModule.REPORTS] = mutableSetOf(PermissionAction.VIEW, PermissionAction.CREATE)
        matrix[UserRole.ADMIN to AppModule.NOTIFICATIONS] = mutableSetOf(PermissionAction.VIEW, PermissionAction.UPDATE)
        matrix[UserRole.ADMIN to AppModule.SETTINGS] = mutableSetOf(PermissionAction.VIEW, PermissionAction.UPDATE)

        // Finance Manager: Manage loan schedules, payments, due amounts and financial verification
        matrix[UserRole.FINANCE_MANAGER to AppModule.DASHBOARD] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.FINANCE_MANAGER to AppModule.CUSTOMER_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.FINANCE_MANAGER to AppModule.LOAN_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW, PermissionAction.UPDATE)
        matrix[UserRole.FINANCE_MANAGER to AppModule.EMI_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW, PermissionAction.CREATE, PermissionAction.UPDATE)
        matrix[UserRole.FINANCE_MANAGER to AppModule.PENDING_DUES] = mutableSetOf(PermissionAction.VIEW, PermissionAction.UPDATE)
        matrix[UserRole.FINANCE_MANAGER to AppModule.EMPLOYEE_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.FINANCE_MANAGER to AppModule.EMPLOYEE_ASSIGNMENTS] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.FINANCE_MANAGER to AppModule.LOAN_COLLECTION] = mutableSetOf(PermissionAction.VIEW, PermissionAction.REVERSE)
        matrix[UserRole.FINANCE_MANAGER to AppModule.PAYMENT_VERIFICATION] = mutableSetOf(PermissionAction.VIEW, PermissionAction.APPROVE, PermissionAction.REVERSE)
        matrix[UserRole.FINANCE_MANAGER to AppModule.REPORTS] = mutableSetOf(PermissionAction.VIEW, PermissionAction.CREATE)
        matrix[UserRole.FINANCE_MANAGER to AppModule.NOTIFICATIONS] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.FINANCE_MANAGER to AppModule.SETTINGS] = mutableSetOf(PermissionAction.VIEW)

        // Collection Supervisor: View assigned team, allocate collection tasks and monitor employees
        matrix[UserRole.COLLECTION_SUPERVISOR to AppModule.DASHBOARD] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_SUPERVISOR to AppModule.CUSTOMER_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_SUPERVISOR to AppModule.LOAN_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_SUPERVISOR to AppModule.EMI_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_SUPERVISOR to AppModule.PENDING_DUES] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_SUPERVISOR to AppModule.EMPLOYEE_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_SUPERVISOR to AppModule.EMPLOYEE_ASSIGNMENTS] = mutableSetOf(PermissionAction.VIEW, PermissionAction.CREATE, PermissionAction.UPDATE)
        matrix[UserRole.COLLECTION_SUPERVISOR to AppModule.LOAN_COLLECTION] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_SUPERVISOR to AppModule.PAYMENT_VERIFICATION] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_SUPERVISOR to AppModule.REPORTS] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_SUPERVISOR to AppModule.NOTIFICATIONS] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_SUPERVISOR to AppModule.SETTINGS] = mutableSetOf(PermissionAction.VIEW)

        // Collection Employee: View only assigned customers and submit collection transactions
        matrix[UserRole.COLLECTION_EMPLOYEE to AppModule.DASHBOARD] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_EMPLOYEE to AppModule.CUSTOMER_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_EMPLOYEE to AppModule.EMPLOYEE_ASSIGNMENTS] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_EMPLOYEE to AppModule.LOAN_COLLECTION] = mutableSetOf(PermissionAction.VIEW, PermissionAction.CREATE)
        matrix[UserRole.COLLECTION_EMPLOYEE to AppModule.EMI_MANAGEMENT] = mutableSetOf(PermissionAction.VIEW)
        matrix[UserRole.COLLECTION_EMPLOYEE to AppModule.NOTIFICATIONS] = mutableSetOf(PermissionAction.VIEW)

        return matrix
    }

    fun getInitialAuditLogs(): List<AuditLogEntry> {
        val now = System.currentTimeMillis()
        val oneHour = 3600000L
        return listOf(
            AuditLogEntry(
                id = "AUD-1008",
                timestamp = now - (15 * 60000L),
                actorId = "EMP-SA-001",
                actorName = "Arthur Pendelton",
                actorRole = UserRole.SUPER_ADMIN,
                action = PermissionAction.UPDATE,
                module = AppModule.SETTINGS,
                entityType = "PermissionPolicy",
                entityId = "POL-ENTERPRISE-V2",
                details = "Configured granular RBAC enforcement across 5 roles and 12 modules.",
                status = AuditStatus.SUCCESS
            ),
            AuditLogEntry(
                id = "AUD-1007",
                timestamp = now - (45 * 60000L),
                actorId = "EMP-FM-003",
                actorName = "Evelyn Vance",
                actorRole = UserRole.FINANCE_MANAGER,
                action = PermissionAction.APPROVE,
                module = AppModule.PAYMENT_VERIFICATION,
                entityType = "PaymentReceipt",
                entityId = "RCP-2026-0891",
                details = "Audited and verified field cash collection of $ 850 from Marcus Aurelius.",
                status = AuditStatus.SUCCESS
            ),
            AuditLogEntry(
                id = "AUD-1006",
                timestamp = now - (90 * 60000L),
                actorId = "EMP-FM-003",
                actorName = "Evelyn Vance",
                actorRole = UserRole.FINANCE_MANAGER,
                action = PermissionAction.REVERSE,
                module = AppModule.PAYMENT_VERIFICATION,
                entityType = "PaymentReceipt",
                entityId = "RCP-2026-0888",
                details = "Reversed duplicate bank transfer receipt #RCP-2026-0888 ($ 1,200). Balance restored.",
                status = AuditStatus.REVERSED
            ),
            AuditLogEntry(
                id = "AUD-1005",
                timestamp = now - (2 * oneHour),
                actorId = "EMP-CS-004",
                actorName = "Marcus Zhao",
                actorRole = UserRole.COLLECTION_SUPERVISOR,
                action = PermissionAction.CREATE,
                module = AppModule.EMPLOYEE_ASSIGNMENTS,
                entityType = "Assignment",
                entityId = "ASN-2026-114",
                details = "Allocated high-priority overdue recovery task (LN-2026-402) to David Miller.",
                status = AuditStatus.SUCCESS
            ),
            AuditLogEntry(
                id = "AUD-1004",
                timestamp = now - (3 * oneHour),
                actorId = "EMP-AD-002",
                actorName = "Elena Rostova",
                actorRole = UserRole.ADMIN,
                action = PermissionAction.CREATE,
                module = AppModule.CUSTOMER_MANAGEMENT,
                entityType = "Customer",
                entityId = "CUST-1006",
                details = "Registered new SME commercial borrower (Sarah Jenkins, Austin TX).",
                status = AuditStatus.SUCCESS
            ),
            AuditLogEntry(
                id = "AUD-1003",
                timestamp = now - (4 * oneHour),
                actorId = "EMP-CE-005",
                actorName = "David Miller",
                actorRole = UserRole.COLLECTION_EMPLOYEE,
                action = PermissionAction.CREATE,
                module = AppModule.LOAN_COLLECTION,
                entityType = "PaymentReceipt",
                entityId = "RCP-2026-0894",
                details = "Submitted on-ground UPI collection of $ 750 for LN-2026-401.",
                status = AuditStatus.SUCCESS
            ),
            AuditLogEntry(
                id = "AUD-1002",
                timestamp = now - (5 * oneHour),
                actorId = "EMP-CE-005",
                actorName = "David Miller",
                actorRole = UserRole.COLLECTION_EMPLOYEE,
                action = PermissionAction.APPROVE,
                module = AppModule.PAYMENT_VERIFICATION,
                entityType = "PaymentReceipt",
                entityId = "RCP-2026-0890",
                details = "BLOCKED: Unauthorized attempt by Collection Employee to approve payment.",
                status = AuditStatus.DENIED
            ),
            AuditLogEntry(
                id = "AUD-1001",
                timestamp = now - (6 * oneHour),
                actorId = "EMP-SA-001",
                actorName = "Arthur Pendelton",
                actorRole = UserRole.SUPER_ADMIN,
                action = PermissionAction.VIEW,
                module = AppModule.DASHBOARD,
                entityType = "SystemSession",
                entityId = "SESS-SEC-BOOT",
                details = "Initialized system security session and verified multi-tenant RBAC policies.",
                status = AuditStatus.SUCCESS
            )
        )
    }
}

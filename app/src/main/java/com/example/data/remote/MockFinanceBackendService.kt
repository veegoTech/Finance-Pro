package com.example.data.remote

import com.example.data.models.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Simulates a scalable Node.js REST backend service backed by a relational PostgreSQL schema.
 * Implements granular role-based permissions, transactional security, payment reversal,
 * and automated audit logging.
 */
class MockFinanceBackendService : FinanceApiService {

    // Dynamic Permission Matrix
    private val permissionsMatrix = ConcurrentHashMap<Pair<UserRole, AppModule>, MutableSet<PermissionAction>>().apply {
        putAll(DefaultPermissions.getInitialPermissionMatrix())
    }

    // Privileged Audit Logs Table
    private val auditLogsTable = mutableListOf<AuditLogEntry>().apply {
        addAll(DefaultPermissions.getInitialAuditLogs())
    }

    // Simulated PostgreSQL Relational Tables
    private val customersTable = mutableListOf(
        Customer(
            id = "CUST-1001",
            name = "Alexander Wright",
            phone = "+1 (555) 234-5678",
            email = "alex.wright@apextech.io",
            address = "742 Evergreen Terrace, Sector 4, Springfield",
            nationalId = "SSN-XXX-4592",
            kycStatus = "Verified",
            creditScore = 745,
            activeLoansCount = 1,
            totalBorrowed = 45000.0,
            totalOutstanding = 28500.0
        ),
        Customer(
            id = "CUST-1002",
            name = "Sarah Jenkins",
            phone = "+1 (555) 876-5432",
            email = "sarah.j@retailhub.com",
            address = "1204 Pineview Boulevard, Suite 3B",
            nationalId = "SSN-XXX-8812",
            kycStatus = "Verified",
            creditScore = 690,
            activeLoansCount = 1,
            totalBorrowed = 25000.0,
            totalOutstanding = 12400.0
        ),
        Customer(
            id = "CUST-1003",
            name = "Marcus Aurelius Vance",
            phone = "+1 (555) 345-6789",
            email = "m.vance@logisticsflow.net",
            address = "88 Harbor Point Way, Bay View",
            nationalId = "SSN-XXX-1204",
            kycStatus = "Verified",
            creditScore = 780,
            activeLoansCount = 1,
            totalBorrowed = 75000.0,
            totalOutstanding = 58200.0
        ),
        Customer(
            id = "CUST-1004",
            name = "Elena Rostova",
            phone = "+1 (555) 901-2345",
            email = "elena.r@designcraft.org",
            address = "45 Elmhurst Circle, Apt 2",
            nationalId = "SSN-XXX-7731",
            kycStatus = "Under Review",
            creditScore = 610,
            activeLoansCount = 1,
            totalBorrowed = 15000.0,
            totalOutstanding = 6800.0
        ),
        Customer(
            id = "CUST-1005",
            name = "Derrick Campbell",
            phone = "+1 (555) 678-9012",
            email = "d.campbell@metrohaul.com",
            address = "550 Market Street, 14th Floor",
            nationalId = "SSN-XXX-9943",
            kycStatus = "Verified",
            creditScore = 580,
            activeLoansCount = 1,
            totalBorrowed = 35000.0,
            totalOutstanding = 31200.0
        ),
        Customer(
            id = "CUST-1006",
            name = "Priya Sharma",
            phone = "+1 (555) 432-1098",
            email = "priya.sharma@innovatech.io",
            address = "32 Silicon Parkway, Block C",
            nationalId = "SSN-XXX-3321",
            kycStatus = "Verified",
            creditScore = 720,
            activeLoansCount = 1,
            totalBorrowed = 50000.0,
            totalOutstanding = 39000.0
        )
    )

    private val loansTable = mutableListOf(
        Loan(
            id = "LN-8801",
            loanNumber = "FP-2026-LN-001",
            customerId = "CUST-1001",
            customerName = "Alexander Wright",
            customerPhone = "+1 (555) 234-5678",
            principalAmount = 45000.0,
            interestRate = 12.5,
            tenureMonths = 24,
            emiAmount = 2128.0,
            disbursedDate = "2025-06-15",
            status = LoanStatus.ACTIVE,
            totalPaid = 16500.0,
            remainingBalance = 28500.0,
            nextDueDate = "2026-11-15",
            assignedAgentName = "David Miller"
        ),
        Loan(
            id = "LN-8802",
            loanNumber = "FP-2026-LN-002",
            customerId = "CUST-1002",
            customerName = "Sarah Jenkins",
            customerPhone = "+1 (555) 876-5432",
            principalAmount = 25000.0,
            interestRate = 14.0,
            tenureMonths = 18,
            emiAmount = 1548.0,
            disbursedDate = "2025-09-10",
            status = LoanStatus.ACTIVE,
            totalPaid = 12600.0,
            remainingBalance = 12400.0,
            nextDueDate = "2026-11-10",
            assignedAgentName = "David Miller"
        ),
        Loan(
            id = "LN-8803",
            loanNumber = "FP-2026-LN-003",
            customerId = "CUST-1003",
            customerName = "Marcus Aurelius Vance",
            customerPhone = "+1 (555) 345-6789",
            principalAmount = 75000.0,
            interestRate = 11.0,
            tenureMonths = 36,
            emiAmount = 2598.0,
            disbursedDate = "2025-01-20",
            status = LoanStatus.ACTIVE,
            totalPaid = 16800.0,
            remainingBalance = 58200.0,
            nextDueDate = "2026-11-20",
            assignedAgentName = "David Miller"
        ),
        Loan(
            id = "LN-8804",
            loanNumber = "FP-2026-LN-004",
            customerId = "CUST-1004",
            customerName = "Elena Rostova",
            customerPhone = "+1 (555) 901-2345",
            principalAmount = 15000.0,
            interestRate = 16.5,
            tenureMonths = 12,
            emiAmount = 1361.0,
            disbursedDate = "2026-02-01",
            status = LoanStatus.DEFAULTED,
            totalPaid = 8200.0,
            remainingBalance = 6800.0,
            nextDueDate = "2026-10-10",
            assignedAgentName = "Rachel Green"
        ),
        Loan(
            id = "LN-8805",
            loanNumber = "FP-2026-LN-005",
            customerId = "CUST-1005",
            customerName = "Derrick Campbell",
            customerPhone = "+1 (555) 678-9012",
            principalAmount = 35000.0,
            interestRate = 15.0,
            tenureMonths = 24,
            emiAmount = 1746.0,
            disbursedDate = "2025-11-05",
            status = LoanStatus.DEFAULTED,
            totalPaid = 3800.0,
            remainingBalance = 31200.0,
            nextDueDate = "2026-07-05",
            assignedAgentName = "Carlos Ortiz"
        ),
        Loan(
            id = "LN-8806",
            loanNumber = "FP-2026-LN-006",
            customerId = "CUST-1006",
            customerName = "Priya Sharma",
            customerPhone = "+1 (555) 432-1098",
            principalAmount = 50000.0,
            interestRate = 11.5,
            tenureMonths = 36,
            emiAmount = 1637.0,
            disbursedDate = "2025-05-12",
            status = LoanStatus.ACTIVE,
            totalPaid = 11000.0,
            remainingBalance = 39000.0,
            nextDueDate = "2026-11-12",
            assignedAgentName = "Rachel Green"
        )
    )

    private val emisTable = mutableListOf(
        EMIItem("EMI-1", "LN-8801", "FP-2026-LN-001", "Alexander Wright", 17, 24, "2026-11-15", 2128.0, 1850.0, 278.0, 0.0, EMIStatus.PENDING),
        EMIItem("EMI-2", "LN-8802", "FP-2026-LN-002", "Sarah Jenkins", 14, 18, "2026-11-10", 1548.0, 1360.0, 188.0, 0.0, EMIStatus.PENDING),
        EMIItem("EMI-3", "LN-8803", "FP-2026-LN-003", "Marcus Aurelius Vance", 22, 36, "2026-11-20", 2598.0, 2210.0, 388.0, 0.0, EMIStatus.PENDING),
        EMIItem("EMI-4", "LN-8804", "FP-2026-LN-004", "Elena Rostova", 8, 12, "2026-10-10", 1361.0, 1210.0, 151.0, 75.0, EMIStatus.OVERDUE),
        EMIItem("EMI-5", "LN-8805", "FP-2026-LN-005", "Derrick Campbell", 7, 24, "2026-07-05", 1746.0, 1230.0, 516.0, 250.0, EMIStatus.OVERDUE),
        EMIItem("EMI-6", "LN-8806", "FP-2026-LN-006", "Priya Sharma", 24, 36, "2026-11-12", 1637.0, 1472.0, 165.0, 0.0, EMIStatus.PENDING),
        EMIItem("EMI-7", "LN-8801", "FP-2026-LN-001", "Alexander Wright", 16, 24, "2026-10-15", 2128.0, 1830.0, 298.0, 0.0, EMIStatus.PAID, "2026-10-14", PaymentMode.UPI, "David Miller"),
        EMIItem("EMI-8", "LN-8802", "FP-2026-LN-002", "Sarah Jenkins", 13, 18, "2026-10-10", 1548.0, 1345.0, 203.0, 0.0, EMIStatus.PAID, "2026-10-09", PaymentMode.CASH, "Rachel Green")
    )

    private val employeesTable = mutableListOf(
        Employee("EMP-SA-001", "Arthur Pendelton", "superadmin@financepro.com", "+1 (555) 001-9901", UserRole.SUPER_ADMIN, "Global HQ", 0, 0.0, 0.0),
        Employee("EMP-AD-002", "James Sterling", "admin@financepro.com", "+1 (555) 001-9902", UserRole.ADMIN, "Headquarters (HQ)", 0, 0.0, 0.0),
        Employee("EMP-FM-003", "Victoria Bennett", "finance@financepro.com", "+1 (555) 001-9903", UserRole.FINANCE_MANAGER, "Central Treasury", 0, 0.0, 0.0),
        Employee("EMP-CS-004", "Robert Chen", "supervisor@financepro.com", "+1 (555) 001-9904", UserRole.COLLECTION_SUPERVISOR, "Metro North Branch", 14, 180000.0, 145000.0),
        Employee("EMP-CE-005", "David Miller", "collector@financepro.com", "+1 (555) 001-9905", UserRole.COLLECTION_EMPLOYEE, "Metro North Branch", 8, 45000.0, 39500.0),
        Employee("EMP-CE-006", "Rachel Green", "r.green@financepro.com", "+1 (555) 200-012", UserRole.COLLECTION_EMPLOYEE, "Metro South Branch", 6, 40000.0, 34200.0),
        Employee("EMP-CE-007", "Carlos Ortiz", "c.ortiz@financepro.com", "+1 (555) 200-013", UserRole.COLLECTION_EMPLOYEE, "East Industrial Zone", 9, 50000.0, 28000.0)
    )

    private val assignmentsTable = mutableListOf(
        Assignment(
            id = "ASN-901",
            employeeId = "EMP-CE-005",
            employeeName = "David Miller",
            customerId = "CUST-1001",
            customerName = "Alexander Wright",
            customerPhone = "+1 (555) 234-5678",
            customerAddress = "742 Evergreen Terrace, Sector 4",
            loanId = "LN-8801",
            loanNumber = "FP-2026-LN-001",
            dueAmount = 2128.0,
            priority = "Standard",
            daysOverdue = 0,
            assignedDate = "2026-11-01"
        ),
        Assignment(
            id = "ASN-902",
            employeeId = "EMP-CE-005",
            employeeName = "David Miller",
            customerId = "CUST-1002",
            customerName = "Sarah Jenkins",
            customerPhone = "+1 (555) 876-5432",
            customerAddress = "1204 Pineview Boulevard, Suite 3B",
            loanId = "LN-8802",
            loanNumber = "FP-2026-LN-002",
            dueAmount = 1548.0,
            priority = "High",
            daysOverdue = 2,
            assignedDate = "2026-11-02"
        ),
        Assignment(
            id = "ASN-903",
            employeeId = "EMP-CE-005",
            employeeName = "David Miller",
            customerId = "CUST-1003",
            customerName = "Marcus Aurelius Vance",
            customerPhone = "+1 (555) 345-6789",
            customerAddress = "88 Harbor Point Way, Bay View",
            loanId = "LN-8803",
            loanNumber = "FP-2026-LN-003",
            dueAmount = 2598.0,
            priority = "Standard",
            daysOverdue = 0,
            assignedDate = "2026-11-03"
        ),
        Assignment(
            id = "ASN-904",
            employeeId = "EMP-CE-006",
            employeeName = "Rachel Green",
            customerId = "CUST-1004",
            customerName = "Elena Rostova",
            customerPhone = "+1 (555) 901-2345",
            customerAddress = "45 Elmhurst Circle, Apt 2",
            loanId = "LN-8804",
            loanNumber = "FP-2026-LN-004",
            dueAmount = 1436.0,
            priority = "Urgent",
            daysOverdue = 29,
            assignedDate = "2026-10-15"
        ),
        Assignment(
            id = "ASN-905",
            employeeId = "EMP-CE-007",
            employeeName = "Carlos Ortiz",
            customerId = "CUST-1005",
            customerName = "Derrick Campbell",
            customerPhone = "+1 (555) 678-9012",
            customerAddress = "550 Market Street, 14th Floor",
            loanId = "LN-8805",
            loanNumber = "FP-2026-LN-005",
            dueAmount = 1996.0,
            priority = "Urgent",
            daysOverdue = 126,
            assignedDate = "2026-08-01"
        )
    )

    private val collectionsTable = mutableListOf(
        CollectionRecord(
            id = "COL-701",
            receiptNumber = "REC-2026-8801",
            loanId = "LN-8801",
            loanNumber = "FP-2026-LN-001",
            customerId = "CUST-1001",
            customerName = "Alexander Wright",
            customerPhone = "+1 (555) 234-5678",
            emiId = "EMI-7",
            amountPaid = 2128.0,
            paymentMode = PaymentMode.UPI,
            collectedBy = "David Miller",
            collectedAt = "2026-10-14 14:32",
            status = ReceiptStatus.VERIFIED,
            verifiedBy = "Victoria Bennett (Finance Manager)",
            verifiedAt = "2026-10-14 17:00",
            auditNote = "Bank statement confirmed via automated payment gateway webhook"
        ),
        CollectionRecord(
            id = "COL-702",
            receiptNumber = "REC-2026-8802",
            loanId = "LN-8802",
            loanNumber = "FP-2026-LN-002",
            customerId = "CUST-1002",
            customerName = "Sarah Jenkins",
            customerPhone = "+1 (555) 876-5432",
            emiId = "EMI-8",
            amountPaid = 1548.0,
            paymentMode = PaymentMode.CASH,
            collectedBy = "David Miller",
            collectedAt = "2026-10-09 11:15",
            status = ReceiptStatus.VERIFIED,
            verifiedBy = "Victoria Bennett (Finance Manager)",
            verifiedAt = "2026-10-09 16:30",
            auditNote = "Physical cash deposited at branch counter #3"
        ),
        CollectionRecord(
            id = "COL-703",
            receiptNumber = "REC-2026-8803",
            loanId = "LN-8803",
            loanNumber = "FP-2026-LN-003",
            customerId = "CUST-1003",
            customerName = "Marcus Aurelius Vance",
            customerPhone = "+1 (555) 345-6789",
            emiId = "EMI-MANUAL-01",
            amountPaid = 2598.0,
            paymentMode = PaymentMode.CHEQUE,
            collectedBy = "David Miller",
            collectedAt = "2026-11-04 15:40",
            status = ReceiptStatus.PENDING_VERIFICATION,
            auditNote = "Cheque #881902 Chase Bank pending 2-day clearing"
        ),
        CollectionRecord(
            id = "COL-704",
            receiptNumber = "REC-2026-8804",
            loanId = "LN-8804",
            loanNumber = "FP-2026-LN-004",
            customerId = "CUST-1004",
            customerName = "Elena Rostova",
            customerPhone = "+1 (555) 901-2345",
            emiId = "EMI-MANUAL-02",
            amountPaid = 1361.0,
            paymentMode = PaymentMode.UPI,
            collectedBy = "Rachel Green",
            collectedAt = "2026-11-05 09:20",
            status = ReceiptStatus.PENDING_VERIFICATION,
            auditNote = "UPI transaction ref UPI-9921448 awaiting treasury confirmation"
        )
    )

    private val branchPerformances = listOf(
        BranchPerformance("Metro North Branch", 485000.0, 412000.0, 84.9, 42),
        BranchPerformance("Headquarters (HQ)", 620000.0, 565000.0, 91.1, 56),
        BranchPerformance("Metro South Branch", 390000.0, 315000.0, 80.7, 34),
        BranchPerformance("East Industrial Zone", 280000.0, 198000.0, 70.7, 28)
    )

    private val notificationsTable = mutableListOf(
        NotificationItem(
            id = "NOTIF-1",
            title = "High Risk NPA Alert",
            message = "Loan FP-2026-LN-005 (Derrick Campbell) reached 126 days overdue. Escalation to Legal Recovery initiated.",
            timestamp = "10 mins ago",
            category = "Risk",
            isRead = false,
            priority = "Urgent"
        ),
        NotificationItem(
            id = "NOTIF-2",
            title = "Payment Verification Required",
            message = "New UPI payment of $ 2,598 from Marcus Aurelius Vance requires supervisor sign-off.",
            timestamp = "45 mins ago",
            category = "Verification",
            isRead = false,
            priority = "High"
        ),
        NotificationItem(
            id = "NOTIF-3",
            title = "Daily Target Achieved",
            message = "Metro North field team reached 67% of today's collection target ($ 8,450 / $ 12,500).",
            timestamp = "2 hours ago",
            category = "System",
            isRead = true,
            priority = "Normal"
        )
    )

    /**
     * Internal security helper to verify granular role permission.
     */
    private fun hasPermission(role: UserRole, module: AppModule, action: PermissionAction): Boolean {
        val actions = permissionsMatrix[role to module] ?: emptySet()
        return actions.contains(action)
    }

    override suspend fun getPermissionsMatrix(): Map<Pair<UserRole, AppModule>, Set<PermissionAction>> {
        return permissionsMatrix.toMap()
    }

    override suspend fun updatePermission(
        role: UserRole,
        module: AppModule,
        action: PermissionAction,
        enabled: Boolean,
        callerSession: AuthSession?
    ): Result<Boolean> {
        val caller = callerSession?.user
            ?: return Result.failure(SecurityException("Unauthorized: Authentication session required."))

        // Only Super Admin can modify permissions
        if (caller.role != UserRole.SUPER_ADMIN && !hasPermission(caller.role, AppModule.SETTINGS, PermissionAction.UPDATE)) {
            val deniedEntry = AuditLogEntry(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = caller.employeeId,
                actorName = caller.fullName,
                actorRole = caller.role,
                action = PermissionAction.UPDATE,
                module = AppModule.SETTINGS,
                entityType = "PermissionMatrix",
                entityId = "${role.name}:${module.id}:${action.key}",
                details = "BLOCKED: Non-admin attempted to change permission policy.",
                status = AuditStatus.DENIED
            )
            auditLogsTable.add(0, deniedEntry)
            return Result.failure(SecurityException("Access Denied: Only Super Admin can modify role permissions."))
        }

        val key = role to module
        val currentSet = permissionsMatrix.getOrPut(key) { mutableSetOf() }
        if (enabled) {
            currentSet.add(action)
        } else {
            currentSet.remove(action)
        }

        // Record privileged action in audit logs
        val auditEntry = AuditLogEntry(
            id = "AUD-${System.currentTimeMillis()}",
            actorId = caller.employeeId,
            actorName = caller.fullName,
            actorRole = caller.role,
            action = PermissionAction.UPDATE,
            module = AppModule.SETTINGS,
            entityType = "PermissionPolicy",
            entityId = "${role.name}:${module.id}:${action.key}",
            details = "Updated permission for role '${role.badgeLabel}' on module '${module.shortTitle}' [${action.label} = $enabled]",
            status = AuditStatus.SUCCESS
        )
        auditLogsTable.add(0, auditEntry)

        return Result.success(true)
    }

    override suspend fun resetPermissionsToDefaults(callerSession: AuthSession?): Result<Boolean> {
        val caller = callerSession?.user
            ?: return Result.failure(SecurityException("Unauthorized: Authentication session required."))

        if (caller.role != UserRole.SUPER_ADMIN) {
            return Result.failure(SecurityException("Access Denied: Only Super Admin can reset system permissions."))
        }

        permissionsMatrix.clear()
        permissionsMatrix.putAll(DefaultPermissions.getInitialPermissionMatrix())

        val auditEntry = AuditLogEntry(
            id = "AUD-${System.currentTimeMillis()}",
            actorId = caller.employeeId,
            actorName = caller.fullName,
            actorRole = caller.role,
            action = PermissionAction.UPDATE,
            module = AppModule.SETTINGS,
            entityType = "PermissionMatrix",
            entityId = "ALL_ROLES_DEFAULT",
            details = "Reset all 5 role permission policies to system defaults.",
            status = AuditStatus.SUCCESS
        )
        auditLogsTable.add(0, auditEntry)

        return Result.success(true)
    }

    override suspend fun getAuditLogs(): List<AuditLogEntry> {
        delay(40)
        return auditLogsTable.toList()
    }

    override suspend fun logAuditEvent(entry: AuditLogEntry): Boolean {
        auditLogsTable.add(0, entry)
        return true
    }

    override suspend fun getDashboardMetrics(role: UserRole): DashboardMetrics {
        delay(100)
        val totalDisbursed = loansTable.sumOf { it.principalAmount }
        val totalCollected = collectionsTable.filter { it.status == ReceiptStatus.VERIFIED }.sumOf { it.amountPaid }
        val totalOutstanding = loansTable.sumOf { it.remainingBalance }
        val recoveryRate = if (totalDisbursed > 0) (totalCollected / totalDisbursed) * 100 else 0.0
        val pendingCount = assignmentsTable.count { it.daysOverdue > 0 }
        val defaultedLoans = loansTable.filter { it.status == LoanStatus.DEFAULTED }.sumOf { it.remainingBalance }
        val npaRate = if (totalOutstanding > 0) (defaultedLoans / totalOutstanding) * 100 else 0.0
        val pendingVerifs = collectionsTable.count { it.status == ReceiptStatus.PENDING_VERIFICATION }

        return DashboardMetrics(
            totalDisbursed = totalDisbursed,
            totalCollected = totalCollected,
            totalOutstanding = totalOutstanding,
            recoveryRate = recoveryRate,
            activeLoansCount = loansTable.count { it.status == LoanStatus.ACTIVE },
            pendingDuesCount = pendingCount,
            npaRate = npaRate,
            dailyCollectionTarget = 12500.0,
            dailyCollectedSoFar = 8450.0,
            pendingVerificationsCount = pendingVerifs,
            activeAgentsCount = employeesTable.count { it.role == UserRole.COLLECTION_EMPLOYEE }
        )
    }

    override suspend fun getCustomers(
        searchQuery: String,
        callerRole: UserRole?,
        callerEmployeeId: String?
    ): List<Customer> {
        delay(60)

        // Backend enforcement: Check VIEW permission
        if (callerRole != null && !hasPermission(callerRole, AppModule.CUSTOMER_MANAGEMENT, PermissionAction.VIEW)) {
            return emptyList()
        }

        var list = customersTable.toList()

        // Backend enforcement: Collection Employee views only assigned customers
        if (callerRole == UserRole.COLLECTION_EMPLOYEE && callerEmployeeId != null) {
            val assignedCustIds = assignmentsTable
                .filter { it.employeeId == callerEmployeeId }
                .map { it.customerId }
                .toSet()
            list = list.filter { it.id in assignedCustIds }
        }

        return if (searchQuery.isBlank()) {
            list
        } else {
            list.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.phone.contains(searchQuery, ignoreCase = true) ||
                it.nationalId.contains(searchQuery, ignoreCase = true) ||
                it.id.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    override suspend fun getLoans(filterStatus: LoanStatus?): List<Loan> {
        delay(60)
        return if (filterStatus == null) loansTable else loansTable.filter { it.status == filterStatus }
    }

    override suspend fun getEMISchedule(loanId: String?): List<EMIItem> {
        delay(60)
        return if (loanId == null) emisTable else emisTable.filter { it.loanId == loanId }
    }

    override suspend fun getPendingDues(): List<Assignment> {
        delay(60)
        return assignmentsTable.filter { it.daysOverdue > 0 }.sortedByDescending { it.daysOverdue }
    }

    override suspend fun getAgingBuckets(): List<AgingBucket> {
        delay(40)
        return listOf(
            AgingBucket("Current / 1-30 Days", "1-30 Days", 18, 28450.0, "Low"),
            AgingBucket("Special Mention 1", "31-60 Days", 7, 14200.0, "Moderate"),
            AgingBucket("Substandard / SMA-2", "61-90 Days", 4, 9800.0, "Critical"),
            AgingBucket("Doubtful / NPA", "90+ Days", 2, 33800.0, "NPA")
        )
    }

    override suspend fun getEmployees(): List<Employee> {
        delay(50)
        return employeesTable
    }

    override suspend fun getAssignments(employeeId: String?): List<Assignment> {
        delay(50)
        return if (employeeId == null) assignmentsTable else assignmentsTable.filter { it.employeeId == employeeId }
    }

    override suspend fun getCollections(): List<CollectionRecord> {
        delay(50)
        return collectionsTable.sortedByDescending { it.collectedAt }
    }

    override suspend fun recordCollection(
        customerId: String,
        loanId: String,
        amount: Double,
        mode: PaymentMode,
        collectedBy: String,
        note: String,
        callerSession: AuthSession?
    ): Result<CollectionRecord> {
        delay(120)

        val caller = callerSession?.user
            ?: return Result.failure(SecurityException("Unauthorized: Authentication session required."))

        // Backend permission check: CREATE on LOAN_COLLECTION
        if (!hasPermission(caller.role, AppModule.LOAN_COLLECTION, PermissionAction.CREATE)) {
            val deniedEntry = AuditLogEntry(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = caller.employeeId,
                actorName = caller.fullName,
                actorRole = caller.role,
                action = PermissionAction.CREATE,
                module = AppModule.LOAN_COLLECTION,
                entityType = "CollectionReceipt",
                entityId = loanId,
                details = "BLOCKED: Role '${caller.role.badgeLabel}' is not authorized to submit collections.",
                status = AuditStatus.DENIED
            )
            auditLogsTable.add(0, deniedEntry)
            return Result.failure(SecurityException("Access Denied: Role '${caller.role.badgeLabel}' cannot submit collections."))
        }

        // Granular rule: Collection Employee can only collect for assigned customers
        if (caller.role == UserRole.COLLECTION_EMPLOYEE) {
            val isAssigned = assignmentsTable.any {
                it.employeeId == caller.employeeId && (it.customerId == customerId || it.loanId == loanId)
            }
            if (!isAssigned) {
                val deniedEntry = AuditLogEntry(
                    id = "AUD-${System.currentTimeMillis()}",
                    actorId = caller.employeeId,
                    actorName = caller.fullName,
                    actorRole = caller.role,
                    action = PermissionAction.CREATE,
                    module = AppModule.LOAN_COLLECTION,
                    entityType = "CollectionReceipt",
                    entityId = customerId,
                    details = "BLOCKED: Collector attempted to submit payment for unassigned customer.",
                    status = AuditStatus.DENIED
                )
                auditLogsTable.add(0, deniedEntry)
                return Result.failure(SecurityException("Access Denied: You can only record collections for assigned customers."))
            }
        }

        val customer = customersTable.find { it.id == customerId }
            ?: return Result.failure(Exception("Customer not found"))
        val loan = loansTable.find { it.id == loanId }
            ?: return Result.failure(Exception("Loan not found"))

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val nowStr = sdf.format(Date())
        val receiptNum = "REC-2026-${(1000..9999).random()}"

        val newRecord = CollectionRecord(
            id = "COL-${UUID.randomUUID().toString().take(6).uppercase()}",
            receiptNumber = receiptNum,
            loanId = loan.id,
            loanNumber = loan.loanNumber,
            customerId = customer.id,
            customerName = customer.name,
            customerPhone = customer.phone,
            emiId = "EMI-MANUAL",
            amountPaid = amount,
            paymentMode = mode,
            collectedBy = collectedBy,
            collectedAt = nowStr,
            status = ReceiptStatus.PENDING_VERIFICATION,
            auditNote = note
        )

        collectionsTable.add(0, newRecord)

        // Update customer & loan balances
        val updatedBalance = (loan.remainingBalance - amount).coerceAtLeast(0.0)
        val loanIdx = loansTable.indexOf(loan)
        if (loanIdx >= 0) {
            loansTable[loanIdx] = loan.copy(
                remainingBalance = updatedBalance,
                totalPaid = loan.totalPaid + amount
            )
        }

        // Audit log of successful collection
        auditLogsTable.add(0, AuditLogEntry(
            id = "AUD-${System.currentTimeMillis()}",
            actorId = caller.employeeId,
            actorName = caller.fullName,
            actorRole = caller.role,
            action = PermissionAction.CREATE,
            module = AppModule.LOAN_COLLECTION,
            entityType = "CollectionReceipt",
            entityId = newRecord.receiptNumber,
            details = "Recorded collection of $ ${amount.toInt()} via ${mode.label} for ${customer.name} (Receipt: $receiptNum)",
            status = AuditStatus.SUCCESS
        ))

        // System notification
        notificationsTable.add(0, NotificationItem(
            id = "NOTIF-${System.currentTimeMillis()}",
            title = "New Field Payment Recorded",
            message = "Agent $collectedBy collected $ ${amount.toInt()} via ${mode.label} for ${customer.name} (Receipt: $receiptNum)",
            timestamp = "Just now",
            category = "Verification",
            isRead = false,
            priority = "Normal"
        ))

        return Result.success(newRecord)
    }

    override suspend fun verifyPayment(
        receiptId: String,
        approved: Boolean,
        verifiedBy: String,
        note: String,
        callerSession: AuthSession?
    ): Result<CollectionRecord> {
        delay(100)

        val caller = callerSession?.user
            ?: return Result.failure(SecurityException("Unauthorized: Authentication session required."))

        // Backend permission check: APPROVE on PAYMENT_VERIFICATION
        if (!hasPermission(caller.role, AppModule.PAYMENT_VERIFICATION, PermissionAction.APPROVE)) {
            val deniedEntry = AuditLogEntry(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = caller.employeeId,
                actorName = caller.fullName,
                actorRole = caller.role,
                action = PermissionAction.APPROVE,
                module = AppModule.PAYMENT_VERIFICATION,
                entityType = "PaymentReceipt",
                entityId = receiptId,
                details = "BLOCKED: Role '${caller.role.badgeLabel}' lacks APPROVE permission.",
                status = AuditStatus.DENIED
            )
            auditLogsTable.add(0, deniedEntry)
            return Result.failure(SecurityException("Access Denied: Role '${caller.role.badgeLabel}' is not authorized to approve payments."))
        }

        val record = collectionsTable.find { it.id == receiptId }
            ?: return Result.failure(Exception("Receipt not found"))

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val nowStr = sdf.format(Date())

        val updatedRecord = record.copy(
            status = if (approved) ReceiptStatus.VERIFIED else ReceiptStatus.REJECTED,
            verifiedBy = verifiedBy,
            verifiedAt = nowStr,
            auditNote = if (note.isBlank()) record.auditNote else "$note (Audited by $verifiedBy)"
        )

        val idx = collectionsTable.indexOf(record)
        collectionsTable[idx] = updatedRecord

        // Record privileged action in audit logs
        auditLogsTable.add(0, AuditLogEntry(
            id = "AUD-${System.currentTimeMillis()}",
            actorId = caller.employeeId,
            actorName = caller.fullName,
            actorRole = caller.role,
            action = PermissionAction.APPROVE,
            module = AppModule.PAYMENT_VERIFICATION,
            entityType = "PaymentReceipt",
            entityId = record.receiptNumber,
            details = "${if (approved) "Approved & Cleared" else "Rejected"} receipt ${record.receiptNumber} ($ ${record.amountPaid.toInt()}) by $verifiedBy",
            status = AuditStatus.SUCCESS
        ))

        return Result.success(updatedRecord)
    }

    override suspend fun reversePayment(
        receiptId: String,
        reason: String,
        callerSession: AuthSession?
    ): Result<CollectionRecord> {
        delay(120)

        val caller = callerSession?.user
            ?: return Result.failure(SecurityException("Unauthorized: Authentication session required."))

        // Backend permission check: REVERSE on PAYMENT_VERIFICATION
        if (!hasPermission(caller.role, AppModule.PAYMENT_VERIFICATION, PermissionAction.REVERSE)) {
            val deniedEntry = AuditLogEntry(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = caller.employeeId,
                actorName = caller.fullName,
                actorRole = caller.role,
                action = PermissionAction.REVERSE,
                module = AppModule.PAYMENT_VERIFICATION,
                entityType = "PaymentReceipt",
                entityId = receiptId,
                details = "BLOCKED: Role '${caller.role.badgeLabel}' is not authorized to reverse transactions.",
                status = AuditStatus.DENIED
            )
            auditLogsTable.add(0, deniedEntry)
            return Result.failure(SecurityException("Access Denied: Role '${caller.role.badgeLabel}' cannot reverse transactions. Restricted to Finance Manager and Super Admin."))
        }

        val record = collectionsTable.find { it.id == receiptId }
            ?: return Result.failure(Exception("Receipt $receiptId not found."))

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val nowStr = sdf.format(Date())

        // Reverse loan balance mutation if it was recorded
        val loan = loansTable.find { it.id == record.loanId }
        if (loan != null) {
            val loanIdx = loansTable.indexOf(loan)
            loansTable[loanIdx] = loan.copy(
                remainingBalance = loan.remainingBalance + record.amountPaid,
                totalPaid = (loan.totalPaid - record.amountPaid).coerceAtLeast(0.0)
            )
        }

        val reversedRecord = record.copy(
            status = ReceiptStatus.REVERSED,
            auditNote = "REVERSED on $nowStr by ${caller.fullName} (${caller.role.badgeLabel}). Reason: $reason"
        )
        val idx = collectionsTable.indexOf(record)
        collectionsTable[idx] = reversedRecord

        // Record privileged reversal in audit logs
        val auditEntry = AuditLogEntry(
            id = "AUD-${System.currentTimeMillis()}",
            actorId = caller.employeeId,
            actorName = caller.fullName,
            actorRole = caller.role,
            action = PermissionAction.REVERSE,
            module = AppModule.PAYMENT_VERIFICATION,
            entityType = "PaymentReceipt",
            entityId = record.receiptNumber,
            details = "Reversed collection receipt ${record.receiptNumber} ($ ${record.amountPaid.toInt()}). Reason: $reason. Loan balance restored.",
            status = AuditStatus.REVERSED
        )
        auditLogsTable.add(0, auditEntry)

        return Result.success(reversedRecord)
    }

    override suspend fun allocateAssignment(
        customerId: String,
        loanId: String,
        employeeId: String,
        dueAmount: Double,
        callerSession: AuthSession?
    ): Result<Assignment> {
        val caller = callerSession?.user
            ?: return Result.failure(SecurityException("Unauthorized: Authentication session required."))

        // Backend permission check: CREATE on EMPLOYEE_ASSIGNMENTS
        if (!hasPermission(caller.role, AppModule.EMPLOYEE_ASSIGNMENTS, PermissionAction.CREATE)) {
            val denied = AuditLogEntry(
                id = "AUD-${System.currentTimeMillis()}",
                actorId = caller.employeeId,
                actorName = caller.fullName,
                actorRole = caller.role,
                action = PermissionAction.CREATE,
                module = AppModule.EMPLOYEE_ASSIGNMENTS,
                entityType = "Assignment",
                entityId = employeeId,
                details = "BLOCKED: Role '${caller.role.badgeLabel}' cannot allocate field assignments.",
                status = AuditStatus.DENIED
            )
            auditLogsTable.add(0, denied)
            return Result.failure(SecurityException("Access Denied: Role '${caller.role.badgeLabel}' cannot allocate collection assignments."))
        }

        val employee = employeesTable.find { it.id == employeeId }
            ?: return Result.failure(Exception("Employee not found"))
        val customer = customersTable.find { it.id == customerId }
            ?: return Result.failure(Exception("Customer not found"))
        val loan = loansTable.find { it.id == loanId }
            ?: return Result.failure(Exception("Loan not found"))

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val newAssignment = Assignment(
            id = "ASN-${(100..999).random()}",
            employeeId = employee.id,
            employeeName = employee.name,
            customerId = customer.id,
            customerName = customer.name,
            customerPhone = customer.phone,
            customerAddress = customer.address,
            loanId = loan.id,
            loanNumber = loan.loanNumber,
            dueAmount = dueAmount,
            priority = "High",
            daysOverdue = 5,
            assignedDate = sdf.format(Date()),
            status = "Pending Visit"
        )
        assignmentsTable.add(0, newAssignment)

        // Record in audit logs
        auditLogsTable.add(0, AuditLogEntry(
            id = "AUD-${System.currentTimeMillis()}",
            actorId = caller.employeeId,
            actorName = caller.fullName,
            actorRole = caller.role,
            action = PermissionAction.CREATE,
            module = AppModule.EMPLOYEE_ASSIGNMENTS,
            entityType = "Assignment",
            entityId = newAssignment.id,
            details = "Allocated collection task for ${customer.name} to agent ${employee.name}",
            status = AuditStatus.SUCCESS
        ))

        return Result.success(newAssignment)
    }

    override suspend fun getBranchPerformances(): List<BranchPerformance> {
        delay(30)
        return branchPerformances
    }

    override suspend fun getNotifications(): List<NotificationItem> {
        delay(30)
        return notificationsTable
    }

    override suspend fun markNotificationRead(id: String): Boolean {
        val idx = notificationsTable.indexOfFirst { it.id == id }
        if (idx >= 0) {
            notificationsTable[idx] = notificationsTable[idx].copy(isRead = true)
            return true
        }
        return false
    }

    override suspend fun getArchitectureStatus(): Map<String, String> {
        return mapOf(
            "frontend" to "Jetpack Compose M3 (Fintech UI System)",
            "backend" to "REST API (Role-Based Access Enforcement)",
            "database" to "PostgreSQL (Granular Audit & Entity Logs)",
            "auth" to "Multi-Tenant Session & Salted Cryptographic Hashing",
            "rbac_engine" to "Active (VIEW, CREATE, UPDATE, APPROVE, REVERSE)",
            "audit_logging" to "Active (Immutable Security Ledger)",
            "api_status" to "Healthy (200 OK)",
            "db_latency" to "21 ms",
            "cache_layer" to "Redis / In-Memory StateStore Active",
            "offline_mode" to "Enabled (Field Collector Sync)"
        )
    }
}

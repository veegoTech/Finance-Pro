package com.example.data.models

enum class AppModule(
    val id: String,
    val title: String,
    val shortTitle: String,
    val category: String,
    val allowedRoles: Set<UserRole>
) {
    DASHBOARD(
        id = "dashboard",
        title = "Executive Dashboard",
        shortTitle = "Dashboard",
        category = "Overview",
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.FINANCE_MANAGER, UserRole.COLLECTION_SUPERVISOR, UserRole.COLLECTION_EMPLOYEE)
    ),
    CUSTOMER_MANAGEMENT(
        id = "customers",
        title = "Customer Management",
        shortTitle = "Customers",
        category = "Core Banking",
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.FINANCE_MANAGER, UserRole.COLLECTION_SUPERVISOR, UserRole.COLLECTION_EMPLOYEE)
    ),
    LOAN_MANAGEMENT(
        id = "loans",
        title = "Loan Portfolio Management",
        shortTitle = "Loans",
        category = "Core Banking",
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.FINANCE_MANAGER, UserRole.COLLECTION_SUPERVISOR)
    ),
    EMI_MANAGEMENT(
        id = "emi",
        title = "EMI Schedule & Calculator",
        shortTitle = "EMI Schedule",
        category = "Financial",
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.FINANCE_MANAGER, UserRole.COLLECTION_SUPERVISOR, UserRole.COLLECTION_EMPLOYEE)
    ),
    PENDING_DUES(
        id = "pending_dues",
        title = "Pending Dues & Aging Buckets",
        shortTitle = "Pending Dues",
        category = "Collections",
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.FINANCE_MANAGER, UserRole.COLLECTION_SUPERVISOR, UserRole.COLLECTION_EMPLOYEE)
    ),
    EMPLOYEE_MANAGEMENT(
        id = "employees",
        title = "Staff & Employee Management",
        shortTitle = "Employees",
        category = "Administration",
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.FINANCE_MANAGER, UserRole.COLLECTION_SUPERVISOR)
    ),
    EMPLOYEE_ASSIGNMENTS(
        id = "assignments",
        title = "Field Route & Lead Assignments",
        shortTitle = "Assignments",
        category = "Collections",
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.COLLECTION_SUPERVISOR, UserRole.COLLECTION_EMPLOYEE)
    ),
    LOAN_COLLECTION(
        id = "collection",
        title = "Loan Collection & Instant Receipt",
        shortTitle = "Collect EMI",
        category = "Operations",
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.COLLECTION_SUPERVISOR, UserRole.COLLECTION_EMPLOYEE)
    ),
    PAYMENT_VERIFICATION(
        id = "verification",
        title = "Payment Audit & Verification",
        shortTitle = "Verification",
        category = "Operations",
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.FINANCE_MANAGER, UserRole.COLLECTION_SUPERVISOR)
    ),
    REPORTS(
        id = "reports",
        title = "Financial & Recovery Reports",
        shortTitle = "Reports",
        category = "Analytics",
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.FINANCE_MANAGER, UserRole.COLLECTION_SUPERVISOR)
    ),
    NOTIFICATIONS(
        id = "notifications",
        title = "Alerts & Due Notifications",
        shortTitle = "Alerts",
        category = "Communication",
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.FINANCE_MANAGER, UserRole.COLLECTION_SUPERVISOR, UserRole.COLLECTION_EMPLOYEE)
    ),
    SETTINGS(
        id = "settings",
        title = "System Architecture & Security",
        shortTitle = "Settings",
        category = "Administration",
        allowedRoles = setOf(UserRole.SUPER_ADMIN, UserRole.ADMIN, UserRole.FINANCE_MANAGER, UserRole.COLLECTION_SUPERVISOR, UserRole.COLLECTION_EMPLOYEE)
    );

    fun isAvailableFor(role: UserRole): Boolean = allowedRoles.contains(role)
}

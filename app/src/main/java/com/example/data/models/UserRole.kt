package com.example.data.models

enum class UserRole(
    val title: String,
    val badgeLabel: String,
    val description: String,
    val iconName: String,
    val accessLevel: Int
) {
    SUPER_ADMIN(
        title = "Super Admin Dashboard",
        badgeLabel = "Super Admin",
        description = "Enterprise governance, global liquidity, security audits, and role permission authorization.",
        iconName = "security",
        accessLevel = 100
    ),
    ADMIN(
        title = "Admin Dashboard",
        badgeLabel = "Administrator",
        description = "Executive oversight, system liquidity, company health, audit logs, and employee controls.",
        iconName = "admin_panel_settings",
        accessLevel = 80
    ),
    FINANCE_MANAGER(
        title = "Finance Manager Dashboard",
        badgeLabel = "Finance Manager",
        description = "Loan origination & disbursement, interest yield, risk assessment, and NPA portfolio.",
        iconName = "account_balance",
        accessLevel = 60
    ),
    COLLECTION_SUPERVISOR(
        title = "Supervisor Dashboard",
        badgeLabel = "Collection Supervisor",
        description = "Field collection monitoring, route assignments, recovery rate targets, and bucket aging.",
        iconName = "supervisor_account",
        accessLevel = 40
    ),
    COLLECTION_EMPLOYEE(
        title = "Field Collection App",
        badgeLabel = "Collection Employee",
        description = "On-ground customer visits, instant EMI collection, digital receipt generation, and offline sync.",
        iconName = "point_of_sale",
        accessLevel = 20
    );

    companion object {
        // Backward-compatibility alias
        val FIELD_EMPLOYEE: UserRole get() = COLLECTION_EMPLOYEE
    }
}

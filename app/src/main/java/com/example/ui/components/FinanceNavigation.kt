package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AppModule
import com.example.data.models.AuthSession
import com.example.data.models.UserRole
import com.example.ui.theme.*

@Composable
fun getModuleIcon(module: AppModule): ImageVector {
    return when (module) {
        AppModule.DASHBOARD -> Icons.Default.GridView
        AppModule.CUSTOMER_MANAGEMENT -> Icons.Default.PeopleAlt
        AppModule.LOAN_MANAGEMENT -> Icons.Default.AccountBalance
        AppModule.EMI_MANAGEMENT -> Icons.Default.CalendarMonth
        AppModule.PENDING_DUES -> Icons.Default.PendingActions
        AppModule.EMPLOYEE_MANAGEMENT -> Icons.Default.Badge
        AppModule.EMPLOYEE_ASSIGNMENTS -> Icons.Default.AltRoute
        AppModule.LOAN_COLLECTION -> Icons.Default.Payments
        AppModule.PAYMENT_VERIFICATION -> Icons.Default.FactCheck
        AppModule.REPORTS -> Icons.Default.BarChart
        AppModule.NOTIFICATIONS -> Icons.Default.Notifications
        AppModule.SETTINGS -> Icons.Default.Tune
    }
}

@Composable
fun FinanceAppDrawerContent(
    currentSession: AuthSession?,
    currentModule: AppModule,
    currentRole: UserRole,
    onSelectModule: (AppModule) -> Unit,
    onCloseDrawer: () -> Unit,
    onOpenRoleSelector: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier
            .width(310.dp)
            .fillMaxHeight(),
        drawerContainerColor = BackgroundWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Enterprise Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkNavyBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Finance",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = DarkNavyBlue
                        )
                        Text(
                            text = "Pro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = RoyalBlue
                        )
                    }
                    Text(
                        text = "RBAC Security Layer Active",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // User Account Session Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundLightGrey)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DarkNavyBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentSession?.user?.fullName?.take(2)?.uppercase() ?: "US",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentSession?.user?.fullName ?: "Authorized User",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "ID: ${currentSession?.user?.employeeId ?: "EMP-NA"} • ${currentRole.badgeLabel}",
                                fontSize = 11.sp,
                                color = RoyalBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = BorderSubtle.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Branch: ${currentSession?.user?.branch ?: "Headquarters"}",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Switch Persona >",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue,
                            modifier = Modifier.clickable(onClick = onOpenRoleSelector)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            // Role-Specific Navigation Groups
            val navigationGroups: List<Pair<String, List<AppModule>>> = when (currentRole) {
                UserRole.SUPER_ADMIN -> listOf(
                    "Global Command" to listOf(AppModule.DASHBOARD),
                    "Core Banking & Treasury" to listOf(AppModule.CUSTOMER_MANAGEMENT, AppModule.LOAN_MANAGEMENT, AppModule.EMI_MANAGEMENT),
                    "Operations & Audit" to listOf(AppModule.PAYMENT_VERIFICATION, AppModule.PENDING_DUES, AppModule.LOAN_COLLECTION),
                    "Security & Governance" to listOf(AppModule.EMPLOYEE_MANAGEMENT, AppModule.EMPLOYEE_ASSIGNMENTS, AppModule.REPORTS, AppModule.NOTIFICATIONS, AppModule.SETTINGS)
                )
                UserRole.ADMIN -> listOf(
                    "Overview" to listOf(AppModule.DASHBOARD),
                    "Core Banking" to listOf(AppModule.CUSTOMER_MANAGEMENT, AppModule.LOAN_MANAGEMENT, AppModule.EMI_MANAGEMENT),
                    "Operations" to listOf(AppModule.PAYMENT_VERIFICATION, AppModule.PENDING_DUES, AppModule.LOAN_COLLECTION),
                    "Administration" to listOf(AppModule.EMPLOYEE_MANAGEMENT, AppModule.EMPLOYEE_ASSIGNMENTS, AppModule.REPORTS, AppModule.NOTIFICATIONS, AppModule.SETTINGS)
                )
                UserRole.FINANCE_MANAGER -> listOf(
                    "Executive Hub" to listOf(AppModule.DASHBOARD),
                    "Lending & Origination" to listOf(AppModule.LOAN_MANAGEMENT, AppModule.CUSTOMER_MANAGEMENT, AppModule.EMI_MANAGEMENT),
                    "Risk & Verification" to listOf(AppModule.PAYMENT_VERIFICATION, AppModule.PENDING_DUES),
                    "Analytics & Governance" to listOf(AppModule.REPORTS, AppModule.EMPLOYEE_MANAGEMENT, AppModule.NOTIFICATIONS, AppModule.SETTINGS)
                )
                UserRole.COLLECTION_SUPERVISOR -> listOf(
                    "Supervisor Board" to listOf(AppModule.DASHBOARD),
                    "Field Assignments" to listOf(AppModule.EMPLOYEE_ASSIGNMENTS, AppModule.PENDING_DUES),
                    "Receipt Audit" to listOf(AppModule.PAYMENT_VERIFICATION, AppModule.LOAN_COLLECTION),
                    "Team & Reports" to listOf(AppModule.EMPLOYEE_MANAGEMENT, AppModule.CUSTOMER_MANAGEMENT, AppModule.REPORTS, AppModule.NOTIFICATIONS)
                )
                UserRole.COLLECTION_EMPLOYEE -> listOf(
                    "Field Agent Tasks" to listOf(AppModule.DASHBOARD, AppModule.LOAN_COLLECTION, AppModule.EMPLOYEE_ASSIGNMENTS),
                    "Customer Directory" to listOf(AppModule.CUSTOMER_MANAGEMENT, AppModule.EMI_MANAGEMENT, AppModule.PENDING_DUES),
                    "Account" to listOf(AppModule.NOTIFICATIONS, AppModule.SETTINGS)
                )
            }

            navigationGroups.forEach { (sectionTitle, modules) ->
                Text(
                    text = sectionTitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )

                modules.forEach { module ->
                    val isSelected = module == currentModule
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) RoyalBlueSubtle else Color.Transparent)
                            .clickable {
                                onSelectModule(module)
                                onCloseDrawer()
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .testTag("nav_item_${module.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = getModuleIcon(module),
                                contentDescription = null,
                                tint = if (isSelected) RoyalBlue else TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = module.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) RoyalBlue else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button at bottom of drawer
            Button(
                onClick = {
                    onCloseDrawer()
                    onLogoutClick()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = OverdueRedLight,
                    contentColor = OverdueRedDark
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("drawer_logout_btn")
            ) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out (${currentSession?.user?.employeeId ?: "Session"})", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FinanceBottomNav(
    currentModule: AppModule,
    currentRole: UserRole,
    onSelectModule: (AppModule) -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomModules = when (currentRole) {
        UserRole.SUPER_ADMIN, UserRole.ADMIN -> listOf(
            AppModule.DASHBOARD,
            AppModule.CUSTOMER_MANAGEMENT,
            AppModule.LOAN_MANAGEMENT,
            AppModule.PAYMENT_VERIFICATION,
            AppModule.REPORTS
        )
        UserRole.FINANCE_MANAGER -> listOf(
            AppModule.DASHBOARD,
            AppModule.LOAN_MANAGEMENT,
            AppModule.EMI_MANAGEMENT,
            AppModule.PAYMENT_VERIFICATION,
            AppModule.REPORTS
        )
        UserRole.COLLECTION_SUPERVISOR -> listOf(
            AppModule.DASHBOARD,
            AppModule.EMPLOYEE_ASSIGNMENTS,
            AppModule.PENDING_DUES,
            AppModule.PAYMENT_VERIFICATION,
            AppModule.REPORTS
        )
        UserRole.COLLECTION_EMPLOYEE -> listOf(
            AppModule.DASHBOARD,
            AppModule.LOAN_COLLECTION,
            AppModule.EMPLOYEE_ASSIGNMENTS,
            AppModule.PENDING_DUES,
            AppModule.NOTIFICATIONS
        )
    }

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = BackgroundWhite,
        tonalElevation = 0.dp
    ) {
        bottomModules.forEach { module ->
            val isSelected = module == currentModule
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectModule(module) },
                icon = {
                    Icon(
                        imageVector = getModuleIcon(module),
                        contentDescription = module.shortTitle
                    )
                },
                label = {
                    Text(
                        text = module.shortTitle,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RoyalBlue,
                    selectedTextColor = RoyalBlue,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = RoyalBlueLight
                ),
                modifier = Modifier.testTag("bottom_nav_${module.id}")
            )
        }
    }
}

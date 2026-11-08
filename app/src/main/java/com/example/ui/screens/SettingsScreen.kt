package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.AppModule
import com.example.data.models.AuditLogEntry
import com.example.data.models.AuditStatus
import com.example.data.models.PermissionAction
import com.example.data.models.UserRole
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    currentRole: UserRole,
    permissionsMatrix: Map<Pair<UserRole, AppModule>, Set<PermissionAction>>,
    auditLogs: List<AuditLogEntry>,
    architectureStatus: Map<String, String>,
    onTogglePermission: (role: UserRole, module: AppModule, action: PermissionAction, enabled: Boolean) -> Unit,
    onResetDefaults: () -> Unit,
    onOpenRoleSelector: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Granular Permissions, 1: Privileged Audit Logs, 2: Stack Topology
    var inspectedRole by remember { mutableStateOf(currentRole) }
    var auditSearchQuery by remember { mutableStateOf("") }
    var selectedActionFilter by remember { mutableStateOf<PermissionAction?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<AuditStatus?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val isSuperAdmin = currentRole == UserRole.SUPER_ADMIN

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLightGrey)
    ) {
        // Governance Navigation Tabs
        Surface(
            color = BackgroundWhite,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Permission & Governance Hub",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavyBlue
                        )
                        Text(
                            text = "Granular Access Control: View, Create, Update, Approve, Reverse",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    if (isSuperAdmin && selectedTab == 0) {
                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavyBlue),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("reset_permissions_btn")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Defaults", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BackgroundLightGrey,
                    contentColor = DarkNavyBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = RoyalBlue,
                            height = 3.dp
                        )
                    },
                    divider = {}
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Permissions", fontSize = 12.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HistoryEdu, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Audit Logs (${auditLogs.size})", fontSize = 12.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Memory, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Topology", fontSize = 12.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                PermissionsMatrixTab(
                    inspectedRole = inspectedRole,
                    onSelectRole = { inspectedRole = it },
                    isSuperAdmin = isSuperAdmin,
                    permissionsMatrix = permissionsMatrix,
                    onTogglePermission = onTogglePermission
                )
            }
            1 -> {
                AuditLogsTab(
                    auditLogs = auditLogs,
                    searchQuery = auditSearchQuery,
                    onSearchChange = { auditSearchQuery = it },
                    selectedActionFilter = selectedActionFilter,
                    onSelectActionFilter = { selectedActionFilter = it },
                    selectedStatusFilter = selectedStatusFilter,
                    onSelectStatusFilter = { selectedStatusFilter = it }
                )
            }
            2 -> {
                TopologyTab(
                    currentRole = currentRole,
                    architectureStatus = architectureStatus,
                    onOpenRoleSelector = onOpenRoleSelector
                )
            }
        }
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Reset Role Permissions to System Defaults?", fontWeight = FontWeight.Bold, color = DarkNavyBlue) },
            text = {
                Text(
                    "This will restore all default permission policies for Super Admin, Admin, Finance Manager, Collection Supervisor, and Collection Employee according to enterprise specifications. All custom rule overrides will be reverted.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetDefaults()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OverdueRed, contentColor = Color.White)
                ) {
                    Text("Confirm Reset", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun TopologyTab(
    currentRole: UserRole,
    architectureStatus: Map<String, String>,
    onOpenRoleSelector: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Active Role Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundWhite)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "CURRENT ACTIVE PERSONA", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = currentRole.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DarkNavyBlue)
                        Text(text = currentRole.badgeLabel, fontSize = 12.sp, color = RoyalBlue)
                    }

                    Button(
                        onClick = onOpenRoleSelector,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.testTag("settings_switch_role_btn")
                    ) {
                        Text("Switch Persona", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // System Architecture Info Card
        item {
            Text(
                text = "STACK & ARCHITECTURE TOPOLOGY",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextMuted
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundWhite)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ArchRow("Frontend Client", architectureStatus["frontend"] ?: "Jetpack Compose M3 (Multiplatform Ready)")
                    ArchRow("Backend Service", architectureStatus["backend"] ?: "REST API (Role-Based Access Enforcement)")
                    ArchRow("Primary Database", architectureStatus["database"] ?: "PostgreSQL (Schema: public.customers, loans, collections)")
                    ArchRow("RBAC Security Engine", architectureStatus["rbac_engine"] ?: "Active (VIEW, CREATE, UPDATE, APPROVE, REVERSE)", isSuccess = true)
                    ArchRow("Audit Logging Engine", architectureStatus["audit_logging"] ?: "Active (Immutable Security Ledger)", isSuccess = true)
                    ArchRow("Identity / Auth", architectureStatus["auth"] ?: "Multi-Tenant Session & Salted Cryptographic Hashing")
                    ArchRow("Health Check", architectureStatus["api_status"] ?: "Healthy (200 OK)", isSuccess = true)
                    ArchRow("Database Latency", architectureStatus["db_latency"] ?: "21 ms")
                    ArchRow("Offline Sync Layer", architectureStatus["offline_mode"] ?: "Active (Field Collector Sync)")
                }
            }
        }

        item {
            Text(
                text = "CORE FINANCIAL POLICIES & ENFORCEMENT",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextMuted
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundWhite)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PolicyRow("Overdue Grace Period", "5 Days")
                    PolicyRow("Late Payment Penalty", "2.0% Monthly Flat")
                    PolicyRow("NPA Classification", "Overdue > 90 Days")
                    PolicyRow("Payment Clearance Authority", "Supervisor / Finance Manager (APPROVE)")
                    PolicyRow("Payment Reversal Authority", "Finance Manager & Super Admin (REVERSE)")
                    PolicyRow("Field Collector Restriction", "Assigned Customers Only (VIEW & CREATE)")
                }
            }
        }
    }
}

@Composable
private fun ArchRow(label: String, value: String, isSuccess: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = if (isSuccess) SuccessGreenDark else TextPrimary
        )
    }
}

@Composable
private fun PolicyRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

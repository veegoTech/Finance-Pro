package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.data.models.*
import com.example.ui.components.FinTechStatusPill
import com.example.ui.components.QuadColor
import com.example.ui.components.getModuleIcon
import com.example.ui.theme.*

@Composable
fun PermissionManagementScreen(
    currentRole: UserRole,
    permissionsMatrix: Map<Pair<UserRole, AppModule>, Set<PermissionAction>>,
    auditLogs: List<AuditLogEntry>,
    onTogglePermission: (role: UserRole, module: AppModule, action: PermissionAction, enabled: Boolean) -> Unit,
    onResetDefaults: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Granular Permissions, 1: Privileged Audit Logs
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
        // Top Header
        Surface(
            color = BackgroundWhite,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkNavyBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Permission & Audit Governance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavyBlue
                            )
                        }
                        Text(
                            text = "Granular Access Control: View, Create, Update, Approve, Reverse",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    if (isSuperAdmin) {
                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkNavyBlue),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
                            modifier = Modifier.testTag("reset_permissions_btn")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Defaults", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs
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
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Granular Permissions", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HistoryEdu, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Audit Logs (${auditLogs.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    )
                }
            }
        }

        // Main Tab Content
        if (selectedTab == 0) {
            PermissionsMatrixTab(
                inspectedRole = inspectedRole,
                onSelectRole = { inspectedRole = it },
                isSuperAdmin = isSuperAdmin,
                permissionsMatrix = permissionsMatrix,
                onTogglePermission = onTogglePermission
            )
        } else {
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
fun PermissionsMatrixTab(
    inspectedRole: UserRole,
    onSelectRole: (UserRole) -> Unit,
    isSuperAdmin: Boolean,
    permissionsMatrix: Map<Pair<UserRole, AppModule>, Set<PermissionAction>>,
    onTogglePermission: (role: UserRole, module: AppModule, action: PermissionAction, enabled: Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Role Selector Chips
        item {
            Column {
                Text(
                    text = "SELECT ROLE TO CONFIGURE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(UserRole.values()) { role ->
                        val isSelected = role == inspectedRole
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectRole(role) },
                            label = { Text(role.badgeLabel, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) RoyalBlue else TextMuted)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalBlueLight,
                                selectedLabelColor = RoyalBlueDark
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) RoyalBlue else BorderSubtle
                            )
                        )
                    }
                }
            }
        }

        // Role Policy Summary Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BackgroundWhite)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = inspectedRole.badgeLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkNavyBlue
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                FinTechStatusPill(
                                    label = "Level ${inspectedRole.accessLevel}",
                                    dotColor = RoyalBlue,
                                    bgColor = RoyalBlueLight,
                                    textColor = RoyalBlueDark,
                                    borderColor = RoyalBlueBorder
                                )
                            }
                            Text(
                                text = inspectedRole.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Action legend description
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ActionLegendPill("VIEW", "Read records", Color(0xFF1E40AF), Color(0xFFEFF6FF))
                        ActionLegendPill("CREATE", "Add new data", SuccessGreenDark, SuccessGreenLight)
                        ActionLegendPill("UPDATE", "Edit & modify", DueOrangeDark, DueOrangeLight)
                        ActionLegendPill("APPROVE", "Verify & audit", Color(0xFF0369A1), Color(0xFFE0F2FE))
                        ActionLegendPill("REVERSE", "Void & undo", Color(0xFF6D28D9), Color(0xFFF5F3FF))
                    }
                }
            }
        }

        if (!isSuperAdmin) {
            item {
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = DueOrangeDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Read-Only View: Only Super Admin has authority to modify granular role permission policies.",
                            fontSize = 12.sp,
                            color = Color(0xFF92400E)
                        )
                    }
                }
            }
        }

        // Module Granular Cards
        items(AppModule.values()) { module ->
            val actions = permissionsMatrix[inspectedRole to module] ?: emptySet()
            ModulePermissionCard(
                role = inspectedRole,
                module = module,
                activeActions = actions,
                isEditable = isSuperAdmin,
                onToggleAction = { action, enabled ->
                    onTogglePermission(inspectedRole, module, action, enabled)
                }
            )
        }
    }
}

@Composable
fun ActionLegendPill(label: String, desc: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun ModulePermissionCard(
    role: UserRole,
    module: AppModule,
    activeActions: Set<PermissionAction>,
    isEditable: Boolean,
    onToggleAction: (PermissionAction, Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkNavyBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getModuleIcon(module),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = module.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavyBlue
                        )
                        Text(
                            text = "Category: ${module.category}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                val hasAnyAccess = activeActions.isNotEmpty()
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (hasAnyAccess) SuccessGreenLight else Color(0xFFF1F5F9))
                        .border(1.dp, if (hasAnyAccess) SuccessGreenBorder else BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (hasAnyAccess) "${activeActions.size} Actions Active" else "No Access",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasAnyAccess) SuccessGreenDark else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(12.dp))

            // Action switches / chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PermissionAction.values().forEach { action ->
                    val isGranted = activeActions.contains(action)
                    ActionPermissionSwitch(
                        action = action,
                        isGranted = isGranted,
                        isEditable = isEditable,
                        onToggle = { onToggleAction(action, !isGranted) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActionPermissionSwitch(
    action: PermissionAction,
    isGranted: Boolean,
    isEditable: Boolean,
    onToggle: () -> Unit
) {
    val (activeText, activeBg, activeBorder) = when (action) {
        PermissionAction.VIEW -> Triple(Color(0xFF1E40AF), Color(0xFFEFF6FF), Color(0xFFBFDBFE))
        PermissionAction.CREATE -> Triple(SuccessGreenDark, SuccessGreenLight, SuccessGreenBorder)
        PermissionAction.UPDATE -> Triple(DueOrangeDark, DueOrangeLight, DueOrangeBorder)
        PermissionAction.APPROVE -> Triple(Color(0xFF0369A1), Color(0xFFE0F2FE), Color(0xFFBAE6FD))
        PermissionAction.REVERSE -> Triple(Color(0xFF6D28D9), Color(0xFFF5F3FF), Color(0xFFDDD6FE))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = isEditable) { onToggle() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isGranted) activeBg else Color(0xFFF8FAFC))
                .border(1.dp, if (isGranted) activeBorder else BorderSubtle, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = action.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isGranted) activeText else TextMuted
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isEditable) {
            Switch(
                checked = isGranted,
                onCheckedChange = { onToggle() },
                modifier = Modifier
                    .scale(0.7f)
                    .height(24.dp),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = activeText,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFCBD5E1)
                )
            )
        } else {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (isGranted) activeText else TextMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.size((48 * scale).dp, (24 * scale).dp)
)

@Composable
fun AuditLogsTab(
    auditLogs: List<AuditLogEntry>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedActionFilter: PermissionAction?,
    onSelectActionFilter: (PermissionAction?) -> Unit,
    selectedStatusFilter: AuditStatus?,
    onSelectStatusFilter: (AuditStatus?) -> Unit
) {
    val filteredLogs = remember(auditLogs, searchQuery, selectedActionFilter, selectedStatusFilter) {
        auditLogs.filter { log ->
            val matchesSearch = searchQuery.isBlank() ||
                log.actorName.contains(searchQuery, ignoreCase = true) ||
                log.actorId.contains(searchQuery, ignoreCase = true) ||
                log.entityId.contains(searchQuery, ignoreCase = true) ||
                log.details.contains(searchQuery, ignoreCase = true) ||
                log.module.title.contains(searchQuery, ignoreCase = true)

            val matchesAction = selectedActionFilter == null || log.action == selectedActionFilter
            val matchesStatus = selectedStatusFilter == null || log.status == selectedStatusFilter

            matchesSearch && matchesAction && matchesStatus
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Search & Filter Box
        item {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search actor ID, entity, action, or audit notes...", color = TextMuted) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BackgroundWhite,
                        unfocusedContainerColor = BackgroundWhite,
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Action Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedActionFilter == null,
                            onClick = { onSelectActionFilter(null) },
                            label = { Text("All Actions") }
                        )
                    }
                    items(PermissionAction.values()) { action ->
                        FilterChip(
                            selected = selectedActionFilter == action,
                            onClick = { onSelectActionFilter(if (selectedActionFilter == action) null else action) },
                            label = { Text(action.name) }
                        )
                    }
                }
            }
        }

        // Audit Summary Stats Card
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AuditMetricChip("Audited Events", auditLogs.size.toString(), RoyalBlue, Modifier.weight(1f))
                AuditMetricChip("Approvals", auditLogs.count { it.action == PermissionAction.APPROVE }.toString(), SuccessGreen, Modifier.weight(1f))
                AuditMetricChip("Reversals", auditLogs.count { it.action == PermissionAction.REVERSE }.toString(), Color(0xFF7C3AED), Modifier.weight(1f))
                AuditMetricChip("Denied Blocks", auditLogs.count { it.status == AuditStatus.DENIED }.toString(), OverdueRed, Modifier.weight(1f))
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BackgroundWhite)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No audit log events match the active search criteria.", color = TextMuted)
                }
            }
        } else {
            items(filteredLogs) { log ->
                AuditLogCard(log = log)
            }
        }
    }
}

@Composable
fun AuditMetricChip(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(text = title, fontSize = 10.sp, color = TextMuted, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = count, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun AuditLogCard(log: AuditLogEntry) {
    val (actionDot, actionBg, actionText, actionBorder) = when (log.action) {
        PermissionAction.VIEW -> QuadColor(Color(0xFF1E40AF), Color(0xFFEFF6FF), Color(0xFF1E40AF), Color(0xFFBFDBFE))
        PermissionAction.CREATE -> QuadColor(SuccessGreen, SuccessGreenLight, SuccessGreenDark, SuccessGreenBorder)
        PermissionAction.UPDATE -> QuadColor(DueOrange, DueOrangeLight, DueOrangeDark, DueOrangeBorder)
        PermissionAction.APPROVE -> QuadColor(Color(0xFF0284C7), Color(0xFFE0F2FE), Color(0xFF0369A1), Color(0xFFBAE6FD))
        PermissionAction.REVERSE -> QuadColor(Color(0xFF7C3AED), Color(0xFFF5F3FF), Color(0xFF6D28D9), Color(0xFFDDD6FE))
    }

    val statusPillColor = when (log.status) {
        AuditStatus.SUCCESS -> QuadColor(SuccessGreen, SuccessGreenLight, SuccessGreenDark, SuccessGreenBorder)
        AuditStatus.DENIED -> QuadColor(OverdueRed, OverdueRedLight, OverdueRedDark, OverdueRedBorder)
        AuditStatus.REVERSED -> QuadColor(Color(0xFF7C3AED), Color(0xFFF5F3FF), Color(0xFF6D28D9), Color(0xFFDDD6FE))
        AuditStatus.FLAGGED -> QuadColor(DueOrange, DueOrangeLight, DueOrangeDark, DueOrangeBorder)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundWhite)
            .border(1.dp, if (log.status == AuditStatus.DENIED) OverdueRedBorder else BorderSubtle, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DarkNavyBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = log.actorName.take(2).uppercase(),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = log.actorName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavyBlue
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = log.actorId,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = RoyalBlue
                            )
                            Text(text = " • ", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = log.actorRole.badgeLabel,
                                fontSize = 10.sp,
                                color = TextMuted
                            )
                        }
                    }
                }

                FinTechStatusPill(
                    label = log.status.label,
                    dotColor = statusPillColor.dot,
                    bgColor = statusPillColor.bg,
                    textColor = statusPillColor.text,
                    borderColor = statusPillColor.border
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action & Module badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FinTechStatusPill(
                    label = log.action.name,
                    dotColor = actionDot,
                    bgColor = actionBg,
                    textColor = actionText,
                    borderColor = actionBorder
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${log.module.shortTitle} / ${log.entityType}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = log.formattedTime,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details text
            Text(
                text = log.details,
                style = MaterialTheme.typography.bodySmall,
                color = DarkNavyBlue,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Gateway & Entity ID footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ref: ${log.entityId}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextMuted
                )
                Text(
                    text = log.ipAddress,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.AppModule
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.screens.auth.ForgotPasswordDialog
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                FinanceProApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceProApp(
    viewModel: FinanceViewModel = viewModel()
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()
    val currentSession by viewModel.currentSession.collectAsStateWithLifecycle()
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentModule by viewModel.currentModule.collectAsStateWithLifecycle()
    val dashboardMetrics by viewModel.dashboardMetrics.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val emis by viewModel.emis.collectAsStateWithLifecycle()
    val pendingDues by viewModel.pendingDues.collectAsStateWithLifecycle()
    val agingBuckets by viewModel.agingBuckets.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val branchPerformances by viewModel.branchPerformances.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val architectureStatus by viewModel.architectureStatus.collectAsStateWithLifecycle()
    val permissionsMatrix by viewModel.permissionsMatrix.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val showRoleSelector by viewModel.showRoleSelector.collectAsStateWithLifecycle()
    val showCollectDialog by viewModel.showCollectDialog.collectAsStateWithLifecycle()
    val activeReceipt by viewModel.activeReceiptPreview.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    // Forgot Password State
    val resetStep by viewModel.resetStep.collectAsStateWithLifecycle()
    val resetEmail by viewModel.resetEmail.collectAsStateWithLifecycle()
    val demoOtp by viewModel.generatedOtpForDemo.collectAsStateWithLifecycle()
    val demoAccounts by viewModel.demoCredentials.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // 1. UNPROTECTED / AUTHENTICATION GATE
    if (!isAuthenticated) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            LoginScreen(
                onLogin = { id, pass ->
                    viewModel.login(id, pass) { success, _ -> }
                },
                onForgotPasswordClick = {
                    viewModel.startForgotPassword()
                },
                demoAccounts = demoAccounts,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }

        if (resetStep != FinanceViewModel.ResetStep.NONE) {
            ForgotPasswordDialog(
                currentStep = resetStep,
                initialEmail = resetEmail,
                demoOtp = demoOtp,
                onRequestOtp = { email -> viewModel.requestPasswordResetOtp(email) { _, _ -> } },
                onVerifyOtp = { otp -> viewModel.verifyResetOtp(otp) { _, _ -> } },
                onSubmitNewPassword = { otp, newPass -> viewModel.submitNewPassword(otp, newPass) { _, _ -> } },
                onDismiss = { viewModel.cancelForgotPassword() }
            )
        }
        return
    }

    // 2. PROTECTED ROUTES & SYSTEM SESSION
    BackHandler(enabled = currentModule != AppModule.DASHBOARD) {
        viewModel.selectModule(AppModule.DASHBOARD)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            FinanceAppDrawerContent(
                currentSession = currentSession,
                currentModule = currentModule,
                currentRole = currentRole,
                onSelectModule = { module ->
                    viewModel.selectModule(module)
                },
                onCloseDrawer = {
                    scope.launch { drawerState.close() }
                },
                onOpenRoleSelector = {
                    scope.launch { drawerState.close() }
                    viewModel.openRoleSelector()
                },
                onLogoutClick = {
                    viewModel.logout()
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                FinanceProHeader(
                    currentSession = currentSession,
                    currentRole = currentRole,
                    unreadNotificationsCount = notifications.count { !it.isRead },
                    onOpenRoleSelector = { viewModel.openRoleSelector() },
                    onOpenMenu = { scope.launch { drawerState.open() } },
                    onNotificationsClick = { viewModel.selectModule(AppModule.NOTIFICATIONS) },
                    onRefresh = { viewModel.refresh() },
                    onLogoutClick = { viewModel.logout() }
                )
            },
            bottomBar = {
                FinanceBottomNav(
                    currentModule = currentModule,
                    currentRole = currentRole,
                    onSelectModule = { module ->
                        viewModel.selectModule(module)
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Role-Based Access Control Gate for Active Module
                if (!viewModel.canAccess(currentModule)) {
                    AccessDeniedView(
                        module = currentModule,
                        role = currentRole,
                        onReturnToDashboard = { viewModel.selectModule(AppModule.DASHBOARD) }
                    )
                } else {
                    when (currentModule) {
                        AppModule.DASHBOARD -> {
                            DashboardScreen(
                                currentRole = currentRole,
                                metrics = dashboardMetrics,
                                collections = collections,
                                assignments = assignments,
                                loans = loans,
                                employees = employees,
                                branchPerformances = branchPerformances,
                                agingBuckets = agingBuckets,
                                onOpenCollect = { viewModel.openCollectDialog() },
                                onNavigateModule = { module -> viewModel.selectModule(module) },
                                onViewReceipt = { record -> viewModel.showReceipt(record) }
                            )
                        }

                        AppModule.CUSTOMER_MANAGEMENT -> {
                            CustomerManagementScreen(
                                customers = customers,
                                searchQuery = searchQuery,
                                onSearchChanged = { viewModel.onSearchQueryChanged(it) },
                                currentRole = currentRole
                            )
                        }

                        AppModule.LOAN_MANAGEMENT -> {
                            LoanManagementScreen(
                                loans = loans,
                                onCollectForLoan = { loan -> viewModel.openCollectDialog(targetLoan = loan) }
                            )
                        }

                        AppModule.EMI_MANAGEMENT -> {
                            EMIManagementScreen(emis = emis)
                        }

                        AppModule.PENDING_DUES -> {
                            PendingDuesScreen(
                                agingBuckets = agingBuckets,
                                pendingDues = pendingDues,
                                onCollectForDue = { due ->
                                    val targetLoan = loans.find { it.id == due.loanId }
                                    viewModel.openCollectDialog(targetLoan = targetLoan)
                                }
                            )
                        }

                        AppModule.EMPLOYEE_MANAGEMENT -> {
                            EmployeeManagementScreen(employees = employees)
                        }

                        AppModule.EMPLOYEE_ASSIGNMENTS -> {
                            EmployeeAssignmentsScreen(
                                assignments = assignments,
                                onCollectForAssignment = { assignment ->
                                    val targetLoan = loans.find { it.id == assignment.loanId }
                                    viewModel.openCollectDialog(targetLoan = targetLoan)
                                }
                            )
                        }

                        AppModule.LOAN_COLLECTION -> {
                            LoanCollectionScreen(
                                collections = collections,
                                onOpenCollectDialog = { viewModel.openCollectDialog() },
                                onViewReceipt = { record -> viewModel.showReceipt(record) }
                            )
                        }

                        AppModule.PAYMENT_VERIFICATION -> {
                            PaymentVerificationScreen(
                                collections = collections,
                                canApprove = viewModel.canPerform(AppModule.PAYMENT_VERIFICATION, com.example.data.models.PermissionAction.APPROVE),
                                canReverse = viewModel.canPerform(AppModule.PAYMENT_VERIFICATION, com.example.data.models.PermissionAction.REVERSE),
                                onVerifyPayment = { id, approved, note ->
                                    viewModel.verifyPayment(id, approved, note)
                                },
                                onReversePayment = { id, reason ->
                                    viewModel.reversePayment(id, reason)
                                }
                            )
                        }

                        AppModule.REPORTS -> {
                            ReportsScreen(
                                metrics = dashboardMetrics,
                                branches = branchPerformances
                            )
                        }

                        AppModule.NOTIFICATIONS -> {
                            NotificationsScreen(
                                notifications = notifications,
                                onMarkAsRead = { id -> viewModel.markNotificationRead(id) }
                            )
                        }

                        AppModule.SETTINGS -> {
                            SettingsScreen(
                                currentRole = currentRole,
                                permissionsMatrix = permissionsMatrix,
                                auditLogs = auditLogs,
                                architectureStatus = architectureStatus,
                                onTogglePermission = { role, module, action, enabled ->
                                    viewModel.updatePermission(role, module, action, enabled)
                                },
                                onResetDefaults = { viewModel.resetPermissionsToDefaults() },
                                onOpenRoleSelector = { viewModel.openRoleSelector() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Role Switcher Modal
    if (showRoleSelector) {
        RoleSelectorModal(
            currentRole = currentRole,
            onRoleSelected = { role -> viewModel.selectRole(role) },
            onDismiss = { viewModel.closeRoleSelector() }
        )
    }

    // Record Collection Dialog
    if (showCollectDialog) {
        RecordCollectionDialog(
            loans = loans,
            customers = customers,
            currentRole = currentRole,
            onConfirm = { customerId, loanId, amount, mode, collector, note ->
                viewModel.recordCollection(customerId, loanId, amount, mode, collector, note)
            },
            onDismiss = { viewModel.closeCollectDialog() }
        )
    }

    // Official Receipt Modal
    activeReceipt?.let { record ->
        CollectionReceiptDialog(
            record = record,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }
}

@Composable
fun AccessDeniedView(
    module: AppModule,
    role: com.example.data.models.UserRole,
    onReturnToDashboard: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLightGrey)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundWhite,
            border = androidx.compose.foundation.BorderStroke(1.dp, OverdueRedBorder),
            modifier = Modifier.fillMaxWidth().widthIn(max = 440.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(OverdueRedLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Access Denied",
                        tint = OverdueRedDark,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Access Restricted by RBAC Policy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavyBlue
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your active persona (${role.badgeLabel}) does not hold permission to access ${module.title}. Access is logged in the security audit ledger.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onReturnToDashboard,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Return to Authorized Dashboard")
                }
            }
        }
    }
}

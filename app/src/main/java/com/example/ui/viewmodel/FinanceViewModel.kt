package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.*
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(
    private val repository: FinanceRepository = FinanceRepository()
) : ViewModel() {

    val currentSession: StateFlow<AuthSession?> = repository.currentSession
    val isAuthenticated: StateFlow<Boolean> = repository.currentSession
        .map { it != null && !it.isExpired }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentRole: StateFlow<UserRole> = repository.currentRole
    val currentModule: StateFlow<AppModule> = repository.currentModule
    val dashboardMetrics: StateFlow<DashboardMetrics?> = repository.dashboardMetrics
    val customers: StateFlow<List<Customer>> = repository.customers
    val loans: StateFlow<List<Loan>> = repository.loans
    val emis: StateFlow<List<EMIItem>> = repository.emis
    val pendingDues: StateFlow<List<Assignment>> = repository.pendingDues
    val agingBuckets: StateFlow<List<AgingBucket>> = repository.agingBuckets
    val employees: StateFlow<List<Employee>> = repository.employees
    val assignments: StateFlow<List<Assignment>> = repository.assignments
    val collections: StateFlow<List<CollectionRecord>> = repository.collections
    val branchPerformances: StateFlow<List<BranchPerformance>> = repository.branchPerformances
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
    val permissionsMatrix: StateFlow<Map<Pair<UserRole, AppModule>, Set<PermissionAction>>> = repository.permissionsMatrix
    val auditLogs: StateFlow<List<AuditLogEntry>> = repository.auditLogs
    val architectureStatus: StateFlow<Map<String, String>> = repository.architectureStatus
    val isLoading: StateFlow<Boolean> = repository.isLoading

    // UI Dialog & Filter States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _selectedLoan = MutableStateFlow<Loan?>(null)
    val selectedLoan: StateFlow<Loan?> = _selectedLoan.asStateFlow()

    private val _activeReceiptPreview = MutableStateFlow<CollectionRecord?>(null)
    val activeReceiptPreview: StateFlow<CollectionRecord?> = _activeReceiptPreview.asStateFlow()

    private val _showRoleSelector = MutableStateFlow(false)
    val showRoleSelector: StateFlow<Boolean> = _showRoleSelector.asStateFlow()

    private val _showCollectDialog = MutableStateFlow(false)
    val showCollectDialog: StateFlow<Boolean> = _showCollectDialog.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Forgot Password Flow State
    private val _resetStep = MutableStateFlow<ResetStep>(ResetStep.NONE)
    val resetStep: StateFlow<ResetStep> = _resetStep.asStateFlow()

    private val _resetEmail = MutableStateFlow("")
    val resetEmail: StateFlow<String> = _resetEmail.asStateFlow()

    private val _generatedOtpForDemo = MutableStateFlow<String?>(null)
    val generatedOtpForDemo: StateFlow<String?> = _generatedOtpForDemo.asStateFlow()

    private val _demoCredentials = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val demoCredentials: StateFlow<List<Pair<String, String>>> = _demoCredentials.asStateFlow()

    enum class ResetStep {
        NONE, REQUEST_OTP, VERIFY_OTP, SET_NEW_PASSWORD
    }

    init {
        viewModelScope.launch {
            _demoCredentials.value = repository.getDemoCredentials()
            // Optional: Auto-login as Super Admin for immediate testing if desired, or stay on login screen
            // repository.login("EMP-SA-001", "Password@123")
        }
    }

    fun login(identifier: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = repository.login(identifier, password)
            result.onSuccess { session ->
                _statusMessage.value = "Welcome back, ${session.user.fullName} (${session.user.role.badgeLabel})"
                onResult(true, null)
            }.onFailure { err ->
                _statusMessage.value = err.message ?: "Authentication failed."
                onResult(false, err.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _statusMessage.value = "Successfully logged out. Session terminated."
        }
    }

    fun startForgotPassword() {
        _resetStep.value = ResetStep.REQUEST_OTP
    }

    fun cancelForgotPassword() {
        _resetStep.value = ResetStep.NONE
        _resetEmail.value = ""
        _generatedOtpForDemo.value = null
    }

    fun requestPasswordResetOtp(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.requestPasswordReset(email)
            res.onSuccess { otp ->
                _resetEmail.value = email
                _generatedOtpForDemo.value = otp
                _resetStep.value = ResetStep.VERIFY_OTP
                _statusMessage.value = "Security OTP generated: $otp (Valid for 15 mins)"
                onResult(true, otp)
            }.onFailure { err ->
                _statusMessage.value = err.message
                onResult(false, err.message)
            }
        }
    }

    fun verifyResetOtp(otp: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.verifyResetOtp(_resetEmail.value, otp)
            res.onSuccess {
                _resetStep.value = ResetStep.SET_NEW_PASSWORD
                onResult(true, null)
            }.onFailure { err ->
                _statusMessage.value = err.message
                onResult(false, err.message)
            }
        }
    }

    fun submitNewPassword(otp: String, newPass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = repository.resetPassword(_resetEmail.value, otp, newPass)
            res.onSuccess {
                _resetStep.value = ResetStep.NONE
                _resetEmail.value = ""
                _generatedOtpForDemo.value = null
                _statusMessage.value = "Password successfully reset! You can now log in with your new password."
                onResult(true, null)
            }.onFailure { err ->
                _statusMessage.value = err.message
                onResult(false, err.message)
            }
        }
    }

    fun canAccess(module: AppModule): Boolean = repository.canAccess(module)

    fun selectRole(role: UserRole) {
        repository.setRole(role)
        _showRoleSelector.value = false
        _statusMessage.value = "Switched to ${role.title}"
    }

    fun selectModule(module: AppModule) {
        repository.setModule(module)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            repository.searchCustomers(query)
        }
    }

    fun openRoleSelector() {
        _showRoleSelector.value = true
    }

    fun closeRoleSelector() {
        _showRoleSelector.value = false
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun selectLoan(loan: Loan?) {
        _selectedLoan.value = loan
    }

    fun openCollectDialog(targetLoan: Loan? = null, targetCustomer: Customer? = null) {
        if (targetLoan != null) _selectedLoan.value = targetLoan
        if (targetCustomer != null) _selectedCustomer.value = targetCustomer
        _showCollectDialog.value = true
    }

    fun closeCollectDialog() {
        _showCollectDialog.value = false
    }

    fun showReceipt(record: CollectionRecord) {
        _activeReceiptPreview.value = record
    }

    fun dismissReceipt() {
        _activeReceiptPreview.value = null
    }

    fun recordCollection(
        customerId: String,
        loanId: String,
        amount: Double,
        mode: PaymentMode,
        collectedBy: String,
        note: String
    ) {
        viewModelScope.launch {
            val result = repository.recordCollection(customerId, loanId, amount, mode, collectedBy, note)
            result.onSuccess { record ->
                _showCollectDialog.value = false
                _activeReceiptPreview.value = record
                _statusMessage.value = "Payment of $${amount} collected! Receipt ${record.receiptNumber} issued."
            }.onFailure { err ->
                _statusMessage.value = "Access Error: ${err.message}"
            }
        }
    }

    fun verifyPayment(receiptId: String, approved: Boolean, note: String) {
        viewModelScope.launch {
            val user = currentSession.value?.user
            val auditorName = if (user != null) "${user.fullName} (${user.role.badgeLabel})" else "Supervisor"
            val result = repository.verifyPayment(receiptId, approved, auditorName, note)
            result.onSuccess {
                _statusMessage.value = if (approved) "Payment verified and credited to ledger." else "Payment rejected."
            }.onFailure {
                _statusMessage.value = "Verification error: ${it.message}"
            }
        }
    }

    fun reversePayment(receiptId: String, reason: String) {
        viewModelScope.launch {
            val result = repository.reversePayment(receiptId, reason)
            result.onSuccess { record ->
                _statusMessage.value = "Transaction reversed. Receipt ${record.receiptNumber} voided and balance restored."
            }.onFailure { err ->
                _statusMessage.value = "Reversal error: ${err.message}"
            }
        }
    }

    fun canPerform(module: AppModule, action: PermissionAction): Boolean {
        return repository.canPerform(module, action)
    }

    fun canPerformForRole(role: UserRole, module: AppModule, action: PermissionAction): Boolean {
        return repository.canPerformForRole(role, module, action)
    }

    fun updatePermission(role: UserRole, module: AppModule, action: PermissionAction, enabled: Boolean) {
        viewModelScope.launch {
            val result = repository.updatePermission(role, module, action, enabled)
            result.onSuccess {
                _statusMessage.value = "Permission policy updated for ${role.badgeLabel} on ${module.shortTitle}."
            }.onFailure { err ->
                _statusMessage.value = "Permission error: ${err.message}"
            }
        }
    }

    fun resetPermissionsToDefaults() {
        viewModelScope.launch {
            val result = repository.resetPermissionsToDefaults()
            result.onSuccess {
                _statusMessage.value = "All system role permissions reset to defaults."
            }.onFailure { err ->
                _statusMessage.value = "Reset error: ${err.message}"
            }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refreshAll()
            _statusMessage.value = "Database records synchronized"
        }
    }
}

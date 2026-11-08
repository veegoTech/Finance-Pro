package com.example.data.models

import java.text.NumberFormat
import java.util.Locale

enum class LoanStatus(val label: String) {
    ACTIVE("Active"),
    PENDING_APPROVAL("Pending Approval"),
    DISBURSED("Disbursed"),
    CLOSED("Closed"),
    DEFAULTED("Defaulted / NPA")
}

enum class EMIStatus(val label: String) {
    PAID("Paid"),
    PENDING("Upcoming"),
    OVERDUE("Overdue")
}

enum class PaymentMode(val label: String) {
    CASH("Cash"),
    UPI("UPI / QR Code"),
    CHEQUE("Cheque"),
    BANK_TRANSFER("Bank NEFT/IMPS")
}

enum class ReceiptStatus(val label: String) {
    VERIFIED("Verified"),
    PENDING_VERIFICATION("Pending Audit"),
    REJECTED("Rejected"),
    REVERSED("Reversed")
}

data class Customer(
    val id: String,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val nationalId: String,
    val kycStatus: String, // "Verified", "Pending", "Under Review"
    val creditScore: Int,
    val activeLoansCount: Int,
    val totalBorrowed: Double,
    val totalOutstanding: Double,
    val avatarInitial: String = name.take(2).uppercase()
)

data class Loan(
    val id: String,
    val loanNumber: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val principalAmount: Double,
    val interestRate: Double, // annual rate in % e.g. 14.5
    val tenureMonths: Int,
    val emiAmount: Double,
    val disbursedDate: String,
    val status: LoanStatus,
    val totalPaid: Double,
    val remainingBalance: Double,
    val nextDueDate: String,
    val assignedAgentName: String,
    val loanType: String = "Personal / SME Loan"
)

data class EMIItem(
    val id: String,
    val loanId: String,
    val loanNumber: String,
    val customerName: String,
    val installmentNumber: Int,
    val totalInstallments: Int,
    val dueDate: String,
    val amount: Double,
    val principalComponent: Double,
    val interestComponent: Double,
    val penaltyFee: Double = 0.0,
    val status: EMIStatus,
    val paidDate: String? = null,
    val paymentMode: PaymentMode? = null,
    val collectedByAgent: String? = null
)

data class Employee(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val branch: String,
    val activeAssignmentsCount: Int,
    val monthlyTarget: Double,
    val monthlyCollected: Double,
    val status: String = "Active"
) {
    val completionPercentage: Int
        get() = if (monthlyTarget > 0) ((monthlyCollected / monthlyTarget) * 100).toInt().coerceIn(0, 100) else 0
}

data class Assignment(
    val id: String,
    val employeeId: String,
    val employeeName: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val loanId: String,
    val loanNumber: String,
    val dueAmount: Double,
    val priority: String, // "Urgent", "High", "Medium", "Standard"
    val daysOverdue: Int,
    val assignedDate: String,
    val status: String = "Pending Visit" // "Pending Visit", "Collected", "Promise to Pay"
)

data class CollectionRecord(
    val id: String,
    val receiptNumber: String,
    val loanId: String,
    val loanNumber: String,
    val customerId: String,
    val customerName: String,
    val customerPhone: String,
    val emiId: String,
    val amountPaid: Double,
    val paymentMode: PaymentMode,
    val collectedBy: String,
    val collectedAt: String,
    val status: ReceiptStatus,
    val verifiedBy: String? = null,
    val verifiedAt: String? = null,
    val auditNote: String = "Standard field collection entry"
)

data class AgingBucket(
    val bucketName: String,
    val daysRange: String,
    val count: Int,
    val totalAmount: Double,
    val riskLevel: String // "Low", "Moderate", "Critical", "NPA"
)

data class BranchPerformance(
    val branchName: String,
    val totalDisbursed: Double,
    val totalCollected: Double,
    val recoveryRate: Double,
    val activeLoans: Int
)

data class DashboardMetrics(
    val totalDisbursed: Double,
    val totalCollected: Double,
    val totalOutstanding: Double,
    val recoveryRate: Double,
    val activeLoansCount: Int,
    val pendingDuesCount: Int,
    val npaRate: Double,
    val dailyCollectionTarget: Double,
    val dailyCollectedSoFar: Double,
    val pendingVerificationsCount: Int,
    val activeAgentsCount: Int
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val category: String, // "Due Alert", "Verification", "Risk", "System"
    val isRead: Boolean = false,
    val priority: String = "Normal"
)

fun Double.toCurrencyString(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    format.maximumFractionDigits = 0
    return format.format(this).replace("$", "$ ")
}

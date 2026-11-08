package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Loan
import com.example.data.models.LoanStatus
import com.example.data.models.toCurrencyString
import com.example.ui.components.LoanStatusBadge
import com.example.ui.theme.*

@Composable
fun LoanManagementScreen(
    loans: List<Loan>,
    onCollectForLoan: (Loan) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<LoanStatus?>(null) }
    var selectedLoanForDetail by remember { mutableStateOf<Loan?>(null) }

    val filteredLoans = remember(loans, selectedFilter) {
        if (selectedFilter == null) loans else loans.filter { it.status == selectedFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLightGrey)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header Title & Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Loan Portfolio Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavyBlue
                )
                Text(
                    text = "Total Principal: ${loans.sumOf { it.principalAmount }.toCurrencyString()} across ${loans.size} accounts",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text("All (${loans.size})", fontWeight = if (selectedFilter == null) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = RoyalBlueSubtle,
                    selectedLabelColor = RoyalBlue
                )
            )
            LoanStatus.values().forEach { status ->
                val count = loans.count { it.status == status }
                FilterChip(
                    selected = selectedFilter == status,
                    onClick = { selectedFilter = status },
                    label = { Text("${status.label} ($count)", fontWeight = if (selectedFilter == status) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = RoyalBlueSubtle,
                        selectedLabelColor = RoyalBlue
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredLoans) { loan ->
                LoanCard(
                    loan = loan,
                    onClick = { selectedLoanForDetail = loan },
                    onCollect = { onCollectForLoan(loan) }
                )
            }
        }
    }

    selectedLoanForDetail?.let { loan ->
        LoanDetailDialog(
            loan = loan,
            onDismiss = { selectedLoanForDetail = null },
            onCollect = {
                selectedLoanForDetail = null
                onCollectForLoan(loan)
            }
        )
    }
}

@Composable
fun LoanCard(
    loan: Loan,
    onClick: () -> Unit,
    onCollect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("loan_card_${loan.id}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = loan.customerName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = "${loan.loanNumber} • ${loan.loanType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                LoanStatusBadge(status = loan.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Principal", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(text = loan.principalAmount.toCurrencyString(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
                Column {
                    Text(text = "Monthly EMI", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(text = loan.emiAmount.toCurrencyString(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                }
                Column {
                    Text(text = "Outstanding", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(text = loan.remainingBalance.toCurrencyString(), fontWeight = FontWeight.Bold, color = DarkNavyBlue)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderSubtle.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next Due: ${loan.nextDueDate} • Agent: ${loan.assignedAgentName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                if (loan.status == LoanStatus.ACTIVE || loan.status == LoanStatus.DEFAULTED) {
                    FilledTonalButton(
                        onClick = onCollect,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = RoyalBlueSubtle,
                            contentColor = RoyalBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Collect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun LoanDetailDialog(
    loan: Loan,
    onDismiss: () -> Unit,
    onCollect: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundWhite,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = loan.customerName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DarkNavyBlue)
                        Text(text = "Account: ${loan.loanNumber}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BackgroundLightGrey)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailRow("Borrower Phone", loan.customerPhone)
                    DetailRow("Principal Financed", loan.principalAmount.toCurrencyString())
                    DetailRow("Annual Interest Rate", "${loan.interestRate}% p.a.")
                    DetailRow("Loan Tenure", "${loan.tenureMonths} Months")
                    DetailRow("Monthly EMI Installment", loan.emiAmount.toCurrencyString())
                    DetailRow("Total Paid to Date", loan.totalPaid.toCurrencyString())
                    DetailRow("Outstanding Principal", loan.remainingBalance.toCurrencyString())
                    DetailRow("Disbursement Date", loan.disbursedDate)
                    DetailRow("Next Due Date", loan.nextDueDate)
                    DetailRow("Assigned Collection Lead", loan.assignedAgentName)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", color = TextSecondary)
                    }
                    if (loan.status == LoanStatus.ACTIVE || loan.status == LoanStatus.DEFAULTED) {
                        Button(
                            onClick = onCollect,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RoyalBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Collect EMI", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}

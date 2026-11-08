package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.*
import com.example.ui.theme.*

@Composable
fun RecordCollectionDialog(
    loans: List<Loan>,
    customers: List<Customer>,
    currentRole: UserRole,
    initialLoan: Loan? = null,
    onConfirm: (customerId: String, loanId: String, amount: Double, mode: PaymentMode, collectedBy: String, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    val activeLoans = remember(loans) { loans.filter { it.status == LoanStatus.ACTIVE || it.status == LoanStatus.DEFAULTED } }
    var selectedLoan by remember { mutableStateOf(initialLoan ?: activeLoans.firstOrNull()) }
    var amountText by remember { mutableStateOf(selectedLoan?.emiAmount?.toInt()?.toString() ?: "1500") }
    var selectedMode by remember { mutableStateOf(PaymentMode.UPI) }
    var noteText by remember { mutableStateOf("") }
    var isLoanDropdownOpen by remember { mutableStateOf(false) }

    val collectorName = when (currentRole) {
        UserRole.SUPER_ADMIN -> "Arthur Pendelton (Super Admin)"
        UserRole.ADMIN -> "James Sterling (Admin)"
        UserRole.FINANCE_MANAGER -> "Victoria Bennett (Finance)"
        UserRole.COLLECTION_SUPERVISOR -> "Robert Chen (Supervisor)"
        UserRole.COLLECTION_EMPLOYEE -> "David Miller (Field Agent)"
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BackgroundWhite,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("record_collection_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Record EMI Collection",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavyBlue
                        )
                        Text(
                            text = "Generates verifiable digital collection receipt",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Loan Account
                Text(
                    text = "TARGET LOAN ACCOUNT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(BackgroundLightGrey)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                        .clickable { isLoanDropdownOpen = true }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedLoan?.customerName ?: "Select Loan",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "${selectedLoan?.loanNumber} • Outstanding: ${selectedLoan?.remainingBalance?.toCurrencyString()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary)
                    }

                    DropdownMenu(
                        expanded = isLoanDropdownOpen,
                        onDismissRequest = { isLoanDropdownOpen = false }
                    ) {
                        activeLoans.forEach { loan ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("${loan.customerName} (${loan.loanNumber})", fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text("Monthly EMI: ${loan.emiAmount.toCurrencyString()}", fontSize = 11.sp, color = TextSecondary)
                                    }
                                },
                                onClick = {
                                    selectedLoan = loan
                                    amountText = loan.emiAmount.toInt().toString()
                                    isLoanDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Amount Input
                Text(
                    text = "COLLECTION AMOUNT ($)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("collection_amount_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = BackgroundWhite,
                        unfocusedContainerColor = BackgroundWhite
                    ),
                    leadingIcon = { Text("$", fontWeight = FontWeight.Bold, color = DarkNavyBlue) }
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Mode Selection
                Text(
                    text = "PAYMENT METHOD",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMode.values().forEach { mode ->
                        val isSelected = selectedMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) RoyalBlue else BackgroundLightGrey)
                                .border(1.dp, if (isSelected) RoyalBlue else BorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { selectedMode = mode }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (mode) {
                                    PaymentMode.CASH -> "Cash"
                                    PaymentMode.UPI -> "UPI"
                                    PaymentMode.CHEQUE -> "Cheque"
                                    PaymentMode.BANK_TRANSFER -> "Bank"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes / UTR
                Text(
                    text = "REFERENCE / AUDIT NOTE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("e.g. UPI Ref #491029 or Cash collected at doorstep") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = BackgroundWhite,
                        unfocusedContainerColor = BackgroundWhite
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Auditor / Field Collector: $collectorName",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

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
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                            val loan = selectedLoan
                            if (loan != null && parsedAmount > 0) {
                                onConfirm(
                                    loan.customerId,
                                    loan.id,
                                    parsedAmount,
                                    selectedMode,
                                    collectorName,
                                    noteText.ifBlank { "Doorstep EMI collection via ${selectedMode.label}" }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("confirm_collection_btn")
                    ) {
                        Text("Issue Receipt", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

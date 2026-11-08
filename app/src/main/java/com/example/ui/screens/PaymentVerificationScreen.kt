package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.models.CollectionRecord
import com.example.data.models.ReceiptStatus
import com.example.data.models.toCurrencyString
import com.example.ui.components.ReceiptStatusBadge
import com.example.ui.theme.*

@Composable
fun PaymentVerificationScreen(
    collections: List<CollectionRecord>,
    canApprove: Boolean = true,
    canReverse: Boolean = true,
    onVerifyPayment: (receiptId: String, approved: Boolean, note: String) -> Unit,
    onReversePayment: (receiptId: String, reason: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val pendingVerifications = remember(collections) { collections.filter { it.status == ReceiptStatus.PENDING_VERIFICATION } }
    val auditedRecords = remember(collections) { collections.filter { it.status != ReceiptStatus.PENDING_VERIFICATION } }

    var recordToReverse by remember { mutableStateOf<CollectionRecord?>(null) }
    var reversalReason by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLightGrey)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Payment Audit & Clearance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DarkNavyBlue
                        )
                        Text(
                            text = "${pendingVerifications.size} Field Collections Awaiting Verification",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (canApprove) {
                            Surface(
                                color = SuccessGreenLight,
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreenBorder)
                            ) {
                                Text(
                                    text = "APPROVE ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreenDark,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (canReverse) {
                            Surface(
                                color = Color(0xFFF5F3FF),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE))
                            ) {
                                Text(
                                    text = "REVERSE ACTIVE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6D28D9),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "PENDING CLEARANCE QUEUE (ORANGE STATUS)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = DueOrangeDark
            )
        }

        if (pendingVerifications.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BackgroundWhite)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "All field collections have been audited and posted to ledger.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        } else {
            items(pendingVerifications) { record ->
                VerificationCard(
                    record = record,
                    canApprove = canApprove,
                    onApprove = { onVerifyPayment(record.id, true, "Verified with bank transaction clearance") },
                    onReject = { onVerifyPayment(record.id, false, "Discrepancy in deposit receipt amount") }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "AUDITED TRANSACTIONS LEDGER (VERIFIED & REVERSED)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextMuted
            )
        }

        items(auditedRecords) { record ->
            AuditedRecordCard(
                record = record,
                canReverse = canReverse,
                onRequestReverse = {
                    recordToReverse = record
                    reversalReason = ""
                }
            )
        }
    }

    // Reversal Confirmation Modal
    recordToReverse?.let { record ->
        AlertDialog(
            onDismissRequest = { recordToReverse = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Undo, contentDescription = null, tint = Color(0xFF7C3AED))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reverse Payment Transaction",
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyBlue
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "You are initiating a privileged reversal for receipt ${record.receiptNumber} (${record.amountPaid.toCurrencyString()}). The customer's loan balance will be restored, and an immutable entry will be recorded in the audit logs.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = reversalReason,
                        onValueChange = { reversalReason = it },
                        label = { Text("Reason for Reversal *") },
                        placeholder = { Text("e.g. Duplicate bank deposit, bounced cheque, or field discrepancy") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reasonText = if (reversalReason.isNotBlank()) reversalReason else "Administrative reversal via verification portal"
                        onReversePayment(record.id, reasonText)
                        recordToReverse = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED), contentColor = Color.White),
                    modifier = Modifier.testTag("confirm_reverse_payment_btn")
                ) {
                    Text("Confirm Reversal", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToReverse = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun VerificationCard(
    record: CollectionRecord,
    canApprove: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
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
                Column {
                    Text(
                        text = record.customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyBlue
                    )
                    Text(
                        text = "Receipt #${record.receiptNumber} • ${record.loanNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = RoyalBlue
                    )
                }

                ReceiptStatusBadge(status = record.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "COLLECTION AMOUNT", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = record.amountPaid.toCurrencyString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = SuccessGreenDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "PAYMENT MODE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    Text(
                        text = record.paymentMode.label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Collected By: ${record.collectedBy} at ${record.collectedAt}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            if (record.auditNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(BackgroundLightGrey)
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Field Note: ${record.auditNote}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (canApprove) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = OverdueRed),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(OverdueRedBorder)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("reject_payment_${record.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SuccessGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("approve_payment_${record.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve & Post", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Approval Locked: Clearance requires Supervisor or Finance Manager permissions.",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuditedRecordCard(
    record: CollectionRecord,
    canReverse: Boolean,
    onRequestReverse: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(BackgroundWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = record.customerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Text(
                        text = "${record.receiptNumber} • Verified by ${record.verifiedBy ?: "Supervisor"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = record.amountPaid.toCurrencyString(), fontWeight = FontWeight.Bold, color = TextPrimary)
                    ReceiptStatusBadge(status = record.status)
                }
            }

            if (record.status == ReceiptStatus.REVERSED) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF5F3FF))
                        .padding(6.dp)
                ) {
                    Text(
                        text = record.auditNote,
                        fontSize = 11.sp,
                        color = Color(0xFF6D28D9)
                    )
                }
            } else if (record.status == ReceiptStatus.VERIFIED && canReverse) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onRequestReverse,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7C3AED)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDDD6FE)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reverse Receipt", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

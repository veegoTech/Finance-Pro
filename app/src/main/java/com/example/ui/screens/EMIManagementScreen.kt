package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.data.models.EMIItem
import com.example.data.models.toCurrencyString
import com.example.ui.components.EMIStatusBadge
import com.example.ui.theme.*
import kotlin.math.pow

@Composable
fun EMIManagementScreen(
    emis: List<EMIItem>,
    modifier: Modifier = Modifier
) {
    var showCalculator by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLightGrey)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "EMI Schedule & Calculator",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkNavyBlue
                )
                Text(
                    text = "${emis.size} Installments Scheduled Across Accounts",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Button(
                onClick = { showCalculator = !showCalculator },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (showCalculator) "Hide Tool" else "EMI Calculator", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (showCalculator) {
            FintechEMICalculatorWidget()
            Spacer(modifier = Modifier.height(14.dp))
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(emis) { emi ->
                EMIRowCard(emi = emi)
            }
        }
    }
}

@Composable
fun EMIRowCard(emi: EMIItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = emi.customerName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = "${emi.loanNumber} • Installment #${emi.installmentNumber} of ${emi.totalInstallments}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Due Date: ${emi.dueDate}${if (emi.penaltyFee > 0) " (Late Fee: ${emi.penaltyFee.toCurrencyString()})" else ""}",
                    fontSize = 11.sp,
                    color = if (emi.penaltyFee > 0) OverdueRed else TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (emi.amount + emi.penaltyFee).toCurrencyString(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DarkNavyBlue
                )
                Spacer(modifier = Modifier.height(4.dp))
                EMIStatusBadge(status = emi.status)
            }
        }
    }
}

@Composable
fun FintechEMICalculatorWidget() {
    var principalInput by remember { mutableStateOf("25000") }
    var interestRateInput by remember { mutableStateOf("12.5") }
    var tenureInput by remember { mutableStateOf("24") }

    val principal = principalInput.toDoubleOrNull() ?: 0.0
    val annualRate = interestRateInput.toDoubleOrNull() ?: 0.0
    val months = tenureInput.toIntOrNull() ?: 1

    val monthlyRate = (annualRate / 12.0) / 100.0
    val emi = if (principal > 0 && monthlyRate > 0 && months > 0) {
        val factor = (1.0 + monthlyRate).pow(months.toDouble())
        (principal * monthlyRate * factor) / (factor - 1.0)
    } else 0.0

    val totalPayable = emi * months
    val totalInterest = totalPayable - principal

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BackgroundWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(16.dp)
            .testTag("emi_calculator_widget")
    ) {
        Column {
            Text(
                text = "Loan Installment Calculator",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = DarkNavyBlue
            )
            Text(
                text = "Amortization estimation tool",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = principalInput,
                    onValueChange = { principalInput = it },
                    label = { Text("Principal ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = interestRateInput,
                    onValueChange = { interestRateInput = it },
                    label = { Text("Rate (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.weight(0.8f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = tenureInput,
                    onValueChange = { tenureInput = it },
                    label = { Text("Months") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier.weight(0.8f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BackgroundLightGrey)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "ESTIMATED MONTHLY EMI", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        Text(text = emi.toCurrencyString(), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = DarkNavyBlue)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Total Interest: ${totalInterest.toCurrencyString()}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        Text(text = "Total Repayment: ${totalPayable.toCurrencyString()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }
    }
}

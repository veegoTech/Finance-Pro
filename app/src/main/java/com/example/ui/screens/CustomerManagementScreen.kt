package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.models.Customer
import com.example.data.models.toCurrencyString
import com.example.ui.theme.*

@Composable
fun CustomerManagementScreen(
    customers: List<Customer>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    currentRole: com.example.data.models.UserRole = com.example.data.models.UserRole.ADMIN,
    modifier: Modifier = Modifier
) {
    var selectedCustomerForModal by remember { mutableStateOf<Customer?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLightGrey)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChanged,
            placeholder = { Text("Search by name, phone, SSN or customer ID...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoyalBlue,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = BackgroundWhite,
                unfocusedContainerColor = BackgroundWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("customer_search_input")
        )

        if (currentRole == com.example.data.models.UserRole.COLLECTION_EMPLOYEE) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                color = Color(0xFFEFF6FF),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = RoyalBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Field Collector Portfolio: Viewing ${customers.size} Assigned Customers only. Restricted by granular RBAC policy.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = RoyalBlueDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Summary Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${customers.size} Registered Borrowers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(RoyalBlueSubtle)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "PostgreSQL Synced",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoyalBlue
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Customers List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(customers) { customer ->
                CustomerCard(customer = customer, onClick = { selectedCustomerForModal = customer })
            }
        }
    }

    selectedCustomerForModal?.let { customer ->
        CustomerDetailDialog(customer = customer, onDismiss = { selectedCustomerForModal = null })
    }
}

@Composable
fun CustomerCard(customer: Customer, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("customer_card_${customer.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DarkNavyBlue),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.avatarInitial,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customer.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    KycBadge(status = customer.kycStatus)
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${customer.phone} • ${customer.nationalId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Outstanding: ${customer.totalOutstanding.toCurrencyString()} (${customer.activeLoansCount} active)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkNavyBlue
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                CreditScorePill(score = customer.creditScore)
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun KycBadge(status: String) {
    val isVerified = status == "Verified"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isVerified) SuccessGreenLight else DueOrangeLight)
            .border(1.dp, if (isVerified) SuccessGreenBorder else DueOrangeBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = status,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isVerified) SuccessGreenDark else DueOrangeDark
        )
    }
}

@Composable
fun CreditScorePill(score: Int) {
    val color = when {
        score >= 750 -> SuccessGreenDark
        score >= 650 -> RoyalBlue
        else -> OverdueRed
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$score",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = color
        )
        Text(
            text = "Score",
            fontSize = 9.sp,
            color = TextMuted
        )
    }
}

@Composable
fun CustomerDetailDialog(customer: Customer, onDismiss: () -> Unit) {
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
                        Text(text = customer.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DarkNavyBlue)
                        Text(text = "Borrower ID: ${customer.id}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
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
                    DetailRow("Phone", customer.phone)
                    DetailRow("Email", customer.email)
                    DetailRow("National ID", customer.nationalId)
                    DetailRow("Address", customer.address)
                    DetailRow("KYC Status", customer.kycStatus)
                    DetailRow("Credit Score", "${customer.creditScore} / 850")
                    DetailRow("Total Borrowed", customer.totalBorrowed.toCurrencyString())
                    DetailRow("Current Outstanding", customer.totalOutstanding.toCurrencyString())
                    DetailRow("Active Loan Accounts", "${customer.activeLoansCount}")
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Profile")
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

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    currentRole: UserRole,
    metrics: DashboardMetrics?,
    collections: List<CollectionRecord>,
    assignments: List<Assignment>,
    loans: List<Loan>,
    employees: List<Employee>,
    branchPerformances: List<BranchPerformance>,
    agingBuckets: List<AgingBucket>,
    onOpenCollect: () -> Unit,
    onNavigateModule: (AppModule) -> Unit,
    onViewReceipt: (CollectionRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLightGrey)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        // Hero Role Header Card
        item {
            FintechHeroHeader(
                currentRole = currentRole,
                onOpenCollect = onOpenCollect
            )
        }

        // Metrics Section Tailored to Role
        item {
            when (currentRole) {
                UserRole.SUPER_ADMIN, UserRole.ADMIN -> AdminFintechMetrics(metrics)
                UserRole.FINANCE_MANAGER -> FinanceManagerFintechMetrics(metrics)
                UserRole.COLLECTION_SUPERVISOR -> SupervisorFintechMetrics(metrics)
                UserRole.COLLECTION_EMPLOYEE -> FieldAgentMobileHero(metrics, onOpenCollect)
            }
        }

        // Role-Specific Visual Charts & Sections
        when (currentRole) {
            // 1. SUPER ADMIN & ADMIN DASHBOARD (Desktop-first)
            UserRole.SUPER_ADMIN, UserRole.ADMIN -> {
                item {
                    FintechBarChart(
                        data = listOf(
                            BarData("Jul", 38000f, 34000f),
                            BarData("Aug", 42000f, 39000f),
                            BarData("Sep", 48000f, 43000f),
                            BarData("Oct", 54000f, 51000f),
                            BarData("Nov", 62000f, 57000f)
                        )
                    )
                }

                item {
                    FintechSectionHeader(
                        title = "Branch Liquidity & Recovery League",
                        subtitle = "Performance metrics across active regional hubs",
                        actionLabel = "Detailed Report",
                        onAction = { onNavigateModule(AppModule.REPORTS) }
                    )
                }

                item {
                    BranchTableCard(branches = branchPerformances)
                }

                item {
                    FintechSectionHeader(
                        title = "Recent System Transactions",
                        subtitle = "Live audit feed of payments collected from the field",
                        actionLabel = "Audit Queue",
                        onAction = { onNavigateModule(AppModule.PAYMENT_VERIFICATION) }
                    )
                }

                items(collections.take(4)) { record ->
                    FintechCollectionRow(record = record, onClick = { onViewReceipt(record) })
                }
            }

            // 2. FINANCE MANAGER DASHBOARD
            UserRole.FINANCE_MANAGER -> {
                item {
                    RecoveryRadialGauge(
                        percentage = (metrics?.recoveryRate ?: 91.5).toFloat(),
                        targetAmount = "$ 245,000",
                        collectedAmount = "$ 145,000"
                    )
                }

                item {
                    FintechSectionHeader(
                        title = "Pending Loan Origination Queue",
                        subtitle = "Applications awaiting credit approval and disbursement",
                        actionLabel = "Manage Portfolio",
                        onAction = { onNavigateModule(AppModule.LOAN_MANAGEMENT) }
                    )
                }

                val pendingLoans = loans.filter { it.status == LoanStatus.PENDING_APPROVAL }
                if (pendingLoans.isEmpty()) {
                    item { EmptyStateBanner("No pending loans awaiting origination approval.") }
                } else {
                    items(pendingLoans) { loan ->
                        FintechPendingLoanCard(loan = loan)
                    }
                }

                item {
                    FintechSectionHeader(
                        title = "Delinquency Risk & Aging Watch",
                        subtitle = "Non-performing assets (NPA) and SMA classifications",
                        actionLabel = "View Buckets",
                        onAction = { onNavigateModule(AppModule.PENDING_DUES) }
                    )
                }

                items(agingBuckets) { bucket ->
                    FintechAgingBucketCard(bucket = bucket)
                }
            }

            // 3. COLLECTION SUPERVISOR DASHBOARD
            UserRole.COLLECTION_SUPERVISOR -> {
                item {
                    RecoveryRadialGauge(
                        percentage = 68f,
                        targetAmount = "$ 12,500",
                        collectedAmount = "$ 8,450"
                    )
                }

                item {
                    FintechSectionHeader(
                        title = "Overdue Delinquency Buckets",
                        subtitle = "Accounts prioritized by overdue severity",
                        actionLabel = "Pending Dues",
                        onAction = { onNavigateModule(AppModule.PENDING_DUES) }
                    )
                }

                items(agingBuckets) { bucket ->
                    FintechAgingBucketCard(bucket = bucket)
                }

                item {
                    FintechSectionHeader(
                        title = "Field Recovery Agent Leaderboard",
                        subtitle = "Monthly target achievement rate per agent",
                        actionLabel = "Team Roster",
                        onAction = { onNavigateModule(AppModule.EMPLOYEE_MANAGEMENT) }
                    )
                }

                items(employees.filter { it.role == UserRole.COLLECTION_EMPLOYEE }) { emp ->
                    FintechAgentCard(employee = emp)
                }
            }

            // 4. EMPLOYEE MOBILE APPLICATION (Mobile-First)
            UserRole.COLLECTION_EMPLOYEE -> {
                item {
                    FintechSectionHeader(
                        title = "Today's Field Collection Route",
                        subtitle = "Customer visits assigned for doorstep recovery",
                        actionLabel = "All Leads",
                        onAction = { onNavigateModule(AppModule.EMPLOYEE_ASSIGNMENTS) }
                    )
                }

                items(assignments.take(4)) { assignment ->
                    FintechFieldLeadCard(assignment = assignment, onCollect = onOpenCollect)
                }

                item {
                    FintechSectionHeader(
                        title = "My Today's Verified Receipts",
                        subtitle = "Instant digital receipts recorded on-ground",
                        actionLabel = "History",
                        onAction = { onNavigateModule(AppModule.LOAN_COLLECTION) }
                    )
                }

                items(collections.take(3)) { record ->
                    FintechCollectionRow(record = record, onClick = { onViewReceipt(record) })
                }
            }
        }
    }
}

@Composable
fun FintechHeroHeader(
    currentRole: UserRole,
    onOpenCollect: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkNavyBlue)
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentRole.badgeLabel.uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlueBright,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (currentRole) {
                            UserRole.SUPER_ADMIN -> "Global Governance, Security & Liquidity"
                            UserRole.ADMIN -> "Executive Financial Overview"
                            UserRole.FINANCE_MANAGER -> "Lending Portfolio & Risk Control"
                            UserRole.COLLECTION_SUPERVISOR -> "Field Collection Command Center"
                            UserRole.COLLECTION_EMPLOYEE -> "Doorstep Collection Agent App"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                if (currentRole == UserRole.COLLECTION_EMPLOYEE) {
                    Button(
                        onClick = onOpenCollect,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalBlueBright,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("hero_collect_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Collect EMI", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = currentRole.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFCBD5E1),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun AdminFintechMetrics(metrics: DashboardMetrics?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Disbursed",
                value = (metrics?.totalDisbursed ?: 245000.0).toCurrencyString(),
                subtitle = "Active loan capital",
                icon = Icons.Default.AccountBalance,
                accentColor = DarkNavyBlue,
                trendText = "+12% MoM",
                isPositiveTrend = true,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Total Collected",
                value = (metrics?.totalCollected ?: 145000.0).toCurrencyString(),
                subtitle = "Successful payments (Green)",
                icon = Icons.Default.Verified,
                accentColor = SuccessGreen,
                trendText = "91.5% Recovery",
                isPositiveTrend = true,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Upcoming Dues",
                value = "${metrics?.pendingDuesCount ?: 4} Accounts",
                subtitle = "Dues in grace period (Orange)",
                icon = Icons.Default.PendingActions,
                accentColor = DueOrange,
                trendText = "$ 14,200",
                isPositiveTrend = true,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Overdue & NPA",
                value = String.format("%.1f%%", metrics?.npaRate ?: 3.2),
                subtitle = "Delinquent loans (Red)",
                icon = Icons.Default.Warning,
                accentColor = OverdueRed,
                trendText = "3.2% NPA Ratio",
                isPositiveTrend = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FinanceManagerFintechMetrics(metrics: DashboardMetrics?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Outstanding Capital",
                value = (metrics?.totalOutstanding ?: 178000.0).toCurrencyString(),
                subtitle = "Principal in circulation",
                icon = Icons.Default.AccountBalanceWallet,
                accentColor = RoyalBlue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Avg Interest Yield",
                value = "13.8%",
                subtitle = "Weighted annual portfolio rate",
                icon = Icons.Default.ShowChart,
                accentColor = DarkNavyBlue,
                trendText = "Prime Margin",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Active Accounts",
                value = "${metrics?.activeLoansCount ?: 6} Loans",
                subtitle = "Standard performing loans",
                icon = Icons.Default.CheckCircle,
                accentColor = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Pending Verification",
                value = "${metrics?.pendingVerificationsCount ?: 2} Deposits",
                subtitle = "Awaiting ledger audit",
                icon = Icons.Default.HourglassTop,
                accentColor = DueOrange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SupervisorFintechMetrics(metrics: DashboardMetrics?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Daily Target",
                value = (metrics?.dailyCollectionTarget ?: 12500.0).toCurrencyString(),
                subtitle = "Branch recovery goal",
                icon = Icons.Default.Flag,
                accentColor = RoyalBlue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Recovered Today",
                value = (metrics?.dailyCollectedSoFar ?: 8450.0).toCurrencyString(),
                subtitle = "Successful field deposits",
                icon = Icons.Default.CheckCircleOutline,
                accentColor = SuccessGreen,
                trendText = "68% Met",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Active Agents",
                value = "${metrics?.activeAgentsCount ?: 3} On Duty",
                subtitle = "Field collection workforce",
                icon = Icons.Default.DirectionsRun,
                accentColor = DarkNavyBlue,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Overdue Accounts",
                value = "${metrics?.pendingDuesCount ?: 4} Defaulters",
                subtitle = "Escalated for immediate visit",
                icon = Icons.Default.ErrorOutline,
                accentColor = OverdueRed,
                trendText = "Immediate Action",
                isPositiveTrend = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun FieldAgentMobileHero(metrics: DashboardMetrics?, onOpenCollect: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TODAY'S FIELD COLLECTION GOAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$ 2,850 / $ 4,200",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SuccessGreenLight)
                        .border(1.dp, SuccessGreenBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "68% Achieved",
                        color = SuccessGreenDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = 0.68f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SuccessGreen,
                trackColor = SurfaceCardAlt
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onOpenCollect,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("field_agent_quick_collect")
            ) {
                Icon(imageVector = Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record New EMI Collection", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun BranchTableCard(branches: List<BranchPerformance>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BackgroundWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BackgroundLightGrey)
                    .clip(RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "BRANCH NAME", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.weight(1.4f))
                Text(text = "DISBURSED", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.weight(1f))
                Text(text = "RECOVERED", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.weight(1f))
                Text(text = "RATE", style = MaterialTheme.typography.labelSmall, color = TextMuted, modifier = Modifier.weight(0.8f))
            }

            Spacer(modifier = Modifier.height(6.dp))

            branches.forEachIndexed { i, branch ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = branch.branchName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1.4f)
                    )
                    Text(
                        text = branch.totalDisbursed.toCurrencyString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = branch.totalCollected.toCurrencyString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier.weight(0.8f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SuccessGreenLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${branch.recoveryRate}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SuccessGreenDark
                            )
                        }
                    }
                }
                if (i < branches.size - 1) {
                    Divider(color = BorderSubtle.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun FintechCollectionRow(record: CollectionRecord, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.customerName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = "${record.loanNumber} • ${record.paymentMode.label} • by ${record.collectedBy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = record.amountPaid.toCurrencyString(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                ReceiptStatusBadge(status = record.status)
            }
        }
    }
}

@Composable
fun FintechAgingBucketCard(bucket: AgingBucket) {
    val riskColor = when (bucket.riskLevel) {
        "NPA" -> OverdueRed
        "Critical" -> OverdueRed
        "Moderate" -> DueOrange
        else -> SuccessGreen
    }

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(riskColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = bucket.bucketName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${bucket.count} Accounts • ${bucket.daysRange}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = bucket.totalAmount.toCurrencyString(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (bucket.riskLevel == "NPA") OverdueRed else TextPrimary
                )
                Text(
                    text = bucket.riskLevel.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = riskColor
                )
            }
        }
    }
}

@Composable
fun FintechAgentCard(employee: Employee) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = employee.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${employee.branch} • ${employee.activeAssignmentsCount} Leads",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(SuccessGreenLight)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${employee.completionPercentage}% Target",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = SuccessGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = (employee.completionPercentage / 100f).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = RoyalBlue,
                trackColor = SurfaceCardAlt
            )
        }
    }
}

@Composable
fun FintechFieldLeadCard(assignment: Assignment, onCollect: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BackgroundWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assignment.customerName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = assignment.customerAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                PriorityBadge(priority = assignment.priority)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Due: ${assignment.dueAmount.toCurrencyString()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = DarkNavyBlue
                    )
                    Text(
                        text = "Loan: ${assignment.loanNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = onCollect,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Collect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FintechPendingLoanCard(loan: Loan) {
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
            Column {
                Text(
                    text = loan.customerName,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )
                Text(
                    text = "${loan.loanType} • ${loan.tenureMonths} Mos @ ${loan.interestRate}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = loan.principalAmount.toCurrencyString(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextPrimary
                )
                LoanStatusBadge(status = loan.status)
            }
        }
    }
}

@Composable
fun FintechSectionHeader(
    title: String,
    subtitle: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
        TextButton(onClick = onAction) {
            Text(actionLabel, color = RoyalBlue, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        }
    }
}

@Composable
fun EmptyStateBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundWhite)
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

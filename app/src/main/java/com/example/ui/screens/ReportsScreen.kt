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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.BranchPerformance
import com.example.data.models.DashboardMetrics
import com.example.data.models.toCurrencyString
import com.example.ui.components.BarData
import com.example.ui.components.FintechBarChart
import com.example.ui.theme.*

@Composable
fun ReportsScreen(
    metrics: DashboardMetrics?,
    branches: List<BranchPerformance>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLightGrey)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Financial & Recovery Analytics",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyBlue
                    )
                    Text(
                        text = "Executive Performance & Liquidity Statements",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Button(
                    onClick = { /* Export */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("export_report_btn")
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BackgroundWhite)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Executive Liquidity Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkNavyBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ReportMetric("Total Portfolio Capital", (metrics?.totalDisbursed ?: 245000.0).toCurrencyString(), DarkNavyBlue)
                        ReportMetric("Total Collections Recovered", (metrics?.totalCollected ?: 145000.0).toCurrencyString(), SuccessGreenDark)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        ReportMetric("Recovery Efficiency Rate", String.format("%.1f%%", metrics?.recoveryRate ?: 91.5), RoyalBlue)
                        ReportMetric("NPA Default Ratio", String.format("%.1f%%", metrics?.npaRate ?: 3.2), OverdueRed)
                    }
                }
            }
        }

        item {
            Text(
                text = "REGIONAL BRANCH RECOVERY TABLE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextMuted
            )
        }

        item {
            BranchTableCard(branches = branches)
        }

        item {
            Text(
                text = "PAYMENT MODE BREAKDOWN",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextMuted
            )
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BackgroundWhite)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModeRow(mode = "UPI / Instant QR Payment", percentage = 58, amount = "$ 84,100", color = RoyalBlue)
                    ModeRow(mode = "Doorstep Cash Field Collection", percentage = 28, amount = "$ 40,600", color = SuccessGreen)
                    ModeRow(mode = "Bank Cheque & Clearing", percentage = 14, amount = "$ 20,300", color = DarkNavyBlue)
                }
            }
        }
    }
}

@Composable
private fun ReportMetric(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun ModeRow(mode: String, percentage: Int, amount: String, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = mode, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(text = "$amount ($percentage%)", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = percentage / 100f,
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = SurfaceCardAlt
        )
    }
}

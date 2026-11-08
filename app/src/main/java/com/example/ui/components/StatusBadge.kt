package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.models.EMIStatus
import com.example.data.models.LoanStatus
import com.example.data.models.ReceiptStatus
import com.example.ui.theme.*

@Composable
fun FinTechStatusPill(
    label: String,
    dotColor: Color,
    bgColor: Color,
    textColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun LoanStatusBadge(status: LoanStatus, modifier: Modifier = Modifier) {
    val (dot, bg, text, border) = when (status) {
        LoanStatus.ACTIVE -> QuadColor(SuccessGreen, SuccessGreenLight, SuccessGreenDark, SuccessGreenBorder)
        LoanStatus.PENDING_APPROVAL -> QuadColor(DueOrange, DueOrangeLight, DueOrangeDark, DueOrangeBorder)
        LoanStatus.DISBURSED -> QuadColor(RoyalBlue, RoyalBlueSubtle, RoyalBlue, BorderSubtle)
        LoanStatus.CLOSED -> QuadColor(TextMuted, SurfaceCardAlt, TextSecondary, BorderSubtle)
        LoanStatus.DEFAULTED -> QuadColor(OverdueRed, OverdueRedLight, OverdueRedDark, OverdueRedBorder)
    }

    FinTechStatusPill(
        label = status.label,
        dotColor = dot,
        bgColor = bg,
        textColor = text,
        borderColor = border,
        modifier = modifier
    )
}

@Composable
fun EMIStatusBadge(status: EMIStatus, modifier: Modifier = Modifier) {
    val (dot, bg, text, border) = when (status) {
        // Green for successful payments
        EMIStatus.PAID -> QuadColor(SuccessGreen, SuccessGreenLight, SuccessGreenDark, SuccessGreenBorder)
        // Orange for upcoming dues
        EMIStatus.PENDING -> QuadColor(DueOrange, DueOrangeLight, DueOrangeDark, DueOrangeBorder)
        // Red for overdue payments
        EMIStatus.OVERDUE -> QuadColor(OverdueRed, OverdueRedLight, OverdueRedDark, OverdueRedBorder)
    }

    FinTechStatusPill(
        label = when(status) {
            EMIStatus.PAID -> "Paid (Success)"
            EMIStatus.PENDING -> "Upcoming Due"
            EMIStatus.OVERDUE -> "Overdue"
        },
        dotColor = dot,
        bgColor = bg,
        textColor = text,
        borderColor = border,
        modifier = modifier
    )
}

@Composable
fun ReceiptStatusBadge(status: ReceiptStatus, modifier: Modifier = Modifier) {
    val (dot, bg, text, border) = when (status) {
        // Green for successful verified payments
        ReceiptStatus.VERIFIED -> QuadColor(SuccessGreen, SuccessGreenLight, SuccessGreenDark, SuccessGreenBorder)
        // Orange for upcoming clearance
        ReceiptStatus.PENDING_VERIFICATION -> QuadColor(DueOrange, DueOrangeLight, DueOrangeDark, DueOrangeBorder)
        // Red for rejected
        ReceiptStatus.REJECTED -> QuadColor(OverdueRed, OverdueRedLight, OverdueRedDark, OverdueRedBorder)
        // Purple for reversed
        ReceiptStatus.REVERSED -> QuadColor(
            dot = Color(0xFF7C3AED),
            bg = Color(0xFFF5F3FF),
            text = Color(0xFF6D28D9),
            border = Color(0xFFDDD6FE)
        )
    }

    FinTechStatusPill(
        label = status.label,
        dotColor = dot,
        bgColor = bg,
        textColor = text,
        borderColor = border,
        modifier = modifier
    )
}

@Composable
fun PriorityBadge(priority: String, modifier: Modifier = Modifier) {
    val (dot, bg, text, border) = when (priority) {
        "Urgent" -> QuadColor(OverdueRed, OverdueRedLight, OverdueRedDark, OverdueRedBorder)
        "High" -> QuadColor(DueOrange, DueOrangeLight, DueOrangeDark, DueOrangeBorder)
        "Medium" -> QuadColor(RoyalBlue, RoyalBlueSubtle, RoyalBlue, BorderSubtle)
        else -> QuadColor(TextMuted, SurfaceCardAlt, TextSecondary, BorderSubtle)
    }

    FinTechStatusPill(
        label = priority,
        dotColor = dot,
        bgColor = bg,
        textColor = text,
        borderColor = border,
        modifier = modifier
    )
}

data class QuadColor(val dot: Color, val bg: Color, val text: Color, val border: Color)

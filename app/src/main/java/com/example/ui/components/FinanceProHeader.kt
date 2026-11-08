package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.data.models.AuthSession
import com.example.data.models.UserRole
import com.example.ui.theme.*

@Composable
fun FinanceProHeader(
    currentSession: AuthSession?,
    currentRole: UserRole,
    unreadNotificationsCount: Int,
    onOpenRoleSelector: () -> Unit,
    onOpenMenu: () -> Unit,
    onNotificationsClick: () -> Unit,
    onRefresh: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val roleBadgeColor = when (currentRole) {
        UserRole.SUPER_ADMIN -> Color(0xFF6366F1)
        UserRole.ADMIN -> DarkNavyBlue
        UserRole.FINANCE_MANAGER -> RoyalBlue
        UserRole.COLLECTION_SUPERVISOR -> Color(0xFF7C3AED)
        UserRole.COLLECTION_EMPLOYEE -> SuccessGreenDark
    }

    Surface(
        color = BackgroundWhite,
        tonalElevation = 0.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Brand & Navigation Menu Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenMenu,
                        modifier = Modifier.size(38.dp).testTag("header_menu_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Sidebar",
                            tint = DarkNavyBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Finance",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavyBlue,
                                letterSpacing = (-0.5).sp
                            )
                            Text(
                                text = "Pro",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue,
                                letterSpacing = (-0.5).sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Employee ID Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BackgroundLightGrey)
                                    .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = currentSession?.user?.employeeId ?: "EMP-AUTH",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }
                        Text(
                            text = currentSession?.user?.fullName ?: "Finance & Collection Core",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Right Actions: Persona Pill + Notifications + Logout
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Persona Switcher Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BackgroundLightGrey)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                            .clickable(onClick = onOpenRoleSelector)
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("persona_switcher_chip")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(roleBadgeColor)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = currentRole.badgeLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = TextSecondary
                            )
                        }
                    }

                    // Notification Bell
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier.size(34.dp).testTag("header_notifications_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(containerColor = OverdueRed, contentColor = Color.White) {
                                        Text(unreadNotificationsCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = "Notifications",
                                modifier = Modifier.size(19.dp),
                                tint = TextPrimary
                            )
                        }
                    }

                    // Sync Refresh
                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.size(34.dp).testTag("header_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            modifier = Modifier.size(19.dp),
                            tint = TextSecondary
                        )
                    }

                    // Secure Logout Action
                    IconButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.size(34.dp).testTag("header_logout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Sign Out",
                            modifier = Modifier.size(19.dp),
                            tint = OverdueRedDark
                        )
                    }
                }
            }

            Divider(color = BorderSubtle, thickness = 1.dp)
        }
    }
}

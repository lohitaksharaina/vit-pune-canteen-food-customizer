package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuthUser
import com.example.data.model.UserRole
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.NonVegRed
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.AppMode

@Composable
fun TopRestaurantBar(
    currentMode: AppMode,
    onModeChange: (AppMode) -> Unit,
    activeOrdersCount: Int,
    isWaitlistActive: Boolean,
    isCloudSyncConnected: Boolean,
    currentUser: AuthUser?,
    onLogout: () -> Unit,
    onOpenSyncSettings: () -> Unit,
    onOpenFeedback: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Top Row: Logo, Title, Logged in User, and Quick Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FlameOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SoupKitchen,
                            contentDescription = "Canteen Logo",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "VIT PUNE CANTEEN",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                ),
                                color = WarmIvory
                            )
                        }
                        Text(
                            text = "Vishwakarma Institute of Technology • Pune",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action icons: User Badge, Sync Status, Feedback & Logout
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Logged in user tag
                    if (currentUser != null) {
                        val roleColor = when (currentUser.role) {
                            UserRole.STUDENT -> Color(0xFF1565C0)
                            UserRole.CHEF -> Color(0xFFE65100)
                            UserRole.ADMIN -> Color(0xFF6A1B9A)
                        }
                        val roleIcon = when (currentUser.role) {
                            UserRole.STUDENT -> Icons.Default.School
                            UserRole.CHEF -> Icons.Default.SoupKitchen
                            UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(roleColor.copy(alpha = 0.2f))
                                .border(1.dp, roleColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("user_role_badge")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = roleIcon,
                                    contentDescription = null,
                                    tint = roleColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = currentUser.role.badge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = WarmIvory
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(
                        onClick = onOpenFeedback,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("feedback_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Feedback,
                            contentDescription = "Feedback",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenSyncSettings,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("sync_settings_button")
                    ) {
                        Icon(
                            imageVector = if (isCloudSyncConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = "Cloud Sync",
                            tint = if (isCloudSyncConnected) Color(0xFF00E676) else Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Logout Button
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = NonVegRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Row: Mode Switcher & Capacity Status Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mode Switcher depending on User Role
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (currentUser?.role) {
                        UserRole.ADMIN -> {
                            ModePill(
                                title = "Customer",
                                icon = Icons.Default.Restaurant,
                                isSelected = currentMode == AppMode.CUSTOMER,
                                onClick = { onModeChange(AppMode.CUSTOMER) },
                                testTag = "mode_customer_button"
                            )
                            ModePill(
                                title = "Chef KDS",
                                icon = Icons.Default.SoupKitchen,
                                isSelected = currentMode == AppMode.CHEF,
                                onClick = { onModeChange(AppMode.CHEF) },
                                testTag = "mode_chef_button"
                            )
                            ModePill(
                                title = "Admin",
                                icon = Icons.Default.AdminPanelSettings,
                                isSelected = currentMode == AppMode.ADMIN,
                                onClick = { onModeChange(AppMode.ADMIN) },
                                testTag = "mode_admin_button"
                            )
                        }
                        UserRole.CHEF -> {
                            ModePill(
                                title = "Chef KDS",
                                icon = Icons.Default.SoupKitchen,
                                isSelected = currentMode == AppMode.CHEF,
                                onClick = { onModeChange(AppMode.CHEF) },
                                testTag = "mode_chef_button"
                            )
                            ModePill(
                                title = "Customer View",
                                icon = Icons.Default.Restaurant,
                                isSelected = currentMode == AppMode.CUSTOMER,
                                onClick = { onModeChange(AppMode.CUSTOMER) },
                                testTag = "mode_customer_button"
                            )
                        }
                        else -> {
                            // STUDENT or default
                            ModePill(
                                title = "Customer Menu",
                                icon = Icons.Default.Restaurant,
                                isSelected = currentMode == AppMode.CUSTOMER,
                                onClick = { onModeChange(AppMode.CUSTOMER) },
                                testTag = "mode_customer_button"
                            )
                        }
                    }
                }

                // Live Active Kitchen Capacity Indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isWaitlistActive) Color(0xFF4A1010) else Color(0xFF281E15)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isWaitlistActive) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Waitlist Full",
                                tint = NonVegRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "WAITLIST (${activeOrdersCount}/30)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color(0xFFFF8A80)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(FlameOrange)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Active Kitchen: $activeOrdersCount/30",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModePill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) FlameOrange else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

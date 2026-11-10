package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.MenuItem
import com.example.data.model.OrderStatus
import com.example.data.model.RestaurantOrder
import com.example.ui.components.VegNonVegSymbol
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.NonVegRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WaitlistAmber
import com.example.ui.theme.WarmIvory
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    orders: List<RestaurantOrder>,
    activeOrdersCount: Int,
    isWaitlistActive: Boolean,
    menuItems: List<MenuItem>,
    unavailableItemIds: Set<String>,
    onToggleItemAvailability: (String) -> Unit,
    onFillWaitlistForTesting: () -> Unit,
    onResetWaitlistOrders: () -> Unit,
    onTriggerAutoPurge: () -> Unit,
    onSwitchToStudentView: () -> Unit,
    onSwitchToCookView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalRevenue = orders.filter { it.isPaidAtCounter }.sumOf { it.totalAmount }
    val settledCount = orders.count { it.status == OrderStatus.COMPLETED_SETTLED }
    val preparingCount = orders.count { it.status == OrderStatus.ACCEPTED_PREPARING }
    val incomingCount = orders.count { it.status == OrderStatus.RECEIVED }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("admin_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF4A148C)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Admin",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CANTEEN ADMINISTRATION",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = WarmIvory
                                )
                                Text(
                                    text = "Vishwakarma Institute of Technology • Pune",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }

                        // Live status pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isWaitlistActive) Color(0xFF4A1010) else Color(0xFF1B4D20))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isWaitlistActive) "WAITLIST FULL" else "CAPACITY OK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (isWaitlistActive) Color(0xFFFF8A80) else SuccessGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Jump to Other Views
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSwitchToStudentView,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_preview_student_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = FlameOrange
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Student Menu",
                                style = MaterialTheme.typography.labelMedium,
                                color = WarmIvory
                            )
                        }

                        OutlinedButton(
                            onClick = onSwitchToCookView,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_preview_cook_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SoupKitchen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Kitchen KDS",
                                style = MaterialTheme.typography.labelMedium,
                                color = WarmIvory
                            )
                        }
                    }
                }
            }
        }

        // 2. Metrics Analytics Grid
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "CANTEEN METRICS & LOAD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Kitchen Load",
                        value = "$activeOrdersCount / 30",
                        subtitle = if (isWaitlistActive) "Waitlist active" else "Normal capacity",
                        color = if (isWaitlistActive) NonVegRed else FlameOrange,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Gross Revenue",
                        value = "₹${String.format(Locale.getDefault(), "%.0f", totalRevenue)}",
                        subtitle = "$settledCount paid at counter",
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Incoming Orders",
                        value = "$incomingCount",
                        subtitle = "Awaiting cook accept",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "In Preparation",
                        value = "$preparingCount",
                        subtitle = "Currently cooking",
                        color = Color(0xFFFFB74D),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Queue & Capacity Operations
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Text(
                        text = "CAPACITY & DATA RETENTION CONTROLS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onFillWaitlistForTesting,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_fill_waitlist_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A1010))
                        ) {
                            Text(
                                text = "Test 30 Waitlist",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFFFF8A80)
                            )
                        }

                        Button(
                            onClick = onResetWaitlistOrders,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_clear_waitlist_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                text = "Clear Test Orders",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = WarmIvory
                            )
                        }

                        Button(
                            onClick = onTriggerAutoPurge,
                            modifier = Modifier.testTag("admin_trigger_purge_btn"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoDelete,
                                contentDescription = "Purge 7 Days",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }

        // 4. Menu Item Stock / Availability Manager
        item {
            Text(
                text = "MENU STOCK & AVAILABILITY MANAGER",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "Toggle items on/off when canteen dishes run out. Sold-out dishes will be blocked from customer orders.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(menuItems, key = { it.id }) { item ->
            val isOutOfStock = unavailableItemIds.contains(item.id)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_stock_item_${item.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isOutOfStock) Color(0xFF2B1C1A) else MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        VegNonVegSymbol(isVeg = item.isVeg)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = if (isOutOfStock) Color(0xFFFF8A80) else WarmIvory
                                )
                                if (isOutOfStock) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(NonVegRed)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "SOLD OUT",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${item.category.title} • ₹${item.price.toInt()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isOutOfStock) "Unavailable" else "In Stock",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isOutOfStock) Color(0xFFFF8A80) else SuccessGreen
                            ),
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Switch(
                            checked = !isOutOfStock,
                            onCheckedChange = { onToggleItemAvailability(item.id) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = SuccessGreen,
                                uncheckedThumbColor = Color(0xFFFF8A80),
                                uncheckedTrackColor = Color(0xFF4A1010)
                            ),
                            modifier = Modifier.testTag("admin_toggle_${item.id}")
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VIT Canteen Management Portal • Engineered with Codex ChatGPT",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black
                ),
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

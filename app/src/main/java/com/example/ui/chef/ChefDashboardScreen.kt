package com.example.ui.chef

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CustomizationPricingStatus
import com.example.data.model.OrderStatus
import com.example.data.model.RestaurantOrder
import com.example.ui.components.VegNonVegSymbol
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.NonVegRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WaitlistAmber
import com.example.ui.theme.WarmIvory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChefDashboardScreen(
    orders: List<RestaurantOrder>,
    activeOrdersCount: Int,
    isWaitlistActive: Boolean,
    statusFilter: OrderStatus?,
    onStatusFilterChange: (OrderStatus?) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAcceptOrder: (String) -> Unit,
    onMarkReady: (String) -> Unit,
    onMarkPaidAtCounter: (String) -> Unit,
    onViewBill: (RestaurantOrder) -> Unit,
    onTriggerAutoPurge: () -> Unit,
    onFillWaitlistForTesting: () -> Unit,
    onResetWaitlistOrders: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Memoized filter for smooth scrolling and elimination of re-calculation jank
    val displayedOrders = remember(orders, statusFilter, searchQuery) {
        val trimmedQuery = searchQuery.trim()
        orders.filter { order ->
            val matchesStatus = statusFilter == null || order.status == statusFilter
            val matchesQuery = trimmedQuery.isBlank() ||
                order.orderId.contains(trimmedQuery, ignoreCase = true) ||
                order.tableNumber.contains(trimmedQuery, ignoreCase = true) ||
                order.customerName.contains(trimmedQuery, ignoreCase = true) ||
                order.orderNumber.toString().contains(trimmedQuery)
            matchesStatus && matchesQuery
        }.distinctBy { it.orderId } // Prevents LazyColumn duplicate key crash
    }

    // Memoize status counts to avoid multiple full list iterations per recomposition frame
    val orderCounts = remember(orders) {
        mapOf(
            null to orders.size,
            OrderStatus.RECEIVED to orders.count { it.status == OrderStatus.RECEIVED },
            OrderStatus.ACCEPTED_PREPARING to orders.count { it.status == OrderStatus.ACCEPTED_PREPARING },
            OrderStatus.READY_FOR_COUNTER to orders.count { it.status == OrderStatus.READY_FOR_COUNTER },
            OrderStatus.COMPLETED_SETTLED to orders.count { it.status == OrderStatus.COMPLETED_SETTLED }
        )
    }

    // Single unified LazyColumn root to solve the "kitchen non scrolling bug" completely
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("chef_tickets_list"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Chef KDS Control Panel Card
        item(key = "kds_control_card") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .animateContentSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Header: KDS Status & Workload Counter
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFF381400)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SoupKitchen,
                                    contentDescription = null,
                                    tint = FlameOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "KITCHEN DISPLAY SYSTEM",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = WarmIvory
                                )
                                Text(
                                    text = "Vishwakarma Institute of Technology • Canteen KDS",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Active Capacity Meter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isWaitlistActive) Color(0xFF4A1010) else Color(0xFF1E2818))
                            .border(
                                1.dp,
                                if (isWaitlistActive) NonVegRed else SuccessGreen,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (isWaitlistActive) "WAITLIST ACTIVE" else "NORMAL LOAD",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = if (isWaitlistActive) Color(0xFFFF8A80) else SuccessGreen
                                )
                                Text(
                                    text = "$activeOrdersCount / 30 Active",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = WarmIvory
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Queue Testing Tools (Allows testing the 30-limit waitlist guard cleanly)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onFillWaitlistForTesting,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_fill_waitlist_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Fill to 30 (Test Waitlist)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = WaitlistAmber
                            )
                        }

                        OutlinedButton(
                            onClick = onResetWaitlistOrders,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_clear_waitlist_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Clear Test Orders",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        OutlinedButton(
                            onClick = onTriggerAutoPurge,
                            modifier = Modifier.testTag("trigger_purge_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoDelete,
                                contentDescription = "7-Day Purge",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }

        // 2. Search & Filter Status Chips
        item(key = "kds_search_and_filters") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = "Search Order #, Token, Table...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chef_search_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = WarmIvory,
                        unfocusedTextColor = WarmIvory,
                        focusedBorderColor = FlameOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Status Filter Tabs
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = statusFilter == null,
                            onClick = { onStatusFilterChange(null) },
                            label = { Text("All (${orderCounts[null] ?: 0})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlameOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == OrderStatus.RECEIVED,
                            onClick = {
                                onStatusFilterChange(if (statusFilter == OrderStatus.RECEIVED) null else OrderStatus.RECEIVED)
                            },
                            label = {
                                Text("Incoming (${orderCounts[OrderStatus.RECEIVED] ?: 0})")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlameOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == OrderStatus.ACCEPTED_PREPARING,
                            onClick = {
                                onStatusFilterChange(if (statusFilter == OrderStatus.ACCEPTED_PREPARING) null else OrderStatus.ACCEPTED_PREPARING)
                            },
                            label = {
                                Text("Preparing (${orderCounts[OrderStatus.ACCEPTED_PREPARING] ?: 0})")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlameOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == OrderStatus.READY_FOR_COUNTER,
                            onClick = {
                                onStatusFilterChange(if (statusFilter == OrderStatus.READY_FOR_COUNTER) null else OrderStatus.READY_FOR_COUNTER)
                            },
                            label = {
                                Text("Ready (${orderCounts[OrderStatus.READY_FOR_COUNTER] ?: 0})")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlameOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        FilterChip(
                            selected = statusFilter == OrderStatus.COMPLETED_SETTLED,
                            onClick = {
                                onStatusFilterChange(if (statusFilter == OrderStatus.COMPLETED_SETTLED) null else OrderStatus.COMPLETED_SETTLED)
                            },
                            label = {
                                Text("Settled (${orderCounts[OrderStatus.COMPLETED_SETTLED] ?: 0})")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlameOrange,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 3. Kitchen Order Tickets List or Empty State
        if (displayedOrders.isEmpty()) {
            item(key = "empty_state") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SoupKitchen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No tickets found for '$searchQuery'" else "No kitchen tickets matching this filter.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (statusFilter != null || searchQuery.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = {
                                    onStatusFilterChange(null)
                                    onSearchChange("")
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Show All Orders (${orders.size})", color = FlameOrange)
                            }
                        }
                    }
                }
            }
        } else {
            items(displayedOrders, key = { it.orderId }) { order ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ChefOrderTicketCard(
                        order = order,
                        onAccept = {
                            try { onAcceptOrder(order.orderId) } catch (_: Exception) {}
                        },
                        onMarkReady = {
                            try { onMarkReady(order.orderId) } catch (_: Exception) {}
                        },
                        onMarkPaidAtCounter = {
                            try { onMarkPaidAtCounter(order.orderId) } catch (_: Exception) {}
                        },
                        onViewBill = { onViewBill(order) }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ChefOrderTicketCard(
    order: RestaurantOrder,
    onAccept: () -> Unit,
    onMarkReady: () -> Unit,
    onMarkPaidAtCounter: () -> Unit,
    onViewBill: () -> Unit
) {
    // Cache formatted timestamp to prevent jank during list scrolling
    val timeFormatted = remember(order.createdAt) {
        try {
            SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(order.createdAt))
        } catch (_: Exception) {
            "Just now"
        }
    }

    // Elapsed waiting time indicator
    val elapsedMinutes = remember(order.createdAt) {
        val diff = System.currentTimeMillis() - order.createdAt
        (diff / (60 * 1000L)).coerceAtLeast(0)
    }

    val isUrgent = order.status == OrderStatus.RECEIVED && elapsedMinutes >= 15

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("chef_ticket_${order.orderId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Ticket Header: Token, Table, Time, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FlameOrange)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "#ORD-${order.orderNumber}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${order.tableNumber} • ${order.customerName}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = WarmIvory
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Received at $timeFormatted (${elapsedMinutes}m ago)",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isUrgent) WaitlistAmber else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (order.status) {
                                OrderStatus.RECEIVED -> Color(0xFF3E2723)
                                OrderStatus.ACCEPTED_PREPARING -> Color(0xFF4E260E)
                                OrderStatus.READY_FOR_COUNTER -> Color(0xFF1B4D20)
                                OrderStatus.COMPLETED_SETTLED -> Color(0xFF212121)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = order.status.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = when (order.status) {
                            OrderStatus.RECEIVED -> FlameOrange
                            OrderStatus.ACCEPTED_PREPARING -> MaterialTheme.colorScheme.secondary
                            OrderStatus.READY_FOR_COUNTER -> SuccessGreen
                            OrderStatus.COMPLETED_SETTLED -> Color(0xFFB0BEC5)
                        }
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(vertical = 10.dp)
            )

            // Custom Pricing Status Badge (shown if admin proposed a price)
            if (order.customizationPricingStatus != CustomizationPricingStatus.NONE) {
                val (badgeColor, badgeBg, badgeText) = when (order.customizationPricingStatus) {
                    CustomizationPricingStatus.PRICE_PROPOSED_BY_ADMIN ->
                        Triple(Color(0xFFFFC107), Color(0xFF332500), "⏳ Price Awaiting Customer")
                    CustomizationPricingStatus.ACCEPTED_BY_CUSTOMER ->
                        Triple(SuccessGreen, Color(0xFF0D2B10), "✓ Custom Price Accepted — Prepare Customised")
                    CustomizationPricingStatus.DECLINED_BY_CUSTOMER ->
                        Triple(NonVegRed, Color(0xFF2B0D0D), "✗ Customer Declined — PREPARE NORMAL FOOD")
                    else -> Triple(Color.Gray, Color(0xFF1A1A1A), "")
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Items & Customisation Alerts
            order.items.forEach { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            VegNonVegSymbol(isVeg = item.isVeg)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${item.quantity}x ${item.name}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = WarmIvory
                            )
                        }
                        Text(
                            text = "Spice: ${item.spicePreference}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                    }

                    // CRITICAL: Customised Disliked Ingredients to Exclude
                    if (item.dislikedIngredients.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF4A1010))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = Color(0xFFFF8A80),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "EXCLUDE: ${item.dislikedIngredients.joinToString(", ")}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF8A80)
                                    )
                                )
                            }
                        }
                    }

                    // CRITICAL: Chef message / note
                    if (item.chefNote.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF332508))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Note: \"${item.chefNote}\"",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View Bill Button
                OutlinedButton(
                    onClick = onViewBill,
                    modifier = Modifier.testTag("ticket_view_bill_${order.orderId}"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = "Bill",
                        modifier = Modifier.size(16.dp),
                        tint = WarmIvory
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bill",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmIvory
                    )
                }

                // Workflow Action Button
                when (order.status) {
                    OrderStatus.RECEIVED -> {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ticket_accept_btn_${order.orderId}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FlameOrange)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Accept & Prepare Order",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    OrderStatus.ACCEPTED_PREPARING -> {
                        Button(
                            onClick = onMarkReady,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ticket_ready_btn_${order.orderId}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mark Ready for Counter",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
                            )
                        }
                    }
                    OrderStatus.READY_FOR_COUNTER -> {
                        Button(
                            onClick = onMarkPaidAtCounter,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ticket_settle_btn_${order.orderId}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mark Paid at Counter",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                    OrderStatus.COMPLETED_SETTLED -> {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1B4D20))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paid & Settled (Retained 7 days)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFFA5D6A7)
                            )
                        }
                    }
                }
            }
        }
    }
}

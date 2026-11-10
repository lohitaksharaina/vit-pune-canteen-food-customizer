package com.example.ui.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.data.model.CustomisedOrderItem
import com.example.data.model.MenuCategory
import com.example.data.model.MenuItem
import com.example.data.model.RestaurantOrder
import com.example.ui.components.DishCard
import com.example.ui.components.VegNonVegSymbol
import com.example.ui.components.WaitlistBanner
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.NonVegRed
import com.example.ui.theme.VegGreen
import com.example.ui.theme.WarmIvory

@Composable
fun CustomerMenuScreen(
    menuItems: List<MenuItem>,
    selectedCategory: MenuCategory,
    onCategorySelect: (MenuCategory) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    vegOnly: Boolean,
    onToggleVeg: () -> Unit,
    nonVegOnly: Boolean,
    onToggleNonVeg: () -> Unit,
    cartItems: List<CustomisedOrderItem>,
    isWaitlistActive: Boolean,
    activeOrdersCount: Int,
    customerOrders: List<RestaurantOrder>,
    unavailableItemIds: Set<String> = emptySet(),
    onOpenCustomiser: (MenuItem) -> Unit,
    onOpenCart: () -> Unit,
    onViewOrderBill: (RestaurantOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCartItems = cartItems.sumOf { it.quantity }
    val totalCartPrice = cartItems.sumOf { it.totalPrice }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Waitlist Notice Banner
            WaitlistBanner(
                isVisible = isWaitlistActive,
                activeOrdersCount = activeOrdersCount,
                maxOrders = 30
            )

            // Search Bar & Dietary Filter Pills
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            text = "Search VIT canteen snacks, misal, thali...",
                            style = MaterialTheme.typography.bodyMedium,
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
                        if (searchQuery.isNotEmpty()) {
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
                        .testTag("menu_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = FlameOrange,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = WarmIvory,
                        unfocusedTextColor = WarmIvory
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Veg / Non-Veg Quick Filters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = vegOnly,
                        onClick = onToggleVeg,
                        leadingIcon = {
                            VegNonVegSymbol(isVeg = true)
                        },
                        label = {
                            Text(
                                text = "Veg Only",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1B4D20),
                            selectedLabelColor = Color(0xFFA5D6A7)
                        ),
                        modifier = Modifier.testTag("filter_veg_button")
                    )

                    FilterChip(
                        selected = nonVegOnly,
                        onClick = onToggleNonVeg,
                        leadingIcon = {
                            VegNonVegSymbol(isVeg = false)
                        },
                        label = {
                            Text(
                                text = "Non-Veg Only",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4A1010),
                            selectedLabelColor = Color(0xFFFF8A80)
                        ),
                        modifier = Modifier.testTag("filter_nonveg_button")
                    )

                    // Recent Active Bills Pill
                    if (customerOrders.isNotEmpty()) {
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onViewOrderBill(customerOrders.first()) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Latest Bill",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }

            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = MenuCategory.values().indexOf(selectedCategory),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = FlameOrange,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    val idx = MenuCategory.values().indexOf(selectedCategory)
                    if (idx in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[idx]),
                            color = FlameOrange,
                            height = 3.dp
                        )
                    }
                }
            ) {
                MenuCategory.values().forEach { category ->
                    val isSelected = category == selectedCategory
                    Tab(
                        selected = isSelected,
                        onClick = { onCategorySelect(category) },
                        text = {
                            Text(
                                text = category.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) FlameOrange else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.testTag("category_tab_${category.name}")
                    )
                }
            }

            // Food Items List
            if (menuItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No dishes found matching your filter.\nTry clearing the search query or dietary filters.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 22.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("menu_dish_list"),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        bottom = if (totalCartItems > 0) 90.dp else 24.dp,
                        top = 8.dp
                    )
                ) {
                    items(menuItems, key = { it.id }) { item ->
                        DishCard(
                            item = item,
                            isAvailable = !unavailableItemIds.contains(item.id),
                            onCustomiseClick = onOpenCustomiser
                        )
                    }
                }
            }
        }

        // Floating Cart Summary Bar
        AnimatedVisibility(
            visible = totalCartItems > 0,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCart() }
                    .testTag("floating_cart_bar"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FlameOrange
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0x33000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "$totalCartItems item${if (totalCartItems > 1) "s" else ""} in cart",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "₹${totalCartPrice.toInt()} (Taxes applied at bill)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFDCC2)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isWaitlistActive) "Waitlist Active" else "View Cart & Bill ➔",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

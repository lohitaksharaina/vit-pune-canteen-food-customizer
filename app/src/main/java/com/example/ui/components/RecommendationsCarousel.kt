package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.MenuItem
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.WarmIvory
import java.util.Calendar

@Composable
fun RecommendationsSection(
    menuItems: List<MenuItem>,
    unavailableItemIds: Set<String>,
    onCustomiseClick: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }

    val (timeSlotTitle, timeSlotSubtitle, recommendedItemIds) = remember(currentHour) {
        when {
            currentHour in 7..11 -> Triple(
                "Fresh Morning Nashta",
                "Warm Puneri breakfast favorites to start college day",
                listOf("vit_snack_01", "vit_snack_02", "vit_snack_03", "vit_snack_04", "vit_bev_02")
            )
            currentHour in 12..16 -> Triple(
                "Campus Lunch Specials",
                "Hearty thalis, combo meals & hot rice bowls",
                listOf("vit_meal_01", "vit_meal_02", "vit_meal_04", "vit_snack_09", "vit_bev_01")
            )
            else -> Triple(
                "Evening Canteen Bites & Chai",
                "Crispy snacks, sandwiches & refreshing coolers",
                listOf("vit_snack_01", "vit_snack_02", "vit_snack_06", "vit_bev_01", "vit_bev_02")
            )
        }
    }

    val recommendedList = remember(menuItems, unavailableItemIds, recommendedItemIds) {
        val matches = menuItems.filter { it.id in recommendedItemIds && !unavailableItemIds.contains(it.id) }
        if (matches.isNotEmpty()) matches else menuItems.filter { !unavailableItemIds.contains(it.id) }.take(5)
    }

    if (recommendedList.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .testTag("recommendations_section")
    ) {
        // Section Title with Clock / Sparkle icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(FlameOrange.copy(alpha = 0.3f), FlameOrange.copy(alpha = 0.1f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = FlameOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = timeSlotTitle,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = WarmIvory
                    )
                    Text(
                        text = timeSlotSubtitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF331A00))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "CHEF PICKS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    ),
                    color = FlameOrange
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Carousel
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(recommendedList, key = { "rec_${it.id}" }) { item ->
                RecommendedDishCard(
                    item = item,
                    onCustomiseClick = onCustomiseClick
                )
            }
        }
    }
}

@Composable
fun RecommendedDishCard(
    item: MenuItem,
    onCustomiseClick: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(170.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCustomiseClick(item) }
            .testTag("recommended_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, Color(0x1FFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Food Image with robust fallback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(105.dp)
            ) {
                // Background category gradient
                FoodCategoryPlaceholder(
                    category = item.category,
                    dishName = item.name,
                    modifier = Modifier.fillMaxSize()
                )

                // High-res local drawable priority
                if (item.drawableResId != null) {
                    Image(
                        painter = painterResource(id = item.drawableResId),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (!item.imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Vignette gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xCC141211))
                            )
                        )
                )

                // Veg symbol
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.TopStart)
                ) {
                    VegNonVegSymbol(isVeg = item.isVeg)
                }

                // Price pill
                Box(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.BottomEnd)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xE6141211))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "₹${item.price.toInt()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = FlameOrange,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Info section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = WarmIvory,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.category.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.MenuCategory
import com.example.data.model.MenuItem
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.NonVegRed
import com.example.ui.theme.VegGreen
import com.example.ui.theme.WarmIvory

@Composable
fun FoodCategoryPlaceholder(
    category: MenuCategory,
    dishName: String,
    modifier: Modifier = Modifier
) {
    val gradient = when (category) {
        MenuCategory.PUNERI_NASHTA -> listOf(Color(0xFF5A1E08), Color(0xFF260D04))
        MenuCategory.SANDWICH_MAGGI -> listOf(Color(0xFF52280A), Color(0xFF261204))
        MenuCategory.MEALS_THALI -> listOf(Color(0xFF4A2A06), Color(0xFF1F1203))
        MenuCategory.SOUTH_INDIAN -> listOf(Color(0xFF214418), Color(0xFF0F200B))
        MenuCategory.RICE_NOODLES -> listOf(Color(0xFF521515), Color(0xFF260909))
        MenuCategory.BEVERAGES -> listOf(Color(0xFF1D354D), Color(0xFF0D1824))
        MenuCategory.SWEETS -> listOf(Color(0xFF4A1840), Color(0xFF240A1F))
        else -> listOf(Color(0xFF3E2419), Color(0xFF1E1714))
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(gradient)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalFireDepartment,
                contentDescription = null,
                tint = FlameOrange.copy(alpha = 0.45f),
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = category.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = WarmIvory.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
fun DishCard(
    item: MenuItem,
    isAvailable: Boolean = true,
    onCustomiseClick: (MenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, Color(0x1FFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .testTag("dish_card_${item.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Visual header image or graceful category gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                // Base layer: Always visible appetizing gradient
                FoodCategoryPlaceholder(
                    category = item.category,
                    dishName = item.name,
                    modifier = Modifier.fillMaxSize()
                )

                // Foreground photo layer: bundled drawable takes immediate priority
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

                // Smooth gradient vignette at bottom of image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xE6141211))
                            )
                        )
                )

                // Veg / Non-Veg Indicator Badge
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.TopStart)
                ) {
                    VegNonVegSymbol(isVeg = item.isVeg)
                }

                // Custom badge or Sold Out badge
                if (!isAvailable) {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NonVegRed)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SOLD OUT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color.White
                        )
                    }
                } else if (item.badge != null) {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .align(Alignment.TopEnd)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(FlameOrange, Color(0xFFD84315))
                                )
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = item.badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = Color.White
                        )
                    }
                }
            }

            // Card content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = WarmIvory,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "₹${item.price.toInt()}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = FlameOrange
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Customisable items preview tag
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = FlameOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Customise: Exclude ${item.dislikableIngredients.take(3).joinToString(", ")} & more",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Row: Spice Meter & Customise & Add Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Spice flames
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(item.spiciness.coerceAtLeast(1)) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Spicy",
                                tint = if (item.spiciness >= 2) FlameOrange else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (item.spiciness) {
                                0 -> "Mild"
                                1 -> "Medium"
                                2 -> "Spicy"
                                else -> "Fiery"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Customise & Add Button
                    Button(
                        onClick = { if (isAvailable) onCustomiseClick(item) },
                        enabled = isAvailable,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FlameOrange,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("customise_add_button_${item.id}")
                    ) {
                        if (isAvailable) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Customise & Add",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        } else {
                            Text(
                                text = "Sold Out",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VegNonVegSymbol(isVeg: Boolean) {
    val borderColor = if (isVeg) VegGreen else NonVegRed
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xE6141211))
            .border(1.5.dp, borderColor, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(borderColor)
        )
    }
}

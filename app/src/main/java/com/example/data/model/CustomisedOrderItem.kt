package com.example.data.model

import java.util.UUID

data class CustomisedOrderItem(
    val id: String = UUID.randomUUID().toString(),
    val menuItemId: String,
    val name: String,
    val unitPrice: Double,
    val quantity: Int = 1,
    val isVeg: Boolean,
    val dislikedIngredients: List<String> = emptyList(),
    val chefNote: String = "",
    val spicePreference: String = "Normal"
) {
    val totalPrice: Double
        get() = unitPrice * quantity
}

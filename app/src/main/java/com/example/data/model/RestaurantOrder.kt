package com.example.data.model

enum class OrderStatus(val label: String, val stepIndex: Int) {
    RECEIVED("Order Received", 0),
    ACCEPTED_PREPARING("Cook Accepted / Preparing", 1),
    READY_FOR_COUNTER("Ready for Counter", 2),
    COMPLETED_SETTLED("Counter Paid & Settled", 3)
}

enum class CustomizationPricingStatus(val label: String) {
    NONE("Standard Price"),
    PRICE_PROPOSED_BY_ADMIN("Price Review Proposed"),
    ACCEPTED_BY_CUSTOMER("Customer Accepted Custom Price"),
    DECLINED_BY_CUSTOMER("Customer Declined (Normal Food)")
}

data class RestaurantOrder(
    val orderId: String,
    val orderNumber: Int, // e.g. 101, 102...
    val customerName: String,
    val tableNumber: String,
    val customerPhone: String = "",
    val items: List<CustomisedOrderItem>,
    val subtotal: Double,
    val gst: Double,
    val totalAmount: Double,
    val status: OrderStatus = OrderStatus.RECEIVED,
    val isPaidAtCounter: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val chefNotes: String? = null,
    val customizationPricingStatus: CustomizationPricingStatus = CustomizationPricingStatus.NONE,
    val proposedCustomPrice: Double? = null,
    val priceAdjustmentNote: String? = null,
    val isCustomizedFoodActive: Boolean = true
) {
    val totalItemsCount: Int
        get() = items.sumOf { it.quantity }

    val hasCustomizations: Boolean
        get() = items.any { it.dislikedIngredients.isNotEmpty() || it.chefNote.isNotBlank() || it.spicePreference != "Normal" }
}

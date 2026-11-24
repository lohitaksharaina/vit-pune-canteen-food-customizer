package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.CustomisedOrderItem
import com.example.data.model.CustomizationPricingStatus
import com.example.data.model.OrderStatus
import com.example.data.model.RestaurantOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

class OrderRepository private constructor() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var firestoreRepo: FirestoreRestaurantRepository? = null

    private val _orders = MutableStateFlow<List<RestaurantOrder>>(emptyList())
    val orders: StateFlow<List<RestaurantOrder>> = _orders.asStateFlow()

    private val _cart = MutableStateFlow<List<CustomisedOrderItem>>(emptyList())
    val cart: StateFlow<List<CustomisedOrderItem>> = _cart.asStateFlow()

    private val _unavailableItemIds = MutableStateFlow<Set<String>>(emptySet())
    val unavailableItemIds: StateFlow<Set<String>> = _unavailableItemIds.asStateFlow()

    private val _newOrderEvents = MutableSharedFlow<RestaurantOrder>(extraBufferCapacity = 64)
    val newOrderEvents: SharedFlow<RestaurantOrder> = _newOrderEvents.asSharedFlow()

    private val _syncChannel = MutableStateFlow("VIT-PUNE-CANTEEN")
    val syncChannel: StateFlow<String> = _syncChannel.asStateFlow()

    private val _isCloudSyncConnected = MutableStateFlow(true)
    val isCloudSyncConnected: StateFlow<Boolean> = _isCloudSyncConnected.asStateFlow()

    private val _lastPruneTimestamp = MutableStateFlow(System.currentTimeMillis())
    val lastPruneTimestamp: StateFlow<Long> = _lastPruneTimestamp.asStateFlow()

    private val _prunedCount = MutableStateFlow(0)
    val prunedCount: StateFlow<Int> = _prunedCount.asStateFlow()

    private val nextOrderSequence = AtomicInteger(103)

    init {
        seedInitialDemoOrders()
        purgeOldSettledOrders()
    }

    companion object {
        private const val TAG = "OrderRepository"
        const val MAX_ACTIVE_ORDERS = 30
        const val RETENTION_PERIOD_MILLIS = 7L * 24 * 60 * 60 * 1000L // 7 days

        @Volatile
        private var instance: OrderRepository? = null

        fun getInstance(): OrderRepository {
            return instance ?: synchronized(this) {
                instance ?: OrderRepository().also { instance = it }
            }
        }
    }

    fun initFirestore(context: Context) {
        if (firestoreRepo == null) {
            try {
                firestoreRepo = FirestoreRestaurantRepository.getInstance(context.applicationContext)
                // Seed menu in Firestore in background
                scope.launch {
                    firestoreRepo?.seedMenuCatalogIfEmpty()
                    listenToFirestoreOrders()
                    listenToFirestoreAvailability()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to initialize Firestore", e)
            }
        }
    }

    private fun listenToFirestoreOrders() {
        val repo = firestoreRepo ?: return
        scope.launch {
            try {
                repo.observeOrdersRealtime(_syncChannel.value).collect { remoteOrders ->
                    if (remoteOrders.isNotEmpty()) {
                        val existingIds = _orders.value.map { it.orderId }.toSet()
                        // Check for new incoming orders to trigger notifications
                        remoteOrders.forEach { remote ->
                            if (!existingIds.contains(remote.orderId) && remote.status == OrderStatus.RECEIVED) {
                                _newOrderEvents.tryEmit(remote)
                            }
                        }
                        // Merge remote orders with local list
                        val currentMap = _orders.value.associateBy { it.orderId }.toMutableMap()
                        remoteOrders.forEach { remote ->
                            currentMap[remote.orderId] = remote
                        }
                        _orders.value = currentMap.values.sortedByDescending { it.createdAt }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error in Firestore real-time observation", e)
            }
        }
    }

    private fun listenToFirestoreAvailability() {
        val repo = firestoreRepo ?: return
        scope.launch {
            try {
                repo.observeUnavailableItemsRealtime(_syncChannel.value).collect { ids ->
                    _unavailableItemIds.value = ids
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error in Firestore availability observation", e)
            }
        }
    }

    fun getActiveOrdersCount(): Int {
        return _orders.value.count { it.status != OrderStatus.COMPLETED_SETTLED }
    }

    fun isWaitlistActive(): Boolean {
        return getActiveOrdersCount() >= MAX_ACTIVE_ORDERS
    }

    // Cart operations
    fun addToCart(item: CustomisedOrderItem) {
        val current = _cart.value.toMutableList()
        current.add(item)
        _cart.value = current
    }

    fun updateCartItemQuantity(itemId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(itemId)
            return
        }
        _cart.value = _cart.value.map {
            if (it.id == itemId) it.copy(quantity = newQuantity) else it
        }
    }

    fun removeFromCart(itemId: String) {
        _cart.value = _cart.value.filterNot { it.id == itemId }
    }

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun placeOrder(
        customerName: String,
        tableNumber: String,
        customerPhone: String = ""
    ): Result<RestaurantOrder> {
        purgeOldSettledOrders()

        if (isWaitlistActive()) {
            return Result.failure(
                IllegalStateException("Kitchen capacity reached ($MAX_ACTIVE_ORDERS active orders). Waitlist is currently active. Please try again when active orders drop below 30.")
            )
        }

        val cartItems = _cart.value
        if (cartItems.isEmpty()) {
            return Result.failure(IllegalStateException("Cart is empty."))
        }

        val orderNum = nextOrderSequence.getAndIncrement()
        val datePrefix = SimpleDateFormat("ddMM", Locale.getDefault()).format(Date())
        val orderId = "VIT-CAN-$datePrefix-$orderNum"

        val subtotal = cartItems.sumOf { it.totalPrice }
        val gst = subtotal * 0.05
        val total = subtotal + gst

        val newOrder = RestaurantOrder(
            orderId = orderId,
            orderNumber = orderNum,
            customerName = customerName.ifBlank { "Guest" },
            tableNumber = tableNumber.ifBlank { "Table T-12" },
            customerPhone = customerPhone,
            items = cartItems,
            subtotal = subtotal,
            gst = gst,
            totalAmount = total,
            status = OrderStatus.RECEIVED,
            isPaidAtCounter = false,
            createdAt = System.currentTimeMillis()
        )

        val updated = _orders.value.toMutableList()
        updated.add(0, newOrder)
        _orders.value = updated
        clearCart()
        _newOrderEvents.tryEmit(newOrder)

        // Sync to Firestore in real-time
        firestoreRepo?.let { repo ->
            scope.launch {
                try {
                    repo.saveOrder(newOrder, _syncChannel.value)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed syncing placed order to Firestore", e)
                }
            }
        }

        return Result.success(newOrder)
    }

    fun acceptOrder(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.ACCEPTED_PREPARING)
    }

    fun markOrderReady(orderId: String) {
        updateOrderStatus(orderId, OrderStatus.READY_FOR_COUNTER)
    }

    fun markOrderPaidAtCounter(orderId: String) {
        val now = System.currentTimeMillis()
        _orders.value = _orders.value.map { order ->
            if (order.orderId == orderId) {
                order.copy(
                    status = OrderStatus.COMPLETED_SETTLED,
                    isPaidAtCounter = true,
                    completedAt = now
                )
            } else {
                order
            }
        }

        // Sync to Firestore in real-time
        firestoreRepo?.let { repo ->
            scope.launch {
                try {
                    repo.updateOrderStatus(orderId, OrderStatus.COMPLETED_SETTLED, isPaidAtCounter = true)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed syncing paid order to Firestore", e)
                }
            }
        }

        purgeOldSettledOrders()
    }

    private fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.value = _orders.value.map {
            if (it.orderId == orderId) it.copy(status = newStatus) else it
        }

        // Sync to Firestore in real-time
        firestoreRepo?.let { repo ->
            scope.launch {
                try {
                    repo.updateOrderStatus(orderId, newStatus)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed syncing status update to Firestore", e)
                }
            }
        }
    }

    fun purgeOldSettledOrders(): Int {
        val now = System.currentTimeMillis()
        val cutoff = now - RETENTION_PERIOD_MILLIS
        val beforeCount = _orders.value.size

        val retained = _orders.value.filterNot { order ->
            order.status == OrderStatus.COMPLETED_SETTLED &&
                order.isPaidAtCounter &&
                (order.completedAt ?: 0L) < cutoff
        }

        val removed = beforeCount - retained.size
        if (removed > 0) {
            _orders.value = retained
            _prunedCount.value = _prunedCount.value + removed
        }
        _lastPruneTimestamp.value = now

        // Also purge remote Firestore records
        firestoreRepo?.let { repo ->
            scope.launch {
                try {
                    repo.purgeOldSettledOrders()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed remote Firestore purge", e)
                }
            }
        }

        return removed
    }

    fun toggleItemAvailability(itemId: String) {
        val current = _unavailableItemIds.value
        val updated = if (current.contains(itemId)) current - itemId else current + itemId
        _unavailableItemIds.value = updated
        firestoreRepo?.let { repo ->
            scope.launch {
                repo.syncUnavailableItems(_syncChannel.value, updated)
            }
        }
    }

    fun proposeCustomPrice(orderId: String, newPrice: Double, note: String) {
        _orders.value = _orders.value.map { order ->
            if (order.orderId == orderId) {
                order.copy(
                    customizationPricingStatus = CustomizationPricingStatus.PRICE_PROPOSED_BY_ADMIN,
                    proposedCustomPrice = newPrice,
                    priceAdjustmentNote = note
                )
            } else order
        }
        firestoreRepo?.let { repo ->
            scope.launch {
                repo.proposeCustomPrice(orderId, newPrice, note)
            }
        }
    }

    fun respondToCustomPrice(orderId: String, accept: Boolean) {
        val target = _orders.value.find { it.orderId == orderId } ?: return
        val proposedPrice = target.proposedCustomPrice
        _orders.value = _orders.value.map { order ->
            if (order.orderId == orderId) {
                if (accept && proposedPrice != null && proposedPrice > 0) {
                    val newGst = proposedPrice * 0.05
                    val newTotal = proposedPrice + newGst
                    order.copy(
                        customizationPricingStatus = CustomizationPricingStatus.ACCEPTED_BY_CUSTOMER,
                        subtotal = proposedPrice,
                        gst = newGst,
                        totalAmount = newTotal,
                        isCustomizedFoodActive = true
                    )
                } else {
                    order.copy(
                        customizationPricingStatus = CustomizationPricingStatus.DECLINED_BY_CUSTOMER,
                        isCustomizedFoodActive = false
                    )
                }
            } else order
        }
        firestoreRepo?.let { repo ->
            scope.launch {
                repo.respondToCustomPrice(orderId, accept, proposedPrice, target.subtotal)
            }
        }
    }

    suspend fun uploadMonthlyReport(reportData: Map<String, Any>): String {
        return firestoreRepo?.uploadMonthlyReport(_syncChannel.value, reportData) ?: ""
    }

    fun setSyncChannel(channel: String) {
        _syncChannel.value = channel
        listenToFirestoreOrders()
    }

    fun toggleCloudSync() {
        _isCloudSyncConnected.value = !_isCloudSyncConnected.value
    }

    fun fillOrdersToTestWaitlist() {
        val currentActive = getActiveOrdersCount()
        val needed = MAX_ACTIVE_ORDERS - currentActive
        if (needed <= 0) return

        val dummyOrders = (1..needed).map { idx ->
            val num = nextOrderSequence.getAndIncrement()
            RestaurantOrder(
                orderId = "VIT-TEST-$num",
                orderNumber = num,
                customerName = "Canteen Student #$idx",
                tableNumber = "Table ${10 + idx}",
                items = listOf(
                    CustomisedOrderItem(
                        menuItemId = "vit_snack_01",
                        name = "Puneri Special Misal Pav",
                        unitPrice = 50.0,
                        quantity = 1,
                        isVeg = true,
                        dislikedIngredients = listOf("Onions (Kanda)"),
                        chefNote = "VIT canteen rush hour counter ticket"
                    )
                ),
                subtotal = 50.0,
                gst = 2.50,
                totalAmount = 52.50,
                status = OrderStatus.ACCEPTED_PREPARING,
                isPaidAtCounter = false,
                createdAt = System.currentTimeMillis() - (idx * 60_000L)
            )
        }
        val current = _orders.value.toMutableList()
        current.addAll(0, dummyOrders)
        _orders.value = current
    }

    fun clearActiveWaitlistDemoOrders() {
        _orders.value = _orders.value.filter {
            !it.orderId.startsWith("VIT-TEST-")
        }
    }

    private fun seedInitialDemoOrders() {
        val initialList = listOf(
            RestaurantOrder(
                orderId = "VIT-CAN-101",
                orderNumber = 101,
                customerName = "Tanmay Patil",
                tableNumber = "Table 04",
                customerPhone = "9822012345",
                items = listOf(
                    CustomisedOrderItem(
                        menuItemId = "vit_snack_01",
                        name = "Puneri Special Misal Pav",
                        unitPrice = 50.0,
                        quantity = 2,
                        isVeg = true,
                        dislikedIngredients = listOf("Onions (Kanda)"),
                        chefNote = "Extra crispy pav, medium spicy rassa.",
                        spicePreference = "Normal"
                    ),
                    CustomisedOrderItem(
                        menuItemId = "vit_bev_01",
                        name = "Pune Special Thick Cold Coffee",
                        unitPrice = 40.0,
                        quantity = 2,
                        isVeg = true,
                        dislikedIngredients = listOf("Extra Ice"),
                        chefNote = "Extra chocolate drizzle please.",
                        spicePreference = "Mild"
                    )
                ),
                subtotal = 180.0,
                gst = 9.00,
                totalAmount = 189.00,
                status = OrderStatus.ACCEPTED_PREPARING,
                isPaidAtCounter = false,
                createdAt = System.currentTimeMillis() - (15 * 60 * 1000L)
            ),
            RestaurantOrder(
                orderId = "VIT-CAN-102",
                orderNumber = 102,
                customerName = "Shruti Kulkarni",
                tableNumber = "Table 09",
                customerPhone = "9876543210",
                items = listOf(
                    CustomisedOrderItem(
                        menuItemId = "vit_ff_01",
                        name = "Veg Cheese Grilled Sandwich",
                        unitPrice = 70.0,
                        quantity = 1,
                        isVeg = true,
                        dislikedIngredients = listOf("Capsicum / Bell Peppers"),
                        chefNote = "Grilled extra crisp with mint chutney.",
                        spicePreference = "Normal"
                    ),
                    CustomisedOrderItem(
                        menuItemId = "vit_bev_02",
                        name = "Kadak Special Cutting Chai",
                        unitPrice = 15.0,
                        quantity = 2,
                        isVeg = true,
                        chefNote = "Adrak chai extra hot."
                    )
                ),
                subtotal = 100.0,
                gst = 5.00,
                totalAmount = 105.00,
                status = OrderStatus.READY_FOR_COUNTER,
                isPaidAtCounter = false,
                createdAt = System.currentTimeMillis() - (25 * 60 * 1000L)
            ),
            RestaurantOrder(
                orderId = "VIT-CAN-100",
                orderNumber = 100,
                customerName = "Aditya Deshmukh",
                tableNumber = "Table 02",
                customerPhone = "9823098765",
                items = listOf(
                    CustomisedOrderItem(
                        menuItemId = "vit_meal_01",
                        name = "VIT Student Special Thali",
                        unitPrice = 95.0,
                        quantity = 1,
                        isVeg = true,
                        dislikedIngredients = listOf("Garlic (Lasun)"),
                        chefNote = "3 hot chapatis, extra dal tadka."
                    )
                ),
                subtotal = 95.0,
                gst = 4.75,
                totalAmount = 99.75,
                status = OrderStatus.COMPLETED_SETTLED,
                isPaidAtCounter = true,
                createdAt = System.currentTimeMillis() - (2 * 60 * 60 * 1000L),
                completedAt = System.currentTimeMillis() - (1 * 60 * 60 * 1000L)
            )
        )
        _orders.value = initialList
    }
}

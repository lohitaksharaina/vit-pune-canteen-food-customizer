package com.example.ui.viewmodel

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.data.MenuCatalog
import com.example.data.model.CustomisedOrderItem
import com.example.data.model.CustomizationPricingStatus
import com.example.data.model.MenuCategory
import com.example.data.model.MenuItem
import com.example.data.model.OrderStatus
import com.example.data.model.RestaurantOrder
import com.example.data.repository.OrderRepository
import com.example.util.MonthlyRevenueReport
import com.example.util.RevenueReportGenerator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.model.AuthUser
import com.example.data.model.DemoCredentials
import com.example.data.model.UserRole

enum class AppMode {
    CUSTOMER,
    CHEF,
    ADMIN
}

class RestaurantViewModel : ViewModel() {

    private val repository = OrderRepository.getInstance()

    // Authentication state
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    // Mode Switcher
    private val _currentMode = MutableStateFlow(AppMode.CUSTOMER)
    val currentMode: StateFlow<AppMode> = _currentMode.asStateFlow()

    // Menu Stock / Availability Management — synced from Firestore via Repository
    val unavailableItemIds: StateFlow<Set<String>> = repository.unavailableItemIds

    // Authenticated Google Account for Cloud Sync
    private val _userEmail = MutableStateFlow<String?>(Firebase.auth.currentUser?.email ?: AuthManager.TARGET_GMAIL)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    // Menu state
    val allMenuItems = MenuCatalog.items

    private val _selectedCategory = MutableStateFlow(MenuCategory.ALL)
    val selectedCategory: StateFlow<MenuCategory> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _vegOnlyFilter = MutableStateFlow(false)
    val vegOnlyFilter: StateFlow<Boolean> = _vegOnlyFilter.asStateFlow()

    private val _nonVegOnlyFilter = MutableStateFlow(false)
    val nonVegOnlyFilter: StateFlow<Boolean> = _nonVegOnlyFilter.asStateFlow()

    // Filtered menu items
    val filteredMenuItems: StateFlow<List<MenuItem>> = combine(
        _selectedCategory,
        _searchQuery,
        _vegOnlyFilter,
        _nonVegOnlyFilter
    ) { category, query, vegOnly, nonVegOnly ->
        allMenuItems.filter { item ->
            val matchesCategory = category == MenuCategory.ALL || item.category == category
            val matchesQuery = query.isBlank() ||
                item.name.contains(query, ignoreCase = true) ||
                item.description.contains(query, ignoreCase = true)
            val matchesDiet = when {
                vegOnly -> item.isVeg
                nonVegOnly -> !item.isVeg
                else -> true
            }
            matchesCategory && matchesQuery && matchesDiet
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allMenuItems)

    // Customisation sheet state
    private val _selectedItemForCustomisation = MutableStateFlow<MenuItem?>(null)
    val selectedItemForCustomisation: StateFlow<MenuItem?> = _selectedItemForCustomisation.asStateFlow()

    private val _customiseQuantity = MutableStateFlow(1)
    val customiseQuantity: StateFlow<Int> = _customiseQuantity.asStateFlow()

    private val _excludedIngredients = MutableStateFlow<Set<String>>(emptySet())
    val excludedIngredients: StateFlow<Set<String>> = _excludedIngredients.asStateFlow()

    private val _chefNote = MutableStateFlow("")
    val chefNote: StateFlow<String> = _chefNote.asStateFlow()

    private val _spicePreference = MutableStateFlow("Normal")
    val spicePreference: StateFlow<String> = _spicePreference.asStateFlow()

    // Cart & Checkout
    val cart: StateFlow<List<CustomisedOrderItem>> = repository.cart

    private val _isCartSheetOpen = MutableStateFlow(false)
    val isCartSheetOpen: StateFlow<Boolean> = _isCartSheetOpen.asStateFlow()

    private val _customerName = MutableStateFlow("")
    val customerName: StateFlow<String> = _customerName.asStateFlow()

    private val _tableNumber = MutableStateFlow("Table 05")
    val tableNumber: StateFlow<String> = _tableNumber.asStateFlow()

    private val _customerPhone = MutableStateFlow("")
    val customerPhone: StateFlow<String> = _customerPhone.asStateFlow()

    // Generated Bill Modal
    private val _activeGeneratedBill = MutableStateFlow<RestaurantOrder?>(null)
    val activeGeneratedBill: StateFlow<RestaurantOrder?> = _activeGeneratedBill.asStateFlow()

    // Orders state from Repository
    val orders: StateFlow<List<RestaurantOrder>> = repository.orders

    // Capacity & Waitlist states
    val isWaitlistActive: StateFlow<Boolean> = combine(orders) {
        repository.isWaitlistActive()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.isWaitlistActive())

    val activeOrdersCount: StateFlow<Int> = combine(orders) {
        repository.getActiveOrdersCount()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getActiveOrdersCount())

    // Chef UI states
    private val _chefStatusFilter = MutableStateFlow<OrderStatus?>(null)
    val chefStatusFilter: StateFlow<OrderStatus?> = _chefStatusFilter.asStateFlow()

    private val _chefSearchQuery = MutableStateFlow("")
    val chefSearchQuery: StateFlow<String> = _chefSearchQuery.asStateFlow()

    // Cloud Sync & Connectivity states
    val syncChannel = repository.syncChannel
    val isCloudSyncConnected = repository.isCloudSyncConnected
    val lastPruneTimestamp = repository.lastPruneTimestamp
    val prunedCount = repository.prunedCount

    private val _isSyncSettingsOpen = MutableStateFlow(false)
    val isSyncSettingsOpen: StateFlow<Boolean> = _isSyncSettingsOpen.asStateFlow()

    // Notification toast / error message
    private val _statusBannerMessage = MutableStateFlow<String?>(null)
    val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

    // Feedback modal state
    private val _isFeedbackSheetOpen = MutableStateFlow(false)
    val isFeedbackSheetOpen: StateFlow<Boolean> = _isFeedbackSheetOpen.asStateFlow()

    private val _feedbackRating = MutableStateFlow(5)
    val feedbackRating: StateFlow<Int> = _feedbackRating.asStateFlow()

    private val _feedbackComment = MutableStateFlow("")
    val feedbackComment: StateFlow<String> = _feedbackComment.asStateFlow()

    private val _feedbackSubmitted = MutableStateFlow(false)
    val feedbackSubmitted: StateFlow<Boolean> = _feedbackSubmitted.asStateFlow()

    // Notification events for new orders (CHEF / ADMIN will trigger push notifications)
    private val _notifyNewOrder = MutableSharedFlow<RestaurantOrder>(extraBufferCapacity = 64)
    val notifyNewOrder: SharedFlow<RestaurantOrder> = _notifyNewOrder.asSharedFlow()

    // Revenue report state
    private val _activeRevenueReport = MutableStateFlow<MonthlyRevenueReport?>(null)
    val activeRevenueReport: StateFlow<MonthlyRevenueReport?> = _activeRevenueReport.asStateFlow()

    private val _isRevenueReportOpen = MutableStateFlow(false)
    val isRevenueReportOpen: StateFlow<Boolean> = _isRevenueReportOpen.asStateFlow()

    // Custom price proposal state (Admin proposes → Customer sees)
    private val _priceAdjustmentTargetOrder = MutableStateFlow<RestaurantOrder?>(null)
    val priceAdjustmentTargetOrder: StateFlow<RestaurantOrder?> = _priceAdjustmentTargetOrder.asStateFlow()

    private val _pendingPriceProposalOrder = MutableStateFlow<RestaurantOrder?>(null)
    val pendingPriceProposalOrder: StateFlow<RestaurantOrder?> = _pendingPriceProposalOrder.asStateFlow()

    fun initFirestore(context: Context) {
        repository.initFirestore(context)
        _userEmail.value = Firebase.auth.currentUser?.email ?: AuthManager.TARGET_GMAIL
        // Listen for new order events and forward for notification
        viewModelScope.launch {
            repository.newOrderEvents.collect { order ->
                _notifyNewOrder.tryEmit(order)
            }
        }
        // Track pending price proposals for STUDENT users
        viewModelScope.launch {
            repository.orders.collect { orderList ->
                val proposed = orderList.firstOrNull {
                    it.customizationPricingStatus == CustomizationPricingStatus.PRICE_PROPOSED_BY_ADMIN
                }
                _pendingPriceProposalOrder.value = proposed
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        val credentialManager = CredentialManager.create(context)
        AuthManager.onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { email ->
                _userEmail.value = email
                showTransientStatus("Signed in with Google: $email")
            },
            onAuthError = { err ->
                showTransientStatus("Google Sign-In note: $err")
            },
            scope = viewModelScope
        )
    }

    fun signOut(context: Context) {
        val credentialManager = CredentialManager.create(context)
        AuthManager.signOut(
            context = context,
            credentialManager = credentialManager,
            onSignOutComplete = {
                _userEmail.value = null
                showTransientStatus("Signed out")
            },
            scope = viewModelScope
        )
    }

    // Mode operations
    fun setMode(mode: AppMode) {
        _currentMode.value = mode
    }

    // Filter operations
    fun selectCategory(category: MenuCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleVegFilter() {
        if (!_vegOnlyFilter.value) {
            _vegOnlyFilter.value = true
            _nonVegOnlyFilter.value = false
        } else {
            _vegOnlyFilter.value = false
        }
    }

    fun toggleNonVegFilter() {
        if (!_nonVegOnlyFilter.value) {
            _nonVegOnlyFilter.value = true
            _vegOnlyFilter.value = false
        } else {
            _nonVegOnlyFilter.value = false
        }
    }

    // Customisation flow
    fun openCustomisation(item: MenuItem) {
        _selectedItemForCustomisation.value = item
        _customiseQuantity.value = 1
        _excludedIngredients.value = emptySet()
        _chefNote.value = ""
        _spicePreference.value = when (item.spiciness) {
            0 -> "Mild"
            3 -> "Extra Fiery"
            else -> "Normal"
        }
    }

    fun dismissCustomisation() {
        _selectedItemForCustomisation.value = null
    }

    fun toggleIngredientExclusion(ingredient: String) {
        val current = _excludedIngredients.value.toMutableSet()
        if (current.contains(ingredient)) {
            current.remove(ingredient)
        } else {
            current.add(ingredient)
        }
        _excludedIngredients.value = current
    }

    fun setCustomiseQuantity(qty: Int) {
        if (qty in 1..20) {
            _customiseQuantity.value = qty
        }
    }

    fun setChefNote(note: String) {
        _chefNote.value = note
    }

    fun setSpicePreference(spice: String) {
        _spicePreference.value = spice
    }

    fun confirmAddToCart() {
        val item = _selectedItemForCustomisation.value ?: return
        val orderItem = CustomisedOrderItem(
            menuItemId = item.id,
            name = item.name,
            unitPrice = item.price,
            quantity = _customiseQuantity.value,
            isVeg = item.isVeg,
            dislikedIngredients = _excludedIngredients.value.toList(),
            chefNote = _chefNote.value.trim(),
            spicePreference = _spicePreference.value
        )
        repository.addToCart(orderItem)
        dismissCustomisation()
        showTransientStatus("Added ${orderItem.quantity}x ${item.name} to Cart")
    }

    // Cart operations
    fun openCart() {
        _isCartSheetOpen.value = true
    }

    fun dismissCart() {
        _isCartSheetOpen.value = false
    }

    fun updateCartItemQuantity(id: String, qty: Int) {
        repository.updateCartItemQuantity(id, qty)
    }

    fun removeCartItem(id: String) {
        repository.removeFromCart(id)
    }

    fun setCustomerName(name: String) {
        _customerName.value = name
    }

    fun setTableNumber(table: String) {
        _tableNumber.value = table
    }

    fun setCustomerPhone(phone: String) {
        _customerPhone.value = phone
    }

    fun checkoutAndGenerateBill() {
        if (repository.isWaitlistActive()) {
            showTransientStatus("⚠️ Kitchen capacity full (30 active orders). Please wait before checkout.")
            return
        }

        val result = repository.placeOrder(
            customerName = _customerName.value,
            tableNumber = _tableNumber.value,
            customerPhone = _customerPhone.value
        )

        result.onSuccess { order ->
            _isCartSheetOpen.value = false
            _activeGeneratedBill.value = order
            showTransientStatus("Bill generated for ${order.orderId}! Show at counter to pay.")
        }.onFailure { err ->
            showTransientStatus("Error: ${err.message}")
        }
    }

    fun viewBill(order: RestaurantOrder) {
        _activeGeneratedBill.value = order
    }

    fun dismissBill() {
        _activeGeneratedBill.value = null
    }

    // Chef actions
    fun setChefStatusFilter(status: OrderStatus?) {
        _chefStatusFilter.value = status
    }

    fun setChefSearchQuery(q: String) {
        _chefSearchQuery.value = q
    }

    fun chefAcceptOrder(orderId: String) {
        repository.acceptOrder(orderId)
        showTransientStatus("Order $orderId accepted & being prepared in kitchen.")
    }

    fun chefMarkReady(orderId: String) {
        repository.markOrderReady(orderId)
        showTransientStatus("Order $orderId marked ready for counter.")
    }

    fun chefMarkPaidAtCounter(orderId: String) {
        repository.markOrderPaidAtCounter(orderId)
        showTransientStatus("Order $orderId settled at store counter.")
    }

    fun triggerAutoPurgeNow() {
        val count = repository.purgeOldSettledOrders()
        showTransientStatus("Auto-purge checked. $count old settled orders removed (>7 days).")
    }

    fun fillWaitlistForTesting() {
        repository.fillOrdersToTestWaitlist()
        showTransientStatus("Active orders filled to 30. Waitlist banner active!")
    }

    fun resetWaitlistOrders() {
        repository.clearActiveWaitlistDemoOrders()
        showTransientStatus("Test orders cleared. Capacity available.")
    }

    // Sync settings
    fun openSyncSettings() {
        _isSyncSettingsOpen.value = true
    }

    fun dismissSyncSettings() {
        _isSyncSettingsOpen.value = false
    }

    fun setSyncChannel(code: String) {
        repository.setSyncChannel(code)
    }

    fun toggleCloudSync() {
        repository.toggleCloudSync()
    }

    // Feedback
    fun openFeedback() {
        _isFeedbackSheetOpen.value = true
        _feedbackSubmitted.value = false
    }

    fun dismissFeedback() {
        _isFeedbackSheetOpen.value = false
    }

    fun setFeedbackRating(stars: Int) {
        _feedbackRating.value = stars
    }

    fun setFeedbackComment(c: String) {
        _feedbackComment.value = c
    }

    fun submitFeedback() {
        _feedbackSubmitted.value = true
        showTransientStatus("Thank you for your VIT Pune canteen feedback!")
    }

    // Authentication Actions
    fun login(username: String, pass: String): Boolean {
        val user = DemoCredentials.authenticate(username, pass)
        return if (user != null) {
            _currentUser.value = user
            when (user.role) {
                UserRole.STUDENT -> {
                    _currentMode.value = AppMode.CUSTOMER
                    _customerName.value = user.displayName
                }
                UserRole.CHEF -> {
                    _currentMode.value = AppMode.CHEF
                }
                UserRole.ADMIN -> {
                    _currentMode.value = AppMode.ADMIN
                }
            }
            showTransientStatus("Welcome, ${user.displayName}! Signed in as ${user.role.badge}.")
            true
        } else {
            showTransientStatus("Invalid credentials. Please select student, cook, or admin demo accounts.")
            false
        }
    }

    fun quickDemoLogin(role: UserRole) {
        when (role) {
            UserRole.STUDENT -> login("student", "vit_student")
            UserRole.CHEF -> login("cook", "vit_cook")
            UserRole.ADMIN -> login("admin", "vit_admin")
        }
    }

    fun logout() {
        val name = _currentUser.value?.displayName ?: "User"
        _currentUser.value = null
        _currentMode.value = AppMode.CUSTOMER
        showTransientStatus("Logged out $name. Demo login active.")
    }

    // Availability / Stock Toggles for Admin — synced to Firestore
    fun toggleItemAvailability(itemId: String) {
        val isCurrentlyUnavailable = unavailableItemIds.value.contains(itemId)
        repository.toggleItemAvailability(itemId)
        if (isCurrentlyUnavailable) {
            showTransientStatus("Item marked In Stock.")
        } else {
            showTransientStatus("Item marked Sold Out (Unavailable).")
        }
    }

    // Revenue Report
    fun generateRevenueReport(context: Context) {
        viewModelScope.launch {
            val report = RevenueReportGenerator.calculateMonthlyReport(context, orders.value)
            _activeRevenueReport.value = report
            _isRevenueReportOpen.value = true
        }
    }

    fun dismissRevenueReport() {
        _isRevenueReportOpen.value = false
    }

    fun uploadRevenueReport(report: MonthlyRevenueReport) {
        viewModelScope.launch {
            repository.uploadMonthlyReport(report.toFirestoreMap())
            showTransientStatus("Report ${report.fileName} uploaded to Firebase!")
        }
    }

    // Custom Price Proposal (Admin → Customer)
    fun openPriceAdjustmentForOrder(order: RestaurantOrder) {
        _priceAdjustmentTargetOrder.value = order
    }

    fun dismissPriceAdjustment() {
        _priceAdjustmentTargetOrder.value = null
    }

    fun proposePriceForOrder(orderId: String, newPrice: Double, note: String) {
        repository.proposeCustomPrice(orderId, newPrice, note)
        _priceAdjustmentTargetOrder.value = null
        showTransientStatus("Price proposal sent to customer for Order #$orderId")
    }

    fun acceptCustomPrice(orderId: String) {
        repository.respondToCustomPrice(orderId, true)
        _pendingPriceProposalOrder.value = null
        showTransientStatus("Custom price accepted! Kitchen will prepare customised dish.")
    }

    fun declineCustomPrice(orderId: String) {
        repository.respondToCustomPrice(orderId, false)
        _pendingPriceProposalOrder.value = null
        showTransientStatus("Standard food will be prepared at original price.")
    }

    fun clearStatusBanner() {
        _statusBannerMessage.value = null
    }

    private fun showTransientStatus(msg: String) {
        _statusBannerMessage.value = msg
    }
}

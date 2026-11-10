package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.auth.LoginScreen
import com.example.ui.chef.ChefDashboardScreen
import com.example.ui.components.CustomerFeedbackDialog
import com.example.ui.components.DigitalBillDialog
import com.example.ui.components.DishCustomiserDialog
import com.example.ui.components.OnlineSyncDialog
import com.example.ui.components.TopRestaurantBar
import com.example.ui.customer.CartBottomSheet
import com.example.ui.customer.CustomerMenuScreen
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WarmIvory
import com.example.ui.viewmodel.AppMode
import com.example.ui.viewmodel.RestaurantViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                RestaurantApp()
            }
        }
    }
}

@Composable
fun RestaurantApp(viewModel: RestaurantViewModel = viewModel()) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val unavailableItemIds by viewModel.unavailableItemIds.collectAsStateWithLifecycle()

    val filteredMenuItems by viewModel.filteredMenuItems.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val vegOnly by viewModel.vegOnlyFilter.collectAsStateWithLifecycle()
    val nonVegOnly by viewModel.nonVegOnlyFilter.collectAsStateWithLifecycle()

    val selectedCustomiseItem by viewModel.selectedItemForCustomisation.collectAsStateWithLifecycle()
    val customiseQuantity by viewModel.customiseQuantity.collectAsStateWithLifecycle()
    val excludedIngredients by viewModel.excludedIngredients.collectAsStateWithLifecycle()
    val chefNote by viewModel.chefNote.collectAsStateWithLifecycle()
    val spicePreference by viewModel.spicePreference.collectAsStateWithLifecycle()

    val cartItems by viewModel.cart.collectAsStateWithLifecycle()
    val isCartOpen by viewModel.isCartSheetOpen.collectAsStateWithLifecycle()
    val customerName by viewModel.customerName.collectAsStateWithLifecycle()
    val tableNumber by viewModel.tableNumber.collectAsStateWithLifecycle()
    val customerPhone by viewModel.customerPhone.collectAsStateWithLifecycle()

    val activeBill by viewModel.activeGeneratedBill.collectAsStateWithLifecycle()
    val orders by viewModel.orders.collectAsStateWithLifecycle()
    val activeOrdersCount by viewModel.activeOrdersCount.collectAsStateWithLifecycle()
    val isWaitlistActive by viewModel.isWaitlistActive.collectAsStateWithLifecycle()

    val chefStatusFilter by viewModel.chefStatusFilter.collectAsStateWithLifecycle()
    val chefSearchQuery by viewModel.chefSearchQuery.collectAsStateWithLifecycle()

    val syncChannel by viewModel.syncChannel.collectAsStateWithLifecycle()
    val isCloudSyncConnected by viewModel.isCloudSyncConnected.collectAsStateWithLifecycle()
    val lastPruneTimestamp by viewModel.lastPruneTimestamp.collectAsStateWithLifecycle()
    val prunedCount by viewModel.prunedCount.collectAsStateWithLifecycle()
    val isSyncSettingsOpen by viewModel.isSyncSettingsOpen.collectAsStateWithLifecycle()

    val isFeedbackOpen by viewModel.isFeedbackSheetOpen.collectAsStateWithLifecycle()
    val feedbackRating by viewModel.feedbackRating.collectAsStateWithLifecycle()
    val feedbackComment by viewModel.feedbackComment.collectAsStateWithLifecycle()
    val feedbackSubmitted by viewModel.feedbackSubmitted.collectAsStateWithLifecycle()

    val userEmail by viewModel.userEmail.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.initFirestore(context)
    }

    val statusMessage by viewModel.statusBannerMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusBanner()
        }
    }

    // If no user is logged in, show Demo Login Screen
    if (currentUser == null) {
        LoginScreen(
            onLogin = { u, p -> viewModel.login(u, p) },
            onQuickDemoLogin = { role -> viewModel.quickDemoLogin(role) }
        )
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopRestaurantBar(
                currentMode = currentMode,
                onModeChange = { viewModel.setMode(it) },
                activeOrdersCount = activeOrdersCount,
                isWaitlistActive = isWaitlistActive,
                isCloudSyncConnected = isCloudSyncConnected,
                currentUser = currentUser,
                onLogout = { viewModel.logout() },
                onOpenSyncSettings = { viewModel.openSyncSettings() },
                onOpenFeedback = { viewModel.openFeedback() }
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .testTag("restaurant_main_screen")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentMode) {
                AppMode.CUSTOMER -> {
                    CustomerMenuScreen(
                        menuItems = filteredMenuItems,
                        selectedCategory = selectedCategory,
                        onCategorySelect = { viewModel.selectCategory(it) },
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        vegOnly = vegOnly,
                        onToggleVeg = { viewModel.toggleVegFilter() },
                        nonVegOnly = nonVegOnly,
                        onToggleNonVeg = { viewModel.toggleNonVegFilter() },
                        cartItems = cartItems,
                        isWaitlistActive = isWaitlistActive,
                        activeOrdersCount = activeOrdersCount,
                        customerOrders = orders,
                        unavailableItemIds = unavailableItemIds,
                        onOpenCustomiser = { viewModel.openCustomisation(it) },
                        onOpenCart = { viewModel.openCart() },
                        onViewOrderBill = { viewModel.viewBill(it) }
                    )
                }

                AppMode.CHEF -> {
                    ChefDashboardScreen(
                        orders = orders,
                        activeOrdersCount = activeOrdersCount,
                        isWaitlistActive = isWaitlistActive,
                        statusFilter = chefStatusFilter,
                        onStatusFilterChange = { viewModel.setChefStatusFilter(it) },
                        searchQuery = chefSearchQuery,
                        onSearchChange = { viewModel.setChefSearchQuery(it) },
                        onAcceptOrder = { viewModel.chefAcceptOrder(it) },
                        onMarkReady = { viewModel.chefMarkReady(it) },
                        onMarkPaidAtCounter = { viewModel.chefMarkPaidAtCounter(it) },
                        onViewBill = { viewModel.viewBill(it) },
                        onTriggerAutoPurge = { viewModel.triggerAutoPurgeNow() },
                        onFillWaitlistForTesting = { viewModel.fillWaitlistForTesting() },
                        onResetWaitlistOrders = { viewModel.resetWaitlistOrders() }
                    )
                }

                AppMode.ADMIN -> {
                    AdminDashboardScreen(
                        orders = orders,
                        activeOrdersCount = activeOrdersCount,
                        isWaitlistActive = isWaitlistActive,
                        menuItems = viewModel.allMenuItems,
                        unavailableItemIds = unavailableItemIds,
                        onToggleItemAvailability = { viewModel.toggleItemAvailability(it) },
                        onFillWaitlistForTesting = { viewModel.fillWaitlistForTesting() },
                        onResetWaitlistOrders = { viewModel.resetWaitlistOrders() },
                        onTriggerAutoPurge = { viewModel.triggerAutoPurgeNow() },
                        onSwitchToStudentView = { viewModel.setMode(AppMode.CUSTOMER) },
                        onSwitchToCookView = { viewModel.setMode(AppMode.CHEF) }
                    )
                }
            }
        }

        // Customisation Sheet
        DishCustomiserDialog(
            item = selectedCustomiseItem,
            quantity = customiseQuantity,
            onQuantityChange = { viewModel.setCustomiseQuantity(it) },
            excludedIngredients = excludedIngredients,
            onToggleIngredient = { viewModel.toggleIngredientExclusion(it) },
            chefNote = chefNote,
            onChefNoteChange = { viewModel.setChefNote(it) },
            spicePreference = spicePreference,
            onSpicePreferenceChange = { viewModel.setSpicePreference(it) },
            onAddToCart = { viewModel.confirmAddToCart() },
            onDismiss = { viewModel.dismissCustomisation() }
        )

        // Cart & Checkout Sheet (with Capacity Guard)
        CartBottomSheet(
            isOpen = isCartOpen,
            cartItems = cartItems,
            customerName = customerName,
            onCustomerNameChange = { viewModel.setCustomerName(it) },
            tableNumber = tableNumber,
            onTableNumberChange = { viewModel.setTableNumber(it) },
            customerPhone = customerPhone,
            onCustomerPhoneChange = { viewModel.setCustomerPhone(it) },
            isWaitlistActive = isWaitlistActive,
            activeOrdersCount = activeOrdersCount,
            onQuantityChange = { id, qty -> viewModel.updateCartItemQuantity(id, qty) },
            onRemoveItem = { viewModel.removeCartItem(it) },
            onCheckout = { viewModel.checkoutAndGenerateBill() },
            onDismiss = { viewModel.dismissCart() }
        )

        // Store Counter Bill Dialog
        DigitalBillDialog(
            order = activeBill,
            onDismiss = { viewModel.dismissBill() },
            onTrackOrder = {
                viewModel.dismissBill()
                viewModel.setMode(AppMode.CUSTOMER)
            }
        )

        // Online Sync & Data Sharing Settings Dialog
        OnlineSyncDialog(
            isOpen = isSyncSettingsOpen,
            syncChannel = syncChannel,
            isCloudSyncConnected = isCloudSyncConnected,
            userEmail = userEmail,
            lastPruneTimestamp = lastPruneTimestamp,
            prunedCount = prunedCount,
            onSaveChannel = { viewModel.setSyncChannel(it) },
            onToggleSync = { viewModel.toggleCloudSync() },
            onGoogleSignIn = { viewModel.signInWithGoogle(context) },
            onTriggerPurgeNow = { viewModel.triggerAutoPurgeNow() },
            onDismiss = { viewModel.dismissSyncSettings() }
        )

        // Customer Feedback Dialog
        CustomerFeedbackDialog(
            isOpen = isFeedbackOpen,
            rating = feedbackRating,
            onRatingChange = { viewModel.setFeedbackRating(it) },
            comment = feedbackComment,
            onCommentChange = { viewModel.setFeedbackComment(it) },
            isSubmitted = feedbackSubmitted,
            onSubmit = { viewModel.submitFeedback() },
            onDismiss = { viewModel.dismissFeedback() }
        )
    }
}

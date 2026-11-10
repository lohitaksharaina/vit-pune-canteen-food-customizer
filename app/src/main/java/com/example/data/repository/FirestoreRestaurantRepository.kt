package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.MenuCatalog
import com.example.data.model.CustomisedOrderItem
import com.example.data.model.MenuCategory
import com.example.data.model.MenuItem
import com.example.data.model.OrderStatus
import com.example.data.model.RestaurantOrder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreRestaurantRepository(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val TAG = "FirestoreRepo"
        const val COLLECTION_ORDERS = "orders"
        const val COLLECTION_MENU = "menu_items"

        @Volatile
        private var instance: FirestoreRestaurantRepository? = null

        fun getInstance(context: Context): FirestoreRestaurantRepository {
            return instance ?: synchronized(this) {
                instance ?: run {
                    val dbId = try {
                        context.getString(R.string.firestore_database_id)
                    } catch (e: Exception) {
                        Log.w(TAG, "Missing firestore_database_id in config", e)
                        ""
                    }
                    val db = if (dbId.isNotBlank()) {
                        FirebaseFirestore.getInstance(dbId)
                    } else {
                        FirebaseFirestore.getInstance()
                    }
                    FirestoreRestaurantRepository(db).also { instance = it }
                }
            }
        }
    }

    /**
     * Real-time Flow of orders synchronized via Firestore.
     */
    fun observeOrdersRealtime(channelCode: String): Flow<List<RestaurantOrder>> = callbackFlow {
        val query = firestore.collection(COLLECTION_ORDERS)
            .whereEqualTo("channelCode", channelCode)

        val registration: ListenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Listen failed for orders query", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val ordersList = snapshot.documents.mapNotNull { doc ->
                    try {
                        val orderId = doc.getString("orderId") ?: doc.id
                        val orderNum = doc.getLong("orderNumber")?.toInt() ?: 100
                        val customerName = doc.getString("customerName") ?: "Guest"
                        val tableNumber = doc.getString("tableNumber") ?: "Table"
                        val customerPhone = doc.getString("customerPhone") ?: ""
                        val subtotal = doc.getDouble("subtotal") ?: 0.0
                        val gst = doc.getDouble("gst") ?: 0.0
                        val totalAmount = doc.getDouble("totalAmount") ?: 0.0
                        val statusStr = doc.getString("status") ?: OrderStatus.RECEIVED.name
                        val status = try {
                            OrderStatus.valueOf(statusStr)
                        } catch (e: Exception) {
                            OrderStatus.RECEIVED
                        }
                        val isPaidAtCounter = doc.getBoolean("isPaidAtCounter") ?: false
                        val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: System.currentTimeMillis()
                        val completedAt = doc.getTimestamp("completedAt")?.toDate()?.time

                        // Parse items array
                        @Suppress("UNCHECKED_CAST")
                        val itemsRaw = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                        val items = itemsRaw.map { itemMap ->
                            CustomisedOrderItem(
                                id = itemMap["id"] as? String ?: java.util.UUID.randomUUID().toString(),
                                menuItemId = itemMap["menuItemId"] as? String ?: "",
                                name = itemMap["name"] as? String ?: "",
                                unitPrice = (itemMap["unitPrice"] as? Number)?.toDouble() ?: 0.0,
                                quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 1,
                                isVeg = itemMap["isVeg"] as? Boolean ?: true,
                                dislikedIngredients = (itemMap["dislikedIngredients"] as? List<String>) ?: emptyList(),
                                chefNote = itemMap["chefNote"] as? String ?: "",
                                spicePreference = itemMap["spicePreference"] as? String ?: "Normal"
                            )
                        }

                        RestaurantOrder(
                            orderId = orderId,
                            orderNumber = orderNum,
                            customerName = customerName,
                            tableNumber = tableNumber,
                            customerPhone = customerPhone,
                            items = items,
                            subtotal = subtotal,
                            gst = gst,
                            totalAmount = totalAmount,
                            status = status,
                            isPaidAtCounter = isPaidAtCounter,
                            createdAt = createdAt,
                            completedAt = completedAt
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deserializing order ${doc.id}", e)
                        null
                    }
                }.sortedByDescending { it.createdAt }

                trySend(ordersList)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Saves a new order to Firestore in real-time.
     */
    suspend fun saveOrder(order: RestaurantOrder, channelCode: String) {
        val uid = Firebase.auth.currentUser?.uid ?: "guest_${System.currentTimeMillis()}"
        val docRef = firestore.collection(COLLECTION_ORDERS).document(order.orderId)

        val itemsPayload = order.items.map { item ->
            mapOf(
                "id" to item.id,
                "menuItemId" to item.menuItemId,
                "name" to item.name,
                "unitPrice" to item.unitPrice,
                "quantity" to item.quantity,
                "isVeg" to item.isVeg,
                "dislikedIngredients" to item.dislikedIngredients,
                "chefNote" to item.chefNote,
                "spicePreference" to item.spicePreference
            )
        }

        val payload = hashMapOf<String, Any>(
            "orderId" to order.orderId,
            "orderNumber" to order.orderNumber,
            "customerName" to order.customerName,
            "tableNumber" to order.tableNumber,
            "customerPhone" to order.customerPhone,
            "subtotal" to order.subtotal,
            "gst" to order.gst,
            "totalAmount" to order.totalAmount,
            "status" to order.status.name,
            "isPaidAtCounter" to order.isPaidAtCounter,
            "channelCode" to channelCode,
            "userId" to uid,
            "items" to itemsPayload,
            "createdAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            Log.d(TAG, "Order ${order.orderId} written to Firestore successfully")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write order ${order.orderId} to Firestore", e)
        }
    }

    /**
     * Updates order status in Firestore (e.g. ACCEPTED_PREPARING, READY_FOR_COUNTER, COMPLETED_SETTLED).
     */
    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
        isPaidAtCounter: Boolean = false
    ) {
        val docRef = firestore.collection(COLLECTION_ORDERS).document(orderId)
        val updates = mutableMapOf<String, Any>(
            "status" to newStatus.name
        )
        if (isPaidAtCounter) {
            updates["isPaidAtCounter"] = true
            updates["completedAt"] = FieldValue.serverTimestamp()
        }

        try {
            docRef.update(updates).await()
            Log.d(TAG, "Order $orderId status updated to ${newStatus.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update order status $orderId", e)
        }
    }

    /**
     * Seeds Vishwakarma Institute of Technology (VIT) Pune Canteen menu items into Firestore.
     */
    suspend fun seedMenuCatalogIfEmpty() {
        try {
            val batch = firestore.batch()
            MenuCatalog.items.forEach { item ->
                val docRef = firestore.collection(COLLECTION_MENU).document(item.id)
                val data = hashMapOf(
                    "id" to item.id,
                    "name" to item.name,
                    "category" to item.category.name,
                    "description" to item.description,
                    "price" to item.price,
                    "isVeg" to item.isVeg,
                    "spiciness" to item.spiciness,
                    "badge" to (item.badge ?: ""),
                    "dislikableIngredients" to item.dislikableIngredients
                )
                batch.set(docRef, data, SetOptions.merge())
            }
            batch.commit().await()
            Log.d(TAG, "Synced VIT Pune Canteen menu into Firestore")
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing VIT menu to Firestore", e)
        }
    }

    /**
     * Automatic 1-Week (7-day) Data Retention Cleanup:
     * Queries settled orders older than 7 days and deletes them from Firestore.
     */
    suspend fun purgeOldSettledOrders(): Int {
        val sevenDaysMillis = 7L * 24 * 60 * 60 * 1000L
        val cutoffDate = java.util.Date(System.currentTimeMillis() - sevenDaysMillis)

        return try {
            val querySnapshot = firestore.collection(COLLECTION_ORDERS)
                .whereEqualTo("status", OrderStatus.COMPLETED_SETTLED.name)
                .whereEqualTo("isPaidAtCounter", true)
                .whereLessThan("completedAt", cutoffDate)
                .get()
                .await()

            var deleted = 0
            val batch = firestore.batch()
            querySnapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
                deleted++
            }
            if (deleted > 0) {
                batch.commit().await()
                Log.d(TAG, "Purged $deleted settled orders older than 7 days from Firestore")
            }
            deleted
        } catch (e: Exception) {
            Log.w(TAG, "Error during 7-day auto-purge in Firestore", e)
            0
        }
    }
}

package com.example.util

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.RestaurantOrder

object CanteenNotificationHelper {

    private const val TAG = "CanteenNotif"
    const val CHANNEL_ID = "vit_canteen_orders_v2"
    private const val CHANNEL_NAME = "VIT Canteen Live Orders"
    private const val CHANNEL_DESC = "Status bar notifications for students, cooks and admin"

    const val EXTRA_TARGET_APP_MODE = "TARGET_APP_MODE"
    const val EXTRA_TARGET_ORDER_ID = "TARGET_ORDER_ID"

    /**
     * Initializes the Android notification channel with high priority, sound and vibration.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
                setShowBadge(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Shows a system status bar notification for kitchen staff on new orders.
     * Clicking opens CHEF mode.
     */
    fun showNewOrderNotification(context: Context, order: RestaurantOrder, targetMode: String = "CHEF") {
        createNotificationChannel(context)
        if (!hasNotificationPermission(context)) {
            Log.d(TAG, "Notification permission not granted, skipping showNewOrderNotification")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_APP_MODE, targetMode)
            putExtra(EXTRA_TARGET_ORDER_ID, order.orderId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            order.orderNumber,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val itemsSummary = order.items.joinToString(", ") { "${it.quantity}x ${it.name}" }
        val contentText = "$itemsSummary • ₹${order.totalAmount.toInt()}"
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("🔔 New Kitchen Order #${order.orderNumber} (${order.tableNumber})")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("Customer: ${order.customerName}\nItems: $itemsSummary\nTotal: ₹${order.totalAmount.toInt()}"))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 250, 150, 250))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(order.orderNumber, builder.build())
            Log.d(TAG, "Displayed notification on phone status bar for Order #${order.orderNumber}")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying notification", e)
        }
    }

    /**
     * Shows a system status bar notification for Admin when a customized order needs price review.
     * Clicking opens ADMIN mode.
     */
    fun showAdminCustomOrderNotification(context: Context, order: RestaurantOrder) {
        createNotificationChannel(context)
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_APP_MODE, "ADMIN")
            putExtra(EXTRA_TARGET_ORDER_ID, order.orderId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            order.orderNumber + 30000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("📝 Customization Review: Order #${order.orderNumber}")
            .setContentText("Customer ${order.customerName} requested food customizations. Tap to review & set price.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Customer: ${order.customerName} (${order.tableNumber})\nHas customized ingredients/notes requiring admin review.\nTap to open Admin Mode."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(order.orderNumber + 30000, builder.build())
            Log.d(TAG, "Displayed admin review notification for Order #${order.orderNumber}")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying admin notification", e)
        }
    }

    /**
     * Shows a status bar notification to student when order is placed, accepted, or ready for pickup.
     * Clicking opens CUSTOMER (Student) mode.
     */
    fun showStudentOrderStatusNotification(
        context: Context,
        order: RestaurantOrder,
        title: String,
        message: String
    ) {
        createNotificationChannel(context)
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_APP_MODE, "CUSTOMER")
            putExtra(EXTRA_TARGET_ORDER_ID, order.orderId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            order.orderNumber + 10000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 200, 100, 200))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(order.orderNumber + 10000, builder.build())
            Log.d(TAG, "Displayed student notification on phone status bar for Order #${order.orderNumber}")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying student notification", e)
        }
    }

    /**
     * Shows a status bar notification when Admin/Chef proposes a price adjustment.
     * Clicking opens CUSTOMER (Student) mode.
     */
    fun showCustomPriceNotification(
        context: Context,
        order: RestaurantOrder,
        newPrice: Double
    ) {
        createNotificationChannel(context)
        if (!hasNotificationPermission(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_APP_MODE, "CUSTOMER")
            putExtra(EXTRA_TARGET_ORDER_ID, order.orderId)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            order.orderNumber + 20000,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("🏷️ Price Adjusted for Order #${order.orderNumber}")
            .setContentText("Reviewer proposed ₹${newPrice.toInt()} for your customized food. Tap to accept or decline.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Admin/Chef proposed ₹${newPrice.toInt()} for your customized items on Order #${order.orderNumber}. Open the app to Accept or Decline.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(order.orderNumber + 20000, builder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying custom price notification", e)
        }
    }
}

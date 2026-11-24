package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
    const val CHANNEL_ID = "vit_canteen_orders"
    private const val CHANNEL_NAME = "VIT Canteen Orders"
    private const val CHANNEL_DESC = "Notifications for kitchen staff and admin on new orders"

    /**
     * Initializes the Android notification channel.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a system notification for a new incoming order with a 1-line description of items and order number.
     */
    fun showNewOrderNotification(context: Context, order: RestaurantOrder) {
        createNotificationChannel(context)

        // Check notification permission for Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "POST_NOTIFICATIONS permission not granted, skipping notification")
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            order.orderNumber,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // One line description of what was ordered + order number
        val itemsSummary = order.items.joinToString(", ") { "${it.quantity}x ${it.name}" }
        val contentText = "$itemsSummary • ₹${order.totalAmount.toInt()}"

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("New Order #${order.orderNumber} (${order.tableNumber})")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(order.orderNumber, builder.build())
            Log.d(TAG, "Displayed notification for Order #${order.orderNumber}")
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException sending notification", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying notification", e)
        }
    }
}

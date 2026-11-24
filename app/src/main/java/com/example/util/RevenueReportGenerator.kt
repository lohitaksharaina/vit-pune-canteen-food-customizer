package com.example.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.data.model.OrderStatus
import com.example.data.model.RestaurantOrder
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DayRevenueItem(
    val dateStr: String,
    val ordersCount: Int,
    val revenue: Double
)

data class ItemSaleStat(
    val itemId: String,
    val itemName: String,
    val quantitySold: Int,
    val totalRevenue: Double
)

data class MonthlyRevenueReport(
    val generationIndex: Int,
    val fileName: String,
    val monthYearStr: String,
    val generatedTimestamp: Long,
    val totalGrossRevenue: Double,
    val totalGst: Double,
    val totalSettledOrders: Int,
    val dayWiseRevenue: List<DayRevenueItem>,
    val itemSales: List<ItemSaleStat>
) {
    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "generationIndex" to generationIndex,
            "fileName" to fileName,
            "monthYearStr" to monthYearStr,
            "generatedTimestamp" to generatedTimestamp,
            "totalGrossRevenue" to totalGrossRevenue,
            "totalGst" to totalGst,
            "totalSettledOrders" to totalSettledOrders,
            "dayWiseRevenue" to dayWiseRevenue.map {
                mapOf(
                    "date" to it.dateStr,
                    "ordersCount" to it.ordersCount,
                    "revenue" to it.revenue
                )
            },
            "itemSales" to itemSales.map {
                mapOf(
                    "itemId" to it.itemId,
                    "itemName" to it.itemName,
                    "quantitySold" to it.quantitySold,
                    "totalRevenue" to it.totalRevenue
                )
            }
        )
    }
}

object RevenueReportGenerator {

    private const val TAG = "RevenueReportGen"
    private const val PREFS_NAME = "vit_canteen_report_prefs"
    private const val KEY_GENERATION_COUNTER = "pdf_generation_counter"

    /**
     * Gets and automatically increments the generation index 'i', starting with 1.
     */
    @Synchronized
    fun getNextGenerationIndex(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_GENERATION_COUNTER, 0)
        val next = current + 1
        prefs.edit().putInt(KEY_GENERATION_COUNTER, next).apply()
        return next
    }

    /**
     * Current generation counter without incrementing.
     */
    fun getCurrentGenerationIndex(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_GENERATION_COUNTER, 0)
    }

    /**
     * Calculates monthly day-wise revenue and per-item sales from order list.
     */
    fun calculateMonthlyReport(context: Context, orders: List<RestaurantOrder>): MonthlyRevenueReport {
        val i = getNextGenerationIndex(context)
        val fileName = "VitCanteen_revenue($i)"
        val now = System.currentTimeMillis()
        val monthYearStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(now))
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val settledOrders = orders.filter { it.isPaidAtCounter || it.status == OrderStatus.COMPLETED_SETTLED }
        // If no settled orders yet, also take all placed orders for comprehensive review
        val targetOrders = if (settledOrders.isNotEmpty()) settledOrders else orders

        val totalGrossRevenue = targetOrders.sumOf { it.totalAmount }
        val totalGst = targetOrders.sumOf { it.gst }
        val totalSettledOrders = targetOrders.size

        // 1. Day-wise Revenue grouping
        val dayGroups = targetOrders.groupBy { order ->
            dayFormat.format(Date(order.completedAt ?: order.createdAt))
        }
        val dayWiseRevenue = dayGroups.map { (dateStr, dayOrders) ->
            DayRevenueItem(
                dateStr = dateStr,
                ordersCount = dayOrders.size,
                revenue = dayOrders.sumOf { it.totalAmount }
            )
        }.sortedByDescending { it.dateStr }

        // 2. Per-Item Sales aggregation
        val itemSalesMap = mutableMapOf<String, ItemSaleStat>()
        targetOrders.forEach { order ->
            order.items.forEach { item ->
                val existing = itemSalesMap[item.menuItemId]
                if (existing != null) {
                    itemSalesMap[item.menuItemId] = existing.copy(
                        quantitySold = existing.quantitySold + item.quantity,
                        totalRevenue = existing.totalRevenue + item.totalPrice
                    )
                } else {
                    itemSalesMap[item.menuItemId] = ItemSaleStat(
                        itemId = item.menuItemId,
                        itemName = item.name,
                        quantitySold = item.quantity,
                        totalRevenue = item.totalPrice
                    )
                }
            }
        }
        val itemSales = itemSalesMap.values.sortedByDescending { it.quantitySold }

        return MonthlyRevenueReport(
            generationIndex = i,
            fileName = fileName,
            monthYearStr = monthYearStr,
            generatedTimestamp = now,
            totalGrossRevenue = totalGrossRevenue,
            totalGst = totalGst,
            totalSettledOrders = totalSettledOrders,
            dayWiseRevenue = dayWiseRevenue,
            itemSales = itemSales
        )
    }

    /**
     * Creates an Android PdfDocument with styled tables for day-wise revenue and per-item sales.
     */
    fun generatePdfDocument(report: MonthlyRevenueReport): PdfDocument {
        val document = PdfDocument()
        val pageWidth = 595 // Standard A4 width at 72dpi
        val pageHeight = 842 // Standard A4 height at 72dpi
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Header Background Banner
        paint.color = Color.parseColor("#1B1412")
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 105f, paint)

        // Accent top border
        paint.color = Color.parseColor("#E65100") // Flame Orange
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 6f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("VISHWAKARMA INSTITUTE OF TECHNOLOGY, PUNE", 30f, 36f, paint)

        paint.textSize = 13f
        paint.color = Color.parseColor("#FFCC80")
        paint.isFakeBoldText = false
        canvas.drawText("VIT Pune Canteen • Monthly Day-wise Revenue & Per-Item Sales", 30f, 58f, paint)

        paint.textSize = 10f
        paint.color = Color.parseColor("#E0E0E0")
        val dateGenerated = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(report.generatedTimestamp))
        canvas.drawText("Document: ${report.fileName}.pdf  |  Report Period: ${report.monthYearStr}  |  Generated: $dateGenerated", 30f, 82f, paint)

        var y = 130f

        // Summary Boxes
        paint.color = Color.parseColor("#F5F5F5")
        canvas.drawRoundRect(30f, y, 190f, y + 55f, 8f, 8f, paint)
        canvas.drawRoundRect(210f, y, 370f, y + 55f, 8f, 8f, paint)
        canvas.drawRoundRect(390f, y, 565f, y + 55f, 8f, 8f, paint)

        paint.color = Color.DKGRAY
        paint.textSize = 9f
        paint.isFakeBoldText = false
        canvas.drawText("GROSS REVENUE", 42f, y + 20f, paint)
        canvas.drawText("TOTAL ORDERS", 222f, y + 20f, paint)
        canvas.drawText("ESTIMATED GST (5%)", 402f, y + 20f, paint)

        paint.color = Color.parseColor("#1B5E20") // Green
        paint.textSize = 17f
        paint.isFakeBoldText = true
        canvas.drawText("₹${String.format(Locale.getDefault(), "%.2f", report.totalGrossRevenue)}", 42f, y + 44f, paint)

        paint.color = Color.parseColor("#0D47A1") // Blue
        canvas.drawText("${report.totalSettledOrders}", 222f, y + 44f, paint)

        paint.color = Color.parseColor("#E65100") // Orange
        canvas.drawText("₹${String.format(Locale.getDefault(), "%.2f", report.totalGst)}", 402f, y + 44f, paint)

        y += 80f

        // SECTION 1: Day-Wise Revenue Table
        paint.color = Color.parseColor("#212121")
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("1. Day-Wise Revenue Breakdown", 30f, y, paint)

        y += 14f

        // Table Header
        paint.color = Color.parseColor("#EEEEEE")
        canvas.drawRect(30f, y, 565f, y + 22f, paint)
        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("Date", 42f, y + 15f, paint)
        canvas.drawText("Orders Count", 240f, y + 15f, paint)
        canvas.drawText("Revenue Collected (₹)", 410f, y + 15f, paint)

        y += 22f
        paint.isFakeBoldText = false
        val maxDaysToShow = minOf(report.dayWiseRevenue.size, 8)
        for (idx in 0 until maxDaysToShow) {
            val item = report.dayWiseRevenue[idx]
            paint.color = if (idx % 2 == 0) Color.WHITE else Color.parseColor("#FAFAFA")
            canvas.drawRect(30f, y, 565f, y + 18f, paint)

            paint.color = Color.parseColor("#333333")
            paint.textSize = 9f
            canvas.drawText(item.dateStr, 42f, y + 13f, paint)
            canvas.drawText("${item.ordersCount} orders", 240f, y + 13f, paint)
            paint.isFakeBoldText = true
            canvas.drawText("₹${String.format(Locale.getDefault(), "%.2f", item.revenue)}", 410f, y + 13f, paint)
            paint.isFakeBoldText = false
            y += 18f
        }

        y += 24f

        // SECTION 2: Per-Item Sales Table
        paint.color = Color.parseColor("#212121")
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("2. Per-Item Sales Summary", 30f, y, paint)

        y += 14f

        // Table Header
        paint.color = Color.parseColor("#EEEEEE")
        canvas.drawRect(30f, y, 565f, y + 22f, paint)
        paint.color = Color.BLACK
        paint.textSize = 9.5f
        paint.isFakeBoldText = true
        canvas.drawText("Item Name", 42f, y + 15f, paint)
        canvas.drawText("Units Sold", 280f, y + 15f, paint)
        canvas.drawText("Total Sales (₹)", 410f, y + 15f, paint)

        y += 22f
        paint.isFakeBoldText = false
        val maxItemsToShow = minOf(report.itemSales.size, 14)
        for (idx in 0 until maxItemsToShow) {
            val item = report.itemSales[idx]
            paint.color = if (idx % 2 == 0) Color.WHITE else Color.parseColor("#FAFAFA")
            canvas.drawRect(30f, y, 565f, y + 18f, paint)

            paint.color = Color.parseColor("#333333")
            paint.textSize = 9f
            canvas.drawText(item.itemName, 42f, y + 13f, paint)
            canvas.drawText("${item.quantitySold} pcs", 280f, y + 13f, paint)
            paint.isFakeBoldText = true
            canvas.drawText("₹${String.format(Locale.getDefault(), "%.2f", item.totalRevenue)}", 410f, y + 13f, paint)
            paint.isFakeBoldText = false
            y += 18f
        }

        // Footer
        paint.color = Color.LTGRAY
        canvas.drawLine(30f, pageHeight - 35f, 565f, pageHeight - 35f, paint)
        paint.color = Color.GRAY
        paint.textSize = 8.5f
        canvas.drawText("VIT Pune Canteen Order & Customizer Management System • Generated with Antigravity", 30f, pageHeight - 20f, paint)
        canvas.drawText("Page 1 of 1", 510f, pageHeight - 20f, paint)

        document.finishPage(page)
        return document
    }

    /**
     * Saves the PDF to internal/external Downloads storage.
     * Uses MediaStore on Android 10+ (API 29+); falls back to Downloads file on earlier versions.
     */
    fun savePdfToStorage(context: Context, document: PdfDocument, fileName: String): Result<String> {
        return try {
            val fullFileName = if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fullFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/VitCanteen")
                }
                val resolver = context.contentResolver
                val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        document.writeTo(outputStream)
                    }
                    document.close()
                    Result.success("Saved to Downloads/VitCanteen/$fullFileName")
                } else {
                    // Fallback to app files dir
                    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fullFileName)
                    FileOutputStream(file).use { out -> document.writeTo(out) }
                    document.close()
                    Result.success("Saved to ${file.absolutePath}")
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val file = File(downloadsDir, fullFileName)
                FileOutputStream(file).use { out -> document.writeTo(out) }
                document.close()
                Result.success("Saved to ${file.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving PDF $fileName", e)
            try { document.close() } catch (_: Exception) {}
            Result.failure(e)
        }
    }
}

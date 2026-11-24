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
    val revenue: Double,
    val settledRevenue: Double = revenue
)

data class ItemSaleStat(
    val itemId: String,
    val itemName: String,
    val quantitySold: Int,
    val totalRevenue: Double,
    val percentageOfTotal: Double = 0.0
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
    val itemSales: List<ItemSaleStat>,
    val totalPendingRevenue: Double = 0.0,
    val totalOrdersCount: Int = totalSettledOrders,
    val averageOrderValue: Double = if (totalOrdersCount > 0) totalGrossRevenue / totalOrdersCount else 0.0
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
            "totalPendingRevenue" to totalPendingRevenue,
            "totalOrdersCount" to totalOrdersCount,
            "averageOrderValue" to averageOrderValue,
            "dayWiseRevenue" to dayWiseRevenue.map {
                mapOf(
                    "date" to it.dateStr,
                    "ordersCount" to it.ordersCount,
                    "revenue" to it.revenue,
                    "settledRevenue" to it.settledRevenue
                )
            },
            "itemSales" to itemSales.map {
                mapOf(
                    "itemId" to it.itemId,
                    "itemName" to it.itemName,
                    "quantitySold" to it.quantitySold,
                    "totalRevenue" to it.totalRevenue,
                    "percentageOfTotal" to it.percentageOfTotal
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
     * Accurately calculates monthly revenue, day-wise metrics and per-item sales.
     */
    fun calculateMonthlyReport(context: Context, orders: List<RestaurantOrder>): MonthlyRevenueReport {
        val i = getNextGenerationIndex(context)
        val fileName = "VitCanteen_revenue($i)"
        val now = System.currentTimeMillis()
        val monthYearStr = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(now))
        val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val settledOrders = orders.filter { it.isPaidAtCounter || it.status == OrderStatus.COMPLETED_SETTLED }
        val pendingOrders = orders.filter { !it.isPaidAtCounter && it.status != OrderStatus.COMPLETED_SETTLED }

        val totalSettledRevenue = settledOrders.sumOf { it.totalAmount }
        val totalPendingRevenue = pendingOrders.sumOf { it.totalAmount }
        // Total gross revenue encompasses all valid canteen turnover
        val totalGrossRevenue = if (settledOrders.isNotEmpty()) totalSettledRevenue else orders.sumOf { it.totalAmount }
        val totalGst = totalGrossRevenue * 0.05
        val totalSettledOrders = settledOrders.size
        val totalOrdersCount = orders.size
        val averageOrderValue = if (totalOrdersCount > 0) totalGrossRevenue / totalOrdersCount else 0.0

        // Use all available orders for comprehensive historical day-wise aggregation
        val ordersToAnalyze = if (settledOrders.isNotEmpty()) settledOrders else orders

        // 1. Accurate Day-wise Revenue grouping
        val dayGroups = ordersToAnalyze.groupBy { order ->
            dayFormat.format(Date(order.completedAt ?: order.createdAt))
        }
        val dayWiseRevenue = dayGroups.map { (dateStr, dayOrders) ->
            val daySettled = dayOrders.filter { it.isPaidAtCounter || it.status == OrderStatus.COMPLETED_SETTLED }
            DayRevenueItem(
                dateStr = dateStr,
                ordersCount = dayOrders.size,
                revenue = dayOrders.sumOf { it.totalAmount },
                settledRevenue = if (daySettled.isNotEmpty()) daySettled.sumOf { it.totalAmount } else dayOrders.sumOf { it.totalAmount }
            )
        }.sortedByDescending { it.dateStr }

        // 2. Accurate Per-Item Sales aggregation
        val itemSalesMap = mutableMapOf<String, ItemSaleStat>()
        ordersToAnalyze.forEach { order ->
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

        val totalItemRevenueSum = itemSalesMap.values.sumOf { it.totalRevenue }.coerceAtLeast(1.0)
        val itemSales = itemSalesMap.values.map { stat ->
            stat.copy(percentageOfTotal = (stat.totalRevenue / totalItemRevenueSum) * 100.0)
        }.sortedByDescending { it.quantitySold }

        return MonthlyRevenueReport(
            generationIndex = i,
            fileName = fileName,
            monthYearStr = monthYearStr,
            generatedTimestamp = now,
            totalGrossRevenue = totalGrossRevenue,
            totalGst = totalGst,
            totalSettledOrders = totalSettledOrders,
            dayWiseRevenue = dayWiseRevenue,
            itemSales = itemSales,
            totalPendingRevenue = totalPendingRevenue,
            totalOrdersCount = totalOrdersCount,
            averageOrderValue = averageOrderValue
        )
    }

    /**
     * Creates an Android PdfDocument with styled multi-page tables for day-wise revenue and per-item sales.
     */
    fun generatePdfDocument(report: MonthlyRevenueReport): PdfDocument {
        val document = PdfDocument()
        val pageWidth = 595 // Standard A4 width at 72dpi
        val pageHeight = 842 // Standard A4 height at 72dpi
        val paint = Paint()

        var currentPageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas

        // Header Background
        paint.color = Color.parseColor("#141211")
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 105f, paint)

        // Accent top border
        paint.color = Color.parseColor("#E65100")
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 6f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 17f
        paint.isFakeBoldText = true
        canvas.drawText("VISHWAKARMA INSTITUTE OF TECHNOLOGY, PUNE", 30f, 36f, paint)

        paint.textSize = 12f
        paint.color = Color.parseColor("#FFCC80")
        paint.isFakeBoldText = false
        canvas.drawText("VIT Pune Canteen • Monthly Day-wise Revenue & Per-Item Sales", 30f, 58f, paint)

        paint.textSize = 9.5f
        paint.color = Color.parseColor("#E0E0E0")
        val dateGenerated = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(report.generatedTimestamp))
        canvas.drawText("Document: ${report.fileName}.pdf  |  Period: ${report.monthYearStr}  |  Generated: $dateGenerated", 30f, 82f, paint)

        var y = 125f

        // Summary Boxes
        val boxWidth = 125f
        val boxHeight = 55f

        // Box 1: Gross Revenue
        paint.color = Color.parseColor("#F5F5F5")
        canvas.drawRoundRect(30f, y, 30f + boxWidth, y + boxHeight, 8f, 8f, paint)
        paint.color = Color.DKGRAY
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        canvas.drawText("GROSS REVENUE", 40f, y + 18f, paint)
        paint.color = Color.parseColor("#1B5E20")
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("₹${String.format(Locale.getDefault(), "%.0f", report.totalGrossRevenue)}", 40f, y + 42f, paint)

        // Box 2: Settled Orders
        paint.color = Color.parseColor("#F5F5F5")
        canvas.drawRoundRect(165f, y, 165f + boxWidth, y + boxHeight, 8f, 8f, paint)
        paint.color = Color.DKGRAY
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        canvas.drawText("SETTLED ORDERS", 175f, y + 18f, paint)
        paint.color = Color.parseColor("#0D47A1")
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("${report.totalSettledOrders} / ${report.totalOrdersCount}", 175f, y + 42f, paint)

        // Box 3: GST (5%)
        paint.color = Color.parseColor("#F5F5F5")
        canvas.drawRoundRect(300f, y, 300f + boxWidth, y + boxHeight, 8f, 8f, paint)
        paint.color = Color.DKGRAY
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        canvas.drawText("ESTIMATED GST (5%)", 310f, y + 18f, paint)
        paint.color = Color.parseColor("#E65100")
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("₹${String.format(Locale.getDefault(), "%.1f", report.totalGst)}", 310f, y + 42f, paint)

        // Box 4: Average Order Value
        paint.color = Color.parseColor("#F5F5F5")
        canvas.drawRoundRect(435f, y, 565f, y + boxHeight, 8f, 8f, paint)
        paint.color = Color.DKGRAY
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        canvas.drawText("AVG ORDER VALUE", 445f, y + 18f, paint)
        paint.color = Color.parseColor("#4A148C")
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("₹${String.format(Locale.getDefault(), "%.0f", report.averageOrderValue)}", 445f, y + 42f, paint)

        y += 75f

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
        canvas.drawText("Orders Count", 230f, y + 15f, paint)
        canvas.drawText("Settled Revenue", 350f, y + 15f, paint)
        canvas.drawText("Total Revenue (₹)", 460f, y + 15f, paint)

        y += 22f
        paint.isFakeBoldText = false

        for (idx in report.dayWiseRevenue.indices) {
            // Check if page overflow
            if (y > pageHeight - 65f) {
                // Draw footer for current page
                drawFooter(canvas, paint, pageWidth, pageHeight, currentPageNumber)
                document.finishPage(page)

                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 40f

                // Re-draw Section 1 continued header
                paint.color = Color.parseColor("#212121")
                paint.textSize = 11f
                paint.isFakeBoldText = true
                canvas.drawText("1. Day-Wise Revenue Breakdown (Continued)", 30f, y, paint)
                y += 14f
                paint.color = Color.parseColor("#EEEEEE")
                canvas.drawRect(30f, y, 565f, y + 22f, paint)
                paint.color = Color.BLACK
                paint.textSize = 9.5f
                paint.isFakeBoldText = true
                canvas.drawText("Date", 42f, y + 15f, paint)
                canvas.drawText("Orders Count", 230f, y + 15f, paint)
                canvas.drawText("Settled Revenue", 350f, y + 15f, paint)
                canvas.drawText("Total Revenue (₹)", 460f, y + 15f, paint)
                y += 22f
                paint.isFakeBoldText = false
            }

            val item = report.dayWiseRevenue[idx]
            paint.color = if (idx % 2 == 0) Color.WHITE else Color.parseColor("#FAFAFA")
            canvas.drawRect(30f, y, 565f, y + 18f, paint)

            paint.color = Color.parseColor("#333333")
            paint.textSize = 9f
            canvas.drawText(item.dateStr, 42f, y + 13f, paint)
            canvas.drawText("${item.ordersCount} orders", 230f, y + 13f, paint)
            canvas.drawText("₹${String.format(Locale.getDefault(), "%.0f", item.settledRevenue)}", 350f, y + 13f, paint)
            paint.isFakeBoldText = true
            canvas.drawText("₹${String.format(Locale.getDefault(), "%.2f", item.revenue)}", 460f, y + 13f, paint)
            paint.isFakeBoldText = false
            y += 18f
        }

        y += 20f

        // SECTION 2: Per-Item Sales Summary Table
        if (y > pageHeight - 120f) {
            drawFooter(canvas, paint, pageWidth, pageHeight, currentPageNumber)
            document.finishPage(page)

            currentPageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            page = document.startPage(pageInfo)
            canvas = page.canvas
            y = 40f
        }

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
        canvas.drawText("Rank & Item Name", 42f, y + 15f, paint)
        canvas.drawText("Units Sold", 280f, y + 15f, paint)
        canvas.drawText("Total Sales (₹)", 400f, y + 15f, paint)
        canvas.drawText("Share %", 500f, y + 15f, paint)

        y += 22f
        paint.isFakeBoldText = false

        for (idx in report.itemSales.indices) {
            if (y > pageHeight - 65f) {
                drawFooter(canvas, paint, pageWidth, pageHeight, currentPageNumber)
                document.finishPage(page)

                currentPageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                y = 40f

                paint.color = Color.parseColor("#212121")
                paint.textSize = 11f
                paint.isFakeBoldText = true
                canvas.drawText("2. Per-Item Sales Summary (Continued)", 30f, y, paint)
                y += 14f
                paint.color = Color.parseColor("#EEEEEE")
                canvas.drawRect(30f, y, 565f, y + 22f, paint)
                paint.color = Color.BLACK
                paint.textSize = 9.5f
                paint.isFakeBoldText = true
                canvas.drawText("Rank & Item Name", 42f, y + 15f, paint)
                canvas.drawText("Units Sold", 280f, y + 15f, paint)
                canvas.drawText("Total Sales (₹)", 400f, y + 15f, paint)
                canvas.drawText("Share %", 500f, y + 15f, paint)
                y += 22f
                paint.isFakeBoldText = false
            }

            val item = report.itemSales[idx]
            paint.color = if (idx % 2 == 0) Color.WHITE else Color.parseColor("#FAFAFA")
            canvas.drawRect(30f, y, 565f, y + 18f, paint)

            paint.color = Color.parseColor("#333333")
            paint.textSize = 9f
            canvas.drawText("#${idx + 1}  ${item.itemName}", 42f, y + 13f, paint)
            canvas.drawText("${item.quantitySold} pcs", 280f, y + 13f, paint)
            paint.isFakeBoldText = true
            canvas.drawText("₹${String.format(Locale.getDefault(), "%.2f", item.totalRevenue)}", 400f, y + 13f, paint)
            paint.isFakeBoldText = false
            canvas.drawText("${String.format(Locale.getDefault(), "%.1f", item.percentageOfTotal)}%", 500f, y + 13f, paint)
            y += 18f
        }

        drawFooter(canvas, paint, pageWidth, pageHeight, currentPageNumber)
        document.finishPage(page)
        return document
    }

    private fun drawFooter(canvas: Canvas, paint: Paint, pageWidth: Int, pageHeight: Int, pageNum: Int) {
        paint.color = Color.LTGRAY
        canvas.drawLine(30f, pageHeight - 35f, 565f, pageHeight - 35f, paint)
        paint.color = Color.GRAY
        paint.textSize = 8.5f
        paint.isFakeBoldText = false
        canvas.drawText("VIT Pune Canteen Order & Customizer Management System", 30f, pageHeight - 20f, paint)
        canvas.drawText("Page $pageNum", 515f, pageHeight - 20f, paint)
    }

    /**
     * Saves the PDF to internal/external Downloads storage.
     * Uses MediaStore on Android 10+ (API 29+); falls back to Downloads file on earlier versions.
     */
    fun savePdfToStorage(context: Context, document: PdfDocument, fileName: String): Result<String> {
        return try {
            val fullFileName = if (fileName.endsWith(".pdf")) fileName else "$fileName.pdf"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fullFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/VITCanteen")
                }

                val resolver = context.contentResolver
                val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                if (uri != null) {
                    val outputStream: OutputStream? = resolver.openOutputStream(uri)
                    outputStream?.use { out ->
                        document.writeTo(out)
                    }
                    Log.d(TAG, "Saved PDF to MediaStore: $uri")
                    Result.success("Saved to Downloads/VITCanteen/$fullFileName")
                } else {
                    Result.failure(Exception("Failed to create MediaStore entry for $fullFileName"))
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val vitDir = File(downloadsDir, "VITCanteen").apply { if (!exists()) mkdirs() }
                val targetFile = File(vitDir, fullFileName)
                val outputStream = FileOutputStream(targetFile)
                outputStream.use { out ->
                    document.writeTo(out)
                }
                Log.d(TAG, "Saved PDF to legacy file: ${targetFile.absolutePath}")
                Result.success("Saved to ${targetFile.absolutePath}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving PDF: ${e.message}", e)
            Result.failure(e)
        } finally {
            document.close()
        }
    }
}

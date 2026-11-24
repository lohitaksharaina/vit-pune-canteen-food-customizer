package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarmIvory
import com.example.util.MonthlyRevenueReport
import com.example.util.RevenueReportGenerator
import java.util.Locale

@Composable
fun AdminRevenueReportDialog(
    isOpen: Boolean,
    report: MonthlyRevenueReport?,
    onUploadToFirebase: (MonthlyRevenueReport) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen || report == null) return

    val context = LocalContext.current
    var isSavingPdf by remember { mutableStateOf(false) }
    var saveStatusMsg by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    // Android Storage Permission Launcher for older APIs (< Android 10)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val pdfDoc = RevenueReportGenerator.generatePdfDocument(report)
            val res = RevenueReportGenerator.savePdfToStorage(context, pdfDoc, report.fileName)
            res.onSuccess { msg ->
                saveStatusMsg = "✓ $msg"
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            }.onFailure { err ->
                saveStatusMsg = "Error saving PDF: ${err.message}"
            }
        } else {
            Toast.makeText(context, "Storage permission required to download PDF", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .clip(RoundedCornerShape(20.dp))
                .testTag("admin_revenue_report_dialog"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1A237E)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Revenue & Sales Analytics",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = WarmIvory
                            )
                            Text(
                                text = "${report.fileName}.pdf • ${report.monthYearStr}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                // 4 Key Metric Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportStatChip(
                        title = "Gross Revenue",
                        value = "₹${String.format(Locale.getDefault(), "%.0f", report.totalGrossRevenue)}",
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    ReportStatChip(
                        title = "Settled Orders",
                        value = "${report.totalSettledOrders} / ${report.totalOrdersCount}",
                        color = Color(0xFF64B5F6),
                        modifier = Modifier.weight(1f)
                    )
                    ReportStatChip(
                        title = "GST (5%)",
                        value = "₹${String.format(Locale.getDefault(), "%.1f", report.totalGst)}",
                        color = FlameOrange,
                        modifier = Modifier.weight(1f)
                    )
                    ReportStatChip(
                        title = "Avg Order",
                        value = "₹${String.format(Locale.getDefault(), "%.0f", report.averageOrderValue)}",
                        color = Color(0xFFCE93D8),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content preview
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Day-wise revenue list
                    item {
                        Text(
                            text = "DAY-WISE REVENUE BREAKDOWN (${report.dayWiseRevenue.size} Days)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    items(report.dayWiseRevenue) { day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = day.dateStr,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = WarmIvory
                            )
                            Text(
                                text = "${day.ordersCount} orders",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%.2f", day.revenue)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = SuccessGreen
                            )
                        }
                    }

                    // Per-item sales list
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "PER-ITEM SALES BREAKDOWN (${report.itemSales.size} Dishes)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    items(report.itemSales) { itemStat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = itemStat.itemName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = WarmIvory
                                )
                                Text(
                                    text = "Sold: ${itemStat.quantitySold} units (${String.format(Locale.getDefault(), "%.1f", itemStat.percentageOfTotal)}% share)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%.2f", itemStat.totalRevenue)}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = FlameOrange
                            )
                        }
                    }
                }

                // Status message indicator
                if (!saveStatusMsg.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = saveStatusMsg ?: "",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (saveStatusMsg?.startsWith("✓") == true) SuccessGreen else Color(0xFFFF8A80)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: Download PDF vs Upload to Firebase
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Download to Phone Internal/Downloads Storage
                    Button(
                        onClick = {
                            isSavingPdf = true
                            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    val pdfDoc = RevenueReportGenerator.generatePdfDocument(report)
                                    val res = RevenueReportGenerator.savePdfToStorage(context, pdfDoc, report.fileName)
                                    res.onSuccess { msg -> saveStatusMsg = "✓ $msg" }
                                        .onFailure { err -> saveStatusMsg = "Error: ${err.message}" }
                                } else {
                                    permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                }
                            } else {
                                // Android 10+ uses MediaStore / app storage without dangerous permissions
                                val pdfDoc = RevenueReportGenerator.generatePdfDocument(report)
                                val res = RevenueReportGenerator.savePdfToStorage(context, pdfDoc, report.fileName)
                                res.onSuccess { msg -> saveStatusMsg = "✓ $msg" }
                                    .onFailure { err -> saveStatusMsg = "Error: ${err.message}" }
                            }
                            isSavingPdf = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("download_pdf_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = WarmIvory)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Download PDF", style = MaterialTheme.typography.labelMedium, color = WarmIvory)
                    }

                    // Upload to Firebase Cloud
                    Button(
                        onClick = {
                            isUploading = true
                            onUploadToFirebase(report)
                            saveStatusMsg = "✓ Successfully uploaded ${report.fileName} to Firebase Cloud!"
                            isUploading = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upload_firebase_report_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FlameOrange)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Upload Firebase", style = MaterialTheme.typography.labelMedium, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportStatChip(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                color = color,
                maxLines = 1
            )
        }
    }
}

package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AttendanceRecord
import com.example.ui.theme.*
import com.example.util.AppLanguage
import com.example.util.ExcelExportHelper
import com.example.util.LanguageManager
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceExportDialog(
    records: List<AttendanceRecord>,
    dateLabel: String,
    siteLabel: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isMy = currentLanguage == AppLanguage.MYANMAR

    val totalManDays = records.sumOf { it.totalDayUnits }
    val totalEarned = records.sumOf { it.totalEarned }
    val totalAdvance = records.sumOf { it.advanceToday }
    val totalPaid = records.sumOf { it.paidToday }
    val totalBalance = totalEarned - totalAdvance - totalPaid

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("attendance_export_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ConstructionGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Print,
                                contentDescription = null,
                                tint = Slate900,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isMy) "တက်ရောက်မှု မှတ်တမ်း ထုတ်ယူ & Print" else "Export & Print Attendance",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$dateLabel • $siteLabel",
                                fontSize = 12.sp,
                                color = Slate500
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate600)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Calculation Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate900)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isMy) "တွက်ချက်မှု အကျဉ်းချုပ် (Summary)" else "Wages & Attendance Summary",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ConstructionGold
                                )
                                Surface(
                                    color = Slate800,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "${records.size} ဦး (${String.format(Locale.US, "%.1f", totalManDays)} ရက်)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Divider(color = Slate800, thickness = 1.dp)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(if (isMy) "နေ့တွက်ရငွေ စုစုပေါင်း" else "Total Wages", fontSize = 10.sp, color = Slate400)
                                    Text(ExcelExportHelper.formatCurrency(totalEarned), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = WagesGreen)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (isMy) "ကြိုထုတ်ငွေ" else "Advance", fontSize = 10.sp, color = Slate400)
                                    Text(ExcelExportHelper.formatCurrency(totalAdvance), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AdvanceRedLight)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(if (isMy) "ရှင်းရန်ကျန်ငွေ" else "Net Balance Due", fontSize = 10.sp, color = Slate400)
                                    Text(ExcelExportHelper.formatCurrency(totalBalance), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = ConstructionGold)
                                }
                            }
                        }
                    }

                    if (records.isEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Slate500, modifier = Modifier.size(32.dp))
                                Text(
                                    text = if (isMy) "ထုတ်ယူရန် တက်ရောက်မှု မှတ်တမ်း မရှိပါ" else "No attendance records to export",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isMy) "ရက်စွဲ သို့မဟုတ် ဆိုဒ် filter ကို ပြောင်းလဲပေးပါ" else "Please adjust date or site filter",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                        }
                    } else {
                        // Section 1: Direct Print Out
                        ExportOptionSection(
                            title = if (isMy) "1. တိုက်ရိုက် Print ထုတ်ရန် (Print Out)" else "1. Direct Print Out (A4 Landscape)",
                            description = if (isMy) "Printer စက်များသို့ တိုက်ရိုက်ချိတ်ဆက်ထုတ်ယူခြင်း သို့မဟုတ် Print Preview မှ PDF အဖြစ် သိမ်းဆည်းခြင်း" else "Send directly to printer or save via Android Print Preview",
                            icon = Icons.Default.Print,
                            iconColor = ConstructionGoldDark,
                            primaryActionTitle = if (isMy) "🖨️ ပရင့်စတင်ထုတ်မည် (Print Now)" else "🖨️ Print Now",
                            onPrimaryAction = {
                                val pdfFile = ExcelExportHelper.exportAttendancePdf(context, records, dateLabel, siteLabel)
                                ExcelExportHelper.printPdf(context, pdfFile, "Attendance_${dateLabel.replace("-", "")}")
                            },
                            secondaryActionTitle = if (isMy) "HTML Preview ဖြင့် Print" else "Print as Styled HTML",
                            onSecondaryAction = {
                                val html = ExcelExportHelper.generateAttendanceHtml(records, dateLabel, siteLabel)
                                ExcelExportHelper.printHtml(context, html, "Attendance_HTML_${dateLabel.replace("-", "")}")
                            }
                        )

                        // Section 2: PDF Document (.pdf)
                        ExportOptionSection(
                            title = if (isMy) "2. PDF ဖိုင်အဖြစ် ထုတ်ယူ သိမ်းဆည်းရန်" else "2. PDF Document (.pdf)",
                            description = if (isMy) "A4 Landscape အရွယ်အစားဖြင့် စနစ်တကျ ပုံနှိပ်ထုတ်ယူနိုင်သော တရားဝင် PDF ဖိုင်" else "Official A4 Landscape formatted PDF document for printing and sharing",
                            icon = Icons.Default.PictureAsPdf,
                            iconColor = AdvanceRed,
                            primaryActionTitle = if (isMy) "👁️ PDF ဖွင့်ကြည့်မည်" else "👁️ Open PDF",
                            onPrimaryAction = {
                                val pdfFile = ExcelExportHelper.exportAttendancePdf(context, records, dateLabel, siteLabel)
                                ExcelExportHelper.openExportFile(context, pdfFile, "application/pdf", "Open PDF Attendance Report")
                            },
                            secondaryActionTitle = if (isMy) "💾 ဖုန်းထဲသိမ်းမည် (Download)" else "💾 Save to Downloads",
                            onSecondaryAction = {
                                val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                val filename = "Labour_Attendance_$stamp.pdf"
                                val pdfFile = ExcelExportHelper.exportAttendancePdf(context, records, dateLabel, siteLabel)
                                val saved = ExcelExportHelper.saveToDownloads(context, pdfFile, filename, "application/pdf")
                                Toast.makeText(
                                    context,
                                    if (saved) {
                                        if (isMy) "PDF ဖိုင်ကို Downloads/LabourPro သို့ သိမ်းဆည်းပြီးပါပြီ ($filename)" else "PDF saved to Downloads/LabourPro ($filename)"
                                    } else {
                                        if (isMy) "ဖိုင်သိမ်းဆည်းရာတွင် အမှားဖြစ်ပေါ်ပါသည်" else "Failed to save file to Downloads"
                                    },
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            tertiaryActionTitle = if (isMy) "📤 မျှဝေမည် (Share PDF)" else "📤 Share PDF",
                            onTertiaryAction = {
                                val pdfFile = ExcelExportHelper.exportAttendancePdf(context, records, dateLabel, siteLabel)
                                ExcelExportHelper.shareExportFile(context, pdfFile, "application/pdf", "Attendance Report - $dateLabel ($siteLabel)")
                            }
                        )

                        // Section 3: Excel Spreadsheet (.xlsx / .xml)
                        ExportOptionSection(
                            title = if (isMy) "3. Microsoft Excel စာရင်းဇယား ဖိုင် (.xlsx / .xml)" else "3. Excel Spreadsheet (.xls / .xml)",
                            description = if (isMy) "Microsoft Excel, Google Sheets, WPS Office တို့တွင် ပုံစံမပျက် တွက်ချက်နိုင်သော စာရင်းဇယား" else "Full XML Spreadsheet natively supported in MS Excel, Sheets, and WPS Office",
                            icon = Icons.Default.TableChart,
                            iconColor = WagesGreenDark,
                            primaryActionTitle = if (isMy) "📊 Excel ဖြင့်ဖွင့်မည်" else "📊 Open in Excel",
                            onPrimaryAction = {
                                val excelFile = ExcelExportHelper.exportAttendanceExcel(context, records, dateLabel, siteLabel)
                                ExcelExportHelper.openExportFile(context, excelFile, "application/vnd.ms-excel", "Open Excel Report")
                            },
                            secondaryActionTitle = if (isMy) "💾 ဖုန်းထဲသိမ်းမည် (Download)" else "💾 Save to Downloads",
                            onSecondaryAction = {
                                val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                val filename = "Labour_Attendance_$stamp.xls"
                                val excelFile = ExcelExportHelper.exportAttendanceExcel(context, records, dateLabel, siteLabel)
                                val saved = ExcelExportHelper.saveToDownloads(context, excelFile, filename, "application/vnd.ms-excel")
                                Toast.makeText(
                                    context,
                                    if (saved) {
                                        if (isMy) "Excel ဖိုင်ကို Downloads/LabourPro သို့ သိမ်းဆည်းပြီးပါပြီ ($filename)" else "Excel file saved to Downloads/LabourPro ($filename)"
                                    } else {
                                        if (isMy) "ဖိုင်သိမ်းဆည်းရာတွင် အမှားဖြစ်ပေါ်ပါသည်" else "Failed to save file"
                                    },
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            tertiaryActionTitle = if (isMy) "📤 မျှဝေမည် (Share Excel)" else "📤 Share Excel",
                            onTertiaryAction = {
                                val excelFile = ExcelExportHelper.exportAttendanceExcel(context, records, dateLabel, siteLabel)
                                ExcelExportHelper.shareExportFile(context, excelFile, "application/vnd.ms-excel", "Excel Attendance Report - $dateLabel")
                            }
                        )

                        // Section 4: CSV Data File (.csv)
                        ExportOptionSection(
                            title = if (isMy) "4. CSV ဒေတာဖိုင် (.csv)" else "4. CSV Data File (.csv)",
                            description = if (isMy) "Unicode UTF-8 BOM ပါဝင်ပြီး စာလုံးမပျက် ဒေတာသွင်းယူ/သိမ်းဆည်းနိုင်သော ဖိုင်" else "UTF-8 BOM CSV compatible with Microsoft Excel, Google Sheets and database systems",
                            icon = Icons.Default.Description,
                            iconColor = OtBlue,
                            primaryActionTitle = if (isMy) "💾 CSV ဖုန်းထဲသိမ်းမည်" else "💾 Save CSV",
                            onPrimaryAction = {
                                val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                val filename = "Labour_Attendance_$stamp.csv"
                                val csvFile = ExcelExportHelper.exportAttendanceCsv(context, records, dateLabel, siteLabel)
                                val saved = ExcelExportHelper.saveToDownloads(context, csvFile, filename, "text/csv")
                                Toast.makeText(
                                    context,
                                    if (saved) {
                                        if (isMy) "CSV ဖိုင်ကို Downloads/LabourPro သို့ သိမ်းဆည်းပြီးပါပြီ ($filename)" else "CSV saved to Downloads/LabourPro ($filename)"
                                    } else {
                                        if (isMy) "ဖိုင်သိမ်းဆည်းရာတွင် အမှားဖြစ်ပေါ်ပါသည်" else "Failed to save CSV"
                                    },
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            secondaryActionTitle = if (isMy) "📤 မျှဝေမည် (Share CSV)" else "📤 Share CSV",
                            onSecondaryAction = {
                                val csvFile = ExcelExportHelper.exportAttendanceCsv(context, records, dateLabel, siteLabel)
                                ExcelExportHelper.shareExportFile(context, csvFile, "text/csv", "CSV Attendance Data - $dateLabel")
                            }
                        )

                        // Section 5: Text / Message Summary (Viber / Telegram)
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(20.dp))
                                    Text(
                                        text = if (isMy) "5. Viber / Telegram စာသားအကျဉ်းချုပ် မျှဝေရန်" else "5. Share Text Summary (Viber/Telegram)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = if (isMy) "ဆိုဒ်တာဝန်ခံနှင့် အင်ဂျင်နီယာထံသို့ နေ့စဉ်လုပ်အားခ အကျဉ်းချုပ် စာတိုပေးပို့ရန်" else "Send structured daily labour text report to supervisors via messaging apps",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                                OutlinedButton(
                                    onClick = {
                                        val reportText = """
                                        🏗️ LabourPro နေ့စဉ်လုပ်အားခနှင့် တက်ရောက်မှု အစီရင်ခံစာ
                                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                                        🏢 ဆိုဒ်အမည်: $siteLabel
                                        📅 ရက်စွဲ: $dateLabel
                                        👷 လုပ်သားဦးရေ: ${records.size} ဦး (${String.format(Locale.US, "%.1f", totalManDays)} Man-Days)
                                        💰 နေ့တွက်ရငွေ စုစုပေါင်း: %,.0f ကျပ်
                                        🔻 ကြိုထုတ်ငွေ စုစုပေါင်း: %,.0f ကျပ်
                                        🔻 ပေးချေပြီးငွေ: %,.0f ကျပ်
                                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                                        ✨ ရှင်းရန်ကျန်ငွေ: %,.0f ကျပ်
                                        ━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                                        ထုတ်ယူချိန်: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}
                                        """.trimIndent().format(Locale.US, totalEarned, totalAdvance, totalPaid, totalBalance)

                                        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, reportText)
                                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Labour Report - $siteLabel ($dateLabel)")
                                        }
                                        context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Summary via"))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isMy) "စာတို မျှဝေမည် (Share Message)" else "Share Text Report", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900)
                ) {
                    Text(if (isMy) "ပိတ်မည်" else "Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ExportOptionSection(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    primaryActionTitle: String,
    onPrimaryAction: () -> Unit,
    secondaryActionTitle: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    tertiaryActionTitle: String? = null,
    onTertiaryAction: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = description,
                fontSize = 11.sp,
                color = Slate600
            )

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = iconColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = primaryActionTitle,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (iconColor == ConstructionGoldDark) Color.White else Color.White
                    )
                }

                if (secondaryActionTitle != null && onSecondaryAction != null) {
                    OutlinedButton(
                        onClick = onSecondaryAction,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = secondaryActionTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (tertiaryActionTitle != null && onTertiaryAction != null) {
                    FilledTonalButton(
                        onClick = onTertiaryAction,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tertiaryActionTitle,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RemoveCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ClassConstants
import com.example.model.KasPaymentEntry
import com.example.model.KasTransaction
import com.example.model.StudentMaster
import com.example.ui.components.ClassCard
import com.example.ui.components.PortalFeedback
import com.example.ui.components.StatusBadge
import com.example.ui.components.V9DialogCard
import com.example.ui.components.V9DropdownButton
import com.example.ui.components.V9Field
import com.example.ui.components.V9FooterText
import com.example.ui.components.V9PillChip
import com.example.ui.components.formatRupiah
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldKas
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.PrimaryTintDark
import com.example.ui.theme.PrimaryTintLight
import com.example.ui.theme.RoseAlpa
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueBright
import com.example.ui.theme.RoyalBlueDeep
import com.example.ui.theme.RoyalBlueMid
import com.example.viewmodel.ClassPortalData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KasScreen(
    data: ClassPortalData,
    onSelectPeriod: (String) -> Unit,
    onSelectNominal: (Long) -> Unit,
    onAddPeriod: (String, () -> Unit) -> Unit,
    onToggleStudentPayment: (Int) -> Unit,
    onMarkAllPaid: () -> Unit,
    onAddTransaction: (String, Long, String, String, String, () -> Unit) -> Unit,
    onDeleteTransaction: (KasTransaction) -> Unit,
    onRequestAdminLogin: () -> Unit
) {
    val context = LocalContext.current
    val isGlass = LocalLiquidGlass.current
    val isDark = LocalIsDarkTheme.current
    val summary = data.kasSummaryForPeriod(data.selectedKasPeriod)
    var subView by remember { mutableStateOf("siswa") } // "siswa" | "transaksi"
    var studentFilter by remember { mutableStateOf("ALL") } // "ALL" | "PAID" | "UNPAID"
    var txFilter by remember { mutableStateOf("ALL") } // "ALL" | "Pemasukan" | "Pengeluaran"
    var localStudentSearch by remember { mutableStateOf("") }
    var periodMenuOpen by remember { mutableStateOf(false) }
    var nominalMenuOpen by remember { mutableStateOf(false) }
    var showAddPeriodDialog by remember { mutableStateOf(false) }
    var showTxDialog by remember { mutableStateOf<String?>(null) }

    val csvContent = remember(data.selectedKasPeriod, summary, data.kasTransactions) {
        buildString {
            appendLine("No Absen,Nama Siswa,Periode Kas,Status Pembayaran,Nominal,Tanggal Bayar")
            ClassConstants.STUDENTS.forEach { st ->
                val info = summary.payments[st.id.toString()]
                val paid = info?.paid == true
                appendLine("\"${st.noAbsen}\",\"${st.name}\",\"${data.selectedKasPeriod}\",\"${if (paid) "Sudah Bayar (Lunas)" else "Belum Bayar"}\",${if (paid) info?.amount ?: 0 else 0},\"${if (paid) info?.date ?: "-" else "-"}\"")
            }
            appendLine()
            appendLine("Tanggal,Jenis Transaksi,Kategori,Nominal,Keterangan,Petugas")
            data.kasTransactions.filter { !it.voided }.forEach { tx ->
                appendLine("\"${tx.date}\",\"${tx.type}\",\"${tx.category}\",${tx.amount},\"${tx.note.replace("\"", "\"\"")}\",\"${tx.author}\"")
            }
        }
    }

    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Laporan Uang Kas (.CSV) tersimpan.", Toast.LENGTH_SHORT).show()
            } catch (_: Exception) {
                Toast.makeText(context, "Gagal menyimpan CSV.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val globalQ = data.globalSearch.trim().lowercase()
    val localQ = localStudentSearch.trim().lowercase()
    val filteredStudents = remember(data.selectedKasPeriod, summary.payments, studentFilter, globalQ, localQ) {
        ClassConstants.STUDENTS.filter { st ->
            val paid = summary.payments[st.id.toString()]?.paid == true
            val matchGlobal = globalQ.isBlank() || st.name.lowercase().contains(globalQ) || st.noAbsen.contains(globalQ)
            val matchLocal = localQ.isBlank() || st.name.lowercase().contains(localQ) || st.noAbsen.contains(localQ)
            val matchFilter = when (studentFilter) {
                "PAID" -> paid
                "UNPAID" -> !paid
                else -> true
            }
            matchGlobal && matchLocal && matchFilter
        }
    }

    val activeTxCount = data.kasTransactions.count { !it.voided }
    val filteredTx = data.kasTransactions.filter { tx ->
        !tx.voided &&
            (txFilter == "ALL" || tx.type == txFilter) &&
            (globalQ.isBlank() || tx.note.lowercase().contains(globalQ) || tx.category.lowercase().contains(globalQ))
    }

    val heroBrush = when {
        isGlass && isDark -> Brush.linearGradient(listOf(Color(0xD6125EF0), Color(0xD6083096)))
        isGlass -> Brush.linearGradient(listOf(Color(0xE01476FF), Color(0xD60048CC)))
        isDark -> Brush.linearGradient(listOf(Color(0xFF0A4FC4), RoyalBlueDeep))
        else -> Brush.linearGradient(listOf(RoyalBlueBright, RoyalBlueMid))
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val studentCols = when {
            screenWidth >= 840.dp -> 3
            screenWidth >= 560.dp -> 2
            else -> 1
        }
        val studentRows = remember(filteredStudents, studentCols) { filteredStudents.chunked(studentCols) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Hero Saldo Kas Card matching HTML lines 955-1028
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(if (isGlass) 22.dp else 18.dp))
                        .background(heroBrush)
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Top header row + 4 pill action buttons
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.20f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AccountBalanceWallet,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "KAS TRANSPARAN CLASS XI",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                Text(
                                    text = "Iuran Standar: Rp 10.000 / Bulan",
                                    color = Color(0xFFD1FAE5),
                                    fontFamily = JetBrainsMono,
                                    fontSize = 11.sp,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )
                            }

                            Column {
                                Text(
                                    text = "Saldo Kas Kelas Saat Ini",
                                    color = Color(0xFFD1FAE5).copy(alpha = 0.92f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = formatRupiah(summary.saldo),
                                    color = Color.White,
                                    fontFamily = JetBrainsMono,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 30.sp
                                )
                            }

                            // 4 Hero Action Pills matching HTML lines 972-989
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            if (data.isAdminMode) showTxDialog = "Pemasukan" else onRequestAdminLogin()
                                        }
                                        .minimumInteractiveComponentSize()
                                        .testTag("kas_add_in_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.AddCircle,
                                            contentDescription = null,
                                            tint = EmeraldKas,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Catat Pemasukan",
                                            color = Color(0xFF065F46),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = RoseAlpa,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            if (data.isAdminMode) showTxDialog = "Pengeluaran" else onRequestAdminLogin()
                                        }
                                        .minimumInteractiveComponentSize()
                                        .testTag("kas_add_out_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.RemoveCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Catat Pengeluaran",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.20f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            val safeName = "Laporan_Uang_Kas_CLASS_XI_${data.selectedKasPeriod.replace(" ", "_")}.csv"
                                            createCsvLauncher.launch(safeName)
                                        }
                                        .minimumInteractiveComponentSize()
                                        .testTag("kas_export_csv_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Download,
                                            contentDescription = "Excel CSV",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Excel .CSV",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.20f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            val recent = data.kasTransactions.filter { !it.voided }.take(8).joinToString("\n") { tx ->
                                                val sign = if (tx.type == "Pemasukan") "+" else "-"
                                                "• [${tx.date}] $sign${formatRupiah(tx.amount)} (${tx.note})"
                                            }.ifBlank { "Belum ada transaksi." }
                                            val msg = "*LAPORAN UANG KAS CLASS XI*\nSaldo: *${formatRupiah(summary.saldo)}*\nPemasukan: ${formatRupiah(summary.totalIn)}\nPengeluaran: ${formatRupiah(summary.totalOut)}\n\n$recent"
                                            PortalFeedback.shareText(context, "Laporan Kas CLASS XI", msg)
                                        }
                                        .minimumInteractiveComponentSize()
                                        .testTag("kas_share_report_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Share,
                                            contentDescription = "Laporan WA",
                                            tint = Color.White,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = "Laporan WA",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // 3 Sub-cards Statistik Kas matching HTML lines 993-1027
                        val subCardIn: @Composable (Modifier) -> Unit = { mod ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.16f),
                                modifier = mod
                            ) {
                                Row(
                                    modifier = Modifier.padding(13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Total Pemasukan", color = Color(0xFFD1FAE5), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(formatRupiah(summary.totalIn), color = Color.White, fontFamily = JetBrainsMono, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = Color(0xFFA7F3D0), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        val subCardOut: @Composable (Modifier) -> Unit = { mod ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.16f),
                                modifier = mod
                            ) {
                                Row(
                                    modifier = Modifier.padding(13.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Total Pengeluaran", color = Color(0xFFD1FAE5), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(formatRupiah(summary.totalOut), color = Color(0xFFFECDD3), fontFamily = JetBrainsMono, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(RoseAlpa.copy(alpha = 0.25f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Rounded.TrendingDown, contentDescription = null, tint = Color(0xFFFECDD3), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        val subCardProg: @Composable (Modifier) -> Unit = { mod ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.16f),
                                modifier = mod
                            ) {
                                Column(modifier = Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Progres Lunas (${data.selectedKasPeriod})",
                                            color = Color(0xFFD1FAE5),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${summary.paidCount} / 47",
                                            color = Color.White,
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.Black.copy(alpha = 0.22f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction = (summary.paidPercent / 100f).coerceIn(0f, 1f))
                                                .height(7.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(Color.White)
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${summary.paidPercent}% Lunas", color = Color(0xFFD1FAE5), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                                        Text("${summary.unpaidCount} Belum Bayar", color = Color(0xFFD1FAE5), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }

                        if (screenWidth >= 640.dp) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                subCardIn(Modifier.weight(1f))
                                subCardOut(Modifier.weight(1f))
                                subCardProg(Modifier.weight(1.1f))
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    subCardIn(Modifier.weight(1f))
                                    subCardOut(Modifier.weight(1f))
                                }
                                subCardProg(Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }

            // 2. Sub-Navigation Switcher matching HTML lines 1031-1041
            item {
                ClassCard(contentPadding = PaddingValues(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val siswaActive = subView == "siswa"
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (siswaActive) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { subView = "siswa" }
                                .minimumInteractiveComponentSize()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Group,
                                    contentDescription = null,
                                    tint = if (siswaActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Status Bayar 47 Siswa",
                                    fontSize = 12.sp,
                                    fontWeight = if (siswaActive) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (siswaActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        val txActive = subView == "transaksi"
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (txActive) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { subView = "transaksi" }
                                .minimumInteractiveComponentSize()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 11.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ReceiptLong,
                                    contentDescription = null,
                                    tint = if (txActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Pemasukan & Pengeluaran",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (txActive) FontWeight.ExtraBold else FontWeight.Bold,
                                    color = if (txActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = if (txActive) Color.White.copy(alpha = 0.24f) else MaterialTheme.colorScheme.outline
                                ) {
                                    Text(
                                        text = activeTxCount.toString(),
                                        fontFamily = JetBrainsMono,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (txActive) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (subView == "siswa") {
                // Period, Nominal & WA/MarkAll Control Card matching HTML lines 1046-1081
                item {
                    ClassCard {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "PERIODE TAGIHAN KAS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box {
                                            V9DropdownButton(
                                                text = data.selectedKasPeriod,
                                                onClick = { periodMenuOpen = true },
                                                modifier = Modifier
                                                    .width(185.dp)
                                                    .testTag("kas_period_dropdown")
                                            )
                                            DropdownMenu(expanded = periodMenuOpen, onDismissRequest = { periodMenuOpen = false }) {
                                                data.kasPeriodNames.forEach { p ->
                                                    DropdownMenuItem(
                                                        text = { Text(p) },
                                                        onClick = { onSelectPeriod(p); periodMenuOpen = false }
                                                    )
                                                }
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (isDark) PrimaryTintDark else PrimaryTintLight,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .clickable {
                                                    if (data.isAdminMode) showAddPeriodDialog = true else onRequestAdminLogin()
                                                }
                                                .minimumInteractiveComponentSize()
                                                .testTag("kas_add_period_btn")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.Add,
                                                    contentDescription = "Tambah Periode Kas",
                                                    tint = if (isDark) RoyalBlueBright else RoyalBlue,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "Periode",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDark) RoyalBlueBright else RoyalBlue
                                                )
                                            }
                                        }
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "NOMINAL IURAN",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Box {
                                        V9DropdownButton(
                                            text = formatRupiah(data.selectedKasNominal),
                                            onClick = { nominalMenuOpen = true },
                                            modifier = Modifier
                                                .width(136.dp)
                                                .testTag("kas_nominal_dropdown")
                                        )
                                        DropdownMenu(expanded = nominalMenuOpen, onDismissRequest = { nominalMenuOpen = false }) {
                                            ClassConstants.KAS_NOMINALS.forEach { nom ->
                                                DropdownMenuItem(
                                                    text = { Text(formatRupiah(nom)) },
                                                    onClick = { onSelectNominal(nom); nominalMenuOpen = false }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val waBg = if (isDark) Color(0x28F59E0B) else Color(0xFFFFFBEB)
                                val waBorder = if (isDark) Color(0x55F59E0B) else Color(0xFFFDE68A)
                                val waFg = if (isDark) Color(0xFFFCD34D) else Color(0xFF92400E)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = waBg,
                                    border = BorderStroke(1.dp, waBorder),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            val unpaidLines = ClassConstants.STUDENTS.mapNotNull { st ->
                                                val info = summary.payments[st.id.toString()]
                                                if (info?.paid == true) null else "⏳ ${st.noAbsen}. ${st.name}"
                                            }
                                            val msg = buildString {
                                                appendLine("*REKAP PEMBAYARAN UANG KAS CLASS XI*")
                                                appendLine("Periode: *${data.selectedKasPeriod}*")
                                                appendLine("Lunas: ${summary.paidCount}/47 Siswa | Belum: ${summary.unpaidCount} Siswa")
                                                appendLine()
                                                if (unpaidLines.isEmpty()) appendLine("Alhamdulillah semua 47 siswa sudah lunas!")
                                                else unpaidLines.forEach { appendLine(it) }
                                            }
                                            PortalFeedback.shareText(context, "Tagihan Kas ${data.selectedKasPeriod}", msg)
                                        }
                                        .minimumInteractiveComponentSize()
                                        .testTag("kas_share_unpaid_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(Icons.Rounded.Share, contentDescription = "Kirim Tagihan WA", tint = EmeraldKas, modifier = Modifier.size(14.dp))
                                        Text("Salin Belum Bayar WA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = waFg)
                                    }
                                }

                                if (data.isAdminMode) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = EmeraldKas,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .clickable(onClick = onMarkAllPaid)
                                            .minimumInteractiveComponentSize()
                                            .testTag("kas_mark_all_paid_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Icon(Icons.Rounded.DoneAll, contentDescription = "Lunaskan Semua", tint = Color.White, modifier = Modifier.size(15.dp))
                                            Text("Lunaskan Semua", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Filter Pills + Search matching HTML lines 1083-1094
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            V9PillChip(
                                text = "Semua (47)",
                                selected = studentFilter == "ALL",
                                onClick = { studentFilter = "ALL" }
                            )
                            V9PillChip(
                                text = "Sudah Bayar (${summary.paidCount})",
                                selected = studentFilter == "PAID",
                                onClick = { studentFilter = "PAID" },
                                unselectedContentColor = if (isDark) Color(0xFF6EE7B7) else EmeraldDark,
                                unselectedBorderColor = if (isDark) Color(0x4D10B981) else Color(0xFFA7F3D0)
                            )
                            V9PillChip(
                                text = "Belum Bayar (${summary.unpaidCount})",
                                selected = studentFilter == "UNPAID",
                                onClick = { studentFilter = "UNPAID" },
                                unselectedContentColor = RoseAlpa,
                                unselectedBorderColor = if (isDark) Color(0x4DF43F5E) else Color(0xFFFECDD3)
                            )
                        }
                        V9Field(
                            value = localStudentSearch,
                            onValueChange = { localStudentSearch = it },
                            placeholder = "Cari nama lengkap siswa...",
                            leadingIcon = Icons.Rounded.Search,
                            roundedPill = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (studentRows.isEmpty()) {
                    item {
                        ClassCard {
                            Text(
                                text = "Tidak ada data siswa yang sesuai filter.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ubah kata kunci pencarian atau pilih filter Semua (47).",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(studentRows, key = { chunk -> chunk.first().id }) { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEach { st ->
                                Box(modifier = Modifier.weight(1f)) {
                                    KasStudentCard(
                                        st = st,
                                        payInfo = summary.payments[st.id.toString()],
                                        selectedKasPeriod = data.selectedKasPeriod,
                                        isDark = isDark,
                                        onToggle = {
                                            if (data.isAdminMode) onToggleStudentPayment(st.id) else onRequestAdminLogin()
                                        }
                                    )
                                }
                            }
                            repeat(studentCols - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                // Transaction Filter Card matching HTML lines 1102-1119
                item {
                    ClassCard {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                V9PillChip(
                                    text = "Semua Transaksi",
                                    selected = txFilter == "ALL",
                                    onClick = { txFilter = "ALL" }
                                )
                                V9PillChip(
                                    text = "Pemasukan (+)",
                                    selected = txFilter == "Pemasukan",
                                    onClick = { txFilter = "Pemasukan" },
                                    unselectedContentColor = if (isDark) Color(0xFF6EE7B7) else EmeraldDark
                                )
                                V9PillChip(
                                    text = "Pengeluaran (-)",
                                    selected = txFilter == "Pengeluaran",
                                    onClick = { txFilter = "Pengeluaran" },
                                    unselectedContentColor = RoseAlpa
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = EmeraldKas,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            if (data.isAdminMode) showTxDialog = "Pemasukan" else onRequestAdminLogin()
                                        }
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                        Text("+ Pemasukan", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = RoseAlpa,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(50))
                                        .clickable {
                                            if (data.isAdminMode) showTxDialog = "Pengeluaran" else onRequestAdminLogin()
                                        }
                                        .minimumInteractiveComponentSize()
                                ) {
                                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                        Text("- Pengeluaran", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                if (filteredTx.isEmpty()) {
                    item {
                        ClassCard {
                            Text("Belum Ada Riwayat Transaksi Kas", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Catat pemasukan atau pengeluaran kelas agar seluruh siswa dapat memantau transparansi kas.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredTx, key = { it.id }) { tx ->
                        val isIn = tx.type == "Pemasukan"
                        ClassCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isIn) {
                                                    if (isDark) Color(0x2810B981) else Color(0xFFECFDF5)
                                                } else {
                                                    if (isDark) Color(0x28F43F5E) else Color(0xFFFFF1F2)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isIn) Icons.Rounded.ArrowDownward else Icons.Rounded.ArrowUpward,
                                            contentDescription = tx.type,
                                            tint = if (isIn) EmeraldKas else RoseAlpa
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                            StatusBadge(
                                                text = tx.category,
                                                containerColor = if (isIn) {
                                                    if (isDark) Color(0x2810B981) else Color(0xFFD1FAE5)
                                                } else {
                                                    if (isDark) Color(0x28F43F5E) else Color(0xFFFFE4E6)
                                                },
                                                contentColor = if (isIn) {
                                                    if (isDark) Color(0xFF6EE7B7) else EmeraldDark
                                                } else {
                                                    if (isDark) Color(0xFFFDA4AF) else Color(0xFF9F1239)
                                                },
                                                cornerRadius = 50.dp
                                            )
                                            Text(tx.date, fontSize = 10.5.sp, fontFamily = JetBrainsMono, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(tx.note, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        Text("Oleh: ${tx.author}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${if (isIn) "+" else "-"} ${formatRupiah(tx.amount)}",
                                        fontFamily = JetBrainsMono,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.sp,
                                        color = if (isIn) EmeraldKas else RoseAlpa
                                    )
                                    if (data.isAdminMode) {
                                        IconButton(onClick = { onDeleteTransaction(tx) }) {
                                            Icon(Icons.Rounded.Delete, contentDescription = "Hapus Transaksi", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { V9FooterText() }
        }
    }

    if (showAddPeriodDialog) {
        var periodName by remember { mutableStateOf("") }
        V9DialogCard(
            onDismissRequest = { showAddPeriodDialog = false },
            maxWidth = 380.dp
        ) {
            Text("Tambah Periode Tagihan Kas", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text("Buat periode baru untuk mendata pembayaran 47 siswa", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            V9Field(
                value = periodName,
                onValueChange = { periodName = it },
                placeholder = "Contoh: Minggu 5 - Nov 2026"
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showAddPeriodDialog = false },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) { Text("Batal", fontWeight = FontWeight.Bold) }
                Button(
                    onClick = { onAddPeriod(periodName) { showAddPeriodDialog = false } },
                    enabled = periodName.isNotBlank(),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) { Text("Buat Periode", fontWeight = FontWeight.Bold) }
            }
        }
    }

    if (showTxDialog != null) {
        var txType by remember(showTxDialog) { mutableStateOf(showTxDialog!!) }
        var amountStr by remember { mutableStateOf("") }
        var dateStr by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
        var category by remember(showTxDialog) {
            mutableStateOf(
                if (showTxDialog == "Pemasukan") "Dana Tambahan / Donasi" else "Perlengkapan Kebersihan Kelas"
            )
        }
        var catMenuOpen by remember { mutableStateOf(false) }
        var noteStr by remember { mutableStateOf("") }

        V9DialogCard(onDismissRequest = { showTxDialog = null }) {
            Text("Catat Transaksi Uang Kas", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text("Pencatatan kas masuk & keluar transparan CLASS XI", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (txType == "Pemasukan") EmeraldKas else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { txType = "Pemasukan"; category = "Dana Tambahan / Donasi" }
                        .minimumInteractiveComponentSize()
                ) {
                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Pemasukan (+)",
                            color = if (txType == "Pemasukan") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (txType == "Pengeluaran") RoseAlpa else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { txType = "Pengeluaran"; category = "Perlengkapan Kebersihan Kelas" }
                        .minimumInteractiveComponentSize()
                ) {
                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Pengeluaran (-)",
                            color = if (txType == "Pengeluaran") Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Nominal (Rp)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    V9Field(
                        value = amountStr,
                        onValueChange = { amountStr = it.filter(Char::isDigit) },
                        placeholder = "50000",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tanggal", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    V9Field(
                        value = dateStr,
                        onValueChange = { dateStr = it },
                        placeholder = "YYYY-MM-DD"
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Kategori", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                Box {
                    V9DropdownButton(
                        text = category,
                        onClick = { catMenuOpen = true },
                        roundedPill = false
                    )
                    DropdownMenu(expanded = catMenuOpen, onDismissRequest = { catMenuOpen = false }) {
                        ClassConstants.KAS_CATEGORIES.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; catMenuOpen = false })
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Keterangan / Rincian", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                V9Field(
                    value = noteStr,
                    onValueChange = { noteStr = it },
                    placeholder = "Contoh: Beli spidol papan tulis & penghapus",
                    singleLine = false,
                    minLines = 2,
                    maxLines = 3
                )
            }

            val amt = amountStr.toLongOrNull() ?: 0L
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showTxDialog = null },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) { Text("Batal", fontWeight = FontWeight.Bold) }
                Button(
                    onClick = { onAddTransaction(txType, amt, dateStr, category, noteStr) { showTxDialog = null } },
                    enabled = amt > 0 && noteStr.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (txType == "Pemasukan") EmeraldKas else RoseAlpa),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) { Text("Simpan", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun KasStudentCard(
    st: StudentMaster,
    payInfo: KasPaymentEntry?,
    selectedKasPeriod: String,
    isDark: Boolean,
    onToggle: () -> Unit
) {
    val paid = payInfo?.paid == true
    val borderCol = when {
        paid && isDark -> Color(0x5510B981)
        paid -> Color(0xFFA7F3D0)
        isDark -> Color(0x4DF43F5E)
        else -> Color(0xFFFECDD3)
    }
    val cardBg = when {
        paid && isDark -> Color(0xFF102421)
        paid -> Color(0xFFF7FEFA)
        else -> MaterialTheme.colorScheme.surface
    }

    ClassCard(
        borderColor = borderCol,
        containerColor = cardBg,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (paid) {
                                if (isDark) Color(0x3010B981) else Color(0xFFD1FAE5)
                            } else {
                                if (isDark) Color(0x30F43F5E) else Color(0xFFFFE4E6)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = st.noAbsen,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = if (paid) {
                            if (isDark) Color(0xFF6EE7B7) else EmeraldDark
                        } else {
                            if (isDark) Color(0xFFFDA4AF) else RoseAlpa
                        }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = st.name,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (paid) "${formatRupiah(payInfo?.amount ?: 0L)} • ${payInfo?.date ?: ""}" else "Tagihan $selectedKasPeriod",
                        fontSize = 11.sp,
                        color = if (paid) {
                            if (isDark) Color(0xFF6EE7B7) else EmeraldDark
                        } else {
                            if (isDark) Color(0xFFFDA4AF) else RoseAlpa
                        },
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = if (paid) EmeraldKas else if (isDark) Color(0x2EF43F5E) else Color(0xFFFFF1F2),
                border = if (paid) null else BorderStroke(1.dp, if (isDark) Color(0x55F43F5E) else Color(0xFFFECDD3)),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onToggle)
                    .minimumInteractiveComponentSize()
                    .testTag("kas_toggle_${st.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (paid) Icons.Rounded.CheckCircle else Icons.Rounded.Schedule,
                        contentDescription = if (paid) "Lunas" else "Belum Bayar",
                        tint = if (paid) Color.White else if (isDark) Color(0xFFFDA4AF) else RoseAlpa,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (paid) "Lunas" else "Belum Bayar",
                        color = if (paid) Color.White else if (isDark) Color(0xFFFDA4AF) else RoseAlpa,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

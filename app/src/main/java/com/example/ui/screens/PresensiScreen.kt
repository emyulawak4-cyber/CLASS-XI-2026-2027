package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttendanceStatus
import com.example.model.JAM_MASUK_NORMAL
import com.example.model.ResolvedStudentAttendance
import com.example.ui.components.ClassCard
import com.example.ui.components.PortalFeedback
import com.example.ui.components.StatusBadge
import com.example.ui.components.V9FooterText
import com.example.ui.components.V9PillChip
import com.example.ui.theme.AmberSakit
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
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.VioletDispen
import com.example.viewmodel.ClassPortalData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PresensiScreen(
    data: ClassPortalData,
    initialFilter: AttendanceStatus?,
    onSetDate: (String) -> Unit,
    onSetToday: () -> Unit,
    onUpdateStatus: (Int, AttendanceStatus) -> Unit,
    onSetAllHadir: () -> Unit,
    onOpenStudentQr: (Int) -> Unit,
    onOpenScanner: () -> Unit,
    onRequestAdminLogin: () -> Unit,
    onOpenIzin: () -> Unit = {},
    onConsumeInitialFilter: () -> Unit = {}
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val isGlass = LocalLiquidGlass.current

    var activeFilter by remember { mutableStateOf(initialFilter) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(initialFilter) {
        if (initialFilter != null) {
            activeFilter = initialFilter
            onConsumeInitialFilter()
        }
    }

    val attendanceList = data.attendanceForDate(data.selectedDate)
    val q = data.globalSearch.trim().lowercase()
    val filtered = attendanceList.filter { item ->
        val matchSearch = q.isBlank() ||
            item.student.name.lowercase().contains(q) ||
            item.student.noAbsen.contains(q) ||
            item.student.piket.lowercase().contains(q)
        val matchFilter = activeFilter == null || item.status == activeFilter
        matchSearch && matchFilter
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
                            onSetDate(formatted)
                        }
                        showDatePicker = false
                    }
                ) { Text("Pilih") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cols = when {
            maxWidth >= 840.dp -> 3
            maxWidth >= 560.dp -> 2
            else -> 1
        }
        val rows = remember(filtered, cols) { filtered.chunked(cols) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Controls Card matching HTML lines 1129-1173
            item {
                ClassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Tanggal Presensi",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { showDatePicker = true }
                                        .minimumInteractiveComponentSize()
                                        .testTag("presensi_pick_date_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CalendarToday,
                                            contentDescription = "Pilih Tanggal",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = data.selectedDate,
                                            fontFamily = JetBrainsMono,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable(onClick = onSetToday)
                                        .minimumInteractiveComponentSize()
                                        .testTag("presensi_today_btn")
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Hari Ini",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "≤ $JAM_MASUK_NORMAL WIB",
                            fontFamily = JetBrainsMono,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Surat Izin/Sakit cyan pill
                        val cyanBg = if (isDark) Color(0x2806B6D4) else Color(0xFFECFEFF)
                        val cyanBorder = if (isDark) Color(0x5022D3EE) else Color(0xFFA5F3FC)
                        val cyanFg = if (isDark) Color(0xFF67E8F9) else Color(0xFF155E75)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = cyanBg,
                            border = BorderStroke(1.dp, cyanBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onOpenIzin)
                                .minimumInteractiveComponentSize()
                                .testTag("presensi_open_izin_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MedicalServices,
                                    contentDescription = "Surat Izin/Sakit",
                                    tint = cyanFg,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Surat Izin/Sakit",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = cyanFg
                                )
                                if (data.pendingLeaveCount > 0) {
                                    Surface(
                                        shape = CircleShape,
                                        color = RoseAlpa
                                    ) {
                                        Text(
                                            text = data.pendingLeaveCount.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Scan QR Siswa blue tint pill
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isDark) PrimaryTintDark else PrimaryTintLight,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onOpenScanner)
                                .minimumInteractiveComponentSize()
                                .testTag("presensi_scan_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.QrCodeScanner,
                                    contentDescription = "Scan QR Siswa",
                                    tint = if (isDark) RoyalBlueBright else RoyalBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Scan QR Siswa",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) RoyalBlueBright else RoyalBlue
                                )
                            }
                        }

                        if (!data.isAdminMode) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable(onClick = onRequestAdminLogin)
                                    .minimumInteractiveComponentSize()
                                    .testTag("presensi_unlock_admin_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Visibility,
                                        contentDescription = "Mode Siswa: Hanya Lihat",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Mode Siswa: Hanya Lihat",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            // Hadir Semua
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable(onClick = onSetAllHadir)
                                    .minimumInteractiveComponentSize()
                                    .testTag("presensi_hadir_semua_btn")
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Hadir Semua",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Salin WA
                            val waBg = if (isDark) Color(0x2410B981) else Color(0xFFECFDF5)
                            val waFg = if (isDark) Color(0xFF6EE7B7) else EmeraldDark
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = waBg,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        val hadir = attendanceList.filter { it.status == AttendanceStatus.Hadir }
                                        val izin = attendanceList.filter { it.status == AttendanceStatus.Izin }
                                        val sakit = attendanceList.filter { it.status == AttendanceStatus.Sakit }
                                        val dispen = attendanceList.filter { it.status == AttendanceStatus.Dispensasi }
                                        val alpa = attendanceList.filter { it.status == AttendanceStatus.Alpa }

                                        val report = buildString {
                                            appendLine("*PRESENSI HARIAN CLASS XI*")
                                            appendLine("SMA Puspita Kota Tangerang")
                                            appendLine("Tanggal: ${data.selectedDate}")
                                            appendLine()
                                            appendLine("*Hadir:* ${hadir.size} / 47 Siswa")
                                            hadir.forEach { s ->
                                                val timeInfo = s.scanTime.ifBlank { "jam belum dicatat" }
                                                val lateTag = if (s.lateArrival) " (TERLAMBAT)" else ""
                                                appendLine("- ${s.student.noAbsen}. ${s.student.name}: $timeInfo$lateTag")
                                            }
                                            appendLine()
                                            appendLine("*Izin:* ${if (izin.isEmpty()) "Nihil" else izin.joinToString("\n") { "- ${it.student.noAbsen}. ${it.student.name}" }}")
                                            appendLine("*Sakit:* ${if (sakit.isEmpty()) "Nihil" else sakit.joinToString("\n") { "- ${it.student.noAbsen}. ${it.student.name}" }}")
                                            appendLine("*Dispensasi:* ${if (dispen.isEmpty()) "Nihil" else dispen.joinToString("\n") { "- ${it.student.noAbsen}. ${it.student.name}" }}")
                                            appendLine("*Alpa:* ${if (alpa.isEmpty()) "Nihil" else alpa.joinToString("\n") { "- ${it.student.noAbsen}. ${it.student.name}" }}")
                                        }
                                        PortalFeedback.copyToClipboard(context, "Laporan Presensi", report)
                                        PortalFeedback.shareText(context, "Presensi CLASS XI ${data.selectedDate}", report)
                                    }
                                    .minimumInteractiveComponentSize()
                                    .testTag("presensi_share_wa_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Share,
                                        contentDescription = "Salin WA",
                                        tint = waFg,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Salin WA",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = waFg
                                    )
                                }
                            }

                            // Simpan
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = RoyalBlue,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        PortalFeedback.playScanChime(context, true)
                                        Toast.makeText(
                                            context,
                                            "Data presensi tanggal ${data.selectedDate} tersimpan di Cloud",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .minimumInteractiveComponentSize()
                                    .testTag("presensi_save_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Simpan",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Simpan",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Pill Filter Row matching HTML lines 1175-1182
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    V9PillChip(
                        text = "Semua (${attendanceList.size})",
                        selected = activeFilter == null,
                        onClick = { activeFilter = null },
                        modifier = Modifier.testTag("presensi_filter_all")
                    )
                    AttendanceStatus.entries.forEach { st ->
                        val count = attendanceList.count { it.status == st }
                        V9PillChip(
                            text = if (count > 0) "${st.shortLabel} ($count)" else st.shortLabel,
                            selected = activeFilter == st,
                            onClick = { activeFilter = st },
                            modifier = Modifier.testTag("presensi_filter_${st.name.lowercase()}")
                        )
                    }
                }
            }

            // 3. Responsive 1/2/3-col Student Grid matching HTML line 1184 & renderStudentGrid()
            if (rows.isEmpty()) {
                item {
                    ClassCard {
                        Text(
                            text = "Tidak ada siswa sesuai filter atau pencarian.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(rows, key = { chunk -> chunk.first().student.id }) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowItems.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                PresensiStudentCard(
                                    item = item,
                                    isAdminMode = data.isAdminMode,
                                    isDark = isDark,
                                    isGlass = isGlass,
                                    onUpdateStatus = { st -> onUpdateStatus(item.student.id, st) },
                                    onOpenStudentQr = { onOpenStudentQr(item.student.id) },
                                    onRequestAdminLogin = onRequestAdminLogin
                                )
                            }
                        }
                        repeat(cols - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item { V9FooterText() }
        }
    }
}

@Composable
private fun PresensiStudentCard(
    item: ResolvedStudentAttendance,
    isAdminMode: Boolean,
    isDark: Boolean,
    isGlass: Boolean,
    onUpdateStatus: (AttendanceStatus) -> Unit,
    onOpenStudentQr: () -> Unit,
    onRequestAdminLogin: () -> Unit
) {
    ClassCard(contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onOpenStudentQr),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.student.noAbsen,
                        fontFamily = JetBrainsMono,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Piket ${item.student.piket}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.approvedLeaveType != null) {
                        StatusBadge(
                            text = "Ada Surat ${item.approvedLeaveType}",
                            containerColor = if (isDark) Color(0x260EA5E9) else Color(0xFFE0F2FE),
                            contentColor = if (isDark) Color(0xFF7DD3FC) else Color(0xFF0369A1),
                            cornerRadius = 50.dp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else if (item.scanTime.isNotBlank()) {
                        val textCol = if (item.lateArrival) AmberSakit else EmeraldKas
                        Text(
                            text = if (item.lateArrival) "Terlambat · ${item.scanTime} WIB" else "Datang pukul ${item.scanTime} WIB",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textCol
                        )
                    } else {
                        Text(
                            text = "Jam kedatangan belum tercatat",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                        )
                    }
                }
            }
            IconButton(
                onClick = onOpenStudentQr,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("student_qr_btn_${item.student.id}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.QrCode,
                    contentDescription = "Lihat QR ${item.student.name}",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        val trackBg = when {
            isGlass && isDark -> Color.White.copy(alpha = 0.08f)
            isGlass -> Color.White.copy(alpha = 0.45f)
            else -> MaterialTheme.colorScheme.surfaceVariant
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(trackBg)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AttendanceStatus.entries.forEach { st ->
                val active = item.status == st
                val activeBg = when (st) {
                    AttendanceStatus.Hadir -> RoyalBlue
                    AttendanceStatus.Izin -> SkyBlue
                    AttendanceStatus.Sakit -> AmberSakit
                    AttendanceStatus.Dispensasi -> VioletDispen
                    AttendanceStatus.Alpa -> RoseAlpa
                }
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = if (active) activeBg else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .clickable {
                            if (isAdminMode) onUpdateStatus(st) else onRequestAdminLogin()
                        }
                        .testTag("att_${item.student.id}_${st.name.lowercase()}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = st.shortLabel,
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                            color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

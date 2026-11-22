package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AssignmentInd
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AttendanceStatus
import com.example.model.ClassConstants
import com.example.ui.components.ClassCard
import com.example.ui.components.formatRupiah
import com.example.ui.theme.AmberSakit
import com.example.ui.theme.DividerDark
import com.example.ui.theme.DividerLight
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldKas
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.PrimaryTintDark
import com.example.ui.theme.PrimaryTintLight
import com.example.ui.theme.RoseAlpa
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueBright
import com.example.ui.theme.RoyalBlueMid
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.VioletDispen
import com.example.viewmodel.ClassPortalData

private data class QuickMenuItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val badgeCount: Int = 0,
    val route: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    data: ClassPortalData,
    onNavigate: (String) -> Unit,
    onOpenStudentQr: (Int) -> Unit,
    onOpenScanner: () -> Unit,
    onFilterPresensi: (AttendanceStatus) -> Unit
) {
    val isGlass = LocalLiquidGlass.current
    val isDark = LocalIsDarkTheme.current
    val dividerColor = when {
        isGlass && isDark -> Color.White.copy(alpha = 0.10f)
        isGlass -> Color(0x2E64748B)
        isDark -> DividerDark
        else -> DividerLight
    }
    val trackColor = when {
        isGlass && isDark -> Color.White.copy(alpha = 0.10f)
        isGlass -> Color(0x2E64748B)
        isDark -> DividerDark
        else -> DividerLight
    }

    val attendanceList = data.attendanceForDate(data.selectedDate)
    val total = attendanceList.size.coerceAtLeast(47)
    val hadirCount = attendanceList.count { it.status == AttendanceStatus.Hadir }
    val izinCount = attendanceList.count { it.status == AttendanceStatus.Izin }
    val sakitCount = attendanceList.count { it.status == AttendanceStatus.Sakit }
    val dispenCount = attendanceList.count { it.status == AttendanceStatus.Dispensasi }
    val alpaCount = attendanceList.count { it.status == AttendanceStatus.Alpa }
    val hadirPct = Math.round(((hadirCount + dispenCount).toFloat() / total) * 100f)

    val kasSummary = data.kasSummaryForPeriod(data.selectedKasPeriod)
    val todayDay = data.selectedScheduleDay
    val todaySubjects = ClassConstants.SCHEDULE[todayDay].orEmpty()
    val todayPicket = ClassConstants.STUDENTS.filter { it.piket == todayDay.key }

    // Exact 10 Menu Items matching KELAS_XI_perbaikan_v9 lines 863-872
    val quickMenuItems = listOf(
        QuickMenuItem("presensi", "Presensi", Icons.Rounded.AssignmentInd, 0, "presensi"),
        QuickMenuItem("kas", "Uang Kas", Icons.Rounded.AccountBalanceWallet, 0, "kas"),
        QuickMenuItem("dok", "Dokumentasi", Icons.Rounded.PhotoLibrary, 0, "dokumentasi"),
        QuickMenuItem("izin", "Izin/Sakit", Icons.Rounded.MedicalServices, data.pendingLeaveCount, "izin"),
        QuickMenuItem("mapel", "Mapel", Icons.Rounded.Book, 0, "jadwal"),
        QuickMenuItem("piket", "Piket", Icons.Rounded.CleaningServices, 0, "jadwal"),
        QuickMenuItem("qr", "Kartu QR", Icons.Rounded.Badge, 0, "qr"),
        QuickMenuItem("rekap", "Rekap Guru", Icons.Rounded.PieChart, 0, "rekap"),
        QuickMenuItem("info", "Info Kelas", Icons.Rounded.Campaign, 0, "info"),
        QuickMenuItem("materi", "Bank Materi", Icons.Rounded.Bookmarks, 0, "materi")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Ringkasan kehadiran (.att-card)
        item {
            ClassCard(contentPadding = PaddingValues(0.dp)) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kehadiran hari ini",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (data.isAdminMode) {
                                if (isDark) Color(0x2210B981) else Color(0xFFECFDF5)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        ) {
                            Text(
                                text = if (data.isAdminMode) "Mode ${data.activePengurusRole}" else "Mode Siswa (Read-Only)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (data.isAdminMode) {
                                    if (isDark) Color(0xFF6EE7B7) else EmeraldDark
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$hadirPct%",
                            style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "hadir atau dispensasi dari 47 siswa",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF7C8BA3) else Color(0xFF94A3B8),
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    // 8.dp 5-segment progress bar (.att-bar)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(trackColor)
                    ) {
                        listOf(
                            hadirCount to RoyalBlue,
                            izinCount to SkyBlue,
                            sakitCount to AmberSakit,
                            dispenCount to VioletDispen,
                            alpaCount to RoseAlpa
                        ).forEach { (count, col) ->
                            if (count > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(count.toFloat())
                                        .fillMaxHeight()
                                        .background(col)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                HorizontalDivider(thickness = 1.dp, color = dividerColor)

                // 5-column divider grid (.att-cols)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(androidx.compose.foundation.layout.IntrinsicSize.Min)
                ) {
                    val cols = listOf(
                        Triple(AttendanceStatus.Hadir, hadirCount, RoyalBlue),
                        Triple(AttendanceStatus.Izin, izinCount, SkyBlue),
                        Triple(AttendanceStatus.Sakit, sakitCount, AmberSakit),
                        Triple(AttendanceStatus.Dispensasi, dispenCount, VioletDispen),
                        Triple(AttendanceStatus.Alpa, alpaCount, RoseAlpa)
                    )
                    cols.forEachIndexed { index, (st, count, dotColor) ->
                        if (index > 0) {
                            VerticalDivider(
                                thickness = 1.dp,
                                color = dividerColor,
                                modifier = Modifier.fillMaxHeight()
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onFilterPresensi(st) }
                                .padding(vertical = 12.dp, horizontal = 2.dp)
                                .testTag("home_stat_${st.name.lowercase()}")
                        ) {
                            Text(
                                text = count.toString(),
                                style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                                Text(
                                    text = st.shortLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Saldo Kas Hero Banner (.kas-hero-gradient: Royal Blue)
        item {
            val heroBrush = when {
                isGlass && isDark -> Brush.linearGradient(
                    listOf(Color(0xD11864F0), Color(0xDB083296))
                )
                isGlass -> Brush.linearGradient(
                    listOf(Color(0xEB187DFF), Color(0xE60050D2))
                )
                isDark -> Brush.linearGradient(
                    listOf(Color(0xFF0A4FC4), Color(0xFF073A94))
                )
                else -> Brush.linearGradient(
                    listOf(RoyalBlue, RoyalBlue)
                )
            }
            val heroShape = RoundedCornerShape(16.dp)
            Surface(
                shape = heroShape,
                color = Color.Transparent,
                border = if (isGlass) BorderStroke(1.dp, Color.White.copy(alpha = if (isDark) 0.2f else 0.35f)) else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(heroShape)
                    .background(heroBrush)
                    .clickable { onNavigate("kas") }
                    .testTag("home_kas_hero_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // .kas-ico (44dp rounded 13dp translucent box)
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Color.White.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountBalanceWallet,
                                contentDescription = "Saldo Kas",
                                tint = Color.White,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Saldo uang kas",
                                    color = Color(0xFFDBEAFE),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = data.selectedKasPeriod,
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = formatRupiah(kasSummary.saldo),
                                color = Color.White,
                                style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                                fontWeight = FontWeight.Bold,
                                fontSize = 23.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${kasSummary.paidCount} / 47 Siswa Lunas",
                            color = Color.White,
                            style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Lihat detail",
                                color = Color(0xFFDBEAFE),
                                fontSize = 11.sp
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFFDBEAFE),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Menu Grid Card (5 cols x 2 rows = 10 items, matching v9 lines 860-874)
        item {
            val icoBg = when {
                isGlass && isDark -> Color.White.copy(alpha = 0.10f)
                isGlass -> Color(0x1F0062FF)
                isDark -> PrimaryTintDark
                else -> PrimaryTintLight
            }
            val icoTint = when {
                isGlass && isDark -> Color(0xFF8DBBFF)
                isDark -> Color(0xFF6AA6FF)
                else -> RoyalBlue
            }

            ClassCard {
                Text(
                    text = "Menu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    quickMenuItems.chunked(5).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            rowItems.forEach { item ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onNavigate(item.route) }
                                        .padding(vertical = 3.dp, horizontal = 2.dp)
                                        .testTag("quick_menu_${item.id}")
                                ) {
                                    Box {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(13.dp))
                                                .background(icoBg)
                                                .then(
                                                    if (isGlass) {
                                                        Modifier.border(
                                                            1.dp,
                                                            Color.White.copy(alpha = if (isDark) 0.15f else 0.72f),
                                                            RoundedCornerShape(13.dp)
                                                        )
                                                    } else Modifier
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title,
                                                tint = icoTint,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        if (item.badgeCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = 5.dp, y = (-4).dp)
                                                    .clip(CircleShape)
                                                    .background(RoseAlpa)
                                                    .border(
                                                        2.dp,
                                                        if (isDark) Color(0xFF131C2E) else Color.White,
                                                        CircleShape
                                                    )
                                                    .padding(horizontal = 5.dp, vertical = 1.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = item.badgeCount.toString(),
                                                    color = Color.White,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(7.dp))
                                    Text(
                                        text = item.title,
                                        fontSize = 10.5.sp,
                                        lineHeight = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Today's Highlight: Jadwal Pelajaran Hari Ini
        item {
            ClassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jadwal Pelajaran Hari Ini (${todayDay.label})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Lihat Semua",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigate("jadwal") }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    todaySubjects.forEach { sub ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onNavigate("jadwal") }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${sub.no}. ${sub.name}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = sub.time,
                                    style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Today's Highlight: Petugas Piket Hari Ini
        item {
            ClassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Petugas Piket Hari Ini",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = CircleShape,
                        color = if (isDark) Color(0x2210B981) else Color(0xFFECFDF5)
                    ) {
                        Text(
                            text = "${todayPicket.size} Siswa",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF6EE7B7) else EmeraldDark,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    todayPicket.forEach { st ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onOpenStudentQr(st.id) }
                        ) {
                            Text(
                                text = "${st.noAbsen}. ${st.name}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // Footer matching v9 lines 1548-1550
        item {
            Text(
                text = "CLASS XI — SMA Puspita Kota Tangerang • Yayasan Pendidikan Puspitasari",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp)
            )
        }
    }
}

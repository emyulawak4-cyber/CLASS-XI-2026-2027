package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NoteAlt
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.ClassConstants
import com.example.model.GOOGLE_FORM_IZIN_URL
import com.example.model.LeaveRequest
import com.example.ui.components.ClassCard
import com.example.ui.components.PortalFeedback
import com.example.ui.components.StatusBadge
import com.example.ui.components.V9DropdownButton
import com.example.ui.components.V9Field
import com.example.ui.components.V9FooterText
import com.example.ui.components.V9PillChip
import com.example.ui.components.V9SegmentedControl
import com.example.ui.theme.AmberSakit
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldKas
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.PrimaryTintDark
import com.example.ui.theme.PrimaryTintLight
import com.example.ui.theme.RoseAlpa
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SkyBlue
import com.example.viewmodel.ClassPortalData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IzinScreen(
    data: ClassPortalData,
    currentUid: String,
    onRequestAdminLogin: () -> Unit,
    onSubmitLeave: (studentId: Int, date: String, type: String, reason: String, gformConfirmed: Boolean, onDone: () -> Unit) -> Unit,
    onVerifyLeave: (LeaveRequest, Boolean) -> Unit,
    onDeleteLeave: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    fun offsetDate(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return fmt.format(cal.time)
    }

    var leaveType by remember { mutableStateOf("Sakit") }
    var selectedStudentId by remember { mutableIntStateOf(ClassConstants.STUDENTS.first().id) }
    var studentQuery by remember { mutableStateOf("") }
    var showStudentDropdown by remember { mutableStateOf(false) }
    var dateInput by remember { mutableStateOf(offsetDate(0)) }
    var reason by remember { mutableStateOf("") }
    var gformConfirmed by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    var statusFilter by remember { mutableStateOf("Semua") }
    var historySearch by remember { mutableStateOf("") }

    val pendingCount = data.leaveRequests.count { it.status == "Menunggu" }
    val approvedCount = data.leaveRequests.count { it.status == "Disetujui" }
    val rejectedCount = data.leaveRequests.count { it.status == "Ditolak" }

    val selectedStudent = remember(selectedStudentId) {
        ClassConstants.STUDENTS.firstOrNull { it.id == selectedStudentId } ?: ClassConstants.STUDENTS.first()
    }
    val matchingStudents = remember(studentQuery) {
        val sq = studentQuery.trim().lowercase()
        if (sq.isBlank()) ClassConstants.STUDENTS
        else ClassConstants.STUDENTS.filter {
            it.name.lowercase().contains(sq) || it.noAbsen.contains(sq)
        }
    }

    val globalQ = data.globalSearch.trim().lowercase()
    val histQ = historySearch.trim().lowercase()
    val filteredLeaves = remember(data.leaveRequests, statusFilter, globalQ, histQ) {
        data.leaveRequests.filter { item ->
            val matchStatus = statusFilter == "Semua" || item.status == statusFilter
            val matchGlobal = globalQ.isBlank() ||
                item.studentName.lowercase().contains(globalQ) ||
                item.reason.lowercase().contains(globalQ) ||
                item.noAbsen.contains(globalQ)
            val matchLocal = histQ.isBlank() ||
                item.studentName.lowercase().contains(histQ) ||
                item.reason.lowercase().contains(histQ) ||
                item.noAbsen.contains(histQ)
            matchStatus && matchGlobal && matchLocal
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val isSplit = screenWidth >= 840.dp
        val isTopRow = screenWidth >= 680.dp

        val formCard: @Composable () -> Unit = {
            ClassCard(contentPadding = PaddingValues(18.dp)) {
                // Step 1 matching HTML lines 1207-1217
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StepNoBadge("1", isDark)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Unggah bukti di Google Forms",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Foto surat dokter atau surat orang tua.",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { PortalFeedback.openUrl(context, GOOGLE_FORM_IZIN_URL) },
                                shape = RoundedCornerShape(11.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("izin_open_gform")
                            ) {
                                Icon(Icons.Rounded.OpenInNew, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buka Forms", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                            OutlinedButton(
                                onClick = {
                                    PortalFeedback.copyToClipboard(context, "Google Forms Izin CLASS XI", GOOGLE_FORM_IZIN_URL)
                                    onShowMessage("Link Google Forms berhasil disalin!")
                                },
                                shape = RoundedCornerShape(11.dp),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("izin_copy_gform")
                            ) {
                                Icon(Icons.Rounded.ContentCopy, contentDescription = "Salin link Google Forms", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Salin link", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f))
                Spacer(modifier = Modifier.height(16.dp))

                // Step 2 matching HTML lines 1222-1262
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StepNoBadge("2", isDark)
                    Text(
                        text = "Isi pengajuan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Jenis",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("Sakit", AmberSakit, Icons.Rounded.LocalHospital),
                        Triple("Izin", SkyBlue, Icons.Rounded.NoteAlt)
                    ).forEach { (typeOption, accent, typeIcon) ->
                        val active = leaveType == typeOption
                        Surface(
                            shape = RoundedCornerShape(11.dp),
                            color = if (active) accent.copy(alpha = if (isDark) 0.22f else 0.10f) else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.5.dp, if (active) accent else MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(11.dp))
                                .clickable { leaveType = typeOption }
                                .minimumInteractiveComponentSize()
                                .testTag("izin_type_${typeOption.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = typeIcon,
                                    contentDescription = typeOption,
                                    tint = if (active) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = typeOption,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = if (active) accent else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Nama siswa",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                V9Field(
                    value = studentQuery,
                    onValueChange = {
                        studentQuery = it
                        matchingStudents.firstOrNull()?.let { firstMatch ->
                            selectedStudentId = firstMatch.id
                        }
                    },
                    placeholder = "Ketik nama atau nomor absen",
                    leadingIcon = Icons.Rounded.Search
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    V9DropdownButton(
                        text = "${selectedStudent.noAbsen}. ${selectedStudent.name} (Piket ${selectedStudent.piket})",
                        onClick = { showStudentDropdown = true },
                        roundedPill = false,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("izin_student_selector")
                    )
                    DropdownMenu(
                        expanded = showStudentDropdown,
                        onDismissRequest = { showStudentDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.84f)
                    ) {
                        matchingStudents.take(20).forEach { st ->
                            DropdownMenuItem(
                                text = { Text("${st.noAbsen}. ${st.name}") },
                                onClick = {
                                    selectedStudentId = st.id
                                    showStudentDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tanggal",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                V9Field(
                    value = dateInput,
                    onValueChange = { dateInput = it.take(10) },
                    placeholder = "YYYY-MM-DD",
                    modifier = Modifier.testTag("izin_date_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Kemarin" to -1, "Hari Ini" to 0, "Besok" to 1).forEach { (lbl, offset) ->
                        val target = offsetDate(offset)
                        V9PillChip(
                            text = lbl,
                            selected = dateInput == target,
                            onClick = { dateInput = target }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alasan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${reason.length}/200",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                V9Field(
                    value = reason,
                    onValueChange = { reason = it.take(200) },
                    placeholder = "Tulis alasan singkat atau pilih di bawah",
                    singleLine = false,
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.testTag("izin_reason_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ClassConstants.LEAVE_REASONS[leaveType].orEmpty().forEach { template ->
                        V9PillChip(
                            text = template,
                            selected = reason == template,
                            onClick = { reason = template }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { gformConfirmed = !gformConfirmed }
                ) {
                    Checkbox(
                        checked = gformConfirmed,
                        onCheckedChange = { gformConfirmed = it },
                        modifier = Modifier.testTag("izin_gform_checkbox")
                    )
                    Text(
                        text = "Bukti sudah saya unggah di Google Forms (opsional, bisa menyusul)",
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (formError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(formError!!, color = RoseAlpa, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        when {
                            dateInput.trim().length != 10 -> formError = "Format tanggal harus YYYY-MM-DD."
                            reason.trim().length < 4 -> formError = "Tuliskan alasan izin minimal 4 karakter."
                            else -> {
                                formError = null
                                onSubmitLeave(
                                    selectedStudentId,
                                    dateInput.trim(),
                                    leaveType,
                                    reason.trim(),
                                    gformConfirmed
                                ) {
                                    reason = ""
                                    gformConfirmed = false
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(11.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("izin_submit_button")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Kirim pengajuan", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        val historyCard: @Composable () -> Unit = {
            ClassCard(contentPadding = PaddingValues(18.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Riwayat pengajuan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            StatusBadge(
                                text = "$pendingCount Menunggu",
                                containerColor = if (isDark) Color(0x28F59E0B) else Color(0xFFFFFBEB),
                                contentColor = if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309),
                                cornerRadius = 8.dp
                            )
                        }
                    }

                    V9SegmentedControl(
                        options = listOf(
                            "Semua" to "Semua",
                            "Menunggu" to "Menunggu",
                            "Disetujui" to "Disetujui",
                            "Ditolak" to "Ditolak"
                        ),
                        selectedKey = statusFilter,
                        onSelect = { statusFilter = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    V9Field(
                        value = historySearch,
                        onValueChange = { historySearch = it },
                        placeholder = "Cari nama siswa di riwayat",
                        leadingIcon = Icons.Rounded.Search
                    )

                    // #leaveAdminModeBar matching HTML line 1278
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = if (data.isAdminMode) EmeraldKas else RoyalBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (data.isAdminMode) {
                                        "Mode ${data.activePengurusRole}: Anda dapat menyetujui atau menolak surat."
                                    } else {
                                        "Masuk Mode Pengurus untuk memverifikasi surat & otomatis memperbarui presensi."
                                    },
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!data.isAdminMode) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = RoyalBlue,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(onClick = onRequestAdminLogin)
                                ) {
                                    Text(
                                        text = "Login",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (filteredLeaves.isEmpty()) {
                        Text(
                            text = "Belum ada riwayat surat izin pada filter ini.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.5.sp,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            filteredLeaves.forEach { leave ->
                                LeaveHistoryItem(
                                    leave = leave,
                                    isAdminMode = data.isAdminMode,
                                    isDark = isDark,
                                    canDelete = leave.userId == currentUid || data.isAdminMode,
                                    onApprove = { onVerifyLeave(leave, true) },
                                    onReject = { onVerifyLeave(leave, false) },
                                    onRequestAdminLogin = onRequestAdminLogin,
                                    onDelete = { onDeleteLeave(leave.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Title + 3 Clickable Stat Cards matching HTML lines 1191-1201
            item {
                val statsRow: @Composable (Modifier) -> Unit = { mod ->
                    Row(
                        modifier = mod,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LeaveStatTile("Menunggu", pendingCount, AmberSakit, Modifier.weight(1f)) {
                            statusFilter = "Menunggu"
                        }
                        LeaveStatTile("Disetujui", approvedCount, EmeraldKas, Modifier.weight(1f)) {
                            statusFilter = "Disetujui"
                        }
                        LeaveStatTile("Ditolak", rejectedCount, RoseAlpa, Modifier.weight(1f)) {
                            statusFilter = "Ditolak"
                        }
                    }
                }

                if (isTopRow) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                            Text("Surat Izin & Sakit", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "Ajukan keterangan tidak hadir. Pengurus akan memverifikasi dan presensi terisi otomatis.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        statsRow(Modifier.width(340.dp))
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column {
                            Text("Surat Izin & Sakit", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "Ajukan keterangan tidak hadir. Pengurus akan memverifikasi dan presensi terisi otomatis.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        statsRow(Modifier.fillMaxWidth())
                    }
                }
            }

            // 5:7 split on expanded screens, stacked on phones
            item {
                if (isSplit) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(modifier = Modifier.weight(5f)) { formCard() }
                        Box(modifier = Modifier.weight(7f)) { historyCard() }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        formCard()
                        historyCard()
                    }
                }
            }

            item { V9FooterText() }
        }
    }
}

@Composable
private fun StepNoBadge(num: String, isDark: Boolean) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(if (isDark) PrimaryTintDark else PrimaryTintLight),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = num,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun LeaveStatTile(
    label: String,
    count: Int,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ClassCard(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(
            text = count.toString(),
            fontFamily = JetBrainsMono,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = accent
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LeaveHistoryItem(
    leave: LeaveRequest,
    isAdminMode: Boolean,
    isDark: Boolean,
    canDelete: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onRequestAdminLogin: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (leave.status) {
        "Disetujui" -> EmeraldKas
        "Ditolak" -> RoseAlpa
        else -> AmberSakit
    }
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusBadge(
                            text = leave.type,
                            containerColor = if (leave.type == "Sakit") {
                                if (isDark) Color(0x28F59E0B) else Color(0xFFFEF3C7)
                            } else {
                                if (isDark) Color(0x280EA5E9) else Color(0xFFE0F2FE)
                            },
                            contentColor = if (leave.type == "Sakit") {
                                if (isDark) Color(0xFFFCD34D) else Color(0xFFB45309)
                            } else {
                                if (isDark) Color(0xFF7DD3FC) else SkyBlue
                            }
                        )
                        StatusBadge(
                            text = leave.status,
                            containerColor = statusColor.copy(alpha = 0.14f),
                            contentColor = statusColor
                        )
                        if (leave.gformConfirmed) {
                            StatusBadge(
                                text = "Forms ✓",
                                containerColor = if (isDark) Color(0x2810B981) else Color(0xFFECFDF5),
                                contentColor = if (isDark) Color(0xFF6EE7B7) else EmeraldDark
                            )
                        }
                    }
                    Text(
                        text = "${leave.noAbsen}. ${leave.studentName}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tanggal: ${leave.date} • Piket ${leave.piket}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("izin_delete_${leave.id}")
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, contentDescription = "Hapus riwayat surat", tint = RoseAlpa)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(text = leave.reason, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurface)
            if (leave.verifiedBy.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Diverifikasi oleh ${leave.verifiedBy}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (leave.status == "Menunggu") {
                Spacer(modifier = Modifier.height(10.dp))
                if (isAdminMode) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = onApprove,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldKas),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("izin_approve_${leave.id}")
                        ) {
                            Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Setujui", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("izin_reject_${leave.id}")
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = null, tint = RoseAlpa, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tolak", color = RoseAlpa, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onRequestAdminLogin,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("izin_unlock_admin_${leave.id}")
                    ) {
                        Text("Masuk Mode Pengurus untuk Verifikasi", color = RoyalBlue, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

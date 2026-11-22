package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ThemeMode
import com.example.data.ThemeStyle
import com.example.ui.components.ClassCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BgDark
import com.example.ui.theme.BgLight
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.OutlineLight
import com.example.ui.theme.PrimaryTintDark
import com.example.ui.theme.PrimaryTintLight
import com.example.ui.theme.RoseAlpa
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.SurfaceDark
import com.example.viewmodel.ClassPortalData
import com.google.firebase.auth.FirebaseUser

@Composable
fun SettingsScreen(
    data: ClassPortalData,
    currentUser: FirebaseUser?,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetThemeStyle: (ThemeStyle) -> Unit,
    onSignOut: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val isGlass = LocalLiquidGlass.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Settings Header Card matching v9 lines 1502-1508
        item {
            ClassCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(if (isDark) PrimaryTintDark else PrimaryTintLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Pengaturan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Atur tampilan aplikasi sesuai selera Anda",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Mode Tampilan (Terang / Gelap / Otomatis) matching v9 lines 1510-1520
        item {
            ClassCard {
                Text(
                    text = "Mode Tampilan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Pilih terang, gelap, atau ikuti pengaturan perangkat.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ModeOptionTile(
                        label = "Terang",
                        sublabel = null,
                        icon = Icons.Rounded.LightMode,
                        selected = data.preferences.themeMode == ThemeMode.LIGHT,
                        onClick = { onSetThemeMode(ThemeMode.LIGHT) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_theme_light")
                    )
                    ModeOptionTile(
                        label = "Gelap",
                        sublabel = null,
                        icon = Icons.Rounded.DarkMode,
                        selected = data.preferences.themeMode == ThemeMode.DARK,
                        onClick = { onSetThemeMode(ThemeMode.DARK) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_theme_dark")
                    )
                    ModeOptionTile(
                        label = "Otomatis",
                        sublabel = "ikuti perangkat",
                        icon = Icons.Rounded.Contrast,
                        selected = data.preferences.themeMode == ThemeMode.AUTO,
                        onClick = { onSetThemeMode(ThemeMode.AUTO) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_theme_auto")
                    )
                }
            }
        }

        // 3. Tema Visual (Default / Liquid Glass Preview Cards) matching v9 lines 1522-1542
        item {
            ClassCard {
                Text(
                    text = "Tema",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Gaya tampilan seluruh aplikasi.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemePreviewOptionCard(
                        title = "Default",
                        description = "Datar, rapi, dan ringan.",
                        isGlassPreview = false,
                        isDark = isDark,
                        selected = data.preferences.themeStyle == ThemeStyle.DEFAULT,
                        onClick = { onSetThemeStyle(ThemeStyle.DEFAULT) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_style_default")
                    )
                    ThemePreviewOptionCard(
                        title = "Liquid Glass",
                        description = "Kaca transparan dengan efek blur.",
                        isGlassPreview = true,
                        isDark = isDark,
                        selected = data.preferences.themeStyle == ThemeStyle.GLASS,
                        onClick = { onSetThemeStyle(ThemeStyle.GLASS) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("settings_style_glass")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                val activeStyleText = if (isGlass) "Liquid Glass" else "Default"
                val activeModeText = if (isDark) "Mode Gelap" else "Mode Terang"
                val autoSuffix = if (data.preferences.themeMode == ThemeMode.AUTO) " (otomatis)" else ""
                Text(
                    text = "Aktif: $activeStyleText • $activeModeText$autoSuffix. Pilihan tersimpan di perangkat ini.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 4. Google Account Card (Clean v9 styling, Hak Akses & Notifikasi Real-time cards removed per user request)
        item {
            ClassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val photoUrl = currentUser?.photoUrl?.toString()
                    if (!photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Foto Profil Google",
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.displayName ?: "Pengguna CLASS XI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = currentUser?.email ?: "-",
                            fontSize = 11.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    StatusBadge(
                        text = "Terhubung",
                        containerColor = if (isDark) Color(0x2210B981) else Color(0xFFECFDF5),
                        contentColor = if (isDark) Color(0xFF6EE7B7) else EmeraldDark
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onSignOut,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseAlpa),
                    border = BorderStroke(1.dp, RoseAlpa.copy(alpha = 0.38f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("settings_sign_out")
                ) {
                    Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keluar Akun Google", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ModeOptionTile(
    label: String,
    sublabel: String?,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val isGlass = LocalLiquidGlass.current
    val bg = when {
        selected && isDark -> Color(0x330062FF)
        selected && isGlass -> Color(0x1F0062FF)
        selected -> PrimaryTintLight
        isGlass && isDark -> Color.White.copy(alpha = 0.06f)
        isGlass -> Color.White.copy(alpha = 0.36f)
        isDark -> Color(0xFF182238)
        else -> Color.White
    }
    val borderColor = when {
        selected && isDark -> Color(0xFF4D94FF)
        selected -> RoyalBlue
        isGlass -> Color.White.copy(alpha = if (isDark) 0.16f else 0.78f)
        isDark -> Color(0xFF2B3A57)
        else -> Color(0xFFE2E8F0)
    }
    val contentColor = when {
        selected && isDark -> Color(0xFF8DBBFF)
        selected -> RoyalBlue
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            if (sublabel != null) {
                Text(
                    text = sublabel,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ThemePreviewOptionCard(
    title: String,
    description: String,
    isGlassPreview: Boolean,
    isDark: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGlass = LocalLiquidGlass.current
    val cardBg = when {
        selected && isDark -> Color(0x330062FF)
        selected && isGlass -> Color(0x1F0062FF)
        selected -> Color(0xFFF3F8FF)
        isGlass && isDark -> Color.White.copy(alpha = 0.06f)
        isGlass -> Color.White.copy(alpha = 0.36f)
        isDark -> Color(0xFF182238)
        else -> Color.White
    }
    val borderColor = when {
        selected && isDark -> Color(0xFF4D94FF)
        selected -> RoyalBlue
        isGlass -> Color.White.copy(alpha = if (isDark) 0.16f else 0.78f)
        isDark -> Color(0xFF2B3A57)
        else -> Color(0xFFE2E8F0)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(1.5.dp, borderColor),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Mini Visual Mockup (.tp-default or .tp-glass, 86.dp height)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                        if (isGlassPreview) {
                            if (isDark) Color(0xFF060B18) else Color(0xFFE4EEFF)
                        } else {
                            if (isDark) BgDark else BgLight
                        }
                    )
            ) {
                if (isGlassPreview) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color(if (isDark) 0x992563EB else 0x8C007AFF), Color.Transparent),
                                center = Offset(size.width * 0.14f, size.height * 0.14f),
                                radius = size.width * 0.65f
                            ),
                            center = Offset(size.width * 0.14f, size.height * 0.14f),
                            radius = size.width * 0.65f
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color(if (isDark) 0x807C3AED else 0x738B5CF6), Color.Transparent),
                                center = Offset(size.width * 0.92f, size.height * 0.44f),
                                radius = size.width * 0.60f
                            ),
                            center = Offset(size.width * 0.92f, size.height * 0.44f),
                            radius = size.width * 0.60f
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color(if (isDark) 0x800891B2 else 0x8006B6D4), Color.Transparent),
                                center = Offset(size.width * 0.42f, size.height * 0.98f),
                                radius = size.width * 0.60f
                            ),
                            center = Offset(size.width * 0.42f, size.height * 0.98f),
                            radius = size.width * 0.60f
                        )
                    }
                }

                // Top miniature bar (.tp-bar)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .background(
                            if (isGlassPreview) {
                                Brush.linearGradient(listOf(Color(0xEB1276FF), Color(0xE0004CCC)))
                            } else {
                                Brush.linearGradient(listOf(RoyalBlue, RoyalBlue))
                            }
                        )
                )

                // Checkmark badge (.tick)
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(RoyalBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Dipilih",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // 2 mini cards (.tp-c.l, .tp-c.r)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp, start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(
                                    when {
                                        isGlassPreview && isDark -> Color.White.copy(alpha = 0.12f)
                                        isGlassPreview -> Color.White.copy(alpha = 0.55f)
                                        isDark -> SurfaceDark
                                        else -> Color.White
                                    }
                                )
                                .border(
                                    1.dp,
                                    when {
                                        isGlassPreview && isDark -> Color.White.copy(alpha = 0.22f)
                                        isGlassPreview -> Color.White.copy(alpha = 0.85f)
                                        isDark -> OutlineDark
                                        else -> OutlineLight
                                    },
                                    RoundedCornerShape(9.dp)
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(9.dp))
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 10.5.sp,
                lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2
            )
        }
    }
}

@Composable
fun AdminPinDialog(
    onDismiss: () -> Unit,
    onVerify: (String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var wrongPin by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Masuk Mode Pengurus", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Masukkan 4 digit PIN Pengurus (Ketua, Sekretaris, Bendahara, atau Wali Kelas) untuk mengedit data kelas.",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it.filter(Char::isDigit).take(6)
                        wrongPin = false
                    },
                    label = { Text("PIN Pengurus") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = wrongPin,
                    supportingText = if (wrongPin) {
                        { Text("PIN tidak sesuai dengan daftar pengurus.") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pin_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (onVerify(pin)) onDismiss() else wrongPin = true
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("admin_pin_submit")
            ) {
                Text("Verifikasi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun GuruPinDialog(
    onDismiss: () -> Unit,
    onVerify: (String) -> Boolean
) {
    var pin by remember { mutableStateOf("") }
    var wrongPin by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Masuk Mode Guru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Masukkan PIN Guru / Wali Kelas untuk mengunggah modul ke Bank Materi CLASS XI.",
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it.filter(Char::isDigit).take(6)
                        wrongPin = false
                    },
                    label = { Text("PIN Guru") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    isError = wrongPin,
                    supportingText = if (wrongPin) {
                        { Text("PIN guru tidak sesuai.") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("guru_pin_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (onVerify(pin)) onDismiss() else wrongPin = true
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("guru_pin_submit")
            ) {
                Text("Buka Mode Guru")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                Text("Batal")
            }
        }
    )
}

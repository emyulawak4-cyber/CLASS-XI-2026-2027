package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.AuthManager
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueDark

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF0B6BFF), RoyalBlue, Color(0xFF0047C2))
                )
            )
            .safeDrawingPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                modifier = Modifier.size(96.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_puspita),
                    contentDescription = "Logo Yayasan Pendidikan Puspitasari",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "CLASS XI",
                    color = Color(0xFFDBEAFE),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Masuk ke Portal CLASS XI",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.school_name),
                    color = Color(0xFFDBEAFE),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.foundation_name),
                    color = Color(0xFFBFDBFE),
                    fontSize = 12.sp
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.14f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.NotificationsActive,
                            contentDescription = "Sinkronisasi Cloud CLASS XI",
                            tint = Color.White
                        )
                        Text(
                            text = "Sinkronisasi Cloud & Notifikasi Kelas",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = "Masuk dengan akun Google agar data presensi, uang kas, surat izin, galeri dokumentasi, dan pengumuman langsung tersinkron.",
                        color = Color(0xFFEFF6FF),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = "Keamanan Akun",
                            tint = Color(0xFF86EFAC),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "47 Siswa Terdata • Mode Siswa & Pengurus",
                            color = Color(0xFFD1FAE5),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFB91C1C),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    AuthManager.onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isLoading = false
                            onAuthSuccess()
                        },
                        onAuthError = { msg ->
                            isLoading = false
                            errorMessage = msg
                        },
                        onAuthCancelled = {
                            isLoading = false
                        },
                        scope = scope
                    )
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = RoyalBlue
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_sign_in_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = RoyalBlue,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = stringResource(R.string.sign_in_google),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SplashWelcomeOverlay(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    val isGlass = LocalLiquidGlass.current
    val isDark = LocalIsDarkTheme.current
    val transition = rememberInfiniteTransition(label = "splashOrbs")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val bgColor = when {
            isGlass && isDark -> Color(0xFF070D1C)
            isDark -> Color(0xFF0A1020)
            else -> RoyalBlue
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
        ) {
            if (isGlass) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(if (isDark) 0x803B82F6 else 0xB37DD3FC),
                                Color.Transparent
                            ),
                            center = Offset(w * (0.15f + 0.14f * drift), h * (0.14f + 0.08f * drift)),
                            radius = w * 0.65f
                        ),
                        center = Offset(w * (0.15f + 0.14f * drift), h * (0.14f + 0.08f * drift)),
                        radius = w * 0.65f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(if (isDark) 0x808B5CF6 else 0xB3A5B4FC),
                                Color.Transparent
                            ),
                            center = Offset(w * (0.88f - 0.14f * drift), h * (0.45f - 0.08f * drift)),
                            radius = w * 0.70f
                        ),
                        center = Offset(w * (0.88f - 0.14f * drift), h * (0.45f - 0.08f * drift)),
                        radius = w * 0.70f
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(if (isDark) 0x8006B6D4 else 0xB35EEAD4),
                                Color.Transparent
                            ),
                            center = Offset(w * (0.35f - 0.10f * drift), h * (0.90f - 0.10f * drift)),
                            radius = w * 0.60f
                        ),
                        center = Offset(w * (0.35f - 0.10f * drift), h * (0.90f - 0.10f * drift)),
                        radius = w * 0.60f
                    )
                }
            } else if (isDark) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(RoyalBlue.copy(alpha = 0.38f), Color.Transparent),
                            center = Offset(size.width * 0.5f, size.height * 0.26f),
                            radius = size.width * 0.75f
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.26f),
                        radius = size.width * 0.75f
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 380.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Splash emblem with glowing orb behind
                    Box(
                        modifier = Modifier.size(190.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGlass) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = if (isDark) 0.26f else 0.32f),
                                                Color.White.copy(alpha = if (isDark) 0.08f else 0.12f)
                                            )
                                        )
                                    )
                                    .border(
                                        1.dp,
                                        Color.White.copy(alpha = if (isDark) 0.28f else 0.45f),
                                        CircleShape
                                    )
                            )
                        } else if (isDark) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.28f),
                                                Color.White.copy(alpha = 0.08f),
                                                Color.Transparent
                                            )
                                        ),
                                        CircleShape
                                    )
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.logo_splash_xi),
                            contentDescription = "Logo CLASS XI",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (isGlass) 14.dp else 4.dp)
                        )
                    }

                    // Text card (frosted glass card in Liquid Glass mode, clean open text in Default mode)
                    val textContent: @Composable () -> Unit = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "CLASS XI",
                                color = Color(0xFFDBEAFE),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 3.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Selamat Datang di Portal CLASS XI",
                                color = Color.White,
                                fontSize = 24.sp,
                                lineHeight = 30.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Sistem Manajemen Presensi 47 Siswa, Uang Kas Kelas, Dokumentasi Kegiatan, Surat Izin & Sakit, Jadwal Pelajaran & Piket SMA Puspita Kota Tangerang.",
                                color = Color(0xFFEFF6FF).copy(alpha = 0.92f),
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (isGlass) {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = Color.White.copy(alpha = if (isDark) 0.14f else 0.22f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = if (isDark) 0.22f else 0.38f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)) {
                                textContent()
                            }
                        }
                    } else {
                        textContent()
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 380.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val btnContainer = when {
                        isGlass -> Color.White.copy(alpha = if (isDark) 0.16f else 0.26f)
                        isDark -> RoyalBlue
                        else -> Color.White
                    }
                    val btnContent = when {
                        isGlass || isDark -> Color.White
                        else -> RoyalBlue
                    }
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = btnContainer,
                            contentColor = btnContent
                        ),
                        border = if (isGlass) BorderStroke(1.dp, Color.White.copy(alpha = 0.42f)) else null,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("dismiss_splash_button")
                    ) {
                        Text(
                            text = "LANJUTKAN KE CLASS XI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    val footMod = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    if (isGlass) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.30f))
                        ) {
                            Text(
                                text = "Yayasan Pendidikan Puspitasari • Kota Tangerang",
                                color = Color(0xFFEFF6FF),
                                fontSize = 11.sp,
                                modifier = footMod
                            )
                        }
                    } else {
                        Text(
                            text = "Yayasan Pendidikan Puspitasari • Kota Tangerang",
                            color = Color(0xFFEFF6FF).copy(alpha = 0.90f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

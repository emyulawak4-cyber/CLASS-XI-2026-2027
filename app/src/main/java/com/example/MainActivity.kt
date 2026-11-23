package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auth.AuthManager
import com.example.model.AttendanceStatus
import com.example.model.UiState
import com.example.notifications.ClassNotificationWorker
import com.example.notifications.NotificationHelper
import com.example.ui.components.GlassBackdropBox
import com.example.ui.screens.AdminPinDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DokumentasiScreen
import com.example.ui.screens.GuruPinDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InfoScreen
import com.example.ui.screens.IzinScreen
import com.example.ui.screens.JadwalScreen
import com.example.ui.screens.KasScreen
import com.example.ui.screens.MateriScreen
import com.example.ui.screens.PresensiScreen
import com.example.ui.screens.QrCardsScreen
import com.example.ui.screens.QrScannerDialog
import com.example.ui.screens.RekapScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashWelcomeOverlay
import com.example.ui.screens.StudentQrDialog
import com.example.ui.theme.AmberSakit
import com.example.ui.theme.ClassXiTheme
import com.example.ui.theme.DividerDark
import com.example.ui.theme.DividerLight
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldKas
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.NavBarDark
import com.example.ui.theme.OutlineDark
import com.example.ui.theme.OutlineLight
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueBright
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueDeep
import com.example.ui.theme.RoyalBlueMid
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextMutedLight
import com.example.viewmodel.ClassPortalData
import com.example.viewmodel.ClassViewModel
import com.example.viewmodel.ToastEvent
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable data object BerandaRoute
@Serializable data object PresensiRoute
@Serializable data object KasRoute
@Serializable data object SuratIzinRoute
@Serializable data object JadwalRoute
@Serializable data object QrRoute
@Serializable data object DokumentasiRoute
@Serializable data object InfoRoute
@Serializable data object MateriRoute
@Serializable data object RekapRoute
@Serializable data object SettingsRoute

private fun routeObjFromKey(key: String): Any = when (key.lowercase()) {
    "presensi" -> PresensiRoute
    "kas" -> KasRoute
    "izin", "surat_izin" -> SuratIzinRoute
    "jadwal" -> JadwalRoute
    "qr" -> QrRoute
    "dokumentasi" -> DokumentasiRoute
    "info" -> InfoRoute
    "materi" -> MateriRoute
    "rekap" -> RekapRoute
    "settings", "pengaturan" -> SettingsRoute
    else -> BerandaRoute
}

class MainActivity : ComponentActivity() {
    private var deepLinkTab by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.ensureChannel(this)
        ClassNotificationWorker.schedulePeriodicSync(this)
        deepLinkTab = intent?.getStringExtra(NotificationHelper.EXTRA_TARGET_TAB)

        setContent {
            val context = LocalContext.current
            val isFirebaseReady = remember {
                try {
                    FirebaseApp.getApps(context).isNotEmpty() || FirebaseApp.initializeApp(context) != null
                } catch (_: Exception) {
                    false
                }
            }

            if (!isFirebaseReady) {
                ClassXiTheme {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.linearGradient(listOf(RoyalBlueBright, RoyalBlue, RoyalBlueDark)))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(44.dp)
                                )
                                Text(
                                    text = "Konfigurasi Firebase Belum Terpasang",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "APK ini dibuat tanpa file google-services.json. Tambahkan secret GOOGLE_SERVICES_JSON di GitHub Repository Settings → Secrets and variables → Actions lalu build ulang APK.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                return@setContent
            }

            val scope = rememberCoroutineScope()
            val credentialManager = remember { CredentialManager.create(context) }
            var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }
            var checkingAutoSignIn by remember { mutableStateOf(currentUser == null) }

            DisposableEffect(Unit) {
                val listener = FirebaseAuth.AuthStateListener { auth ->
                    currentUser = auth.currentUser
                    if (auth.currentUser != null) {
                        checkingAutoSignIn = false
                    }
                }
                Firebase.auth.addAuthStateListener(listener)
                onDispose { Firebase.auth.removeAuthStateListener(listener) }
            }

            LaunchedEffect(Unit) {
                if (Firebase.auth.currentUser == null) {
                    AuthManager.attemptAutoSignIn(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = { checkingAutoSignIn = false },
                        onUnauthenticated = { checkingAutoSignIn = false },
                        scope = scope
                    )
                }
            }

            val signedInUser = currentUser
            if (signedInUser == null) {
                ClassXiTheme {
                    if (checkingAutoSignIn) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(listOf(RoyalBlueBright, RoyalBlue, RoyalBlueDark))),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    } else {
                        AuthScreen(
                            onAuthSuccess = {
                                currentUser = Firebase.auth.currentUser
                            }
                        )
                    }
                }
            } else {
                key(signedInUser.uid) {
                    val viewModel: ClassViewModel = viewModel(
                        key = signedInUser.uid,
                        factory = ClassViewModel.Factory(context)
                    )
                    ClassXiPortalRoot(
                        viewModel = viewModel,
                        currentUser = signedInUser,
                        deepLinkTab = deepLinkTab,
                        onConsumeDeepLink = { deepLinkTab = null },
                        onSignOut = {
                            AuthManager.signOut(credentialManager, scope)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkTab = intent.getStringExtra(NotificationHelper.EXTRA_TARGET_TAB)
    }
}

@Composable
private fun ClassXiPortalRoot(
    viewModel: ClassViewModel,
    currentUser: FirebaseUser,
    deepLinkTab: String?,
    onConsumeDeepLink: () -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSplashOverlay by remember { mutableStateOf(true) }
    var showAdminPinDialog by remember { mutableStateOf(false) }
    var showGuruPinDialog by remember { mutableStateOf(false) }
    var openMateriUploadOnGuruVerify by remember { mutableStateOf(false) }
    var openMateriUploadTrigger by remember { mutableStateOf(0) }
    var activeQrStudentId by remember { mutableStateOf<Int?>(null) }
    var showScannerDialog by remember { mutableStateOf(false) }
    var pendingPresensiFilter by remember { mutableStateOf<AttendanceStatus?>(null) }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        scope.launch {
            snackbarHostState.showSnackbar(
                if (granted) "Notifikasi perangkat aktif! Info dari teman akan muncul." else "Izin notifikasi ditolak; pengumuman tetap bisa dilihat di Info Kelas."
            )
        }
    }

    LaunchedEffect(currentUser.uid) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !NotificationHelper.hasNotificationPermission(context)
        ) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { event ->
            if (event is ToastEvent.Message) {
                snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDest = navBackStackEntry?.destination

    fun navigateTo(routeObj: Any) {
        navController.navigate(routeObj) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(deepLinkTab) {
        if (!deepLinkTab.isNullOrBlank()) {
            showSplashOverlay = false
            navigateTo(routeObjFromKey(deepLinkTab))
            onConsumeDeepLink()
        }
    }

    BackHandler(enabled = showSplashOverlay) {
        showSplashOverlay = false
    }

    val portalData = (uiState as? UiState.Success)?.data ?: ClassPortalData()

    ClassXiTheme(
        themeMode = portalData.preferences.themeMode,
        themeStyle = portalData.preferences.themeStyle
    ) {
        GlassBackdropBox {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isExpandedWidth = maxWidth >= 840.dp
                val isBeranda = currentDest?.hasRoute(BerandaRoute::class) == true
                val isSettings = currentDest?.hasRoute(SettingsRoute::class) == true

                Row(modifier = Modifier.fillMaxSize()) {
                    if (isExpandedWidth) {
                        NavigationRail(
                            modifier = Modifier.fillMaxHeight(),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            Spacer(modifier = Modifier.height(16.dp))
                            NavigationRailItem(
                                selected = isBeranda,
                                onClick = { navigateTo(BerandaRoute) },
                                icon = { Icon(Icons.Rounded.Home, contentDescription = "Beranda") },
                                label = { Text("Beranda", fontSize = 11.sp, maxLines = 1) },
                                modifier = Modifier.testTag("nav_beranda")
                            )
                            NavigationRailItem(
                                selected = false,
                                onClick = { showScannerDialog = true },
                                icon = { Icon(Icons.Rounded.QrCodeScanner, contentDescription = "Scan QR", tint = RoyalBlue) },
                                label = { Text("Scan QR", fontSize = 11.sp, maxLines = 1, color = RoyalBlue) },
                                modifier = Modifier.testTag("nav_scan_qr")
                            )
                            NavigationRailItem(
                                selected = isSettings,
                                onClick = { navigateTo(SettingsRoute) },
                                icon = { Icon(Icons.Rounded.Settings, contentDescription = "Pengaturan") },
                                label = { Text("Pengaturan", fontSize = 11.sp, maxLines = 1) },
                                modifier = Modifier.testTag("nav_settings")
                            )
                        }
                    }

                    Scaffold(
                        modifier = Modifier.weight(1f),
                        containerColor = Color.Transparent,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        topBar = {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                ClassXiTopHeader(
                                    data = portalData,
                                    onLogoClick = { navigateTo(BerandaRoute) },
                                    onOpenSettings = {
                                        if (isSettings) {
                                            navController.popBackStack()
                                        } else {
                                            navigateTo(SettingsRoute)
                                        }
                                    },
                                    onSearchChange = viewModel::setGlobalSearch,
                                    onToggleAdmin = {
                                        if (portalData.isAdminMode) viewModel.exitAdminMode() else showAdminPinDialog = true
                                    }
                                )
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    V9StatusStrip(
                                        pendingLeaveCount = portalData.pendingLeaveCount,
                                        isOnlineSynced = portalData.isOnlineSynced,
                                        onClickPendingLeave = { navigateTo(SuratIzinRoute) },
                                        modifier = Modifier.widthIn(max = 960.dp)
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            if (!isExpandedWidth) {
                                V9BottomBar(
                                    isBerandaSelected = isBeranda,
                                    onSelectBeranda = { navigateTo(BerandaRoute) },
                                    onOpenScanner = { showScannerDialog = true }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            when (val state = uiState) {
                                is UiState.Loading -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                            Text("Menyinkronkan data CLASS XI...", fontSize = 13.sp)
                                        }
                                    }
                                }

                                is UiState.Error -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text("Sinkronisasi Terganggu", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)
                                            Text(state.message, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }

                                is UiState.Success -> {
                                    val data = state.data
                                    NavHost(
                                        navController = navController,
                                        startDestination = BerandaRoute,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .widthIn(max = 960.dp)
                                    ) {
                                        composable<BerandaRoute> {
                                            HomeScreen(
                                                data = data,
                                                onNavigate = { key -> navigateTo(routeObjFromKey(key)) },
                                                onOpenStudentQr = { sid -> activeQrStudentId = sid },
                                                onOpenScanner = { showScannerDialog = true },
                                                onFilterPresensi = { st ->
                                                    pendingPresensiFilter = st
                                                    navigateTo(PresensiRoute)
                                                }
                                            )
                                        }
                                        composable<PresensiRoute> {
                                            PresensiScreen(
                                                data = data,
                                                initialFilter = pendingPresensiFilter,
                                                onConsumeInitialFilter = { pendingPresensiFilter = null },
                                                onOpenIzin = { navigateTo(SuratIzinRoute) },
                                                onSetDate = viewModel::setSelectedDate,
                                                onSetToday = viewModel::setTodayDate,
                                                onUpdateStatus = viewModel::updateStudentAttendance,
                                                onSetAllHadir = { viewModel.setAllAttendance(AttendanceStatus.Hadir) },
                                                onOpenStudentQr = { sid -> activeQrStudentId = sid },
                                                onOpenScanner = { showScannerDialog = true },
                                                onRequestAdminLogin = { showAdminPinDialog = true }
                                            )
                                        }
                                        composable<KasRoute> {
                                            KasScreen(
                                                data = data,
                                                onSelectPeriod = viewModel::setSelectedKasPeriod,
                                                onSelectNominal = viewModel::setSelectedKasNominal,
                                                onAddPeriod = viewModel::addKasPeriod,
                                                onToggleStudentPayment = viewModel::toggleStudentKasPayment,
                                                onMarkAllPaid = viewModel::markAllKasPaid,
                                                onAddTransaction = viewModel::addKasTransaction,
                                                onDeleteTransaction = viewModel::voidOrDeleteKasTransaction,
                                                onRequestAdminLogin = { showAdminPinDialog = true }
                                            )
                                        }
                                        composable<SuratIzinRoute> {
                                            IzinScreen(
                                                data = data,
                                                currentUid = currentUser.uid,
                                                onRequestAdminLogin = { showAdminPinDialog = true },
                                                onSubmitLeave = viewModel::submitLeaveRequest,
                                                onVerifyLeave = viewModel::verifyLeaveRequest,
                                                onDeleteLeave = viewModel::deleteLeaveRequest,
                                                onShowMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                            )
                                        }
                                        composable<JadwalRoute> {
                                            JadwalScreen(
                                                data = data,
                                                onSelectDay = viewModel::setSelectedScheduleDay,
                                                onOpenStudentQr = { sid -> activeQrStudentId = sid }
                                            )
                                        }
                                        composable<QrRoute> {
                                            QrCardsScreen(
                                                data = data,
                                                onOpenStudentQr = { sid -> activeQrStudentId = sid },
                                                onOpenScanner = { showScannerDialog = true }
                                            )
                                        }
                                        composable<DokumentasiRoute> {
                                            DokumentasiScreen(
                                                data = data,
                                                currentUid = currentUser.uid,
                                                observeComments = viewModel::observeComments,
                                                onRequestAdminLogin = { showAdminPinDialog = true },
                                                onAddPost = viewModel::addDokumentasiPost,
                                                onToggleLike = viewModel::toggleDokumentasiLike,
                                                onAddComment = viewModel::addDokumentasiComment,
                                                onDeleteComment = viewModel::deleteDokumentasiComment,
                                                onDeletePost = viewModel::deleteDokumentasiPost,
                                                onShowMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                            )
                                        }
                                        composable<InfoRoute> {
                                            InfoScreen(
                                                data = data,
                                                currentUid = currentUser.uid,
                                                onRequestAdminLogin = { showAdminPinDialog = true },
                                                onAddAnnouncement = viewModel::addAnnouncement,
                                                onDeleteAnnouncement = viewModel::deleteAnnouncement
                                            )
                                        }
                                        composable<MateriRoute> {
                                            MateriScreen(
                                                data = data,
                                                currentUid = currentUser.uid,
                                                openUploadTrigger = openMateriUploadTrigger,
                                                onRequestGuruLogin = {
                                                    openMateriUploadOnGuruVerify = true
                                                    showGuruPinDialog = true
                                                },
                                                onExitGuruMode = viewModel::exitGuruMode,
                                                onAddMateri = viewModel::addMateri,
                                                onIncrementDownload = viewModel::incrementMateriDownload,
                                                onDeleteMateri = viewModel::deleteMateri,
                                                onShowMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                            )
                                        }
                                        composable<RekapRoute> {
                                            RekapScreen(
                                                data = data,
                                                onOpenStudentQr = { sid -> activeQrStudentId = sid },
                                                onShowMessage = { msg -> scope.launch { snackbarHostState.showSnackbar(msg) } }
                                            )
                                        }
                                        composable<SettingsRoute> {
                                            SettingsScreen(
                                                data = data,
                                                currentUser = currentUser,
                                                onSetThemeMode = viewModel::setThemeMode,
                                                onSetThemeStyle = viewModel::setThemeStyle,
                                                onSignOut = onSignOut
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (activeQrStudentId != null) {
                StudentQrDialog(
                    studentId = activeQrStudentId!!,
                    isAdminMode = portalData.isAdminMode,
                    onMarkPresent = { sid -> viewModel.updateStudentAttendance(sid, AttendanceStatus.Hadir) },
                    onRequestAdminLogin = { showAdminPinDialog = true },
                    onDismiss = { activeQrStudentId = null }
                )
            }

            if (showScannerDialog) {
                QrScannerDialog(
                    isAdminMode = portalData.isAdminMode,
                    onScanQr = viewModel::processScannedStudentQr,
                    onRequestAdminLogin = { showAdminPinDialog = true },
                    onDismiss = { showScannerDialog = false }
                )
            }

            if (showAdminPinDialog) {
                AdminPinDialog(
                    onDismiss = { showAdminPinDialog = false },
                    onVerify = viewModel::verifyAdminPin
                )
            }

            if (showGuruPinDialog) {
                GuruPinDialog(
                    onDismiss = {
                        openMateriUploadOnGuruVerify = false
                        showGuruPinDialog = false
                    },
                    onVerify = { pin ->
                        val ok = viewModel.verifyGuruPin(pin)
                        if (ok && openMateriUploadOnGuruVerify) {
                            openMateriUploadOnGuruVerify = false
                            openMateriUploadTrigger++
                        }
                        ok
                    }
                )
            }

            SplashWelcomeOverlay(
                visible = showSplashOverlay,
                onDismiss = { showSplashOverlay = false }
            )
        }
    }
}

@Composable
private fun ClassXiTopHeader(
    data: ClassPortalData,
    onLogoClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleAdmin: () -> Unit
) {
    val isGlass = LocalLiquidGlass.current
    val isDark = LocalIsDarkTheme.current

    val headerBrush = when {
        isGlass && isDark -> Brush.linearGradient(listOf(Color(0xCC145AE6), Color(0xD6083096)))
        isGlass -> Brush.linearGradient(listOf(Color(0xEB1276FF), Color(0xE0004CCC)))
        isDark -> Brush.linearGradient(listOf(Color(0xFF0A4FC4), RoyalBlueDeep))
        else -> Brush.linearGradient(listOf(RoyalBlueBright, RoyalBlueMid))
    }

    val headerDateLabel = remember {
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(Date())
    }

    Surface(
        shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBrush),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 960.dp)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top identity + controls row (matches .app-header lines 745-777)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onLogoClick)
                            .minimumInteractiveComponentSize()
                            .testTag("topbar_logo_home")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isGlass) Color(0xEDFFFFFF) else Color.White)
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_puspita),
                                contentDescription = "Logo SMA Puspita CLASS XI",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "CLASS XI",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.20f)
                                ) {
                                    Text(
                                        text = "SMA Puspita",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = headerDateLabel,
                                color = Color.White.copy(alpha = 0.80f),
                                fontSize = 11.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("topbar_settings_btn")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.22f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "Pengaturan Tampilan",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        val adminBg = when {
                            data.isAdminMode -> EmeraldKas
                            isGlass -> Color.White.copy(alpha = 0.86f)
                            else -> Color.White
                        }
                        val adminFg = if (data.isAdminMode) Color.White else RoyalBlue
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = adminBg,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onToggleAdmin)
                                .minimumInteractiveComponentSize()
                                .testTag("topbar_admin_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (data.isAdminMode) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                                    contentDescription = "Mode Pengurus",
                                    tint = adminFg,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (data.isAdminMode) data.activePengurusRole else "Mode Pengurus",
                                    color = adminFg,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.5.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // v9 Global Search bar (#globalSearch)
                val searchBg = when {
                    isGlass -> Color.White.copy(alpha = 0.22f)
                    isDark -> Color(0xF0131C2E)
                    else -> Color(0xF5FFFFFF)
                }
                val searchBorder = when {
                    isGlass -> Color.White.copy(alpha = 0.45f)
                    isDark -> Color.White.copy(alpha = 0.14f)
                    else -> Color.White.copy(alpha = 0.70f)
                }
                val searchTextColor = when {
                    isGlass -> Color.White
                    isDark -> Color(0xFFF1F5F9)
                    else -> Color(0xFF0F172A)
                }
                val searchPlaceholderColor = when {
                    isGlass -> Color.White.copy(alpha = 0.78f)
                    isDark -> Color(0xFF94A3B8)
                    else -> Color(0xFF64748B)
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = searchBg,
                    border = BorderStroke(1.dp, searchBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = searchPlaceholderColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            if (data.globalSearch.isEmpty()) {
                                Text(
                                    text = "Cari siswa, kas, dokumentasi, jadwal, atau materi...",
                                    color = searchPlaceholderColor,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = data.globalSearch,
                                onValueChange = onSearchChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    color = searchTextColor,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                cursorBrush = SolidColor(if (isGlass) Color.White else RoyalBlue),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("topbar_global_search")
                            )
                        }
                        if (data.globalSearch.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(searchPlaceholderColor.copy(alpha = 0.18f))
                                    .clickable { onSearchChange("") }
                                    .testTag("topbar_search_clear"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Hapus pencarian",
                                    tint = searchTextColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun V9StatusStrip(
    pendingLeaveCount: Int,
    isOnlineSynced: Boolean,
    onClickPendingLeave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val isGlass = LocalLiquidGlass.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (pendingLeaveCount > 0) {
            val pendingBg = when {
                isGlass -> Color(0x28F59E0B)
                isDark -> Color(0x2EF59E0B)
                else -> Color(0xFFFFFBEB)
            }
            val pendingBorder = if (isDark) Color(0x55F59E0B) else Color(0xFFFDE68A)
            val pendingText = if (isDark) Color(0xFFFCD34D) else Color(0xFF92400E)
            val pendingIconTint = if (isDark) Color(0xFFFCD34D) else Color(0xFFD97706)

            Surface(
                shape = RoundedCornerShape(50),
                color = pendingBg,
                border = BorderStroke(1.dp, pendingBorder),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onClickPendingLeave)
                    .testTag("status_pending_leave_pill")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MailOutline,
                        contentDescription = null,
                        tint = pendingIconTint,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$pendingLeaveCount Surat Izin/Sakit menunggu verifikasi",
                        color = pendingText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.width(1.dp))
        }

        val cloudBg = when {
            isOnlineSynced && isDark -> Color(0x2210B981)
            isOnlineSynced -> Color(0xFFECFDF5)
            isDark -> Color(0x22F59E0B)
            else -> Color(0xFFFFFBEB)
        }
        val cloudBorder = when {
            isOnlineSynced && isDark -> Color(0x5010B981)
            isOnlineSynced -> Color(0xFFA7F3D0)
            isDark -> Color(0x50F59E0B)
            else -> Color(0xFFFDE68A)
        }
        val cloudFg = when {
            isOnlineSynced && isDark -> Color(0xFF6EE7B7)
            isOnlineSynced -> EmeraldDark
            isDark -> Color(0xFFFCD34D)
            else -> Color(0xFFB45309)
        }
        val dotColor = if (isOnlineSynced) Color(0xFF10B981) else AmberSakit

        Surface(
            shape = RoundedCornerShape(50),
            color = cloudBg,
            border = BorderStroke(1.dp, cloudBorder),
            modifier = Modifier.testTag("status_cloud_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
                Text(
                    text = if (isOnlineSynced) "Online" else "Tidak ada internet",
                    color = cloudFg,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun V9BottomBar(
    isBerandaSelected: Boolean,
    onSelectBeranda: () -> Unit,
    onOpenScanner: () -> Unit
) {
    val isGlass = LocalLiquidGlass.current
    val isDark = LocalIsDarkTheme.current
    val activeBlue = if (isDark) Color(0xFF5B9BFF) else RoyalBlue
    val inactiveSlate = if (isDark) TextMutedDark else TextMutedLight

    if (isGlass) {
        val pillBg = if (isDark) Color(0xB8162040) else Color(0xD6FFFFFF)
        val pillBorder = if (isDark) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.90f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 14.dp, end = 14.dp, bottom = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = pillBg,
                border = BorderStroke(1.dp, pillBorder),
                modifier = Modifier
                    .widthIn(max = 448.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    V9BottomNavItem(
                        icon = Icons.Rounded.Home,
                        label = "Beranda",
                        color = if (isBerandaSelected) activeBlue else inactiveSlate,
                        onClick = onSelectBeranda,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_beranda")
                    )
                    V9BottomNavItem(
                        icon = Icons.Rounded.QrCodeScanner,
                        label = "Scan QR",
                        color = activeBlue,
                        onClick = onOpenScanner,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_scan_qr")
                    )
                }
            }
        }
    } else {
        val barBg = if (isDark) NavBarDark else Color.White
        val borderCol = if (isDark) DividerDark else OutlineLight
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(barBg)
                .navigationBarsPadding()
        ) {
            HorizontalDivider(thickness = 1.dp, color = borderCol)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 448.dp)
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    V9BottomNavItem(
                        icon = Icons.Rounded.Home,
                        label = "Beranda",
                        color = if (isBerandaSelected) activeBlue else inactiveSlate,
                        onClick = onSelectBeranda,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_beranda")
                    )
                    V9BottomNavItem(
                        icon = Icons.Rounded.QrCodeScanner,
                        label = "Scan QR",
                        color = activeBlue,
                        onClick = onOpenScanner,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_scan_qr")
                    )
                }
            }
        }
    }
}

@Composable
private fun V9BottomNavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .minimumInteractiveComponentSize()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(21.dp)
        )
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

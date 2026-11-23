package com.example.ui.screens

import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ClassConstants
import com.example.model.DokumentasiComment
import com.example.model.DokumentasiPost
import com.example.ui.components.ClassCard
import com.example.ui.components.MediaEncodingUtil
import com.example.ui.components.MediaImageBox
import com.example.ui.components.PortalFeedback
import com.example.ui.components.StatusBadge
import com.example.ui.components.V9DialogCard
import com.example.ui.components.V9Field
import com.example.ui.components.V9FooterText
import com.example.ui.components.V9PillChip
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.RoseAlpa
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.RoyalBlueBright
import com.example.ui.theme.RoyalBlueDark
import com.example.ui.theme.RoyalBlueMid
import com.example.ui.theme.VioletDispen
import com.example.viewmodel.ClassPortalData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DokumentasiScreen(
    data: ClassPortalData,
    currentUid: String,
    observeComments: (String) -> Flow<List<DokumentasiComment>>,
    onRequestAdminLogin: () -> Unit,
    onAddPost: (
        title: String,
        category: String,
        mediaType: String,
        mediaUrl: String,
        fileName: String,
        caption: String,
        date: String,
        onDone: () -> Unit
    ) -> Unit,
    onToggleLike: (DokumentasiPost) -> Unit,
    onAddComment: (dokId: String, customName: String, text: String, onDone: () -> Unit) -> Unit,
    onDeleteComment: (dokId: String, commentId: String) -> Unit,
    onDeletePost: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current

    var selectedFilter by remember { mutableStateOf("Semua") }
    var localSearch by remember { mutableStateOf("") }
    var showUploadDialog by remember { mutableStateOf(false) }
    var pendingSavePost by remember { mutableStateOf<DokumentasiPost?>(null) }

    val savePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/jpeg")
    ) { uri: Uri? ->
        val post = pendingSavePost
        pendingSavePost = null
        if (uri != null && post != null && post.mediaUrl.startsWith("data:image")) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) {
                    try {
                        val b64 = post.mediaUrl.substringAfter("base64,", "")
                        val bytes = Base64.decode(b64, Base64.DEFAULT)
                        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                        true
                    } catch (_: Exception) {
                        false
                    }
                }
                onShowMessage(if (ok) "Foto berhasil disimpan ke perangkat." else "Gagal menyimpan foto.")
            }
        }
    }

    val filterOptions = listOf("Semua", "Foto", "Video") + ClassConstants.DOK_CATEGORIES
    val globalQ = data.globalSearch.trim().lowercase()
    val localQ = localSearch.trim().lowercase()
    val filteredPosts = remember(data.dokumentasi, selectedFilter, globalQ, localQ) {
        data.dokumentasi.filter { post ->
            val matchFilter = when (selectedFilter) {
                "Semua" -> true
                "Foto" -> post.mediaType == "photo"
                "Video" -> post.mediaType == "video"
                else -> post.category == selectedFilter
            }
            val matchGlobal = globalQ.isBlank() ||
                post.title.lowercase().contains(globalQ) ||
                post.caption.lowercase().contains(globalQ) ||
                post.category.lowercase().contains(globalQ) ||
                post.author.lowercase().contains(globalQ)
            val matchLocal = localQ.isBlank() ||
                post.title.lowercase().contains(localQ) ||
                post.caption.lowercase().contains(localQ) ||
                post.category.lowercase().contains(localQ)
            matchFilter && matchGlobal && matchLocal
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cols = if (maxWidth >= 700.dp) 2 else 1
        val postRows = remember(filteredPosts, cols) { filteredPosts.chunked(cols) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Violet Header Banner Card matching HTML lines 901-921
            item {
                val bannerContainer = if (isDark) Color(0xFF1A1936) else Color(0xFFF7F4FF)
                val bannerBorder = if (isDark) Color(0x4D8B5CF6) else Color(0xFFDDD6FE)
                ClassCard(
                    containerColor = bannerContainer,
                    borderColor = bannerBorder,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isDark) Color(0x308B5CF6) else Color(0xFFEDE9FE)
                                ) {
                                    Text(
                                        text = "Galeri & Arsip CLASS XI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDark) Color(0xFFC4B5FD) else Color(0xFF6D28D9),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = "${data.dokumentasi.size} Momen",
                                    fontFamily = JetBrainsMono,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Dokumentasi Foto & Video Kelas",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Momen kebersamaan, praktikum, dan kegiatan CLASS XI. Seluruh siswa dapat memberikan Like, menulis Komentar, serta Download dokumentasi.",
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = RoyalBlue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                if (data.isAdminMode) showUploadDialog = true else onRequestAdminLogin()
                            }
                            .minimumInteractiveComponentSize()
                            .testTag("dok_upload_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CloudUpload,
                                contentDescription = "Upload Foto / Video",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Upload Foto / Video",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            // 2. Filter Kategori & Local Search Card matching HTML lines 924-942
            item {
                ClassCard(contentPadding = PaddingValues(14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filterOptions.forEach { option ->
                                val icon = when (option) {
                                    "Foto" -> Icons.Rounded.CameraAlt
                                    "Video" -> Icons.Rounded.Videocam
                                    else -> null
                                }
                                V9PillChip(
                                    text = option,
                                    selected = selectedFilter == option,
                                    onClick = { selectedFilter = option },
                                    leadingIcon = icon,
                                    modifier = Modifier.testTag("dok_filter_${option.lowercase().replace(" ", "_")}")
                                )
                            }
                        }

                        V9Field(
                            value = localSearch,
                            onValueChange = { localSearch = it },
                            placeholder = "Cari judul atau caption...",
                            leadingIcon = Icons.Rounded.Search,
                            roundedPill = true
                        )
                    }
                }
            }

            // 3. Dokumentasi Feed Cards (1 or 2 columns)
            if (postRows.isEmpty()) {
                item {
                    ClassCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0x288B5CF6) else Color(0xFFF5F3FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.PhotoLibrary,
                                    contentDescription = "Galeri Kosong",
                                    tint = VioletDispen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Text(
                                text = "Belum Ada Dokumentasi Ditemukan",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Pengurus kelas dapat mengunggah foto kegiatan atau tautan video dokumentasi.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(postRows, key = { chunk -> chunk.first().id }) { rowPosts ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        rowPosts.forEach { post ->
                            val comments by observeComments(post.id).collectAsStateWithLifecycle(initialValue = emptyList())
                            Box(modifier = Modifier.weight(1f)) {
                                DokumentasiPostCard(
                                    post = post,
                                    isLiked = post.id in data.likedDokIds,
                                    canDeletePost = post.userId == currentUid || data.isAdminMode,
                                    currentUid = currentUid,
                                    isAdminMode = data.isAdminMode,
                                    isDark = isDark,
                                    comments = comments,
                                    onToggleLike = { onToggleLike(post) },
                                    onAddComment = { customName, text, onDone ->
                                        onAddComment(post.id, customName, text, onDone)
                                    },
                                    onDeleteComment = { cid -> onDeleteComment(post.id, cid) },
                                    onDeletePost = { onDeletePost(post.id) },
                                    onSavePhoto = {
                                        pendingSavePost = post
                                        val baseName = post.fileName.ifBlank { "CLASS_XI_${post.id}.jpg" }
                                        savePhotoLauncher.launch(if (baseName.endsWith(".jpg", true)) baseName else "$baseName.jpg")
                                    },
                                    onOpenExternalMedia = {
                                        if (post.mediaUrl.startsWith("http://") || post.mediaUrl.startsWith("https://")) {
                                            PortalFeedback.openUrl(context, post.mediaUrl)
                                        } else {
                                            PortalFeedback.shareText(
                                                context,
                                                "Dokumentasi CLASS XI: ${post.title}",
                                                "${post.title} (${post.category})\n${post.caption}\n${post.mediaUrl}"
                                            )
                                        }
                                    }
                                )
                            }
                        }
                        if (rowPosts.size < cols) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item { V9FooterText() }
        }
    }

    if (showUploadDialog) {
        UploadDokumentasiDialog(
            onDismiss = { showUploadDialog = false },
            onSubmit = { title, cat, mediaType, mediaUrl, fileName, caption, date ->
                onAddPost(title, cat, mediaType, mediaUrl, fileName, caption, date) {
                    showUploadDialog = false
                }
            }
        )
    }
}

@Composable
private fun DokumentasiPostCard(
    post: DokumentasiPost,
    isLiked: Boolean,
    canDeletePost: Boolean,
    currentUid: String,
    isAdminMode: Boolean,
    isDark: Boolean,
    comments: List<DokumentasiComment>,
    onToggleLike: () -> Unit,
    onAddComment: (customName: String, text: String, onDone: () -> Unit) -> Unit,
    onDeleteComment: (String) -> Unit,
    onDeletePost: () -> Unit,
    onSavePhoto: () -> Unit,
    onOpenExternalMedia: () -> Unit
) {
    val context = LocalContext.current
    var showComments by remember { mutableStateOf(true) }
    var commentText by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }

    ClassCard(contentPadding = PaddingValues(16.dp)) {
        // Post Header matching HTML lines 2767-2786
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
                        .background(Brush.linearGradient(listOf(RoyalBlueBright, RoyalBlueMid))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AdminPanelSettings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = post.author,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        StatusBadge(
                            text = post.category,
                            containerColor = if (isDark) Color(0x308B5CF6) else Color(0xFFF5F3FF),
                            contentColor = if (isDark) Color(0xFFC4B5FD) else Color(0xFF6D28D9),
                            cornerRadius = 50.dp
                        )
                    }
                    Text(
                        text = post.date,
                        fontFamily = JetBrainsMono,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (canDeletePost) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isDark) Color(0x2DF43F5E) else Color(0xFFFFF1F2),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onDeletePost)
                        .testTag("dok_delete_${post.id}")
                ) {
                    Text(
                        text = "Hapus",
                        color = RoseAlpa,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Media Photo / Video Block with top-left badge overlay
        if (post.mediaType == "photo" && post.mediaUrl.isNotBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                MediaImageBox(
                    url = post.mediaUrl,
                    contentDescription = "Foto dokumentasi ${post.title}",
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.60f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Rounded.CameraAlt, contentDescription = null, tint = Color(0xFF7DD3FC), modifier = Modifier.size(11.dp))
                        Text("FOTO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        } else if (post.mediaType == "video") {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = RoyalBlueDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.45f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Rounded.Videocam, contentDescription = null, tint = Color(0xFFFDA4AF), modifier = Modifier.size(11.dp))
                            Text("VIDEO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(RoyalBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Rounded.Movie, contentDescription = "Video Dokumentasi", tint = Color.White)
                            }
                            Column {
                                Text(
                                    text = post.fileName.ifBlank { "Video Dokumentasi CLASS XI" },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = post.mediaUrl,
                                    color = Color(0xFFBFDBFE),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onOpenExternalMedia,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dok_open_video_${post.id}")
                        ) {
                            Icon(Icons.Rounded.OpenInNew, contentDescription = "Buka video", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buka", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = post.title,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (post.caption.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.caption,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(10.dp))

        // Action Bar: Like, Komentar Counter, Download/Share matching HTML lines 2800-2817
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isLiked) RoseAlpa else if (isDark) Color(0x2AF43F5E) else Color(0xFFFFF1F2),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onToggleLike)
                        .minimumInteractiveComponentSize()
                        .testTag("dok_like_${post.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Sukai postingan",
                            tint = if (isLiked) Color.White else RoseAlpa,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${post.likesCount} Suka",
                            color = if (isLiked) Color.White else RoseAlpa,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { showComments = !showComments }
                        .minimumInteractiveComponentSize()
                        .testTag("dok_comment_toggle_${post.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChatBubbleOutline,
                            contentDescription = "Komentar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${comments.size} Komentar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (post.mediaType == "photo" && post.mediaUrl.startsWith("data:image")) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isDark) Color(0x2810B981) else Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, if (isDark) Color(0x5010B981) else Color(0xFFA7F3D0)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onSavePhoto)
                        .minimumInteractiveComponentSize()
                        .testTag("dok_download_${post.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Download,
                            contentDescription = "Simpan foto ke perangkat",
                            tint = if (isDark) Color(0xFF6EE7B7) else EmeraldDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Download",
                            color = if (isDark) Color(0xFF6EE7B7) else EmeraldDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isDark) Color(0x2810B981) else Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, if (isDark) Color(0x5010B981) else Color(0xFFA7F3D0)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            PortalFeedback.shareText(
                                context,
                                "Dokumentasi CLASS XI: ${post.title}",
                                "${post.title} (${post.category})\n${post.caption}\n${post.mediaUrl}"
                            )
                        }
                        .minimumInteractiveComponentSize()
                        .testTag("dok_share_${post.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Bagikan dokumentasi",
                            tint = if (isDark) Color(0xFF6EE7B7) else EmeraldDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Bagikan",
                            color = if (isDark) Color(0xFF6EE7B7) else EmeraldDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Inline Comments + Form matching HTML lines 2819-2835
        if (showComments) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (comments.isEmpty()) {
                    Text(
                        text = "Belum ada komentar. Jadilah yang pertama berkomentar!",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    comments.forEach { comment ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = comment.senderName,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = comment.timeLabel,
                                            fontFamily = JetBrainsMono,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = comment.text,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (comment.userId == currentUid || isAdminMode) {
                                    IconButton(
                                        onClick = { onDeleteComment(comment.id) },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("dok_del_comment_${comment.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Hapus komentar",
                                            tint = RoseAlpa,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                V9Field(
                    value = customName,
                    onValueChange = { customName = it.take(40) },
                    placeholder = "Nama kamu...",
                    roundedPill = true,
                    modifier = Modifier.testTag("dok_comment_name_${post.id}")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    V9Field(
                        value = commentText,
                        onValueChange = { commentText = it.take(240) },
                        placeholder = "Tulis komentar...",
                        roundedPill = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dok_comment_input_${post.id}")
                    )
                    Surface(
                        shape = CircleShape,
                        color = RoyalBlue,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (commentText.isNotBlank()) {
                                    onAddComment(customName, commentText) {
                                        commentText = ""
                                    }
                                }
                            }
                            .minimumInteractiveComponentSize()
                            .testTag("dok_comment_send_${post.id}")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Send,
                                contentDescription = "Kirim komentar",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UploadDokumentasiDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        category: String,
        mediaType: String,
        mediaUrl: String,
        fileName: String,
        caption: String,
        date: String
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isDark = LocalIsDarkTheme.current

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ClassConstants.DOK_CATEGORIES.first()) }
    var mediaType by remember { mutableStateOf("photo") }
    var mediaUrl by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())) }
    var encodingPhoto by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            encodingPhoto = true
            errorMsg = null
            scope.launch {
                val encoded = MediaEncodingUtil.encodeCompressedImageDataUrl(context, uri)
                encodingPhoto = false
                if (encoded != null) {
                    mediaUrl = encoded
                    fileName = MediaEncodingUtil.getFileName(context, uri)
                } else {
                    errorMsg = "Ukuran gambar terlalu besar atau gagal diproses."
                }
            }
        }
    }

    V9DialogCard(onDismissRequest = onDismiss) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(end = 28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDark) Color(0x308B5CF6) else Color(0xFFEDE9FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, tint = VioletDispen)
            }
            Column {
                Text("Upload Dokumentasi CLASS XI", fontWeight = FontWeight.ExtraBold, fontSize = 15.5.sp)
                Text("Unggah foto atau video kegiatan kelas", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            V9PillChip(
                text = "Foto",
                selected = mediaType == "photo",
                onClick = { mediaType = "photo" },
                modifier = Modifier.testTag("dok_type_photo")
            )
            V9PillChip(
                text = "Link Video",
                selected = mediaType == "video",
                onClick = { mediaType = "video" },
                modifier = Modifier.testTag("dok_type_video")
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Judul Kegiatan / Momen", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            V9Field(
                value = title,
                onValueChange = { title = it.take(100) },
                placeholder = "Contoh: Praktikum Kimia Titrasi Asam Basa",
                modifier = Modifier.testTag("dok_dialog_title")
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Kategori", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ClassConstants.DOK_CATEGORIES.forEach { cat ->
                    V9PillChip(
                        text = cat,
                        selected = category == cat,
                        onClick = { category = cat }
                    )
                }
            }
        }

        if (mediaType == "photo") {
            OutlinedButton(
                onClick = {
                    photoPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("dok_pick_photo_button")
            ) {
                if (encodingPhoto) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mengompresi Foto...", fontSize = 12.sp)
                } else {
                    Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (fileName.isBlank()) "Pilih Foto dari Galeri" else "Terpilih: $fileName", fontSize = 12.sp)
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = if (mediaType == "video") "URL Video (YouTube / Drive / MP4)" else "Atau Tautan URL Foto (Opsional)",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
            V9Field(
                value = if (mediaUrl.startsWith("data:image")) "(Foto terlampir dari galeri)" else mediaUrl,
                onValueChange = {
                    if (!it.startsWith("(Foto terlampir")) {
                        mediaUrl = it.trim()
                    }
                },
                placeholder = "https://...",
                enabled = !mediaUrl.startsWith("data:image"),
                modifier = Modifier.testTag("dok_dialog_url")
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Tanggal Momen", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            V9Field(
                value = date,
                onValueChange = { date = it.take(10) },
                placeholder = "YYYY-MM-DD"
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Caption / Deskripsi", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            V9Field(
                value = caption,
                onValueChange = { caption = it.take(300) },
                placeholder = "Ceritakan singkat keseruan momen kegiatan ini...",
                singleLine = false,
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.testTag("dok_dialog_caption")
            )
        }

        if (errorMsg != null) {
            Text(errorMsg!!, color = RoseAlpa, fontSize = 12.sp)
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(50),
                modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
            ) {
                Text("Batal", fontWeight = FontWeight.Bold)
            }
            Button(
                enabled = !encodingPhoto,
                onClick = {
                    when {
                        title.isBlank() -> errorMsg = "Judul kegiatan wajib diisi."
                        mediaUrl.isBlank() -> errorMsg = "Pilih foto atau isi URL media terlebih dahulu."
                        else -> onSubmit(title.trim(), category, mediaType, mediaUrl, fileName, caption.trim(), date)
                    }
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("dok_dialog_submit")
            ) {
                Text("Publikasikan", fontWeight = FontWeight.Bold)
            }
        }
    }
}

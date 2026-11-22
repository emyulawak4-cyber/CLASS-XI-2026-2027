package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.print.PrintAttributes
import android.print.PrintManager
import android.provider.OpenableColumns
import android.util.Base64
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.LocalLiquidGlass
import com.example.ui.theme.RoyalBlue
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.NumberFormat
import java.util.Locale

fun formatRupiah(amount: Long): String {
    val nf = NumberFormat.getNumberInstance(Locale("id", "ID"))
    return "Rp ${nf.format(amount)}"
}

@Composable
fun ClassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val isGlass = LocalLiquidGlass.current
    val isDark = LocalIsDarkTheme.current
    val shape = RoundedCornerShape(if (isGlass) 22.dp else 16.dp)
    val resolvedBg = if (isGlass) {
        if (isDark) Color(0x88182242) else Color(0xA6FFFFFF)
    } else {
        containerColor
    }
    val resolvedBorder = if (isGlass) {
        if (isDark) Color.White.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.80f)
    } else {
        borderColor
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = resolvedBg),
        border = BorderStroke(1.dp, resolvedBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun GlassBackdropBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isGlass = LocalLiquidGlass.current
    val isDark = LocalIsDarkTheme.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                if (isGlass) {
                    if (isDark) Color(0xFF060B18) else Color(0xFFE4EEFF)
                } else {
                    MaterialTheme.colorScheme.background
                }
            )
    ) {
        if (isGlass) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val baseStops = if (isDark) {
                    listOf(Color(0xFF060B18), Color(0xFF091229), Color(0xFF060B18))
                } else {
                    listOf(Color(0xFFE4EEFF), Color(0xFFEEF3FF), Color(0xFFE2F4FF))
                }
                drawRect(brush = Brush.linearGradient(baseStops))

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(if (isDark) 0x802563EB else 0x66007AFF),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.10f, h * 0.08f),
                        radius = w * 0.65f
                    ),
                    center = Offset(w * 0.10f, h * 0.08f),
                    radius = w * 0.65f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(if (isDark) 0x617C3AED else 0x4D8B5CF6),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.96f, h * 0.30f),
                        radius = w * 0.60f
                    ),
                    center = Offset(w * 0.96f, h * 0.30f),
                    radius = w * 0.60f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(if (isDark) 0x660891B2 else 0x5C06B6D4),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.28f, h * 0.96f),
                        radius = w * 0.65f
                    ),
                    center = Offset(w * 0.28f, h * 0.96f),
                    radius = w * 0.65f
                )
            }
        }
        content()
    }
}

@Composable
fun V9SegmentedControl(
    options: List<Pair<String, String>>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp
) {
    val isGlass = LocalLiquidGlass.current
    val isDark = LocalIsDarkTheme.current
    val trackBg = when {
        isGlass && isDark -> Color.White.copy(alpha = 0.07f)
        isGlass -> Color.White.copy(alpha = 0.42f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(trackBg)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        options.forEach { (key, label) ->
            val selected = key == selectedKey
            val pillBg = when {
                selected && isDark && !isGlass -> Color(0xFF26334D)
                selected -> MaterialTheme.colorScheme.surface
                else -> Color.Transparent
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = pillBg,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelect(key) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun QrCodeImage(
    payload: String,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(payload) {
        generateQrBitmap(payload, sizePx = 512)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE7ECF3), RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR Code $payload",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun MediaImageBox(
    url: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: androidx.compose.ui.layout.ContentScale = androidx.compose.ui.layout.ContentScale.Crop
) {
    if (url.startsWith("data:image", ignoreCase = true)) {
        val bitmap = remember(url) { MediaEncodingUtil.decodeDataUrlBitmap(url) }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
    } else if (url.isNotBlank()) {
        coil.compose.AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap? {
    return try {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 1
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val darkBlue = 0xFF0043B4.toInt()
        val white = 0xFFFFFFFF.toInt()
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bmp.setPixel(x, y, if (matrix[x, y]) darkBlue else white)
            }
        }
        bmp
    } catch (_: Exception) {
        null
    }
}

object PortalFeedback {
    fun playScanChime(context: Context, isSuccess: Boolean = true) {
        try {
            val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
            tg.startTone(
                if (isSuccess) ToneGenerator.TONE_PROP_BEEP2 else ToneGenerator.TONE_PROP_NACK,
                220
            )
        } catch (_: Exception) {
        }
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(if (isSuccess) 60L else 140L, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            }
        } catch (_: Exception) {
        }
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun shareText(context: Context, subject: String, text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan via"))
    }

    fun openUrl(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun printHtmlReport(context: Context, jobName: String, htmlBody: String) {
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?) = false
            override fun onPageFinished(view: WebView, url: String) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                val printAdapter = view.createPrintDocumentAdapter(jobName)
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlBody, "text/HTML", "UTF-8", null)
    }
}

object MediaEncodingUtil {
    fun getFileName(context: Context, uri: Uri): String {
        var result = "lampiran_class_xi"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) result = cursor.getString(idx) ?: result
            }
        }
        return result
    }

    suspend fun encodeCompressedImageDataUrl(context: Context, uri: Uri, maxDim: Int = 960): String? =
        withContext(Dispatchers.IO) {
            try {
                val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext null
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, bounds)
                var sample = 1
                while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) {
                    sample *= 2
                }
                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sample }
                val bmp = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, decodeOpts)
                    ?: return@withContext null

                var quality = 78
                var outBytes: ByteArray
                do {
                    val baos = ByteArrayOutputStream()
                    bmp.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                    outBytes = baos.toByteArray()
                    quality -= 12
                } while (outBytes.size > 260_000 && quality >= 30)

                if (outBytes.size > 310_000) return@withContext null
                val b64 = Base64.encodeToString(outBytes, Base64.NO_WRAP)
                "data:image/jpeg;base64,$b64"
            } catch (_: Exception) {
                null
            }
        }

    suspend fun encodeSmallFileDataUrl(context: Context, uri: Uri, mimeType: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return@withContext null
                if (bytes.size > 300_000) return@withContext null
                val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                "data:$mimeType;base64,$b64"
            } catch (_: Exception) {
                null
            }
        }

    fun decodeDataUrlBitmap(dataUrl: String): Bitmap? {
        return try {
            val base64Part = dataUrl.substringAfter("base64,", "")
            if (base64Part.isBlank()) return null
            val bytes = Base64.decode(base64Part, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        } catch (_: Exception) {
            null
        }
    }
}

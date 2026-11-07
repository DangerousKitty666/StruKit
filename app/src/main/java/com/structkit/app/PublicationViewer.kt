package com.structkit.app

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable internal fun PublicationViewer(asset: String, title: String, initialPage: Int = 1, close: () -> Unit) {
    BackHandler { close() }
    val context = LocalContext.current
    var page by remember(asset) { mutableIntStateOf((initialPage - 1).coerceAtLeast(0)) }
    var total by remember(asset) { mutableIntStateOf(0) }
    var image by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var jump by remember { mutableStateOf(initialPage.toString()) }
    LaunchedEffect(asset, page) {
        image = null; error = null
        try {
            val rendered = withContext(Dispatchers.IO) {
                val dir = File(context.cacheDir, "publications").apply { mkdirs() }
                val id = asset.replace(Regex("[^a-zA-Z0-9]"), "_")
                val pdf = File(dir, "$id.pdf")
                if (!pdf.exists()) context.assets.open(asset).use { input -> pdf.outputStream().use { input.copyTo(it) } }
                PdfRenderer(ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY)).use { renderer ->
                    val safePage = page.coerceIn(0, renderer.pageCount - 1)
                    val output = File(dir, "$id-$safePage.png")
                    if (!output.exists()) renderer.openPage(safePage).use { p ->
                        val scale = minOf(2200f / p.width, 3000f / p.height)
                        val bmp = Bitmap.createBitmap((p.width*scale).toInt(), (p.height*scale).toInt(), Bitmap.Config.ARGB_8888)
                        bmp.eraseColor(android.graphics.Color.WHITE)
                        p.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        output.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }; bmp.recycle()
                    }
                    Triple(output, renderer.pageCount, safePage)
                }
            }
            total = rendered.second; page = rendered.third; jump = (page+1).toString(); image = rendered.first
        } catch (e: Exception) { error = "Page unavailable: ${e.message}" }
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = close) { Text("‹ Back") }
            Text(title, color = updateInk, fontSize = 13.sp, modifier = Modifier.weight(1f))
        }
        Text("Original PDF · pinch to zoom · page numbers are PDF pages", color = updateMuted, fontSize = 11.sp)
        val img = image
        if (error != null) Box(Modifier.weight(1f)) { Text(error!!, color = updateInk) }
        else if (img == null) Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        else key(img.path) {
            AndroidView(modifier = Modifier.weight(1f).fillMaxWidth(), factory = { ctx -> WebView(ctx).apply {
                setBackgroundColor(0xFF08090B.toInt()); settings.javaScriptEnabled = false
                settings.allowContentAccess = false; settings.allowFileAccess = true
                settings.builtInZoomControls = true; settings.displayZoomControls = false; settings.setSupportZoom(true)
                loadDataWithBaseURL("file://${img.parent}/", "<html><head><meta name='viewport' content='width=device-width,initial-scale=1'></head><body style='margin:0'><img style='width:100%;height:auto' src='${img.name}'></body></html>", "text/html", "UTF-8", null)
            } }, onRelease = { it.stopLoading(); it.destroy() })
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { page-- }, enabled = page > 0) { Text("‹") }
            OutlinedTextField(jump, { jump = it }, modifier = Modifier.width(65.dp), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Text("/ $total", color = updateBlue, fontSize = 12.sp)
            TextButton(onClick = { jump.toIntOrNull()?.takeIf { it in 1..total }?.let { page = it-1 } }, enabled = jump.toIntOrNull() in 1..total) { Text("Go") }
            TextButton(onClick = { page++ }, enabled = page < total-1) { Text("›") }
        }
    }
}

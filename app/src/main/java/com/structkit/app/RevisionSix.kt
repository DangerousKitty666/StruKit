package com.structkit.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebResourceRequest
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable internal fun ConnectionThumbnail(mode: Int) {
    val context = LocalContext.current
    val image = remember(mode) { context.assets.open("connection_thumbnails/$mode.png").use { BitmapFactory.decodeStream(it).asImageBitmap() } }
    Image(image, "Connection preview", Modifier.size(62.dp, 48.dp))
}

internal data class NutRow(val d: Int, val pitch: Double, val s: Double, val height: Double)
internal fun nutRows(type: String): List<NutRow> {
    val sizes = listOf(5,6,8,10,12,16,20,24)
    val across = listOf(8.0,10.0,13.0,17.0,19.0,24.0,30.0,36.0)
    val heights = when(type) {
        "Thin / jam nut" -> listOf(2.7,3.2,4.0,5.0,6.0,8.0,10.0,11.0)
        "Nylon insert locknut", "All-metal locknut", "Flange nut" -> sizes.map { it.toDouble() }
        "Cap / dome nut" -> listOf(10.0,12.0,15.0,18.0,22.0,28.0,34.0,42.0)
        else -> listOf(4.0,5.0,6.5,8.0,10.0,13.0,16.0,19.0)
    }
    return sizes.mapIndexedNotNull { i,d ->
        if(type == "Flange nut" && d == 24) null else {
            val s = when { type == "Flange nut" && d == 10 -> 15.0; type == "Flange nut" && d == 12 -> 18.0
                type == "Cap / dome nut" && d == 10 -> 16.0; type == "Cap / dome nut" && d == 12 -> 18.0; else -> across[i] }
            NutRow(d, boltRefs.first { it.diameter == d }.pitch, s, heights[i])
        }
    }
}

@Composable private fun DimensionRow(values: List<String>, header: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp)) {
        values.forEach { Text(it, color = if(header) Color(0xFF9EC5E8) else Color(0xFFD9DDE3), fontSize = 12.sp, modifier = Modifier.weight(1f)) }
    }
    HorizontalDivider(color = Color(0xFF343A43), thickness = .5.dp)
}

@Composable internal fun NutDimensionTable(type: String) {
    val standard = when(type) {
        "Thin / jam nut" -> "DIN 439"; "Nylon insert locknut" -> "DIN 985"; "All-metal locknut" -> "DIN 980 V"
        "Flange nut" -> "DIN 6923"; "Cap / dome nut" -> "DIN 1587 · Böllhoff brass range"; else -> "DIN 934"
    }
    Text("$standard · reference dimensions (mm)", color = Color(0xFF9EC5E8), fontSize = 13.sp)
    DimensionRow(listOf("Size", "Pitch", "s · A/F", "h · height"), true)
    nutRows(type).forEach { DimensionRow(listOf("M${it.d}", "${it.pitch}", "${it.s}", "${it.height}")) }
    Text("s = across opposite flats; h = total axial thickness/height, including insert collar, flange or dome. Listed catalogue reference/maximum sizes; check product tolerances. DIN 934 M10 uses s = 17 mm; ISO 4032 M10 uses 16 mm and has a different height.", color = Color(0xFF9EA6B2), fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
    val url = when(type) {
        "All-metal locknut" -> "https://eshop.boellhoff.de/out/media/pdf/DIN_980_Stahl_10_gv___de.pdf"
        "Cap / dome nut" -> "https://eshop.boellhoff.de/out/media/pdf/DIN_1587_Messing___en.pdf"
        "Flange nut" -> "https://www.dalloyedfasteners.com/pdf/din-6923-weight-chart.pdf"
        else -> "https://leytonfasteners.co.uk/nut-dimensions/"
    }
    val handler = LocalUriHandler.current
    TextButton(onClick = { handler.openUri(url) }) { Text("Dimension source ↗") }
}

@Composable internal fun WasherDimensionTable(type: String) {
    if(type == "SEAL") {
        Text("Bonded sealing washers are selected by the screw and the manufacturer's product series, not by a universal M-size table. ID, OD, metal thickness and uncompressed seal height must come from that product's data sheet.", color = Color(0xFF9EA6B2), fontSize = 12.sp)
        return
    }
    DimensionRow(listOf("Size", "OD", "ID", "Thickness"), true)
    washerSizes.forEachIndexed { i, w ->
        val values = when(type) {
            "LARGE" -> listOf(w.largeOd,w.largeBore,w.largeThick)
            "TOOTH" -> listOf(listOf(10.0,11.0,15.0,18.0,20.5,24.0,26.0,30.0,33.0,38.0,48.0)[i],w.bore,listOf(.6,.7,.8,.9,1.0,1.0,1.2,1.4,1.4,1.5,1.6)[i])
            "SPLIT" -> listOf(listOf(9.2,11.8,14.8,18.1,21.1,24.1,27.4,29.4,33.6,40.0,48.2)[i],listOf(5.1,6.1,8.1,10.2,12.2,14.2,16.2,18.2,20.2,24.5,30.5)[i],listOf(1.2,1.6,2.0,2.2,2.5,3.0,3.5,3.5,4.0,5.0,6.0)[i])
            else -> listOf(w.normalOd,w.bore,w.normalThick)
        }
        DimensionRow(listOf("M${w.d}") + values.map { "$it" })
    }
    Text(if(type == "SPLIT") "DIN 127 B: OD maximum, ID minimum, nominal material thickness (not free spring height)." else if(type == "TOOTH") "DIN 6798 A external teeth: reference OD/ID and material thickness, not overall tooth height." else "Nominal series dimensions; verify tolerances and material grade.", color = Color(0xFF9EA6B2), fontSize = 11.sp)
    val handler = LocalUriHandler.current
    val link = when(type) {
        "SPLIT" -> "https://www.fastenal.com/content/product_specifications/M.LW.127B.MZ.03.pdf"
        "TOOTH" -> "https://www.ametric.com/Images/document/Washers-Metric.pdf"
        else -> "https://www.schuster-gmbh.de/de/lieferprogramm/produkte/din-en-iso-7090/"
    }
    TextButton(onClick = { handler.openUri(link) }) { Text("Dimension reference ↗") }
}

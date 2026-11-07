package com.structkit.app

import androidx.activity.compose.BackHandler
import android.content.Intent
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

private val referenceTopics = listOf(
    "Overview" to "overview", "Colour" to "colour",
    "Air" to "air", "Dynamics" to "dynamics", "Material properties" to "properties", "Insulation" to "insulation", "Pumps" to "pumps", "Basics" to "basics", "HVAC systems" to "hvac",
    "Design formulas" to "design", "Math formulas" to "math",
    "Regs - LT" to "regs-lt", "Regs - UK" to "regs-uk",
    "ISO 14122 · Stairs, stepladders" to "access", "ISO 14120 · Guarding" to "guarding", "DW/144 · Ductwork" to "dw144", "HTM 03-01 · Ventilation" to "htm0301")
private fun topicTint(id:String):Color=when(id){
    "air","dynamics","properties","insulation","pumps","basics","hvac"->Color(0x662E88B6)
    "design","math"->Color(0x661E367B)
    "regs-lt","regs-uk"->Color(0x66426A8C)
    "access","guarding","dw144","htm0301"->Color(0x661D4C64)
    else->Color.Transparent
}

@Composable internal fun StandardsTool() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var sourcePage by remember { mutableStateOf<Int?>(null) }
    val isPdf=referenceTopics[selected].second in listOf("access","dw144","htm0301")
    BackHandler(enabled=isPdf && sourcePage==null) { selected=0;query="";expanded=false }
    val context = LocalContext.current
    val texts = remember { referenceTopics.map { (_,file) -> runCatching { context.assets.open("standards/$file.html").bufferedReader().use { it.readText() }.replace(Regex("<[^>]*>"), " ") }.getOrDefault("") } }
    val matches = remember(query) { referenceTopics.indices.filter { referenceTopics[it].first.contains(query, true) || texts[it].contains(query, true) } }
    if (sourcePage != null) {
        PublicationViewer("standards/${referenceTopics[selected].second}.pdf", referenceTopics[selected].first, sourcePage!!) { sourcePage = null }
        return
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        BasicTextField(query, { query = it }, singleLine = true,
            textStyle = TextStyle(color = Color(0xFFD9DDE3), fontSize = 14.sp),
            cursorBrush = SolidColor(Color(0xFF9EC5E8)),
            modifier = Modifier.fillMaxWidth().height(40.dp).border(1.dp, Color(0xFF48515C), RoundedCornerShape(9.dp)).padding(horizontal = 12.dp),
            decorationBox = { inner -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                if(query.isEmpty()) Text("Search standards & paragraphs", color = Color(0xFF9EA6B2), fontSize = 14.sp)
                inner()
            } })
        Spacer(Modifier.height(2.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = if(isPdf) Alignment.Bottom else Alignment.CenterVertically) {
            Text("Standards", color = Color(0xFFD9DDE3), fontSize = 27.sp, modifier = Modifier.weight(1f))
            ToolDropdown(expanded,{expanded=it}) {
                referenceTopics.forEachIndexed { i, topic -> DropdownMenuItem(modifier=Modifier.background(topicTint(topic.second)),text = { Text(topic.first, color = Color(0xFF9EC5E8), fontSize = 13.sp) }, onClick = { selected = i; expanded = false }) }
            }
        }
        if(referenceTopics[selected].second !in listOf("access","dw144","htm0301")) Text(referenceTopics[selected].first, color = Color(0xFF9EC5E8), fontSize = 13.sp)
        if(query.isNotBlank()) {
            Text("${matches.size} matching topics · open one to see matching sections", fontSize = 11.sp, color = Color(0xFF9EA6B2))
            Column(Modifier.fillMaxWidth().heightIn(max = 130.dp).fadedVerticalScroll(rememberScrollState())) {
                matches.forEach { i -> TextButton(onClick = { selected = i }) { Text(referenceTopics[i].first) } }
            }
        }
        key(selected) {
            when(val file=referenceTopics[selected].second) {
                "access","dw144","htm0301"->ContinuousPdf("standards/$file.pdf",Modifier.weight(1f),referenceTopics[selected].first)
                "colour"->ColourLibrary(Modifier.weight(1f))
                "guarding"->Column(Modifier.weight(1f)) { Text("Original ISO 14120 PDF not supplied. Existing guide retained below.",color=updateBlue,fontSize=12.sp);ReferencePage("guarding.html",Modifier.weight(1f),query) }
                else->ReferencePage("$file.html",Modifier.weight(1f).fillMaxWidth(),query)
            }
        }
    }
}

@Composable internal fun ReferencePage(file: String, modifier: Modifier, query: String = "", onSource: ((Int) -> Unit)? = null, onPin: ((String,String)->Unit)? = null, flatPanel:Boolean=false) {
    AndroidView(modifier = modifier, factory = { context ->
        WebView(context).apply {
            setBackgroundColor(if(flatPanel)android.graphics.Color.TRANSPARENT else 0xFF08090B.toInt())
            settings.javaScriptEnabled = onPin != null
            settings.allowFileAccess = true
            settings.allowContentAccess = false
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            settings.setSupportZoom(true)
            isVerticalFadingEdgeEnabled = true
            setFadingEdgeLength((14 * resources.displayMetrics.density).toInt())
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val uri = request.url
                    if(uri.scheme == "strukit" && uri.host == "pin") { onPin?.invoke(uri.getQueryParameter("label").orEmpty(),uri.getQueryParameter("value").orEmpty());return true }
                    if(uri.scheme == "strukit" && uri.host == "source") { onSource?.invoke(uri.getQueryParameter("page")?.toIntOrNull()?.coerceAtLeast(1) ?: 1); return true }
                    if(uri.scheme == "https") { context.startActivity(Intent(Intent.ACTION_VIEW, uri)); return true }
                    return uri.scheme != "file" || !uri.path.orEmpty().startsWith("/android_asset/standards/")
                }
            }
            var html = context.assets.open("standards/$file").bufferedReader().use { it.readText() }
            if(flatPanel) html += "<style>body,details,summary,.row{background:transparent!important;border:0!important;box-shadow:none!important}body{margin:0!important;padding:0!important;font-size:16px!important}.label,.value{font-size:16px!important}.row{padding:8px 0!important}h1{display:none}summary{padding-left:0!important}</style>"
            if(onPin!=null) html += """<script>document.querySelectorAll('.row').forEach(function(r){r.style.touchAction='manipulation';r.addEventListener('dblclick',function(e){e.preventDefault();location.href='strukit://pin?label='+encodeURIComponent(r.querySelector('.label').innerText)+'&value='+encodeURIComponent(r.querySelector('.value').innerText || 'See source marker');});});</script>"""
            if(query.isNotBlank()) {
                html = Regex("<details([^>]*)>(.*?)</details>", setOf(RegexOption.DOT_MATCHES_ALL)).replace(html) { m ->
                    if(m.groupValues[2].replace(Regex("<[^>]*>"), " ").contains(query, true)) "<details open${m.groupValues[1]}>${m.groupValues[2]}</details>" else m.value
                }
            }
            val subdir = file.substringBeforeLast('/', "").let { if(it.isEmpty()) "" else "$it/" }
            loadDataWithBaseURL("file:///android_asset/standards/$subdir", html, "text/html", "UTF-8", null)
        }
    }, onRelease = { it.stopLoading(); it.destroy() })
}

@Composable internal fun MaterialsTool() { MetalsPilot() }
@Composable private fun PreviousMaterialsTool() {
    var selected by rememberSaveable { mutableStateOf("Steel") }
    val names = listOf("Steel", "Aluminium", "Polymers", "Wood")
    Row(Modifier.fillMaxSize().padding(start = 12.dp, end = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Text("Material library", fontSize = 27.sp, color = Color(0xFFD9DDE3))
            Text("Physical properties · SI units", fontSize = 12.sp, color = Color(0xFF9EA6B2))
            key(selected) { ReferencePage("materials-${selected.lowercase()}.html", Modifier.weight(1f).fillMaxWidth()) }
        }
        ToolTabRail(names, selected, { if(it == "Aluminium") "Alu" else it }) { selected = it }
    }
}

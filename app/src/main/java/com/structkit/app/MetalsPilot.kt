package com.structkit.app

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

private data class MetalRecord(val id:Int,val name:String,val folders:List<String>,val pages:Int)
private val metalBlue=Color(0xFF9EC5E8)
private val metalInk=Color(0xFFD9DDE3)

@Composable internal fun MetalsPilot(pins: MutableList<SectionPin> = remember { mutableStateListOf<SectionPin>() }) {
    val context=LocalContext.current
    val records=remember {
        val data=JSONArray(context.assets.open("standards/metals/index.json").bufferedReader().use{it.readText()})
        (0 until data.length()).map { i -> val r=data.getJSONObject(i);val fs=r.getJSONArray("folders")
            MetalRecord(r.getInt("id"),r.getString("name"),(0 until fs.length()).map{fs.getString(it)},r.getInt("pages")) }
    }
    var gradePage by rememberSaveable { mutableStateOf<String?>(null) }
    var gradeMenu by remember { mutableStateOf(false) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var pinFull by remember { mutableStateOf(false) }
    var source by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var expanded by rememberSaveable { mutableStateOf(listOf("Metals and alloys")) }
    BackHandler(selected!=0 || gradePage!=null) { if(gradePage!=null)gradePage=null else if(source)source=false else selected=0 }
    Column(Modifier.fillMaxSize().padding(horizontal=12.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            if(selected!=0 || gradePage!=null) TextButton(onClick={if(gradePage!=null)gradePage=null else if(source)source=false else selected=0}) {Text("‹ Library",color=metalBlue,fontSize=19.sp)}
            Text(if(selected==0) "Material library" else if(source) "Original datasheet" else "Material library",color=metalInk,fontSize=19.sp,modifier=Modifier.weight(1f))
        Box {
            OutlinedButton(onClick={gradeMenu=true},border=androidx.compose.foundation.BorderStroke(1.dp,metalBlue),shape=RoundedCornerShape(9.dp)) { Text("Grade comparisons ▾",color=metalBlue,fontSize=12.sp) }
            DropdownMenu(gradeMenu,{gradeMenu=false},containerColor=Color.Black) {
                listOf("Steel grades" to "grades-steel.html","Concrete classes" to "grades-concrete.html","Timber classes" to "grades-timber.html").forEach { (label,file) ->
                    DropdownMenuItem(text={Text(label,color=metalBlue)},onClick={gradePage=file;gradeMenu=false})
                }
            }
        }
        }
        ComparisonTray(pins,{pins.remove(it);pinFull=false}) { p ->
            p.sourceId?.let { id -> ReferencePage("metals/$id.html",Modifier.fillMaxWidth().height(390.dp),flatPanel=true) }
        }
        if(pinFull)Text("Three pins already selected. Double-tap a tile to remove it.",color=metalBlue,fontSize=11.sp)
        if(gradePage!=null) {
            key(gradePage) { ReferencePage(gradePage!!,Modifier.weight(1f).fillMaxWidth()) }
        } else if(selected==0) {
            Text("${records.size} material records · properties & original sources",color=metalBlue,fontSize=12.sp)
            OutlinedTextField(query,{query=it},placeholder={Text("Find a material or family")},singleLine=true,modifier=Modifier.fillMaxWidth().padding(vertical=8.dp),shape=RoundedCornerShape(10.dp))
            val scroll=rememberLazyListState()
            LazyColumn(Modifier.weight(1f).fillMaxWidth().edgeFade(start={scroll.canScrollBackward},end={scroll.canScrollForward}),state=scroll,verticalArrangement=Arrangement.spacedBy(7.dp)) {
                if(query.isNotBlank()) {
                    val matches=records.filter{(it.name+" "+it.folders.joinToString(" ")).contains(query,true)}
                    if(matches.isEmpty())item{Text("No matching materials",color=metalInk)}
                    items(matches,key={it.id}){r->MetalRecordButton(r,0){selected=r.id}}
                } else {
                    materialTree(records, emptyList(), expanded,
                        { path -> expanded=if(path in expanded)expanded-path else expanded+path },
                        { selected=it })
                }
                item{Text("Property ranges and diagrams from your supplied datasheets. Original PDFs are included for reference.",fontSize=11.sp,color=Color(0xFFA5AFBB),modifier=Modifier.padding(vertical=12.dp))}
            }
        } else {
            val record=records.first{it.id==selected}
            if(source) { key(selected){MaterialSourcePdf(record,Modifier.weight(1f))} }
            else {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                    Text(record.folders.joinToString(" › "),color=metalBlue,fontSize=11.sp,modifier=Modifier.weight(1f))

                }
                key(selected){ReferencePage("metals/${record.id}.html",Modifier.weight(1f).fillMaxWidth(),onSource={source=true},onPin={label,value->
                    val pin=SectionPin(record.name,label,value,record.id)
                    if(pin !in pins) { if(pins.size<3)pins.add(pin) else pinFull=true }
                })}
            }
        }
    }
}


private fun LazyListScope.materialTree(records: List<MetalRecord>, path: List<String>, expanded: List<String>, toggle: (String)->Unit, choose:(Int)->Unit) {
    val depth=path.size
    val candidates=records.filter { it.folders.take(depth)==path }
    candidates.filter { it.folders.size==depth }.forEach { record ->
        item(key="record-${record.id}") { MetalRecordButton(record,depth.coerceAtMost(3)) { choose(record.id) } }
    }
    candidates.mapNotNull { it.folders.getOrNull(depth) }.distinct().forEach { folder ->
        val next=path+folder
        val key=next.joinToString("/")
        item(key="folder-$key") { MetalFolder(folder,key in expanded,depth.coerceAtMost(3),next.first()) { toggle(key) } }
        if(key in expanded) materialTree(records,next,expanded,toggle,choose)
    }
}

@Composable private fun MetalFolder(name:String,open:Boolean,depth:Int,family:String,click:()->Unit) {
    val tint=materialFamilyTint(family)
    Text((if(open) "▾  " else "▸  ")+name,color=tint,fontSize=16.sp,
        modifier=Modifier.fillMaxWidth().padding(start=(depth*12).dp).background(tint.copy(alpha=if(depth==0).20f else .10f),RoundedCornerShape(10.dp)).border(1.dp,tint.copy(alpha=.65f),RoundedCornerShape(10.dp)).clickable(onClick=click).padding(13.dp))
}
@Composable private fun MetalRecordButton(r:MetalRecord,depth:Int,click:()->Unit) {
    Column(Modifier.fillMaxWidth().padding(start=(depth*12).dp).background(materialFamilyTint(r.folders.firstOrNull().orEmpty()).copy(alpha=.08f),RoundedCornerShape(9.dp)).border(1.dp,materialFamilyTint(r.folders.firstOrNull().orEmpty()).copy(alpha=.35f),RoundedCornerShape(9.dp)).clickable(onClick=click).padding(12.dp)) {
        Text(r.name,color=metalInk,fontSize=15.sp)
        Text("Properties · diagrams · source",color=Color(0xFFA5AFBB),fontSize=10.sp)
    }
}

@Composable private fun MaterialSourcePdf(record:MetalRecord,modifier:Modifier) {
    val context=LocalContext.current
    var page by rememberSaveable{mutableIntStateOf(0)}
    var imagePath by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(page) {
        imagePath=null;error=null
        try {
            imagePath=withContext(Dispatchers.IO) {
                val dir=File(context.cacheDir,"metal-pdf").apply{mkdirs()}
                val pdf=File(dir,"${record.id}.pdf")
                if(!pdf.exists())context.assets.open("standards/metals/${record.id}.pdf").use{input->pdf.outputStream().use{input.copyTo(it)}}
                val image=File(dir,"${record.id}-$page.png")
                if(!image.exists()) {
                    PdfRenderer(ParcelFileDescriptor.open(pdf,ParcelFileDescriptor.MODE_READ_ONLY)).use { renderer ->
                        renderer.openPage(page).use { p ->
                            val bmp=Bitmap.createBitmap(1800,(1800f*p.height/p.width).toInt(),Bitmap.Config.ARGB_8888)
                            bmp.eraseColor(android.graphics.Color.WHITE)
                            p.render(bmp,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            image.outputStream().use{bmp.compress(Bitmap.CompressFormat.PNG,100,it)};bmp.recycle()
                        }
                    }
                }
                image.absolutePath
            }
        } catch(e:Exception) {error="Could not display this page: ${e.message}"}
    }
    Column(modifier.fillMaxWidth()) {
        Text(record.name,color=metalInk,fontSize=14.sp)
        Text("Original source page · pinch to zoom",color=Color(0xFFA5AFBB),fontSize=11.sp)
        if(error!=null) Box(Modifier.weight(1f)){Text(error!!,color=metalInk)}
        else if(imagePath==null)Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center){CircularProgressIndicator()}
        else key(imagePath) {AndroidView(modifier=Modifier.weight(1f).fillMaxWidth(),factory={ctx->WebView(ctx).apply{
            setBackgroundColor(0xFF08090B.toInt());settings.javaScriptEnabled=false;settings.allowContentAccess=false;settings.allowFileAccess=true
            settings.builtInZoomControls=true;settings.displayZoomControls=false;settings.setSupportZoom(true)
            loadDataWithBaseURL("file://${File(imagePath!!).parent}/","<html><head><meta name='viewport' content='width=device-width,initial-scale=1'></head><body style='margin:0'><img style='width:100%;height:auto' src='${File(imagePath!!).name}'></body></html>","text/html","UTF-8",null)
        }},onRelease={it.stopLoading();it.destroy()})}
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
            TextButton(onClick={page--},enabled=page>0){Text("‹ Previous")}
            Text("${page+1} / ${record.pages}",color=metalBlue,fontSize=13.sp)
            TextButton(onClick={page++},enabled=page<record.pages-1){Text("Next ›")}
        }
    }
}

private fun materialFamilyTint(name:String):Color = when {
 name.contains("metal",true)->Color(0xFF91C8F0)
 name.contains("polymer",true)||name.contains("plastic",true)->Color(0xFFC4A1E8)
 name.contains("ceramic",true)||name.contains("glass",true)->Color(0xFFE6B277)
 name.contains("hybrid",true)||name.contains("wood",true)->Color(0xFF8CD0A1)
 else->Color(0xFF7ACFD4)
}

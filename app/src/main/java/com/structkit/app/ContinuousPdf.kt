package com.structkit.app
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.webkit.WebView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File

@Composable internal fun ContinuousPdf(asset:String,modifier:Modifier=Modifier) {
    val context=LocalContext.current
    var query by remember(asset){mutableStateOf("")};var hit by remember(asset){mutableIntStateOf(0)}
    val scope=rememberCoroutineScope();val scroll=rememberLazyListState()
    var pages by remember(asset){mutableStateOf<List<String>>(emptyList())}
    var pdf by remember(asset){mutableStateOf<File?>(null)};var error by remember(asset){mutableStateOf<String?>(null)}
    var jump by remember(asset){mutableStateOf("")}
    var pageError by remember(asset){mutableStateOf<String?>(null)}
    var zoom by remember(asset){mutableStateOf<File?>(null)}
    LaunchedEffect(asset){
        try { val result=withContext(Dispatchers.IO){
            val dir=File(context.cacheDir,"pdf725").apply{mkdirs()};val f=File(dir,asset.substringAfterLast('/'))
            if(!f.exists()) context.assets.open(asset).use{input->f.outputStream().use{input.copyTo(it)}}
            val data=JSONArray(context.assets.open(asset.removeSuffix(".pdf")+".search.json").bufferedReader().use{it.readText()})
            f to (0 until data.length()).map{data.getString(it)}
        };pdf=result.first;pages=result.second }catch(e:Exception){error="Cannot open PDF: ${e.message}"}
    }
    val matches=remember(query,pages){if(query.isBlank())emptyList() else pages.indices.filter{pages[it].contains(query,true)}}
    Column(modifier.fillMaxSize()) {
        error?.let{Text(it,color=updateInk)}
        if(pdf==null&&error==null)CircularProgressIndicator()
        pdf?.let{file->LazyColumn(state=scroll,modifier=Modifier.weight(1f).fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            items(pages.indices.toList(),key={it}) { index ->
                PdfScrollPage(file,index){zoom=it}
                Text("${index+1} / ${pages.size}",color=updateMuted,fontSize=10.sp)
            }
        }}
        Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
            OutlinedTextField(query,{query=it;hit=0},singleLine=true,label={Text("Search")},modifier=Modifier.weight(1f))
            Spacer(Modifier.width(6.dp))
            OutlinedTextField(jump,{jump=it;pageError=null},singleLine=true,label={Text("Page / ${pages.size}")},modifier=Modifier.weight(.7f),keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=androidx.compose.ui.text.input.KeyboardType.Number))
            TextButton(onClick={val n=jump.toIntOrNull();if(n!=null&&n in 1..pages.size){scope.launch{scroll.scrollToItem(n-1)};pageError=null}else pageError="Enter page 1–${pages.size}"},enabled=pages.isNotEmpty()){Text("Go")}
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Text(pageError?:if(query.isBlank())"PDF page ${scroll.firstVisibleItemIndex+1} / ${pages.size} · tap to zoom" else "${matches.size} matching pages",color=updateMuted,fontSize=11.sp)
            if(matches.isNotEmpty())TextButton(onClick={scope.launch{scroll.scrollToItem(matches[hit % matches.size]);hit++}}){Text("Next match")}
        }
    }
    zoom?.let{file->Dialog(onDismissRequest={zoom=null},properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(Modifier.fillMaxSize(),color=androidx.compose.ui.graphics.Color.Black){Column {
            TextButton(onClick={zoom=null}){Text("Close zoom")}
            AndroidView(modifier=Modifier.weight(1f).fillMaxWidth(),factory={ctx->WebView(ctx).apply{
                settings.javaScriptEnabled=false;settings.allowFileAccess=true;settings.builtInZoomControls=true;settings.displayZoomControls=false
                loadDataWithBaseURL("file://${file.parent}/","<meta name='viewport' content='width=device-width,initial-scale=1'><img style='width:100%' src='${file.name}'>","text/html","UTF-8",null)
            }},onRelease={it.destroy()})
        }}
    }}
}
@Composable private fun PdfScrollPage(file:File,index:Int,zoom:(File)->Unit) {
    var bitmap by remember(file,index){mutableStateOf<Bitmap?>(null)};var rendered by remember(file,index){mutableStateOf<File?>(null)}
    var error by remember(file,index){mutableStateOf<String?>(null)}
    LaunchedEffect(file,index){try {
        val result=withContext(Dispatchers.IO){
            val out=File(file.parentFile,"${file.nameWithoutExtension}-$index.png")
            if(!out.exists())PdfRenderer(ParcelFileDescriptor.open(file,ParcelFileDescriptor.MODE_READ_ONLY)).use{r->r.openPage(index).use{p->
                val scale=minOf(1800f/p.width,2500f/p.height)
                val b=Bitmap.createBitmap((p.width*scale).toInt(),(p.height*scale).toInt(),Bitmap.Config.ARGB_8888)
                b.eraseColor(android.graphics.Color.WHITE);p.render(b,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                out.outputStream().use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()
            }}
            out to android.graphics.BitmapFactory.decodeFile(out.path)
        };rendered=result.first;bitmap=result.second
    }catch(e:Exception){error="Page ${index+1}: ${e.message}"}}
    val b=bitmap
    if(b!=null)Image(b.asImageBitmap(),"Original PDF page ${index+1}",Modifier.fillMaxWidth().aspectRatio(b.width.toFloat()/b.height).clickable{rendered?.let(zoom)})
    else Box(Modifier.fillMaxWidth().height(420.dp)){Text(error?:"Loading page ${index+1}…",color=updateMuted)}
}

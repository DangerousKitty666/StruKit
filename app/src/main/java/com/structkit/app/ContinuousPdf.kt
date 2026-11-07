package com.structkit.app
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
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

@Composable internal fun ContinuousPdf(asset:String,modifier:Modifier=Modifier,title:String="") {
    val context=LocalContext.current
    val preferences=remember { context.getSharedPreferences("pdf-viewer",android.content.Context.MODE_PRIVATE) }
    var brightness by remember { mutableFloatStateOf(preferences.getFloat("brightness",.8f).coerceIn(.25f,1f)) }
    var showBrightness by remember { mutableStateOf(false) }
    var barHeight by remember { mutableIntStateOf(0) }
    val density=LocalDensity.current
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
        Text(title,color=updateBlue,fontSize=13.8.sp,maxLines=2)
        error?.let{Text(it,color=updateInk)}
        if(pdf==null&&error==null)CircularProgressIndicator()
        Box(Modifier.weight(1f).fillMaxWidth()) {
            pdf?.let { file ->
                LazyColumn(state=scroll,modifier=Modifier.fillMaxSize().drawWithContent{drawContent();drawRect(Color.Black.copy(alpha=1f-brightness))},
                    contentPadding=PaddingValues(bottom=with(density){barHeight.toDp()}),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    items(pages.indices.toList(),key={it}) { index -> PdfScrollPage(file,index){zoom=it} }
                }
            }
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().onSizeChanged{barHeight=it.height}) {
                Box(Modifier.fillMaxWidth().height(12.dp).background(Brush.verticalGradient(listOf(Color.Transparent,Color(0xEB08090B)))))
                Column(Modifier.fillMaxWidth().background(Color(0xEB08090B)).padding(start=2.dp,end=2.dp,bottom=2.dp)) {
                    if(showBrightness) Row(Modifier.fillMaxWidth().height(40.dp),verticalAlignment=Alignment.CenterVertically) {
                        Text("PDF ${(brightness*100).toInt()}%",color=updateMuted,fontSize=11.sp)
                        Slider(value=brightness,onValueChange={brightness=it;preferences.edit().putFloat("brightness",it).apply()},valueRange=.25f..1f,
                            modifier=Modifier.weight(1f).semantics{contentDescription="PDF brightness"})
                    }
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        CompactPdfField(query,{query=it;hit=0},"search in off",Modifier.weight(1f))
                        Spacer(Modifier.width(5.dp))
                        Column(Modifier.width(64.dp),horizontalAlignment=Alignment.CenterHorizontally) {
                            Text(if(pages.isEmpty()) "– / –" else "${scroll.firstVisibleItemIndex+1} / ${pages.size}",color=updateMuted,fontSize=10.sp)
                            CompactPdfField(jump,{jump=it;pageError=null},"Page",Modifier.fillMaxWidth(),true)
                        }
                        TextButton(modifier=Modifier.width(38.dp).height(36.dp),contentPadding=PaddingValues(0.dp),onClick={val n=jump.toIntOrNull();if(n!=null&&n in 1..pages.size){scope.launch{scroll.scrollToItem(n-1)};pageError=null}else pageError="Enter page 1–${pages.size}"},enabled=pages.isNotEmpty()){Text("Go",fontSize=12.sp)}
                        TextButton(modifier=Modifier.width(36.dp).height(36.dp).semantics{contentDescription="Adjust PDF brightness"},contentPadding=PaddingValues(0.dp),onClick={showBrightness=!showBrightness}){Text("☀",fontSize=20.sp)}
                    }
                    if(pageError!=null||query.isNotBlank()) Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
                        Text(pageError?:"${matches.size} matching pages",color=updateMuted,fontSize=11.sp)
                        if(matches.isNotEmpty())TextButton(modifier=Modifier.height(32.dp),contentPadding=PaddingValues(horizontal=8.dp,vertical=0.dp),onClick={scope.launch{scroll.scrollToItem(matches[hit % matches.size]);hit++}}){Text("Next match")}
                    }
                }
            }
        }
    }
    zoom?.let{file->Dialog(onDismissRequest={zoom=null},properties=DialogProperties(usePlatformDefaultWidth=false)) {
        Surface(Modifier.fillMaxSize(),color=androidx.compose.ui.graphics.Color.Black){Column {
            TextButton(onClick={zoom=null}){Text("Close zoom")}
            AndroidView(modifier=Modifier.weight(1f).fillMaxWidth().drawWithContent{drawContent();drawRect(Color.Black.copy(alpha=1f-brightness))},factory={ctx->WebView(ctx).apply{
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

@Composable private fun CompactPdfField(value:String,onChange:(String)->Unit,hint:String,modifier:Modifier,numeric:Boolean=false) {
    BasicTextField(value,onChange,singleLine=true,textStyle=TextStyle(color=updateInk,fontSize=13.sp),cursorBrush=SolidColor(updateBlue),
        keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=if(numeric)androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Text),
        modifier=modifier.height(36.dp).border(1.dp,updateMuted,RoundedCornerShape(5.dp)).padding(horizontal=10.dp),
        decorationBox={inner->Box(Modifier.fillMaxSize(),contentAlignment=Alignment.CenterStart){if(value.isEmpty())Text(hint,color=updateMuted,fontSize=13.sp);inner()}})
}

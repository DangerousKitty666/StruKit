package com.structkit.app
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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

@Composable internal fun ContinuousPdf(asset:String,modifier:Modifier=Modifier,title:String="",externalQuery:String="",navigation:Int=0) {
    val context=LocalContext.current
    var words by remember(asset){mutableStateOf<List<List<PdfWord>>>(emptyList())}
    var contentsPage by remember(asset){mutableStateOf<Int?>(null)}
    var brightness by remember(asset) { mutableFloatStateOf(.45f) }
    var showBrightness by remember { mutableStateOf(false) }
    var barHeight by remember { mutableIntStateOf(0) }
    val density=LocalDensity.current
    var query by remember(asset){mutableStateOf("")};var hit by remember(asset){mutableIntStateOf(0)}
    val scope=rememberCoroutineScope();val scroll=rememberLazyListState()
    var pages by remember(asset){mutableStateOf<List<String>>(emptyList())}
    var pdf by remember(asset){mutableStateOf<File?>(null)};var error by remember(asset){mutableStateOf<String?>(null)}
    var pageLabels by remember(asset){mutableStateOf<List<String>>(emptyList())}
    var usePrinted by remember(asset){mutableStateOf(true)}
    var jump by remember(asset){mutableStateOf("")}
    var pageError by remember(asset){mutableStateOf<String?>(null)}
    var zoom by remember(asset){mutableStateOf<File?>(null)}
    LaunchedEffect(asset){
        try { val result=withContext(Dispatchers.IO){
            val dir=File(context.cacheDir,"pdf725").apply{mkdirs()};val f=File(dir,asset.substringAfterLast('/'))
            if(!f.exists()) context.assets.open(asset).use{input->f.outputStream().use{input.copyTo(it)}}
            val data=JSONArray(context.assets.open(asset.removeSuffix(".pdf")+".search.json").bufferedReader().use{it.readText()})
            f to (0 until data.length()).map{data.getString(it)}
        };pdf=result.first;pages=result.second
            pageLabels=runCatching { val o=org.json.JSONObject(context.assets.open(asset.removeSuffix(".pdf")+".labels.json").bufferedReader().use{it.readText()});val a=o.getJSONArray("labels");List(a.length()){a.optString(it)} }.getOrElse{inferPdfLabels(pages)} }catch(e:Exception){error="Cannot open PDF: ${e.message}"}
    }
    LaunchedEffect(asset){withContext(Dispatchers.IO){runCatching{
        val data=org.json.JSONObject(context.assets.open(asset.removeSuffix(".pdf")+".words.json").bufferedReader().use{it.readText()})
        val a=data.getJSONArray("pages")
        val parsed=List(a.length()){i->val page=a.getJSONArray(i);List(page.length()){n->val w=page.getJSONArray(n);PdfWord(w.getString(0),w.getDouble(1).toFloat(),w.getDouble(2).toFloat(),w.getDouble(3).toFloat(),w.getDouble(4).toFloat())}}
        withContext(Dispatchers.Main){words=parsed;contentsPage=if(data.isNull("contents"))null else data.getInt("contents")}
    }}}
    LaunchedEffect(externalQuery){query=externalQuery;hit=0}
    val matches=remember(query,pages){if(query.isBlank())emptyList() else pages.indices.filter{searchRanges(pages[it],query).isNotEmpty()}}
    LaunchedEffect(query,pages){hit=0;if(matches.isNotEmpty())scroll.scrollToItem(matches[0])}
    var previousNav by remember{mutableIntStateOf(navigation)}
    LaunchedEffect(navigation){if(matches.isNotEmpty()&&navigation!=previousNav){hit=Math.floorMod(hit+if(navigation>previousNav)1 else -1,matches.size);scroll.scrollToItem(matches[hit])};previousNav=navigation}
    Column(modifier.fillMaxSize()) {
        Text(title,color=updateBlue,fontSize=13.8.sp,maxLines=2)
        error?.let{Text(it,color=updateInk)}
        if(pdf==null&&error==null)CircularProgressIndicator()
        Box(Modifier.weight(1f).fillMaxWidth().clip(RoundedCornerShape(14.dp))) {
            pdf?.let { file ->
                LazyColumn(state=scroll,modifier=Modifier.fillMaxSize().drawWithContent{drawContent();drawRect(Color.Black.copy(alpha=1f-brightness))},
                    contentPadding=PaddingValues(bottom=with(density){barHeight.toDp()}),verticalArrangement=Arrangement.spacedBy(10.dp)) {
                    items(pages.indices.toList(),key={it}) { index -> PdfScrollPage(file,index,query,words.getOrNull(index).orEmpty()){zoom=it} }
                }
            }
            Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().onSizeChanged{barHeight=it.height}) {
                Box(Modifier.fillMaxWidth().height(12.dp).background(Brush.verticalGradient(listOf(Color.Transparent,Color(0xB008090B)))))
                Column(Modifier.fillMaxWidth().background(Color(0xB008090B)).padding(start=2.dp,end=2.dp,bottom=2.dp)) {
                    if(showBrightness) Row(Modifier.fillMaxWidth().height(40.dp),verticalAlignment=Alignment.CenterVertically) {
                        Text("PDF ${(brightness*100).toInt()}%",color=updateMuted,fontSize=11.sp)
                        Slider(value=brightness,onValueChange={brightness=it},valueRange=.25f..1f,
                            modifier=Modifier.weight(1f).semantics{contentDescription="PDF brightness"})
                    }
                    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                        CompactPdfField(query,{query=it;hit=0},"search in pdf",Modifier.weight(1f))
                        TextButton(enabled=contentsPage!=null,modifier=Modifier.width(62.dp).height(36.dp),contentPadding=PaddingValues(0.dp),onClick={contentsPage?.let{scope.launch{scroll.scrollToItem(it)}}}){Text("Contents",fontSize=10.sp)}
                        Text(if(pages.isEmpty()) "– / –" else if(usePrinted) "${pageLabels.getOrNull(scroll.firstVisibleItemIndex).orEmpty().ifEmpty{"—"}}\nPDF ${scroll.firstVisibleItemIndex+1}/${pages.size}" else "PDF\n${scroll.firstVisibleItemIndex+1}/${pages.size}",color=updateMuted,fontSize=9.sp,modifier=Modifier.padding(horizontal=4.dp).clickable{usePrinted=!usePrinted})
                        CompactPdfField(jump,{jump=it;pageError=null},if(usePrinted)"Page" else "PDF",Modifier.width(54.dp),false)
                        TextButton(modifier=Modifier.width(38.dp).height(36.dp),contentPadding=PaddingValues(0.dp),onClick={val index=if(usePrinted)pageLabels.indexOfFirst{it.isNotBlank()&&it.equals(jump.trim(),true)} else (jump.toIntOrNull()?.minus(1) ?: -1);if(index in pages.indices){scope.launch{scroll.scrollToItem(index)};pageError=null}else pageError="Page not found. Tap counter to switch printed / PDF pages."},enabled=pages.isNotEmpty()){Text("Go",fontSize=12.sp)}
                        TextButton(modifier=Modifier.width(36.dp).height(36.dp).semantics{contentDescription="Adjust PDF brightness"},contentPadding=PaddingValues(0.dp),onClick={showBrightness=!showBrightness}){Canvas(Modifier.size(18.dp)){drawCircle(updateBlue,style=androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()));drawArc(updateBlue,90f,180f,true,topLeft=Offset(1.dp.toPx(),1.dp.toPx()),size=Size(size.width-2.dp.toPx(),size.height-2.dp.toPx()))}}
                    }
                    if(pageError!=null||query.isNotBlank()) Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween) {
                        Text(pageError?:"${matches.size} matching pages",color=updateMuted,fontSize=11.sp)
                        if(matches.isNotEmpty())TextButton(modifier=Modifier.height(32.dp),contentPadding=PaddingValues(horizontal=8.dp,vertical=0.dp),onClick={scope.launch{hit=(hit+1)%matches.size;scroll.scrollToItem(matches[hit])}}){Text("Next match")}
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
@Composable private fun PdfScrollPage(file:File,index:Int,query:String,words:List<PdfWord>,zoom:(File)->Unit) {
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
    if(b!=null)Box(Modifier.fillMaxWidth().aspectRatio(b.width.toFloat()/b.height).clickable{rendered?.let(zoom)}){
        Image(b.asImageBitmap(),"Original PDF page ${index+1}",Modifier.fillMaxSize())
        val highlights=remember(words,query){pdfWordMatches(words,query)}
        Canvas(Modifier.fillMaxSize()){highlights.forEach{w->drawRect(Color(0x99FFCE45),Offset(w.left*size.width,w.top*size.height),Size((w.right-w.left)*size.width,(w.bottom-w.top)*size.height))}}
    }
    else Box(Modifier.fillMaxWidth().height(420.dp)){Text(error?:"Loading page ${index+1}…",color=updateMuted)}
}

@Composable private fun CompactPdfField(value:String,onChange:(String)->Unit,hint:String,modifier:Modifier,numeric:Boolean=false) {
    BasicTextField(value,onChange,singleLine=true,textStyle=TextStyle(color=updateInk,fontSize=13.sp),cursorBrush=SolidColor(updateBlue),
        keyboardOptions=androidx.compose.foundation.text.KeyboardOptions(keyboardType=if(numeric)androidx.compose.ui.text.input.KeyboardType.Number else androidx.compose.ui.text.input.KeyboardType.Text),
        modifier=modifier.height(36.dp).border(1.dp,updateMuted,RoundedCornerShape(5.dp)).padding(horizontal=10.dp),
        decorationBox={inner->Box(Modifier.fillMaxSize(),contentAlignment=Alignment.CenterStart){if(value.isEmpty())Text(hint,color=updateMuted,fontSize=13.sp);inner()}})
}

// Fallback for newly bundled PDFs without a label sidecar. Require a repeated offset,
// never infer from a single number in a table or paragraph.
internal fun inferPdfLabels(pages:List<String>):List<String> {
    val votes=mutableMapOf<Int,Int>()
    pages.forEachIndexed { i,text ->
        val lines=text.lines().map{it.trim()}.filter{it.isNotEmpty()}
        (lines.take(3)+lines.takeLast(5)).mapNotNull{it.toIntOrNull()}.filter{it in 1..pages.size}.distinct().forEach{n->val offset=i+1-n;votes[offset]=(votes[offset]?:0)+1}
    }
    val candidate=votes.maxByOrNull{it.value}
    if(candidate==null||candidate.value<maxOf(3,pages.size/5))return List(pages.size){""}
    return List(pages.size){i->if(i+1>candidate.key)(i+1-candidate.key).toString() else ""}
}


package com.structkit.app
import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.json.JSONArray

private data class ColourSample(val system:String,val code:String,val name:String,val hex:String)
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun ColourLibrary(modifier:Modifier=Modifier) {
    val context=LocalContext.current
    val colours=remember{val a=JSONArray(context.assets.open("colours.json").bufferedReader().use{it.readText()});(0 until a.length()).map{i->val r=a.getJSONObject(i);ColourSample(r.getString("system"),r.getString("code"),r.getString("name"),r.getString("hex"))}}
    var system by remember{mutableStateOf("RAL Classic")};var query by remember{mutableStateOf("")};var full by remember{mutableStateOf<ColourSample?>(null)}
    Column(modifier.fillMaxSize()) {
        ToggleChips(listOf("RAL Classic","BS 4800","BS 381C","Identification"),system){system=it}
        if(system=="Identification") {
            Text("UK / European colour identification",color=updateBlue,fontSize=18.sp)
            Text("RAL Classic: four-digit reference used across Europe for coatings. RAL Design and RAL Effect are different collections. BS 4800 identifies building paint colours; BS 381C uses three-digit references for specified applications. NCS describes perceived blackness, chromaticness and hue, e.g. S 0502-Y. These systems have no universal exact one-to-one conversion. Specify the system, code, finish and physical sample.",color=updateInk,fontSize=14.sp)
            SourceLink("RAL systems","https://www.ral-farben.de/overview-ral-colour-palettes.pdfx")
            SourceLink("NCS notation","https://plus-support.ncscolour.com/using-the-ncs-navigator")
            SourceLink("RAL additions · Koyda", "https://www.koyda.com/guides/ral-colour-chart/")
            SourceLink("UK paint charts · Promain","https://www.promain.co.uk/colour-charts-bs4800")
        } else {
            Text("Digital approximations · double-tap to fill screen; double-tap again to exit. Confirm paint with a physical swatch; screen, lighting and sheen change appearance.",color=updateMuted,fontSize=11.sp)
            OutlinedTextField(query,{query=it},singleLine=true,label={Text("Code or colour name")},modifier=Modifier.fillMaxWidth())
            val filtered=colours.filter{it.system==system&&(it.code+" "+it.name).contains(query,true)}
            Text("${filtered.size} swatches · published reference swatches",color=updateMuted,fontSize=10.sp)
            LazyVerticalGrid(columns=GridCells.Fixed(2),modifier=Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                items(filtered,key={it.system+it.code}){c->Column(Modifier.combinedClickable(onClick={},onDoubleClick={full=c}).padding(4.dp)) {
                    Box(Modifier.fillMaxWidth().height(68.dp).background(Color(android.graphics.Color.parseColor(c.hex))))
                    Text(c.code,color=updateBlue,fontSize=13.sp);Text(c.name,color=updateInk,fontSize=11.sp)
                }}
            }
        }
    }
    full?.let { c->Dialog(onDismissRequest={full=null},properties=DialogProperties(usePlatformDefaultWidth=false,decorFitsSystemWindows=false)) {
        val view=androidx.compose.ui.platform.LocalView.current
        DisposableEffect(view){
            val window=(view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
            val controller=window?.let{androidx.core.view.WindowCompat.getInsetsController(it,view)}
            controller?.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            onDispose { controller?.show(androidx.core.view.WindowInsetsCompat.Type.systemBars()) }
        }
        Box(Modifier.fillMaxSize().background(Color(android.graphics.Color.parseColor(c.hex))).combinedClickable(onClick={},onDoubleClick={full=null}))
    }}
}

package com.structkit.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class SectionPin(val section: String, val property: String, val value: String, val sourceId: Int? = null)
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun ComparisonTray(pins: List<SectionPin>, remove: (SectionPin) -> Unit, details: (@Composable (SectionPin) -> Unit)? = null) {
    var selected by remember { mutableStateOf<SectionPin?>(null) }
    if (pins.isEmpty()) Text("Double-tap a property to pin · up to 3 · double-tap tile to unpin", color=updateMuted,fontSize=10.sp,modifier=Modifier.padding(6.dp))
    else Row(Modifier.fillMaxWidth().padding(4.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
        (0 until 3).forEach { index ->
            val pin=pins.getOrNull(index)
            if(pin==null) { Spacer(Modifier.weight(1f));return@forEach }
            Column(Modifier.weight(1f).border(1.dp,updateBlue,RoundedCornerShape(8.dp)).background(Color(0xFF17212B),RoundedCornerShape(8.dp))
                .combinedClickable(onClick={selected=pin},onDoubleClick={remove(pin)}).padding(6.dp)) {
                Text(pin.section,color=updateBlue,fontSize=11.sp,maxLines=2)
                Text(pin.property,color=updateMuted,fontSize=10.sp,maxLines=2)
                Text(pin.value,color=updateInk,fontSize=12.sp,maxLines=2)
            }
        }
    }
    selected?.let { p ->
        val density=androidx.compose.ui.platform.LocalDensity.current
        val maxHeight=androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp*.72f
        androidx.compose.ui.window.Dialog(onDismissRequest={selected=null},properties=androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth=false)) {
            Surface(color=Color(0xEB17212B),shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth(.94f).widthIn(max=440.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text(p.section,color=updateBlue,fontSize=20.sp)
                    Spacer(Modifier.height(8.dp))
                    CompositionLocalProvider(LocalPropertyPopup provides true,androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(density.density,density.fontScale*1.15f)) {
                        Column(Modifier.fillMaxWidth().heightIn(max=maxHeight).fadedVerticalScroll(rememberScrollState())) {
                            if(details!=null)details(p) else { Text(p.property,color=updateInk);Text(p.value,color=updateBlue) }
                        }
                    }
                    TextButton(onClick={selected=null},modifier=Modifier.align(androidx.compose.ui.Alignment.End)){Text("Close")}
                }
            }
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun PinnableValue(section: String, label: String, value: String, pin: (SectionPin) -> Unit) {
    Column(Modifier.combinedClickable(onClick = {}, onDoubleClick = { pin(SectionPin(section, label, value)) }).padding(vertical = 7.dp, horizontal = 4.dp)) {
        Text(label, color = updateMuted, fontSize = 11.sp)
        Text(value, color = updateBlue, fontSize = 15.sp)
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun SectionPrices(section: String, mass: Double, plate: Boolean, pin: (SectionPin) -> Unit) {
    val context=androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit){PriceCatalog.load(context)}
    val hollow=listOf("RHS","SHS","CHS").any{section.startsWith(it)}
    val grades=if(hollow)listOf("S275","SS 304","SS 316") else if(section.startsWith("Purlin"))listOf("Galvanised") else listOf("S275")
    val unit=if(plate)"m²"else"m"
    Spacer(Modifier.height(12.dp))
    Row(Modifier.fillMaxWidth().padding(vertical=10.dp)){
        Text("Grade",Modifier.weight(.75f),color=updateMuted,fontSize=12.sp)
        Text("UK estimate / $unit",Modifier.weight(1f),color=updateMuted,fontSize=12.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
        Text("LT estimate / $unit",Modifier.weight(1f),color=updateMuted,fontSize=12.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
    }
    HorizontalDivider(color=updateBlue.copy(alpha=.3f))
    grades.forEach{grade->
        Row(Modifier.fillMaxWidth().padding(vertical=10.dp),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically){
            Text(grade,Modifier.weight(.75f),color=updateMuted,fontSize=14.sp)
            listOf("UK","LT").forEach{country->
                val estimate=marketEstimate(PriceCatalog.quotes,section,grade,country,mass,unit)
                val value=estimate?.let{(if(country=="UK")"£"else"€")+fixed(it.amount)} ?: "Not available"
                Text(value,Modifier.weight(1f).combinedClickable(onClick={},onDoubleClick={if(estimate!=null)pin(SectionPin(section,"$grade · $country / $unit",value))}),color=updateInk,fontSize=15.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
            }
        }
        HorizontalDivider(color=updateBlue.copy(alpha=.18f))
    }
    Text((if(Regex("^(IPE|IPN|HEA|HEB|HEM)\\b").containsMatchIn(section))"Budget / $unit · ex VAT; delivery excluded.\nIndicative kg/m × rate; obtain a beam quote."else "Estimates / $unit · ex VAT; delivery excluded.\nUnavailable = no reliable price data."),color=updateMuted,fontSize=10.sp,maxLines=2,modifier=Modifier.padding(top=10.dp))
}

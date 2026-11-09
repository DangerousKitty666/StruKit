package com.structkit.app

import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable internal fun EngineeringPage(title:String,onBack:()->Unit) {
    val tabs=if(title=="HVAC")listOf("Ducts","Air moisture","Heat transfer","Airflow","Canopy") else listOf("Calculator","AC / mains","Equation check","Circuit builder","Symbols","Voltage drop","Energy","RC timing")
    var selected by rememberSaveable(title){mutableStateOf(tabs.first())}
    var web by remember{mutableStateOf<WebView?>(null)}
    BackHandler {
        val view=web
        if(view==null)onBack() else view.evaluateJavascript("window.strukitDismissDialog?.() === true") { result -> if(result!="true")onBack() }
    }
    Column(Modifier.fillMaxSize().padding(horizontal=8.dp)) {
        Text(title,color=Color(0xFFD9DDE3),fontSize=27.sp,modifier=Modifier.padding(start=4.dp,bottom=8.dp))
        Row(Modifier.weight(1f).fillMaxWidth()) {
            ReferencePage("tool-${title.lowercase()}.html",Modifier.weight(1f).fillMaxHeight(),toolTab=selected,onViewReady={web=it})
            Spacer(Modifier.width(4.dp))
            ToolTabRail(tabs,selected,short={when(it){"Air moisture"->"Moisture";"Heat transfer"->"Heat";"Equation check"->"Equation";"Circuit builder"->"Circuit";"Voltage drop"->"V drop";else->it}}){selected=it}
        }
    }
}

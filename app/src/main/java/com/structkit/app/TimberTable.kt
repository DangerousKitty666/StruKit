package com.structkit.app
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable internal fun TimberPinDetails(name:String) {
    val m=Regex("(C16|C24) (\\d+)×(\\d+)").find(name)
    if(m!=null) TimberProperties(m.groupValues[1],m.groupValues[2].toInt(),m.groupValues[3].toInt()) {}
}
@Composable private fun TimberProperties(grade:String,b:Int,h:Int,pin:(SectionPin)->Unit) {
    val rho=if(grade=="C16")370.0 else 420.0
    val area=b*h/1000000.0;val name="$grade $b×$h"
    listOf("b" to "$b mm","h" to "$h mm","Mass" to "${fixed(area*rho)} kg/m","Area" to "${fixed(b*h/100.0)} cm²",
        "I (h depth)" to "${fixed(b.toDouble()*h*h*h/120000.0)} cm⁴","W (h depth)" to "${fixed(b.toDouble()*h*h/6000.0)} cm³",
        "Mean density" to "$rho kg/m³","Mean E ∥" to (if(grade=="C16")"8000 MPa" else "11000 MPa"),
        "Bending characteristic" to (if(grade=="C16")"16 MPa" else "24 MPa"),
        "LT estimate" to "€${fixed(area*13.62/.02565)}/m","UK estimate incl VAT" to "£${fixed(area*11.96/(.047*.100*4.8))}/m")
        .forEach { (label,value)->PinnableValue(name,label,value,pin) }
}
@Composable internal fun TimberTable(pin:(SectionPin)->Unit) {
    var grade by remember { mutableStateOf("C24") };var selected by remember { mutableStateOf<Pair<Int,Int>?>(null) }
    Column(Modifier.fillMaxSize()) {
        InfoPanel(Modifier.fillMaxWidth()) {
            ToggleChips(listOf("C16","C24"),grade){grade=it}
            Canvas(Modifier.fillMaxWidth().height(175.dp)) {
                val l=size.width*.34f;val r=size.width*.66f;val t=size.height*.18f;val b=size.height*.78f
                drawRect(Color(0xFFBBA887),Offset(l,t),Size(r-l,b-t),style=Stroke(2.dp.toPx()))
                val paint=android.graphics.Paint(3).apply{color=0xFF9EC5E8.toInt();textSize=15.sp.toPx();textAlign=android.graphics.Paint.Align.CENTER}
                fun line(a:Offset,z:Offset)=drawLine(updateBlue,a,z,1.dp.toPx())
                line(Offset(l,t-14),Offset(r,t-14));line(Offset(l,t-21),Offset(l,t-7));line(Offset(r,t-21),Offset(r,t-7))
                line(Offset(r+18,t),Offset(r+18,b));line(Offset(r+11,t),Offset(r+25,t));line(Offset(r+11,b),Offset(r+25,b))
                drawContext.canvas.nativeCanvas.drawText("b",(l+r)/2,t-23,paint)
                drawContext.canvas.nativeCanvas.drawText("h",r+36,(t+b)/2,paint)
            }
        }
        Text("Finished dimensions · mm · tap row for properties",color=updateMuted,fontSize=11.sp)
        Row(Modifier.fillMaxWidth().padding(vertical=10.dp)){listOf("b","h","kg/m","Area cm²").forEach { Text(it,color=updateBlue,modifier=Modifier.weight(1f),fontSize=12.sp) }}
        Column(Modifier.weight(1f).fadedVerticalScroll(rememberScrollState())) {
            listOf(38 to 63,38 to 89,38 to 140,45 to 70,45 to 95,45 to 120,45 to 145,45 to 170,45 to 195,45 to 220,70 to 70,95 to 95,145 to 145).forEach { pair ->
                val (b,h)=pair
                Row(Modifier.fillMaxWidth().clickable{selected=if(selected==pair)null else pair}.padding(vertical=12.dp)) {
                    listOf("$b","$h",fixed(b*h/1e6*(if(grade=="C16")370 else 420)),fixed(b*h/100.0)).forEach { Text(it,color=updateInk,fontSize=13.sp,modifier=Modifier.weight(1f)) }
                }
                if(selected==pair)TimberProperties(grade,b,h,pin)
                HorizontalDivider(color=Color(0xFF343A43))
            }
            Text("Weight uses EN 338 mean density. Prices extrapolate C24 supplier volume rates, including for C16; not quotations. LT €13.62 per 45×95×6000 (VAT unspecified); UK £11.96 per advertised 47×100×4800 incl VAT. Checked 6 Nov 2026. Delivery/treatment premiums excluded.",color=updateMuted,fontSize=11.sp)
            SourceLink("LT price reference","https://timbershop.lt/p/graduota-mediena-c24-45x95/")
            SourceLink("UK price reference","https://clarkefencing.co.uk/product/48m-x-100-x-47mm-c24-graded-timber/")
        }
    }
}

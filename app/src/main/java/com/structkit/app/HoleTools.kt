package com.structkit.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable internal fun HoleTools() {
    var type by remember { mutableStateOf("Drills") }
    InfoPanel(Modifier.fillMaxWidth()) {
        ToggleChips(listOf("Drills", "Hole saws"), type) { type = it }
        HoleToolDiagram(type == "Drills", Modifier.fillMaxWidth().height(205.dp))
        Text(if (type == "Drills") "D = cutting diameter · L = total length · l = fluted working length" else "D = cutting diameter · H = cutting depth · pilot drill shown separately", color = updateMuted, fontSize = 11.sp)
        Text("Shaded schematic · dimensions below refer to the listed product series", color = updateMuted, fontSize = 10.sp)
    }
    Spacer(Modifier.height(10.dp))
    if (type == "Drills") {
        Text("Common metric HSS-Co jobber drills · DIN 338", color = updateBlue, fontSize = 15.sp)
        Row(Modifier.fillMaxWidth()) { listOf("D (mm)", "L (mm)", "l (mm)").forEach { Text(it, color = updateMuted, modifier = Modifier.weight(1f), fontSize = 12.sp) } }
        val totals = listOf(34,40,49,57,61,70,75,80,86,93,93,101,109,109,117,117,125,125,133,133,142,142,151,151,151)
        val working = listOf(12,18,24,30,33,39,43,47,52,57,57,63,69,69,75,75,81,81,87,87,94,94,101,101,101)
        totals.indices.forEach { i ->
            Row(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                listOf(fixed(1.0 + i * .5, 1), totals[i].toString(), working[i].toString()).forEach { Text(it, modifier = Modifier.weight(1f), color = updateInk, fontSize = 13.sp) }
            }; HorizontalDivider(color = Color(0xFF343A43))
        }
        Text("Additional intermediate diameters such as 3.2, 4.2, 4.8, 5.2, 6.8, 8.5 and 10.2 mm are separately available. Select by the required finished hole, not merely the nearest kit size. Drill runout, material and process affect finished-hole tolerance.", color = updateMuted, fontSize = 12.sp)
        SourceLink("Bosch · 25-piece HSS-Co series dimensions", "https://www.bosch-professional.com/pl/pl/zestaw-wiertel-pro-stainless-steel-hss-co-3089840-ocs-ac/")
    } else {
        Text("Common multi-material hole-saw sizes", color = updateBlue, fontSize = 15.sp)
        Text("Selected Bosch threaded series · cutting depth H = 40 mm. Arbor, pilot and overall assembly length vary.", color = updateMuted, fontSize = 12.sp)
        listOf(19,20,22,25,29,32,35,38,44,51,54,64,70,76,83,95,102,114,127).chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) { row.forEach { d -> Text("Ø $d mm", modifier = Modifier.weight(1f), color = updateInk, fontSize = 13.sp) }; repeat(3-row.size) { Spacer(Modifier.weight(1f)) } }
            HorizontalDivider(color = Color(0xFF343A43))
        }
        Text("Choose tooth material and speed for the workpiece. The nominal saw diameter is not a precision reamed-hole guarantee. Check the clearance specified by the fitting or fastener supplier.", color = updateMuted, fontSize = 12.sp)
        SourceLink("Bosch · hole-saw sets & cutting depths", "https://www.bosch-professional.com/gb/en/multi-material-threaded-hole-saw-set-7741621-ocs-ac/")
    }
}
@Composable private fun HoleToolDiagram(drill: Boolean, modifier: Modifier) {
    Canvas(modifier) {
        val factor = minOf(size.width / 320, size.height / 205)
        scale(factor, factor, Offset.Zero) {
            val metal = Brush.verticalGradient(listOf(Color(0xFF535A62),Color(0xFFE7EBEF),Color(0xFF6D747C),Color(0xFF272D33)), 65f, 133f)
            fun line(x:Float,y:Float,xx:Float,yy:Float) = drawLine(updateBlue,Offset(x,y),Offset(xx,yy),1.1f)
            fun text(t:String,x:Float,y:Float) {
                drawContext.canvas.nativeCanvas.drawText(t,x,y,android.graphics.Paint(3).apply { color=android.graphics.Color.rgb(158,197,232);textSize=14f;textAlign=android.graphics.Paint.Align.CENTER })
            }
            fun hdim(a:Float,b:Float,y:Float,oy:Float,t:String) {line(a,oy,a,y-5);line(b,oy,b,y-5);line(a,y,b,y);line(a-3,y+3,a+3,y-3);line(b-3,y+3,b+3,y-3);text(t,(a+b)/2,y-7)}
            fun vdim(a:Float,b:Float,x:Float,ox:Float,t:String) {line(ox,a,x+5,a);line(ox,b,x+5,b);line(x,a,x,b);line(x-3,a-3,x+3,a+3);line(x-3,b-3,x+3,b+3);text(t,x+13,(a+b)/2+5)}
            if(drill) {
                drawRect(metal,Offset(35f,85f),Size(220f,42f))
                drawOval(Color(0xFF89919A),Offset(29f,85f),Size(12f,42f))
                val tip=Path().apply {moveTo(255f,85f);lineTo(282f,106f);lineTo(255f,127f);close()};drawPath(tip,metal)
                for(i in 0..5) {
                    val x=114+i*24f
                    val flute=Path().apply{moveTo(x,85f);cubicTo(x-15,100f,x+23,111f,x+9,127f);lineTo(x+16,127f);cubicTo(x+30,111f,x-8,100f,x+7,85f);close()}
                    drawPath(flute,Color(0xFF313840))
                }
                hdim(35f,282f,48f,82f,"L");hdim(112f,282f,166f,130f,"l");vdim(85f,127f,299f,283f,"D")
            } else {
                drawRect(metal,Offset(90f,66f),Size(135f,81f))
                drawOval(Color(0xFF89919A),Offset(73f,66f),Size(34f,81f))
                drawRect(metal,Offset(42f,98f),Size(43f,18f))
                drawOval(Color(0xFFB9C1C9),Offset(207f,66f),Size(36f,81f))
                drawOval(Color(0xFF20262C),Offset(213f,74f),Size(24f,65f))
                drawRect(metal,Offset(225f,103f),Size(48f,8f))
                val tip=Path().apply{moveTo(273f,103f);lineTo(285f,107f);lineTo(273f,111f);close()};drawPath(tip,Color.LightGray)
                for(i in 0..8) {val y=70+i*9f;line(230f,y,239f,y+4)}
                drawRoundRect(Color(0xFF20262C),Offset(125f,94f),Size(65f,15f),androidx.compose.ui.geometry.CornerRadius(7f))
                hdim(90f,225f,45f,64f,"H");vdim(66f,147f,296f,246f,"D")
                text("Pilot",259f,177f);line(263f,160f,267f,116f)
            }
        }
    }
}

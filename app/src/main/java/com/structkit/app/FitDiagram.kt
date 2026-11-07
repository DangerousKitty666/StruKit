package com.structkit.app
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
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

@Composable internal fun FitDiagram(r:FitResult,modifier:Modifier=Modifier) {
    Canvas(modifier.height(325.dp)) {
        val w=size.width;val u=density;val paint=android.graphics.Paint(3).apply{textSize=11*u;color=0xFFE8B46D.toInt();textAlign=android.graphics.Paint.Align.CENTER}
        fun label(t:String,x:Float,y:Float,color:Int=0xFFE8B46D.toInt()){paint.color=color;drawContext.canvas.nativeCanvas.drawText(t,x,y,paint)}
        val blue=updateBlue;val amber=Color(0xFFE8B46D);val green=Color(0xFF85CDB0)
        fun line(a:Offset,b:Offset,c:Color=amber)=drawLine(c,a,b,1.2f*u)
        fun dim(x:Float,a:Float,b:Float){line(Offset(x,a),Offset(x,b));line(Offset(x-4*u,a),Offset(x+4*u,a));line(Offset(x-4*u,b),Offset(x+4*u,b))}
        // Physical section is deliberately not to tolerance scale; the lower plot is scaled.
        val l=w*.22f;val right=w*.78f
        drawRect(blue,Offset(l,25*u),Size(right-l,25*u),style=Stroke(2*u))
        drawRect(blue,Offset(l,95*u),Size(right-l,25*u),style=Stroke(2*u))
        drawRect(green,Offset(l-10*u,55*u),Size(right-l+20*u,35*u),style=Stroke(2*u))
        dim(w*.12f,50*u,95*u);dim(w*.89f,55*u,90*u)
        label("Hole ${r.fit.code.substringBefore('/')}",w*.30f,14*u)
        label("Shaft ${r.fit.code.substringAfter('/')}",w*.73f,14*u)
        label("Ø ${fixed(r.holeMin,3)}…${fixed(r.holeMax,3)}",w/2,140*u)
        label("Ø ${fixed(r.shaftMin,3)}…${fixed(r.shaftMax,3)}",w/2,157*u,0xFF85CDB0.toInt())
        label("SECTION NTS · LIMITS IN mm",w/2,174*u,0xFFA5AFBB.toInt())
        val lo=minOf(0,r.hole.lowerUm,r.shaft.lowerUm)-5.0;val hi=maxOf(0,r.hole.upperUm,r.shaft.upperUm)+5.0
        fun y(v:Double)=(205*u+(hi-v)/(hi-lo)*85*u).toFloat()
        val zero=y(0.0);line(Offset(8*u,zero),Offset(w-8*u,zero),Color.Gray)
        val bw=w*.22f;val hx=w*.23f;val sx=w*.63f
        fun zone(x:Float,d:Deviations,c:Color){val top=y(d.upperUm.toDouble());val bottom=y(d.lowerUm.toDouble());drawRect(c.copy(alpha=.25f),Offset(x,top),Size(bw,bottom-top));drawRect(c,Offset(x,top),Size(bw,bottom-top),style=Stroke(1.5f*u));label("${d.upperUm}",x+bw/2,top-4*u);label("${d.lowerUm}",x+bw/2,bottom+12*u)}
        zone(hx,r.hole,blue);zone(sx,r.shaft,green)
        label("0",10*u,zero-4*u);label("HOLE",hx+bw/2,319*u,0xFF9EC5E8.toInt());label("SHAFT",sx+bw/2,319*u,0xFF85CDB0.toInt())
        label("Deviation from nominal · µm",w/2,193*u,0xFFA5AFBB.toInt())
    }
}
@Composable internal fun FitExtra(r:FitResult) {
    var measuredHole by remember{mutableStateOf("")};var measuredShaft by remember{mutableStateOf("")}
    InfoPanel(Modifier.fillMaxWidth()) {
        Text("Tolerance widths & assembly",color=updateBlue,fontSize=16.sp)
        ResultLine("Hole tolerance", "${r.hole.upperUm-r.hole.lowerUm} µm")
        ResultLine("Shaft tolerance", "${r.shaft.upperUm-r.shaft.lowerUm} µm")
        ResultLine("Radial clearance range", "${fixed(r.minClearanceMm/2,4)} … ${fixed(r.maxClearanceMm/2,4)} mm")
        NumberInput("Measured hole diameter (optional) · mm",measuredHole){measuredHole=it}
        NumberInput("Measured shaft diameter (optional) · mm",measuredShaft){measuredShaft=it}
        decimal(measuredHole)?.let{h->ResultLine("Hole within limits",if(h in r.holeMin..r.holeMax)"Yes" else "No")}
        decimal(measuredShaft)?.let{s->ResultLine("Shaft within limits",if(s in r.shaftMin..r.shaftMax)"Yes" else "No")}
        val h=decimal(measuredHole)?.takeIf{it>0};val s=decimal(measuredShaft)?.takeIf{it>0}
        if(h!=null&&s!=null)ResultLine("Measured diametral clearance", "${fixed(h-s,4)} mm")
        Text("Radial clearance is half the diametral difference. A nominal fit does not specify surface finish, temperature effects, form errors or measurement uncertainty.",color=updateMuted,fontSize=11.sp)
    }
}

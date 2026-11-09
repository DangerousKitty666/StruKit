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
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal fun fitDeviationLabel(value:Int)=when {value>0->"+$value";value<0->"−${-value}";else->"0"}
internal fun fitPlotRange(r:FitResult):Pair<Double,Double> {
    val lo=minOf(0,r.hole.lowerUm,r.shaft.lowerUm).toDouble();val hi=maxOf(0,r.hole.upperUm,r.shaft.upperUm).toDouble()
    val margin=(hi-lo).coerceAtLeast(10.0)*.24
    return (lo-margin) to (hi+margin)
}
@Composable internal fun FitDiagram(r:FitResult,modifier:Modifier=Modifier) {
    Canvas(modifier.height(450.dp)) {
        val w=size.width;val u=density;val blue=updateBlue;val green=Color(0xFF85CDB0)
        val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply{textAlign=android.graphics.Paint.Align.CENTER}
        fun label(t:String,x:Float,y:Float,c:Color=updateInk,font:Float=12f){
            paint.color=((c.alpha*255).toInt() shl 24) or ((c.red*255).toInt() shl 16) or ((c.green*255).toInt() shl 8) or (c.blue*255).toInt()
            paint.textSize=font*u
            val maxWidth=w-12*u
            if(paint.measureText(t)>maxWidth)paint.textSize*=maxWidth/paint.measureText(t)
            drawContext.canvas.nativeCanvas.drawText(t,x,y,paint)
        }
        fun line(x:Float,y:Float,xx:Float,yy:Float,c:Color,stroke:Float=1.5f)=drawLine(c,Offset(x,y),Offset(xx,yy),stroke*u)
        fun hatch(x:Float,y:Float,width:Float,height:Float,c:Color){
            drawRect(c.copy(alpha=.09f),Offset(x,y),Size(width,height))
            clipRect(x,y,x+width,y+height){
                var at=x-height;while(at<x+width){line(at,y+height,at+height,y,c.copy(alpha=.65f),1f);at+=22*u}
            }
            drawRect(c,Offset(x,y),Size(width,height),style=Stroke(2*u))
        }
        val holeX=w*.37f;val holeW=w*.30f
        hatch(holeX,34*u,holeW,34*u,blue);hatch(holeX,144*u,holeW,34*u,blue)
        hatch(w*.10f,84*u,w*.80f,44*u,green)
        // Labels sit on opaque strips, so hatching never crosses the lettering.
        drawRect(Color(0xFF14171B),Offset(holeX+4*u,43*u),Size(holeW-8*u,19*u))
        label("Hole ${r.fit.code.substringBefore('/')}",holeX+holeW/2,57*u,blue)
        drawRect(Color(0xFF14171B),Offset(w*.10f+6*u,96*u),Size(84*u,20*u))
        label("Shaft ${r.fit.code.substringAfter('/')}",w*.10f+48*u,111*u,green)
        fun dim(x:Float,top:Float,bottom:Float,c:Color){line(x,top,x,bottom,c);line(x-4*u,top,x+4*u,top,c);line(x-4*u,bottom,x+4*u,bottom,c)}
        dim(w*.05f,68*u,144*u,blue);line(w*.05f,68*u,holeX-4*u,68*u,blue);line(w*.05f,144*u,holeX-4*u,144*u,blue)
        dim(w*.95f,84*u,128*u,green);line(w*.90f,84*u,w*.95f,84*u,green);line(w*.90f,128*u,w*.95f,128*u,green)
        label("Hole Ø ${fixed(r.holeMin,3)} … ${fixed(r.holeMax,3)} mm",w/2,201*u,blue)
        label("Shaft Ø ${fixed(r.shaftMin,3)} … ${fixed(r.shaftMax,3)} mm",w/2,222*u,green)
        label("SECTION NTS",w/2,18*u,updateMuted,10f)
        line(8*u,240*u,w-8*u,240*u,Color(0xFF343A43),1f)
        label("DEVIATION FROM NOMINAL · µm",w/2,263*u,updateInk,12f)
        val (lo,hi)=fitPlotRange(r)
        fun y(v:Double)=(287*u+(hi-v)/(hi-lo)*114*u).toFloat()
        val zero=y(0.0);line(8*u,zero,w-8*u,zero,Color(0xFFB7BEC7),1.3f)
        label("0",12*u,zero-6*u,updateInk,11f)
        val width=w*.23f
        fun zone(x:Float,d:Deviations,c:Color,upper:String,lower:String){
            val top=y(d.upperUm.toDouble());val bottom=y(d.lowerUm.toDouble())
            drawRect(c.copy(alpha=.28f),Offset(x,top),Size(width,bottom-top));drawRect(c,Offset(x,top),Size(width,bottom-top),style=Stroke(2*u))
            label("$upper ${fitDeviationLabel(d.upperUm)}",x+width/2,top-8*u,c)
            label("$lower ${fitDeviationLabel(d.lowerUm)}",x+width/2,bottom+17*u,c)
        }
        zone(w*.19f,r.hole,blue,"ES","EI");zone(w*.62f,r.shaft,green,"es","ei")
        label("HOLE",w*.305f,438*u,blue);label("SHAFT",w*.735f,438*u,green)
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

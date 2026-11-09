package com.structkit.app
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.*
internal fun holeSawScene()=SolidScene().apply {
    fun ring(x:Double,r:Double,t:Double)=V3(x,r*cos(t),r*sin(t))
    val segments=96
    for(i in 0 until segments){val a=i*2*PI/segments;val b=(i+1)*2*PI/segments
        for((start,end) in listOf(0.0 to .55,.55 to 1.45,1.45 to 2.0)){
            val slot=start==.55&&abs(sin((a+b)/2))<.18&&cos((a+b)/2)<0
            if(!slot)face(0xB2BCC5,ring(start,.75,a),ring(end,.75,a),ring(end,.75,b),ring(start,.75,b))
            face(0x535E69,ring(start,.66,b),ring(end,.66,b),ring(end,.66,a),ring(start,.66,a))
        }
        face(0x7D8B98,ring(0.0,.66,a),ring(0.0,.75,a),ring(0.0,.75,b),ring(0.0,.66,b))
        face(0xDBE2E8,ring(2.0,.66,b),ring(2.0,.75,b),ring(2.0,.75,a),ring(2.0,.66,a))
    }
    for(i in 0 until 24){val a=i*2*PI/24;val b=(i+1)*2*PI/24
        face(0xD3DCE4,ring(2.0,.75,a),ring(2.18,.75,(a+b)/2),ring(2.0,.75,b))
    }
    prism(V3(-.75,0.0,0.0),V3(0.0,0.0,0.0),.16,0xAAB6C0,24)
    prism(V3(0.0,0.0,0.0),V3(.12,0.0,0.0),.75,0x748391,64)
    prism(V3(0.0,0.0,0.0),V3(2.65,0.0,0.0),.055,0xCAD3DC,16)
    face(0xDCE4EB,V3(2.65,-.055,-.055),V3(2.82,0.0,0.0),V3(2.65,-.055,.055))
}

@Composable internal fun HoleSawSolid(modifier:Modifier){
 val bitmap by produceState<android.graphics.Bitmap?>(null){value=withContext(Dispatchers.Default){
  val out=android.graphics.Bitmap.createBitmap(800,400,android.graphics.Bitmap.Config.ARGB_8888)
  val c=android.graphics.Canvas(out);c.drawColor(0xFF14171B.toInt())
  val solid=android.graphics.Bitmap.createBitmap(renderSolid(holeSawScene(),700,260,false,1),700,260,android.graphics.Bitmap.Config.ARGB_8888)
  c.drawBitmap(solid,20f,65f,null);solid.recycle()
  val p=android.graphics.Paint(3).apply{color=0xFF9EC5E8.toInt();strokeWidth=1.7f;textSize=23f}
  val scale=min(644.0/3.57,212.0/1.5)
  fun x(v:Double)=(370+(v-1.035)*scale).toFloat()
  fun y(v:Double)=(195-v*scale).toFloat()
  fun line(a:Float,b:Float,d:Float,e:Float)=c.drawLine(a,b,d,e,p)
  line(x(0.0),45f,x(2.18),45f);line(x(0.0),39f,x(0.0),y(.75));line(x(2.18),39f,x(2.18),y(.75))
  for(v in listOf(0.0,2.18))line(x(v)-4,51f,x(v)+4,39f)
  c.drawText("H",(x(0.0)+x(2.18))/2-7,35f,p)
  line(755f,y(.75),755f,y(-.75));line(x(2.18),y(.75),762f,y(.75));line(x(2.18),y(-.75),762f,y(-.75))
  for(v in listOf(.75,-.75))line(749f,y(v)+4,761f,y(v)-4)
  c.drawText("D",768f,203f,p);c.drawText("SIDE",350f,367f,p);out
 }}
 bitmap?.let{Image(it.asImageBitmap(),"Hole saw solid side view: diameter D and cutting height H",modifier)}
}

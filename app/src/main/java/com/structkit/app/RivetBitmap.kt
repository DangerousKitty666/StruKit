package com.structkit.app
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.min
internal fun rivetBitmap(type: String): Bitmap {
    val rn=type.startsWith("RN_")
    val out=Bitmap.createBitmap(800,if(rn)720 else 380,Bitmap.Config.ARGB_8888)
    val c=Canvas(out);c.drawColor(0xFF14171B.toInt())
    val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply {color=0xFF9EC5E8.toInt();strokeWidth=1.8f;textSize=24f}
    val scene=rivetScene(type)
    fun image(x:Float,y:Float,w:Int,h:Int,view:Int){ val im=Bitmap.createBitmap(renderSolid(scene,w,h,false,view),w,h,Bitmap.Config.ARGB_8888);c.drawBitmap(im,x,y,null);im.recycle() }
    fun line(x:Float,y:Float,xx:Float,yy:Float)=c.drawLine(x,y,xx,yy,paint)
    fun label(t:String,x:Float,y:Float){ c.drawText(t,x,y,paint) }
    fun hd(a:Float,b:Float,y:Float,t:String){line(a,y,b,y);line(a-4,y+6,a+4,y-6);line(b-4,y+6,b+4,y-6);label(t,(a+b)/2-8,y-10)}
    fun vd(x:Float,a:Float,b:Float,t:String){line(x,a,x,b);line(x-6,a+4,x+6,a-4);line(x-6,b+4,x+6,b-4);label(t,x+10,(a+b)/2+7)}
    image(25f,70f,700,220,1)
    val pts=scene.faces.flatMap { it.vertices };val xmin=pts.minOf{it.x};val xmax=pts.maxOf{it.x};val zmin=pts.minOf{it.z};val zmax=pts.maxOf{it.z}
    val scale=min(644/(xmax-xmin),172/(zmax-zmin))
    fun x(a:Double)=(375+(a-(xmin+xmax)/2)*scale).toFloat()
    fun y(a:Double)=(180-(a-(zmin+zmax)/2)*scale).toFloat()
    val cs=type.endsWith("_CS");val bearing=if(cs)0.0 else if(rn).18 else .16
    hd(x(bearing),x(1.6),43f,"L");line(x(bearing),49f,x(bearing),y(if(rn).43 else .28)-4);line(x(1.6),49f,x(1.6),y(if(rn).43 else .28)-4)
    hd(x(0.0),x(if(rn).18 else .16),315f,"k");line(x(0.0),y(-.65),x(0.0),322f);line(x(if(rn).18 else .16),y(-.65),x(if(rn).18 else .16),322f)
    val radius=if(type=="RN_HEX") .42*kotlin.math.sqrt(3.0)/2 else if(rn).42 else .26
    vd(750f,y(radius),y(-radius),if(rn)"D₁" else "d");line(x(1.6)+6,y(radius),756f,y(radius));line(x(1.6)+6,y(-radius),756f,y(-radius))
    label(if(cs)"L includes countersunk head" else "L from under the head",30f,363f)
    if(rn) {
        image(30f,435f,280,220,2);image(395f,418f,370,270,3)
        label("FRONT",110f,693f);label("ISOMETRIC",470f,693f)
        // Front outside diameter D2 is the circular head; hex body A/F is product-specific.
        val frontScale=min(224/(pts.maxOf{it.y}-pts.minOf{it.y}),172/(zmax-zmin))
        val left=(170-.63*frontScale).toFloat();val right=(170+.63*frontScale).toFloat()
        val headTop=(545-.63*frontScale).toFloat();val headBottom=(545+.63*frontScale).toFloat()
        vd(35f,headTop,headBottom,"D₂");line(41f,headTop,170f,headTop);line(41f,headBottom,170f,headBottom)
        val holeTop=(545-.24*frontScale).toFloat();val holeBottom=(545+.24*frontScale).toFloat()
        vd(335f,holeTop,holeBottom,"M")
        line(170f,holeTop,342f,holeTop);line(170f,holeBottom,342f,holeBottom)
    }
    return out
}

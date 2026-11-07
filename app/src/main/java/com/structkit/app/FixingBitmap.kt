package com.structkit.app
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.min
internal fun fixingBitmap(type: String): Bitmap {
    if(type.startsWith("RN_") || type.startsWith("RV_")) return rivetBitmap(type)
    val nut=type.startsWith("NUT_")
    val out=Bitmap.createBitmap(800,if(nut)530 else 430,Bitmap.Config.ARGB_8888)
    val canvas=Canvas(out);canvas.drawColor(0xFF14171B.toInt())
    val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=0xFF9EC5E8.toInt();strokeWidth=1.7f;textSize=23f }
    fun image(scene:SolidScene,x:Float,y:Float,w:Int,h:Int,view:Int){val im=Bitmap.createBitmap(renderSolid(scene,w,h,false,view),w,h,Bitmap.Config.ARGB_8888);canvas.drawBitmap(im,x,y,null);im.recycle()}
    fun label(t:String,x:Float,y:Float,size:Float=23f){paint.textSize=size;canvas.drawText(t,x,y,paint)}
    fun line(x:Float,y:Float,xx:Float,yy:Float)=canvas.drawLine(x,y,xx,yy,paint)
    fun hdim(x:Float,xx:Float,y:Float,t:String){line(x,y,xx,y);line(x-4,y+6,x+4,y-6);line(xx-4,y+6,xx+4,y-6);label(t,(x+xx)/2-7,y-8)}
    fun vdim(x:Float,y:Float,yy:Float,t:String){line(x,y,x,yy);line(x-6,y+4,x+6,y-4);line(x-6,yy+4,x+6,yy-4);label(t,x+12,(y+yy)/2+7)}
    if(nut){
        val scene=nutScene(type.removePrefix("NUT_"))
        image(scene,15f,32f,280,255,2);label("FRONT",100f,30f,18f)
        image(scene,15f,315f,280,170,1);label("SIDE",110f,515f,18f)
        image(scene,365f,55f,425,425,3);label("ISOMETRIC",490f,515f,18f)
        val pts=scene.faces.flatMap { it.vertices }
        val ymin=pts.minOf { it.y };val ymax=pts.maxOf { it.y }
        val zmin=pts.minOf { it.z };val zmax=pts.maxOf { it.z }
        val xmin=pts.minOf { it.x };val xmax=pts.maxOf { it.x }
        val frontScale=min(224/(ymax-ymin),207/(zmax-zmin))
        val halfS=.67*kotlin.math.sqrt(3.0)/2
        val top=(159.5-halfS*frontScale).toFloat();val bottom=(159.5+halfS*frontScale).toFloat()
        vdim(310f,top,bottom,"s")
        val edge=(155+.335*frontScale+5).toFloat()
        line(edge,top,316f,top);line(edge,bottom,316f,bottom)
        val sideScale=min(224/(xmax-xmin),122/(zmax-zmin))
        val left=(155-(xmax-xmin)/2*sideScale).toFloat()
        val right=(155+(xmax-xmin)/2*sideScale).toFloat()
        hdim(left,right,309f,"h")
        line(left,315f,left,400f);line(right,315f,right,400f)
    }else{
        val scene=boltScene(type);image(scene,25f,58f,700,240,1)
        // Orthographic projection is x horizontal and z vertical. Use the model extents for dimension anchors.
        val pts=scene.faces.flatMap{it.vertices};val xmin=pts.minOf{it.x};val xmax=pts.maxOf{it.x};val zmin=pts.minOf{it.z};val zmax=pts.maxOf{it.z}
        val scale=min(644/(xmax-xmin),192/(zmax-zmin))
        fun x(v:Double)=(25+350+(v-(xmin+xmax)/2)*scale).toFloat()
        fun y(v:Double)=(58+120-(v-(zmin+zmax)/2)*scale).toFloat()
        val bearing=if(type=="SCREW2").48 else .4
        val end=if(type=="SCREW2")3.12 else 2.9
        hdim(x(bearing),x(end),33f,"L")
        line(x(bearing),40f,x(bearing),y(.32)-5);line(x(end),40f,x(end),y(.30)-5)
        hdim(x(0.0),x(bearing),317f,"k");line(x(0.0),y(-.57)+7,x(0.0),325f);line(x(bearing),y(-.57)+7,x(bearing),325f)
        vdim(751f,y(.355),y(-.355),"d");line(x(end)+8,y(.355),758f,y(.355));line(x(end)+8,y(-.355),758f,y(-.355))
        if(type!="SCREW2") {hdim(x(if(type=="BOLT_HEX_FULL") .48 else 1.1),x(2.9),287f,"b");line(x(if(type=="BOLT_HEX_FULL") .48 else 1.1),y(-.33)+4,x(if(type=="BOLT_HEX_FULL") .48 else 1.1),295f);line(x(2.9),y(-.33)+4,x(2.9),295f)}
        // End face is needed to show across-flats s truthfully, especially for a socket drive.
        val head=SolidScene().apply { faces.addAll(scene.faces.filter{f->f.vertices.all{it.x<=.4001}}) }
        image(head,500f,310f,180,115,2)
        val socket=type=="BOLT_SOCKET" || type=="BOLT_DOME"
        val headPts=head.faces.flatMap{it.vertices}; val hz=headPts.maxOf{it.z}-headPts.minOf{it.z}; val hy=headPts.maxOf{it.y}-headPts.minOf{it.y}
        val headScale=min(124/hy,67/hz); val halfS=(if(socket) .25 else .64)*kotlin.math.sqrt(3.0)/2*headScale
        vdim(690f,(367.5-halfS).toFloat(),(367.5+halfS).toFloat(),"s")
        val edgeX=(590+(if(socket).25 else .64)*.5*headScale+3).toFloat()
        line(edgeX,(367.5-halfS).toFloat(),697f,(367.5-halfS).toFloat());line(edgeX,(367.5+halfS).toFloat(),697f,(367.5+halfS).toFloat())
        label(if(socket)"s = socket across flats" else "s = head across flats",35f,380f,20f)
        label("Head end view →",280f,408f,18f)
    }
    return out
}






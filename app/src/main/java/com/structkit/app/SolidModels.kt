package com.structkit.app

import kotlin.math.*

// Small offline solid renderer. The same geometry is used for the Android view and preview checks.
internal data class V3(val x: Double, val y: Double, val z: Double) {
    operator fun plus(b: V3) = V3(x+b.x,y+b.y,z+b.z)
    operator fun minus(b: V3) = V3(x-b.x,y-b.y,z-b.z)
    operator fun times(s: Double) = V3(x*s,y*s,z*s)
    fun dot(b: V3) = x*b.x+y*b.y+z*b.z
    fun cross(b: V3) = V3(y*b.z-z*b.y,z*b.x-x*b.z,x*b.y-y*b.x)
    fun unit(): V3 = this * (1.0 / sqrt(dot(this)).coerceAtLeast(1e-9))
}
internal data class Face3(val vertices: List<V3>, val rgb: Int)
internal class SolidScene {
    val faces = mutableListOf<Face3>()
    var transform: (V3)->V3 = { it }
    fun face(c: Int, vararg v: V3) { faces.add(Face3(v.map(transform),c)) }
    fun box(x: Double,y: Double,z: Double,dx: Double,dy: Double,dz: Double,c: Int) {
        val v = listOf(V3(x,y,z),V3(x+dx,y,z),V3(x+dx,y+dy,z),V3(x,y+dy,z),V3(x,y,z+dz),V3(x+dx,y,z+dz),V3(x+dx,y+dy,z+dz),V3(x,y+dy,z+dz))
        for (ids in listOf(listOf(0,3,2,1),listOf(4,5,6,7),listOf(0,1,5,4),listOf(1,2,6,5),listOf(2,3,7,6),listOf(3,0,4,7))) face(c,*ids.map{v[it]}.toTypedArray())
    }
    fun prism(a: V3,b: V3,r: Double,c: Int,n: Int=24) {
        val axis=(b-a).unit(); val u=axis.cross(if(abs(axis.z)<.9) V3(0.0,0.0,1.0) else V3(0.0,1.0,0.0)).unit(); val v=axis.cross(u)
        val ring=List(n){u*(r*cos(it*2*PI/n))+v*(r*sin(it*2*PI/n))}
        face(c,*ring.reversed().map{a+it}.toTypedArray()); face(c,*ring.map{b+it}.toTypedArray())
        for(i in ring.indices) face(c,a+ring[i],a+ring[(i+1)%n],b+ring[(i+1)%n],b+ring[i])
    }
    fun trianglePlate(a: V3,b: V3,c: V3,t: Double,color: Int) {
        val d=V3(0.0,t,0.0); face(color,a,b,c);face(color,a+d,c+d,b+d)
        face(color,a,a+d,b+d,b);face(color,b,b+d,c+d,c);face(color,c,c+d,a+d,a)
    }
    fun slopingFlange(x0: Double,z0: Double,x1: Double,z1: Double) {
        val a=V3(x0,-.42,z0);val b=V3(x1,-.42,z1);val c=V3(x1,.42,z1);val d=V3(x0,.42,z0);val t=V3(0.0,0.0,-.08)
        face(PLATE3,a,b,c,d);face(PLATE3,a+t,d+t,c+t,b+t)
        face(PLATE3,a,a+t,b+t,b);face(PLATE3,b,b+t,c+t,c);face(PLATE3,c,c+t,d+t,d);face(PLATE3,d,d+t,a+t,a)
    }
    fun beam(x: Double,end: Double,z: Double=0.0,h: Double=1.4,cope: Double=0.0,c: Int=0x93A3B3) {
        box(x,-.045,z-h/2+.09,end-x,.09,h-.18,c)
        box(x,-.42,z-h/2,end-x,.84,.09,c)
        box(x+cope,-.42,z+h/2-.09,end-x-cope,.84,.09,c)
    }
    fun column(z0: Double=-1.9,z1: Double=1.9,c: Int=0x687C91) {
        box(-.42,-.045,z0,.84,.09,z1-z0,c)
        box(-.5,-.5,z0,.08,1.0,z1-z0,c);box(.42,-.5,z0,.08,1.0,z1-z0,c)
    }
    fun bolt(a: V3,axis: V3,length: Double=.3) {
        val u=axis.unit(); prism(a-u*.03,a+u*length,.046,0xB8BEC5,16)
        prism(a-u*.06,a,.086,0xDADEE2,6)
        prism(a+u*(length-.08),a+u*(length-.015),.08,0xAAB3BD,6)
    }
}
internal const val PLATE3=0x548EB7
internal const val WELD3=0xD7AA62
internal fun connectionScene(mode: Int): SolidScene = SolidScene().apply {
    fun endPlate(x: Double=.5,lo: Double=-.7,hi: Double=.7) {
        box(x,-.48,lo,.09,.96,hi-lo,PLATE3)
        // Highlight the beam-web weld at the plate intersection.
        box(x+.09,-.065,-.58,.025,.13,1.16,WELD3)
    }
    fun endBolts(x: Double=.67, zs: List<Double> = listOf(-.46,0.0,.46)) {
        for(y in listOf(-.31,.31)) for(z in zs) bolt(V3(x,y,z),V3(-1.0,0.0,0.0),.34)
    }
    fun stiffeners() { for(z in listOf(-.7,.61)) box(-.42,-.46,z,.84,.92,.08,PLATE3) }
    fun fin(x: Double=.5) {
        box(x,-.14,-.5,.65,.09,1.0,PLATE3)
        box(x,-.16,-.5,.03,.14,1.0,WELD3)
        for(z in listOf(-.34,0.0,.34)) bolt(V3(x+.43,-.2,z),V3(0.0,1.0,0.0),.32)
    }
    fun primaryBeam() {
        transform={v->V3(v.y,v.x,v.z)};beam(-1.7,1.7,0.0,1.8,c=0x687C91);transform={it}
    }
    when(mode) {
        0 -> {column();beam(.73,3.0);fin()}
        1 -> {column();beam(.59,3.0);endPlate(lo=-.52,hi=.52);endBolts(zs=listOf(-.34,0.0,.34))}
        2 -> {primaryBeam();beam(.15,2.8,cope=.38)
            for(y in listOf(-.25,.06)) {box(.05,y,-.5,.1,.19,1.0,PLATE3);box(.15,if(y<0)-.13 else .05,-.5,.48,.08,1.0,PLATE3)}
            for(z in listOf(-.34,0.0,.34)){bolt(V3(.43,-.2,z),V3(0.0,1.0,0.0),.4);for(y in listOf(-.2,.2))bolt(V3(.2,y,z),V3(-1.0,0.0,0.0),.3)}}
        3,4 -> {column();beam(.59,3.0);endPlate(lo=if(mode==4)-1.05 else -.7,hi=if(mode==4)1.05 else .7);stiffeners()
            endBolts(zs=if(mode==4) listOf(-.89,-.43,.43,.89) else listOf(-.45,.45))
            for(z in listOf(-.7,.67))box(.59,-.42,z,.03,.84,.03,WELD3)
            if(mode==4) {trianglePlate(V3(.59,-.04,.7),V3(1.15,-.04,.7),V3(.59,-.04,1.02),.08,PLATE3);trianglePlate(V3(.59,-.04,-.7),V3(.59,-.04,-1.02),V3(1.15,-.04,-.7),.08,PLATE3)}}
        5 -> {beam(-2.2,-.04);beam(.04,2.2)
            for(z in listOf(-.8,.7))box(-.8,-.42,z,1.6,.84,.1,PLATE3)
            for(y in listOf(-.12,.045))box(-.8,y,-.48,1.6,.075,.96,PLATE3)
            for(x in listOf(-.6,-.3,.3,.6)){for(y in listOf(-.26,.26))for(z in listOf(-.85,.86))bolt(V3(x,y,z),V3(0.0,0.0,if(z>0)-1.0 else 1.0),.3)
                for(z in listOf(-.28,.28))bolt(V3(x,-.18,z),V3(0.0,1.0,0.0),.36)}}
        6,20 -> {column();beam(.62,3.0);box(.5,-.48,-.81,.72,.96,.11,PLATE3);box(.5,-.48,-1.25,.1,.96,.44,PLATE3)
            trianglePlate(V3(.6,-.05,-.81),V3(1.2,-.05,-.81),V3(.6,-.05,-1.23),.1,PLATE3)
            box(.5,-.35,.7,.1,.7,.28,PLATE3);box(.6,-.35,.7,.4,.7,.07,PLATE3)
            for(y in listOf(-.3,.3)){bolt(V3(.66,y,-1.03),V3(-1.0,0.0,0.0),.3);bolt(V3(.66,y,.86),V3(-1.0,0.0,0.0),.3)}
            for(y in listOf(-.24,.24)){bolt(V3(.84,y,.84),V3(0.0,0.0,-1.0),.27);bolt(V3(.94,y,-.87),V3(0.0,0.0,1.0),.27)}
            if(mode==20){stiffeners();fin();box(.5,-.35,.98,.1,.7,.3,PLATE3);box(.6,-.35,.7,.6,.7,.07,PLATE3);trianglePlate(V3(.6,-.05,.77),V3(1.2,-.05,.77),V3(.6,-.05,1.28),.1,PLATE3)}}
        7 -> {column();stiffeners();transform={v->V3(v.x,v.y,v.z+(v.x-.59)*.12)};beam(.59,3.0)
            trianglePlate(V3(.59,-.045,-.7),V3(2.0,-.045,-.7),V3(.59,-.045,-1.38),.09,PLATE3)
            slopingFlange(.59,-1.38,2.0,-.7)
            box(.59,-.065,-1.38,.025,.13,.68,WELD3)
            for(y in listOf(-.42,.045))box(1.95,y,-.61,.08,.375,1.22,PLATE3)
            transform={it}
            endPlate(lo=-1.5,hi=.95);endBolts(zs=listOf(-1.32,-.95,-.38,.4,.82));box(-.42,-.46,-1.38,.84,.92,.08,PLATE3)}
        8,9 -> {for(sign in listOf(-1.0,1.0)){transform={v->V3(v.x*sign,v.y,v.z+(if(mode==8)-.13*(v.x-.1) else 0.0))};beam(.1,2.4)
                box(0.0,-.48,if(mode==8)-1.18 else -.97,.1,.96,if(mode==8)2.08 else 1.94,PLATE3)
                trianglePlate(V3(.1,-.04,if(mode==8)-.7 else .7),V3(1.1,-.04,if(mode==8)-.7 else .7),V3(.1,-.04,if(mode==8)-1.1 else .95),.08,PLATE3)
                if(mode==8){slopingFlange(.1,-1.1,1.1,-.7);for(y in listOf(-.42,.045))box(1.05,y,-.61,.08,.375,1.22,PLATE3)}
            };transform={it}
            for(y in listOf(-.31,.31))for(z in listOf(-.85,-.4,.4,.82))bolt(V3(.17,y,z),V3(-1.0,0.0,0.0),.36)}
        10 -> {column();stiffeners();endPlate(lo=-.95,hi=.95);endBolts(zs=listOf(-.82,.82))
            box(.59,-.42,-.65,2.3,.84,.08);box(.59,-.42,.57,2.3,.84,.08);box(.59,-.42,-.57,2.3,.08,1.14);box(.59,.34,-.57,2.3,.08,1.14)}
        11 -> {box(-.5,-.5,-1.9,.1,1.0,3.8);box(.4,-.5,-1.9,.1,1.0,3.8);box(-.4,-.5,-1.9,.8,.1,3.8);box(-.4,.4,-1.9,.8,.1,3.8)
            box(.5,-.66,-.75,.09,1.32,1.5,PLATE3);box(.59,-.66,-.72,.09,1.32,1.44,PLATE3);beam(.68,3.0)
            for(y in listOf(-.55,.55))for(z in listOf(-.48,0.0,.48))bolt(V3(.75,y,z),V3(-1.0,0.0,0.0),.32)}
        12,16 -> {column(z1=-.85);box(-.62,-.6,-.85,1.24,1.2,.15,PLATE3);beam(-1.6,2.1)
            for(x in listOf(-.4,.4))for(y in listOf(-.28,.28))bolt(V3(x,y,-.55),V3(0.0,0.0,-1.0),.4)
            if(mode==16)for(y in listOf(-.42,.045))box(-.05,y,-.61,.1,.375,1.22,PLATE3)}
        13 -> {beam(-1.6,2.1);box(-.62,-.6,.7,1.24,1.2,.15,PLATE3);column(z0=.85,z1=2.7)
            for(x in listOf(-.53,.53))for(y in listOf(-.28,.28))bolt(V3(x,y,.93),V3(0.0,0.0,-1.0),.4)
            for(y in listOf(-.42,.045))box(-.05,y,-.61,.1,.375,1.22,PLATE3)}
        14 -> {primaryBeam();beam(.14,2.8,.45,1.4);box(.05,-.48,-.25,.09,.96,1.4,PLATE3)
            for(y in listOf(-.31,.31))for(z in listOf(-.08,.2,.48,.76))bolt(V3(.22,y,z),V3(-1.0,0.0,0.0),.3)}
        15 -> {primaryBeam();beam(-1.5,2.0,-1.6,1.4)
            for(x in listOf(-.25,.25))for(y in listOf(-.25,.25))bolt(V3(x,y,-.76),V3(0.0,0.0,-1.0),.4)}
        18 -> {transform={v->V3(v.y,v.x,v.z)};column();transform={it}
            box(.045,-.38,-.52,.09,.76,1.04,PLATE3)
            box(.135,-.045,-.61,2.5,.09,1.22,0x93A3B3)
            box(.135,-.32,-.7,2.5,.64,.09,0x93A3B3);box(.135,-.32,.61,2.5,.64,.09,0x93A3B3)
            box(.135,-.065,-.5,.025,.13,1.0,WELD3)
            for(y in listOf(-.25,.25))for(z in listOf(-.35,0.0,.35))bolt(V3(.22,y,z),V3(-1.0,0.0,0.0),.3)}
        19 -> {column();beam(.59,3.0);endPlate(lo=-.94,hi=1.45);stiffeners();box(-.42,-.46,1.26,.84,.92,.08,PLATE3)
            trianglePlate(V3(.59,-.045,.7),V3(1.85,-.045,.7),V3(.59,-.045,1.32),.09,PLATE3)
            slopingFlange(.59,1.4,1.85,.78);endBolts(zs=listOf(-.82,-.4,.4,.95,1.3))}
        else -> {primaryBeam();beam(.3,2.8,0.1,1.4,.35);fin(.05)}
    }
}

// Neutral steel is the default for structural members.
private fun SolidScene.box(x: Double,y: Double,z: Double,dx: Double,dy: Double,dz: Double) = box(x,y,z,dx,dy,dz,0x93A3B3)

internal fun renderSolid(scene: SolidScene,width: Int=720,height: Int=560,reverse: Boolean=false,view: Int=0): IntArray {
    val pixels=IntArray(width*height){0xFF14171B.toInt()}; val depth=DoubleArray(pixels.size){-Double.MAX_VALUE}
    val camera=when(view) {1->V3(0.0,-1.0,0.0);2->V3(-1.0,0.0,0.0);3->V3(-6.0,-8.0,5.5).unit();4->V3(12.0,if(reverse)4.0 else -4.0,5.5).unit();else->V3(6.0,if(reverse)8.0 else -8.0,5.5).unit()}
    val right=V3(-camera.y,camera.x,0.0).unit();val up=camera.cross(right).unit()
    fun project(v: V3)=V3(v.dot(right),-v.dot(up),v.dot(camera))
    val points=scene.faces.flatMap{it.vertices}.map(::project)
    val xmin=points.minOf{it.x};val xmax=points.maxOf{it.x};val ymin=points.minOf{it.y};val ymax=points.maxOf{it.y}
    val scale=min((width-56)/(xmax-xmin),(height-48)/(ymax-ymin))
    fun screen(v: V3): V3 {val p=project(v);return V3((p.x-(xmin+xmax)/2)*scale+width/2,(p.y-(ymin+ymax)/2)*scale+height/2,p.z)}
    val light=V3(-.25,-.45,1.0).unit()
    for(face in scene.faces){
        val normal=(face.vertices[1]-face.vertices[0]).cross(face.vertices[2]-face.vertices[0]).unit()
        val shade=(.48+.52*abs(normal.dot(light))).coerceIn(.35,1.0)
        val c=face.rgb;val rgb=(0xFF shl 24) or (((((c shr 16) and 255)*shade).toInt()) shl 16) or (((((c shr 8) and 255)*shade).toInt()) shl 8) or (((c and 255)*shade).toInt())
        val verts=face.vertices.map(::screen)
        for(i in 1 until verts.size-1){val a=verts[0];val b=verts[i];val c3=verts[i+1]
            val den=(b.y-c3.y)*(a.x-c3.x)+(c3.x-b.x)*(a.y-c3.y);if(abs(den)<1e-8)continue
            val left=floor(min(a.x,min(b.x,c3.x))).toInt().coerceAtLeast(0);val rightX=ceil(max(a.x,max(b.x,c3.x))).toInt().coerceAtMost(width-1)
            val top=floor(min(a.y,min(b.y,c3.y))).toInt().coerceAtLeast(0);val bottom=ceil(max(a.y,max(b.y,c3.y))).toInt().coerceAtMost(height-1)
            for(y in top..bottom)for(x in left..rightX){val u=((b.y-c3.y)*(x+.5-c3.x)+(c3.x-b.x)*(y+.5-c3.y))/den;val v=((c3.y-a.y)*(x+.5-c3.x)+(a.x-c3.x)*(y+.5-c3.y))/den;val w=1-u-v
                if(u>=0 && v>=0 && w>=0){val z=u*a.z+v*b.z+w*c3.z;val idx=y*width+x;if(z>depth[idx]){depth[idx]=z;pixels[idx]=rgb}}}
        }
    };return pixels
}




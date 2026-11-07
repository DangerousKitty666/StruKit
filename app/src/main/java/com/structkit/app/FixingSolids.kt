package com.structkit.app

import kotlin.math.*

// Axial coordinate x; annular surfaces preserve open bores rather than painting a black disc.
private fun SolidScene.ring(x: Double,r: Double,x2: Double,r2: Double,color: Int,n: Int=64) {
    for(i in 0 until n){val a=i*2*PI/n;val b=(i+1)*2*PI/n
        face(color,V3(x,r*cos(a),r*sin(a)),V3(x2,r2*cos(a),r2*sin(a)),V3(x2,r2*cos(b),r2*sin(b)),V3(x,r*cos(b),r*sin(b)))}
}
private fun SolidScene.hexRing(x: Double,outer: Double,inner: Double,color: Int) {
    val n=72
    fun outerPoint(a: Double):V3 {val sector=(a%(PI/3))-PI/6;val r=outer*cos(PI/6)/cos(sector);return V3(x,r*cos(a),r*sin(a))}
    for(i in 0 until n){val a=i*2*PI/n;val b=(i+1)*2*PI/n;face(color,outerPoint(a),outerPoint(b),V3(x,inner*cos(b),inner*sin(b)),V3(x,inner*cos(a),inner*sin(a)))}
}
private fun SolidScene.thread(start: Double,end: Double,r: Double,pitch: Double,inside: Boolean=false) {
    val steps=((end-start)/pitch*48).toInt(); val crest=if(inside) r-.028 else r+.045
    for(i in 0 until steps){val x=start+(end-start)*i/steps;val xx=start+(end-start)*(i+1)/steps;val a=(x-start)/pitch*2*PI;val b=(xx-start)/pitch*2*PI
        fun p(t:Double,rad:Double,angle:Double)=V3(t,rad*cos(angle),rad*sin(angle))
        face(0xB5BAC0,p(x-pitch*.32,r,a),p(xx-pitch*.32,r,b),p(xx,crest,b),p(x,crest,a))
        face(0x858B93,p(x,crest,a),p(xx,crest,b),p(xx+pitch*.32,r,b),p(x+pitch*.32,r,a))}
}
internal fun boltScene(type: String): SolidScene = SolidScene().apply {
    val metal=0xC0C4C9
    val screw=type=="SCREW2"
    val start=if(screw).48 else .4
    if(type=="BOLT_SOCKET"){
        ring(0.0,.52,.4,.52,metal);ring(0.0,.52,0.0,.23,metal)
        prism(V3(.14,0.0,0.0),V3(.4,0.0,0.0),.52,metal,48)
        prism(V3(.12,0.0,0.0),V3(.14,0.0,0.0),.23,0x242B34,6)
        for(i in 0..5){val a=i*PI/3;val b=(i+1)*PI/3;face(0x3E454E,V3(0.0,.25*cos(a),.25*sin(a)),V3(0.0,.25*cos(b),.25*sin(b)),V3(.15,.25*cos(b),.25*sin(b)),V3(.15,.25*cos(a),.25*sin(a)))}
    }else if(type=="BOLT_DOME"){
        val profile=listOf(0.0 to .23,.035 to .36,.10 to .49,.22 to .60,.35 to .64,.4 to .64)
        for(i in 0 until profile.size-1)ring(profile[i].first,profile[i].second,profile[i+1].first,profile[i+1].second,metal)
        ring(0.0,.23,.18,.23,0x353C44);prism(V3(.18,0.0,0.0),V3(.4,0.0,0.0),.23,0x454B55,6)
    }else{
        prism(V3(.045,0.0,0.0),V3(.36,0.0,0.0),.64,metal,6)
        prism(V3(0.0,0.0,0.0),V3(.045,0.0,0.0),.55,metal,48)
        prism(V3(.36,0.0,0.0),V3(start,0.0,0.0),if(screw).71 else .47,metal,48)
        if(screw)prism(V3(.46,0.0,0.0),V3(.51,0.0,0.0),.67,0x333A43,48)
    }
    val end=if(screw)2.55 else 2.9
    prism(V3(start,0.0,0.0),V3(end-.08,0.0,0.0),.31,metal,64)
    thread(if(screw).58 else if(type=="BOLT_HEX_FULL") .48 else 1.1,end-.12,.31,if(screw).19 else .115)
    ring(end-.08,.31,end,.25,metal)
    if(screw){
        // Two flats and a relieved cutting tip, distinct from the threaded shank.
        face(0xC7CCD1,V3(end,-.26,-.1),V3(end,.26,-.1),V3(3.12,.13,0.0),V3(3.12,-.13,0.0))
        face(0x7D8792,V3(end,-.26,.1),V3(end,.26,.1),V3(3.12,.13,0.0),V3(3.12,-.13,0.0))
        face(0x9AA7B4,V3(end,-.26,-.1),V3(3.12,-.13,0.0),V3(end,-.26,.1))
    }else prism(V3(end,0.0,0.0),V3(end+.001,0.0,0.0),.25,metal,64)
}
internal fun nutScene(type: String): SolidScene = SolidScene().apply {
    val thin=type=="Thin / jam nut";val cap=type=="Cap / dome nut";val nylon=type=="Nylon insert locknut";val flange=type=="Flange nut";val lock=type=="All-metal locknut"
    val h=if(thin).30 else .60;val r=.67;val hole=.32;val metal=0xC0C4C9
    // Six outer flats, slightly chamfered at both bearing faces.
    for(i in 0..5){val a=i*PI/3;val b=(i+1)*PI/3
        fun p(x:Double,rr:Double,angle:Double)=V3(x,rr*cos(angle),rr*sin(angle))
        face(metal,p(.055,r,a),p(.055,r,b),p(h-.055,r,b),p(h-.055,r,a))
        face(0xD6DADF,p(0.0,r*.94,a),p(0.0,r*.94,b),p(.055,r,b),p(.055,r,a))
        face(0xA3AAB2,p(h-.055,r,a),p(h-.055,r,b),p(h,r*.94,b),p(h,r*.94,a))}
    hexRing(0.0,r*.94,hole,metal);hexRing(h,r*.94,hole,metal)
    ring(0.0,hole,h,hole,0x737D88);thread(.025,h-.025,hole,.105,true)
    if(flange){ring(h,.78,h+.10,.78,metal);ring(h+.10,.78,h+.10,hole,metal);ring(h,.78,h,r*.85,metal)}
    if(nylon || lock){ring(-.16,.48,0.0,.48,metal);ring(-.16,.48,-.16,.32,if(nylon)0x547AA0 else metal);ring(-.16,.32,0.0,.32,if(nylon)0x3B5A7B else 0x747E89)}
    if(cap){val profile=listOf(0.0 to .56,-.18 to .56,-.38 to .49,-.52 to .34,-.60 to .08,-.61 to 0.0)
        for(i in 0 until profile.size-1)ring(profile[i].first,profile[i].second,profile[i+1].first,profile[i+1].second,metal)}
}




internal fun rivetScene(type: String): SolidScene = SolidScene().apply {
    val metal=0xBCC3CB
    if(type.startsWith("RN_")) {
        val hex=type=="RN_HEX";val cs=type=="RN_CS";val closed=type=="RN_CLOSED"
        val r=.42;val bore=.24
        if(hex) {
            // Open annular hex body, with six visible flat faces.
            for(i in 0..5) { val a=i*PI/3;val b=(i+1)*PI/3
                face(metal,V3(.18,r*cos(a),r*sin(a)),V3(1.6,r*cos(a),r*sin(a)),V3(1.6,r*cos(b),r*sin(b)),V3(.18,r*cos(b),r*sin(b))) }
            hexRing(1.6,r,if(closed)0.0 else bore,metal)
        } else {
            ring(.18,r,1.6,r,metal);ring(1.6,r,1.6,if(closed)0.0 else bore,metal)
            if(type=="RN_SPLINED" || cs) for(i in 0 until 40) {val a=i*2*PI/40;val b=a+.025
                face(0x89929B,V3(.22,r*cos(a),r*sin(a)),V3(.90,r*cos(a),r*sin(a)),V3(.90,r*cos(b),r*sin(b)),V3(.22,r*cos(b),r*sin(b))) }
        }
        ring(0.0,.63,.18,if(cs)r else .63,metal);ring(0.0,.63,0.0,bore,metal)
        if(!cs)ring(.18,.63,.18,r,metal)
        ring(0.0,bore,1.59,bore,0x737D88);thread(.75,1.55,bore,.10,true)
    } else {
        val cs=type=="RV_CS";val large=type=="RV_LARGE";val closed=type=="RV_CLOSED"
        val head=if(large).70 else .48
        if(cs) ring(0.0,head,.16,.26,metal) else {
            val profile=listOf(0.0 to .10,.025 to head*.65,.08 to head*.94,.16 to head)
            for(i in 0 until profile.lastIndex) ring(profile[i].first,profile[i].second,profile[i+1].first,profile[i+1].second,metal)
        }
        if(cs)ring(0.0,head,0.0,.10,metal)
        if(!cs)ring(.16,head,.16,.26,metal)
        ring(.16,.26,1.60,.26,metal);ring(1.60,.26,1.60,if(closed)0.0 else .10,metal)
        prism(V3(-1.25,0.0,0.0),V3(1.61,0.0,0.0),.085,0x7F8892,32)
        if(!closed) {ring(1.60,.14,1.70,.20,0x9DA5AE);ring(1.70,.20,1.76,.08,0x9DA5AE)}
    }
}

package com.structkit.app
import kotlin.math.*

internal data class DraftMember(val name:String,val kgPerM:Double,val h:Double,val b:Double,val tw:Double,val tf:Double) {
    val area get()=2*b*tf+(h-2*tf)*tw
    val iy get()=(b*h.pow(3)-(b-tw)*(h-2*tf).pow(3))/12
    val iz get()=(2*tf*b.pow(3)+(h-2*tf)*tw.pow(3))/12
    val wel get()=2*iy/h
    val valid get()=listOf(kgPerM,h,b,tw,tf).all{it.isFinite()&&it>0}&&h>2*tf&&b>tw
}
internal data class MemberScreen(val member:DraftMember,val ratio:Double,val deflectionMm:Double,val limitMm:Double)
internal data class SizingSuggestion(val column:MemberScreen?,val rafter:MemberScreen?)

/** Concept screening model, NOT a portal-frame analysis or an EN 1993 verification.
 * Full developed roof length is screened as one simply supported straight beam.
 * Columns use a fixed-base cantilever idealisation (including weak-axis K=2).
 * The two independent models do not establish frame joint forces or compatibility.
 */
internal fun suggestMembers(f:FrameGeometry,members:List<DraftMember>,dead:Double,snow:Double,wind:Double,grade:String):SizingSuggestion? {
    if(listOf(dead,snow,wind).any{!it.isFinite()||it<0||it>10}||dead+snow<=0||f.width>40||f.apex>12||f.slope>30)return null
    val nominal=when(grade){"S235"->235.0;"S275"->275.0;"S355"->355.0;else->return null}
    val candidates=members.filter{it.valid&&it.tf<=40&&it.tw<=40}.sortedBy{it.kgPerM}
    fun fy(m:DraftMember)=nominal-if(max(m.tf,m.tw)>16)20 else 0
    fun sectionOk(m:DraftMember,column:Boolean):Boolean {
        val eps=sqrt(235/fy(m))
        return (m.b-m.tw)/(2*m.tf)<=14*eps && (m.h-2*m.tf)/m.tw <= (if(column)42 else 124)*eps
    }
    val length=f.rafterPieceLength*(if(f.mono)1 else 2)*1000 // mm, conservative straight-beam screening length
    val height=(if(f.mono)f.apex else f.eaves)*1000
    val e=210000.0
    val rafter=candidates.asSequence().filter{sectionOk(it,false)}.map { m ->
        // kN/m = N/mm. Roof actions are per horizontal plan area.
        val self=m.kgPerM*9.80665/1000
        val g=dead*f.spacing+self;val q=snow*f.spacing;val w=wind*f.spacing
        val uls=max(1.35*g+1.5*q,abs(.9*g-1.5*w))
        val sls=max(g+q,abs(g-w))
        val moment=uls*length*length/8
        val deflection=5*sls*length.pow(4)/(384*e*m.iy)
        MemberScreen(m,max(moment/(fy(m)*m.wel),deflection/(length/250)),deflection,length/250)
    }.firstOrNull{it.ratio<=1}
    val rafterSelf=rafter?.member?.kgPerM?.times(9.80665/1000)?:0.0
    val column=if(rafter==null)null else candidates.asSequence().filter{sectionOk(it,true)}.map { m ->
        val axial=((1.35*dead+1.5*snow)*f.spacing*f.width/2+1.35*rafterSelf*length/2000+1.35*m.kgPerM*9.80665*height/1e6)*1000
        val lateral=1.5*wind*f.spacing // N/mm; full tributary wall pressure on each column
        val moment=lateral*height*height/2
        val ncr=PI*PI*e*min(m.iy,m.iz)/(2*height).pow(2)
        val slender=sqrt(m.area*fy(m)/ncr)
        val phi=.5*(1+.49*(slender-.2)+slender*slender)
        val chi=min(1.0,1/(phi+sqrt(max(0.0,phi*phi-slender*slender))))
        val stressRatio=axial/(chi*m.area*fy(m))+moment/(fy(m)*m.wel)
        val deflection=wind*f.spacing*height.pow(4)/(8*e*m.iy)
        MemberScreen(m,max(stressRatio,deflection/(height/150)),deflection,height/150)
    }.firstOrNull{it.ratio<=1}
    return SizingSuggestion(column,rafter)
}

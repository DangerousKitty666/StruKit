package com.structkit.app
import kotlin.math.*

internal data class WindResult(val meanPressure:Double,val peakPressure:Double,val external:Double,val internal:Double,val net:Double,val force:Double)
internal fun windSurface(speed:Double,turbulence:Double,rho:Double,area:Double,cpe:Double,cpi:Double,peakOverride:Double?=null):WindResult? {
    if(listOf(speed,turbulence,rho,area,cpe,cpi).any{!it.isFinite()} || speed !in 0.0..150.0 || turbulence !in 0.0..1.0 || rho !in .5..2.0 || area<=0 || abs(cpe)>5 || abs(cpi)>5)return null
    val mean=.5*rho*speed*speed/1000
    val peak=peakOverride?:((1+7*turbulence)*mean)
    if(!peak.isFinite()||peak<0||peak>100)return null
    val external=peak*cpe;val internal=peak*cpi;val net=external-internal
    val force=net*area
    if(!force.isFinite())return null
    return WindResult(mean,peak,external,internal,net,force)
}

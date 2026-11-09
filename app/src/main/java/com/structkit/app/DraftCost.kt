package com.structkit.app

internal data class DraftMaterialCost(val purchasedKg:Double,val uk:Double,val lt:Double)
internal fun draftMaterialCost(massKg:Double,ukPerKg:Double,ltPerKg:Double,allowancePercent:Double):DraftMaterialCost? {
    if(!listOf(massKg,ukPerKg,ltPerKg,allowancePercent).all{it.isFinite()}||massKg<=0||ukPerKg<=0||ltPerKg<=0||allowancePercent !in 0.0..100.0)return null
    val purchased=massKg*(1+allowancePercent/100)
    val uk=purchased*ukPerKg;val lt=purchased*ltPerKg
    return if(uk.isFinite()&&lt.isFinite())DraftMaterialCost(purchased,uk,lt)else null
}

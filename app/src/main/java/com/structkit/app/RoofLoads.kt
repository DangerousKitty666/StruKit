package com.structkit.app

import kotlin.math.cos

internal const val GRAVITY = 9.80665
internal fun roofPlanArea(area: Double, slope: Double, surfaceArea: Boolean): Double? {
    if (!area.isFinite() || area <= 0 || !slope.isFinite() || slope < 0 || slope >= 90) return null
    return if (surfaceArea) area * cos(Math.toRadians(slope)) else area
}
internal fun snowShape(slope: Double, retained: Boolean): Double {
    require(slope.isFinite() && slope >= 0 && slope < 90)
    return if (retained || slope <= 30) .8 else if (slope >= 60) 0.0 else .8 * (60 - slope) / 30
}
internal data class WaterResult(val litres: Double, val pressure: Double, val force: Double)
internal fun roofWater(planArea: Double, rainfallMm: Double, efficiencyPercent: Double, retainedMm: Double): WaterResult? {
    if (listOf(planArea, rainfallMm, efficiencyPercent, retainedMm).any { !it.isFinite() } || planArea <= 0 || rainfallMm < 0 || retainedMm < 0 || efficiencyPercent !in 0.0..100.0) return null
    val pressure = retainedMm * GRAVITY / 1000.0
    val result = WaterResult(planArea * rainfallMm * (efficiencyPercent / 100), pressure, pressure * planArea)
    return result.takeIf { it.litres.isFinite() && it.pressure.isFinite() && it.force.isFinite() }
}

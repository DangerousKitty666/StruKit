package com.structkit.app

import kotlin.math.*

internal data class FrameGeometry(val length: Double, val width: Double, val eaves: Double, val apex: Double,
    val mono: Boolean, val bays: Int, val spacing: Double, val columnLength: Double, val rafterLength: Double,
    val rafterPieceLength: Double, val slope: Double) {
    val frames: Int get() = bays + 1
    val area: Double get() = length * width
}
internal fun draftFrame(length: Double, width: Double, eaves: Double, apex: Double, maxBay: Double, mono: Boolean): FrameGeometry? {
    if (listOf(length,width,eaves,apex,maxBay).any { !it.isFinite() } || length !in .5..200.0 || width !in .5..100.0 || eaves !in 1.0..30.0 || apex < eaves || apex > 50 || maxBay !in 1.0..12.0) return null
    val run = if (mono) width else width/2
    val angle = Math.toDegrees(atan2(apex-eaves, run))
    if (angle > 60) return null
    val bays = ceil(length/maxBay).toInt().coerceAtLeast(1)
    val frames = bays+1
    val piece = hypot(run,apex-eaves)
    return FrameGeometry(length,width,eaves,apex,mono,bays,length/bays,
        frames*(if(mono) eaves+apex else 2*eaves), frames*piece*(if(mono)1 else 2),piece,angle)
}

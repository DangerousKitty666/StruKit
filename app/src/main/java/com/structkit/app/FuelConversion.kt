package com.structkit.app

// Exact gallon definitions; international mile = 1.609344 km. Source: NIST SP 811.
internal fun convertFuel(value:Double,from:Int,to:Int):Double? {
    if(!value.isFinite() || value<=0.0 || from !in 0..4 || to !in 0..4)return null
    val us=100.0*3.785411784/1.609344
    val uk=100.0*4.54609/1.609344
    val l100=when(from){0->value;1->100.0/value;2->uk/value;3->us/value;else->value*3.785411784/1.609344}
    return when(to){0->l100;1->100.0/l100;2->uk/l100;3->us/l100;else->l100*1.609344/3.785411784}.takeIf { it.isFinite() && it > 0 }
}

package com.structkit.app
internal data class MarketEstimate(val amount:Double,val suppliers:Int,val basis:String)
private fun netPrice(q:PriceQuote):Double? = when {
    q.tax.contains("ex",true)->q.amount
    q.tax.contains("incl",true)->q.amount/(if(q.country=="UK")1.20 else 1.21)
    else->null
}
private fun gradeMatches(actual:String,wanted:String):Boolean=when(wanted){
    "SS 304"->actual in listOf("304","304L","1.4301","1.4307")
    "SS 316"->actual in listOf("316","316L","1.4401","1.4404")
    "Galvanised"->actual.contains("GD",true)||actual.contains("galv",true)
    else->actual.startsWith(wanted,true)
}
internal fun marketEstimate(quotes:List<PriceQuote>,section:String,grade:String,country:String,mass:Double,unit:String,now:Long=System.currentTimeMillis()):MarketEstimate? {
    val available=quotes.filter{it.country==country&&it.unit==unit&&it.currency==(if(country=="UK")"GBP"else"EUR")&&it.checked<=now&&now-it.checked<180L*86400000&&it.amount.isFinite()&&it.amount>0}
    val exact=available.filter{sectionKey(it.section)==sectionKey(section)&&gradeMatches(it.grade,grade)}
    // Each retailer has equal weight; duplicate product listings cannot dominate the market mean.
    val means=exact.groupBy{it.supplier}.values.mapNotNull{rows->rows.mapNotNull(::netPrice).takeIf{it.isNotEmpty()}?.average()}
    if(means.isNotEmpty())return MarketEstimate(means.average(),means.size,"Comparable catalogue average")
    if(grade.startsWith("SS")||!mass.isFinite()||mass<=0)return null
    // Keep the previous budget method, recalibrated only from dimensionally known carbon hollow sections.
    fun hollowMass(name:String):Double? {
        val key=sectionKey(name);val nums=Regex("[0-9]+(?:\\.[0-9]+)?").findAll(key).map{it.value.toDouble()}.toList()
        val h:Double;val b:Double;val t:Double
        if(key.startsWith("SHS")&&nums.size>=2){h=nums[0];b=h;t=nums[1]}
        else if(key.startsWith("RHS")&&nums.size>=3){h=nums[0];b=nums[1];t=nums[2]}else return null
        if(t<=0||2*t>=minOf(h,b))return null
        return (h*b-(h-2*t)*(b-2*t))*.00785
    }
    val rates=available.filter{it.unit=="m"&&(it.grade=="Unspecified"||it.grade.startsWith("S"))}.groupBy{it.supplier}.values.mapNotNull{rows->
        rows.mapNotNull{q->val kg=hollowMass(q.section);val net=netPrice(q);if(kg!=null&&net!=null)(net/kg).takeIf{it in .1..20.0}else null}.takeIf{it.isNotEmpty()}?.average()
    }
    val rate=if(rates.isEmpty())if(country=="UK")1.55 else 1.40 else rates.average()
    return MarketEstimate(mass*rate,rates.size,"Mass-based carbon budget")
}

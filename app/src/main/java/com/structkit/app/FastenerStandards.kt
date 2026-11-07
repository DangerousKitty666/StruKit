package com.structkit.app

internal data class StandardNote(val label: String,val scope: String,val note: String,val url: String)
internal val fastenerStandards=listOf(
    StandardNote("ISO / EN ISO","International / European","ISO is the international designation. EN ISO identifies European adoption. Always match the part number and edition.","https://www.iso.org/committee/45446.html"),
    StandardNote("BS EN / BS EN ISO","United Kingdom","British adoption of an EN or EN ISO standard. BS EN 15048 and BS EN 14399 cover structural bolting assemblies.","https://knowledge.bsigroup.com/products/non-preloaded-structural-bolting-assemblies-general-requirements"),
    StandardNote("ČSN EN / ČSN EN ISO","Czech Republic","Czech adoption of European standards. ČSN EN ISO 4014 refers to the adopted hexagon-head bolt standard; verify the edition.","https://csnonline.agentura-cas.cz/podrobne.aspx"),
    StandardNote("DIN / DIN EN ISO","Germany","DIN-only numbers are often encountered in older drawings and catalogues. DIN EN ISO is the German adoption of EN ISO. A familiar replacement number does not prove dimensional interchangeability.","https://www.dinmedia.de/en"),
    StandardNote("NF EN · UNI EN · UNE EN · LST EN","Other European adoptions","French, Italian, Spanish and Lithuanian prefixes identify national adoptions. Match the full EN/ISO number, part and edition rather than the prefix alone.","https://www.cencenelec.eu/about-cen/cen-community/members/"),
    StandardNote("GOST / GOST ISO / GOST R","Russia and interstate specifications","GOST 7798-70 is a hexagon-head bolt reference encountered in drawings. GOST, GOST ISO and GOST R are distinct designations. Do not substitute an ISO or DIN part solely by visual similarity.","https://protect.gost.ru/gost?month=0&page=308&search=&year=0"),
    StandardNote("GOST 5915-70 / GOST 5916-70","Hexagon nuts / thin hexagon nuts","These are accuracy-class B nut references. Thread, dimensions and mechanical requirements must be checked against the specified GOST edition rather than assumed from an ISO nut table.","https://protect.gost.ru/gost?month=0&page=257&search=&year=0"),
    StandardNote("GOST 11371-78","Normal-series washers","Washer specification covering accuracy classes A and C. Check the selected size and class rather than assuming DIN 125 or ISO 7089 dimensions.","https://protect.gost.ru/gost/details/5b529c2e-1f16-42cb-9fb4-d21d8a28cec9"),
    StandardNote("ISO 4014 · historical DIN 931","Partially threaded hex bolts","Reference b depends on diameter and length range. Head widths at some diameters differ from older DIN dimensions.","https://www.dueemmesidol.it/en/products/standard-fasteners/screws-bolts/iso-4014/"),
    StandardNote("ISO 4017 · historical DIN 933","Hex screws, thread to head","Under-head length L includes the threaded shank. Incomplete thread and under-head runout are not full load-bearing thread.","https://www.iso.org/standard/72585.html"),
    StandardNote("ISO 4762 · historical DIN 912","Socket head cap screws","s identifies the internal hexagon, not the head outside diameter. k is the cylindrical head height.","https://bossard.co.jp/wp-content/uploads/2024/12/Bossard-selection-20241212.pdf"),
    StandardNote("ISO 7380-1 / ISO 7380-2","Button-head socket screws","Part 1 covers the button-head form; part 2 includes a collar. Reduced head loadability must be considered; these are not interchangeable with structural hex bolt assemblies.","https://www.iso.org/committee/45446.html"),
    StandardNote("ISO 4032 / ISO 4035","Hexagon nuts / thin nuts","Nut style and property class matter. A thin nut must not be assigned the load capacity of a full-height nut.","https://www.iso.org/committee/45446.html"),
    StandardNote("ISO 10511 · historical DIN 985","Thin prevailing-torque nuts with insert","A common reference family for nylon-insert locknuts. Confirm the applicable edition, height, thread range and prevailing-torque requirements; the historical DIN reference is not an unrestricted equivalence claim.","https://www.dinmedia.de/de/norm/din-en-iso-10511/3360015"),
    StandardNote("ISO 7089 / ISO 7093-1","Normal / large-series plain washers","Outside diameter, thickness, hardness and product grade depend on the specified washer standard.","https://www.iso.org/committee/45446.html"),
    StandardNote("EN 15048","Non-preloaded structural bolting","An assembly specification, not merely a bolt shape. Use the specified compatible bolt/nut assembly and documentation.","https://knowledge.bsigroup.com/products/non-preloaded-structural-bolting-assemblies-general-requirements"),
    StandardNote("EN 14399 series","High-strength assemblies for preloading","The series specifies matched assemblies and their suitability for preloading. Do not mix components based only on diameter and strength markings.","https://orangebook.arcelormittal.com/explanatory-notes/long-products/bolts-welds")
)

internal fun fixingSizes(type: String) = when(type) {
    "Socket head cap" -> boltRefs.filter{it.diameter in setOf(5,6,8,10,12,16,20,24)}
    "Button / dome head" -> boltRefs.filter{it.diameter in setOf(5,6,8,10,12,16)}
    else -> boltRefs
}
internal fun fixingS(type: String,d: Int): Int = when(type){
    "Socket head cap" -> mapOf(5 to 4,6 to 5,8 to 6,10 to 8,12 to 10,16 to 14,20 to 17,24 to 19).getValue(d)
    "Button / dome head" -> mapOf(5 to 3,6 to 4,8 to 5,10 to 6,12 to 8,16 to 10).getValue(d)
    else -> boltRefs.first{it.diameter==d}.afIso
}
internal fun fixingK(type: String,d: Int): Double = when(type){
    "Socket head cap" -> d.toDouble()
    "Button / dome head" -> mapOf(5 to 2.75,6 to 3.3,8 to 4.4,10 to 5.5,12 to 6.6,16 to 8.8).getValue(d)
    else -> boltHeadHeight.getValue(d)
}
internal fun threadReference(type: String,d: Int,length: Int,full: Boolean): String {
    if(type=="Hex head" && full) return "Thread to head; runout excluded"
    val b=if(type=="Hex head")2*d+when {length<=125->6;length<=200->12;else->25} else 2*d+12
    return if(length<=b) "Short length: thread-to-head range; verify runout" else "b reference $b mm; verify length table"
}

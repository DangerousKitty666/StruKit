package com.structkit.app

/** Offline engineering reference tables. Values should be checked against the governing standards. */
internal data class Deviations(val lowerUm: Int, val upperUm: Int) {
    init { require(lowerUm <= upperUm) }
}

internal data class IsoFit(val code: String, val group: String, val description: String)
internal val isoFits = listOf(
    IsoFit("H8/f7", "Clearance", "Running clearance · table verified to 120 mm"),
    IsoFit("H7/n6", "Transition", "Tight transition / interference · table verified to 120 mm"),
    IsoFit("H7/g6", "Clearance", "Sliding / precision clearance"),
    IsoFit("H7/h6", "Clearance", "Locating clearance"),
    IsoFit("H7/k6", "Transition", "Close location / transition"),
    IsoFit("H7/m6", "Transition", "Tighter transition"),
    IsoFit("H7/p6", "Interference", "Press / interference (diameter-dependent)")
)

internal data class FitResult(
    val nominal: Double, val fit: IsoFit, val step: String,
    val hole: Deviations, val shaft: Deviations
) {
    val holeMin get() = nominal + hole.lowerUm / 1000.0
    val holeMax get() = nominal + hole.upperUm / 1000.0
    val shaftMin get() = nominal + shaft.lowerUm / 1000.0
    val shaftMax get() = nominal + shaft.upperUm / 1000.0
    val minClearanceMm get() = holeMin - shaftMax
    val maxClearanceMm get() = holeMax - shaftMin
    val realCharacter get() = when {
        minClearanceMm >= -1e-8 -> "Clearance (zero possible)"
        maxClearanceMm <= 1e-8 -> "Interference (zero possible)"
        else -> "Transition (clearance or interference)"
    }
}

// Upper bound inclusive. E.g., 30 belongs to over 18 to 30, not over 30 to 50.
internal val diameterBounds = listOf(3.0, 6.0, 10.0, 18.0, 30.0, 50.0, 80.0, 120.0, 180.0, 250.0, 315.0, 400.0, 500.0)
private val h7 = intArrayOf(10, 12, 15, 18, 21, 25, 30, 35, 40, 46, 52, 57, 63)
private val g6lo = intArrayOf(-8,-12,-14,-17,-20,-25,-29,-34,-39,-44,-49,-54,-60)
private val g6hi = intArrayOf(-2,-4,-5,-6,-7,-9,-10,-12,-14,-15,-17,-18,-20)
private val h6lo = intArrayOf(-6,-8,-9,-11,-13,-16,-19,-22,-25,-29,-32,-36,-40)
private val k6lo = intArrayOf(0,1,1,1,2,2,2,3,3,4,4,4,5)
private val k6hi = intArrayOf(6,9,10,12,15,18,21,25,28,33,36,40,45)
private val m6lo = intArrayOf(2,4,6,7,8,9,11,13,15,17,20,21,23)
private val m6hi = intArrayOf(8,12,15,18,21,25,30,35,40,46,52,57,63)
private val p6lo = intArrayOf(6,12,15,18,22,26,32,37,43,50,56,62,68)
private val p6hi = intArrayOf(12,20,24,29,35,42,51,59,68,79,88,98,108)

internal fun calculateFit(diameter: Double, fit: IsoFit): FitResult? {
    if (!diameter.isFinite() || diameter <= 0.0 || diameter > 500.0) return null
    val idx = diameterBounds.indexOfFirst { diameter <= it }
    if (idx < 0) return null
    if(fit.code in setOf("H8/f7","H7/n6") && diameter>120)return null
    val shaft = when(fit.code.substringAfter('/')) {
        "f7" -> Deviations(intArrayOf(-16,-22,-28,-34,-41,-50,-60,-71)[idx],intArrayOf(-6,-10,-13,-16,-20,-25,-30,-36)[idx])
        "n6" -> Deviations(intArrayOf(4,8,10,12,15,17,20,23)[idx],intArrayOf(10,16,19,23,28,33,39,45)[idx])
        "g6" -> Deviations(g6lo[idx],g6hi[idx])
        "h6" -> Deviations(h6lo[idx],0)
        "k6" -> Deviations(k6lo[idx],k6hi[idx])
        "m6" -> Deviations(m6lo[idx],m6hi[idx])
        "p6" -> Deviations(p6lo[idx],p6hi[idx])
        else -> return null
    }
    val lo = if (idx == 0) 0.0 else diameterBounds[idx-1]
    val step = if (idx == 0) "Up to 3 mm" else "Over ${lo.toInt()} to ${diameterBounds[idx].toInt()} mm"
    return FitResult(diameter, fit, step, Deviations(0,if(fit.code.startsWith("H8"))intArrayOf(14,18,22,27,33,39,46,54)[idx]else h7[idx]),shaft)
}

internal data class KeySize(val over: Double, val upTo: Double, val b: Int, val h: Int,
    val shaftDepth: Double, val hubDepth: Double, val depthTolerance: Double)
// DIN 6885-1 metric key tables; t1 = shaft keyseat depth; t2 = hub keyseat depth.
internal val keySizes = listOf(
    KeySize(6.0,8.0,2,2,1.2,1.0,0.1),
    KeySize(8.0,10.0,3,3,1.8,1.4,0.1),
    KeySize(10.0,12.0,4,4,2.5,1.8,0.1),
    KeySize(12.0,17.0,5,5,3.0,2.3,0.1),
    KeySize(17.0,22.0,6,6,3.5,2.8,0.1),
    KeySize(22.0,30.0,8,7,4.0,3.3,0.2),
    KeySize(30.0,38.0,10,8,5.0,3.3,0.2),
    KeySize(38.0,44.0,12,8,5.0,3.3,0.2),
    KeySize(44.0,50.0,14,9,5.5,3.8,0.2),
    KeySize(50.0,58.0,16,10,6.0,4.3,0.2),
    KeySize(58.0,65.0,18,11,7.0,4.4,0.2),
    KeySize(65.0,75.0,20,12,7.5,4.9,0.2),
    KeySize(75.0,85.0,22,14,9.0,5.4,0.2),
    KeySize(85.0,95.0,25,14,9.0,5.4,0.2),
    KeySize(95.0,110.0,28,16,10.0,6.4,0.2),
    KeySize(110.0,130.0,32,18,11.0,7.4,0.2),
    KeySize(130.0,150.0,36,20,12.0,8.4,0.2),
    KeySize(150.0,170.0,40,22,13.0,9.4,0.2),
    KeySize(170.0,200.0,45,25,15.0,10.4,0.2),
    KeySize(200.0,230.0,50,28,17.0,11.4,0.2)
)
internal fun findKey(diameter: Double): KeySize? = keySizes.find {
    diameter > it.over && diameter <= it.upTo || (diameter == 6.0 && it.over == 6.0)
}

internal data class ConnectionDetail(val title: String, val location: String, val mode: Int,
    val description: String, val dimensions: String, val checks: String)
internal val connections = listOf(
    ConnectionDetail("Fin plate", "Beam to column web/flange", 0,
        "A single plate welded to the supporting member and bolted to the incoming beam web.",
        "Plate t, h, w; bolt Ø; vertical pitch p; end distance e; edge distance e₂; weld a.",
        "Bolt shear/bearing; fin-plate bending and shear; welds; supporting member; tying and rotation."),
    ConnectionDetail("Flexible end plate", "Beam to column / beam", 1,
        "Thin end plate welded to the beam web and bolted to the supporting flange or web.",
        "Plate t, h, b; bolt gauge g; pitch p; edge distance e; weld a.",
        "Bolt group; plate bending and shear; welds; supporting member; connection rotation."),
    ConnectionDetail("Double angle cleats", "Beam to beam / column", 2,
        "Two angle cleats bolted to the beam web and to the supporting member.",
        "Cleat legs a×b×t; bolt Ø; gauge g; pitch p; end distances.",
        "Bolt shear/bearing; angle leg bending; support local resistance; installation clearances."),
    ConnectionDetail("Flush end plate", "Moment joint", 3,
        "End plate welded to beam flanges and web; bolts transfer moments and shear.",
        "End-plate t; gauge g; row pitches; edge distances; flange/web welds.",
        "T-stub prying; bolts in tension; welds; column flange/web; joint stiffness and rotation."),
    ConnectionDetail("Extended end plate", "Moment beam-to-column", 4,
        "Plate projects beyond a beam flange to permit additional tension-bolt rows.",
        "Extension e; plate t; bolt row spacings; g; stiffener/weld details.",
        "T-stub components; bolts in combined action; welds; column panel zone; stiffeners."),
    ConnectionDetail("Bolted beam splice", "In-line beam splice", 5,
        "Flange cover plates and web cover plates connect two aligned beam sections.",
        "Cover plate t, b, L; bolt pitches; gauges; edge distances; splice gap.",
        "Flange force from bending; web shear; slip/bearing; net section; fit-up and erection."),
    ConnectionDetail("Seated beam", "Beam seated on column", 6,
        "Seat angle or plate supports vertical reaction; top restraint provides stability.",
        "Seat projection; plate/angle thickness; stiffener; bolts and welds.",
        "Seat bending; eccentric reaction; web bearing/buckling; stability and restraint."),
    ConnectionDetail("Column base plate", "Column to concrete base", 7,
        "Steel plate welded to column with anchors, grout and concrete foundation.",
        "Plate B×L×t; anchor gauge/pitch; grout thickness; edge distances; welds.",
        "Concrete bearing; anchor tension/shear; plate bending; uplift; concrete breakout and foundation.")
)

internal data class BoltRef(val diameter: Int, val pitch: Double, val afIso: Int, val afDin: Int)
internal val boltRefs = listOf(
    BoltRef(5,0.8,8,8),
    BoltRef(6,1.0,10,10), BoltRef(8,1.25,13,13), BoltRef(10,1.5,16,17),
    BoltRef(12,1.75,18,19), BoltRef(14,2.0,21,22), BoltRef(16,2.0,24,24),
    BoltRef(18,2.5,27,27), BoltRef(20,2.5,30,30), BoltRef(22,2.5,34,32),
    BoltRef(24,3.0,36,36), BoltRef(27,3.0,41,41), BoltRef(30,3.5,46,46)
)
// Common *catalogue length options* for ISO 4017 / DIN 933 fully threaded hex bolts.
// These are representative industry lengths within published standard size ranges,
// not a promise of supplier stock, plating, grade or partial-thread availability.
internal val typicalBoltLengths = mapOf(
    5 to listOf(10,12,16,20,25,30,35,40,45,50),
    6 to listOf(12,16,20,25,30,35,40,45,50,60),
    8 to listOf(16,20,25,30,35,40,45,50,55,60,70,80),
    10 to listOf(20,25,30,35,40,45,50,55,60,65,70,80,90,100),
    12 to listOf(25,30,35,40,45,50,55,60,65,70,75,80,90,100,110,120),
    14 to listOf(30,35,40,45,50,55,60,65,70,80,90,100,110,120,140),
    16 to listOf(30,35,40,45,50,55,60,65,70,75,80,90,100,110,120,130,140,150,160,180,200),
    18 to listOf(35,40,45,50,55,60,65,70,75,80,90,100,110,120,140,160,180,200),
    20 to listOf(40,45,50,55,60,65,70,75,80,90,100,110,120,130,140,150,160,180,200),
    22 to listOf(45,50,55,60,65,70,75,80,90,100,110,120,140,160,180,200),
    24 to listOf(50,55,60,65,70,75,80,90,100,110,120,130,140,150,160,180,200),
    27 to listOf(55,60,65,70,75,80,90,100,110,120,140,160,180,200),
    30 to listOf(60,65,70,75,80,90,100,110,120,130,140,150,160,180,200)
)
// Approximate nominal ISO 4017 head height k (mm); ISO 4014 shares head geometry.
internal val boltHeadHeight = mapOf(
    5 to 3.5,
    6 to 4.0, 8 to 5.3, 10 to 6.4, 12 to 7.5, 14 to 8.8, 16 to 10.0,
    18 to 11.5, 20 to 12.5, 22 to 14.0, 24 to 15.0, 27 to 17.0, 30 to 18.7
)
internal data class WasherType(val name: String, val standard: String, val use: String,
    val caution: String, val sketch: String)
internal val washerTypes = listOf(
    WasherType("Plain / normal", "ISO 7089 (DIN 125 A)",
        "General bearing surface beneath a bolt head or nut. Protects the surface and spreads local pressure.",
        "For preloaded structural assemblies, use the specified compatible hardened washer system.", "PLAIN"),
    WasherType("Large OD / penny", "ISO 7093-1 (DIN 9021)",
        "Larger bearing area for thin sheet, oversized holes or softer substrates, where design permits.",
        "A large outside diameter does not automatically make it appropriate for structural slip-resistant joints.", "LARGE"),
    WasherType("Chamfered", "ISO 7090",
        "Normal-series washer with one chamfered edge to clear the under-head fillet on compatible fasteners.",
        "Orientation and grade must comply with the selected bolt specification.", "CHAMFER"),
    WasherType("Split spring", "DIN 127 (legacy)",
        "Traditional split spring washer used in certain general-purpose, non-critical assemblies.",
        "Not a reliable anti-loosening solution under vibration or a substitute for structural preloading.", "SPLIT"),
    WasherType("Serrated / tooth", "DIN 6798 family",
        "Teeth bite into the contact surfaces; used for some light-duty anti-rotation and electrical bonding applications.",
        "Can damage coatings and is generally unsuitable where an approved pretensioned structural assembly is required.", "TOOTH"),
    WasherType("Bonded sealing", "Manufacturer-specific EPDM bonded washer",
        "Metal washer with bonded elastomer ring, commonly used on roofing and cladding screws for a weather seal.",
        "Confirm sealing compound, roof profile, compression and corrosion compatibility.", "SEAL")
)
internal data class WasherSize(val d: Int, val bore: Double, val largeBore: Double, val normalOd: Double,
    val normalThick: Double, val largeOd: Double, val largeThick: Double)
// Nominal values, mm; ISO 7089 and ISO 7093-1 reference dimensional tables.
internal val washerSizes = listOf(
    WasherSize(5,5.3,5.3,10.0,1.0,15.0,1.2),
    WasherSize(6,6.4,6.4,12.0,1.6,18.0,1.6),
    WasherSize(8,8.4,8.4,16.0,1.6,24.0,2.0),
    WasherSize(10,10.5,10.5,20.0,2.0,30.0,2.5),
    WasherSize(12,13.0,13.0,24.0,2.5,37.0,3.0),
    WasherSize(14,15.0,15.0,28.0,2.5,44.0,3.0),
    WasherSize(16,17.0,17.0,30.0,3.0,50.0,3.0),
    WasherSize(18,19.0,19.0,34.0,3.0,56.0,4.0),
    WasherSize(20,21.0,21.0,37.0,3.0,60.0,4.0),
    WasherSize(24,25.0,25.0,44.0,4.0,72.0,5.0),
    WasherSize(30,31.0,33.0,56.0,4.0,92.0,6.0)
)

internal data class CatalogueExample(val family: String, val designation: String, val market: String,
    val supplier: String, val spec: String, val url: String)
internal val supplierExamples = listOf(
    CatalogueExample("HEX","M12 × 30","UK","F.H. Brundle","Galvanised hex bolt; listing reference 5002HS12X30",
        "https://www.fhbrundle.co.uk/products/5002HS12X30__M12_x_30mm_Hexagon_Bolt_Galvanised_packed_in_100%27s__%28sold_each%29"),
    CatalogueExample("HEX","M12 × 16","UK","Orbital Fasteners","Grade 8.8 full-thread hex setscrew, DIN 933",
        "https://www.orbitalfasteners.co.uk/categories/m12-hexagon-head-set-screws"),
    CatalogueExample("HEX","M10 × 40","LT","Varžtų Pasaulis","DIN 931 / ISO 4014; catalogue reference (2022)",
        "https://www.varztupasaulis.lt/static/media/VP_katalogas.pdf"),
    CatalogueExample("HEX","M12 × 40","LT","Senukai","DIN 933, grade 8.8 zinc plated",
        "https://www.senukai.lt/p/varztas-din-933-haushalt-12-mm-x-40-mm-100-vnt/dwh2"),
    CatalogueExample("HEX","M12 × 30","LT","Senukai","DIN 933, grade 8.8 zinc plated",
        "https://www.senukai.lt/p/varztas-din-933-haushalt-12-mm-x-30-mm-100-vnt/dwh1"),
    CatalogueExample("TEK","5.5 × 25","UK","F.H. Brundle","TEK No.3 hex self-drilling screw; 100 pack",
        "https://www.fhbrundle.co.uk/fixings-and-fastenings/screws"),
    CatalogueExample("TEK","5.5 × 32","UK","F.H. Brundle","TEK No.3 hex self-drilling screw; 16 mm washer",
        "https://www.fhbrundle.co.uk/products/5002TEK35532__TEK_5.5_x_32_Hex_No.3_S/Drill_Screw_Pack_Qty_100"),
    CatalogueExample("TEK","5.5 × 50","UK","F.H. Brundle","TEK No.3 hex self-drilling screw; 16 mm washer",
        "https://www.fhbrundle.co.uk/products/5002TEK35550__TEK_5.5_x_50_Hex_No.3_S/Drill_Screw_Pack_Qty_100"),
    CatalogueExample("TEK","5.5 × 55","UK","F.H. Brundle","TEK No.5 hex self-drilling screw; 16 mm washer",
        "https://www.fhbrundle.co.uk/products/5002TEK55555__TEK_5.5_x_55_Hex_No.5_S/Drill_Screw_Pack_Qty_100"),
    CatalogueExample("TEK","4.2 × 25","LT","Senukai","DIN 7504K hex head; drill point",
        "https://www.senukai.lt/p/savisriegis-haushalt-4-2-mm-x-25-mm-25-vnt/dpwt"),
    CatalogueExample("TEK","5.5 × 25","LT","Senukai","DIN 7504K hex self-drilling screw",
        "https://www.senukai.lt/p/savisriegis-haushalt-din-7504k-5-5-mm-x-25-mm-20-vnt/a9nj"),
    CatalogueExample("TEK","5.5 × 38","LT","Senukai","DIN 7504K hex self-drilling screw",
        "https://www.senukai.lt/p/savisriegis-haushalt-din-7504k-5-5-mm-x-38-mm-250-vnt/d24r")
)

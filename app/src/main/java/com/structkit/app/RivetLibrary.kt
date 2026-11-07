package com.structkit.app
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val refInk=Color(0xFFD9DDE3)
private val refMuted=Color(0xFFA5AFBB)
private val refBlue=Color(0xFF9EC5E8)
@Composable private fun Note(s: String) { Text(s,color=refMuted,fontSize=12.sp,modifier=Modifier.padding(vertical=8.dp)) }
@Composable private fun Cells(values: List<String>, header:Boolean=false) {
    Row(Modifier.fillMaxWidth().padding(vertical=9.dp)) { values.forEach { Text(it,color=if(header)refBlue else refInk,fontSize=12.sp,modifier=Modifier.weight(1f)) } }
    HorizontalDivider(color=Color(0xFF343A43),thickness=.5.dp)
}
@Composable private fun Link(label:String,url:String) {val uri=LocalUriHandler.current;TextButton(onClick={uri.openUri(url)}){Text("$label ↗")}}
private const val rnSource="https://www.gesipa.co.uk/fileadmin/user_upload/pdf/Threaded_Fastener_Booklet_2018.pdf"
private data class RivnutSize(val m:Int,val body:Double,val hole:Double,val length:Double,val head:Double,val grip:String,val part:String,val k:Double=1.5)
private val rivnutSizes=listOf(
    RivnutSize(5,7.0,7.1,13.5,10.0,"1.5–4.0","1455367"),
    RivnutSize(6,9.0,9.1,17.0,12.0,"1.5–4.5","1455369"),
    RivnutSize(8,11.0,11.1,18.5,14.0,"1.5–4.5","1433715"),
    RivnutSize(10,12.0,12.1,19.0,15.0,"1.5–4.5","1455372"),
    RivnutSize(12,15.9,16.0,26.5,19.0,"1.7–4.5","on request",1.7))

@Composable internal fun RivnutHoleChart() {
    Text("Holes for rivnuts",color=refBlue,fontSize=19.sp)
    Note("GESIPA · steel, splined, open-end, countersunk head · catalogue pages 12–13. Dimensions in mm; these drill sizes apply to this series only.")
    InfoPanel(Modifier.fillMaxWidth()) { FixingSolid("RN_CS",Modifier.fillMaxWidth().height(285.dp)) }
    Cells(listOf("Thread","Body D₁","Hole Ø","Grip"),true)
    rivnutSizes.forEach { Cells(listOf("M${it.m}","${it.body}","${it.hole}",it.grip)) }
    Note("A drilled hole is specified by the insert body, not its thread. For example, this M10 body needs Ø12.1; a different GESIPA M10 body is 12.9 and needs Ø13.0. Match the exact part before drilling.")
    Note("Deburr the hole, check finished diameter and sheet grip after coatings. Countersunk inserts also need the specified countersink. Hex and half-hex bodies need a matching hexagonal hole; a round drill does not produce it.")
    DrillAvailability(true)
    Link("Manufacturer dimensions",rnSource)
}

@Composable internal fun RivnutCatalogue() {
    var type by remember { mutableStateOf("Round / splined") }
    val types=listOf("Round / splined","Hex body","Countersunk","Closed end")
    InfoPanel {
        ToggleChips(types, type) { type = it }
        val id=when(type){"Hex body"->"RN_HEX";"Countersunk"->"RN_CS";"Closed end"->"RN_CLOSED";else->"RN_SPLINED"}
        FixingSolid(id,Modifier.fillMaxWidth().height(285.dp))
    }
    Note("M = internal thread · D₁ = body diameter (A/F for hex bodies) · D₂ = head diameter · k = head height · L = body length. Illustrations show the unset insert; proportions are schematic.")
    Note(when(type){"Hex body"->"Full or half-hex bodies resist rotation in a matching hexagonal hole. Available in steel, stainless and aluminium, commonly M5–M12; verify size range and grip for the selected product."
        "Countersunk"->"Flush head after the specified countersink. L includes the countersunk head; check remaining sheet thickness and catalogue grip convention."
        "Closed end"->"Closed base limits ingress through the insert. Allow clearance for the screw tip; closure alone does not certify the whole joint as watertight."
        else->"Round smooth or splined bodies install in round holes. Splines improve anti-rotation. Head choices include standard flange and reduced head; grip and body diameter vary by series."})
    Text("Reference size chart · countersunk splined steel",color=refBlue,fontSize=16.sp)
    Note("GESIPA open-end series, pages 12–13. This chart is independent of the illustrative type selector above.")
    Cells(listOf("M","D₁","D₂","L","k"),true)
    rivnutSizes.forEach { Cells(listOf("M${it.m}","${it.body}","${it.head}","${it.length}","${it.k}")) }
    rivnutSizes.forEach { Note("M${it.m}: grip ${it.grip} · hole Ø${it.hole} · part ${it.part}") }
    DrillAvailability(true)
    Link("Full series and grip ranges",rnSource)
}

@Composable internal fun RivetCatalogue() {
    var selected by remember { mutableStateOf("Dome head") }
    InfoPanel {
        ToggleChips(listOf("Dome head","Large flange","Countersunk","Closed end"), selected) { selected = it }
        val id=when(selected){"Large flange"->"RV_LARGE";"Countersunk"->"RV_CS";"Closed end"->"RV_CLOSED";else->"RV_DOME"}
        FixingSolid(id,Modifier.fillMaxWidth().height(190.dp))
    }
    Note("Blind rivet · front elevation, unset. d = body Ø · L = body length (under head, or total for countersunk) · k = head height. The long narrow stem is the pulling mandrel. Rivets use Ø sizes, not M thread sizes.")
    Note(when(selected){"Large flange"->"Larger head spreads contact pressure on thin or soft sheet. Check bearing and pull-through resistance of the actual joint."
        "Countersunk"->"Flush finish requires a matched countersink and enough remaining sheet. Catalogue length includes the head."
        "Closed end"->"Sealed body contains the mandrel end. Use a tested product and sealing system where leakage performance is specified."
        else->"General-purpose open-end blind rivet. Choose body and mandrel materials for corrosion compatibility and required joint loads."})
    Text("GESIPA catalogue examples · mm",color=refBlue,fontSize=16.sp)
    Cells(listOf("d × L","Hole Ø","Head Ø"),true)
    Cells(listOf("2.4 × 6","2.5","5.0"));Cells(listOf("3 × 6","3.1","6.5"));Cells(listOf("4 × 6","4.1","8.0"));Cells(listOf("4.8 × 10.3*","4.9","16.5"))
    Note("Aluminium/steel dome-head examples: 1433464, 1433530, 1433533. *1455522 is aluminium/aluminium large-flange. Sizes shown are product-specific; select a catalogue length whose grip range covers the total joined thickness. Head height and tolerances depend on the part.")
    Note("Other common families: multi-grip (wider permitted stack range), structural locked-mandrel (specified load performance), and peel rivets (soft substrates). Do not assign structural-rivet capacities to general-purpose rivets.")
    DrillAvailability(false)
    Link("Manufacturer catalogue", "https://www.gesipa.co.uk/products/standard-blind-rivets/")
}

@Composable internal fun SupplierReferences() {
    Text("Reference",color=refBlue,fontSize=19.sp,modifier=Modifier.padding(top=10.dp))
    Note("Supplier catalogues · external links require internet. Lithuania: established specialist suppliers; no market-share ranking is implied.")
    Link("UK · Orbital Fasteners","https://www.orbitalfasteners.co.uk/")
    Link("UK · UK Fixings","https://www.ukfixings.net/")
    Link("UK · F.H. Brundle","https://www.fhbrundle.co.uk/")
    Link("LT · Varžtų pasaulis","https://www.varztupasaulis.lt/")
    Link("LT · ALBA varžtai","https://www.varztai.lt/")
}

private const val drillSource = "https://static-content.cromwell.co.uk/pdfs/s/Medusa_Attachments/Dormer/Data%20Sheets/catalouge-data-sheet-A002.pdf"
@Composable private fun DrillAvailability(rivnuts: Boolean) {
    Text("Drill-size check", color=refBlue, fontSize=16.sp, modifier=Modifier.padding(top=12.dp))
    Cells(listOf("Hole / drill mm", "Drill catalogue"), true)
    val sizes = if(rivnuts) listOf("7.1", "9.1", "11.1", "12.1", "16.0") else listOf("2.5", "3.1", "4.1", "4.9")
    sizes.forEach { size -> Cells(listOf(size, "Dormer A002$size")) }
    Note("These are commercially catalogued metric drill diameters. A 0.5 mm-step drill set does not include the .1 or .9 sizes: buy the specified size separately. Do not round a 4.9 mm hole to 5.0 mm, or a 7.1 mm hole to 7.5 mm, unless the fixing manufacturer's permitted hole range allows it.")
    Note("Nominal drill diameter is a starting tool size, not a guarantee of finished hole size. Check runout, burrs and the finished hole against the exact fixing specification. Catalogue availability is not a live stock check.")
    Link("Drill manufacturer's size catalogue", drillSource)
}

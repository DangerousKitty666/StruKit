package com.structkit.app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.max
import kotlin.math.min
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

private val bg4 = Color(0xFF08090B)
private val plate4 = Color(0xFF14171B)
private val text4 = Color(0xFFD9DDE3)
private val muted4 = Color(0xFFA5AFBB)
private val blue4 = Color(0xFF9EC5E8)
private val line4 = Color(0xFF48515C)

@Composable internal fun InfoPanel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.clip(RoundedCornerShape(13.dp)).background(plate4, RoundedCornerShape(13.dp)).padding(15.dp), content = content)
}
@Composable private fun SmallTag(title: String) {
    Text(title.uppercase(), color = muted4, fontSize = 10.sp, letterSpacing = 1.3.sp, fontWeight = FontWeight.Medium)
}
@Composable private fun DataPair(label: String, value: String, highlighted: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = muted4, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = if (highlighted) blue4 else text4, fontSize = 13.sp,
            fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Normal)
    }
    HorizontalDivider(color = line4.copy(alpha = .43f), thickness = .5.dp)
}
@Composable internal fun ToggleChips(options: List<String>, selected: String, onChoose: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().fadedHorizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        options.forEach { option ->
            FilterChip(shape = RoundedCornerShape(9.dp), colors = FilterChipDefaults.filterChipColors(containerColor = plate4, labelColor = text4, selectedContainerColor = Color(0xFF253442), selectedLabelColor = blue4), border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected == option, borderColor = line4, selectedBorderColor = blue4), selected = selected == option, onClick = { onChoose(option) },
                label = { Text(option, fontSize = 12.sp) })
        }
    }
}
private fun format3(n: Double): String = String.format(Locale.US, "%.3f", n)
private fun signUm(v: Int) = if (v > 0) "+$v" else "$v"


@Composable internal fun ToolTabRail(items: List<String>, selected: String, short: (String) -> String = { it }, onSelect: (String) -> Unit) {
    BoxWithConstraints(Modifier.width(48.dp).fillMaxHeight()) {
        val railHeight = minOf(minOf(maxHeight, maxHeight * .62f + 192.dp), (items.size * 62 + 24).dp)
        Column(Modifier.align(Alignment.BottomCenter).height(railHeight).fillMaxWidth()
            .fadedVerticalScroll(rememberScrollState()).padding(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items.forEach { item ->
                val active = item == selected
                Box(Modifier.fillMaxWidth().height(56.dp)
                    .border(1.dp, if(active) blue4 else line4, RoundedCornerShape(9.dp))
                    .background(if(active) Color(0xFF253442) else plate4, RoundedCornerShape(9.dp))
                    .clickable { onSelect(item) }, contentAlignment = Alignment.Center) {
                    Text(short(item), modifier = Modifier.requiredWidth(54.dp).rotate(-90f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, color = if(active) blue4 else text4,
                        fontSize = 10.sp, fontWeight = FontWeight.Medium, maxLines = 1)
                }
            }
        }
    }
}
@Composable internal fun ToleranceTool() {
    var mode by remember { mutableStateOf("Shaft / hole") }
    Row(Modifier.fillMaxSize().padding(start = 12.dp, end = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight().fadedVerticalScroll(rememberScrollState())) {
            Text("Tolerances", color = text4, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
            Text("ISO limits & fits · keys · rivnut holes", color = muted4, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            when(mode) { "Shaft / hole" -> FitScreen(); "Shaft keys" -> KeyScreen(); "Drill sizes" -> HoleTools(); else -> RivnutHoleChart() }
            Spacer(Modifier.height(18.dp))
        }
        ToolTabRail(listOf("Shaft / hole", "Shaft keys", "Rivnut holes", "Drill sizes"), mode,
            { when(it) { "Shaft / hole" -> "Fits"; "Shaft keys" -> "Keys"; "Drill sizes" -> "Drill sizes"; else -> "Rivnuts" } }) { mode = it }
    }
}

@Composable private fun FitScreen() {
    var diameterText by remember { mutableStateOf("25") }
    var category by remember { mutableStateOf("Clearance") }
    var selectedFit by remember { mutableStateOf("H7/g6") }
    val available = isoFits.filter { it.group == category }
    val diameter = diameterText.replace(',', '.').toDoubleOrNull()
    val fit = isoFits.first { it.code == selectedFit }
    val result = diameter?.let { calculateFit(it, fit) }
    InfoPanel(Modifier.fillMaxWidth()) {
        SmallTag("Nominal diameter · mm")
        OutlinedTextField(value = diameterText, onValueChange = { diameterText = it },
            label = { Text("Ø d") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
        Spacer(Modifier.height(10.dp))
        SmallTag("Fit character")
        ToggleChips(listOf("Clearance", "Transition", "Interference"), category) {
            category = it
            selectedFit = isoFits.first { f -> f.group == category }.code
        }
        Spacer(Modifier.height(8.dp))
        SmallTag("ISO 286 fit")
        ToggleChips(available.map { it.code }, selectedFit) { selectedFit = it }
        Spacer(Modifier.height(6.dp))
        Text(fit.description, color = muted4, fontSize = 12.sp)
    }
    Spacer(Modifier.height(12.dp))
    InfoPanel(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("HOLE / SHAFT", color = text4, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text("schematic · NTS", color = muted4, fontSize = 10.sp)
        }
        Spacer(Modifier.height(5.dp))
        if(result!=null) FitDiagram(result,Modifier.fillMaxWidth())
        Text("Upper section is schematic; lower tolerance zones use the actual deviations.",
            color = muted4, fontSize = 11.sp)
    }
    Spacer(Modifier.height(12.dp))
    if (result == null) {
        InfoPanel(Modifier.fillMaxWidth()) {
            Text("Enter Ø >0 to 500 mm; H8/f7 and H7/n6 are available only to 120 mm.", color = muted4, fontSize = 13.sp)
        }
    } else {
        InfoPanel(Modifier.fillMaxWidth()) {
            SmallTag("Dimensional limits • mm")
            Spacer(Modifier.height(5.dp))
            Text("${result.fit.code}    ·    ${result.step}", color = blue4, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(10.dp))
            DataPair("Hole deviations", "${signUm(result.hole.upperUm)} / ${signUm(result.hole.lowerUm)} µm")
            DataPair("Hole min → max", "${format3(result.holeMin)} — ${format3(result.holeMax)}")
            DataPair("Shaft deviations", "${signUm(result.shaft.upperUm)} / ${signUm(result.shaft.lowerUm)} µm")
            DataPair("Shaft min → max", "${format3(result.shaftMin)} — ${format3(result.shaftMax)}")
            Spacer(Modifier.height(6.dp))
            DataPair("Minimum clearance", "${format3(result.minClearanceMm)} mm", highlighted = true)
            DataPair("Maximum clearance", "${format3(result.maxClearanceMm)} mm", highlighted = true)
            Text(result.realCharacter, color = text4, fontSize = 12.sp, modifier = Modifier.padding(top = 9.dp))
            Text("A negative clearance means interference. Values at the size-step boundary use the lower interval.",
                color = muted4, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
        }
    }
    if(result!=null) FitExtra(result)
    SourceLink("Schaeffler · published shaft deviation tables", "https://www.schaeffler-publications.com/pdf/HG1_en-GB.pdf")
    Spacer(Modifier.height(11.dp))
    Text("Reference: ISO 286-1/2 (selected hole-basis combinations). This is an offline reference, not a certified gauge specification.",
        color = muted4, fontSize = 11.sp, lineHeight = 15.sp)
}

@Composable private fun KeyScreen() {
    var diameterText by remember { mutableStateOf("35") }
    val diameter = diameterText.replace(',', '.').toDoubleOrNull()
    val data = diameter?.let(::findKey)
    InfoPanel(Modifier.fillMaxWidth()) {
        SmallTag("Shaft diameter · mm")
        OutlinedTextField(diameterText, { diameterText = it }, label = { Text("Ø d") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(7.dp))
        Text("DIN 6885-1 · metric parallel keys", color = muted4, fontSize = 12.sp)
    }
    Spacer(Modifier.height(12.dp))
    InfoPanel(Modifier.fillMaxWidth()) {
        SmallTag("Key & keyseats · schematic")
        TechnicalSketch("KEY", Modifier.fillMaxWidth().height(180.dp))
        Text("b = width · h = key height · t₁ = shaft depth · t₂ = hub depth",
            color = muted4, fontSize = 11.sp)
    }
    Spacer(Modifier.height(12.dp))
    InfoPanel(Modifier.fillMaxWidth()) {
        if (data == null) {
            Text("Enter shaft Ø from 6 to 230 mm. Boundary sizes use the preceding key range.",
                color = muted4, fontSize = 12.sp)
        } else {
            SmallTag("Recommended nominal key")
            Text("${data.b} × ${data.h} mm", color = blue4, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
            DataPair("Shaft Ø range", "> ${data.over.toInt()} to ${data.upTo.toInt()} mm")
            DataPair("Key width × height", "${data.b} × ${data.h} mm")
            DataPair("Shaft groove t₁", "${format3(data.shaftDepth)}  +${format3(data.depthTolerance)} / 0")
            DataPair("Hub groove t₂", "${format3(data.hubDepth)}  +${format3(data.depthTolerance)} / 0")
            Spacer(Modifier.height(5.dp))
            SmallTag("Nominal groove width fits")
            DataPair("Shaft keyway width b", "P9 (tight)")
            DataPair("Hub keyway width b", "P9 / JS9 (by design)")
            Text("Width P9/JS9 are ISO tolerance-zone designations, not numerical width limits. Verify the specified shaft/hub fit and key tolerance before cutting.",
                color = muted4, fontSize = 11.sp, lineHeight = 15.sp,
                modifier = Modifier.padding(top = 7.dp))
        }
    }
    Spacer(Modifier.height(9.dp))
    Text("Reference: DIN 6885-1 key and keyseat tables. Tolerances shown for standard-depth keyseats only; material and torque design are not evaluated.",
        color = muted4, fontSize = 11.sp, lineHeight = 15.sp)
}

@Composable internal fun ConnectionTool() {
    var selected by remember { mutableIntStateOf(0) }
    val beamOptions = connections.take(7) + additionalBeamConnections
    val detail = beamOptions[selected]
    var reverseView by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().fadedVerticalScroll(rememberScrollState()).padding(horizontal = 15.dp)) {
        var menuOpen by remember { mutableStateOf(false) }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Steel connections", color = text4, fontSize = 24.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Box {
                OutlinedButton(onClick = { menuOpen = true }, border = androidx.compose.foundation.BorderStroke(1.dp, blue4), shape = RoundedCornerShape(9.dp)) { Text("Select ▾", color = blue4) }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = Color.Black, modifier = Modifier.heightIn(max = 800.dp).widthIn(max = 340.dp).border(1.dp, blue4, RoundedCornerShape(9.dp))) {
                    beamOptions.forEachIndexed { idx, c ->
                        DropdownMenuItem(text = { Text(c.title, fontSize = 13.sp, color = blue4) },
                            leadingIcon = { ConnectionThumbnail(c.mode) },
                            onClick = { selected = idx; reverseView = false; menuOpen = false })
                    }
                }
            }
        }
        Text("Beam connection concepts · illustrative details", color = muted4, fontSize = 12.sp)
        Spacer(Modifier.height(12.dp))
        InfoPanel(Modifier.fillMaxWidth()) {
            SmallTag("${selected+1} / ${beamOptions.size}  ·  ${detail.location}")
            Spacer(Modifier.height(6.dp))
            Text(detail.title, color = text4, fontSize = 21.sp, fontWeight = FontWeight.Medium)
            ToggleChips(listOf("Front isometric", "Reverse isometric"), if (reverseView) "Reverse isometric" else "Front isometric") { reverseView = it == "Reverse isometric" }
            ConnectionSolid(detail.mode, reverseView, Modifier.fillMaxWidth().height(270.dp))
            Text("Steel grey: members · Blue: plates · Gold: highlighted weld locations. Illustrative proportions, not a fabrication drawing.",
                color = muted4, fontSize = 11.sp)
        }
        Spacer(Modifier.height(12.dp))
        InfoPanel(Modifier.fillMaxWidth()) {
            SmallTag("Arrangement")
            Spacer(Modifier.height(5.dp))
            Text(detail.description, color = text4, fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(14.dp))
            SmallTag("Preparation and plate attachment")
            Text(beamPreparation[detail.mode].orEmpty(), color = text4, fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(14.dp))
            SmallTag("Typical drawing dimensions")
            Spacer(Modifier.height(6.dp))
            Text(detail.dimensions, color = text4, fontSize = 13.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(14.dp))
            SmallTag("Engineering checks to perform")
            Spacer(Modifier.height(6.dp))
            Text(detail.checks, color = text4, fontSize = 13.sp, lineHeight = 19.sp)
        }
        Spacer(Modifier.height(12.dp))
        val referenceId = beamReferences[detail.mode]
        if (referenceId != null) {
            val context = LocalContext.current
            Text("Video reference · Comten Consulting Engineers ↗", color = blue4, fontSize = 12.sp,
                modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$referenceId"))) }.padding(vertical = 12.dp))
        }
        Text("REFERENCE ONLY — not engineered or pre-approved connections. Design and detail to BS EN 1993-1-8, relevant National Annex and project requirements. Consider SCI P358 / P398, fabrication access, tying, robustness, fatigue and fire as applicable.",
            color = muted4, fontSize = 11.sp, lineHeight = 16.sp)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable internal fun FixingTool() {
    var family by remember { mutableStateOf("Bolts") }
    Row(Modifier.fillMaxSize().padding(start = 12.dp, end = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    key(family) {
    Column(Modifier.weight(1f).fillMaxHeight().fadedVerticalScroll(rememberScrollState())) {
        Text("Fixing library", color = text4, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
        Text("Fastener dimensions and application notes", color = muted4, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        Spacer(Modifier.height(10.dp))
        when (family) {
            "Bolts" -> BoltCatalogue()
            "Nuts" -> NutsCatalogue()
            "Washers" -> WasherCatalogue()
            "Rivets" -> RivetCatalogue()
            "Rivnuts" -> RivnutCatalogue()
            else -> SelfDrillingCatalogue()
        }
        Spacer(Modifier.height(17.dp))
        SupplierReferences()
        Spacer(Modifier.height(18.dp))
    }
    }
    ToolTabRail(listOf("Bolts", "Nuts", "Washers", "Self-drilling", "Rivets", "Rivnuts"), family, short = { if(it == "Self-drilling") "TEK" else it }, onSelect = { family = it })
    }
}

@Composable private fun BoltCatalogue() {
    var expandedDiameter by remember { mutableStateOf<Int?>(12) }
    var boltType by remember { mutableStateOf("Hex head") }
    var fullThread by remember { mutableStateOf(false) }
    val sketch = when (boltType) {
        "Socket head cap" -> "BOLT_SOCKET"
        "Button / dome head" -> "BOLT_DOME"
        else -> if(fullThread) "BOLT_HEX_FULL" else "BOLT_HEX"
    }
    InfoPanel(Modifier.fillMaxWidth()) {
        SmallTag("Bolt types")
        ToggleChips(listOf("Hex head", "Socket head cap", "Button / dome head"), boltType) { boltType = it }
        if (boltType == "Hex head") ToggleChips(listOf("ISO 4014 · partial", "ISO 4017 · full"), if(fullThread) "ISO 4017 · full" else "ISO 4014 · partial") { fullThread = it.contains("4017") }
        Spacer(Modifier.height(8.dp))
        FixingSolid(sketch, Modifier.fillMaxWidth().height(210.dp))
        Text("d = thread Ø · p = coarse pitch · s = across flats (head or socket) · k = head height · L = length under head · b = threaded length",
            color = muted4, fontSize = 11.sp, lineHeight = 15.sp)
    }
    Spacer(Modifier.height(12.dp))
    SmallTag(if(boltType == "Hex head") "Hex-head dimensions · mm" else "Socket dimensions · s = key size · mm")
    Spacer(Modifier.height(7.dp))
    Row(Modifier.fillMaxWidth().background(plate4, RoundedCornerShape(8.dp)).padding(10.dp)) {
        BoltHead("d", 1f); BoltHead("p", 1f); BoltHead("s", 1.2f); BoltHead("k", 1.2f)
    }
    fixingSizes(boltType).forEach { b ->
        val expanded = expandedDiameter == b.diameter
        Column(Modifier.fillMaxWidth().background(if (expanded) Color(0xFF17212B) else bg4)) {
            Row(Modifier.fillMaxWidth().clickable { expandedDiameter = if (expanded) null else b.diameter }
                .padding(horizontal = 10.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                BoltValue("M${b.diameter}${if (expanded) " −" else " +"}", 1f)
                BoltValue("${b.pitch}", 1f)
                BoltValue("${fixingS(boltType,b.diameter)}", 1.2f)
                BoltValue("${fixingK(boltType,b.diameter)}", 1.2f)
            }
            if (expanded) {
                Column(Modifier.fillMaxWidth().padding(start = 13.dp, end = 11.dp, bottom = 14.dp)) {
                    Text("M${b.diameter}  /  ${boltType}", color = blue4, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(Modifier.height(5.dp))
                    Text("Pitch ${b.pitch} mm · k ${fixingK(boltType,b.diameter)} mm · s ${fixingS(boltType,b.diameter)} mm", color = text4, fontSize = 12.sp)
                    Spacer(Modifier.height(9.dp))
                    Text("COMMON NOMINAL LENGTHS · mm", color = muted4, letterSpacing = .7.sp, fontSize = 10.sp)
                    Spacer(Modifier.height(5.dp))
                    typicalBoltLengths[b.diameter].orEmpty().forEach { length ->
                        DataPair("Nominal length L", "$length mm")
                        Text(threadReference(boltType,b.diameter,length,fullThread), color = muted4, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Spacer(Modifier.height(9.dp))
                    Text("LENGTH CONVENTION", color = muted4, letterSpacing = .7.sp, fontSize = 10.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("L is measured from under the head. b is a reference thread length, not guaranteed usable engagement. Short screws may be threaded to the head with incomplete thread/runout. Listed lengths are examples, not verified diameter/length combinations for every standard. Confirm the manufacturer table.", color = muted4, fontSize = 11.sp, lineHeight = 15.sp)
                }
            }
            HorizontalDivider(color = line4.copy(alpha = .4f), thickness = .5.dp)
        }
    }
    Spacer(Modifier.height(10.dp))
    Text("ISO 4014 / 4017 use nominal length under the head. ISO head widths differ from older DIN 931 / 933 (notably M10/M12). Verify grade, thread engagement and the relevant product standard for the selected head type.",
        color = muted4, fontSize = 11.sp, lineHeight = 15.sp)
}

@Composable private fun SelfDrillingCatalogue() {
    InfoPanel(Modifier.fillMaxWidth()) {
        SmallTag("Self-drilling screw")
        FixingSolid("SCREW2", Modifier.fillMaxWidth().height(210.dp))
        Text("L = length under head · d = nominal screw Ø · k = head height / washer zone · drill point depends on TEK number and manufacturer",
            color = muted4, fontSize = 11.sp, lineHeight = 15.sp)
    }
    Spacer(Modifier.height(12.dp))
    InfoPanel(Modifier.fillMaxWidth()) {
        SmallTag("Reference sizes")
        DataPair("Stitch / light-gauge", "4.2 / 4.8 mm")
        DataPair("Common cladding hex-head TEK", "5.5 mm")
        DataPair("Larger self-drilling", "6.3 mm")
        DataPair("Typical lengths", "16, 19, 25, 32, 38, 50, 55 mm")
        Spacer(Modifier.height(8.dp))
        Text("Check drilling capacity, substrate thickness, thread engagement, pull-out, pull-over, EPDM washer compatibility and corrosion class in the exact manufacturer datasheet.",
            color = muted4, fontSize = 11.sp, lineHeight = 15.sp)
    }
}

@Composable private fun WasherCatalogue() {
    var expandedType by remember { mutableStateOf("PLAIN") }

    SmallTag("Washer forms · illustrations and typical applications")
    Spacer(Modifier.height(8.dp))
    washerTypes.forEach { spec ->
        val expanded = expandedType == spec.sketch
        Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)
            .border(1.dp, if (expanded) blue4.copy(alpha = .8f) else line4, RoundedCornerShape(12.dp))
            .background(plate4, RoundedCornerShape(12.dp))) {
            Row(Modifier.fillMaxWidth().clickable { expandedType = if (expanded) "" else spec.sketch }
                .padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
                WasherIllustration(spec.sketch, Modifier.width(101.dp).height(78.dp))
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(spec.name, color = if (expanded) blue4 else text4,
                        fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(3.dp))
                    Text(spec.standard, color = muted4, fontSize = 11.sp)
                }
                Text(if (expanded) "−" else "+", color = blue4, fontSize = 21.sp)
            }
            if (expanded) {
                HorizontalDivider(color = line4, thickness = .5.dp)
                Column(Modifier.fillMaxWidth().padding(13.dp)) {
                    SmallTag("Typical use")
                    Text(spec.use, color = text4, fontSize = 12.sp, lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 5.dp))
                    Spacer(Modifier.height(11.dp))
                    SmallTag("Selection note")
                    Text(spec.caution, color = muted4, fontSize = 11.sp, lineHeight = 16.sp,
                        modifier = Modifier.padding(top = 5.dp))
                    Spacer(Modifier.height(13.dp))
                    SmallTag("Dimensions · mm · all listed sizes")
                    Spacer(Modifier.height(8.dp))
                    WasherDimensionTable(spec.sketch)

                }
            }
        }
    }
    Text("Illustrations are explanatory, not to scale. ISO 7089 and ISO 7093-1 dimensions are " +
        "nominal reference values; verify tolerances and the approved washer grade before specifying.",
        color = muted4, fontSize = 11.sp, lineHeight = 16.sp)
}

// Standalone vector illustrations; six forms are visually distinct even while collapsed.
@Composable private fun WasherIllustration(type: String, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val mid = Offset(size.width*.49f, size.height*.45f)
        val r = min(size.width*.29f, size.height*.34f)
        val outer = if (type == "LARGE") r*1.12f else r
        val inner = if (type == "LARGE") r*.32f else r*.46f
        val thickness = 1.65f*density
        val faint = line4
        val ring = text4
        // Front view and a small thickness edge-line for the flat series.
        when (type) {
            "SPLIT" -> {
                drawArc(ring, 37f, 318f, false,
                    topLeft = Offset(mid.x-outer,mid.y-outer), size = Size(outer*2,outer*2),
                    style = Stroke(width=6f*density))
                drawArc(faint, 37f, 318f, false,
                    topLeft = Offset(mid.x-inner,mid.y-inner), size = Size(inner*2,inner*2),
                    style = Stroke(width=1.4f*density))
                val a = 37f*PI/180.0
                val b = 355f*PI/180.0
                drawLine(blue4, Offset(mid.x+inner*cos(a).toFloat(), mid.y+inner*sin(a).toFloat()),
                    Offset(mid.x+outer*cos(a).toFloat(),mid.y+outer*sin(a).toFloat()),thickness)
                drawLine(blue4, Offset(mid.x+inner*cos(b).toFloat(),mid.y+inner*sin(b).toFloat()),
                    Offset(mid.x+outer*cos(b).toFloat(),mid.y+outer*sin(b).toFloat()),thickness)
            }
            else -> {
                drawCircle(ring,outer,mid,style=Stroke(thickness))
                drawCircle(ring,inner,mid,style=Stroke(thickness))
                if (type == "CHAMFER") {
                    drawCircle(blue4,outer-thickness*2.6f,mid,style=Stroke(.95f*density))
                }
                if (type == "TOOTH") {
                    for (n in 0 until 12) {
                        val a = (2*PI*n/12).toFloat()
                        drawLine(blue4,
                            Offset(mid.x+(inner-2f*density)*cos(a),mid.y+(inner-2f*density)*sin(a)),
                            Offset(mid.x+(inner+5f*density)*cos(a),mid.y+(inner+5f*density)*sin(a)),
                            1.4f*density)
                    }
                }
                if (type == "SEAL") {
                    drawCircle(blue4,inner+4f*density,mid,style=Stroke(5f*density))
                }
                if (type == "PLAIN" || type == "LARGE") {
                    val bottom = size.height*.93f
                    drawLine(faint,Offset(mid.x-outer*.72f,bottom),
                        Offset(mid.x+outer*.72f,bottom),1.3f*density)
                    drawLine(faint,Offset(mid.x-outer*.72f,bottom-3f*density),
                        Offset(mid.x+outer*.72f,bottom-3f*density),1.3f*density)
                }
            }
        }
    }
}

@Composable private fun RowScope.BoltHead(t: String, weight: Float) {
    Text(t, modifier = Modifier.weight(weight), fontSize = 11.sp, color = muted4, textAlign = TextAlign.Center)
}
@Composable private fun RowScope.BoltValue(t: String, weight: Float) {
    Text(t, modifier = Modifier.weight(weight), fontSize = 13.sp, color = text4, textAlign = TextAlign.Center)
}

@Composable private fun TechnicalSketch(kind: String, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val sx = size.width / 320f
        val sy = size.height / 190f
        fun p(x: Float, y: Float) = Offset(x*sx, y*sy)
        fun line(x1: Float,y1: Float,x2: Float,y2: Float, c: Color = text4, sw: Float = 1.6f) {
            drawLine(c,p(x1,y1),p(x2,y2),strokeWidth=sw*min(sx,sy))
        }
        fun rect(x: Float,y: Float,w: Float,h: Float, c: Color = text4) {
            drawRect(c,p(x,y),Size(w*sx,h*sy),style=Stroke(width=1.65f*min(sx,sy)))
        }
        fun dot(x:Float,y:Float,r:Float=4f) { drawCircle(blue4,r*min(sx,sy),p(x,y),style=Stroke(1.5f*min(sx,sy))) }
        fun label(t:String,x:Float,y:Float) {
            val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color=android.graphics.Color.rgb(169,184,198)
                textSize=12f*min(sx,sy)
                textAlign=android.graphics.Paint.Align.CENTER
                typeface=android.graphics.Typeface.create("sans-serif",android.graphics.Typeface.NORMAL)
            }
            drawContext.canvas.nativeCanvas.drawText(t,x*sx,y*sy,paint)
        }
        fun dim(x1:Float,y1:Float,x2:Float,y2:Float,t:String) {
            line(x1,y1,x2,y2,line4,1f)
            if (kotlin.math.abs(x2-x1)>kotlin.math.abs(y2-y1)) {
                line(x1,y1-4,x1,y1+4,blue4,1f);line(x2,y2-4,x2,y2+4,blue4,1f)
                label(t,(x1+x2)/2f,y1-7f)
            } else {
                line(x1-4,y1,x1+4,y1,blue4,1f);line(x2-4,y2,x2+4,y2,blue4,1f)
                label(t,x1-12f,(y1+y2)/2f+4f)
            }
        }
        when {
            kind == "FIT" -> {
                // Hole and shaft coaxial in front elevation; dimension letters external.
                drawCircle(text4,55*min(sx,sy),p(153f,95f),style=Stroke(1.9f*min(sx,sy)))
                drawCircle(blue4,35*min(sx,sy),p(153f,95f),style=Stroke(1.9f*min(sx,sy)))
                line(153f,27f,153f,165f,line4,1f);line(82f,95f,224f,95f,line4,1f)
                line(112f,55f,72f,28f,line4);label("HOLE H7",51f,24f)
                line(182f,120f,236f,147f,blue4);label("SHAFT",258f,158f)
                dim(118f,95f,188f,95f,"Ød")
            }
            kind == "KEY" -> {
                drawCircle(text4,62f*min(sx,sy),p(146f,101f),style=Stroke(1.7f*min(sx,sy)))
                // Shaft groove and rectangular key in outline; sketch is conceptual.
                rect(130f,36f,32f,28f,blue4)
                line(130f,36f,130f,64f,blue4); line(162f,36f,162f,64f,blue4)
                dim(130f,26f,162f,26f,"b")
                line(167f,36f,203f,36f,line4);line(167f,64f,203f,64f,line4)
                dim(213f,36f,213f,64f,"h")
                line(160f,66f,228f,81f,line4);label("t₁",240f,81f)
                line(142f,32f,91f,16f,line4);label("t₂ / hub",60f,16f)
                label("SHAFT Ød",147f,180f)
            }
            kind.startsWith("JOINT") -> {
                val mode = kind.removePrefix("JOINT").toIntOrNull() ?: 0
                // Connected member schematic: section support + beam web, with plate and bolts.
                if (mode == 7) {
                    rect(108f,14f,44f,117f);rect(65f,131f,134f,16f,blue4)
                    line(33f,167f,273f,167f,line4);line(33f,151f,33f,182f,line4)
                    line(85f,122f,85f,154f,blue4);line(179f,122f,179f,154f,blue4)
                    dot(85f,142f);dot(179f,142f)
                    label("BASE PLATE",226f,141f);label("ANCHORS",225f,159f)
                    dim(65f,115f,199f,115f,"B")
                } else if (mode == 5) {
                    rect(25f,62f,124f,57f);rect(163f,62f,130f,57f)
                    rect(108f,50f,104f,82f,blue4)
                    listOf(122f,148f,174f,198f).forEach {x->dot(x,66f);dot(x,116f)}
                    dim(108f,39f,212f,39f,"L")
                    label("SPLICE PLATE",158f,155f)
                } else {
                    // column = outlined H-shaped member left; incoming web = long beam to right
                    rect(35f,20f,23f,143f)
                    rect(58f,34f,26f,112f)
                    rect(84f,20f,22f,143f)
                    rect(106f,72f,181f,37f)
                    if (mode == 0 || mode == 1 || mode == 2) {
                        rect(100f,56f,13f,69f,blue4)
                        dot(109f,68f);dot(109f,94f);dot(109f,119f)
                        dim(124f,68f,124f,119f,"p")
                        label(if(mode==2) "CLEAT" else "PLATE",136f,146f)
                    } else if (mode == 6) {
                        rect(104f,108f,78f,12f,blue4)
                        line(104f,120f,104f,150f,blue4);line(104f,150f,135f,150f,blue4)
                        label("SEAT",179f,145f)
                    } else {
                        rect(101f,39f,12f,98f,blue4)
                        dot(109f,45f);dot(109f,60f);dot(109f,118f);dot(109f,132f)
                        label(if(mode==4) "EXTENDED" else "FLUSH",173f,145f)
                    }
                    label("COLUMN",68f,180f)
                    label("BEAM",229f,64f)
                }
                label("NTS",296f,184f)
            }
            kind.startsWith("BOLT_") || kind == "SCREW2" -> {
                val screw = kind == "SCREW2"
                val headX = 64f
                val end = if (screw) 263f else 248f
                // Head bearing plane is x=64 for every length dimension.
                if (kind == "BOLT_DOME") {
                    val dome=Path().apply {
                        moveTo(64f*sx,65f*sy)
                        cubicTo(20f*sx,65f*sy,20f*sx,129f*sy,64f*sx,129f*sy)
                        close()
                    }
                    drawPath(dome,blue4,style=Stroke(1.6f*min(sx,sy)))
                } else {
                    rect(34f,65f,30f,64f,blue4)
                    if(kind == "BOLT_SOCKET") {
                        line(34f,84f,49f,84f,line4);line(49f,84f,49f,110f,line4);line(49f,110f,34f,110f,line4)
                    } else {
                        line(34f,77f,64f,77f,line4);line(34f,117f,64f,117f,line4)
                    }
                }
                val shaftEnd=if(screw) 231f else end
                rect(headX,83f,shaftEnd-headX,28f)
                for(i in 0..13) {
                    val x=110f+i*8f
                    if(x+5<shaftEnd) {line(x,83f,x+5,90f,line4,1f);line(x,111f,x+5,104f,line4,1f)}
                }
                if(screw) {
                    rect(64f,71f,7f,52f,blue4)
                    line(231f,83f,263f,97f);line(263f,97f,231f,111f)
                    line(233f,87f,252f,101f,blue4);line(234f,107f,252f,94f,blue4)
                    label("DRILL POINT",246f,148f)
                }
                line(headX,62f,headX,29f,line4);line(end,80f,end,29f,line4)
                dim(headX,36f,end,36f,"L")
                line(34f,132f,34f,159f,line4);line(headX,132f,headX,159f,line4)
                dim(34f,154f,headX,154f,"k")
                line(end+3,83f,285f,83f,line4);line(end+3,111f,285f,111f,line4)
                dim(280f,83f,280f,111f,"d")
                label("L: UNDER HEAD TO TIP",164f,181f)
            }
            kind.startsWith("NUT") -> {
                val flange=kind.contains("Flange")
                val cap=kind.contains("Cap")
                val thin=kind.contains("Thin")
                val height=if(thin) 20f else 40f
                val top=108f-height
                val hex=Path().apply {
                    for(i in 0..5) {
                        val ang=(PI/3*i + PI/6).toFloat()
                        val x=86f+38f*cos(ang);val y=87f+38f*sin(ang)
                        if(i==0) moveTo(x*sx,y*sy) else lineTo(x*sx,y*sy)
                    };close()
                }
                drawPath(hex,blue4,style=Stroke(1.6f*min(sx,sy)))
                drawCircle(text4,18f*min(sx,sy),p(86f,87f),style=Stroke(1.5f*min(sx,sy)))
                rect(183f,top,66f,height,blue4)
                line(203f,top,203f,108f,line4);line(229f,top,229f,108f,line4)
                if(flange) rect(176f,108f,80f,7f,blue4)
                if(cap) drawArc(blue4,180f,180f,false,p(183f,top-30f),Size(66f*sx,60f*sy),style=Stroke(1.6f*min(sx,sy)))
                if(kind.contains("Nylon")) rect(190f,top-8f,52f,8f,blue4)
                if(kind.contains("All-metal")) {line(191f,top,196f,top-5f,blue4);line(196f,top-5f,236f,top-5f,blue4)}
                dim(53f,35f,119f,35f,"s")
                line(53f,68f,53f,30f,line4);line(119f,68f,119f,30f,line4)
                dim(270f,top,270f,108f,"m")
                line(250f,top,276f,top,line4);line(250f,108f,276f,108f,line4)
                label("END VIEW",86f,156f);label("SIDE VIEW",216f,156f)
                label("d = INTERNAL THREAD",160f,180f)
            }
            kind.startsWith("PURLIN_") -> {
                fun poly(vararg points: Pair<Float,Float>) {
                    val path=Path().apply {
                        moveTo(points.first().first*sx, points.first().second*sy)
                        for(i in 1 until points.lastIndex) {
                            val prev=points[i-1]; val v=points[i]; val next=points[i+1]
                            fun near(other: Pair<Float,Float>): Pair<Float,Float> {
                                val dx=other.first-v.first; val dy=other.second-v.second
                                val length=kotlin.math.sqrt(dx*dx+dy*dy); val ratio=minOf(5f/length,.4f)
                                return (v.first+dx*ratio) to (v.second+dy*ratio)
                            }
                            val before=near(prev); val after=near(next)
                            lineTo(before.first*sx,before.second*sy)
                            quadraticBezierTo(v.first*sx,v.second*sy,after.first*sx,after.second*sy)
                        }
                        lineTo(points.last().first*sx,points.last().second*sy)
                    }
                    drawPath(path,text4,style=Stroke(3f*min(sx,sy)))
                }
                if(kind == "PURLIN_HAT") {
                    poly(65f to 116f,110f to 116f,110f to 65f,205f to 65f,205f to 116f,250f to 116f)
                    dim(110f,34f,205f,34f,"a")
                    line(110f,60f,110f,29f,line4);line(205f,60f,205f,29f,line4)
                    dim(65f,146f,110f,146f,"b");dim(205f,146f,250f,146f,"b₁")
                    dim(42f,65f,42f,116f,"h")
                    line(44f,65f,105f,65f,line4);line(44f,116f,60f,116f,line4)
                    line(201f,91f,225f,91f,blue4);line(201f,87f,201f,95f,blue4);line(209f,87f,209f,95f,blue4);label("t",235f,95f)
                } else {
                    val z=kind == "PURLIN_Z"
                    if(z) poly(80f to 70f,80f to 50f,160f to 50f,160f to 130f,235f to 130f,235f to 110f)
                    else poly(235f to 70f,235f to 50f,160f to 50f,160f to 130f,235f to 130f,235f to 110f)
                    val start=if(z) 80f else 160f
                    val finish=if(z) 160f else 235f
                    dim(start,25f,finish,25f,"b")
                    line(start,46f,start,20f,line4);line(finish,46f,finish,20f,line4)
                    dim(160f,158f,235f,158f,"b₁")
                    line(160f,134f,160f,163f,line4);line(235f,134f,235f,163f,line4)
                    dim(48f,50f,48f,130f,"h")
                    line(50f,50f,start-5,50f,line4);line(50f,130f,155f,130f,line4)
                    dim(265f,110f,265f,130f,"a₁")
                    line(239f,110f,270f,110f,line4);line(239f,130f,270f,130f,line4)
                    line(150f,90f,185f,90f,blue4);line(156f,86f,156f,94f,blue4);line(164f,86f,164f,94f,blue4);label("t",195f,94f)
                }
            }        }
    }
}


internal data class NutRef(val diameter: Int, val pitch: Double, val af: Int, val height: Double)
internal val nutRefs = listOf(
    NutRef(5,0.8,8,4.0),
    NutRef(6,1.0,10,5.0), NutRef(8,1.25,13,6.5), NutRef(10,1.5,16,8.0), NutRef(12,1.75,18,10.0),
    NutRef(14,2.0,21,11.0), NutRef(16,2.0,24,13.0), NutRef(20,2.5,30,16.0), NutRef(24,3.0,36,19.0), NutRef(30,3.5,46,24.0)
)
internal data class PurlinRow(val family: String, val profile: String, val t: String, val h: String, val a: String, val b: String, val b1: String, val weight: String)
internal val purlinRows = listOf(
    PurlinRow("HAT","HAT50/07","0.7","20","50","25","25","0.77"),
    PurlinRow("HAT","HAT50/10","1.0","20","50","25","25","1.01"),
    PurlinRow("HAT","HAT50/12","1.2","20","50","25","25","1.31"),
    PurlinRow("HAT","HAT50/15","1.5","20","50","25","25","1.66"),
    PurlinRow("HAT","HAT50/20","2.0","20","50","25","25","2.21"),
    PurlinRow("HAT","HAT70/07","0.7","20","70","25","25","1.00"),
    PurlinRow("HAT","HAT70/10","1.0","20","70","25","25","1.42"),
    PurlinRow("HAT","HAT70/12","1.2","20","70","25","25","1.69"),
    PurlinRow("HAT","HAT70/15","1.5","20","70","25","25","2.14"),
    PurlinRow("HAT","HAT70/20","2.0","20","70","25","25","2.85"),
    PurlinRow("C","C100/07","0.7","100","15","40","40","1.18"),
    PurlinRow("C","C100/10","1.0","100","15","40","40","1.68"),
    PurlinRow("C","C100/12","1.2","100","15","40","40","2.00"),
    PurlinRow("C","C100/15","1.5","100","15","40","40","2.52"),
    PurlinRow("C","C100/20","2.0","100","15","40","40","3.36"),
    PurlinRow("C","C100/25","2.5","100","22","40","40","4.20"),
    PurlinRow("C","C120/07","0.7","120","15","40","40","1.29"),
    PurlinRow("C","C120/10","1.0","120","15","40","40","1.84"),
    PurlinRow("C","C120/12","1.2","120","15","40","40","2.19"),
    PurlinRow("C","C120/15","1.5","120","15","40","40","2.76"),
    PurlinRow("C","C120/20","2.0","120","15","40","40","3.68"),
    PurlinRow("C","C120/25","2.5","120","15","40","40","4.60"),
    PurlinRow("C","C150/07","0.7","150","15","40","40","1.40"),
    PurlinRow("C","C150/10","1.0","150","15","40","40","2.00"),
    PurlinRow("C","C150/12","1.2","150","15","40","40","2.38"),
    PurlinRow("C","C150/15","1.5","150","15","40","40","3.00"),
    PurlinRow("C","C150/20","2.0","150","15","40","40","4.00"),
    PurlinRow("C","C150/25","2.5","150","15","40","40","5.00"),
    PurlinRow("C","C150/30","3.0","150","15","40","40","6.00"),
    PurlinRow("C","C200/07","0.7","200","15","40","40","1.68"),
    PurlinRow("C","C200/10","1.0","200","15","40","40","2.40"),
    PurlinRow("C","C200/12","1.2","200","15","40","40","2.86"),
    PurlinRow("C","C200/15","1.5","200","15","40","40","3.60"),
    PurlinRow("C","C200/20","2.0","200","15","40","40","4.80"),
    PurlinRow("C","C200/25","2.5","200","22","40","40","6.20"),
    PurlinRow("C","C200/30","3.0","200","30","40","40","7.44"),
    PurlinRow("C","C250/10","1.0","250","15","40","40","3.28"),
    PurlinRow("C","C250/12","1.2","250","15","40","40","3.90"),
    PurlinRow("C","C250/15","1.5","250","15","40","40","4.92"),
    PurlinRow("C","C250/20","2.0","250","15","40","40","6.56"),
    PurlinRow("C","C250/25","2.5","250","22","40","40","8.20"),
    PurlinRow("C","C250/30","3.0","250","30","40","40","9.84"),
    PurlinRow("C","C300/10","1.0","300","15","40","40","3.60"),
    PurlinRow("C","C300/12","1.2","300","15","40","40","4.28"),
    PurlinRow("C","C300/15","1.5","300","15","40","40","5.40"),
    PurlinRow("C","C300/20","2.0","300","15","40","40","7.20"),
    PurlinRow("C","C300/25","2.5","300","22","40","40","9.00"),
    PurlinRow("C","C300/30","3.0","300","30","40","40","10.80"),
    PurlinRow("C","C350/10","1.0","350","15","40","40","4.40"),
    PurlinRow("C","C350/12","1.2","350","15","40","40","5.24"),
    PurlinRow("C","C350/15","1.5","350","15","40","40","6.60"),
    PurlinRow("C","C350/20","2.0","350","15","40","40","8.80"),
    PurlinRow("C","C350/25","2.5","350","22","40","40","11.00"),
    PurlinRow("C","C350/30","3.0","350","30","40","40","13.20"),
    PurlinRow("Z","Z50/07","0.7","50","12","46","40","0.84"),
    PurlinRow("Z","Z50/10","1.0","50","12","46","40","1.20"),
    PurlinRow("Z","Z50/12","1.2","50","12","46","40","1.43"),
    PurlinRow("Z","Z50/15","1.5","50","12","46","40","1.80"),
    PurlinRow("Z","Z50/20","2.0","50","15","46","40","2.40"),
    PurlinRow("Z","Z75/07","0.7","75","12","46","40","1.01"),
    PurlinRow("Z","Z75/10","1.0","75","12","46","40","1.44"),
    PurlinRow("Z","Z75/12","1.2","75","12","46","40","1.73"),
    PurlinRow("Z","Z75/15","1.5","75","12","46","40","2.16"),
    PurlinRow("Z","Z75/20","2.0","75","15","46","40","2.88"),
    PurlinRow("Z","Z100/07","0.7","100","12","46","40","1.18"),
    PurlinRow("Z","Z100/10","1.0","100","12","46","40","1.68"),
    PurlinRow("Z","Z100/12","1.2","100","12","46","40","1.99"),
    PurlinRow("Z","Z100/15","1.5","100","12","46","40","2.52"),
    PurlinRow("Z","Z100/20","2.0","100","15","46","40","3.36"),
    PurlinRow("Z","Z100/25","2.5","100","22","46","40","4.20"),
    PurlinRow("Z","Z120/07","0.7","120","12","46","40","1.29"),
    PurlinRow("Z","Z120/10","1.0","120","12","46","40","1.84"),
    PurlinRow("Z","Z120/12","1.2","120","12","46","40","2.19"),
    PurlinRow("Z","Z120/15","1.5","120","12","46","40","2.76"),
    PurlinRow("Z","Z120/20","2.0","120","15","46","40","3.68"),
    PurlinRow("Z","Z120/25","2.5","120","22","46","40","4.60"),
    PurlinRow("Z","Z150/07","0.7","150","12","46","40","1.40"),
    PurlinRow("Z","Z150/10","1.0","150","12","46","40","2.00"),
    PurlinRow("Z","Z150/12","1.2","150","12","46","40","2.38"),
    PurlinRow("Z","Z150/15","1.5","150","12","46","40","3.00"),
    PurlinRow("Z","Z150/20","2.0","150","15","46","40","4.00"),
    PurlinRow("Z","Z150/25","2.5","150","22","46","40","5.00"),
    PurlinRow("Z","Z150_2/10","1.0","150","15","70","60","2.40"),
    PurlinRow("Z","Z150_2/12","1.2","150","15","70","60","2.86"),
    PurlinRow("Z","Z150_2/15","1.5","150","15","70","60","3.60"),
    PurlinRow("Z","Z150_2/20","2.0","150","15","70","60","4.80"),
    PurlinRow("Z","Z200_1/07","0.7","200","12","46","40","1.68"),
    PurlinRow("Z","Z200_1/10","1.0","200","12","46","40","2.40"),
    PurlinRow("Z","Z200_1/12","1.2","200","12","46","40","2.86"),
    PurlinRow("Z","Z200_1/15","1.5","200","12","46","40","3.60"),
    PurlinRow("Z","Z200_1/20","2.0","200","15","46","40","4.80"),
    PurlinRow("Z","Z200_1/25","2.5","200","22","46","40","6.20"),
    PurlinRow("Z","Z200_2/07","0.7","200","20","70","60","1.96"),
    PurlinRow("Z","Z200_2/10","1.0","200","20","70","60","2.80"),
    PurlinRow("Z","Z200_2/12","1.2","200","20","70","60","3.33"),
    PurlinRow("Z","Z200_2/15","1.5","200","20","70","60","4.20"),
    PurlinRow("Z","Z200_2/20","2.0","200","20","70","60","5.60"),
    PurlinRow("Z","Z200_2/25","2.5","200","22","70","60","7.00"),
    PurlinRow("Z","Z200_2/30","3.0","200","30","70","60","8.40"),
    PurlinRow("Z","Z250/10","1.0","250","20","70","60","3.28"),
    PurlinRow("Z","Z250/12","1.2","250","20","70","60","3.94"),
    PurlinRow("Z","Z250/15","1.5","250","20","70","60","4.92"),
    PurlinRow("Z","Z250/20","2.0","250","20","70","60","6.56"),
    PurlinRow("Z","Z250/25","2.5","250","22","70","60","8.20"),
    PurlinRow("Z","Z250/30","3.0","250","28","70","60","9.84"),
    PurlinRow("Z","Z300/10","1.0","300","25","70","60","3.60"),
    PurlinRow("Z","Z300/12","1.2","300","25","70","60","4.28"),
    PurlinRow("Z","Z300/15","1.5","300","25","70","60","5.40"),
    PurlinRow("Z","Z300/20","2.0","300","30","70","60","7.20"),
    PurlinRow("Z","Z300/25","2.5","300","30","70","60","9.00"),
    PurlinRow("Z","Z300/30","3.0","300","30","70","60","10.80"),
    PurlinRow("Z","Z350/10","1.0","350","30","70","80","4.40"),
    PurlinRow("Z","Z350/12","1.2","350","30","70","80","5.24"),
    PurlinRow("Z","Z350/15","1.5","350","30","70","80","6.60"),
    PurlinRow("Z","Z350/20","2.0","350","30","70","80","8.80"),
    PurlinRow("Z","Z350/25","2.5","350","30","70","80","11.00"),
    PurlinRow("Z","Z350/30","3.0","350","30","70","80","13.20")
)

@Composable internal fun PurlinTool() {
    var family by remember { mutableStateOf("C") }
    val rows = purlinRows.filter { it.family == family }
    val purlinScroll = rememberLazyListState()
    LaunchedEffect(family) { purlinScroll.scrollToItem(0) }
    Row(Modifier.fillMaxSize().padding(start = 13.dp, end = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Text("Steel profiles · Purlins", color = text4, fontSize = 27.sp, fontWeight = FontWeight.SemiBold)
            Text("$family section · ${rows.size} entries", color = muted4, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            InfoPanel(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("$family / SECTION", color = text4, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.weight(1f))
                    Text("NTS", color = muted4, fontSize = 10.sp)
                }
                ToggleChips(listOf("C", "Z", "HAT"), family) { family = it }
                TechnicalSketch("PURLIN_$family", Modifier.fillMaxWidth().height(if(family == "HAT") 170.dp else 221.dp))
                Text("Reference note: data taken from metalmaster.lt charts supplied by the user.", color = muted4, fontSize = 10.sp)
            }
            Spacer(Modifier.height(10.dp))
            SmallTag("Dimensions")
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth().background(plate4, RoundedCornerShape(8.dp)).padding(8.dp)) {
                listOf("Profile","t","h","a","b","b1","kg/m").forEachIndexed { idx, it ->
                    Text(it, color = muted4, fontSize = 10.sp, modifier = Modifier.weight(if (idx==0) 1.8f else 1f), textAlign = TextAlign.Center)
                }
            }
            LazyColumn(Modifier.fillMaxWidth().weight(1f).edgeFade(start={purlinScroll.canScrollBackward},end={purlinScroll.canScrollForward}), state=purlinScroll) {
                items(rows) { r ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                        listOf(r.profile,r.t,r.h,r.a,r.b,r.b1,r.weight).forEachIndexed { idx, v ->
                            Text(v, color = text4, fontSize = 12.sp, modifier = Modifier.weight(if (idx==0) 1.8f else 1f), textAlign = TextAlign.Center)
                        }
                    }
                    HorizontalDivider(color = line4.copy(alpha = .4f), thickness = .5.dp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Data is presented as an offline quick reference. Verify the exact supplier table, material grade and availability before specifying.", color = muted4, fontSize = 11.sp, lineHeight = 15.sp)
        }
    }
}

@Composable private fun NutsCatalogue() {
    var selectedType by remember { mutableStateOf("Hex nut") }
    val descriptions = linkedMapOf(
        "Hex nut" to "General-purpose six-sided nut for a matching metric bolt or threaded rod.",
        "Thin / jam nut" to "Low-profile nut; check the specified locking arrangement and load capacity.",
        "Nylon insert locknut" to "Prevailing-torque nut with a polymer insert. Check temperature and reuse limits with the manufacturer.",
        "All-metal locknut" to "Prevailing-torque nut with an all-metal locking feature. Check installation torque and reuse limits.",
        "Flange nut" to "Integral flange spreads the bearing load. Serrated and plain forms have different surface requirements.",
        "Cap / dome nut" to "Closed end covers the exposed thread. Check the available internal thread depth."
    )
    InfoPanel(Modifier.fillMaxWidth()) {
        SmallTag("Common nut types")
        ToggleChips(descriptions.keys.toList(), selectedType) { selectedType = it }
        Spacer(Modifier.height(10.dp))
        FixingSolid("NUT_$selectedType", Modifier.fillMaxWidth().height(255.dp))
        Text(selectedType, color = blue4, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(descriptions.getValue(selectedType), color = text4, fontSize = 12.sp, lineHeight = 18.sp)
    }
    Spacer(Modifier.height(12.dp))
    NutDimensionTable(selectedType)

}







@Composable internal fun StandardsCatalogue() {
    SmallTag("Fixing standards · identification guide")
    Spacer(Modifier.height(8.dp))
    Text("Related references are not automatic equivalents. Check the complete designation, edition, dimensions, material, coating and assembly requirements.", color = muted4, fontSize = 12.sp, lineHeight = 18.sp)
    fastenerStandards.forEach { item ->
        Spacer(Modifier.height(10.dp))
        InfoPanel(Modifier.fillMaxWidth()) {
            val context = LocalContext.current
            Text(item.label, color = blue4, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(item.scope, color = text4, fontSize = 12.sp, modifier = Modifier.padding(vertical = 5.dp))
            Text(item.note, color = muted4, fontSize = 12.sp, lineHeight = 18.sp)
            Text("Reference ↗", color = blue4, fontSize = 12.sp, modifier = Modifier.clickable { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.url))) }.padding(top = 10.dp))
        }
    }
}



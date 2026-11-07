package com.structkit.app

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

// The palette is deliberately high-contrast on an almost-black background.
private val background = Color(0xFF08090B)
private val panel = Color(0xFF14171B)
private val ink = Color(0xFFD9DDE3)
private val secondaryInk = Color(0xFF9EA6B2)
private val subtleInk = Color(0xFF74808E)
private val accent = Color(0xFF9EC5E8)
private val outline = Color(0xFF343A43)

private val families = listOf("RHS", "SHS", "CHS", "RSA", "IPE", "IPN", "HEA", "HEB", "HEM", "UPN", "UPE", "T", "PLATE", "Purlins", "Timber")
private val rolledI = setOf("IPE", "IPN", "HEA", "HEB", "HEM")
private val channels = setOf("UPN", "UPE")

private data class Profile(
    val family: String, val designation: String,
    val h: Float, val b: Float, val tw: Float, val tf: Float,
    val area: Float, val mass: Float, val basis: String,
    val r1: Float?, val r2: Float?, val d: Float?
)

private fun loadProfiles(text: String): List<Profile> = text.lineSequence().drop(1).mapNotNull { raw ->
    val cells = raw.trim().split('|')
    if (cells.size != 12) null else try {
        Profile(cells[0], cells[1], cells[2].toFloat(), cells[3].toFloat(), cells[4].toFloat(),
            cells[5].toFloat(), cells[6].toFloat(), cells[7].toFloat(), cells[8],
            cells[9].toFloatOrNull(), cells[10].toFloatOrNull(), cells[11].toFloatOrNull())
    } catch (_: NumberFormatException) { null }
}.toList()

private fun num(value: Float): String {
    if (value == 0f) return "—"
    if (value % 1f == 0f) return String.format(Locale.US, "%.0f", value)
    return String.format(Locale.US, "%.2f", value)
        .trimEnd('0').trimEnd('.')
}

internal class ComparisonSession : ViewModel() {
    val pins = mutableStateListOf<SectionPin>()
    val materialPins = mutableStateListOf<SectionPin>()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(8, 9, 11)
        window.navigationBarColor = android.graphics.Color.rgb(8, 9, 11)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = 0
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val referenceData = assets.open("profiles.csv").bufferedReader().use { it.readText() }
        val sections = loadProfiles(referenceData)
        val session = ViewModelProvider(this)[ComparisonSession::class.java]
        setContent { StructKit(sections, session.pins, session.materialPins) }
    }
}

@Composable private fun StructKit(profiles: List<Profile>, pins: MutableList<SectionPin>, materialPins: MutableList<SectionPin>) {
    var screen by rememberSaveable { mutableStateOf("Home") }
    val landscapeViewer = (screen == "2D viewer" || screen == "3D viewer") &&
        androidx.compose.ui.platform.LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val activity = androidx.compose.ui.platform.LocalContext.current as android.app.Activity
    DisposableEffect(landscapeViewer) {
        val bars = androidx.core.view.WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        bars.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if(landscapeViewer) bars.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        else bars.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        onDispose { bars.show(androidx.core.view.WindowInsetsCompat.Type.systemBars()) }
    }
    BackHandler(enabled = screen != "Home") { screen = "Home" }
    MaterialTheme(colorScheme = darkColorScheme(
        background = background, surface = panel, primary = accent, onPrimary = background,
        onBackground = ink, onSurface = ink, onSurfaceVariant = secondaryInk
    )) {
        Column(Modifier.fillMaxSize().background(background).then(if(landscapeViewer) Modifier.displayCutoutPadding() else Modifier.statusBarsPadding().navigationBarsPadding()).imePadding()) {
            if(!landscapeViewer) Row(
                Modifier.fillMaxWidth().height(if(screen == "2D viewer" || screen == "3D viewer") 66.dp else 82.dp).padding(start = 15.dp, end = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (screen != "Home") {
                    Text("‹", modifier = Modifier.clickable { screen = "Home" }.padding(end = 14.dp),
                        color = accent, fontSize = 30.sp)
                }
                Column {
                    Text("StruKit", color = ink, fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = 1.6.sp)
                    Text("Revision 3.15 A", color = secondaryInk, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Image(painter = painterResource(R.drawable.strukit_logo), contentDescription = "StruKit logo",
                    modifier = Modifier.padding(end = 8.dp).size(48.dp))
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
            when (screen) {
                "Home" -> Home { screen = it }
                "About" -> ReferencePage("about.html", Modifier.fillMaxSize())
                "Check for updates" -> AppUpdates()
                "Say thanks" -> ReferencePage("thanks.html", Modifier.fillMaxSize())
                "Steel profiles & other" -> ProfileBrowser(profiles, pins)
                "Unit converter" -> Converter()
                "Tolerances" -> ToleranceTool()
                "Steel connections" -> ConnectionTool()
                "Fixing library" -> FixingTool()
                "Purlins library" -> PurlinTool()
                "Forces calculator" -> ForcesTool()
                "Building drafter" -> BuildingDrafter(profiles.filter { it.family in setOf("IPE", "HEA", "HEB") }.map { DraftMember("${it.family} ${it.designation.removePrefix(it.family).trim()}", it.mass.toDouble(), it.h.toDouble(),it.b.toDouble(),it.tw.toDouble(),it.tf.toDouble()) })
                "Standards" -> StandardsTool()
                "Material library" -> MetalsPilot(materialPins)
                "2D viewer" -> CadViewer(false)
                "3D viewer" -> CadViewer(true)
            }
            }
        }
    }
}

@Composable private fun Home(onOpen: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(Modifier.fillMaxSize().fadedVerticalScroll(rememberScrollState()).padding(top = 2.dp, bottom = 20.dp)) {
        Image(painterResource(R.drawable.home_banner), "Steel-frame building", Modifier.fillMaxWidth().aspectRatio(6000f / (2496f * 1.2f)), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
        Column(Modifier.padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(6.dp))
        Text("Engineering tools", color = ink, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
        Text("Offline tools for everyday structural work.", color = secondaryInk, fontSize = 13.sp)
        Spacer(Modifier.height(26.dp))
        ToolCard("01", "Steel profiles & other", "Sections & purlins · properties · comparison") { onOpen("Steel profiles & other") }
        Spacer(Modifier.height(12.dp))
        ToolCard("02", "Unit converter", "Right-hand categories · instant engineering conversion") { onOpen("Unit converter") }
        Spacer(Modifier.height(12.dp))
        ToolCard("03", "Tolerances", "ISO 286 fits · shaft keys / DIN 6885") { onOpen("Tolerances") }
        Spacer(Modifier.height(12.dp))
        ToolCard("04", "Steel connections", "I / H joints · diagrams · detail checks") { onOpen("Steel connections") }
        Spacer(Modifier.height(12.dp))
        ToolCard("05", "Fixing library", "Bolts · nuts · washers · self-drilling screws") { onOpen("Fixing library") }
        Spacer(Modifier.height(12.dp))
        ToolCard("06", "Building drafter", "Frame layout · four views · main-member weight") { onOpen("Building drafter") }
        Spacer(Modifier.height(12.dp))
        ToolCard("07", "Forces calculator", "Rain collection · retained water · roof snow loads") { onOpen("Forces calculator") }
        Spacer(Modifier.height(12.dp))
        ToolCard("08", "Standards", "Standards overview · access · guarding · formulas") { onOpen("Standards") }
        Spacer(Modifier.height(12.dp))
        ToolCard("09", "Material library", "100 materials · grade comparisons · original diagrams") { onOpen("Material library") }
        Spacer(Modifier.height(12.dp))
        ToolCard("10", "2D viewer", "DWG · DXF · pan, zoom & layers") { onOpen("2D viewer") }
        Spacer(Modifier.height(12.dp))
        ToolCard("11", "3D viewer", "STEP · STL · OBJ · orbit & parts") { onOpen("3D viewer") }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("About", color = subtleInk, fontSize = 10.sp, letterSpacing = 1.sp,
                modifier = Modifier.clickable { onOpen("About") }.padding(vertical = 12.dp))
            Text("Check for updates", color = accent, fontSize = 10.sp,
                modifier = Modifier.clickable { onOpen("Check for updates") }.padding(vertical = 12.dp))
            Text("Say thanks", color = accent, fontSize = 10.sp, letterSpacing = 1.sp,
                modifier = Modifier.clickable { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://ko-fi.com/strukit"))) }.padding(vertical = 12.dp))
        }
        Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable private fun ToolCard(index: String, name: String, description: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 66.dp).clip(RoundedCornerShape(14.dp)).background(panel)
        .clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(index, color = accent, fontSize = 10.sp, modifier = Modifier.padding(end = 8.dp))
                Text(name, color = ink, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(3.dp))
            Text(description, color = secondaryInk, fontSize = 11.sp)
        }
        Spacer(Modifier.width(8.dp))
        ToolIllustration(index, Modifier.size(44.dp))
    }
}

@Composable private fun ToolIllustration(index: String, modifier: Modifier) {
    Canvas(modifier) {
        val u = size.width / 48f
        fun line(x:Float,y:Float,a:Float,b:Float) = drawLine(accent,Offset(x*u,y*u),Offset(a*u,b*u),1.5f*u)
        fun box(x:Float,y:Float,w:Float,h:Float) = drawRect(accent,Offset(x*u,y*u),Size(w*u,h*u),style=Stroke(1.5f*u))
        when(index) {
            "10" -> {box(5f,7f,38f,34f);box(12f,15f,24f,18f);line(12f,11f,36f,11f);line(8f,15f,8f,33f)}
            "11" -> {box(8f,18f,24f,24f);box(17f,7f,24f,24f);line(8f,18f,17f,7f);line(32f,18f,41f,7f);line(32f,42f,41f,31f);line(8f,42f,17f,31f)}
            "01" -> { box(6f,7f,36f,5f);box(21f,12f,6f,24f);box(6f,36f,36f,5f) }
            "02" -> { line(6f,16f,41f,16f);line(34f,9f,41f,16f);line(34f,23f,41f,16f);line(7f,32f,42f,32f);line(7f,32f,14f,25f);line(7f,32f,14f,39f) }
            "03" -> { box(5f,10f,10f,28f);box(33f,10f,10f,28f);box(18f,17f,12f,14f);line(18f,7f,30f,7f);line(18f,4f,18f,10f);line(30f,4f,30f,10f) }
            "04" -> { box(6f,5f,10f,38f);box(16f,16f,27f,16f);box(17f,11f,7f,26f);for(y in listOf(16f,24f,32f))drawCircle(accent,1.4f*u,Offset(20.5f*u,y*u)) }
            "05" -> { box(6f,15f,8f,18f);box(14f,20f,28f,8f);for(x in 24..40 step 4)line(x.toFloat(),20f,x-3f,28f) }
            "06" -> { line(7f,41f,7f,18f);line(7f,18f,22f,7f);line(22f,7f,37f,18f);line(37f,18f,37f,41f);line(14f,36f,14f,16f);line(14f,16f,29f,5f);line(29f,5f,44f,16f);line(44f,16f,44f,36f);line(7f,18f,14f,16f);line(37f,18f,44f,16f) }
            "07" -> { val p=Path().apply{moveTo(24*u,5*u);cubicTo(18*u,17*u,9*u,24*u,12*u,33*u);cubicTo(17*u,46*u,36*u,41*u,36*u,30*u);cubicTo(36*u,23*u,28*u,14*u,24*u,5*u)};drawPath(p,accent,style=Stroke(1.5f*u));line(18f,31f,21f,35f) }
            "08" -> { box(8f,6f,31f,36f);line(14f,6f,14f,42f);for(y in 15..33 step 6)line(20f,y.toFloat(),33f,y.toFloat()) }
            else -> { for(y in listOf(11f,21f,31f)){line(5f,y,24f,y-6f);line(24f,y-6f,43f,y);line(43f,y,24f,y+7f);line(24f,y+7f,5f,y)} }
        }
    }
}

@Composable private fun ProfileBrowser(profiles: List<Profile>, pins: MutableList<SectionPin>) {
    var pinFull by remember { mutableStateOf(false) }
    val addPin: (SectionPin) -> Unit = { p ->
        if (pins.contains(p)) Unit else if (pins.size < 3) { pins.add(p); pinFull = false } else pinFull = true
    }
    var family by remember { mutableStateOf("RHS") }
    var expanded by remember(family) { mutableStateOf<String?>(null) }
    var highlighted by remember(family) { mutableStateOf<String?>(null) }
    val matches = remember(family, profiles) { profiles.filter { it.family == family } }
    val simple = family in setOf("RHS", "SHS", "CHS", "RSA")
    val columns = remember(family) { columnsFor(family) }
    val tableWidth = columns.sumOf { it.width }
    val listState = rememberLazyListState()
    LaunchedEffect(family) { listState.scrollToItem(0) }
    Column(Modifier.fillMaxSize()) {
        Text("Steel profiles & other",color=ink,fontSize=23.sp,modifier=Modifier.padding(horizontal=8.dp))
        ComparisonTray(pins, { pins.remove(it); pinFull=false }) { pin ->
            val entry=profiles.find { (if(it.designation.startsWith(it.family))it.designation else "${it.family} ${it.designation}")==pin.section }
            if(entry!=null) {
                SectionProperties(entry,420,addPin,false)
            } else if(pin.section.startsWith("Purlin ")) PurlinPinDetails(pin.section) else TimberPinDetails(pin.section)
        }
        if(pinFull)Text("Three materials pinned. Double-tap a tile to remove it.",color=accent,fontSize=11.sp)
        Row(Modifier.weight(1f).fillMaxWidth().padding(start=2.dp,end=4.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
        if (family == "Purlins") {
            Box(Modifier.weight(1f).fillMaxHeight()) { PurlinTool(addPin) }
        } else if (family == "Timber") {
            Box(Modifier.weight(1f).fillMaxHeight()) { TimberTable(addPin) }
        } else Column(Modifier.weight(1f).fillMaxHeight()) {
            Box(Modifier.fillMaxWidth().height(265.dp).background(panel, RoundedCornerShape(14.dp))) {
                Column(Modifier.fillMaxSize().padding(start = 8.dp, end = 8.dp, top = 10.dp, bottom = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$family  /  SECTION", color = ink, fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
                        Text("NTS", color = secondaryInk, fontSize = 10.sp)
                    }
                    SectionSketch(family, Modifier.fillMaxWidth().weight(1f))
                    Text("Outline schematic · dimensions by letters", color = secondaryInk,
                        fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("DIMENSIONS", color = ink, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(Modifier.weight(1f))
                Text("Tap to expand", color = secondaryInk, fontSize = 10.sp)
            }
            Spacer(Modifier.height(7.dp))
            BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                val availableWidth=maxWidth.value
                val factor=maxOf(1f,availableWidth/tableWidth)
                val fitted=columns.map { it.copy(width=(it.width*factor).toInt()) }
                Row(Modifier.fillMaxSize().fadedHorizontalScroll(rememberScrollState())) {
                Column(Modifier.width(maxOf(tableWidth.toFloat(),availableWidth).dp)) {
                    Row(Modifier.height(43.dp), verticalAlignment = Alignment.CenterVertically) {
                        fitted.forEach { HeaderCell(it.label, it.unit, it.width) }
                    }
                    HorizontalDivider(thickness = 1.dp, color = outline)
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().edgeFade(start={listState.canScrollBackward},end={listState.canScrollForward})) {
                        items(matches, key = { it.designation }) { entry ->
                            Column {
                                TableRow(entry, fitted, family, highlighted == entry.designation) {
                                    highlighted = entry.designation
                                    expanded = if (expanded == entry.designation) null else entry.designation
                                }
                                if (expanded == entry.designation) {
                                    SectionProperties(entry, maxOf(tableWidth.toFloat(),availableWidth).toInt(), addPin)
                                }
                                HorizontalDivider(thickness = .5.dp, color = outline)
                            }
                        }
                    }
                }
            }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                when (family) {
                    "SHS", "RHS", "CHS" -> "Nominal sizes; ≈ mass uses idealised geometry (corner effects excluded)."
                    "RSA" -> "F.H. Brundle reference sizes. ≈ marks estimated mass; radii depend on the rolling process."
                    "PLATE" -> "Plate thickness only. Open a row for theoretical mass per m²."
                    "T" -> "Indicative tee sizes; geometry is idealised, not a defined rolled series."
                    else -> "Nominal section dimensions. Radius data is manufacturer reference, not a guaranteed tolerance."
                },
                color = secondaryInk, fontSize = 10.sp, lineHeight = 13.sp,
                modifier = Modifier.padding(bottom = 10.dp, end = 2.dp)
            )
        }
        ToolTabRail(items = families, selected = family, short = { if(it == "PLATE") "Plates" else it }, onSelect = { family = it })
        }
    }
}

private data class TableColumn(val label: String, val unit: String, val width: Int)

private fun columnsFor(family: String): List<TableColumn> {
    val section = TableColumn("Section", "", 110)
    val h = TableColumn("h", "mm", 47)
    val b = TableColumn("b", "mm", 47)
    val t = TableColumn("t", "mm", 47)
    val tw = TableColumn("tw", "mm", 47)
    val tf = TableColumn("tf", "mm", 47)
    val r1 = TableColumn("r1", "mm", 47)
    val r2 = TableColumn("r2", "mm", 47)
    val d = TableColumn("d", "mm", 52)
    val mass = TableColumn("Mass", "kg/m", 63)
    return when (family) {
        "PLATE" -> listOf(TableColumn("Thickness", "mm", 130))
        "CHS" -> listOf(section, TableColumn("D", "mm", 50), t, mass)
        "RSA" -> listOf(section, h, b, t, r1, r2, mass)
        "SHS", "RHS" -> listOf(section, h, b, t, mass)
        "T" -> listOf(section, h, b, tw, tf)
        "IPN", "UPN" -> listOf(section, h, b, tw, tf, r1, r2)
        else -> listOf(section, h, b, tw, tf, r1, d)
    }
}

@Composable private fun HeaderCell(title: String, unit: String, width: Int) {
    Column(Modifier.width(width.dp).padding(start = 5.dp), verticalArrangement = Arrangement.Center) {
        Text(title, color = ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        if (unit.isNotEmpty()) Text(unit, color = secondaryInk, fontSize = 10.sp)
    }
}

private fun fmt(value: Float?): String = if (value == null) "—" else num(value)

@Composable private fun TableRow(
    profile: Profile, columns: List<TableColumn>, family: String, active: Boolean, onSelect: () -> Unit
) {
    val values = columns.map { col ->
        when (col.label) {
            "Section", "Thickness" -> profile.designation
            "h" -> num(profile.h)
            "b" -> num(profile.b)
            "D" -> num(profile.h)
            "tw" -> num(profile.tw)
            "tf" -> num(profile.tf)
            "t" -> num(profile.tw)
            "r1" -> fmt(profile.r1)
            "r2" -> fmt(profile.r2)
            "d" -> fmt(profile.d)
            "Mass" -> (if (profile.basis in setOf("APPROX", "IDEAL")) "≈" else "") + num(profile.mass)
            else -> "—"
        }
    }
    val bg = if (active) Color(0xFF1F3140) else background
    Row(
        Modifier.fillMaxWidth().height(44.dp).background(bg).clickable(onClick = onSelect),
        verticalAlignment = Alignment.CenterVertically
    ) {
        values.forEachIndexed { i, value ->
            Text(value, modifier = Modifier.width(columns[i].width.dp).padding(start = 5.dp, end = 2.dp),
                maxLines = 1, color = if (active) accent else ink, fontSize = 13.sp)
        }
    }
}

@Composable private fun SectionProperties(profile: Profile, width: Int, pin: (SectionPin) -> Unit, heading: Boolean = true) {
    val isPlate = profile.family == "PLATE"
    val pinName = if (profile.designation.startsWith(profile.family)) profile.designation else "${profile.family} ${profile.designation}"
    ProfilePropertiesPanel {
        if(heading) Text("SECTION PROPERTIES  /  ${profile.designation}", color = accent,
            fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = .5.sp)
        Spacer(Modifier.height(7.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(25.dp)) {
            PinnableValue(pinName, if (isPlate) "Mass / m²" else "Mass / m",
                "${if (profile.basis == "IDEAL") "≈" else ""}${num(profile.mass)} ${if (isPlate) "kg/m²" else "kg/m"}", pin)
            if (!isPlate) PinnableValue(pinName, "Area", "${num(profile.area)} cm²", pin)
        }
        SectionPrices(pinName, profile.mass.toDouble(), isPlate, pin)
        Spacer(Modifier.height(7.dp))
        Text(when(profile.basis) {
            "PUBLISHED" -> "Published nominal section properties"
            "MASS_DERIVED" -> "Area derived from nominal mass at 7,850 kg/m³"
            "THEORETICAL" -> "Theoretical plate mass at 7,850 kg/m³"
            else -> "Idealised geometry — not a certified section property"
        }, color = secondaryInk, fontSize = 10.sp)
    }
}

// All drawings below are FIXED family schematics, deliberately not scaled to a chosen row.
// Fillet radii and slopes are illustrated; numeric dimensions are read from the table.
private fun DrawScope.textAt(text: String, x: Float, y: Float) {
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.rgb(213, 223, 233)
        textSize = 14f * density
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textAlign = android.graphics.Paint.Align.CENTER
    }
    drawContext.canvas.nativeCanvas.drawText(text, x, y, paint)
}

private fun DrawScope.hDim(x1: Float, x2: Float, y: Float, objY: Float, label: String) {
    drawLine(subtleInk, Offset(x1, objY - 4f*density), Offset(x1, y - 7f*density), 1.1f*density)
    drawLine(subtleInk, Offset(x2, objY - 4f*density), Offset(x2, y - 7f*density), 1.1f*density)
    drawLine(subtleInk, Offset(x1, y), Offset(x2, y), 1.2f*density)
    drawLine(subtleInk, Offset(x1 - 3f*density, y + 3f*density), Offset(x1 + 3f*density, y - 3f*density), 1.2f*density)
    drawLine(subtleInk, Offset(x2 - 3f*density, y + 3f*density), Offset(x2 + 3f*density, y - 3f*density), 1.2f*density)
    textAt(label, (x1 + x2)/2f, y - 6f*density)
}

private fun DrawScope.vDim(y1: Float, y2: Float, x: Float, objX: Float, label: String) {
    drawLine(subtleInk, Offset(objX - 5f*density, y1), Offset(x + 6f*density, y1), 1.1f*density)
    drawLine(subtleInk, Offset(objX - 5f*density, y2), Offset(x + 6f*density, y2), 1.1f*density)
    drawLine(subtleInk, Offset(x, y1), Offset(x, y2), 1.2f*density)
    drawLine(subtleInk, Offset(x - 3f*density, y1 - 3f*density), Offset(x + 3f*density, y1 + 3f*density), 1.2f*density)
    drawLine(subtleInk, Offset(x - 3f*density, y2 - 3f*density), Offset(x + 3f*density, y2 + 3f*density), 1.2f*density)
    textAt(label, x - 14f*density, (y1 + y2)/2f + 4f*density)
}

private fun DrawScope.leader(tag: String, from: Offset, to: Offset) {
    drawCircle(accent, radius = 2.2f*density, center = from)
    drawLine(accent, from, Offset(to.x - 10f*density, to.y - 5f*density), 1.35f*density)
    textAt(tag, to.x, to.y)
}

@Composable private fun SectionSketch(family: String, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        // normalized schematic measurements; selection does not affect any parameter
        val shapeW = when (family) {
            "IPE", "IPN" -> 94f
            "HEA", "HEB", "HEM" -> 148f
            "UPN", "UPE" -> 96f
            "RHS" -> 108f
            "PLATE" -> 150f
            else -> 136f
        }
        val shapeH = when (family) {
            "IPE", "IPN" -> 164f
            "HEA", "HEB", "HEM" -> 130f
            "UPN", "UPE" -> 162f
            "RHS" -> 155f
            "PLATE" -> 23f
            else -> 138f
        }
        // Work in actual canvas pixels, with explicit dimension-callout margins in dp.
        // The older 1.45 scale cap made sketches tiny on high-density phones.
        val u = density
        val padLeft = 51f*u
        val padRight = 49f*u
        val padTop = 44f*u
        val padBottom = 34f*u
        val s = min((w - padLeft - padRight) / shapeW,
                    (h - padTop - padBottom) / shapeH).coerceAtLeast(.2f)
        val bw = shapeW * s
        val bh = shapeH * s
        val left = padLeft + (w - padLeft - padRight - bw) / 2f
        val top = padTop + (h - padTop - padBottom - bh) / 2f
        val right = left + bw
        val bottom = top + bh
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f
        val tw = max(5f*u, bw * when (family) { "HEA" -> .065f; "HEB" -> .095f; "HEM" -> .15f; else -> .093f })
        val tf = max(5f*u, bh * when (family) { "HEA" -> .075f; "HEB" -> .12f; "HEM" -> .19f; else -> .084f })
        val rad = min(15f*s, min((bw - tw)/3f, (bh - 2*tf)/4f)).coerceAtLeast(3f)
        val steel = Color(0xFFD4DCE4)
        val pen = Stroke(width = max(1.4f*u, .85f*s))
        when {
            family in rolledI -> {
                // Closed outline with four concave web-to-flange root fillets.
                // IPN has tapered inner faces; its toe rounding is schematic.
                val slope = if (family == "IPN") tf*.26f else 0f
                val toe = if (family == "IPN") 4f*s else 0f
                val path = Path().apply {
                    moveTo(left, top)
                    lineTo(right, top)
                    if (family == "IPN") {
                        lineTo(right, top + tf - slope - toe)
                        quadraticBezierTo(right, top + tf - slope, right - toe, top + tf - slope)
                    } else lineTo(right, top + tf)
                    lineTo(cx + tw/2f + rad, top + tf + slope)
                    quadraticBezierTo(cx + tw/2f, top + tf + slope, cx + tw/2f, top + tf + slope + rad)
                    lineTo(cx + tw/2f, bottom - tf - slope - rad)
                    quadraticBezierTo(cx + tw/2f, bottom - tf - slope, cx + tw/2f + rad, bottom - tf - slope)
                    if (family == "IPN") {
                        lineTo(right-toe,bottom-tf+slope)
                        quadraticBezierTo(right,bottom-tf+slope,right,bottom-tf+slope+toe)
                    } else lineTo(right, bottom - tf)
                    lineTo(right, bottom)
                    lineTo(left, bottom)
                    if (family == "IPN") {
                        lineTo(left,bottom-tf+slope+toe)
                        quadraticBezierTo(left,bottom-tf+slope,left+toe,bottom-tf+slope)
                    } else lineTo(left,bottom-tf)
                    lineTo(cx - tw/2f - rad, bottom - tf - slope)
                    quadraticBezierTo(cx - tw/2f, bottom - tf - slope, cx - tw/2f, bottom - tf - slope - rad)
                    lineTo(cx - tw/2f, top + tf + slope + rad)
                    quadraticBezierTo(cx - tw/2f, top + tf + slope, cx - tw/2f - rad, top + tf + slope)
                    if (family == "IPN") {
                        lineTo(left+toe,top+tf-slope)
                        quadraticBezierTo(left,top+tf-slope,left,top+tf-slope-toe)
                    } else lineTo(left,top+tf)
                    close()
                }
                drawPath(path, steel, style = pen)
            }
            family in channels -> {
                val slope = if (family == "UPN") tf*.22f else 0f
                val toe = if (family == "UPN") 4f*s else 0f
                val path = Path().apply {
                    moveTo(left, top)
                    lineTo(right, top)
                    if (family=="UPN") {
                        lineTo(right,top+tf-slope-toe)
                        quadraticBezierTo(right,top+tf-slope,right-toe,top+tf-slope)
                    } else lineTo(right,top+tf)
                    lineTo(left + tw + rad, top + tf + slope)
                    quadraticBezierTo(left + tw, top + tf + slope, left + tw, top + tf + slope + rad)
                    lineTo(left + tw, bottom - tf - slope - rad)
                    quadraticBezierTo(left + tw, bottom - tf - slope, left + tw + rad, bottom - tf - slope)
                    if (family=="UPN") {
                        lineTo(right-toe,bottom-tf+slope)
                        quadraticBezierTo(right,bottom-tf+slope,right,bottom-tf+slope+toe)
                    } else lineTo(right,bottom-tf)
                    lineTo(right, bottom)
                    lineTo(left, bottom)
                    close()
                }
                drawPath(path, steel, style = pen)
            }
            family == "SHS" || family == "RHS" -> {
                val radius = 13f*s
                val thick = max(5f, min(bw, bh)*.084f)
                drawRoundRect(steel, Offset(left,top), Size(bw,bh), CornerRadius(radius), style=pen)
                drawRoundRect(steel, Offset(left+thick,top+thick), Size(bw-2*thick,bh-2*thick),
                    CornerRadius(max(2f,radius-thick)), style=pen)
            }
            family == "CHS" -> {
                val diameter = min(bw,bh)
                val thick = diameter*.105f
                drawCircle(steel, radius=diameter/2f, center=Offset(cx,cy), style=pen)
                drawCircle(steel, radius=diameter/2f-thick, center=Offset(cx,cy), style=pen)
            }
            family == "RSA" -> {
                val angleT = max(11f, min(bw,bh)*.15f)
                val root = 13f*s
                val toe = max(3f,4f*s)
                val path = Path().apply {
                    moveTo(left, top)
                    lineTo(left+angleT-toe, top)
                    quadraticBezierTo(left+angleT,top, left+angleT,top+toe)
                    lineTo(left+angleT,bottom-angleT-root)
                    quadraticBezierTo(left+angleT,bottom-angleT,left+angleT+root,bottom-angleT)
                    lineTo(right-toe,bottom-angleT)
                    quadraticBezierTo(right,bottom-angleT,right,bottom-angleT+toe)
                    lineTo(right,bottom)
                    lineTo(left,bottom)
                    close()
                }
                drawPath(path,steel,style=pen)
            }
            family == "T" -> {
                val path = Path().apply {
                    moveTo(left,top); lineTo(right,top);lineTo(right,top+tf)
                    lineTo(cx+tw/2f+rad,top+tf)
                    quadraticBezierTo(cx+tw/2f,top+tf,cx+tw/2f,top+tf+rad)
                    lineTo(cx+tw/2f,bottom);lineTo(cx-tw/2f,bottom)
                    lineTo(cx-tw/2f,top+tf+rad)
                    quadraticBezierTo(cx-tw/2f,top+tf,cx-tw/2f-rad,top+tf)
                    lineTo(left,top+tf);close()
                }
                drawPath(path,steel,style=pen)
            }
            family == "PLATE" -> drawRect(steel, Offset(left,top),Size(bw,bh),style=pen)
        }
        if (family == "PLATE") {
            vDim(top,bottom,left-30f*u,left,"t")
        } else {
            hDim(left,right,top-28f*u,top,"${if (family=="CHS") "D" else "b"}")
            if (family!="CHS") vDim(top,bottom,left-31f*u,left,"h")
            fun thicknessH(x1: Float, x2: Float, y: Float, tag: String) {
                drawLine(accent, Offset(x1-7*u,y), Offset(x2+7*u,y), 1.2f*u)
                for (x in listOf(x1,x2)) drawLine(accent,Offset(x-3*u,y+4*u),Offset(x+3*u,y-4*u),1.2f*u)
                textAt(tag,if(tag == "t") x2+17*u else x1-17*u,y-8*u)
            }
            fun thicknessV(y1: Float, y2: Float, x: Float, edge: Float, tag: String) {
                for (y in listOf(y1,y2)) {
                    drawLine(subtleInk,Offset(edge,y),Offset(x+5*u,y),u)
                    drawLine(accent,Offset(x-3*u,y+4*u),Offset(x+3*u,y-4*u),1.2f*u)
                }
                drawLine(accent,Offset(x,y1-7*u),Offset(x,y2+7*u),1.2f*u)
                textAt(tag,x+13*u,(y1+y2)/2+5*u)
            }
            when {
                family in rolledI || family == "T" -> {
                    thicknessH(cx-tw/2,cx+tw/2,cy,"tw")
                    thicknessV(top,top+tf,right+13*u,right,"tf")
                    leader("r1",Offset(cx+tw/2+rad*.3f,top+tf+rad*.3f),Offset(right+28*u,top+tf+40*u))
                    if (family == "IPN") leader("r2",Offset(right,bottom-tf),Offset(right+28*u,bottom-20*u))
                }
                family in channels -> {
                    thicknessH(left,left+tw,cy,"tw")
                    thicknessV(top,top+tf,right+13*u,right,"tf")
                    leader("r1",Offset(left+tw+rad*.3f,top+tf+rad*.3f),Offset(right+28*u,top+tf+40*u))
                    if (family == "UPN") leader("r2",Offset(right,bottom-tf),Offset(right+28*u,bottom-20*u))
                }
                family == "RSA" -> {
                    val angleT = max(11f,min(bw,bh)*.15f)
                    thicknessV(bottom-angleT,bottom,right+13*u,right,"t")
                    leader("r1",Offset(left+angleT+rad*.3f,bottom-angleT-rad*.3f),Offset(right-8*u,cy))
                    leader("r2",Offset(right-3*s,bottom-angleT),Offset(right+28*u,cy+30*u))
                }
                family == "CHS" -> {
                    val rr=min(bw,bh)/2
                    thicknessH(cx+rr-rr*.21f,cx+rr,cy,"t")
                }
                else -> {
                    val thick=max(5f,min(bw,bh)*.084f)
                    thicknessH(right-thick,right,cy,"t")
                }
            }
        }
    }
}
// Tool 2: retained from v0.1 with high-contrast labels.
private data class UnitDef(val name: String, val factor: Double, val offset: Double = 0.0)
private val unitsByCategory = linkedMapOf(
    "Length" to listOf(UnitDef("mm",.001),UnitDef("cm",.01),UnitDef("m",1.0),UnitDef("km",1000.0),UnitDef("in",.0254),UnitDef("ft",.3048)),
    "Area" to listOf(UnitDef("mm²",1e-6),UnitDef("cm²",1e-4),UnitDef("m²",1.0),UnitDef("ft²",.09290304)),
    "Volume" to listOf(UnitDef("mm³",1e-9),UnitDef("cm³",1e-6),UnitDef("m³",1.0),UnitDef("L",.001),UnitDef("UK gal",.00454609),UnitDef("US gal",.003785411784)),
    "Force" to listOf(UnitDef("N",1.0),UnitDef("kN",1000.0),UnitDef("MN",1e6),UnitDef("lbf",4.448221615)),
    "Stress / Pressure" to listOf(UnitDef("Pa",1.0),UnitDef("kPa",1000.0),UnitDef("MPa",1e6),UnitDef("GPa",1e9),UnitDef("N/mm²",1e6),UnitDef("bar",1e5),UnitDef("psi",6894.757293)),
    "Mass" to listOf(UnitDef("g",.001),UnitDef("kg",1.0),UnitDef("tonne",1000.0),UnitDef("lb",.45359237)),
    "Moment / Torque" to listOf(UnitDef("N·mm",.001),UnitDef("N·m",1.0),UnitDef("kN·m",1000.0),UnitDef("lbf·ft",1.355817948)),
    "Distributed load" to listOf(UnitDef("N/m",1.0),UnitDef("kN/m",1000.0),UnitDef("N/mm",1000.0)),
    "Surface load" to listOf(UnitDef("N/m²",1.0),UnitDef("kN/m²",1000.0),UnitDef("kg/m² (mass)",9.80665)),
    "Angle" to listOf(UnitDef("degrees",1.0),UnitDef("radians",180.0/Math.PI)),
    "Fuel" to listOf(UnitDef("L/100 km",1.0),UnitDef("km/L",1.0),UnitDef("mpg (UK)",1.0),UnitDef("mpg (US)",1.0),UnitDef("US gal/100 mi",1.0)),
    "Temperature" to listOf(UnitDef("°C",1.0),UnitDef("°F",5.0/9.0,-32.0),UnitDef("K",1.0,-273.15))
)

// The same thumb-side navigation pattern as the steel library. Categories remain visible.
private val converterTabs = linkedMapOf(
    "Length" to "LEN", "Area" to "AREA", "Volume" to "VOL", "Force" to "FORCE",
    "Stress / Pressure" to "PRESS", "Mass" to "MASS", "Moment / Torque" to "TORQUE",
    "Distributed load" to "LINE", "Surface load" to "SURF", "Angle" to "ANGLE",
    "Temperature" to "TEMP", "Fuel" to "FUEL"
)

@Composable private fun Converter() {
    var group by remember { mutableStateOf("Force") }
    var input by remember { mutableStateOf("1") }
    var from by remember { mutableIntStateOf(0) }
    var to by remember { mutableIntStateOf(1) }
    val units = unitsByCategory.getValue(group)
    val result = input.trim().replace(',', '.').toDoubleOrNull()?.let { n ->
        val source = units[from]; val target = units[to]
        if(group == "Fuel") convertFuel(n,from,to) else (n + source.offset) * source.factor / target.factor - target.offset
    }
    Row(Modifier.fillMaxSize().padding(start = 13.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Text("Unit converter", color = ink, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
            Text(group, color = secondaryInk, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.BottomStart) {
            Column(Modifier.fillMaxWidth().fadedVerticalScroll(rememberScrollState())) {
            Column(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(14.dp)).padding(16.dp)) {
                Text("RESULT", color = secondaryInk, fontSize = 11.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(8.dp))
                Text(if (result == null || !result.isFinite()) "—" else String.format(Locale.US,"%.8g",result),
                    color = accent, fontSize = 29.sp, fontWeight = FontWeight.SemiBold)
                Text(units[to].name, color = secondaryInk, fontSize = 13.sp)
            }
            if(group == "Fuel") Text("UK and US gallons differ. Fuel economy (mpg, km/L) is reciprocal to consumption (L/100 km). Enter a positive value.",color=secondaryInk,fontSize=11.sp)
            if (group == "Surface load") {
                Spacer(Modifier.height(10.dp))
                Text("kg/m² is mass/area; conversion to force uses standard gravity (9.80665 m/s²).",
                    color = secondaryInk, fontSize = 11.sp)
            }
            Spacer(Modifier.height(8.dp))
            Column(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(14.dp)).padding(14.dp)) {
                Text("INPUT", color = ink, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = input, onValueChange = { input = it },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    label = { Text("Value") }, colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent, unfocusedBorderColor = outline,
                        focusedTextColor = ink, unfocusedTextColor = ink,
                        focusedLabelColor = accent, unfocusedLabelColor = secondaryInk))
                Spacer(Modifier.height(15.dp))
                Text("FROM", color = secondaryInk, fontSize = 11.sp, modifier=Modifier.align(Alignment.End))
                Spacer(Modifier.height(7.dp))
                UnitPicker(units, from) { from = it }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("SWAP ↕", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)).clickable {
                            val oldFrom = from; from = to; to = oldFrom
                        }.padding(7.dp))
                    Spacer(Modifier.weight(1f))
                    Text("TO", color=secondaryInk,fontSize=11.sp)
                }
                Spacer(Modifier.height(7.dp))
                UnitPicker(units, to) { to = it }
            }
            Spacer(Modifier.height(4.dp))
            }
            }
        }
        ToolTabRail(items = converterTabs.keys.toList(), selected = group,
            short = { converterTabs[it] ?: it }, onSelect = { group = it; from = 0; to = 1 })
    }
}

@Composable private fun UnitPicker(units: List<UnitDef>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().fadedHorizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End)) {
        units.forEachIndexed { i, unit ->
            val active = selected == i
            Box(Modifier.clip(RoundedCornerShape(8.dp))
                .border(1.dp, if (active) accent else outline, RoundedCornerShape(8.dp))
                .background(if (active) Color(0xFF253442) else background, RoundedCornerShape(8.dp))
                .clickable { onSelect(i) }.padding(horizontal = 12.dp, vertical = 11.dp)) {
                Text(unit.name, color = if (active) accent else ink, fontSize = 12.sp)
            }
        }
    }
}






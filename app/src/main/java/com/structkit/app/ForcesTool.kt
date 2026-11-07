package com.structkit.app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

internal val updateInk = Color(0xFFD9DDE3)
internal val updateMuted = Color(0xFFA5AFBB)
internal val updateBlue = Color(0xFF9EC5E8)
internal fun decimal(text: String) = text.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
internal fun fixed(value: Double, digits: Int = 2) = String.format(Locale.UK, "%.${digits}f", value)
@Composable internal fun NumberInput(label: String, value: String, change: (String) -> Unit) {
    OutlinedTextField(value, change, label = { Text(label, fontSize = 12.sp) }, singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}
@Composable internal fun ResultLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = updateMuted, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(value, color = updateBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
@Composable internal fun SourceLink(label: String, url: String) {
    val context = LocalContext.current
    Text(label + " ↗", color = updateBlue, fontSize = 12.sp, modifier = Modifier.clickable {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.padding(vertical = 8.dp))
}
@Composable internal fun ForcesTool() {
    var tab by remember { mutableStateOf("Rain") }
    var area by remember { mutableStateOf("50") }
    var areaBasis by remember { mutableStateOf("Plan area") }
    var slope by remember { mutableStateOf("15") }
    Row(Modifier.fillMaxSize().padding(start = 12.dp, end = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight().fadedVerticalScroll(rememberScrollState())) {
            Text("Forces calculator", color = updateInk, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
            Text("Roof actions · $tab", color = updateMuted, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            if(tab=="Wind") WindTool() else {
            InfoPanel(Modifier.fillMaxWidth()) {
                ToggleChips(listOf("Plan area", "Roof surface"), areaBasis) { areaBasis = it }
                NumberInput("${areaBasis} (m²)", area) { area = it }
                NumberInput("Roof slope (degrees, 0–<90)", slope) { slope = it }
                Text("Plan area is the horizontal footprint. For equal slopes, roof surface × cos(angle) gives plan area.", color = updateMuted, fontSize = 12.sp)
            }
            Spacer(Modifier.height(10.dp))
            val angle = decimal(slope)
            val plan = decimal(area)?.let { a -> angle?.let { roofPlanArea(a, it, areaBasis == "Roof surface") } }
            if (plan == null || angle == null) Text("Enter a positive area and a slope from 0 to below 90°.", color = updateBlue)
            else {
                ResultLine("Horizontal catchment / loaded area", "${fixed(plan)} m²")
                if (tab == "Rain") RainLoads(plan) else SnowLoads(plan, angle)
            }
            }
            Spacer(Modifier.height(24.dp))
        }
        ToolTabRail(listOf("Rain", "Snow", "Wind"), tab) { tab = it }
    }
}
@Composable private fun RainLoads(plan: Double) {
    var rain by remember { mutableStateOf("25") }
    var efficiency by remember { mutableStateOf("90") }
    var stored by remember { mutableStateOf("0") }
    InfoPanel(Modifier.fillMaxWidth()) {
        NumberInput("Rainfall depth over the event (mm)", rain) { rain = it }
        NumberInput("Collection efficiency (0–100%)", efficiency) { efficiency = it }
        Text("Rainfall depth is still needed: slope changes the catchment projection, not the amount of rain falling per horizontal square metre.", color = updateMuted, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        NumberInput("Retained water: equivalent depth over plan (mm)", stored) { stored = it }
        Text("Use retained volume ÷ plan area × 1,000. This is water actually held on the roof, not all water collected during the storm. A draining roof does not carry the full storm volume at once.", color = updateMuted, fontSize = 12.sp)
    }
    val result = decimal(rain)?.let { r -> decimal(efficiency)?.let { e -> decimal(stored)?.let { roofWater(plan, r, e, it) } } }
    Spacer(Modifier.height(10.dp))
    InfoPanel(Modifier.fillMaxWidth()) {
        if (result == null) Text("Check the rainfall, efficiency and retained-water inputs.", color = updateBlue)
        else {
            ResultLine("Collected rain", "${fixed(result.litres)} L")
            ResultLine("Collected volume", "${fixed(result.litres / 1000, 3)} m³")
            ResultLine("Retained-water load / plan area", "${fixed(result.pressure, 3)} kN/m²")
            ResultLine("Total vertical water force", "${fixed(result.force)} kN")
            Text("V = Aplan × rain depth × efficiency. F = ρgVretained; ρ = 1,000 kg/m³. These are gravity loads, excluding raindrop impact, wind, blocked-drain hydraulics and ponding instability. Non-uniform ponding requires its actual loaded footprint.", color = updateMuted, fontSize = 12.sp)
        }
    }
}
@Composable private fun SnowLoads(plan: Double, angle: Double) {
    var method by remember { mutableStateOf("Ground-load method") }
    var zone by remember { mutableStateOf("LT zone I") }
    var ground by remember { mutableStateOf("1.2") }
    var ce by remember { mutableStateOf("1.0") }
    var ct by remember { mutableStateOf("1.0") }
    var retained by remember { mutableStateOf(false) }
    var depth by remember { mutableStateOf("100") }
    var density by remember { mutableStateOf("200") }
    InfoPanel(Modifier.fillMaxWidth()) {
        ToggleChips(listOf("Ground-load method", "Measured roof snow"), method) { method = it }
        if (method == "Ground-load method") {
            ToggleChips(listOf("LT zone I", "LT zone II", "Custom"), zone) {
                zone = it; if (it != "Custom") ground = if (it == "LT zone I") "1.2" else "1.6"
            }
            NumberInput("Ground snow load sk (kN/m²)", ground) { ground = it; zone = "Custom" }
            NumberInput("Exposure coefficient Ce", ce) { ce = it }
            NumberInput("Thermal coefficient Ct", ct) { ct = it }
            Row { Checkbox(retained, { retained = it }); Text("Snow guards / sliding obstructed", color = updateInk, fontSize = 12.sp, modifier = Modifier.padding(top = 12.dp)) }
            Text("LT ground-load references: zone I 1.2 and zone II 1.6 kN/m². Select the site zone from the official map; these are not snow densities. Ce = Ct = 1 are starting assumptions, not a site assessment.", color = updateMuted, fontSize = 12.sp)
            SourceLink("LT snow-zone map · STR 2.05.04:2003", "https://e-seimas.lrs.lt/rs/actualedition/TAIS.213447/SpZvKIooTB/")
        } else {
            ToggleChips(listOf("Fresh 100", "Settled 200", "Wet 400"), "") { density = it.substringAfterLast(' ') }
            NumberInput("Snow bulk density (kg/m³)", density) { density = it }
            NumberInput("Vertical snow depth on roof (mm)", depth) { depth = it }
            Text("Density presets are illustrative. Weigh a representative sample for an actual load. Depth is vertical, not normal to the slope; no slope reduction is applied to snow already present.", color = updateMuted, fontSize = 12.sp)
        }
    }
    val mu = snowShape(angle, retained)
    val q = if (method == "Ground-load method") decimal(ground)?.takeIf { it >= 0 }?.let { s ->
        decimal(ce)?.takeIf { it > 0 }?.let { e -> decimal(ct)?.takeIf { it > 0 }?.let { t -> mu * e * t * s } }
    } else decimal(density)?.takeIf { it > 0 }?.let { rho -> decimal(depth)?.takeIf { it >= 0 }?.let { rho * it / 1000 * GRAVITY / 1000 } }
    Spacer(Modifier.height(10.dp))
    InfoPanel(Modifier.fillMaxWidth()) {
        if (q == null || !q.isFinite() || !(q * plan).isFinite()) Text("Enter valid positive coefficients / density and non-negative load or depth.", color = updateBlue)
        else {
            if (method == "Ground-load method") ResultLine("Balanced roof shape factor μ", fixed(mu, 3))
            ResultLine("Snow load / horizontal area", "${fixed(q, 3)} kN/m²")
            ResultLine("Equivalent mass / horizontal area", "${fixed(q * 1000 / GRAVITY)} kg/m²")
            ResultLine("Total vertical snow force", "${fixed(q * plan)} kN")
            Text(if (method == "Ground-load method") "s = μ × Ce × Ct × sk. Basic balanced mono/duopitch case: μ = 0.8 to 30°, reducing linearly to 0 at 60° if sliding is unrestricted. Obstructed sliding retains at least 0.8. This is an unfactored reference case; unbalanced snow, drifts, adjacent roofs, local accumulations and load combinations require separate checks." else "s = density × vertical depth × g. This estimates the existing snow mass only, not the design snow event.", color = updateMuted, fontSize = 12.sp)
        }
    }
    SourceLink("Eurocode snow-load worked guidance · JRC", "https://eurocodes.jrc.ec.europa.eu/sites/default/files/2021-12/handbook3.pdf")
}

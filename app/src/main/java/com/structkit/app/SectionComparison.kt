package com.structkit.app

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal data class SectionPin(val section: String, val property: String, val value: String, val sourceId: Int? = null)
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun ComparisonTray(pins: List<SectionPin>, remove: (SectionPin) -> Unit, details: (@Composable (SectionPin) -> Unit)? = null) {
    var selected by remember { mutableStateOf<SectionPin?>(null) }
    if (pins.isEmpty()) Text("Double-tap a property to pin · up to 3 · double-tap tile to unpin", color=updateMuted,fontSize=10.sp,modifier=Modifier.padding(6.dp))
    else Row(Modifier.fillMaxWidth().padding(4.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
        (0 until 3).forEach { index ->
            val pin=pins.getOrNull(index)
            if(pin==null) { Spacer(Modifier.weight(1f));return@forEach }
            Column(Modifier.weight(1f).border(1.dp,updateBlue,RoundedCornerShape(8.dp)).background(Color(0xFF17212B),RoundedCornerShape(8.dp))
                .combinedClickable(onClick={selected=pin},onDoubleClick={remove(pin)}).padding(6.dp)) {
                Text(pin.section,color=updateBlue,fontSize=11.sp,maxLines=2)
                Text(pin.property,color=updateMuted,fontSize=10.sp,maxLines=2)
                Text(pin.value,color=updateInk,fontSize=12.sp,maxLines=2)
            }
        }
    }
    selected?.let { p -> AlertDialog(containerColor=Color(0xFF17212B),titleContentColor=updateBlue,textContentColor=updateInk,onDismissRequest={selected=null},title={Text(p.section)},text={Column(Modifier.heightIn(max=440.dp).fadedVerticalScroll(rememberScrollState())) {
        if(details!=null)details(p) else { Text(p.property);Text(p.value) }
    }},confirmButton={TextButton(onClick={selected=null}){Text("Close")}}) }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable internal fun PinnableValue(section: String, label: String, value: String, pin: (SectionPin) -> Unit) {
    Column(Modifier.combinedClickable(onClick = {}, onDoubleClick = { pin(SectionPin(section, label, value)) }).padding(vertical = 7.dp, horizontal = 4.dp)) {
        Text(label, color = updateMuted, fontSize = 11.sp)
        Text(value, color = updateBlue, fontSize = 15.sp)
    }
}
@Composable internal fun SectionPrices(section: String, mass: Double, plate: Boolean, pin: (SectionPin) -> Unit) {
    var lt by remember { mutableStateOf("1.40") }
    var uk by remember { mutableStateOf("1.55") }
    var rates by remember { mutableStateOf(false) }
    val unit = if (plate) "m²" else "m"
    Text("INDICATIVE MATERIAL COST · ex VAT", color = updateMuted, fontSize = 11.sp)
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        decimal(lt)?.takeIf { it >= 0 }?.let { PinnableValue(section, "LT · estimate / $unit", "€${fixed(it * mass)}", pin) }
        decimal(uk)?.takeIf { it >= 0 }?.let { PinnableValue(section, "UK · estimate / $unit", "£${fixed(it * mass)}", pin) }
    }
    Text("Mass × indicative kg rate, not a quotation for this section. Excludes fabrication, coating, cutting, delivery and VAT. Plate is priced per m².", color = updateMuted, fontSize = 11.sp)
    TextButton(onClick = { rates = !rates }, contentPadding = PaddingValues(0.dp)) { Text(if (rates) "Close rates & sources ▴" else "Rates & sources ▾", fontSize = 11.sp) }
    if (rates) {
        NumberInput("LT estimate €/kg, ex VAT", lt) { lt = it }
        NumberInput("UK estimate £/kg, ex VAT", uk) { uk = it }
        Text("Reference check 5 Nov 2026. LT rate ≈ €1.40/kg: Lankava RHS 100×50×3 at €11.49/m incl. VAT, divided by 1.21 and idealised 6.7824 kg/m. UK £1.55/kg: Liontek UB 203×102×23 at £35.65/m ex VAT. Applying these reference rates to other families is a budget extrapolation; replace with a supplier's rate for the grade, section and quantity.", color = updateMuted, fontSize = 11.sp)
        SourceLink("LT reference · Lankava", "https://www.lankava.lt/staciakampis-vamzdis-100x50x3-mm")
        SourceLink("UK reference · Liontek", "https://www.liontek.co.uk/product-category/universal-beams-structural-steel/universal-beam/")
    }
}

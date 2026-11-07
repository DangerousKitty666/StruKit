package com.structkit.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable internal fun TimberSections(pin:(SectionPin)->Unit, pins:List<SectionPin>, remove:(SectionPin)->Unit) {
    var grade by remember { mutableStateOf("C24") }
    var expanded by remember { mutableStateOf<String?>(null) }
    val rho=if(grade=="C16")370.0 else 420.0
    Column(Modifier.fillMaxSize().fadedVerticalScroll(rememberScrollState()).padding(end=8.dp)) {
        Text("Timber",color=updateInk,fontSize=25.sp)
        ComparisonTray(pins,remove)
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
            listOf("C16","C24").forEach { g -> FilterChip(selected=grade==g,onClick={grade=g},label={Text(g)}) }
        }
        Text("Rectangular sections · b × h in mm. Listed dimensions are the calculation dimensions; check actual finished size before use.",color=updateMuted,fontSize=12.sp)
        Text("Size                 Mass kg/m",color=updateBlue,modifier=Modifier.padding(vertical=12.dp))
        val sizes=listOf(38 to 63,38 to 89,38 to 140,45 to 70,45 to 95,45 to 120,45 to 145,45 to 170,45 to 195,45 to 220,70 to 70,95 to 95,145 to 145)
        sizes.forEach { (b,h) ->
            val name="$grade $b×$h"
            val area=b*h/1000000.0
            Column(Modifier.fillMaxWidth().padding(bottom=7.dp).background(androidx.compose.ui.graphics.Color(0xFF151A20),RoundedCornerShape(10.dp)).padding(10.dp)) {
                Text("${if(expanded==name) "▾" else "▸"}  $b × $h     ${fixed(area*rho)}",color=updateInk,fontSize=16.sp,modifier=Modifier.fillMaxWidth().clickable{expanded=if(expanded==name)null else name}.padding(vertical=8.dp))
                if(expanded==name) {
                    PinnableValue(name,"Mass · kg/m",fixed(area*rho),pin)
                    PinnableValue(name,"Area · cm²",fixed(b*h/100.0),pin)
                    PinnableValue(name,"I, bending about b · cm⁴",fixed(b.toDouble()*h*h*h/120000.0),pin)
                    PinnableValue(name,"W, bending about b · cm³",fixed(b.toDouble()*h*h/6000.0),pin)
                    PinnableValue(name,"Mean density · kg/m³",fixed(rho),pin)
                    PinnableValue(name,"Mean E parallel · MPa",if(grade=="C16")"8000" else "11000",pin)
                    PinnableValue(name,"Characteristic bending · MPa",if(grade=="C16")"16" else "24",pin)
                    PinnableValue(name,"LT budget estimate · €/m",fixed(area*13.62/0.02565),pin)
                    PinnableValue(name,"UK budget estimate · £/m incl VAT",fixed(area*11.96/(.047*.100*4.8)),pin)
                }
            }
        }
        Text("Weight uses EN 338 mean density. Section properties use rectangular geometry with h as bending depth. These are reference properties, not a load capacity check.",color=updateMuted,fontSize=12.sp)
        Text("Price basis · 6 Nov 2026: LT C24 45×95×6000, €13.62 (VAT status not stated); UK advertised 47×100×4800 C24, £11.96 incl VAT. Other sizes and C16 use volume extrapolation from these C24 anchors, not supplier quotations. Treatment, grade premiums and delivery are not modelled.",color=updateMuted,fontSize=12.sp)
        SourceLink("LT · Timbershop", "https://timbershop.lt/p/graduota-mediena-c24-45x95/")
        SourceLink("UK · Clarke Fencing", "https://clarkefencing.co.uk/product/48m-x-100-x-47mm-c24-graded-timber/")
        SourceLink("EN 338 properties · Swedish Wood", "https://www.svenskttra.se/siteassets/5-publikationer/pdfer/design-of-timber-structures-2-2016.pdf")
    }
}


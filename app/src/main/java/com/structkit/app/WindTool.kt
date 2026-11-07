package com.structkit.app
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable internal fun WindTool() {
    var mode by remember{mutableStateOf("Mean wind speed")}
    var v by remember{mutableStateOf("25")};var iv by remember{mutableStateOf("0.20")};var rho by remember{mutableStateOf("1.25")}
    var area by remember{mutableStateOf("50")};var ext by remember{mutableStateOf("0.8")};var inside by remember{mutableStateOf("0.2")};var qp by remember{mutableStateOf("1.0")}
    InfoPanel(Modifier.fillMaxWidth()) {
        ToggleChips(listOf("Mean wind speed","Peak pressure"),mode){mode=it}
        if(mode=="Mean wind speed") {
            NumberInput("Mean wind speed at reference height · m/s",v){v=it}
            NumberInput("Turbulence intensity Iv · fraction",iv){iv=it}
            NumberInput("Air density · kg/m³",rho){rho=it}
        } else NumberInput("Peak velocity pressure qp · kN/m²",qp){qp=it}
        NumberInput("Loaded surface area · m²",area){area=it}
        NumberInput("External pressure coefficient cpe",ext){ext=it}
        NumberInput("Internal pressure coefficient cpi",inside){inside=it}
        Text("Starting values are examples. Use site-specific mean speed and turbulence at the loaded height—not a weather-app gust or basic map speed. Alternatively enter qp from your National Annex/site assessment. Coefficients depend on building shape, roof angle, zone, loaded area and openings.",color=updateMuted,fontSize=12.sp)
    }
    val values=(if(mode=="Peak pressure")listOf("0","0","1.25",area,ext,inside)else listOf(v,iv,rho,area,ext,inside)).map(::decimal)
    val result=if(values.all{it!=null}&&(mode!="Peak pressure"||decimal(qp)!=null))windSurface(values[0]!!,values[1]!!,values[2]!!,values[3]!!,values[4]!!,values[5]!!,if(mode=="Peak pressure")decimal(qp)else null)else null
    InfoPanel(Modifier.fillMaxWidth().padding(top=10.dp)) {
        if(result==null)Text("Check numeric inputs, positive area and the coefficient range −5…+5.",color=updateBlue)
        else {
            if(mode=="Mean wind speed")ResultLine("Mean velocity pressure", "${fixed(result.meanPressure,3)} kN/m²")
            ResultLine("Peak velocity pressure qp", "${fixed(result.peakPressure,3)} kN/m²")
            ResultLine("External pressure", "${fixed(result.external,3)} kN/m²")
            ResultLine("Internal pressure", "${fixed(result.internal,3)} kN/m²")
            ResultLine("Net surface pressure", "${fixed(result.net,3)} kN/m²")
            ResultLine("Surface-normal force", "${fixed(result.force)} kN")
            Text(if(result.net<0)"Negative = outward suction / roof uplift." else "Positive = inward pressure on the selected surface.",color=updateBlue,fontSize=12.sp)
        }
        Text("qp = (1 + 7Iv) × ½ρvm²; w = qp(cpe − cpi); F = wA. Internal and external reference pressures are assumed equal here. This is one unfactored surface load case, not the whole building wind resultant. Check opposite internal-pressure signs, all zones/directions, differing reference heights, dynamic response and load combinations separately.",color=updateMuted,fontSize=11.sp)
        SourceLink("JRC · Eurocode wind pressure method","https://eurocodes.jrc.ec.europa.eu/sites/default/files/2022-06/Bridge_Design-Eurocodes-Worked_examples.pdf")
        SourceLink("SCI · building wind actions, section 4.2","https://www.steelconstruction.info/images/c/c5/SCI_P397.pdf")
    }
}

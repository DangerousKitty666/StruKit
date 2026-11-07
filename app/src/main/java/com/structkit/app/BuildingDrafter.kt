package com.structkit.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import org.json.JSONArray

@Composable internal fun BuildingDrafter(members: List<DraftMember>) {
    var length by remember { mutableStateOf("10") }; var width by remember { mutableStateOf("5") }
    var low by remember { mutableStateOf("3") };var high by remember { mutableStateOf("3.5") };var pitch by remember { mutableStateOf("6") }
    var heightMode by remember { mutableStateOf("Apex height") }
    var roof by remember { mutableStateOf("Auto") }
    var bay by remember { mutableStateOf("") }
    var col by remember { mutableIntStateOf(members.indexOfFirst { it.name == "IPE 160" }.coerceAtLeast(0)) }
    var beam by remember { mutableIntStateOf(members.indexOfFirst { it.name == "IPE 160" }.coerceAtLeast(0)) }
    var grade by remember { mutableStateOf("S355") }
    var dead by remember { mutableStateOf("0.35") };var snow by remember { mutableStateOf("1.0") };var wind by remember { mutableStateOf("0.5") }
    var manual by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxSize().padding(start=12.dp,end=8.dp), horizontalArrangement=Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Text("Building drafter", color=updateInk,fontSize=26.sp)
            Text("Concept layout & main-member take-off",color=updateMuted,fontSize=12.sp)
            Column(Modifier.fillMaxWidth().weight(1f).fadedVerticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(10.dp))
                InfoPanel(Modifier.fillMaxWidth()) {
                    Text("Concept geometry, member screening and quantities. Suggestions use independent beam/cantilever models, not a complete portal-frame design. Confirm site loads and the real frame system before using section sizes.",color=updateMuted,fontSize=12.sp)
                    NumberInput("Footprint length (m)",length){length=it};NumberInput("Footprint width / frame span (m)",width){width=it}
                    ToggleChips(listOf("Auto","Mono-pitch","Duo-pitch"),roof){roof=it}
                    NumberInput("Lowest eaves level (m)",low){low=it}
                    ToggleChips(listOf("Apex height","Roof angle"),heightMode){heightMode=it}
                    if(heightMode=="Apex height")NumberInput("Highest roof level (m)",high){high=it} else NumberInput("Roof angle (degrees)",pitch){pitch=it}
                    NumberInput("Maximum bay spacing (m), blank = suggestion",bay){bay=it}
                }
                val l=decimal(length);val w=decimal(width);val e=decimal(low)
                val small=l!=null&&w!=null&&l*w<=50
                val mono=roof=="Mono-pitch"||(roof=="Auto"&&small)
                val suggestedBay=if(small)3.0 else 6.0
                val a=if(heightMode=="Apex height")decimal(high) else decimal(pitch)?.takeIf{it in 0.0..60.0}?.let{p->if(e!=null&&w!=null)e+tan(Math.toRadians(p))*w/(if(mono)1 else 2)else null}
                val b=if(bay.isBlank())suggestedBay else decimal(bay)
                val frame=if(l!=null&&w!=null&&e!=null&&a!=null&&b!=null)draftFrame(l,w,e,a,b,mono)else null
                if(frame==null)Text("Check inputs: length 0.5–200 m, width 0.5–100 m, eaves 1–30 m, apex ≥ eaves and ≤50 m, slope ≤60°, bay 1–12 m.",color=updateBlue,fontSize=12.sp)
                else {
                    ResultLine("Footprint", "${fixed(frame.area)} m²")
                    ResultLine("Roof form / slope", "${if(mono)"Mono" else "Duo"} · ${fixed(frame.slope)}°")
                    ResultLine("Main support assemblies", "${frame.frames}")
                    ResultLine("Equal bays", "${frame.bays} × ${fixed(frame.spacing)} m")
                    Text("Auto uses mono-pitch up to 50 m². Spacing suggestions: 3 m for a compact study, 6 m for larger layouts. These are editable planning assumptions, not calculated structural limits. Levels and spans are model lines; section depth and haunches reduce clear space.",color=updateMuted,fontSize=12.sp)
                    Spacer(Modifier.height(10.dp))
                    InfoPanel(Modifier.fillMaxWidth()) {
                        Text("Suggested material schedule",color=updateBlue,fontSize=16.sp)
                        ToggleChips(listOf("S235","S275","S355"),grade){grade=it}
                        NumberInput("Permanent roof load · kN/m² plan (excluding main steel)",dead){dead=it}
                        NumberInput("Roof snow load · kN/m² plan",snow){snow=it}
                        NumberInput("Wind pressure magnitude · kN/m²",wind){wind=it}
                        Text("Initial loads are examples, not Lithuanian or UK site values. Wind is screened as wall pressure and roof uplift of the entered magnitude; actual zone coefficients and snow arrangements must be established separately.",color=updateMuted,fontSize=11.sp)
                        val g=decimal(dead);val q=decimal(snow);val v=decimal(wind)
                        val suggestion=remember(frame,members,g,q,v,grade){if(g!=null&&q!=null&&v!=null)suggestMembers(frame,members,g,q,v,grade)else null}
                        val suggestedCol=suggestion?.column;val suggestedBeam=suggestion?.rafter
                        ResultLine("Suggested columns",suggestedCol?.member?.name?:"No suitable candidate")
                        ResultLine("Suggested rafters",suggestedBeam?.member?.name?:"No suitable candidate")
                        ToggleChips(listOf("Suggestions","Manual sections"),if(manual)"Manual sections" else "Suggestions"){manual=it=="Manual sections"}
                        if(manual){MemberPicker("Columns",members,col){col=it};MemberPicker("Rafters",members,beam){beam=it}}
                        val chosenCol=if(manual)members[col]else suggestedCol?.member
                        val chosenBeam=if(manual)members[beam]else suggestedBeam?.member
                        if(chosenCol!=null&&chosenBeam!=null) {
                        ResultLine("Columns · ${2*frame.frames} pieces", "${fixed(frame.columnLength)} m total")
                        ResultLine("Rafters · ${frame.frames*(if(mono)1 else 2)} pieces", "${fixed(frame.rafterPieceLength)} m each")
                        val columnKg=frame.columnLength*chosenCol.kgPerM
                        val rafterKg=frame.rafterLength*chosenBeam.kgPerM
                        ResultLine("Column mass", "${fixed(columnKg)} kg")
                        ResultLine("Rafter mass", "${fixed(rafterKg)} kg")
                        ResultLine("One support assembly", "≈ ${fixed((columnKg+rafterKg)/frame.frames)} kg")
                        ResultLine("Main members total", "≈ ${fixed(columnKg+rafterKg)} kg")
                        Text("${chosenCol.name} columns / ${chosenBeam.name} rafters · $grade. ${if(manual)"Manual selections are not checked." else "Lightest available sections meeting this screening model."} Mass excludes purlins, rails, bracing, haunches, plates, bolts, cladding, foundations and waste.",color=updateMuted,fontSize=12.sp)
                        } else Text("No schedule generated. Change geometry/loads or seek a different structural scheme. Screening range: span ≤40 m, highest level ≤12 m, slope ≤30°, loads 0–10 kN/m² and positive gravity load.",color=updateBlue,fontSize=12.sp)
                        suggestedCol?.let{ResultLine("Column model deflection / limit","${fixed(it.deflectionMm)} / ${fixed(it.limitMm)} mm")}
                        suggestedBeam?.let{ResultLine("Rafter model deflection / limit","${fixed(it.deflectionMm)} / ${fixed(it.limitMm)} mm")}
                        var assumptions by remember{mutableStateOf(false)}
                        TextButton(onClick={assumptions=!assumptions}){Text(if(assumptions)"Hide calculation basis ▴" else "Calculation basis & project references ▾")}
                        if(assumptions) {
                            Text("Rafter: full developed roof length as a simply supported straight beam, M=wL²/8, δ=5wL⁴/384EI; fully restrained compression flange assumed. Column: fixed-base cantilever, M=wH²/2, δ=wH⁴/8EI; weak-axis effective length 2H. The two models do not resolve frame joint forces or compatibility.\n\nScreen: elastic bending; column axial/bending sum with curve-c buckling reduction; conservative flange/web slenderness limits. Idealised sharp-corner I-section properties, E=210 GPa. Yield reduced by 20 MPa above 16 mm thickness (up to 40 mm). Gravity 1.35G+1.5Q; uplift 0.9G−1.5W; lateral 1.5W. Trial deflection limits L/250 and H/150. These are explicit screening assumptions, not a full Eurocode combination or member interaction check.\n\nArchived drawings A004, A007, A011, A012 and A014 show IPE/HEA/HEB families, internal supports, trusses and mixed grades. Their sizes are reference context, not a floor-area sizing rule. The search uses actual dimensions and mass from this app’s section library.\n\nPortal action, second-order response, lateral-torsional buckling, connections, haunches, base fixity, load combinations and foundations still require structural design. Passing this model does not demonstrate that a building is safe.",color=updateMuted,fontSize=12.sp)
                            SourceLink("SCI P397 · portal-frame design and preliminary-sizing limits","https://www.steelconstruction.info/images/c/c5/SCI_P397.pdf")
                        }
                    }
                    listOf("Top","Front","Side","Isometric").forEach { view ->
                        Spacer(Modifier.height(10.dp));InfoPanel(Modifier.fillMaxWidth()) {
                            Text(view,color=updateBlue,fontSize=15.sp)
                            FrameDrawing(frame,view,Modifier.fillMaxWidth().height(215.dp))
                        }
                    }
                    Text("Drawings show only primary member centrelines. Longitudinal bracing, purlin restraint and moment connections are not designed or shown. Assemblies cannot be erected from this concept sketch.",color=updateMuted,fontSize=12.sp)
                    SourceLink("SCI / BCSA · portal-frame layout & design", "https://steelconstruction.info/topics/design/portal-frames/")
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}
@Composable private fun MemberPicker(label:String,members:List<DraftMember>,selected:Int,choose:(Int)->Unit) {
    var open by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
        Box {
        OutlinedButton(onClick={open=true}){Text("$label ▾",fontSize=12.sp)}
        DropdownMenu(open,{open=false},modifier=Modifier.heightIn(max=440.dp),containerColor=Color.Black) {
            members.forEachIndexed { i,m -> DropdownMenuItem(text={Text("${m.name} · ${fixed(m.kgPerM)} kg/m",color=updateBlue,fontSize=12.sp)},onClick={choose(i);open=false}) }
        }
        }
        Text("${members[selected].name} · trial",color=updateBlue,fontSize=14.sp,modifier=Modifier.padding(start=12.dp))
    }
}
@Composable private fun FrameDrawing(f:FrameGeometry,view:String,modifier:Modifier) {
    Canvas(modifier) {
        fun project(x:Double,y:Double,z:Double):Pair<Double,Double> = when(view) {
            "Top" -> y to x
            "Front" -> x to -z
            "Side" -> y to -z
            else -> (x*.866+y*.5) to (x*.35-y*.35-z)
        }
        val lines=mutableListOf<Pair<Pair<Double,Double>,Pair<Double,Double>>>()
        fun segment(x:Double,y:Double,z:Double,xx:Double,yy:Double,zz:Double){lines.add(project(x,y,z) to project(xx,yy,zz))}
        for(i in 0..f.bays) {
            val y=i*f.spacing
            segment(0.0,y,0.0,0.0,y,f.eaves);segment(f.width,y,0.0,f.width,y,if(f.mono)f.apex else f.eaves)
            if(f.mono)segment(0.0,y,f.eaves,f.width,y,f.apex)
            else {segment(0.0,y,f.eaves,f.width/2,y,f.apex);segment(f.width/2,y,f.apex,f.width,y,f.eaves)}
        }
        val points=lines.flatMap{listOf(it.first,it.second)}
        val minX=points.minOf{it.first};val minY=points.minOf{it.second};val maxX=points.maxOf{it.first};val maxY=points.maxOf{it.second}
        val factor=minOf((size.width-100*density)/(maxX-minX).coerceAtLeast(1.0),(size.height-75*density)/(maxY-minY).coerceAtLeast(1.0))
        fun pt(p:Pair<Double,Double>)=Offset(((p.first-minX)*factor+(size.width-(maxX-minX)*factor)/2).toFloat(),((p.second-minY)*factor+(size.height-(maxY-minY)*factor)/2).toFloat())
        lines.forEach { (a,b)->drawLine(updateBlue,pt(a),pt(b),2f) }
        val dim=Color(0xFFE8B46D)
        val paint=android.graphics.Paint(3).apply{color=0xFFE8B46D.toInt();textSize=11*density;textAlign=android.graphics.Paint.Align.CENTER}
        fun dimension(a:Pair<Double,Double>,b:Pair<Double,Double>,offset:Offset,label:String) {
            val aa=pt(a);val bb=pt(b);val ap=aa+offset;val bp=bb+offset
            drawLine(dim.copy(alpha=.55f),aa,ap,1f);drawLine(dim.copy(alpha=.55f),bb,bp,1f)
            drawLine(dim,ap,bp,1.2f)
            val v=bp-ap;val len=v.getDistance().coerceAtLeast(1f);val normal=Offset(-v.y/len,v.x/len)*4f
            drawLine(dim,ap-normal,ap+normal,1.5f);drawLine(dim,bp-normal,bp+normal,1.5f)
            val c=(ap+bp)*.5f
            val textWidth=paint.measureText(label)
            drawRect(Color(0xFF14171B),Offset(c.x-textWidth/2-3,c.y-13*density),androidx.compose.ui.geometry.Size(textWidth+6,15*density))
            drawContext.canvas.nativeCanvas.drawText(label,c.x,c.y-2*density,paint)
        }
        when(view) {
            "Top"->{
                dimension(project(0.0,0.0,0.0),project(0.0,f.length,0.0),Offset(0f,-22*density),"${fixed(f.length)} m")
                dimension(project(0.0,f.length,0.0),project(f.width,f.length,0.0),Offset(30*density,0f),"${fixed(f.width)} m")
                dimension(project(f.width,0.0,0.0),project(f.width,f.spacing,0.0),Offset(0f,25*density),"${fixed(f.spacing)} m bay")
            }
            "Front"->{
                dimension(project(0.0,0.0,0.0),project(f.width,0.0,0.0),Offset(0f,24*density),"${fixed(f.width)} m")
                dimension(project(0.0,0.0,0.0),project(0.0,0.0,f.eaves),Offset(-30*density,0f),"${fixed(f.eaves)} m")
                if(!f.mono)drawLine(dim.copy(alpha=.55f),pt(project(f.width/2,0.0,f.apex)),pt(project(f.width,0.0,f.apex)),1f)
                dimension(project(f.width,0.0,0.0),project(f.width,0.0,f.apex),Offset(30*density,0f),"${fixed(f.apex)} m")
            }
            "Side"->{
                dimension(project(0.0,0.0,0.0),project(0.0,f.length,0.0),Offset(0f,25*density),"${fixed(f.length)} m")
                dimension(project(0.0,0.0,0.0),project(0.0,0.0,f.eaves),Offset(-30*density,0f),"${fixed(f.eaves)} m")
                dimension(project(0.0,0.0,f.apex),project(0.0,f.spacing,f.apex),Offset(0f,-20*density),"${fixed(f.spacing)} m bay")
            }
            else->{
                dimension(project(0.0,0.0,0.0),project(f.width,0.0,0.0),Offset(0f,22*density),"${fixed(f.width)} m")
                dimension(project(f.width,0.0,0.0),project(f.width,f.length,0.0),Offset(22*density,0f),"${fixed(f.length)} m")
                dimension(project(0.0,0.0,0.0),project(0.0,0.0,f.eaves),Offset(-24*density,0f),"${fixed(f.eaves)} m")
            }
        }
    }
}

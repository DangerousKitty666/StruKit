package com.structkit.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


internal val priceSuppliers=listOf(
    PriceSupplier("FH Brundle","UK","https://www.fhbrundle.co.uk/"),
    PriceSupplier("metals4U","UK","https://www.metals4u.co.uk/"),
    PriceSupplier("Cladco","UK","https://www.cladco.co.uk/z-purlins"),
    PriceSupplier("The Metal Store","UK","https://www.themetalstore.co.uk/"),
    PriceSupplier("Lytagra","LT","https://lytagra.lt/"),
    PriceSupplier("Metalmaster","LT","https://metalmaster.lt/"),
    PriceSupplier("Kovas","LT","https://kovas.lt/statybines-medziagos/metalai/vamzdis/"),
    PriceSupplier("b-a.eu","LT","https://www.b-a.eu/c11/Metalai"),
    PriceSupplier("Linoksa","LT","https://linoksa.com/lt/")
)

private fun quoteDate(time:Long)=java.text.SimpleDateFormat("dd MMM yyyy",java.util.Locale.UK).format(java.util.Date(time))
@Composable internal fun PriceUpdateButton(){
    val context=androidx.compose.ui.platform.LocalContext.current
    val scope=rememberCoroutineScope()
    var open by remember{mutableStateOf(false)}
    LaunchedEffect(Unit){PriceCatalog.load(context)}
    TextButton(onClick={open=true},contentPadding=PaddingValues(4.dp)){Text("Update prices",fontSize=11.sp)}
    if(open)AlertDialog(onDismissRequest={open=false},title={Text("Supplier prices")},text={
        Column(Modifier.heightIn(max=420.dp).fadedVerticalScroll(rememberScrollState())){
            Text("Checks public catalogue pages from all nine suppliers. Only exact-size prices with an explicit selling unit are accepted. Missing grades, blocked pages and quote-only products are not estimated.",fontSize=12.sp)
            Text(if(PriceCatalog.lastCheck==0L)"Bundled observations · refresh to check" else "Last check: "+quoteDate(PriceCatalog.lastCheck),fontSize=11.sp)
            if(PriceCatalog.busy)LinearProgressIndicator(Modifier.fillMaxWidth())
            priceSuppliers.forEach{supplier->
                Text(supplier.country+" · "+supplier.name,color=updateBlue,fontSize=13.sp,modifier=Modifier.padding(top=10.dp))
                Text(PriceCatalog.status[supplier.name] ?: "Not checked in this installation",fontSize=11.sp)
                SourceLink("Open supplier",supplier.url)
            }
            Text("Cached observations retain their original dates. Availability, delivery, cutting and quantity discounts must be confirmed with the supplier.",fontSize=11.sp)
        }
    },confirmButton={TextButton(enabled=!PriceCatalog.busy,onClick={scope.launch{PriceCatalog.refresh(context.applicationContext)}}){Text(if(PriceCatalog.busy)"Checking…"else"Check all suppliers")}},dismissButton={TextButton(onClick={open=false}){Text("Close")}})
}
@Composable internal fun SupplierPriceOptions(section:String){
    val context=androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit){PriceCatalog.load(context)}
    val hollow=listOf("RHS","SHS","CHS").any{section.startsWith(it)}
    val beam=listOf("IPE","IPN","HEA","HEB","HEM").any{section.startsWith(it)}
    var expanded by remember(section){mutableStateOf(true)}
    var country by remember(section){mutableStateOf("UK")}
    var grade by remember(section){mutableStateOf("All grades")}
    val grades=listOf("All grades")+(if(hollow)listOf("S235","S275","S355","Stainless 304","Stainless 304L","Stainless 316","Stainless 316L")else if(beam)listOf("S235","S275","S355")else emptyList())+"Unspecified"
    val matches=PriceCatalog.quotes.filter{sectionKey(it.section)==sectionKey(section)&&it.country==country}
    fun inGrade(g:String):Boolean=when(grade){"All grades"->true;"Stainless 304"->g in listOf("304","1.4301");"Stainless 304L"->g in listOf("304L","1.4307");"Stainless 316"->g in listOf("316","1.4401");"Stainless 316L"->g in listOf("316L","1.4404");else->g==grade||grade.startsWith("S")&&g.startsWith(grade)}
    val visible=matches.filter{inGrade(it.grade)}
    TextButton(onClick={expanded=!expanded},contentPadding=PaddingValues(0.dp)){Text(if(expanded)"Supplier & grade prices ▴" else "Supplier & grade prices ▾",fontSize=12.sp)}
    if(expanded){
        Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("UK","LT").forEach{FilterChip(selected=country==it,onClick={country=it},label={Text(it)})}}
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(5.dp)){grades.forEach{FilterChip(selected=grade==it,onClick={grade=it},label={Text(it,fontSize=11.sp)})}}
        if(visible.isEmpty())Text("No verified price for $section · $grade in this catalogue. Request a supplier quote; availability is not implied.",color=updateMuted,fontSize=11.sp)
        visible.forEach{q->
            Column(Modifier.fillMaxWidth().padding(vertical=7.dp)){
                Text(q.grade+" · "+q.supplier,color=updateBlue,fontSize=13.sp)
                Text((if(q.currency=="GBP")"£"else"€")+fixed(q.amount)+" / "+q.unit+" · "+q.tax,color=updateInk,fontSize=16.sp)
                if(q.finish.isNotBlank())Text(q.finish,color=updateMuted,fontSize=11.sp)
                Text("Observed "+quoteDate(q.checked)+" · confirm current price",color=updateMuted,fontSize=10.sp)
                SourceLink("Source",q.url)
            }
        }
        Text("Grade suffixes, finish and tax basis are shown as published. Unspecified-grade prices are not quotations for S235/S355 or stainless steel.",color=updateMuted,fontSize=10.sp)
        priceSuppliers.filter{it.country==country}.forEach{SourceLink(it.name,it.url)}
        Text("Budget estimates below are separate generic carbon-steel estimates, not prices for the selected grade.",color=updateMuted,fontSize=10.sp)
        Spacer(Modifier.height(8.dp))
    }
}

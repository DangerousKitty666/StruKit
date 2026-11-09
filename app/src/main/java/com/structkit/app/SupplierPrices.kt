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
    if(open)AlertDialog(onDismissRequest={open=false},containerColor=androidx.compose.ui.graphics.Color(0xEB17212B),titleContentColor=updateBlue,textContentColor=updateInk,title={Text("Update market estimates")},text={
        Column(Modifier.heightIn(max=420.dp).fadedVerticalScroll(rememberScrollState())){
            Text("Checks UK and LT catalogues, converts prices to ex-VAT per metre and averages comparable observations once per supplier. Exact grade/size averages update the table. Carbon-steel budget rates are calibrated separately; stainless sizes without comparable data show not available.",fontSize=12.sp)
            Text(if(PriceCatalog.lastCheck==0L)"Bundled observations · refresh to check" else "Last check: "+quoteDate(PriceCatalog.lastCheck),fontSize=11.sp)
            if(PriceCatalog.busy)LinearProgressIndicator(Modifier.fillMaxWidth())
            priceSuppliers.forEach{supplier->
                Text(supplier.country+" · "+supplier.name,color=updateBlue,fontSize=13.sp,modifier=Modifier.padding(top=10.dp))
                Text(PriceCatalog.status[supplier.name] ?: "Not checked in this installation",fontSize=11.sp)
                SourceLink("Open supplier",supplier.url)
            }
            Text("The main table shows UK/LT estimates, not individual supplier quotes. Old or inaccessible listings do not create new prices. Delivery, cutting and quantity discounts are excluded.",fontSize=11.sp)
        }
    },confirmButton={TextButton(enabled=!PriceCatalog.busy,onClick={scope.launch{PriceCatalog.refresh(context.applicationContext)}}){Text(if(PriceCatalog.busy)"Checking…"else"Check all suppliers")}},dismissButton={TextButton(onClick={open=false}){Text("Close")}})
}

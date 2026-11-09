package com.structkit.app

import android.content.Context
import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.util.Locale

internal object PriceCatalog {
    var quotes by mutableStateOf<List<PriceQuote>>(emptyList()); private set
    var status by mutableStateOf<Map<String,String>>(emptyMap()); private set
    var busy by mutableStateOf(false); private set
    var lastCheck by mutableLongStateOf(0); private set
    private var loaded=false
    fun load(context:Context){if(loaded)return;loaded=true;val prefs=context.getSharedPreferences("supplier-prices",0)
        val bundled=runCatching{decode(JSONArray(context.assets.open("supplier-prices.json").bufferedReader().use{it.readText()}))}.getOrDefault(emptyList())
        val cached=runCatching{decode(JSONArray(prefs.getString("quotes","[]")!!))}.getOrDefault(emptyList())
        quotes=(bundled+cached).groupBy{listOf(it.supplier,sectionKey(it.section),it.grade,it.finish,it.url)}.values.map{rows->rows.maxBy{it.checked}}
        lastCheck=prefs.getLong("lastCheck",0)
        status=runCatching{val o=JSONObject(prefs.getString("status","{}")!!);o.keys().asSequence().associateWith{o.getString(it)}}.getOrDefault(emptyMap())
    }
    fun decode(a:JSONArray)=List(a.length()){i->val o=a.getJSONObject(i);PriceQuote(o.getString("supplier"),o.getString("country"),o.getString("section"),o.optString("grade","Unspecified"),o.optString("finish"),o.getDouble("amount"),o.getString("currency"),o.getString("unit"),o.getString("tax"),o.getString("url"),o.getLong("checked"))}.filter{it.amount.isFinite()&&it.amount>0&&it.amount<1e7&&it.unit in listOf("m","m²")&&it.currency in listOf("GBP","EUR")&&it.url.startsWith("https://")}
    suspend fun refresh(context:Context){if(busy)return;load(context);busy=true
        try {supervisorScope {priceSuppliers.map{supplier->async(Dispatchers.IO){
            val fresh=mutableListOf<PriceQuote>();val failed=mutableListOf<String>();var checkedPages=0
            for(url in pricePages.getValue(supplier.name)){
                ensureActive()
                try{val html=fetch(url);checkedPages++;fresh+=parsePricePage(supplier,url,html,System.currentTimeMillis())}catch(e:CancellationException){throw e}catch(e:Exception){failed+=e.message.orEmpty()}
            }
            Triple(supplier.name,fresh.distinctBy{listOf(it.section,it.grade,it.finish,it.url)},when{
                fresh.isNotEmpty()->"${fresh.size} exact-size prices found · $checkedPages catalogue pages checked"+(if(failed.isNotEmpty())" · ${failed.size} pages unavailable" else "")
                checkedPages>0->"No unambiguous per-metre prices published in checked catalogue pages · use supplier quote"+(if(failed.isNotEmpty())"; ${failed.size} pages unavailable" else "")
                else->"Could not refresh: ${failed.firstOrNull() ?: "unavailable"}. Last verified data retained."
            })
        }}.awaitAll().forEach{(name,newQuotes,message)->
            // Never erase a prior observation after a block, outage or a changed page layout.
            val keys=newQuotes.map{listOf(it.supplier,it.section,it.grade,it.finish,it.url)}.toSet()
            quotes=(quotes.filter{listOf(it.supplier,it.section,it.grade,it.finish,it.url) !in keys}+newQuotes).sortedWith(compareBy({it.country},{it.section},{it.grade}))
            status=status+(name to message)
        }}
        lastCheck=System.currentTimeMillis()
        context.getSharedPreferences("supplier-prices",0).edit().putString("quotes",JSONArray(quotes.map{it.json()}).toString()).putString("status",JSONObject(status).toString()).putLong("lastCheck",lastCheck).apply()
        }finally{busy=false}
    }
    private fun fetch(address:String):String {
        var uri=URI(address)
        repeat(4){
            val connection=uri.toURL().openConnection() as HttpURLConnection
            try{connection.connectTimeout=8000;connection.readTimeout=10000;connection.instanceFollowRedirects=false
                connection.setRequestProperty("User-Agent","StruKit/0.789 (public price reference)")
                val code=connection.responseCode
                if(code in 300..399){val next=uri.resolve(connection.getHeaderField("Location") ?: error("Missing redirect"));require(next.scheme=="https"&&next.host.removePrefix("www.")==uri.host.removePrefix("www.")){"Supplier redirect needs browser"};uri=next}
                else {if(code!=200)error("HTTP $code");val data=connection.inputStream.use{it.readBytesBounded(4*1024*1024)};val charset=Regex("charset=([^; ]+)",RegexOption.IGNORE_CASE).find(connection.contentType.orEmpty())?.groupValues?.get(1)?.trim('"')?.let{runCatching{charset(it)}.getOrNull()} ?: Charsets.UTF_8
                    val html=String(data,charset);if(listOf("Checking your browser","Just a moment...","cf-chl-").any{html.contains(it,true)})error("Supplier requires browser verification");return html}
            }finally{connection.disconnect()}
        };error("Too many redirects")
    }
}
private fun java.io.InputStream.readBytesBounded(limit:Int):ByteArray {val out=java.io.ByteArrayOutputStream();val b=ByteArray(16384);while(true){val n=read(b);if(n<0)break;require(out.size()+n<=limit){"Catalogue page too large"};out.write(b,0,n)};return out.toByteArray()}

// Only known public catalogue pages. A refresh checks all suppliers, not an invented market-rate feed.
internal val pricePages=mapOf(
    "FH Brundle" to listOf("https://www.fhbrundle.co.uk/steel-sections/square-hollow-section","https://www.fhbrundle.co.uk/steel-sections/rectangular-hollow-section","https://www.fhbrundle.co.uk/steel-sections/circular-hollow-section"),
    "metals4U" to (listOf("https://www.metals4u.co.uk/materials/mild-steel/mild-steel-box-section?product_list_limit=100")+(1..4).map{"https://www.metals4u.co.uk/materials/stainless-steel/stainless-steel-box-section?p=$it"}+(1..5).map{"https://www.metals4u.co.uk/materials/stainless-steel/stainless-steel-tube?p=$it"}),
    "Cladco" to listOf("https://www.cladco.co.uk/z-purlins"),
    "The Metal Store" to listOf("https://www.themetalstore.co.uk/products/steel-box-section-uk-suppliers","https://www.themetalstore.co.uk/products/stainless-steel-tube"),
    "Lytagra" to listOf("https://lytagra.lt/metalo-gaminiai"),
    "Metalmaster" to listOf("https://metalmaster.lt/"),
    "Kovas" to listOf("https://kovas.lt/statybines-medziagos/metalai/vamzdis/profilinis/","https://kovas.lt/statybines-medziagos/metalai/vamzdis/apvalus-juodieji/","https://kovas.lt/statybines-medziagos/metalai/kampuotis/","https://kovas.lt/statybines-medziagos/metalai/plienine-juosta/","https://kovas.lt/statybines-medziagos/metalai/lovys/","https://kovas.lt/statybines-medziagos/metalai/lakstas/"),
    "b-a.eu" to listOf("https://www.b-a.eu/c11/Metalai"),
    "Linoksa" to listOf("https://linoksa.com/lt/81-apvalus-vamzdziai","https://linoksa.com/lt/82-profiliniai-vamzdziai")
)

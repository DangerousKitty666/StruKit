package com.structkit.app
import java.net.URI
import java.util.Locale
import org.json.JSONObject
import org.json.JSONArray
internal fun priceText(s:String):String {
    val stripped=s.replace(Regex("<(script|style)\\b.*?</\\1>",setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL))," ").replace(Regex("<[^>]*>")," ")
    val named=mapOf("nbsp" to " ","amp" to "&","pound" to "£","euro" to "€","times" to "×","quot" to "\"","apos" to "'","lt" to "<","gt" to ">")
    return Regex("&(#x[0-9a-fA-F]+|#[0-9]+|[a-z]+);").replace(stripped){m->val key=m.groupValues[1];named[key] ?: runCatching {val n=if(key.startsWith("#x"))key.substring(2).toInt(16)else key.substring(1).toInt();String(Character.toChars(n))}.getOrDefault(m.value)}.replace('\u00a0',' ').replace(Regex("\\s+")," ").trim()
}
internal fun sectionKey(s:String):String {
    val upper=s.uppercase(Locale.ROOT).replace('×','X').replace(',','.').replace("MM","")
    val family=Regex("^(RHS|SHS|CHS|IPE|IPN|HEA|HEB|HEM|RSA|UPN|UPE|PLATE|PLATES|T)\\s*").find(upper)?.groupValues?.get(1) ?: return upper.replace(" ","")
    val nums=Regex("\\d+(?:\\.\\d+)?").findAll(upper.substring(family.length)).map{it.value.toDouble().toString().removeSuffix(".0")}.toList()
    return family+":"+nums.joinToString("x")
}
internal fun quoteSection(name:String,hint:String=""):String? {
    val n=name.uppercase(Locale.ROOT).replace('×','X').replace(',','.')
    Regex("\\b(IPE|IPN|HEA|HEB|HEM|UPN|UPE)\\s*(\\d{2,4})\\b").find(n)?.let{return it.groupValues[1]+" "+it.groupValues[2]}
    val dims=Regex("(\\d+(?:\\.\\d+)?)\\s*(?:MM)?\\s*X\\s*(\\d+(?:\\.\\d+)?)(?:\\s*(?:MM)?\\s*X\\s*(\\d+(?:\\.\\d+)?))?").find(n) ?: return null
    val a=dims.groupValues[1];val b=dims.groupValues[2];val c=dims.groupValues[3]
    val family=when{hint=="RSA"->"RSA";hint=="CHS"||n.contains("ROUND TUBE")->"CHS";hint=="RHS"||n.contains("BOX SECTION")||n.contains("RECTANGULAR TUBE")||n.contains("SQUARE TUBE")->if(a.toDouble()==b.toDouble())"SHS" else "RHS";else->return null}
    if(family=="CHS"&&c.isNotEmpty()||family!="CHS"&&c.isEmpty())return null
    return "$family $a"+"x$b"+(if(c.isNotEmpty())"x$c"else "")
}
internal fun quoteGrade(name:String):String = Regex("(?i)\\b(S(?:235|275|355)[A-Z0-9]*|1\\.(?:4301|4307|4401|4404)|304L?|316L?)\\b").find(name)?.value?.uppercase(Locale.ROOT) ?: "Unspecified"
internal fun parsePricePage(supplier:PriceSupplier,url:String,html:String,checked:Long):List<PriceQuote>{
    val result=mutableListOf<PriceQuote>();val text=priceText(html)
    fun add(name:String,amount:Double,grade:String,finish:String,tax:String,link:String=url,hint:String=""){
        val section=quoteSection(name,hint) ?: return
        val address=runCatching{URI(link)}.getOrNull() ?: return
        if(address.scheme!="https"||address.host.orEmpty().removePrefix("www.")!=URI(supplier.url).host.orEmpty().removePrefix("www."))return
        if(amount.isFinite()&&amount>0&&amount<1e7)result+=PriceQuote(supplier.name,supplier.country,section,grade,finish,amount,if(supplier.country=="UK")"GBP"else"EUR","m",tax,link,checked)
    }
    if(supplier.name=="Kovas"&&text.contains("metrą",true)){
        val hint=when{url.contains("profilinis")->"RHS";url.contains("apvalus-juodieji")->"CHS";url.contains("kampuotis")->"RSA";else->""}
        if(hint.isNotEmpty())for(row in Regex("<tr\\b[^>]*>(.*?)</tr>",setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL)).findAll(html)){
            val cells=Regex("<t[dh]\\b[^>]*>(.*?)</t[dh]>",setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL)).findAll(row.value).map{priceText(it.groupValues[1])}.toList()
            if(cells.size==2&&cells[1].contains("EUR",true)){val amount=cells[1].replace("EUR","",true).trim().replace(',','.').toDoubleOrNull() ?: continue
                add(cells[0],amount,"Unspecified","Carbon steel; grade not published",if(text.contains("su įskaičiuotu PVM",true))"VAT included"else"VAT basis not published",hint=hint)}
        }
    }
    if(supplier.name=="metals4U"){
        // Product-card boundaries stop a price from leaking into the next item's title.
        for(card in Regex("<li\\b[^>]*class=[\"'][^\"']*product-item[^\"']*[\"'][^>]*>(.*?)</li>",setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL)).findAll(html)){
            val a=Regex("<a\\b[^>]*class=[\"'][^\"']*product-item-link[^\"']*[\"'][^>]*>(.*?)</a>",setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL)).find(card.value) ?: continue
            val name=priceText(a.groupValues[1]);val cardText=priceText(card.value)
            val p=Regex("£\\s*([0-9,]+\\.[0-9]{2})\\s*p/m\\s*\\(ex VAT\\)",RegexOption.IGNORE_CASE).find(cardText) ?: continue
            val link=Regex("href=[\"']([^\"']+)[\"']").find(a.value)?.groupValues?.get(1)?.let{URI(url).resolve(it).toString()} ?: url
            add(name,p.groupValues[1].replace(",","").toDouble(),quoteGrade(name),name.substringAfter("Section","").trim(),"ex VAT",link)
        }
    }
    // Other supplier schemas are accepted only when Product + explicit unit-price specification agree.
    fun walk(node:Any?){when(node){is JSONArray->for(i in 0 until node.length())walk(node.opt(i));is JSONObject->{
        if(node.optString("@type")=="Product"){
            val name=node.optString("name");val offers=node.opt("offers");val list=if(offers is JSONArray)List(offers.length()){offers.optJSONObject(it)}else listOf(offers as? JSONObject)
            for(offer in list.filterNotNull()){val spec=offer.optJSONObject("priceSpecification") ?: continue
                if(spec.optString("unitCode")!="MTR"||spec.optString("priceCurrency",offer.optString("priceCurrency"))!=(if(supplier.country=="UK")"GBP"else"EUR"))continue
                if(!spec.has("valueAddedTaxIncluded"))continue
                add(name,spec.optDouble("price"),quoteGrade(name),node.optString("description").take(160),if(spec.optBoolean("valueAddedTaxIncluded"))"VAT included"else"ex VAT",node.optString("url",url))
            }
        };node.keys().forEach{walk(node.opt(it))}
    }}}
    for(script in Regex("<script\\b[^>]*type=[\"']application/ld\\+json[\"'][^>]*>(.*?)</script>",setOf(RegexOption.IGNORE_CASE,RegexOption.DOT_MATCHES_ALL)).findAll(html))runCatching{walk(org.json.JSONTokener(script.groupValues[1]).nextValue())}
    return result.distinctBy{listOf(it.section,it.grade,it.finish,it.url)}
}

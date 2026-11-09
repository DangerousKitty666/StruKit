package com.structkit.app
fun main(){
 val now=1800000000000L
 fun q(s:String,n:Double,tax:String="ex VAT",country:String="UK",grade:String="S235")=PriceQuote(s,country,"RHS 80x40x3",grade,"",n,if(country=="UK")"GBP"else"EUR","m",tax,"https://example.com",now)
 val rows=listOf(q("A",12.0,"incl VAT"),q("A",24.0,"incl VAT"),q("B",30.0))
 check(kotlin.math.abs(marketEstimate(rows,"RHS 80×40×3","S235","UK",5.37,"m",now)!!.amount-22.5)<1e-8)
 check(marketEstimate(rows,"RHS 80x40x3","SS 304","UK",5.37,"m",now)==null)
 check(kotlin.math.abs(marketEstimate(emptyList(),"RHS 80x40x3","S235","UK",5.37,"m",now)!!.amount-8.3235)<1e-8)
 check(kotlin.math.abs(marketEstimate(listOf(q("A",12.1,"incl VAT","LT")),"RHS 80x40x3","S235","LT",5.37,"m",now)!!.amount-10)<1e-8)
 val words=listOf(PdfWord("Air",0f,0f,.1f,.1f),PdfWord("flow",.2f,0f,.3f,.1f),PdfWord("AIR",.4f,0f,.5f,.1f))
 check(pdfWordMatches(words,"air").size==2);check(pdfWordMatches(words,"air flow").size==2);check(pdfWordMatches(words,"").isEmpty())
 check(searchRanges("chair repair air AIR air-flow","air").size==3)
 check(searchRanges("R + C (test)","R + C").size==1)
 check(sectionKey("SHS 30x30x2")==sectionKey("SHS 30x2"))
 check(sectionKey("RHS 40x80x3")==sectionKey("RHS 80x40x3"))
 check(quoteSection("38.1mm OD x 1.5mm Bright Polished Stainless Steel Tube 316")=="CHS 38.1x1.5")
 val sq=q("A",19.19,grade="304").copy(section="SHS 30x30x2")
 check(marketEstimate(listOf(sq),"SHS 30x2","SS 304","UK",1.0,"m",now)!!.amount==19.19)
 check(marketEstimate(listOf(sq),"SHS 30x3","SS 304","UK",1.0,"m",now)==null)
 val lt=PriceSupplier("Linoksa","LT","https://linoksa.com/lt/")
 val parsed=parsePricePage(lt,"https://linoksa.com/lt/82-profiliniai-vamzdziai","<p>Kaina be PVM.</p><div>100*80*3,0 mm N/p vamzdis 304 MF 228,79 € / vnt. 38,13 € / m</div><div>25*25*1,5 mm N/p vamzdis 304 Grid 24,70 € / vnt. 4,12 € / m</div>",now)
 check(parsed.size==2);check(parsed[0].amount==38.13);check(parsed[1].amount==4.12)
 println("Market price and PDF word-span checks passed.")
}

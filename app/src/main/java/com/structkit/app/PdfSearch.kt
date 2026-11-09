package com.structkit.app
internal data class PdfWord(val text:String,val left:Float,val top:Float,val right:Float,val bottom:Float)
internal fun pdfWordMatches(words:List<PdfWord>,query:String):List<PdfWord>{
    val q=query.trim().lowercase();if(q.isEmpty())return emptyList()
    val text=words.joinToString(" "){it.text.lowercase()};val spans=mutableListOf<IntRange>();var from=0
    while(from<text.length){val i=text.indexOf(q,from);if(i<0)break;spans+=i until i+q.length;from=i+q.length}
    var pos=0;return words.filter{w->val start=pos;val end=pos+w.text.length;pos=end+1;spans.any{it.first<end&&it.last>=start}}
}

package com.structkit.app
internal data class PdfWord(val text:String,val left:Float,val top:Float,val right:Float,val bottom:Float)

internal fun searchRanges(text:String,query:String):List<IntRange>{
 val q=query.trim();if(q.isEmpty())return emptyList()
 val body=q.split(Regex("\\s+")).joinToString("\\s+"){Regex.escape(it)}
 val pattern=(if(q.first().isLetterOrDigit())"(?<![\\p{L}\\p{N}_])"else "")+body+(if(q.last().isLetterOrDigit())"(?![\\p{L}\\p{N}_])"else "")
 return Regex(pattern,RegexOption.IGNORE_CASE).findAll(text).map{it.range}.toList()
}
internal fun pdfWordMatches(words:List<PdfWord>,query:String):List<PdfWord>{
 val text=words.joinToString(" "){it.text};val spans=searchRanges(text,query);var pos=0
 return words.filter{w->val start=pos;val end=pos+w.text.length;pos=end+1;spans.any{it.first<end&&it.last>=start}}
}

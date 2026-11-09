package com.structkit.app
import org.json.JSONObject
internal data class PriceSupplier(val name:String,val country:String,val url:String)
internal data class PriceQuote(val supplier:String,val country:String,val section:String,val grade:String,val finish:String,val amount:Double,val currency:String,val unit:String,val tax:String,val url:String,val checked:Long) {
    fun json()=JSONObject().put("supplier",supplier).put("country",country).put("section",section).put("grade",grade).put("finish",finish).put("amount",amount).put("currency",currency).put("unit",unit).put("tax",tax).put("url",url).put("checked",checked)
}

package com.structkit.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayInputStream

/** Local HTTPS origin for bundled CAD code. CAD requests stay local even though the updater can use the Internet. */
@Composable internal fun CadViewer(is3d:Boolean,incoming:IncomingCad?=null) {
    var pending by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri=if(result.resultCode==Activity.RESULT_OK)result.data?.data else null
        pending?.onReceiveValue(uri?.let{arrayOf(it)});pending=null
    }
    DisposableEffect(Unit){onDispose{pending?.onReceiveValue(null);pending=null}}
    key(is3d,incoming?.id) {
        AndroidView(modifier=Modifier.fillMaxSize(),factory={context->
            object:WebView(context) {
                private var registered=false
                private val penReceiver=object:android.content.BroadcastReceiver(){override fun onReceive(c:android.content.Context?,i:Intent?){if(i?.hasExtra("penInsert")==true&&!i.getBooleanExtra("penInsert",true))post{evaluateJavascript("window.activatePenMeasure?.()",null)}}}
                override fun onAttachedToWindow(){super.onAttachedToWindow();if(!registered){val filter=android.content.IntentFilter("com.samsung.pen.INSERT");if(android.os.Build.VERSION.SDK_INT>=33)context.registerReceiver(penReceiver,filter,android.content.Context.RECEIVER_EXPORTED)else context.registerReceiver(penReceiver,filter);registered=true}}
                override fun onDetachedFromWindow(){if(registered){context.unregisterReceiver(penReceiver);registered=false};super.onDetachedFromWindow()}
            }.apply {
                layoutParams = android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT)
                setBackgroundColor(0xFF08090B.toInt())
                settings.javaScriptEnabled=true
                settings.domStorageEnabled=false
                settings.allowFileAccess=false
                settings.allowContentAccess=true // Android file-picker result only
                settings.mixedContentMode=WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.javaScriptCanOpenWindowsAutomatically=false
                settings.setSupportMultipleWindows(false)
                webViewClient=object:WebViewClient() {
                    override fun onPageFinished(view:WebView,url:String){
                        if(incoming!=null&&url.startsWith("https://cad.strukit.local/viewer/index.html")){
                            val name=org.json.JSONObject.quote(incoming.name)
                            view.evaluateJavascript("""(()=>{let tries=0;const timer=setInterval(async()=>{if(!window.openViewerFile){if(++tries>200)clearInterval(timer);return}clearInterval(timer);try{const r=await fetch('/viewer/incoming/${incoming.id}');if(!r.ok)throw Error('Cannot read shared file');const b=await r.blob();window.openViewerFile(new File([b],$name));}catch(e){document.getElementById('status').textContent=e.message}},50)})()""",null)
                        }
                    }
                    override fun shouldOverrideUrlLoading(view:WebView,request:WebResourceRequest):Boolean =
                        request.url.scheme!="https" || request.url.host!="cad.strukit.local"
                    override fun shouldInterceptRequest(view:WebView,request:WebResourceRequest):WebResourceResponse? {
                        val u=request.url
                        // Blob and content URLs are handled by WebView. Everything else stays local.
                        if(u.scheme=="blob" || u.scheme=="content" || u.scheme=="data")return null
                        val path=u.path.orEmpty()
                        if(incoming!=null&&u.scheme=="https"&&u.host=="cad.strukit.local"&&path=="/viewer/incoming/${incoming.id}")return try {
                            val bytes=context.contentResolver.openInputStream(incoming.uri)?.use{input->val out=java.io.ByteArrayOutputStream();val buf=ByteArray(65536);var n=input.read(buf);while(n!=-1){if(out.size()+n>50*1024*1024)throw java.io.IOException("File exceeds 50 MB");out.write(buf,0,n);n=input.read(buf)};out.toByteArray()} ?: throw java.io.IOException("File unavailable")
                            WebResourceResponse("application/octet-stream",null,ByteArrayInputStream(bytes))
                        }catch(_:Exception){blocked()}
                        if(u.scheme!="https"||u.host!="cad.strukit.local"||!path.startsWith("/viewer/")||path.split('/').any{it==".."}||path.contains('\\'))return blocked()
                        return try {
                            val mime=when(path.substringAfterLast('.')){"html"->"text/html";"js"->"application/javascript";"css"->"text/css";"wasm"->"application/wasm";"ttf"->"font/ttf";else->"text/plain"}
                            WebResourceResponse(mime,if(mime.startsWith("text/")||mime=="application/javascript")"UTF-8"else null,context.assets.open(path.removePrefix("/")))
                        }catch(_:Exception){blocked()}
                    }
                    private fun blocked()=WebResourceResponse("text/plain","UTF-8",403,"Unavailable",emptyMap(),ByteArrayInputStream("Local viewer resources only".toByteArray()))
                }
                webChromeClient=object:WebChromeClient() {
                    override fun onShowFileChooser(webView:WebView,callback:ValueCallback<Array<Uri>>,params:FileChooserParams):Boolean {
                        pending?.onReceiveValue(null);pending=callback
                        // CAD MIME labels vary between cloud/file providers; validate extension in viewer.
                        try {picker.launch(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="*/*";putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false)})}
                        catch(_:Exception){pending?.onReceiveValue(null);pending=null}
                        return true
                    }
                }
                loadUrl("https://cad.strukit.local/viewer/index.html?mode=${if(is3d)"3d"else"2d"}")
            }
        },onRelease={it.stopLoading();it.loadUrl("about:blank");it.webChromeClient=null;it.destroy()})
    }
}


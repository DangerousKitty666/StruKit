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
@Composable internal fun CadViewer(is3d:Boolean) {
    var pending by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    val picker=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri=if(result.resultCode==Activity.RESULT_OK)result.data?.data else null
        pending?.onReceiveValue(uri?.let{arrayOf(it)});pending=null
    }
    DisposableEffect(Unit){onDispose{pending?.onReceiveValue(null);pending=null}}
    key(is3d) {
        AndroidView(modifier=Modifier.fillMaxSize(),factory={context->
            WebView(context).apply {
                setBackgroundColor(0xFF08090B.toInt())
                settings.javaScriptEnabled=true
                settings.domStorageEnabled=false
                settings.allowFileAccess=false
                settings.allowContentAccess=true // Android file-picker result only
                settings.mixedContentMode=WebSettings.MIXED_CONTENT_NEVER_ALLOW
                settings.javaScriptCanOpenWindowsAutomatically=false
                settings.setSupportMultipleWindows(false)
                webViewClient=object:WebViewClient() {
                    override fun shouldOverrideUrlLoading(view:WebView,request:WebResourceRequest):Boolean =
                        request.url.scheme!="https" || request.url.host!="cad.strukit.local"
                    override fun shouldInterceptRequest(view:WebView,request:WebResourceRequest):WebResourceResponse? {
                        val u=request.url
                        // Blob and content URLs are handled by WebView. Everything else stays local.
                        if(u.scheme=="blob" || u.scheme=="content" || u.scheme=="data")return null
                        val path=u.path.orEmpty()
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


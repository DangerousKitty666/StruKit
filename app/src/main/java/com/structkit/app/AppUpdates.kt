package com.structkit.app

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/** Only HTTPS GitHub release/API endpoints; no account tokens are stored in the app. */
private fun updateConnection(address:String):HttpURLConnection {
    var url=URL(address)
    repeat(6) {
        require(url.protocol=="https" && url.port in listOf(-1,443) && url.userInfo==null && url.host in setOf("api.github.com","github.com","release-assets.githubusercontent.com","objects.githubusercontent.com")) { "Untrusted update address." }
        val c=url.openConnection() as HttpURLConnection
        c.connectTimeout=15000;c.readTimeout=20000;c.instanceFollowRedirects=false
        c.setRequestProperty("User-Agent","StruKit-Android-Updater")
        c.setRequestProperty("Accept",if(url.host=="api.github.com")"application/vnd.github+json" else "application/octet-stream")
        c.useCaches=false
        val status=try { c.responseCode } catch(e:Exception) { c.disconnect();throw e }
        if(status in listOf(301,302,303,307,308)) {
            val next=c.getHeaderField("Location");c.disconnect()
            require(!next.isNullOrBlank()) { "Missing update redirect." };url=URL(url,next)
        } else {
            if(status!=200) {
                c.disconnect()
                throw IOException(when(status) {
                    404 -> "No published release found. The repository must be public and have a stable release."
                    403,429 -> "GitHub is temporarily limiting update checks. Please try again later."
                    else -> "The update server returned HTTP $status. Please try again later."
                })
            }
            return c
        }
    }
    throw IOException("Too many update redirects.")
}

private suspend fun latestAppUpdate():AppUpdate=withContext(Dispatchers.IO) {
    val c=updateConnection("https://api.github.com/repos/$UPDATE_REPOSITORY/releases/latest")
    try {
        val data=c.inputStream.use { input ->
            val output=java.io.ByteArrayOutputStream()
            val buffer=ByteArray(8192)
            while(true) {
                ensureActive();val n=input.read(buffer);if(n<0)break
                require(output.size()+n<=1024*1024) { "Update response is too large." }
                output.write(buffer,0,n)
            }
            output.toByteArray()
        }
        ensureActive();parseAppUpdate(data.toString(Charsets.UTF_8))
    } finally { c.disconnect() }
}

@Suppress("DEPRECATION")
private fun versionCode(info:PackageInfo):Long=if(Build.VERSION.SDK_INT>=28)info.longVersionCode else info.versionCode.toLong()
@Suppress("DEPRECATION")
private fun signatureFlags():Int=if(Build.VERSION.SDK_INT>=28)PackageManager.GET_SIGNING_CERTIFICATES else PackageManager.GET_SIGNATURES
@Suppress("DEPRECATION")
private fun signerHashes(info:PackageInfo):Set<String> {
    val signatures=if(Build.VERSION.SDK_INT>=28)info.signingInfo?.apkContentsSigners else info.signatures
    return signatures.orEmpty().map { signature -> MessageDigest.getInstance("SHA-256").digest(signature.toByteArray()).joinToString(""){"%02x".format(it)} }.toSet()
}
@Suppress("DEPRECATION")
private fun validateUpdate(context:Context,file:File,expectedCode:Long) {
    val pm=context.packageManager
    val installed=pm.getPackageInfo(context.packageName,signatureFlags())
    val candidate=pm.getPackageArchiveInfo(file.absolutePath,signatureFlags()) ?: throw IOException("Downloaded file is not a readable APK.")
    require(candidate.packageName==context.packageName) { "This APK is not StruKit." }
    require(versionCode(candidate)==expectedCode && expectedCode>versionCode(installed)) { "APK version does not match a newer release." }
    require(signerHashes(candidate).isNotEmpty() && signerHashes(candidate)==signerHashes(installed)) { "This update was signed with a different key. Ask for an APK signed with the original StruKit key." }
}

private suspend fun downloadAppUpdate(context:Context,update:AppUpdate,progress:(Float)->Unit):File=withContext(Dispatchers.IO) {
    val folder=File(context.cacheDir,"updates").apply { mkdirs() }
    require(folder.usableSpace>update.size+20L*1024*1024) { "Not enough free space to download this update." }
    val temp=File.createTempFile("download-",".part",folder)
    val ready=File(folder,"update.apk")
    ready.delete()
    val c=updateConnection(update.url)
    try {
        val hash=MessageDigest.getInstance("SHA-256")
        var total=0L;var reported=-1
        c.inputStream.use { input -> temp.outputStream().use { output ->
            val buffer=ByteArray(64*1024)
            while(true) {
                ensureActive();val n=input.read(buffer);if(n<0)break
                total+=n;require(total<=update.size) { "The download size does not match the release." }
                output.write(buffer,0,n);hash.update(buffer,0,n)
                val percent=(total*100/update.size).toInt()
                if(percent!=reported) { reported=percent;withContext(Dispatchers.Main){progress(total.toFloat()/update.size)} }
            }
        } }
        require(total==update.size) { "The download was incomplete. Please retry." }
        val actual=hash.digest().joinToString(""){"%02x".format(it)}
        require(update.digest==null || actual.equals(update.digest,true)) { "Download checksum failed. Please retry." }
        validateUpdate(context,temp,update.code);ensureActive()
        check(temp.renameTo(ready)) { "Could not save the update." };ready
    } finally { c.disconnect();temp.delete() }
}

private fun openUpdateInstaller(context:Context,file:File) {
    val uri=FileProvider.getUriForFile(context,"${context.packageName}.updates",file)
    context.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive")
        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).apply { clipData=ClipData.newRawUri("StruKit update",uri) })
}

@Composable internal fun AppUpdates() {
    val context=LocalContext.current
    val scope=rememberCoroutineScope()
    val installed=remember { context.packageManager.getPackageInfo(context.packageName,0) }
    var update by remember { mutableStateOf<AppUpdate?>(null) }
    var status by remember { mutableStateOf("Checking GitHub…") }
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var downloading by remember { mutableStateOf(false) }
    var job by remember { mutableStateOf<Job?>(null) }
    var readyCode by rememberSaveable { mutableLongStateOf(0L) }
    val ready=File(context.cacheDir,"updates/update.apk")
    fun install() {
        scope.launch {
            busy=true
            try {
                withContext(Dispatchers.IO){validateUpdate(context,ready,readyCode)}
                openUpdateInstaller(context,ready)
                status="Confirm Install in Android. If you cancel, you can tap Install update again."
            } catch(e:CancellationException){throw e} catch(e:Exception){status=e.message ?: "Could not open the installer."}
            finally {busy=false}
        }
    }
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if(context.packageManager.canRequestPackageInstalls() && readyCode>0 && ready.exists()) install()
        else status="Installation permission was not granted. Tap Install update when you are ready."
    }
    fun check() {
        job=scope.launch {
            busy=true;downloading=false;update=null;status="Checking GitHub…"
            try {
                val latest=latestAppUpdate()
                if(latest.code>versionCode(installed)){update=latest;status="${latest.title} is available."}
                else status="You’re up to date with the latest published release."
            } catch(e:CancellationException){throw e} catch(e:Exception){status=e.message ?: "Unable to check. Please check your internet connection."}
            finally {busy=false}
        }
    }
    LaunchedEffect(Unit) { if(readyCode==0L || !ready.exists()) check() else status="Downloaded update is ready to install." }
    Column(Modifier.fillMaxSize().padding(horizontal=16.dp).fadedVerticalScroll(rememberScrollState())) {
        Text("Check for updates",color=updateInk,fontSize=25.sp)
        Text("Installed: ${installed.versionName}",color=updateMuted,fontSize=13.sp)
        Spacer(Modifier.height(18.dp))
        InfoPanel(Modifier.fillMaxWidth()) {
            Text(status,color=updateInk,fontSize=15.sp)
            if(busy) {
                Spacer(Modifier.height(12.dp))
                if(downloading){LinearProgressIndicator(progress={progress},modifier=Modifier.fillMaxWidth());Text("${(progress*100).toInt()}%",color=updateBlue)}
                else LinearProgressIndicator(Modifier.fillMaxWidth())
                if(job?.isActive==true) TextButton(onClick={job?.cancel();status="Cancelled. You can retry."}){Text("Cancel")}
            }
            if(!busy) {
                update?.let { release ->
                    Text("Download: ${release.size/(1024*1024)} MB",color=updateMuted,fontSize=12.sp)
                    if(readyCode!=release.code || !ready.exists()) OutlinedButton(onClick={
                        job=scope.launch {
                            busy=true;downloading=true;progress=0f;readyCode=0
                            status="Downloading update…"
                            try {downloadAppUpdate(context,release){progress=it};readyCode=release.code;status="Download checked. Ready to install."}
                            catch(e:CancellationException){throw e}
                            catch(e:Exception){status=e.message ?: "Download failed. Please retry."}
                            finally {busy=false;downloading=false}
                        }
                    }){Text("Download update")}
                }
                if(readyCode>versionCode(installed) && ready.exists()) OutlinedButton(onClick={
                    if(context.packageManager.canRequestPackageInstalls()) install()
                    else runCatching {permission.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:${context.packageName}")))}
                        .onFailure {status="Open Android Settings and allow StruKit to install unknown apps, then retry."}
                }){Text("Install update")}
                TextButton(onClick={check()}){Text("Check again")}
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Updates download only when you choose. Android will ask you to confirm installation. The first time, allow installations from StruKit in Android Settings.",color=updateMuted,fontSize=13.sp)
        SourceLink("View releases on GitHub","https://github.com/$UPDATE_REPOSITORY/releases")
    }
}

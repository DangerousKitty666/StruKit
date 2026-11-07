package com.structkit.app

import org.json.JSONObject
import java.io.IOException

internal const val UPDATE_REPOSITORY = "DangerousKitty666/StruKit"
internal const val MAX_APK_BYTES = 1024L * 1024 * 1024
private val updateAssetName = Regex("^StruKit-[0-9]+(?:\\.[0-9]+)*-A-vc([1-9][0-9]*)\\.apk$")
internal data class AppUpdate(val title:String,val code:Long,val url:String,val size:Long,val digest:String?)

internal fun parseAppUpdate(json:String):AppUpdate {
    val release=JSONObject(json)
    require(!release.optBoolean("draft") && !release.optBoolean("prerelease")) { "This release is not a stable Android update." }
    val assets=release.getJSONArray("assets")
    val candidates=(0 until assets.length()).mapNotNull { i ->
        val asset=assets.getJSONObject(i)
        val code=updateAssetName.matchEntire(asset.optString("name"))?.groupValues?.get(1)?.toLongOrNull() ?: return@mapNotNull null
        val url=asset.getString("browser_download_url")
        require(url.startsWith("https://github.com/$UPDATE_REPOSITORY/releases/download/")) { "Unexpected update download address." }
        val size=asset.getLong("size")
        require(size in 1..MAX_APK_BYTES) { "The update has an invalid download size." }
        val digest=asset.optString("digest").takeIf { it.startsWith("sha256:") }?.removePrefix("sha256:")
        require(digest==null || digest.matches(Regex("[a-fA-F0-9]{64}"))) { "Invalid update checksum." }
        AppUpdate(release.optString("name").ifBlank { release.optString("tag_name") },code,url,size,digest)
    }
    return candidates.maxByOrNull { it.code } ?: throw IOException("The latest release has no Android update APK yet.")
}


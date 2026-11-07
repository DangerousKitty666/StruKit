package com.structkit.app

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable internal fun ConnectionSolid(mode: Int, reverse: Boolean, modifier: Modifier = Modifier) {
    val bitmap by produceState<Bitmap?>(null,mode,reverse) {
        value=withContext(Dispatchers.Default) {
            Bitmap.createBitmap(renderSolid(connectionScene(mode),720,560,reverse,if(mode==18)4 else 0),720,560,Bitmap.Config.ARGB_8888)
        }
    }
    Box(modifier) { bitmap?.let { Image(it.asImageBitmap(),"3D connection: members in steel grey, plates in blue, highlighted weld locations in gold",Modifier.fillMaxSize()) } }
}


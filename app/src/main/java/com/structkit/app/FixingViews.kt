package com.structkit.app

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

@Composable internal fun FixingSolid(type: String, modifier: Modifier = Modifier) {
    val bitmap by produceState<Bitmap?>(null,type) { value=withContext(Dispatchers.Default){fixingBitmap(type)} }
    Box(modifier) { bitmap?.let { Image(it.asImageBitmap(),"Shaded fixing views with dimension letters",Modifier.fillMaxSize()) } }
}



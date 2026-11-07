package com.structkit.app
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
internal fun Modifier.edgeFade(horizontal: Boolean = false, start: () -> Boolean = {true}, end: () -> Boolean = {true}): Modifier = this.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }.drawWithContent {
    drawContent()
    val extent=if(horizontal) size.width else size.height
    if(extent > 0f) {
        val fraction=(12.dp.toPx()/extent).coerceAtMost(.2f)
        val stops=arrayOf(0f to if(start()) Color.Transparent else Color.Black, fraction to Color.Black, (1f-fraction) to Color.Black, 1f to if(end()) Color.Transparent else Color.Black)
        drawRect(if(horizontal) Brush.horizontalGradient(*stops) else Brush.verticalGradient(*stops), blendMode = BlendMode.DstIn)
    }
}
internal fun Modifier.fadedVerticalScroll(state: ScrollState): Modifier = edgeFade(start={state.canScrollBackward},end={state.canScrollForward}).verticalScroll(state)
internal fun Modifier.fadedHorizontalScroll(state: ScrollState): Modifier = edgeFade(horizontal=true,start={state.canScrollBackward},end={state.canScrollForward}).horizontalScroll(state)

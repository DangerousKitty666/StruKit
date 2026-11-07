package com.structkit.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*

@Composable internal fun ToolDropdown(expanded:Boolean, onExpanded:(Boolean)->Unit, content:@Composable ColumnScope.()->Unit) {
    var bottom by remember { mutableFloatStateOf(0f) }
    val density=LocalDensity.current
    val height=(LocalConfiguration.current.screenHeightDp.dp-with(density){bottom.toDp()}-36.dp).coerceAtLeast(80.dp)
    val gap=with(density){20.dp.roundToPx()}
    val position=remember(gap){object:PopupPositionProvider {
        override fun calculatePosition(anchorBounds:IntRect,windowSize:IntSize,layoutDirection:LayoutDirection,popupContentSize:IntSize)=
            IntOffset((anchorBounds.right-popupContentSize.width).coerceIn(0,(windowSize.width-popupContentSize.width).coerceAtLeast(0)),
                (anchorBounds.bottom+gap).coerceIn(0,(windowSize.height-popupContentSize.height).coerceAtLeast(0)))
    }}
    Box(Modifier.onGloballyPositioned{bottom=it.boundsInWindow().bottom}) {
        OutlinedButton(onClick={onExpanded(true)},border=BorderStroke(1.dp,updateBlue),shape=RoundedCornerShape(9.dp)) { Text("Select ▾",color=updateBlue) }
        if(expanded) Popup(popupPositionProvider=position,onDismissRequest={onExpanded(false)},properties=PopupProperties(focusable=true)) {
            Surface(color=Color.Black.copy(alpha=.90f),shape=RoundedCornerShape(9.dp),border=BorderStroke(1.dp,updateBlue)) {
                Column(Modifier.width(300.dp).heightIn(max=height).fadedVerticalScroll(rememberScrollState()),content=content)
            }
        }
    }
}

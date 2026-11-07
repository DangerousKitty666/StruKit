package com.structkit.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val LocalPropertyPopup = compositionLocalOf { false }

@Composable internal fun ProfilePropertiesPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(if(LocalPropertyPopup.current) Color.Transparent else Color(0xFF17212B))
        .padding(horizontal=if(LocalPropertyPopup.current) 0.dp else 13.dp, vertical=10.dp), content=content)
}

/** Uses the same dimensions heading, 43 dp header and 44 dp rows as the steel tables. */
@Composable internal fun <T> ProfileDataTable(
    columns: List<Pair<String,String>>, rows: List<T>, selected: T?, onSelect: (T)->Unit,
    cells: (T)->List<String>, modifier: Modifier=Modifier, details: @Composable (T)->Unit
) {
    val scroll=rememberLazyListState()
    LaunchedEffect(rows) { scroll.scrollToItem(0) }
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text("DIMENSIONS",color=updateInk,fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=1.sp)
            Spacer(Modifier.weight(1f))
            Text("Tap row to expand",color=updateMuted,fontSize=10.sp)
        }
        Spacer(Modifier.height(7.dp))
        Row(Modifier.fillMaxWidth().height(43.dp),verticalAlignment=Alignment.CenterVertically) {
            columns.forEachIndexed { i, (label,unit) ->
                Column(Modifier.weight(if(i==0) 2.2f else 1f).padding(start=5.dp)) {
                    Text(label,color=updateInk,fontSize=13.sp,fontWeight=FontWeight.SemiBold)
                    Text(unit,color=updateMuted,fontSize=10.sp)
                }
            }
        }
        HorizontalDivider(color=Color(0xFF343A43))
        LazyColumn(Modifier.fillMaxWidth().weight(1f).edgeFade(start={scroll.canScrollBackward},end={scroll.canScrollForward}),state=scroll) {
            items(rows) { item ->
                Row(Modifier.fillMaxWidth().height(44.dp).background(if(item==selected)Color(0xFF1F3140) else Color.Transparent)
                    .clickable{onSelect(item)},verticalAlignment=Alignment.CenterVertically) {
                    cells(item).forEachIndexed { i,value ->
                        Text(value,color=updateInk,fontSize=13.sp,modifier=Modifier.weight(if(i==0)2.2f else 1f).padding(start=5.dp,end=2.dp))
                    }
                }
                if(item==selected) details(item)
                HorizontalDivider(thickness=.5.dp,color=Color(0xFF343A43))
            }
        }
    }
}

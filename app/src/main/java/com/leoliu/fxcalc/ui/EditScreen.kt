package com.leoliu.fxcalc.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leoliu.fxcalc.data.Currencies
import com.leoliu.fxcalc.ui.theme.Fx
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun EditCurrenciesScreen(
    currencies: List<String>,
    onBack: () -> Unit,
    onMove: (List<String>) -> Unit,
    onRemove: (String) -> Unit,
    onAdd: () -> Unit,
    onPick: (String) -> Unit,
) {
    val view = LocalView.current
    // Local copy so dragging stays smooth; changes are pushed to the ViewModel immediately.
    var list by remember { mutableStateOf(currencies) }
    LaunchedEffect(currencies) { if (list != currencies) list = currencies }

    val lazyListState = rememberLazyListState()
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        list = list.toMutableList().apply { add(to.index, removeAt(from.index)) }
        onMove(list)
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Fx.Bg)
            .systemBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回", tint = Fx.TextPrimary)
            }
            Text("管理貨幣", color = Fx.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            "按住右側把手拖曳排序・點國旗更換貨幣",
            color = Fx.TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 12.dp, bottom = 10.dp),
        )

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            items(list, key = { it }) { code ->
                ReorderableItem(reorderState, key = code) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 10.dp else 0.dp, label = "elev")
                    val info = Currencies.get(code)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isDragging) Fx.SurfaceHigh else Fx.Surface,
                        shadowElevation = elevation,
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(66.dp)
                                .padding(start = 6.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                Modifier
                                    .weight(1f)
                                    .clickable { onPick(code) }
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Flag(info, 38.dp)
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(code, color = Fx.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                    Text(info.name, color = Fx.TextSecondary, fontSize = 12.sp)
                                }
                            }
                            IconButton(
                                onClick = { onRemove(code) },
                                enabled = list.size > 1,
                            ) {
                                Icon(
                                    Icons.Rounded.DeleteOutline,
                                    contentDescription = "移除",
                                    tint = if (list.size > 1) Fx.Bad.copy(alpha = 0.85f) else Color.Gray,
                                )
                            }
                            IconButton(
                                onClick = {},
                                modifier = Modifier.draggableHandle(
                                    onDragStarted = { view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS) },
                                ),
                            ) {
                                Icon(Icons.Rounded.DragHandle, contentDescription = "拖曳排序", tint = Fx.TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onAdd,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Fx.Accent, contentColor = Color.Black),
        ) {
            Icon(Icons.Rounded.Add, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("新增貨幣", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

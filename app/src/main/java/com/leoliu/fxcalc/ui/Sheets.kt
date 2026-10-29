package com.leoliu.fxcalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leoliu.fxcalc.calc.Calculator
import com.leoliu.fxcalc.data.Currencies
import com.leoliu.fxcalc.data.HistoryEntry
import com.leoliu.fxcalc.ui.theme.Fx
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    history: List<HistoryEntry>,
    onDismiss: () -> Unit,
    onSelect: (HistoryEntry) -> Unit,
    onClear: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Fx.Surface,
    ) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("近期計算紀錄", color = Fx.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (history.isNotEmpty()) {
                    TextButton(onClick = onClear) { Text("清除", color = Fx.AccentLight) }
                }
            }
            Text("保留最近 20 筆，點一下即可帶回計算", color = Fx.TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(14.dp))
            if (history.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(140.dp), contentAlignment = Alignment.Center) {
                    Text("尚無紀錄\n按下「=」後會儲存在這裡", color = Fx.TextSecondary, fontSize = 14.sp, lineHeight = 22.sp)
                }
            } else {
                val fmt = remember { SimpleDateFormat("M/d HH:mm", Locale.getDefault()) }
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.75f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(history, key = { it.time }) { h ->
                        val info = Currencies.get(h.currency)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(Fx.SurfaceHigh)
                                .clickable { onSelect(h) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Flag(info, 34.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${h.currency}  ${Calculator.display(h.expression)}",
                                    color = Fx.TextSecondary,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    "= " + (h.result.toBigDecimalOrNull()?.let { Calculator.format(it) } ?: h.result),
                                    color = Fx.TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Text(fmt.format(Date(h.time)), color = Fx.TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerSheet(
    title: String,
    current: String?,
    existing: List<String>,
    hideExisting: Boolean,
    onDismiss: () -> Unit,
    onPicked: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, existing, hideExisting) {
        val q = query.trim()
        Currencies.all.filter { c ->
            (!hideExisting || c.code !in existing) &&
                (q.isEmpty() || c.code.contains(q, ignoreCase = true) || c.name.contains(q))
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Fx.Surface,
    ) {
        Column(
            Modifier
                .fillMaxHeight(0.88f)
                .imePadding()
                .padding(horizontal = 20.dp)
        ) {
            Text(title, color = Fx.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("搜尋代碼或名稱，例如 JPY、日圓") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, contentDescription = "清除") }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Fx.Accent,
                    unfocusedBorderColor = Fx.Outline,
                    cursorColor = Fx.Accent,
                ),
            )
            Spacer(Modifier.height(8.dp))
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                items(filtered, key = { it.code }) { c ->
                    val selected = c.code == current
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onPicked(c.code) }
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Flag(c, 38.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.code, color = Fx.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Text(c.name, color = Fx.TextSecondary, fontSize = 13.sp)
                        }
                        when {
                            selected -> Icon(Icons.Rounded.Check, contentDescription = "目前", tint = Fx.Accent)
                            c.code in existing -> Text("已在列表・互換", color = Fx.TextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

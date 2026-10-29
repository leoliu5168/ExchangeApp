package com.leoliu.fxcalc.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leoliu.fxcalc.calc.Calculator
import com.leoliu.fxcalc.data.Currencies
import com.leoliu.fxcalc.ui.theme.Fx
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(vm: MainViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()

    var showHistory by rememberSaveable { mutableStateOf(false) }
    var showEdit by rememberSaveable { mutableStateOf(false) }
    // Currency code of the row whose flag was tapped (picker in "replace" mode).
    var pickFor by rememberSaveable { mutableStateOf<String?>(null) }
    var pickToAdd by rememberSaveable { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Fx.Bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            TopBar(
                state = state,
                onRefresh = vm::refresh,
                onHistory = { showHistory = true },
                onEdit = { showEdit = true },
            )

            val value = state.activeValue
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(state.currencies, key = { it }) { code ->
                    val isActive = code == state.active
                    val amount: String
                    var preview: String? = null
                    if (isActive) {
                        amount = Calculator.display(state.expression)
                        if (Calculator.hasOperation(state.expression)) {
                            preview = value?.let { "= " + Calculator.format(it) } ?: "無法計算"
                        }
                    } else {
                        amount = value?.let { state.convert(it, state.active, code) }
                            ?.let { Calculator.format(it) } ?: "—"
                    }
                    CurrencyRow(
                        code = code,
                        amount = amount,
                        preview = preview,
                        isActive = isActive,
                        onSelect = { vm.selectRow(code) },
                        onPick = { pickFor = code },
                        modifier = Modifier.animateItem(),
                    )
                }
            }

            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val keypadHeight = min(maxWidth / 1.1f, 440.dp)
                Keypad(
                    onKey = vm::onKey,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(keypadHeight)
                        .padding(top = 6.dp, bottom = 10.dp),
                )
            }
        }

        AnimatedVisibility(
            visible = showEdit,
            enter = slideInHorizontally { it / 3 } + fadeIn(tween(180)),
            exit = slideOutHorizontally { it / 3 } + fadeOut(tween(150)),
        ) {
            EditCurrenciesScreen(
                currencies = state.currencies,
                onBack = { showEdit = false },
                onMove = vm::setOrder,
                onRemove = vm::removeCurrency,
                onAdd = { pickToAdd = true },
                onPick = { pickFor = it },
            )
        }
        BackHandler(enabled = showEdit) { showEdit = false }
    }

    if (showHistory) {
        HistorySheet(
            history = state.history,
            onDismiss = { showHistory = false },
            onSelect = {
                vm.restoreHistory(it)
                showHistory = false
            },
            onClear = vm::clearHistory,
        )
    }

    pickFor?.let { current ->
        CurrencyPickerSheet(
            title = "選擇貨幣",
            current = current,
            existing = state.currencies,
            hideExisting = false,
            onDismiss = { pickFor = null },
            onPicked = {
                vm.changeCurrency(current, it)
                pickFor = null
            },
        )
    }
    if (pickToAdd) {
        CurrencyPickerSheet(
            title = "新增貨幣",
            current = null,
            existing = state.currencies,
            hideExisting = true,
            onDismiss = { pickToAdd = false },
            onPicked = {
                vm.addCurrency(it)
                pickToAdd = false
            },
        )
    }
}

@Composable
private fun TopBar(
    state: UiState,
    onRefresh: () -> Unit,
    onHistory: () -> Unit,
    onEdit: () -> Unit,
) {
    // Ticks so "N 分鐘前" stays current.
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(15_000)
            value = System.currentTimeMillis()
        }
    }
    val rates = state.rates
    val (dotColor, status) = when {
        rates == null && state.loading -> Fx.Warn to "取得匯率中…"
        rates == null -> Fx.Bad to "無法取得匯率，請檢查網路"
        else -> {
            val ago = agoText(now, rates.fetchedAt)
            when {
                state.error -> Fx.Bad to "離線 · 使用 $ago 的匯率"
                now - rates.fetchedAt > MainViewModel.REFRESH_INTERVAL_MS * 2 -> Fx.Warn to "更新於 $ago"
                else -> Fx.Good to "更新於 $ago"
            }
        }
    }
    val animatedDot by animateColorAsState(dotColor, label = "dot")

    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("匯率轉換", color = Fx.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(animatedDot)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    status,
                    color = Fx.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        RefreshButton(loading = state.loading, onClick = onRefresh)
        IconButton(onClick = onHistory) {
            Icon(Icons.Rounded.History, contentDescription = "計算紀錄", tint = Fx.AccentLight)
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Rounded.Tune, contentDescription = "管理貨幣", tint = Fx.AccentLight)
        }
    }
}

@Composable
private fun RefreshButton(loading: Boolean, onClick: () -> Unit) {
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(loading) {
        if (loading) {
            while (true) {
                rotation.animateTo(rotation.value + 360f, tween(700, easing = LinearEasing))
            }
        } else {
            // Finish the current turn smoothly instead of snapping.
            val target = kotlin.math.ceil(rotation.value / 360f) * 360f
            rotation.animateTo(target, tween(250))
            rotation.snapTo(0f)
        }
    }
    IconButton(onClick = onClick) {
        Icon(
            Icons.Rounded.Refresh,
            contentDescription = "更新匯率",
            tint = Fx.AccentLight,
            modifier = Modifier.rotate(rotation.value),
        )
    }
}

private fun agoText(now: Long, then: Long): String {
    val min = (now - then) / 60_000
    return when {
        min < 1 -> "剛剛"
        min < 60 -> "$min 分鐘前"
        else -> SimpleDateFormat("M/d HH:mm", Locale.getDefault()).format(Date(then))
    }
}

@Composable
private fun CurrencyRow(
    code: String,
    amount: String,
    preview: String?,
    isActive: Boolean,
    onSelect: () -> Unit,
    onPick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val info = remember(code) { Currencies.get(code) }
    val bg by animateColorAsState(if (isActive) Fx.Accent.copy(alpha = 0.16f) else Fx.Surface, tween(160), label = "bg")
    val border by animateColorAsState(if (isActive) Fx.Accent.copy(alpha = 0.7f) else Color.Transparent, tween(160), label = "border")
    val fg by animateColorAsState(if (isActive) Fx.AccentLight else Fx.TextPrimary, tween(160), label = "fg")
    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier
            .fillMaxWidth()
            .height(66.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            Modifier
                .width(126.dp)
                .fillMaxHeight()
                .clip(shape)
                .background(Fx.Surface)
                .clickable(onClick = onPick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Flag(info, 36.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(code, color = Fx.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    info.name,
                    color = Fx.TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(shape)
                .background(bg)
                .border(1.5.dp, border, shape)
                .clickable(onClick = onSelect)
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Column(horizontalAlignment = Alignment.End) {
                if (preview != null) {
                    Text(preview, color = Fx.TextSecondary, fontSize = 13.sp, maxLines = 1)
                }
                val size = when {
                    amount.length <= 9 -> 28.sp
                    amount.length <= 12 -> 24.sp
                    amount.length <= 16 -> 20.sp
                    else -> 16.sp
                }
                Text(
                    amount,
                    color = fg,
                    fontSize = size,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

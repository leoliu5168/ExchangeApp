package com.leoliu.fxcalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leoliu.fxcalc.calc.Calculator
import com.leoliu.fxcalc.data.CurrencyInfo
import com.leoliu.fxcalc.data.Discounts
import com.leoliu.fxcalc.data.TaxRule
import com.leoliu.fxcalc.data.TaxRules
import com.leoliu.fxcalc.ui.theme.Fx
import java.math.BigDecimal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    euroCountry: String,
    customDiscounts: List<BigDecimal>,
    onBack: () -> Unit,
    onEuroCountry: (String) -> Unit,
    onAddDiscount: (String) -> Boolean,
    onRemoveDiscount: (BigDecimal) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf(false) }
    fun submit() {
        if (input.isBlank()) return
        if (onAddDiscount(input)) {
            input = ""; inputError = false
        } else {
            inputError = true
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Fx.Bg)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回", tint = Fx.TextPrimary)
            }
            Text("設定", color = Fx.TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item { SectionTitle("自訂折扣", "新增後會出現在鍵盤上方的快捷列") }
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Fx.Surface)
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it.filter { c -> c.isDigit() || c == '.' }.take(5); inputError = false },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = inputError,
                            placeholder = { Text("例如 75 = 75折、7 = 7折") },
                            suffix = { Text("折") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { submit() }),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Fx.Accent,
                                unfocusedBorderColor = Fx.Outline,
                                cursorColor = Fx.Accent,
                            ),
                        )
                        Spacer(Modifier.width(10.dp))
                        Button(
                            onClick = { submit() },
                            modifier = Modifier.height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Fx.Accent, contentColor = Color.Black),
                        ) { Text("新增", fontWeight = FontWeight.SemiBold) }
                    }
                    if (inputError) {
                        Text("請輸入 1–99 之間的折數", color = Fx.Bad, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                    if (customDiscounts.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            customDiscounts.forEach { f ->
                                Row(
                                    Modifier
                                        .height(34.dp)
                                        .clip(RoundedCornerShape(17.dp))
                                        .background(Fx.SurfaceHigh)
                                        .padding(start = 14.dp, end = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(Discounts.label(f), color = Fx.TextPrimary, fontSize = 14.sp)
                                    IconButton(onClick = { onRemoveDiscount(f) }, modifier = Modifier.size(30.dp)) {
                                        Icon(Icons.Rounded.Close, contentDescription = "移除", tint = Fx.TextSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                SectionTitle("歐元區退稅國家", "輸入歐元（EUR）時，「免稅額」「退稅額」使用的國家")
            }
            items(TaxRules.euroRules, key = { "eu" + it.id }) { r ->
                TaxOption(
                    selected = euroCountry == r.id,
                    title = r.country,
                    subtitle = ruleSummary(r),
                    flag = CurrencyInfo(r.currency, r.country, r.region),
                    onClick = { onEuroCountry(r.id) },
                )
            }
            item {
                Spacer(Modifier.height(10.dp))
                SectionTitle("各國免稅規則", "依「目前輸入的貨幣」自動套用；不在列表中的貨幣會提示沒有免稅額")
            }
            items(TaxRules.all.filter { it.currency != "EUR" }, key = { it.id }) { r ->
                TaxOption(
                    selected = false,
                    title = "${r.country}（${r.currency}）",
                    subtitle = ruleSummary(r),
                    flag = CurrencyInfo(r.currency, r.country, r.region),
                    onClick = null,
                )
            }
            item {
                Text(
                    "退稅額 = 含稅金額 × 稅額比例，為參考估算。實際退款會扣除退稅公司手續費，" +
                        "門檻與稅率可能調整，請以當地最新規定為準。",
                    color = Fx.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp)) {
        Text(title, color = Fx.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text(subtitle, color = Fx.TextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun TaxOption(
    selected: Boolean,
    title: String,
    subtitle: String,
    flag: CurrencyInfo?,
    onClick: (() -> Unit)?,
    icon: ImageVector = Icons.Rounded.AutoAwesome,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) Fx.Accent.copy(alpha = 0.14f) else Fx.Surface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (flag != null) {
            Flag(flag, 34.dp)
        } else {
            Icon(icon, contentDescription = null, tint = Fx.AccentLight, modifier = Modifier.size(width = 34.dp, height = 26.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = Fx.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(subtitle, color = Fx.TextSecondary, fontSize = 12.sp)
        }
        if (onClick != null) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = Fx.Accent),
            )
        }
    }
}

private fun ruleSummary(r: TaxRule): String {
    val min = if (r.min.signum() == 0) "無門檻" else "門檻 ${Calculator.format(r.min)} ${r.currency}"
    return "$min・${r.taxLabel}"
}

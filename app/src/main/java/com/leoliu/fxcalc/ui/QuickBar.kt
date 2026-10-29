package com.leoliu.fxcalc.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leoliu.fxcalc.data.CurrencyInfo
import com.leoliu.fxcalc.data.Discounts
import com.leoliu.fxcalc.data.TaxRule
import com.leoliu.fxcalc.ui.theme.Fx
import java.math.BigDecimal

/** Horizontal row of shortcuts: tax-free minimum, tax refund, discounts. */
@Composable
fun QuickBar(
    rule: TaxRule,
    customDiscounts: List<BigDecimal>,
    onTaxFree: () -> Unit,
    onRefund: () -> Unit,
    onDiscount: (BigDecimal) -> Unit,
    modifier: Modifier = Modifier,
) {
    val discounts = Discounts.builtIn + customDiscounts.filter { c -> Discounts.builtIn.none { it.compareTo(c) == 0 } }
    val flagInfo = CurrencyInfo(rule.currency, rule.country, rule.region)
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 2.dp),
    ) {
        item(key = "taxfree") { Chip(flagInfo, "免稅額", onTaxFree) }
        item(key = "refund") { Chip(flagInfo, "退稅額", onRefund) }
        items(discounts, key = { "d" + it.toPlainString() }) { f ->
            Chip(null, Discounts.label(f)) { onDiscount(f) }
        }
    }
}

@Composable
private fun Chip(flag: CurrencyInfo?, label: String, onClick: () -> Unit) {
    val view = LocalView.current
    Row(
        Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Fx.Surface)
            .clickable {
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                onClick()
            }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (flag != null) {
            Flag(flag, 20.dp)
            Spacer(Modifier.width(6.dp))
        }
        Text(label, color = Fx.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

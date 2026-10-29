package com.leoliu.fxcalc.data

import java.math.BigDecimal

/**
 * Tourist tax-free / VAT refund reference data.
 * [min] is the minimum purchase in [currency]; the refund is price × [num] / [den]
 * (the tax portion of a tax-included price). Values are approximate: real refunds
 * are usually lower after operator fees.
 */
data class TaxRule(
    val id: String,
    val region: String,
    val currency: String,
    val country: String,
    val min: BigDecimal,
    val num: Int,
    val den: Int,
    val taxLabel: String,
    val minNote: String,
)

object TaxRules {
    val all = listOf(
        TaxRule("JP", "JP", "JPY", "日本", BigDecimal("5000"), 10, 110, "消費稅 10%", "同一店家同日購物滿 ¥5,000（未稅）"),
        TaxRule("KR", "KR", "KRW", "韓國", BigDecimal("15000"), 10, 110, "VAT 10%", "單筆購物滿 ₩15,000"),
        TaxRule("TH", "TH", "THB", "泰國", BigDecimal("2000"), 7, 107, "VAT 7%", "同店同日滿 ฿2,000（總額需滿 ฿5,000）"),
        TaxRule("SG", "SG", "SGD", "新加坡", BigDecimal("100"), 9, 109, "GST 9%", "同店滿 S$100"),
        TaxRule("AU", "AU", "AUD", "澳洲", BigDecimal("300"), 10, 110, "GST 10%", "同一商家滿 A$300"),
        TaxRule("CN", "CN", "CNY", "中國", BigDecimal("200"), 11, 100, "退稅率 11%", "同店同日滿 ¥200"),
        TaxRule("VN", "VN", "VND", "越南", BigDecimal("2000000"), 10, 110, "VAT 10%", "同店同日滿 ₫2,000,000"),
        TaxRule("TW", "TW", "TWD", "台灣", BigDecimal("2000"), 5, 105, "營業稅 5%", "同店同日滿 NT$2,000"),
        TaxRule("FR", "FR", "EUR", "法國", BigDecimal("100.01"), 20, 120, "VAT 20%", "同店 3 日內滿 €100.01"),
        TaxRule("DE", "DE", "EUR", "德國", BigDecimal("50.01"), 19, 119, "VAT 19%", "單張發票滿 €50.01"),
        TaxRule("IT", "IT", "EUR", "義大利", BigDecimal("70.01"), 22, 122, "VAT 22%", "單張發票滿 €70.01"),
        TaxRule("ES", "ES", "EUR", "西班牙", BigDecimal.ZERO, 21, 121, "VAT 21%", "無最低消費門檻"),
        TaxRule("NL", "NL", "EUR", "荷蘭", BigDecimal("50"), 21, 121, "VAT 21%", "同店同日滿 €50"),
        TaxRule("CH", "CH", "CHF", "瑞士", BigDecimal("300"), 81, 1081, "VAT 8.1%", "同店滿 CHF 300"),
    )

    private val byId = all.associateBy { it.id }
    fun get(id: String): TaxRule? = byId[id]

    /** Default rule for a currency (EUR → France). */
    private val byCurrency = all.groupBy { it.currency }.mapValues { it.value.first() }

    /**
     * "Auto" picks the travel destination: the active currency if it has a rule
     * (and isn't the home currency TWD), otherwise the first listed foreign
     * currency that has one, otherwise Taiwan.
     */
    fun resolve(setting: String, active: String, currencies: List<String>): TaxRule {
        if (setting != AUTO) get(setting)?.let { return it }
        if (active != "TWD") byCurrency[active]?.let { return it }
        currencies.firstOrNull { it != "TWD" && byCurrency[it] != null }?.let { return byCurrency.getValue(it) }
        return byCurrency.getValue("TWD")
    }

    const val AUTO = "auto"
}

object Discounts {
    val builtIn = listOf(BigDecimal("0.9"), BigDecimal("0.88"), BigDecimal("0.85"))

    /** "75" or "7.5" → 0.75, "9" → 0.9, "88.5" → 0.885. Null if invalid. */
    fun parse(input: String): BigDecimal? {
        val d = input.trim().removeSuffix("折").toBigDecimalOrNull() ?: return null
        if (d <= BigDecimal.ZERO || d >= BigDecimal(100)) return null
        val f = if (d < BigDecimal.TEN) d.movePointLeft(1) else d.movePointLeft(2)
        return f.stripTrailingZeros()
    }

    /** 0.9 → "9折", 0.88 → "88折", 0.885 → "88.5折". */
    fun label(f: BigDecimal): String {
        val p = f.movePointRight(2).stripTrailingZeros()
        val tens = p.divide(BigDecimal.TEN)
        val shown = when {
            p < BigDecimal.TEN -> p.movePointLeft(1)
            p.scale() <= 0 && p.remainder(BigDecimal.TEN).signum() == 0 -> tens
            else -> p
        }
        return shown.stripTrailingZeros().toPlainString() + "折"
    }
}

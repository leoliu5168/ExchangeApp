package com.leoliu.fxcalc.data

data class CurrencyInfo(val code: String, val name: String, val region: String) {
    val flagRes: Int? get() = Flags.forRegion(region)
}

object Currencies {
    val all: List<CurrencyInfo> = listOf(
        CurrencyInfo("TWD", "新台幣", "TW"),
        CurrencyInfo("USD", "美元", "US"),
        CurrencyInfo("JPY", "日圓", "JP"),
        CurrencyInfo("CNY", "人民幣", "CN"),
        CurrencyInfo("HKD", "港幣", "HK"),
        CurrencyInfo("EUR", "歐元", "EU"),
        CurrencyInfo("KRW", "韓元", "KR"),
        CurrencyInfo("GBP", "英鎊", "GB"),
        CurrencyInfo("AUD", "澳幣", "AU"),
        CurrencyInfo("NZD", "紐西蘭幣", "NZ"),
        CurrencyInfo("CAD", "加幣", "CA"),
        CurrencyInfo("SGD", "新加坡幣", "SG"),
        CurrencyInfo("THB", "泰銖", "TH"),
        CurrencyInfo("VND", "越南盾", "VN"),
        CurrencyInfo("MYR", "馬來西亞令吉", "MY"),
        CurrencyInfo("PHP", "菲律賓披索", "PH"),
        CurrencyInfo("IDR", "印尼盾", "ID"),
        CurrencyInfo("MOP", "澳門幣", "MO"),
        CurrencyInfo("CHF", "瑞士法郎", "CH"),
        CurrencyInfo("INR", "印度盧比", "IN"),
        CurrencyInfo("SEK", "瑞典克朗", "SE"),
        CurrencyInfo("NOK", "挪威克朗", "NO"),
        CurrencyInfo("DKK", "丹麥克朗", "DK"),
        CurrencyInfo("PLN", "波蘭茲羅提", "PL"),
        CurrencyInfo("CZK", "捷克克朗", "CZ"),
        CurrencyInfo("HUF", "匈牙利福林", "HU"),
        CurrencyInfo("TRY", "土耳其里拉", "TR"),
        CurrencyInfo("RUB", "俄羅斯盧布", "RU"),
        CurrencyInfo("AED", "阿聯酋迪拉姆", "AE"),
        CurrencyInfo("SAR", "沙烏地里亞爾", "SA"),
        CurrencyInfo("ILS", "以色列新謝克爾", "IL"),
        CurrencyInfo("ZAR", "南非蘭特", "ZA"),
        CurrencyInfo("EGP", "埃及鎊", "EG"),
        CurrencyInfo("MXN", "墨西哥披索", "MX"),
        CurrencyInfo("BRL", "巴西雷亞爾", "BR"),
        CurrencyInfo("ARS", "阿根廷披索", "AR"),
        CurrencyInfo("CLP", "智利披索", "CL"),
        CurrencyInfo("PKR", "巴基斯坦盧比", "PK"),
        CurrencyInfo("BDT", "孟加拉塔卡", "BD"),
        CurrencyInfo("LKR", "斯里蘭卡盧比", "LK"),
        CurrencyInfo("NPR", "尼泊爾盧比", "NP"),
        CurrencyInfo("KHR", "柬埔寨瑞爾", "KH"),
        CurrencyInfo("MMK", "緬甸元", "MM"),
        CurrencyInfo("MNT", "蒙古圖格里克", "MN"),
        CurrencyInfo("ISK", "冰島克朗", "IS"),
        CurrencyInfo("QAR", "卡達里亞爾", "QA"),
        CurrencyInfo("KWD", "科威特第納爾", "KW"),
    )

    private val byCode = all.associateBy { it.code }

    fun get(code: String): CurrencyInfo = byCode[code] ?: CurrencyInfo(code, code, "UN")

    val defaults = listOf("TWD", "USD", "JPY", "CNY", "KRW")
}

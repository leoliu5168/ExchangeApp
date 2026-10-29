package com.leoliu.fxcalc.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Rates are "units of currency per 1 USD". */
data class RateSnapshot(
    val rates: Map<String, Double>,
    val fetchedAt: Long,
    val dataDate: String,
    val source: String,
)

/**
 * Free, key-less rate sources, tried in order:
 *  1. fawazahmed0/exchange-api (open source, CC0) via jsDelivr CDN
 *  2. Same dataset via its Cloudflare Pages mirror
 *  3. open.er-api.com (free tier from ExchangeRate-API)
 */
object RateRepository {

    private val fawazUrls = listOf(
        "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.min.json",
        "https://latest.currency-api.pages.dev/v1/currencies/usd.min.json",
    )
    private const val ER_API_URL = "https://open.er-api.com/v6/latest/USD"

    suspend fun fetch(): RateSnapshot = withContext(Dispatchers.IO) {
        var lastError: Exception? = null
        for (url in fawazUrls) {
            try {
                return@withContext parseFawaz(httpGet(url))
            } catch (e: Exception) {
                lastError = e
            }
        }
        try {
            return@withContext parseErApi(httpGet(ER_API_URL))
        } catch (e: Exception) {
            lastError = e
        }
        throw lastError ?: IllegalStateException("No rate source available")
    }

    private val known = Currencies.all.map { it.code }.toSet()

    private fun parseFawaz(body: String): RateSnapshot {
        val root = JSONObject(body)
        val usd = root.getJSONObject("usd")
        val rates = HashMap<String, Double>()
        for (code in known) {
            val v = usd.optDouble(code.lowercase(), Double.NaN)
            if (!v.isNaN() && v > 0) rates[code] = v
        }
        rates["USD"] = 1.0
        require(rates.size > 10) { "Too few rates" }
        return RateSnapshot(rates, System.currentTimeMillis(), root.optString("date"), "fawazahmed0/exchange-api")
    }

    private fun parseErApi(body: String): RateSnapshot {
        val root = JSONObject(body)
        require(root.optString("result") == "success") { "er-api error" }
        val obj = root.getJSONObject("rates")
        val rates = HashMap<String, Double>()
        for (code in known) {
            val v = obj.optDouble(code, Double.NaN)
            if (!v.isNaN() && v > 0) rates[code] = v
        }
        rates["USD"] = 1.0
        val unix = root.optLong("time_last_update_unix", 0L)
        val date = if (unix > 0) {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .format(java.util.Date(unix * 1000))
        } else ""
        return RateSnapshot(rates, System.currentTimeMillis(), date, "open.er-api.com")
    }

    private fun httpGet(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 6_000
            conn.readTimeout = 8_000
            conn.useCaches = false
            conn.setRequestProperty("Accept", "application/json")
            val code = conn.responseCode
            if (code !in 200..299) throw IllegalStateException("HTTP $code")
            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}

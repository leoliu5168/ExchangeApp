package com.leoliu.fxcalc.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class HistoryEntry(
    val currency: String,
    val expression: String,
    val result: String,
    val time: Long,
)

/** Small SharedPreferences wrapper; everything here is tiny so reads are instant. */
class Store(context: Context) {
    private val prefs = context.getSharedPreferences("fxcalc", Context.MODE_PRIVATE)

    var currencies: List<String>
        get() = prefs.getString("currencies", null)
            ?.split(',')?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }
            ?: Currencies.defaults
        set(v) = prefs.edit().putString("currencies", v.joinToString(",")).apply()

    var active: String?
        get() = prefs.getString("active", null)
        set(v) = prefs.edit().putString("active", v).apply()

    var history: List<HistoryEntry>
        get() = try {
            val arr = JSONArray(prefs.getString("history", "[]"))
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                HistoryEntry(o.getString("c"), o.getString("e"), o.getString("r"), o.getLong("t"))
            }
        } catch (e: Exception) {
            emptyList()
        }
        set(v) {
            val arr = JSONArray()
            v.forEach {
                arr.put(JSONObject().put("c", it.currency).put("e", it.expression).put("r", it.result).put("t", it.time))
            }
            prefs.edit().putString("history", arr.toString()).apply()
        }

    var rates: RateSnapshot?
        get() = try {
            prefs.getString("rates", null)?.let { s ->
                val o = JSONObject(s)
                val r = o.getJSONObject("r")
                val map = HashMap<String, Double>()
                r.keys().forEach { k -> map[k] = r.getDouble(k) }
                RateSnapshot(map, o.getLong("t"), o.optString("d"), o.optString("s"))
            }
        } catch (e: Exception) {
            null
        }
        set(v) {
            if (v == null) {
                prefs.edit().remove("rates").apply(); return
            }
            val r = JSONObject()
            v.rates.forEach { (k, value) -> r.put(k, value) }
            val o = JSONObject().put("r", r).put("t", v.fetchedAt).put("d", v.dataDate).put("s", v.source)
            prefs.edit().putString("rates", o.toString()).apply()
        }
}

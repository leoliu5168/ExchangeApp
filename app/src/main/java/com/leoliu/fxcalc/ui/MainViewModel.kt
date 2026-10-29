package com.leoliu.fxcalc.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leoliu.fxcalc.calc.Calculator
import com.leoliu.fxcalc.data.HistoryEntry
import com.leoliu.fxcalc.data.RateRepository
import com.leoliu.fxcalc.data.RateSnapshot
import com.leoliu.fxcalc.data.Store
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext

data class UiState(
    val currencies: List<String>,
    val active: String,
    val expression: String = "",
    val justEvaluated: Boolean = false,
    val rates: RateSnapshot? = null,
    val loading: Boolean = false,
    val error: Boolean = false,
    val history: List<HistoryEntry> = emptyList(),
) {
    /** Value of the active row (null on error, e.g. divide by zero). */
    val activeValue: BigDecimal? get() = Calculator.evaluate(expression)

    fun convert(value: BigDecimal, from: String, to: String): BigDecimal? {
        if (from == to) return value
        val r = rates?.rates ?: return null
        val f = r[from] ?: return null
        val t = r[to] ?: return null
        return value.multiply(BigDecimal(t), MathContext.DECIMAL64).divide(BigDecimal(f), MathContext.DECIMAL64)
    }
}

class MainViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        const val REFRESH_INTERVAL_MS = 3 * 60_000L
        private const val RETRY_MS = 30_000L
        private const val MAX_HISTORY = 5
    }

    private val store = Store(app)

    private val _state = MutableStateFlow(
        store.currencies.let { list ->
            UiState(
                currencies = list,
                active = store.active?.takeIf { it in list } ?: list.first(),
                expression = "100",
                justEvaluated = true,
                rates = store.rates,
                history = store.history,
            )
        }
    )
    val state: StateFlow<UiState> = _state.asStateFlow()

    private var refreshJob: Job? = null

    /** Fetches rates now unless a fetch is already in flight. */
    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch { doRefresh() }
    }

    private suspend fun doRefresh(): Boolean {
        _state.update { it.copy(loading = true) }
        return try {
            val snap = RateRepository.fetch()
            store.rates = snap
            _state.update { it.copy(rates = snap, loading = false, error = false) }
            true
        } catch (e: Exception) {
            _state.update { it.copy(loading = false, error = true) }
            false
        }
    }

    /**
     * Runs while the app is in the foreground: fetches immediately on open,
     * then every 3 minutes (retrying sooner after a failure).
     */
    suspend fun autoRefreshLoop() {
        while (true) {
            val ok = doRefresh()
            delay(if (ok) REFRESH_INTERVAL_MS else RETRY_MS)
        }
    }

    // ---------- keypad ----------

    fun onKey(key: Key) {
        _state.update { s ->
            val start = if (s.justEvaluated && (key is Key.Digit || key == Key.Dot)) "" else s.expression
            val e = when (key) {
                is Key.Digit -> Calculator.inputDigit(start, key.d)
                Key.Dot -> Calculator.inputDot(start)
                is Key.Op -> Calculator.inputOperator(start, key.op)
                Key.Percent -> Calculator.inputPercent(start)
                Key.Back -> if (s.justEvaluated) "" else Calculator.backspace(start)
                Key.Clear -> ""
                Key.Equals -> return@update evaluate(s)
            }
            s.copy(expression = e, justEvaluated = false)
        }
    }

    private fun evaluate(s: UiState): UiState {
        val value = Calculator.evaluate(s.expression) ?: return s
        val result = Calculator.toEditable(value)
        var history = s.history
        if (s.expression.isNotEmpty() && value.signum() != 0) {
            val entry = HistoryEntry(s.active, s.expression, result, System.currentTimeMillis())
            val first = history.firstOrNull()
            if (first == null || first.currency != entry.currency || first.expression != entry.expression) {
                history = (listOf(entry) + history).take(MAX_HISTORY)
                store.history = history
            }
        }
        return s.copy(expression = result, justEvaluated = true, history = history)
    }

    /** Make another row the input row, carrying over its converted amount. */
    fun selectRow(code: String) {
        _state.update { s ->
            if (code == s.active) return@update s
            val v = s.activeValue?.let { s.convert(it, s.active, code) }
            store.active = code
            s.copy(active = code, expression = v?.let { Calculator.toEditable(it) } ?: "", justEvaluated = true)
        }
    }

    // ---------- currency list ----------

    /** Replace the currency of one row; if [newCode] is already in the list, the two swap places. */
    fun changeCurrency(oldCode: String, newCode: String) {
        _state.update { s ->
            if (oldCode == newCode) return@update s
            val list = s.currencies.toMutableList()
            val i = list.indexOf(oldCode)
            if (i < 0) return@update s
            val j = list.indexOf(newCode)
            list[i] = newCode
            if (j >= 0) list[j] = oldCode
            // Rows are identified by currency code, so the input row simply follows its code.
            val active = if (s.active == oldCode) newCode else s.active
            store.currencies = list
            store.active = active
            s.copy(currencies = list, active = active)
        }
    }

    fun addCurrency(code: String) {
        _state.update { s ->
            if (code in s.currencies) return@update s
            val list = s.currencies + code
            store.currencies = list
            s.copy(currencies = list)
        }
    }

    fun removeCurrency(code: String) {
        _state.update { s ->
            if (s.currencies.size <= 1 || code !in s.currencies) return@update s
            val list = s.currencies - code
            var active = s.active
            var expr = s.expression
            if (active == code) {
                active = list.first()
                val v = s.activeValue?.let { s.convert(it, code, active) }
                expr = v?.let { Calculator.toEditable(it) } ?: ""
            }
            store.currencies = list
            store.active = active
            s.copy(currencies = list, active = active, expression = expr, justEvaluated = true)
        }
    }

    fun setOrder(list: List<String>) {
        _state.update { s ->
            if (list.toSet() != s.currencies.toSet()) return@update s
            store.currencies = list
            s.copy(currencies = list)
        }
    }

    // ---------- history ----------

    fun restoreHistory(entry: HistoryEntry) {
        _state.update { s ->
            val list = if (entry.currency in s.currencies) s.currencies else (s.currencies + entry.currency).also { store.currencies = it }
            store.active = entry.currency
            s.copy(currencies = list, active = entry.currency, expression = entry.result, justEvaluated = true)
        }
    }

    fun clearHistory() {
        store.history = emptyList()
        _state.update { it.copy(history = emptyList()) }
    }
}

sealed interface Key {
    data class Digit(val d: Char) : Key
    data class Op(val op: Char) : Key
    data object Dot : Key
    data object Percent : Key
    data object Back : Key
    data object Clear : Key
    data object Equals : Key
}

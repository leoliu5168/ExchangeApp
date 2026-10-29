package com.leoliu.fxcalc.calc

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Expression strings use these characters:
 *   digits, '.', operators '+' '−' '×' '÷', postfix '%',
 *   and an optional leading '-' for a negative first number.
 */
object Calculator {
    const val PLUS = '+'
    const val MINUS = '−'
    const val TIMES = '×'
    const val DIVIDE = '÷'
    const val PERCENT = '%'

    private val operators = setOf(PLUS, MINUS, TIMES, DIVIDE)
    private const val MAX_DIGITS = 15
    private val MC = MathContext.DECIMAL64

    fun isOperator(c: Char) = c in operators
    fun hasOperation(expr: String) = expr.drop(1).any { it in operators || it == PERCENT } || expr.endsWith(PERCENT)

    /** The number currently being typed (after the last operator). */
    private fun lastToken(expr: String): String {
        val idx = expr.indexOfLast { it in operators }
        return if (idx < 0) expr.removePrefix("-") else expr.substring(idx + 1)
    }

    fun inputDigit(expr: String, d: Char): String {
        if (expr.endsWith(PERCENT)) return expr + TIMES + d
        val token = lastToken(expr)
        if (token.count { it.isDigit() } >= MAX_DIGITS) return expr
        if (token == "0") return expr.dropLast(1) + d
        return expr + d
    }

    fun inputDot(expr: String): String {
        if (expr.endsWith(PERCENT)) return expr + TIMES + "0."
        val token = lastToken(expr)
        if (token.contains('.')) return expr
        return if (token.isEmpty()) expr + "0." else "$expr."
    }

    fun inputOperator(expr: String, op: Char): String {
        var e = expr.ifEmpty { "0" }
        if (e == "-") e = "0"
        if (e.endsWith('.')) e = e.dropLast(1)
        if (e.last() in operators) e = e.dropLast(1)
        return e + op
    }

    fun inputPercent(expr: String): String {
        if (expr.isEmpty() || expr.endsWith(PERCENT)) return expr
        val last = expr.last()
        if (last in operators) return expr
        return (if (last == '.') expr.dropLast(1) else expr) + PERCENT
    }

    fun backspace(expr: String): String = expr.dropLast(1).let { if (it == "-") "" else it }

    private data class Operand(val value: BigDecimal, val percent: Boolean)

    /** Evaluates the expression; a trailing operator is ignored. Returns null on error (e.g. ÷0). */
    fun evaluate(expr: String): BigDecimal? {
        if (expr.isEmpty()) return BigDecimal.ZERO
        var e = expr
        while (e.isNotEmpty() && (e.last() in operators || e.last() == '.')) e = e.dropLast(1)
        if (e.isEmpty() || e == "-") return BigDecimal.ZERO

        val operands = ArrayList<Operand>()
        val ops = ArrayList<Char>()
        val sb = StringBuilder()
        var pct = false
        fun flush(): Boolean {
            val s = sb.toString()
            if (s.isEmpty() || s == "-") return false
            operands.add(Operand(s.toBigDecimalOrNull() ?: return false, pct))
            sb.clear(); pct = false
            return true
        }
        for ((i, c) in e.withIndex()) {
            when {
                c.isDigit() || c == '.' -> sb.append(c)
                c == '-' && i == 0 -> sb.append(c)
                c == PERCENT -> pct = true
                c in operators -> { if (!flush()) return null; ops.add(c) }
                else -> return null
            }
        }
        if (!flush()) return null
        if (ops.size != operands.size - 1) return null

        val hundred = BigDecimal(100)
        fun plain(o: Operand) = if (o.percent) o.value.divide(hundred, MC) else o.value

        return try {
            var sum = BigDecimal.ZERO
            var term = plain(operands[0])
            for (i in ops.indices) {
                val n = operands[i + 1]
                when (ops[i]) {
                    TIMES -> term = term.multiply(plain(n), MC)
                    DIVIDE -> {
                        val d = plain(n)
                        if (d.signum() == 0) return null
                        term = term.divide(d, MC)
                    }
                    else -> {
                        sum = sum.add(term, MC)
                        // "a + b%" means a plus b percent of a (like most phone calculators).
                        val base = if (n.percent) sum.multiply(n.value, MC).divide(hundred, MC) else n.value
                        term = if (ops[i] == PLUS) base else base.negate()
                    }
                }
            }
            sum.add(term, MC)
        } catch (e: ArithmeticException) {
            null
        }
    }

    // ---------- formatting ----------

    private val symbols = DecimalFormatSymbols(Locale.US)

    /** Rounded value for display, e.g. 118,370.45 or 0.006812. */
    fun format(v: BigDecimal): String {
        val abs = v.abs()
        val rounded = when {
            abs.signum() == 0 -> return "0"
            abs < BigDecimal.ONE -> v.round(MathContext(4, RoundingMode.HALF_UP))
            else -> v.setScale(2, RoundingMode.HALF_UP)
        }
        val df = DecimalFormat(if (abs < BigDecimal.ONE) "0.########" else "#,##0.##", symbols)
        return df.format(rounded)
    }

    /** Value as an editable plain string (no grouping), used when switching the active row. */
    fun toEditable(v: BigDecimal): String {
        if (v.signum() == 0) return ""
        val abs = v.abs()
        val r = if (abs < BigDecimal.ONE) v.round(MathContext(4, RoundingMode.HALF_UP)) else v.setScale(2, RoundingMode.HALF_UP)
        return r.stripTrailingZeros().toPlainString()
    }

    /** Pretty-prints the raw expression: groups thousands and spaces operators. */
    fun display(expr: String): String {
        if (expr.isEmpty()) return "0"
        val out = StringBuilder()
        val num = StringBuilder()
        fun flushNum() {
            if (num.isEmpty()) return
            val s = num.toString()
            val neg = s.startsWith("-")
            val body = s.removePrefix("-")
            val dot = body.indexOf('.')
            val intPart = if (dot >= 0) body.substring(0, dot) else body
            val frac = if (dot >= 0) body.substring(dot) else ""
            val grouped = intPart.reversed().chunked(3).joinToString(",").reversed()
            if (neg) out.append('-')
            out.append(grouped).append(frac)
            num.clear()
        }
        for ((i, c) in expr.withIndex()) {
            when {
                c.isDigit() || c == '.' || (c == '-' && i == 0) -> num.append(c)
                c == PERCENT -> { flushNum(); out.append(c) }
                else -> { flushNum(); out.append(' ').append(c).append(' ') }
            }
        }
        flushNum()
        return out.toString().trimEnd()
    }
}

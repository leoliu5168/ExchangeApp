package com.leoliu.fxcalc.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leoliu.fxcalc.calc.Calculator
import com.leoliu.fxcalc.ui.theme.Fx

private enum class KeyStyle { Number, Function, Operator }

@Composable
fun Keypad(onKey: (Key) -> Unit, modifier: Modifier = Modifier) {
    val gap = 10.dp
    Column(modifier, verticalArrangement = Arrangement.spacedBy(gap)) {
        KeyRow(gap) {
            CalcKey("C", KeyStyle.Function, onKey, Key.Clear)
            CalcKey(null, KeyStyle.Function, onKey, Key.Back, onLongClick = { onKey(Key.Clear) })
            CalcKey("%", KeyStyle.Function, onKey, Key.Percent)
            CalcKey("÷", KeyStyle.Operator, onKey, Key.Op(Calculator.DIVIDE))
        }
        KeyRow(gap) {
            for (d in "789") CalcKey(d.toString(), KeyStyle.Number, onKey, Key.Digit(d))
            CalcKey("×", KeyStyle.Operator, onKey, Key.Op(Calculator.TIMES))
        }
        KeyRow(gap) {
            for (d in "456") CalcKey(d.toString(), KeyStyle.Number, onKey, Key.Digit(d))
            CalcKey("−", KeyStyle.Operator, onKey, Key.Op(Calculator.MINUS))
        }
        KeyRow(gap) {
            for (d in "123") CalcKey(d.toString(), KeyStyle.Number, onKey, Key.Digit(d))
            CalcKey("+", KeyStyle.Operator, onKey, Key.Op(Calculator.PLUS))
        }
        KeyRow(gap) {
            CalcKey("0", KeyStyle.Number, onKey, Key.Digit('0'), weight = 2f)
            CalcKey(".", KeyStyle.Number, onKey, Key.Dot)
            CalcKey("=", KeyStyle.Operator, onKey, Key.Equals)
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.KeyRow(
    gap: androidx.compose.ui.unit.Dp,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .weight(1f),
        horizontalArrangement = Arrangement.spacedBy(gap),
        content = content,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.CalcKey(
    label: String?,
    style: KeyStyle,
    onKey: (Key) -> Unit,
    key: Key,
    weight: Float = 1f,
    onLongClick: (() -> Unit)? = null,
) {
    val view = LocalView.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, spring(dampingRatio = 0.55f, stiffness = 900f), label = "scale")
    val overlay by animateFloatAsState(if (pressed) 0.18f else 0f, spring(stiffness = 1500f), label = "overlay")

    val bg: Brush = when (style) {
        KeyStyle.Number -> SolidColor(Fx.KeyNumber)
        KeyStyle.Function -> SolidColor(Fx.KeyFunction)
        KeyStyle.Operator -> Fx.AccentGradient
    }
    val fg = if (style == KeyStyle.Operator) Color.White else Fx.TextPrimary

    Box(
        Modifier
            .weight(weight)
            .fillMaxHeight()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(28.dp))
            .background(bg)
            .background(Color.White.copy(alpha = overlay))
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onLongClick = onLongClick?.let { lc ->
                    {
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        lc()
                    }
                },
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    onKey(key)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (label == null) {
            Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "刪除", tint = fg)
        } else {
            val size = if (style == KeyStyle.Operator) 34.sp else 30.sp
            Text(
                label,
                color = fg,
                fontSize = size,
                fontWeight = if (style == KeyStyle.Number) FontWeight.Normal else FontWeight.Medium,
            )
        }
    }
}

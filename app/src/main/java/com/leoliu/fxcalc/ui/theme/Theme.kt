package com.leoliu.fxcalc.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object Fx {
    val Bg = Color(0xFF0B0B0F)
    val Surface = Color(0xFF17171D)
    val SurfaceHigh = Color(0xFF22222A)
    val Outline = Color(0xFF2E2E38)
    val Accent = Color(0xFFFF9F0A)
    val AccentLight = Color(0xFFFFC163)
    val TextPrimary = Color(0xFFF5F5F7)
    val TextSecondary = Color(0xFF8E8E99)
    val KeyNumber = Color(0xFF1E1E25)
    val KeyFunction = Color(0xFF34343F)
    val Good = Color(0xFF30D158)
    val Warn = Color(0xFFFFD60A)
    val Bad = Color(0xFFFF453A)

    val AccentGradient = Brush.verticalGradient(listOf(Color(0xFFFFB340), Color(0xFFFF8A00)))
}

@Composable
fun FxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Fx.Accent,
            onPrimary = Color.Black,
            background = Fx.Bg,
            surface = Fx.Surface,
            surfaceContainerLow = Fx.Surface,
            surfaceContainer = Fx.Surface,
            surfaceContainerHigh = Fx.SurfaceHigh,
            onSurface = Fx.TextPrimary,
            onSurfaceVariant = Fx.TextSecondary,
            outline = Fx.Outline,
        ),
        content = content,
    )
}

package com.leoliu.fxcalc.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.leoliu.fxcalc.data.CurrencyInfo
import com.leoliu.fxcalc.ui.theme.Fx

/** Round flag: the emoji is drawn oversized and clipped to a circle. */
@Composable
fun FlagCircle(info: CurrencyInfo, size: Dp = 34.dp, modifier: Modifier = Modifier) {
    val fontSize = with(LocalDensity.current) { (size * 1.25f).toSp() }
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Fx.SurfaceHigh),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            info.flag,
            fontSize = fontSize,
            lineHeight = fontSize,
            textAlign = TextAlign.Center,
            softWrap = false,
            maxLines = 1,
        )
    }
}

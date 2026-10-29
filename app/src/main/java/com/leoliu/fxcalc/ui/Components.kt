package com.leoliu.fxcalc.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leoliu.fxcalc.data.CurrencyInfo
import com.leoliu.fxcalc.ui.theme.Fx

/** Flat 4:3 flag with slightly rounded corners and a hairline edge so white flags stay visible. */
@Composable
fun Flag(info: CurrencyInfo, width: Dp = 36.dp, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(4.dp)
    val base = modifier
        .size(width, width * 0.75f)
        .clip(shape)
    val res = info.flagRes
    if (res != null) {
        Image(
            painter = painterResource(res),
            contentDescription = info.name,
            contentScale = ContentScale.FillBounds,
            modifier = base.border(0.5.dp, Color.White.copy(alpha = 0.18f), shape),
        )
    } else {
        Box(base.background(Fx.SurfaceHigh), contentAlignment = Alignment.Center) {
            Text(info.code.take(2), color = Fx.TextSecondary, fontSize = 10.sp)
        }
    }
}

package com.leoliu.fxcalc.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.leoliu.fxcalc.ui.theme.Fx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

data class HintMsg(val id: Long, val text: String)

const val HINT_AUTO_DISMISS_MS = 5_000

/**
 * Top notification card. Can always be swiped away (left, right or up);
 * when [autoDismiss] is on it also closes itself after 5 seconds.
 */
@Composable
fun HintBanner(
    hint: HintMsg?,
    autoDismiss: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Keep the last message so the exit animation still has text to show.
    val lastHolder = remember { arrayOfNulls<HintMsg>(1) }
    if (hint != null) lastHolder[0] = hint

    LaunchedEffect(hint?.id, autoDismiss) {
        if (hint != null && autoDismiss) {
            delay(HINT_AUTO_DISMISS_MS.toLong())
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = hint != null,
        modifier = modifier,
        enter = slideInVertically { -it / 2 } + fadeIn(tween(160)),
        exit = fadeOut(tween(160)) + slideOutVertically { -it / 3 },
    ) {
        val msg = lastHolder[0] ?: return@AnimatedVisibility
        key(msg.id) { HintCard(msg, autoDismiss && hint != null, onDismiss) }
    }
}

@Composable
private fun HintCard(msg: HintMsg, showTimer: Boolean, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val dx = remember { Animatable(0f) }
    val dy = remember { Animatable(0f) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(showTimer) {
        if (showTimer) progress.animateTo(0f, tween(HINT_AUTO_DISMISS_MS, easing = LinearEasing))
    }
    val swipeX = with(density) { 90.dp.toPx() }
    val swipeUp = with(density) { 36.dp.toPx() }
    val shape = RoundedCornerShape(18.dp)

    Box(
        Modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = dx.value
                translationY = dy.value
                alpha = (1f - abs(dx.value) / (swipeX * 3f) - abs(dy.value) / (swipeUp * 4f)).coerceIn(0.2f, 1f)
            }
            .pointerInput(msg.id) {
                detectDragGestures(
                    onDragEnd = {
                        scope.launch {
                            when {
                                abs(dx.value) > swipeX -> {
                                    dx.animateTo(if (dx.value > 0) size.width.toFloat() else -size.width.toFloat(), tween(150))
                                    onDismiss()
                                }
                                dy.value < -swipeUp -> {
                                    dy.animateTo(-size.height * 1.5f, tween(150))
                                    onDismiss()
                                }
                                else -> {
                                    launch { dx.animateTo(0f) }
                                    dy.animateTo(0f)
                                }
                            }
                        }
                    },
                    onDrag = { change, drag ->
                        change.consume()
                        scope.launch {
                            dx.snapTo(dx.value + drag.x)
                            // Only allow pulling upwards; downward drag is damped.
                            dy.snapTo((dy.value + drag.y).coerceAtMost(swipeUp / 3f))
                        }
                    },
                )
            }
            .shadow(12.dp, shape)
            .clip(shape)
            .background(Fx.SurfaceHigh),
    ) {
        Column {
            Row(
                Modifier.padding(start = 14.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Info, contentDescription = null, tint = Fx.AccentLight, modifier = Modifier.size(20.dp))
                Text(
                    msg.text,
                    color = Fx.TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Rounded.Close, contentDescription = "關閉", tint = Fx.TextSecondary, modifier = Modifier.size(18.dp))
                }
            }
            if (showTimer) {
                Box(
                    Modifier
                        .fillMaxWidth(progress.value)
                        .height(3.dp)
                        .background(Fx.Accent.copy(alpha = 0.8f))
                )
            }
        }
    }
}

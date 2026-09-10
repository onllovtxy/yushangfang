package com.xixikitchen.jetpack.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import com.xixikitchen.jetpack.ui.designsystem.motion.SpringPress

@Composable
fun Modifier.noRippleClick(enabled: Boolean = true, onClick: () -> Unit): Modifier =
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        enabled = enabled,
        onClick = onClick
    )

/**
 * 可点面。反馈发生在 **pointer-down 的那一刻**,而不是点击回调之后。
 *
 * 这是整个界面"活不活"的分水岭:等 `onClick` 才给反馈,用户会感觉界面慢半拍;
 * 按下即缩,界面就像真的在听你说话。
 *
 * 两个细节:
 *  - 用 [graphicsLayer] 缩放,走合成层,不触发 layout/paint。
 *  - 刻意**不用** Material 涟漪。玻璃/新拟态面上闪一层涟漪是外来语汇,
 *    正确的反馈是"这块面被按下去了" —— 也就是缩放本身。
 *  - 缩放幅度保持克制(0.97)。真实世界里没有东西会从 scale(0) 里蹦出来。
 */
@Composable
fun Modifier.noRipplePressable(
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
    onClick: () -> Unit
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = SpringPress,
        label = "pressScale"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onClick = onClick
        )
}

/** [noRipplePressable] 的长按版本。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Modifier.noRippleCombinedPressable(
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = SpringPress,
        label = "pressScale"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .combinedClickable(
            interactionSource = interaction,
            indication = null,
            enabled = enabled,
            onLongClick = onLongClick,
            onClick = onClick
        )
}

/**
 * 只加按压缩放,不接管点击 —— 给已经有自己的手势处理(比如左滑露出删除)的面用。
 */
@Composable
fun Modifier.pressScale(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = 0.97f,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) pressedScale else 1f,
        animationSpec = SpringPress,
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * 骨架屏微光。线性、1200ms —— 常量运动(进度条、跑马灯)才用 linear。
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -2 * size.width.toFloat(),
        targetValue = 2 * size.width.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    background(
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFF7F3EE),
                Color(0xFFFDEFE2),
                Color(0xFFF7F3EE),
            ),
            start = Offset(startOffsetX, 0f),
            end = Offset(startOffsetX + size.width.toFloat(), size.height.toFloat())
        )
    ).onGloballyPositioned {
        size = it.size
    }
}

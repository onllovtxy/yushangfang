package com.xixikitchen.jetpack.ui.designsystem.theme

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

enum class AppSkin { NEUMORPHIC, GLASS, CLASSIC_NEUMORPHIC }
val LocalAppSkin = staticCompositionLocalOf { AppSkin.GLASS }

// ═══ 背景绘制核心 ═══

/**
 * 在 DrawScope 中绘制主题背景(以 origin 为坐标系偏移)。
 *
 * 几何一律先按"整屏"算(fullSize),再整体减去 origin。这样卡片里画出来的那一小片,
 * 和全屏背景上同一个位置的颜色是对得上的 —— 卡片看起来才像一块挖空的玻璃,
 * 而不是自己另画了一张背景。
 *
 * 角度约定:0° 从上往下,顺时针为正。
 */
private fun DrawScope.drawMesh(tokens: GlassTokens, fullSize: Size, origin: Offset) {
    when (val bg = tokens.background) {
        is GlassBackground.Mesh -> {
            drawRect(bg.base)
            val minDim = minOf(fullSize.width, fullSize.height)
            for (spot in bg.spots) {
                val center = Offset(
                    x = spot.xFraction * fullSize.width - origin.x,
                    y = spot.yFraction * fullSize.height - origin.y,
                )
                val radius = (spot.radiusFraction * minDim).coerceAtLeast(1f)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(spot.color, Color.Transparent),
                        center = center,
                        radius = radius,
                    )
                )
            }
        }

        is GlassBackground.Linear -> {
            if (bg.colors.isEmpty()) return
            val (start, end) = gradientEndpoints(fullSize, bg.angleDegrees, origin)
            drawRect(Brush.linearGradient(bg.colors, start = start, end = end))
        }

        is GlassBackground.Angled -> {
            if (bg.colors.isEmpty()) return
            val (start, end) = gradientEndpoints(fullSize, bg.angleDegrees, origin)
            drawRect(
                Brush.linearGradient(
                    colorStops = smoothStops(bg.colors, bg.weights),
                    start = start,
                    end = end,
                )
            )
        }

        is GlassBackground.Quad -> {
            // 四个颜色各占一角,中间平滑晕开 —— 四团大半径径向渐变叠出来,
            // 而不是画四个矩形。
            //
            // 之前这里就是画四个 drawRect,画出来是四块色块十字拼接,
            // 和"渐变"没有关系。用径向叠加之后,每个角由自己的颜色主导,
            // 越往中间越融合,既看得出"四分",又全程连续。
            if (bg.colors.isEmpty()) return
            drawRect(averageColor(bg.colors))
            val minDim = minOf(fullSize.width, fullSize.height)
            val radius = minDim * 1.15f
            QUAD_CORNERS.forEachIndexed { index, (xFrac, yFrac) ->
                val color = bg.colors.getOrNull(index) ?: return@forEachIndexed
                drawRect(
                    brush = Brush.radialGradient(
                        // 淡出到"自己颜色的透明版",而不是 Color.Transparent ——
                        // 后者是透明黑,插值过去会把中间调灰、发脏。
                        colors = listOf(color, color.copy(alpha = 0f)),
                        center = Offset(
                            x = xFrac * fullSize.width - origin.x,
                            y = yFrac * fullSize.height - origin.y,
                        ),
                        radius = radius,
                    )
                )
            }
        }
    }
}

private val QUAD_CORNERS = listOf(
    0f to 0f,   // 左上
    1f to 0f,   // 右上
    0f to 1f,   // 左下
    1f to 1f,   // 右下
)

private fun averageColor(colors: List<Color>): Color {
    if (colors.isEmpty()) return Color.White
    var r = 0f
    var g = 0f
    var b = 0f
    var a = 0f
    for (c in colors) {
        r += c.red
        g += c.green
        b += c.blue
        a += c.alpha
    }
    val n = colors.size.toFloat()
    return Color(r / n, g / n, b / n, a / n)
}

/**
 * 把角度换算成一条覆盖整屏的渐变线。
 *
 * 线长取矩形在渐变方向上的投影 |w·sinθ| + |h·cosθ| —— 短了会在角落露出 clamp 的纯色。
 */
private fun gradientEndpoints(size: Size, angleDegrees: Float, origin: Offset): Pair<Offset, Offset> {
    val rad = Math.toRadians(angleDegrees.toDouble())
    val dx = sin(rad).toFloat()
    val dy = cos(rad).toFloat()
    val length = abs(size.width * dx) + abs(size.height * dy)
    val cx = size.width / 2f - origin.x
    val cy = size.height / 2f - origin.y
    val halfX = dx * length / 2f
    val halfY = dy * length / 2f
    return Offset(cx - halfX, cy - halfY) to Offset(cx + halfX, cy + halfY)
}

/**
 * 把色值排成一条**平滑**渐变。
 *
 * N 个颜色要在 [0,1] 上首尾对齐,数学上只有 N-1 个自由度 ——
 * 所以 [gaps] 是相邻两个颜色之间的"距离",长度是 N-1,
 * 第一个颜色永远在 0、最后一个永远在 1。
 *
 * 之前这里是硬边实现:每条边界放两个位置相同、颜色不同的 stop。
 * 位置重合必然切出一条刀锋,画出来是两块颜色怼在一起,不是渐变。
 */
private fun smoothStops(colors: List<Color>, gaps: List<Float>): Array<Pair<Float, Color>> {
    if (colors.size == 1) return arrayOf(0f to colors[0])
    val raw = List(colors.size - 1) { i -> (gaps.getOrNull(i) ?: 1f).coerceAtLeast(0.01f) }
    val total = raw.sum().takeIf { it > 0f } ?: raw.size.toFloat()
    val out = ArrayList<Pair<Float, Color>>(colors.size)
    var acc = 0f
    colors.forEachIndexed { index, color ->
        out += acc.coerceIn(0f, 1f) to color
        if (index < raw.size) acc += raw[index] / total
    }
    return out.toTypedArray()
}

// ═══ 全屏 Mesh 背景 ═══

@Composable
fun GlassMeshBackground(tokens: GlassTokens, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind { drawMesh(tokens, fullSize = size, origin = Offset.Zero) }
    )
}

/**
 * 把某个主题的背景铺进任意大小的容器 —— 设置页的主题预览色块用这个,
 * 保证预览和真正切过去之后看到的是同一套画法,而不是另画一张示意图。
 */
fun Modifier.themePreviewBackground(tokens: GlassTokens): Modifier =
    drawBehind { drawMesh(tokens, fullSize = size, origin = Offset.Zero) }

// ═══ 玻璃/新拟态 凸起面 ═══


@Composable
fun Modifier.glassConvex(
    cornerRadius: Dp,
    tokens: GlassTokens = LocalGlassTokens.current,
): Modifier {
    val skin = LocalAppSkin.current
    val shape = RoundedCornerShape(cornerRadius)
    val density = LocalDensity.current
    val radiusPx = with(density) { cornerRadius.toPx() }

    if (skin == AppSkin.CLASSIC_NEUMORPHIC) {
        val elevation = 7.dp
        val darkShadow = Color(0x358C8374)
        val lightShadow = Color(0xFFFFFFFF)
        val bgColor = Color(0xFFF6F4EE)

        return this
            .clip(shape)
            .background(bgColor)
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val composePaint = Paint().apply {
                        asFrameworkPaint().apply {
                            isAntiAlias = true
                            maskFilter = BlurMaskFilter(elevation.toPx(), BlurMaskFilter.Blur.NORMAL)
                        }
                    }
                    // 右下经典软阴影
                    composePaint.color = darkShadow
                    canvas.drawRoundRect(
                        left = elevation.toPx() * 0.5f,
                        top = elevation.toPx() * 0.5f,
                        right = size.width + elevation.toPx() * 0.5f,
                        bottom = size.height + elevation.toPx() * 0.5f,
                        radiusX = radiusPx,
                        radiusY = radiusPx,
                        paint = composePaint
                    )
                    // 左上经典高光
                    composePaint.color = lightShadow
                    canvas.drawRoundRect(
                        left = -elevation.toPx() * 0.5f,
                        top = -elevation.toPx() * 0.5f,
                        right = size.width - elevation.toPx() * 0.5f,
                        bottom = size.height - elevation.toPx() * 0.5f,
                        radiusX = radiusPx,
                        radiusY = radiusPx,
                        paint = composePaint
                    )
                }
            }
    }

    if (skin == AppSkin.NEUMORPHIC) {
        val shadowElevation = 6.dp
        val darkShadow = Color(0x308C8374)
        val lightShadow = Color(0xFFFFFFFF)

        return this
            .clip(shape)
            .drawBehind {
                drawIntoCanvas { canvas ->
                    val composePaint = Paint().apply {
                        asFrameworkPaint().apply {
                            isAntiAlias = true
                            maskFilter = BlurMaskFilter(shadowElevation.toPx(), BlurMaskFilter.Blur.NORMAL)
                        }
                    }
                    // 右下暗影
                    composePaint.color = darkShadow
                    canvas.drawRoundRect(
                        left = shadowElevation.toPx() * 0.4f,
                        top = shadowElevation.toPx() * 0.4f,
                        right = size.width + shadowElevation.toPx() * 0.4f,
                        bottom = size.height + shadowElevation.toPx() * 0.4f,
                        radiusX = radiusPx,
                        radiusY = radiusPx,
                        paint = composePaint
                    )
                    // 左上高光
                    composePaint.color = lightShadow
                    canvas.drawRoundRect(
                        left = -shadowElevation.toPx() * 0.4f,
                        top = -shadowElevation.toPx() * 0.4f,
                        right = size.width - shadowElevation.toPx() * 0.4f,
                        bottom = size.height - shadowElevation.toPx() * 0.4f,
                        radiusX = radiusPx,
                        radiusY = radiusPx,
                        paint = composePaint
                    )
                }
                // 3D 浮雕表面
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFFFFF), Color(0xFFF6F4EE)),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                )
            }
            .drawBehind {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFFFFF), Color(0x35D6CFC0)),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
    }

    var winOffset by remember { mutableStateOf(Offset.Zero) }
    val configuration = LocalConfiguration.current
    val fullSize = with(density) {
        Size(configuration.screenWidthDp.dp.toPx(), configuration.screenHeightDp.dp.toPx())
    }

    return this
        .onGloballyPositioned { winOffset = it.positionInWindow() }
        .drawBehind {
            drawRoundRect(
                color = tokens.outerShadow,
                topLeft = Offset(0f, 3.dp.toPx()),
                size = size,
                cornerRadius = CornerRadius(radiusPx, radiusPx),
            )
        }
        .clip(shape)
        .drawBehind {
            drawMesh(tokens, fullSize, winOffset)
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xD8FFFFFF),
                        tokens.tintConvex,
                        Color(0x95FFFFFF)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
        }
        .drawWithContent {
            drawContent()
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0x00FFFFFF))
                ),
                size = Size(size.width, 2.dp.toPx()),
            )
        }
        .drawBehind {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(tokens.borderHi, tokens.borderLo),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                style = Stroke(width = 1.5.dp.toPx()),
            )
        }
}

// ═══ 玻璃/新拟态 凹陷面 ═══

@Composable
fun Modifier.glassConcave(
    cornerRadius: Dp,
    tokens: GlassTokens = LocalGlassTokens.current,
): Modifier {
    val skin = LocalAppSkin.current
    val shape = RoundedCornerShape(cornerRadius)
    val density = LocalDensity.current
    val radiusPx = with(density) { cornerRadius.toPx() }

    if (skin == AppSkin.NEUMORPHIC || skin == AppSkin.CLASSIC_NEUMORPHIC) {
        return this
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFFEFECE2),
                    cornerRadius = CornerRadius(radiusPx, radiusPx)
                )
            }
            .drawBehind {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0x358C8374), Color(0xFFFFFFFF)),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(width = 1.2.dp.toPx())
                )
            }
    }

    var winOffset by remember { mutableStateOf(Offset.Zero) }
    val configuration = LocalConfiguration.current
    val fullSize = with(density) {
        Size(configuration.screenWidthDp.dp.toPx(), configuration.screenHeightDp.dp.toPx())
    }

    return this
        .onGloballyPositioned { winOffset = it.positionInWindow() }
        .clip(shape)
        .drawBehind {
            drawMesh(tokens, fullSize, winOffset)
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        tokens.tintConcave,
                        Color(0x35FFFFFF)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
        }
        .drawBehind {
            drawRoundRect(
                brush = Brush.linearGradient(listOf(tokens.borderHi, tokens.borderLo)),
                cornerRadius = CornerRadius(radiusPx, radiusPx),
                style = Stroke(width = 1.2.dp.toPx()),
            )
        }
}

// ═══ Overlay 变体（弹窗/底部面板用，自带完整 mesh） ═══

@Composable
fun Modifier.glassConvexOverlay(cornerRadius: Dp, tokens: GlassTokens = LocalGlassTokens.current): Modifier {
    var winOffset by remember { mutableStateOf(Offset.Zero) }
    val fullSize = with(LocalDensity.current) { Size(LocalConfiguration.current.screenWidthDp.dp.toPx(), LocalConfiguration.current.screenHeightDp.dp.toPx()) }
    val radiusPx = with(LocalDensity.current) { cornerRadius.toPx() }
    return this
        .onGloballyPositioned { winOffset = it.positionInWindow() }
        .drawBehind { drawRoundRect(color = tokens.outerShadow, topLeft = Offset(0f, 3.dp.toPx()), size = size, cornerRadius = CornerRadius(radiusPx, radiusPx)) }
        .clip(RoundedCornerShape(cornerRadius))
        .drawBehind {
            drawMesh(tokens, fullSize, winOffset)
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xD8FFFFFF), tokens.tintConvex, Color(0x95FFFFFF)),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                )
            )
        }
        .drawWithContent { drawContent(); drawRect(brush = Brush.verticalGradient(listOf(Color(0xFFFFFFFF), Color(0x00FFFFFF))), size = Size(size.width, 2.dp.toPx())) }
        .drawBehind { drawRoundRect(brush = Brush.linearGradient(listOf(tokens.borderHi, tokens.borderLo)), cornerRadius = CornerRadius(radiusPx, radiusPx), style = Stroke(1.5.dp.toPx())) }
}

@Composable
fun Modifier.glassConcaveOverlay(cornerRadius: Dp, tokens: GlassTokens = LocalGlassTokens.current): Modifier {
    var winOffset by remember { mutableStateOf(Offset.Zero) }
    val fullSize = with(LocalDensity.current) { Size(LocalConfiguration.current.screenWidthDp.dp.toPx(), LocalConfiguration.current.screenHeightDp.dp.toPx()) }
    val radiusPx = with(LocalDensity.current) { cornerRadius.toPx() }
    return this
        .onGloballyPositioned { winOffset = it.positionInWindow() }
        .clip(RoundedCornerShape(cornerRadius))
        .drawBehind { drawMesh(tokens, fullSize, winOffset); drawRect(tokens.tintConcave) }
        .drawBehind { drawRoundRect(brush = Brush.linearGradient(listOf(tokens.borderHi, tokens.borderLo)), cornerRadius = CornerRadius(radiusPx, radiusPx), style = Stroke(1.2.dp.toPx())) }
}

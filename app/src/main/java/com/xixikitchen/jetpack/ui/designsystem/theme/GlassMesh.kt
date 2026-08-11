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

enum class AppSkin { NEUMORPHIC, GLASS, CLASSIC_NEUMORPHIC }
val LocalAppSkin = staticCompositionLocalOf { AppSkin.GLASS }

// ═══ Mesh 绘制核心 ═══

/** 在 DrawScope 中绘制 mesh（以 origin 为坐标系偏移） */
private fun DrawScope.drawMesh(tokens: GlassTokens, fullSize: Size, origin: Offset) {
    drawRect(tokens.meshBase)
    val minDim = minOf(fullSize.width, fullSize.height)
    for (spot in tokens.meshSpots) {
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

// ═══ 全屏 Mesh 背景 ═══

@Composable
fun GlassMeshBackground(tokens: GlassTokens, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawBehind { drawMesh(tokens, fullSize = size, origin = Offset.Zero) }
    )
}

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

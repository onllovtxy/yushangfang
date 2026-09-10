package com.xixikitchen.jetpack.ui.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** mesh 单个光斑 */
data class MeshSpot(
    val xFraction: Float,
    val yFraction: Float,
    val color: Color,
    val radiusFraction: Float,
)

/**
 * 主题背景的画法。
 *
 * 角度统一约定:**0° = 从上往下,顺时针为正**。
 * 所以 90° 是从左往右,45° 是从左上到右下,135° 是从右上到左下。
 *
 * 坐标一律按"屏幕坐标系"算,再由调用方减去 origin —— 这样卡片里画出来的那一小片,
 * 和全屏背景上同一个位置的颜色能对上。
 */
sealed interface GlassBackground {
    /** 底色 + 若干径向光斑(默认主题的极光质感) */
    data class Mesh(val base: Color, val spots: List<MeshSpot>) : GlassBackground

    /** 线性渐变,两端平滑过渡(内置五套柔色主题) */
    data class Linear(val colors: List<Color>, val angleDegrees: Float) : GlassBackground

    /** 四分:从正中画一横一竖,均匀切成 2×2。顺序 [左上, 右上, 左下, 右下] */
    data class Quad(val colors: List<Color>) : GlassBackground

    /**
     * 任意角度的多色**平滑**渐变。
     *
     * [weights] 是相邻两个颜色之间的距离,长度 = 颜色数 - 1
     * (N 个颜色要在 [0,1] 上首尾对齐,只有 N-1 个自由度)。
     * 只有相对大小有意义,渲染时会归一化。
     */
    data class Angled(
        val colors: List<Color>,
        val angleDegrees: Float,
        val weights: List<Float>,
    ) : GlassBackground
}

/**
 * 玻璃令牌 —— 一套完整的玻璃态视觉参数。
 *
 * 强调色 [primary] 系列也放在这里:它必须跟着主题走,
 * 所以从原来写死的 GlassAccent 搬了进来(GlassAccent 现在只做转发)。
 */
data class GlassTokens(
    val background: GlassBackground,

    val primary: Color,
    val primaryBright: Color,
    val primaryDark: Color,

    val tintConvex: Color,
    val tintConcave: Color,
    val borderHi: Color,
    val borderLo: Color,
    val innerHighlight: Color,
    val outerShadow: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
)

/** 主题的代表色 —— Material colorScheme 的 background / surface 用它。 */
val GlassTokens.baseColor: Color
    get() = when (val bg = background) {
        is GlassBackground.Mesh -> bg.base
        is GlassBackground.Linear -> bg.colors.firstOrNull() ?: Color.White
        is GlassBackground.Quad -> bg.colors.firstOrNull() ?: Color.White
        is GlassBackground.Angled -> bg.colors.firstOrNull() ?: Color.White
    }

// ── 主色的透明度阶梯 ──
// 原来是四个写死的常量,换主题就得手动再配一遍。
// 现在从 primary 现算,任何主题都自动拥有一致的强调层次。
val GlassTokens.wash: Color get() = primary.copy(alpha = 0.08f)
val GlassTokens.washStrong: Color get() = primary.copy(alpha = 0.14f)
val GlassTokens.edge: Color get() = primary.copy(alpha = 0.16f)
val GlassTokens.stroke: Color get() = primary.copy(alpha = 0.30f)

val LocalGlassTokens = staticCompositionLocalOf { AppTheme.DEFAULT.tokens() }

package com.xixikitchen.jetpack.ui.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

/**
 * 强调色入口。
 *
 * 上半部分是**跟随主题**的:它们转发到 [LocalGlassTokens],所以全项目 130 多处
 * `GlassAccent.primary` 的调用点一行都不用改,换主题时自动生效。
 * 代价是它们只能在 @Composable 里读 —— 如果有非 Composable 的地方要用,
 * 编译器会直接报错,不会静默取到旧值。
 *
 * 下半部分是**语义色**,不随主题变:成功/警告/出错在任何配色下都该是同一个意思。
 */
object GlassAccent {
    // ── 跟随主题 ──
    val primary: Color
        @Composable @ReadOnlyComposable get() = LocalGlassTokens.current.primary

    val primaryBright: Color
        @Composable @ReadOnlyComposable get() = LocalGlassTokens.current.primaryBright

    val primaryDark: Color
        @Composable @ReadOnlyComposable get() = LocalGlassTokens.current.primaryDark

    /** 主色 8% —— 浅底上的选中/强调填充 */
    val wash: Color
        @Composable @ReadOnlyComposable get() = LocalGlassTokens.current.wash

    /** 主色 14% —— 稍重的强调块 */
    val washStrong: Color
        @Composable @ReadOnlyComposable get() = LocalGlassTokens.current.washStrong

    /** 主色 16% —— 分隔线、卡片描边 */
    val edge: Color
        @Composable @ReadOnlyComposable get() = LocalGlassTokens.current.edge

    /** 主色 30% —— 明确的轮廓 */
    val stroke: Color
        @Composable @ReadOnlyComposable get() = LocalGlassTokens.current.stroke

    // ── 语义色,固定 ──
    val success = Color(0xFF5EC498)      // 薄荷暖绿
    val warning = Color(0xFFFFB366)      // 暖温金
    val error = Color(0xFFFF6B6B)        // 珊瑚暖红

    // 浅色玻璃面上做文字/图标时的深色变体。success / error 本身太亮,
    // 压在浅色玻璃上对比度不够。
    val successDeep = Color(0xFF177A52)
    val errorDeep = Color(0xFFC0392B)
}

/**
 * 订单状态配色。
 *
 * 用低透明度"wash + 前景色"而不是实心色块 —— 实心色压在玻璃面上会把底下的
 * mesh/渐变完全盖死,底色始终应该透出来。
 */
data class GlassStatusColors(val wash: Color, val on: Color)

@Composable
@ReadOnlyComposable
fun orderStatusColors(status: Int): GlassStatusColors = when (status) {
    0 -> GlassStatusColors(Color(0x24D97724), GlassAccent.primaryDark)   // 待接单 · 琥珀
    1 -> GlassStatusColors(Color(0x243B82F6), Color(0xFF1D5FD0))         // 进行中 · 蓝
    2 -> GlassStatusColors(Color(0x2422A06B), Color(0xFF177A52))         // 已完成 · 绿
    3 -> GlassStatusColors(Color(0x24D9534E), Color(0xFFC0392B))         // 已拒绝 · 红
    else -> GlassStatusColors(Color(0x14000000), Color(0xFF6B7280))
}

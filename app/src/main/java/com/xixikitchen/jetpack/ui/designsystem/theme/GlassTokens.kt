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
 * 玻璃令牌 —— 一套完整的玻璃态视觉参数
 */
data class GlassTokens(
    val meshBase: Color,
    val meshSpots: List<MeshSpot>,
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

// ═══ 深色玻璃（炫彩蓝紫暗底 — 经典 Glassmorphism 极光） ═══
val DarkGlassTokens = GlassTokens(
    meshBase = Color(0xFF0F1320),  // 深邃蓝黑底
    meshSpots = listOf(
        MeshSpot(0.20f, 0.25f, Color(0x8C4A6EAA), 0.55f), // 左上：蓝紫
        MeshSpot(0.84f, 0.16f, Color(0x73786096), 0.52f), // 右上：灰紫
        MeshSpot(0.30f, 0.90f, Color(0x663C7878), 0.55f), // 左下：青绿
        MeshSpot(0.90f, 0.82f, Color(0x6B5A548C), 0.55f), // 右下：紫蓝
    ),
    tintConvex = Color(0x1FFFFFFF),  // 12% 白透
    tintConcave = Color(0x24000000), // 14% 黑透
    borderHi = Color(0x8CFFFFFF),    // 55% 白
    borderLo = Color(0x1FFFFFFF),    // 12% 白
    innerHighlight = Color(0x73FFFFFF), // 45% 白
    outerShadow = Color(0x47000000),    // 28% 黑
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xC7FFFFFF),
    textTertiary = Color(0x8CFFFFFF),
)

// ═══ 浅色色盘（米白极简通透 — 象牙暖香奶白） ═══
val LightGlassTokens = GlassTokens(
    meshBase = Color(0xFFF6F4EE),  // 高级象牙米白瓷底
    meshSpots = listOf(
        MeshSpot(0.12f, 0.12f, Color(0x66FFEED6), 0.80f), // 左上：暖香草晕
        MeshSpot(0.88f, 0.30f, Color(0x55F5EAD6), 0.75f), // 右中：温润奶茶晕
        MeshSpot(0.20f, 0.85f, Color(0x50EADCC6), 0.80f), // 左下：米白香膏晕
        MeshSpot(0.70f, 0.75f, Color(0x40FAF2E6), 0.85f), // 右下：清甜羊脂晕
    ),
    tintConvex = Color(0x85FFFFFF),  // 52% 霜白磨砂着色
    tintConcave = Color(0x18D97724), // 9% 暖金凹陷度
    borderHi = Color(0xFFFFFFFF),    // 100% 晶莹高光切边
    borderLo = Color(0x35D6CFC0),    // 21% 米褐阴影边缘
    innerHighlight = Color(0xC8FFFFFF), // 78% 顶部折射高亮光
    outerShadow = Color(0x358C8374), // 悬浮柔和暖米褐阴影
    textPrimary = Color(0xFF2C2824),   // 暖深木褐 / 暖玄
    textSecondary = Color(0xB32C2824),
    textTertiary = Color(0x802C2824),
)

val LocalGlassTokens = staticCompositionLocalOf { LightGlassTokens }

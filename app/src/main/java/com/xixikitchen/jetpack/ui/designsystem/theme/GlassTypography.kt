package com.xixikitchen.jetpack.ui.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 字号阶梯。
 *
 * 两条来自 Apple 字体排印的规则,之前两样都缺:
 *
 * 1. **tracking 随字号变化,不存在一个通用值。**
 *    字越大,字母之间的空隙在视觉上越明显,所以大标题要收紧(负值),
 *    小字要略微放开以保可读性。之前全站 letterSpacing = 0,
 *    于是 36sp 的标题散、10sp 的标签挤。
 *
 * 2. **leading 与字号反向。**
 *    大标题行高要紧,正文要松。之前只有 bodyLarge 设了 lineHeight,
 *    其余全靠字体默认 metrics,标题因此显得浮。
 *
 * 层级由 字重 × 字号 × 行高 三者共同建立,而不是只靠字号。
 */
val GlassTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,
        fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-0.7).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 32.sp, letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 29.sp, letterSpacing = (-0.4).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold,
        fontSize = 20.sp, lineHeight = 26.sp, letterSpacing = (-0.25).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp, letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 22.sp, letterSpacing = (-0.1).sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 21.sp, letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.2.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default, fontWeight = FontWeight.Medium,
        fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp
    ),
)

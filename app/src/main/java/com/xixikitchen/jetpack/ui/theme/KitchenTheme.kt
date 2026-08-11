package com.xixikitchen.jetpack.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassTheme

// Keep original color constants for backward compatibility
val CoralPink = Color(0xFFE8734A)
val PeachSunset = Color(0xFFF4A261)
val CoralDark = Color(0xFFA5422B)
val PrimaryLight = Color(0xFFFFE3D8)
val OatmealPage = Color(0xFFFFF5F0)
val WarmCream = Color(0xFFFFFCFA)
val TextDark = Color(0xFF2D211D)
val TextMuted = Color(0xFF806B63)
val MatchaMist = Color(0xFF819171)

@Composable
fun KitchenTheme(content: @Composable () -> Unit) {
    GlassTheme(content = content)
}

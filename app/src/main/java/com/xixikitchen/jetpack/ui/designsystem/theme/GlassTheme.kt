package com.xixikitchen.jetpack.ui.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.xixikitchen.jetpack.ui.designsystem.motion.LocalReducedMotion
import com.xixikitchen.jetpack.ui.designsystem.motion.rememberReducedMotion

@Composable
fun GlassTheme(
    tokens: GlassTokens,
    content: @Composable () -> Unit,
) {
    val reducedMotion = rememberReducedMotion()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // 不再给系统栏刷不透明底色。窗口已经是 edge-to-edge 的,
            // 主题背景自己会铺到状态栏/导航栏底下 —— 刷一层纯色反而会在
            // 屏幕顶端和底端留出一条和渐变对不上的硬边。
            // 六套主题都是浅色,所以状态栏/导航栏图标固定用深色。
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    CompositionLocalProvider(
        LocalGlassTokens provides tokens,
        LocalReducedMotion provides reducedMotion,
    ) {
        MaterialTheme(
            // 之前只覆盖了 primary/surface,色板里 secondary / tertiary / error /
            // outline 还是 Material 默认的那套紫红 —— 界面里任何一处用到它们都会串色。
            // 这里全部取当前主题的令牌,所以 Material 组件也会跟着换主题。
            colorScheme = MaterialTheme.colorScheme.copy(
                primary = tokens.primary,
                onPrimary = Color.White,
                primaryContainer = tokens.washStrong,
                onPrimaryContainer = tokens.primaryDark,
                secondary = tokens.primaryDark,
                onSecondary = Color.White,
                tertiary = tokens.primaryBright,
                onTertiary = Color.White,
                background = tokens.baseColor,
                onBackground = tokens.textPrimary,
                surface = tokens.baseColor,
                onSurface = tokens.textPrimary,
                surfaceVariant = tokens.baseColor,
                onSurfaceVariant = tokens.textSecondary,
                error = GlassAccent.error,
                onError = Color.White,
                outline = tokens.stroke,
                outlineVariant = tokens.edge,
            ),
            typography = GlassTypography,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                GlassMeshBackground(tokens)
                content()
            }
        }
    }
}

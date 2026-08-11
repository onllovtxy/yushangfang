package com.xixikitchen.jetpack.ui.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun GlassTheme(
    darkTheme: Boolean = false, // 厨房 app 默认浅色
    content: @Composable () -> Unit,
) {
    val tokens = if (darkTheme) DarkGlassTokens else LightGlassTokens

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = tokens.meshBase.toArgb()
            window.navigationBarColor = tokens.meshBase.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalGlassTokens provides tokens) {
        MaterialTheme(
            colorScheme = MaterialTheme.colorScheme.copy(
                background = tokens.meshBase,
                surface = tokens.meshBase,
                onBackground = tokens.textPrimary,
                onSurface = tokens.textPrimary,
                primary = GlassAccent.primary,
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

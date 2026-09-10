package com.xixikitchen.jetpack.ui.designsystem.motion

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * 系统是否要求"移除动画"。
 *
 * Compose 的 `animate*AsState` / `Animatable` 会通过协程上下文里的 MotionDurationScale
 * 自动尊重这个设置(缩放到 0 时直接跳变),但 `rememberInfiniteTransition` 走的是
 * `withFrameNanos`,**完全不看这个设置** —— 所以装饰性的无限循环动画必须手动关掉。
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

/**
 * 读取系统的动画时长缩放。同时监听变化,用户在设置里一改就即时生效。
 */
@Composable
fun rememberReducedMotion(): Boolean {
    val context = LocalContext.current
    var reduced by remember(context) { mutableStateOf(readReduceMotionFlag(context)) }

    DisposableEffect(context) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                reduced = readReduceMotionFlag(context)
            }
        }
        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose { context.contentResolver.unregisterContentObserver(observer) }
    }
    return reduced
}

/** ANIMATOR_DURATION_SCALE == 0 即系统"关闭动画"。读这个设置不需要任何权限。 */
private fun readReduceMotionFlag(context: Context): Boolean = try {
    Settings.Global.getFloat(
        context.contentResolver,
        Settings.Global.ANIMATOR_DURATION_SCALE,
        1f,
    ) == 0f
} catch (_: Exception) {
    false
}

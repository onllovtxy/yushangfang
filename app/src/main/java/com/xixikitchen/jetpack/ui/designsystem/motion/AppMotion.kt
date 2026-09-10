package com.xixikitchen.jetpack.ui.designsystem.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import kotlin.math.PI

/**
 * 缓动曲线。
 *
 * 内置的 FastOutSlowInEasing 太"软",缺少让动效显得果断的那一下收尾。
 * 这里用更强的自定义曲线:
 *  - [out]     进场/交互反馈。起步快 → 用户按下的一瞬间就看到响应。
 *  - [inOut]   屏幕上位移/形变。自然的加速 + 减速。
 *  - [drawer]  iOS 底部抽屉曲线(Ionic 出处)。
 *  - [standard] Material 标准曲线,仅用于纯色/无位移的颜色过渡。
 *
 * 永远不要对 UI 用 ease-in:它起步慢,会让你盯着看的那一瞬间"卡住",
 * 300ms 的 ease-in 体感比 300ms 的 ease-out 更慢。
 */
object AppEase {
    val out: Easing = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
    val inOut: Easing = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)
    val drawer: Easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
    val standard: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)
}

/**
 * 时长(毫秒)。UI 动效一律控制在 300ms 以内:
 * 180ms 的下拉就是比 400ms 的显得快,即使它们做的是同一件事。
 *
 * [exit] 比 [standard] 短 —— 退场永远比进场快。用户按下决定时值得慢,
 * 系统响应时永远要快(非对称节奏)。
 */
object AppDuration {
    const val press = 110        // 按压反馈
    const val quick = 160        // 小组件出场、tooltip
    const val standard = 220     // 弹层、卡片级过渡
    const val large = 320        // 抽屉、整屏面板
    const val exit = 140         // 退场(比进场短)
}

/**
 * 把 Apple 的弹簧参数映射成 Compose 的 (dampingRatio, stiffness)。
 *
 * Apple 用两个设计师能理解的量描述弹簧:
 *  - `dampingRatio` 1.0 = 临界阻尼,不过冲; <1.0 才会回弹。
 *  - `response`     到达目标所需的时间(秒),**不是** duration —— 弹簧没有固定时长。
 *
 * 物理上 stiffness = ω²,ω = 2π / response(mass 归一为 1)。
 */
fun <T> appleSpring(dampingRatio: Float = 1f, response: Float = 0.4f): SpringSpec<T> {
    val omega = (2.0 * PI / response).toFloat()
    return spring(dampingRatio = dampingRatio, stiffness = omega * omega)
}

/**
 * 默认 UI 弹簧。临界阻尼、不过冲 —— 优雅、不抢注意力。
 * 对应 Apple 的 `damping 1.0 / response 0.4`(移动、复位)。
 */
val SpringSettle: SpringSpec<Float> = appleSpring(dampingRatio = 1f, response = 0.4f)

/** 按压反馈:同样的临界阻尼,但更快到位,贴合手指抬起的速度。 */
val SpringPress: SpringSpec<Float> = appleSpring(dampingRatio = 1f, response = 0.25f)

/**
 * 手势弹簧。只有**手势本身带着动量**时才允许回弹 ——
 * 甩一下、抛出去、松手带着速度,过冲才显得对;
 * 一个只是淡入弹出的菜单过冲就是错的。
 * 对应 Apple 的 `damping 0.8 / response 0.35`(抽屉、旋转)。
 */
val SpringGesture: SpringSpec<Float> = appleSpring(dampingRatio = 0.8f, response = 0.35f)

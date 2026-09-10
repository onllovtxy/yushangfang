package com.xixikitchen.jetpack.ui.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.abs

/** 六种渐变方式。 */
enum class GradientKind(val label: String) {
    VERTICAL("上下"),
    HORIZONTAL("左右"),
    QUAD("四分"),
    DIAGONAL_2("对角2份"),
    DIAGONAL_3("对角3份"),
    DIAGONAL_4("对角4份");

    /** 这种画法需要填几个色值 */
    val colorCount: Int
        get() = when (this) {
            VERTICAL, HORIZONTAL, DIAGONAL_2 -> 2
            QUAD, DIAGONAL_4 -> 4
            DIAGONAL_3 -> 3
        }

    /** 对角分带才需要角度和每段比例 */
    val isDiagonal: Boolean
        get() = this == DIAGONAL_2 || this == DIAGONAL_3 || this == DIAGONAL_4

    companion object {
        fun fromName(name: String?): GradientKind =
            entries.firstOrNull { it.name == name } ?: VERTICAL
    }
}

/**
 * 用户自建的主题。
 *
 * 强调色不单独让用户填 —— 从渐变色里推:取饱和度最高的那个当色相来源,
 * 压到能撑住白字的亮度。自建主题不该让人再配一遍一整套色板。
 */
data class CustomTheme(
    val id: String,
    val name: String,
    val kind: GradientKind,
    val colors: List<Color>,
    val angleDegrees: Float = 45f,
    val weights: List<Float> = emptyList(),
) {
    fun toBackground(): GlassBackground = when (kind) {
        GradientKind.VERTICAL -> GlassBackground.Linear(colors.fit(2), 0f)
        GradientKind.HORIZONTAL -> GlassBackground.Linear(colors.fit(2), 90f)
        GradientKind.QUAD -> GlassBackground.Quad(colors.fit(4))
        else -> GlassBackground.Angled(
            colors = colors.fit(kind.colorCount),
            angleDegrees = angleDegrees,
            weights = weights,
        )
    }

    fun toTokens(): GlassTokens {
        val palette = colors.fit(kind.colorCount)
        val (primary, bright, dark) = deriveAccent(palette)
        val hue = toHsl(palette.maxByOrNull { toHsl(it)[1] } ?: primary)[0]
        val text = fromHsl(hue, 0.12f, 0.17f)
        return lightTheme(
            background = toBackground(),
            primary = primary,
            primaryBright = bright,
            primaryDark = dark,
            textPrimary = text,
            borderLo = fromHsl(hue, 0.10f, 0.62f).copy(alpha = 0x36 / 255f),
            outerShadow = fromHsl(hue, 0.10f, 0.55f).copy(alpha = 0x38 / 255f),
        )
    }

    /** 相邻色值之间的距离个数 —— N 个颜色只有 N-1 段。 */
    val gapCount: Int get() = (kind.colorCount - 1).coerceAtLeast(0)

    /**
     * 相邻色值之间的距离。长度 = 颜色数 - 1。
     * 第一个颜色永远在 0、最后一个永远在 1,所以最后一个输入框没有意义,不再展示。
     */
    fun gaps(): List<Float> = List(gapCount) { i -> (weights.getOrNull(i) ?: 1f).coerceAtLeast(0.01f) }
}

private fun List<Color>.fit(n: Int): List<Color> =
    List(n) { i -> getOrNull(i) ?: lastOrNull() ?: Color.White }

// ═══ 强调色推导 ═══

/**
 * 从渐变色里挑一个"最有颜色"的当种子,推出主色三档。
 * 亮度压到 0.42 是为了让白色文字压在上面还有 4.5:1 左右的对比度 ——
 * 按钮上的"登录""立即下单"都是白字。
 */
private fun deriveAccent(colors: List<Color>): Triple<Color, Color, Color> {
    val best = colors.maxByOrNull { toHsl(it)[1] }
    val saturation = best?.let { toHsl(it)[1] } ?: 0f
    // 整套都是灰阶时给一组中性强调色,免得出一个莫名其妙的红
    if (best == null || saturation < 0.08f) {
        return Triple(Color(0xFF5A5A5A), Color(0xFF808080), Color(0xFF3C3C3C))
    }
    val hue = toHsl(best)[0]
    return Triple(
        fromHsl(hue, 0.48f, 0.42f),
        fromHsl(hue, 0.48f, 0.57f),
        fromHsl(hue, 0.55f, 0.32f),
    )
}

/** 返回 [色相 0..360, 饱和度 0..1, 亮度 0..1] */
internal fun toHsl(color: Color): FloatArray {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val l = (max + min) / 2f
    if (max == min) return floatArrayOf(0f, 0f, l)
    val d = max - min
    val s = if (l > 0.5f) d / (2f - max - min) else d / (max + min)
    val h = when (max) {
        r -> ((g - b) / d + if (g < b) 6f else 0f)
        g -> ((b - r) / d + 2f)
        else -> ((r - g) / d + 4f)
    } * 60f
    return floatArrayOf(h, s, l)
}

internal fun fromHsl(hue: Float, saturation: Float, lightness: Float): Color {
    val c = (1f - abs(2f * lightness - 1f)) * saturation
    val hp = (((hue % 360f) + 360f) % 360f) / 60f
    val x = c * (1f - abs(hp % 2f - 1f))
    val (r1, g1, b1) = when (hp.toInt()) {
        0 -> Triple(c, x, 0f)
        1 -> Triple(x, c, 0f)
        2 -> Triple(0f, c, x)
        3 -> Triple(0f, x, c)
        4 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    val m = lightness - c / 2f
    return Color(r1 + m, g1 + m, b1 + m)
}

// ═══ 落盘 ═══

/** 存 JSON 用的扁平结构 —— 颜色存成 "#RRGGBB",改配置文件时肉眼可读。 */
data class CustomThemeDto(
    val id: String,
    val name: String,
    val kind: String,
    val colors: List<String>,
    val angle: Float = 45f,
    val weights: List<Float> = emptyList(),
)

internal fun Color.toHexString(): String = "#%06X".format(0xFFFFFF and toArgb())

internal fun parseHexColor(hex: String): Color? {
    val cleaned = hex.trim().removePrefix("#")
    if (cleaned.length != 6) return null
    val value = cleaned.toLongOrNull(16) ?: return null
    return Color(0xFF000000L or value)
}

fun CustomTheme.toDto(): CustomThemeDto = CustomThemeDto(
    id = id,
    name = name,
    kind = kind.name,
    colors = colors.map { it.toHexString() },
    angle = angleDegrees,
    weights = weights,
)

fun CustomThemeDto.toModel(): CustomTheme = CustomTheme(
    id = id,
    name = name.ifBlank { "我的主题" },
    kind = GradientKind.fromName(kind),
    colors = colors.mapNotNull { parseHexColor(it) }.ifEmpty { listOf(Color.White, Color(0xFFDDDDDD)) },
    angleDegrees = angle,
    weights = weights,
)

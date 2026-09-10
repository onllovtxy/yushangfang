package com.xixikitchen.jetpack.ui.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * 内置主题。
 *
 * [DEFAULT] 是原来那套暖金米白(底色 + 四个径向光斑的极光质感);
 * 其余五套来自柔色渐变配色,背景是竖向两色渐变。
 *
 * [id] 是落盘用的稳定标识 —— 不要改,改了老用户的已选主题会丢。
 */
enum class AppTheme(val id: String, val label: String) {
    DEFAULT("default", "暖金米白"),
    COOL_BLUE("cool_blue", "清冷蓝"),
    GENTLE_PINK("gentle_pink", "温柔粉"),
    SEA_SALT("sea_salt", "静谧海盐"),
    CHERRY("cherry", "清新落樱"),
    PEACH("peach", "甜美蜜桃");

    companion object {
        fun fromId(id: String?): AppTheme = entries.firstOrNull { it.id == id } ?: DEFAULT
    }
}

/** 一个可用的主题:内置六套 + 用户自建的若干套。 */
data class ResolvedTheme(
    val id: String,
    val label: String,
    val tokens: GlassTokens,
    /** 非空表示这是用户自建的主题 —— 只有它能被长按删除/编辑。 */
    val custom: CustomTheme? = null,
)

/**
 * 按 id 解析出真正要用的主题。自定义优先于内置(两者 id 空间不重叠)。
 * 找不到就回落到默认 —— 比如自定义主题被删掉之后,已选 id 就成了悬空值。
 */
fun resolveTheme(selectedId: String?, customs: List<CustomTheme>): ResolvedTheme {
    customs.firstOrNull { it.id == selectedId }?.let {
        return ResolvedTheme(id = it.id, label = it.name, tokens = it.toTokens(), custom = it)
    }
    val builtIn = AppTheme.fromId(selectedId)
    return ResolvedTheme(builtIn.id, builtIn.label, builtIn.tokens(), custom = null)
}

/**
 * 七套主题共用的玻璃质感推导:
 * 霜白凸起面 + 主色凹陷 + 顶部折射高光 + 主题色投影。
 * 只有背景画法和强调色不同,玻璃的手感保持一致 —— 换主题不该换掉材质。
 */
internal fun lightTheme(
    background: GlassBackground,
    primary: Color,
    primaryBright: Color,
    primaryDark: Color,
    textPrimary: Color,
    borderLo: Color,
    outerShadow: Color,
): GlassTokens = GlassTokens(
    background = background,
    primary = primary,
    primaryBright = primaryBright,
    primaryDark = primaryDark,
    tintConvex = Color(0x85FFFFFF),
    tintConcave = primary.copy(alpha = 0.09f),
    borderHi = Color(0xFFFFFFFF),
    borderLo = borderLo,
    innerHighlight = Color(0xC8FFFFFF),
    outerShadow = outerShadow,
    textPrimary = textPrimary,
    textSecondary = textPrimary.copy(alpha = 0.70f),
    textTertiary = textPrimary.copy(alpha = 0.50f),
)

fun AppTheme.tokens(): GlassTokens = when (this) {
    // ═══ 默认:暖金米白 · 极光 mesh ═══
    AppTheme.DEFAULT -> lightTheme(
        background = GlassBackground.Mesh(
            base = Color(0xFFF6F4EE),
            spots = listOf(
                MeshSpot(0.12f, 0.12f, Color(0x66FFEED6), 0.80f), // 左上:暖香草晕
                MeshSpot(0.88f, 0.30f, Color(0x55F5EAD6), 0.75f), // 右中:温润奶茶晕
                MeshSpot(0.20f, 0.85f, Color(0x50EADCC6), 0.80f), // 左下:米白香膏晕
                MeshSpot(0.70f, 0.75f, Color(0x40FAF2E6), 0.85f), // 右下:清甜羊脂晕
            ),
        ),
        primary = Color(0xFFD97724),
        primaryBright = Color(0xFFEAA052),
        primaryDark = Color(0xFFB35D15),
        textPrimary = Color(0xFF2C2824),
        borderLo = Color(0x35D6CFC0),
        outerShadow = Color(0x358C8374),
    )

    // ═══ 清冷蓝 · 霜洁 #F9FFFF → 雪莹 #DBF3FF ═══
    AppTheme.COOL_BLUE -> lightTheme(
        background = GlassBackground.Linear(
            colors = listOf(Color(0xFFF9FFFF), Color(0xFFDBF3FF)),
            angleDegrees = 0f,
        ),
        primary = Color(0xFF367DA1),
        primaryBright = Color(0xFF5BA4C8),
        primaryDark = Color(0xFF225E7D),
        textPrimary = Color(0xFF262D31),
        borderLo = Color(0x3694A1A8),
        outerShadow = Color(0x38819098),
    )

    // ═══ 温柔粉 · 象牙白 #FFFEF8 → 云桃粉 #FBBCBC ═══
    AppTheme.GENTLE_PINK -> lightTheme(
        background = GlassBackground.Linear(
            colors = listOf(Color(0xFFFFFEF8), Color(0xFFFBBCBC)),
            angleDegrees = 0f,
        ),
        primary = Color(0xFFAF4B4E),
        primaryBright = Color(0xFFC5777A),
        primaryDark = Color(0xFF8A3336),
        textPrimary = Color(0xFF312626),
        borderLo = Color(0x36A89495),
        outerShadow = Color(0x38988182),
    )

    // ═══ 静谧海盐 · 月苍白 #F9F6E5 → 海盐蓝 #B6D4E9 ═══
    AppTheme.SEA_SALT -> lightTheme(
        background = GlassBackground.Linear(
            colors = listOf(Color(0xFFF9F6E5), Color(0xFFB6D4E9)),
            angleDegrees = 0f,
        ),
        primary = Color(0xFF486E93),
        primaryBright = Color(0xFF6C91B7),
        primaryDark = Color(0xFF325272),
        textPrimary = Color(0xFF262B31),
        borderLo = Color(0x36949EA8),
        outerShadow = Color(0x38818C98),
    )

    // ═══ 清新落樱 · 云母白 #F5F5F5 → 落樱粉 #FBD2D2 ═══
    AppTheme.CHERRY -> lightTheme(
        background = GlassBackground.Linear(
            colors = listOf(Color(0xFFF5F5F5), Color(0xFFFBD2D2)),
            angleDegrees = 0f,
        ),
        primary = Color(0xFFB04F6F),
        primaryBright = Color(0xFFC57D95),
        primaryDark = Color(0xFF8B3753),
        textPrimary = Color(0xFF31262A),
        borderLo = Color(0x36A8949B),
        outerShadow = Color(0x38988188),
    )

    // ═══ 甜美蜜桃 · 雪山白 #F8F8F8 → 蜜桃粉 #FDB2BD ═══
    AppTheme.PEACH -> lightTheme(
        background = GlassBackground.Linear(
            colors = listOf(Color(0xFFF8F8F8), Color(0xFFFDB2BD)),
            angleDegrees = 0f,
        ),
        primary = Color(0xFFBE5637),
        primaryBright = Color(0xFFD17B61),
        primaryDark = Color(0xFF963D22),
        textPrimary = Color(0xFF312926),
        borderLo = Color(0x36A89994),
        outerShadow = Color(0x38988681),
    )
}

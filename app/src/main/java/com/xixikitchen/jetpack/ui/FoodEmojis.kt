package com.xixikitchen.jetpack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FOOD_EMOJIS = listOf(
    "🍇", "🍈", "🍉", "🍊", "🍋", "🍌", "🍍", "🥭", "🍎", "🍏", "🍐", "🍑", "🍒", "🍓", "🫐", "🥝",
    "🍅", "🫒", "🥥", "🥑", "🍆", "🥔", "🥕", "🌽", "🌶️", "🫑", "🥒", "🥬", "🥦", "🧄", "🧅", "🍄",
    "🥜", "🫘", "🌰", "🫚", "🫛", "🍞", "🥐", "🥖", "🫓", "🥨", "🥯", "🥞", "🧇", "🧀", "🍖",
    "🍗", "🥩", "🥓", "🍔", "🍟", "🍕", "🌭", "🥪", "🌮", "🌯", "🫔", "🍳", "🥘", "🍲", "🫕", "🥣",
    "🥗", "🍿", "🧈", "🧂", "🥫", "🍱", "🍘", "🍙", "🍚", "🍛", "🍜", "🍝", "🍠", "🍢", "🍣",
    "🍤", "🍥", "🥮", "🍡", "🥟", "🥠", "🥡", "🦀", "🦞", "🦐", "🦑", "🦪", "🍦", "🍧", "🍨", "🍩",
    "🍪", "🎂", "🍰", "🧁", "🥧", "🍫", "🍬", "🍭", "🍮", "🍯", "🥛", "☕", "🫖", "🍵", "🍶", "🍾",
    "🍷", "🍸", "🍹", "🍺", "🍻", "🥂", "🥃", "🥤", "🧋", "🧃"
)

fun getFallbackFoodEmoji(dishId: Long, dishName: String?): String {
    val seed = (dishName?.hashCode() ?: dishId.hashCode()).let { if (it < 0) -it else it }
    return FOOD_EMOJIS[(seed % FOOD_EMOJIS.size).toInt()]
}

@Composable
fun FallbackFoodAvatar(
    dishId: Long,
    dishName: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    emojiSize: TextUnit = 28.sp,
    cornerRadius: Dp = 16.dp
) {
    val emoji = getFallbackFoodEmoji(dishId, dishName)
    val seed = (dishName?.hashCode() ?: dishId.hashCode()).let { if (it < 0) -it else it }
    
    // 暖色系回退底色 —— 跟琥珀主色同一族。
    // 原来第一位是 0xFFFFF0F2(淡粉),是上一版粉色主题留下的,和现在的暖金主题打架。
    val bgColors = listOf(
        Color(0xFFFFF5EE), // Peach Cream
        Color(0xFFFFF0E3), // Warm Apricot
        Color(0xFFFFF8DC), // Cornsilk
        Color(0xFFFAF0E6), // Linen
        Color(0xFFFFF9E6), // Light Yellow Cream
        Color(0xFFF3F7EC), // Light Honeydew
        Color(0xFFF0F5F2), // Mint Green
        Color(0xFFF1F6FA), // Alice Blue
        Color(0xFFEAF9FF)  // Soft Aqua
    )
    val bgColor = bgColors[(seed % bgColors.size).toInt()]

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = emojiSize
        )
    }
}

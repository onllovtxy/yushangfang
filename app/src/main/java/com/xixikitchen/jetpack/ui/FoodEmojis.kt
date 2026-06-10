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
    "🥜", "🫘", "🌰", "🫚", "🫛", "🍞", "🥐", "🥖", "🫓", "🥨", "🥯", "🥞", " waffle", "🧀", "🍖",
    "🍗", "🥩", "🥓", "🍔", "🍟", "🍕", "🌭", "🥪", "🌮", "🌯", "🫔", "🍳", "🥘", "🍲", "🫕", "🥣",
    "🥗", "🍿", "🧈", "🧂", "🥫", "🍱", "🍘", "🍙", "🍚", "🍛", "🍜", "🍝", "🍠", "🍢", "🍣",
    "🍤", "🍥", "🥮", "🍡", "🥟", "🥠", "🥡", "🦀", "🦞", "🦐", "🦑", "🦪", "🍦", "🍧", "🍨", "🍩",
    "🍪", "🎂", "🍰", "🧁", "🥧", "🍫", "🍬", "🍭", "🍮", "🍯", "🥛", "☕", "🫖", "🍵", "🍶", "🍾",
    "🍷", "🍸", "🍹", "🍺", "🍻", "🥂", "🥃", "🥤", "🧋", "🧃"
)

fun getFallbackFoodEmoji(dishId: Long, dishName: String?): String {
    val seed = (dishName?.hashCode() ?: dishId.hashCode()).let { if (it < 0) -it else it }
    // Clean emojis to ensure single emoji output
    val cleanedEmojis = FOOD_EMOJIS.map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("waffle") }
    val realEmojis = cleanedEmojis + "🧇"
    return realEmojis[(seed % realEmojis.size).toInt()]
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
    
    // Vibrant pastel gradient-style colors for xixi's kitchen
    val bgColors = listOf(
        Color(0xFFFFF0F2), // Pale Pink
        Color(0xFFFFF5EE), // Peach Cream
        Color(0xFFF0FFF0), // Light Honeydew
        Color(0xFFF5FFFA), // Mint Green
        Color(0xFFF0F8FF), // Alice Blue
        Color(0xFFFFF8DC), // Cornsilk
        Color(0xFFFFF5F5), // Misty Rose
        Color(0xFFFAF0E6), // Linen
        Color(0xFFFFF9E6), // Light Yellow Cream
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

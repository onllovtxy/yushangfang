package com.xixikitchen.jetpack.ui.screens.orderdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.xixikitchen.jetpack.data.OrderItem
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex

@Composable
fun OrderDishItemCard(
    item: OrderItem,
    realImageUrl: (String?) -> String?,
    fallbackFoodAvatar: @Composable (Long, String?, androidx.compose.ui.unit.Dp, Modifier, androidx.compose.ui.unit.TextUnit, androidx.compose.ui.unit.Dp) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassConvex(22.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val model = realImageUrl(item.dishImage)
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(GlassAccent.primary.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                        .border(BorderStroke(1.dp, GlassAccent.primary.copy(alpha = 0.12f)), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!model.isNullOrBlank()) {
                        AsyncImage(
                            model = model,
                            contentDescription = item.dishName,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        fallbackFoodAvatar(item.dishId, item.dishName, 70.dp, Modifier, 30.sp, 18.dp)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "本单菜品",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GlassAccent.primaryDark
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.dishName ?: "未知菜品",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = tokens.textPrimary
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "为你认真准备的味道",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .background(GlassAccent.primary.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 11.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "× ${item.quantity}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = GlassAccent.primary
                    )
                }
            }
        }
    }
}

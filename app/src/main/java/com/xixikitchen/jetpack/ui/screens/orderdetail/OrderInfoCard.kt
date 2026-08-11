package com.xixikitchen.jetpack.ui.screens.orderdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xixikitchen.jetpack.data.Order
import com.xixikitchen.jetpack.data.User
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex

@Composable
fun OrderInfoCard(
    order: Order,
    fromUser: User?,
    toUser: User?,
    statusText: (Int) -> String,
    userMiniAvatar: @Composable (User?) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassTokens.current
    val statusColor = when (order.status) {
        0 -> GlassAccent.primary
        1 -> GlassAccent.primaryDark
        2 -> GlassAccent.success
        3 -> GlassAccent.error
        else -> tokens.textSecondary
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .glassConvex(24.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassAccent.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "订单详情",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = tokens.textPrimary
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "NO. ${order.orderNo}",
                        style = MaterialTheme.typography.labelMedium,
                        color = tokens.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(50))
                        .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.22f)), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = statusText(order.status),
                        color = statusColor,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Column(Modifier.padding(18.dp)) {
                Text("下单时间", style = MaterialTheme.typography.labelMedium, color = tokens.textSecondary)
                Spacer(Modifier.height(4.dp))
                Text(
                    order.createdAt ?: "--",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = tokens.textPrimary
                )

                Spacer(Modifier.height(18.dp))
                Text("成员流转", style = MaterialTheme.typography.labelMedium, color = tokens.textSecondary)
                Spacer(Modifier.height(9.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GlassAccent.primary.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                        .padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        userMiniAvatar(fromUser)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            fromUser?.nickname ?: "未知成员",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = tokens.textPrimary
                        )
                        Text("下单人", style = MaterialTheme.typography.labelSmall, color = tokens.textSecondary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = GlassAccent.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text("送达心意", style = MaterialTheme.typography.labelSmall, color = GlassAccent.primaryDark)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        userMiniAvatar(toUser)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            toUser?.nickname ?: "未知接单人",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = tokens.textPrimary
                        )
                        Text("烹饪人", style = MaterialTheme.typography.labelSmall, color = tokens.textSecondary)
                    }
                }

                if (!order.remark.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(GlassAccent.primary.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .border(BorderStroke(1.dp, GlassAccent.primary.copy(alpha = 0.18f)), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            "订单备注",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = GlassAccent.primaryDark
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = order.remark.orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.textPrimary
                        )
                    }
                }
            }
        }
    }
}

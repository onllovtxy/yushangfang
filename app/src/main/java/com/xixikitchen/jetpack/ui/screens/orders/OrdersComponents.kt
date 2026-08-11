package com.xixikitchen.jetpack.ui.screens.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.xixikitchen.jetpack.data.Order
import com.xixikitchen.jetpack.data.User
import com.xixikitchen.jetpack.ui.KitchenUiState
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave

@Composable
fun StatusFilterChip(
    active: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = modifier
            .then(
                if (active) Modifier.glassConvex(16.dp)
                else Modifier.glassConcave(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) GlassAccent.primary else tokens.textSecondary,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium
            )
        )
    }
}

@Composable
fun OrderCard(
    order: Order,
    state: KitchenUiState,
    onOpen: () -> Unit,
    onAction: (String) -> Unit,
    statusText: (Int) -> String,
    userMiniAvatar: @Composable (User?) -> Unit,
    realImageUrl: (String?) -> String?,
    fallbackFoodAvatar: @Composable (Long, String?, androidx.compose.ui.unit.Dp, Modifier, TextUnit, androidx.compose.ui.unit.Dp) -> Unit
) {
    val tokens = LocalGlassTokens.current
    val statusColor = when (order.status) {
        0 -> GlassAccent.primary
        1 -> GlassAccent.primaryDark
        2 -> GlassAccent.success
        3 -> GlassAccent.error
        else -> tokens.textSecondary
    }
    val fromUser = state.recipients.firstOrNull { it.id == order.fromUserId }
        ?: (if (state.user?.id == order.fromUserId) state.user else null)
    val toUser = state.recipients.firstOrNull { it.id == order.toUserId }
        ?: (if (state.user?.id == order.toUserId) state.user else null)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassConvex(24.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onOpen)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GlassAccent.primary.copy(alpha = 0.08f))
                    .padding(horizontal = 18.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ORDER  ·  ${order.orderNo.takeLast(6)}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = GlassAccent.primaryDark
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = order.createdAt ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = tokens.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(50))
                        .border(BorderStroke(1.dp, statusColor.copy(alpha = 0.24f)), RoundedCornerShape(50))
                        .padding(horizontal = 11.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = statusText(order.status),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }
            }

            Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                OrderStatusTimeline(order.status, statusColor)

                if (order.items.isNotEmpty()) {
                    Spacer(Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            order.items.take(4).forEach { item ->
                                val imageUrl = realImageUrl(item.dishImage)
                                if (!imageUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = item.dishName,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(GlassAccent.primary.copy(alpha = 0.08f))
                                            .border(
                                                BorderStroke(1.dp, GlassAccent.primary.copy(alpha = 0.14f)),
                                                RoundedCornerShape(14.dp)
                                            ),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    fallbackFoodAvatar(item.dishId, item.dishName, 48.dp, Modifier, 23.sp, 14.dp)
                                }
                            }
                            if (order.items.size > 4) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(GlassAccent.primary.copy(alpha = 0.08f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "+${order.items.size - 4}",
                                        color = GlassAccent.primaryDark,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("本单餐点", style = MaterialTheme.typography.labelSmall, color = tokens.textSecondary)
                            Text(
                                text = "${order.items.sumOf { it.quantity }} 份",
                                color = tokens.textPrimary,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                TicketDivider()
                Spacer(Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    userMiniAvatar(fromUser)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("下单人", style = MaterialTheme.typography.labelSmall, color = tokens.textSecondary)
                        Text(
                            fromUser?.nickname ?: "未知成员",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = tokens.textPrimary
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = GlassAccent.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    userMiniAvatar(toUser)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("烹饪人", style = MaterialTheme.typography.labelSmall, color = tokens.textSecondary)
                        Text(
                            toUser?.nickname ?: "未知接单人",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = tokens.textPrimary
                        )
                    }
                }

                val currentUserId = state.user?.id ?: -1
                if (order.toUserId == currentUserId && (order.status == 0 || order.status == 1)) {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (order.status == 0) {
                            OutlinedButton(
                                onClick = { onAction("reject") },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, GlassAccent.primary.copy(alpha = 0.32f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GlassAccent.primaryDark)
                            ) { Text("拒绝") }
                            Spacer(Modifier.width(10.dp))
                            Button(
                                onClick = { onAction("accept") },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
                            ) { Text("接单", fontWeight = FontWeight.Bold) }
                        } else if (order.status == 1) {
                            Button(
                                onClick = { onAction("complete") },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primaryDark)
                            ) { Text("完成烹饪", fontWeight = FontWeight.Bold) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderStatusTimeline(status: Int, statusColor: Color) {
    val tokens = LocalGlassTokens.current
    val labels = listOf("已下单", "烹饪中", if (status == 3) "已拒绝" else "已完成")
    val activeStep = when (status) {
        0 -> 0
        1 -> 1
        2, 3 -> 2
        else -> 0
    }
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        labels.forEachIndexed { index, label ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (index > 0) {
                        Box(
                            Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(if (index <= activeStep) statusColor.copy(alpha = 0.6f) else GlassAccent.primary.copy(alpha = 0.12f))
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(if (index == activeStep) 13.dp else 9.dp)
                            .background(if (index <= activeStep) statusColor else GlassAccent.primary.copy(alpha = 0.14f), CircleShape)
                            .border(
                                BorderStroke(2.dp, if (index == activeStep) statusColor.copy(alpha = 0.2f) else Color.Transparent),
                                CircleShape
                            )
                    )
                    if (index < labels.lastIndex) {
                        Box(
                            Modifier
                                .weight(1f)
                                .height(2.dp)
                                .background(if (index < activeStep) statusColor.copy(alpha = 0.6f) else GlassAccent.primary.copy(alpha = 0.12f))
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (index == activeStep) statusColor else tokens.textSecondary,
                    fontWeight = if (index == activeStep) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun TicketDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        repeat(22) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(GlassAccent.primary.copy(alpha = 0.22f))
            )
        }
    }
}

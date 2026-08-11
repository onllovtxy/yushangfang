package com.xixikitchen.jetpack.ui.screens.orderdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex

@Composable
fun OrderDetailActions(
    orderId: Long,
    orderStatus: Int,
    onAction: (Long, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassTokens.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassConvex(20.dp)
            .padding(14.dp)
    ) {
        if (orderStatus == 0) {
            Text(
                text = "等待你的决定",
                color = GlassAccent.primaryDark,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onAction(orderId, "reject") },
                    shape = RoundedCornerShape(15.dp),
                    border = BorderStroke(1.dp, GlassAccent.primary.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GlassAccent.primaryDark,
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("拒绝接单", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { onAction(orderId, "accept") },
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("接受订单", fontWeight = FontWeight.Bold)
                }
            }
        } else if (orderStatus == 1) {
            Text(
                text = "餐点准备好了吗？",
                color = GlassAccent.primaryDark,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { onAction(orderId, "complete") },
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primaryDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("完成烹饪", fontWeight = FontWeight.Bold)
            }
        }
    }
}

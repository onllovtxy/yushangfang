package com.xixikitchen.jetpack.ui.screens.mine

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex

@Composable
fun StatCard(
    label: String,
    value: Int
) {
    val tokens = LocalGlassTokens.current
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .glassConvex(18.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = value.toString().padStart(2, '0'),
            color = GlassAccent.primaryDark,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = label,
            color = tokens.textSecondary,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

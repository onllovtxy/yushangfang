package com.xixikitchen.jetpack.ui

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.xixikitchen.jetpack.ui.designsystem.theme.CustomTheme
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.GradientKind
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcaveOverlay
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.designsystem.theme.parseHexColor
import com.xixikitchen.jetpack.ui.designsystem.theme.themePreviewBackground
import com.xixikitchen.jetpack.ui.designsystem.theme.toHexString
import java.util.UUID

// 默认值直接取内置主题里对比最明显的那几个色 ——
// 一打开弹窗就能看出这是一条渐变，而不是一片接近白的平色。
private val DEFAULT_COLORS = listOf("#FFFEF8", "#FBBCBC", "#F9F6E5", "#B6D4E9")

/** 四分模式下四个角的名字,让"颜色 1..4"落到具体位置上 */
private val QUAD_SLOTS = listOf("左上", "右上", "左下", "右下")

/**
 * 自定义主题编辑弹窗。新建和编辑共用 —— [initial] 为 null 就是新建。
 *
 * 预览用的是**真正的主题渲染逻辑**(themePreviewBackground),不是另画的示意图,
 * 所以预览什么样,保存之后切过去就是什么样。
 */
@Composable
fun CustomThemeDialog(
    initial: CustomTheme?,
    onDismiss: () -> Unit,
    onSave: (CustomTheme) -> Unit,
) {
    val tokens = LocalGlassTokens.current
    val isEditing = initial != null
    val newId = remember { "custom_${UUID.randomUUID()}" }

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var kind by remember { mutableStateOf(initial?.kind ?: GradientKind.VERTICAL) }
    // 输入框固定备 4 个槽,按当前画法取前 N 个 —— 切换画法时已经填过的颜色不会丢
    var colorInputs by remember {
        mutableStateOf(
            List(4) { i ->
                initial?.colors?.getOrNull(i)?.toHexString() ?: DEFAULT_COLORS[i]
            }
        )
    }
    var angleInput by remember { mutableStateOf((initial?.angleDegrees ?: 45f).toTrimmed()) }
    var weightInputs by remember {
        mutableStateOf(List(4) { i -> (initial?.weights?.getOrNull(i) ?: 1f).toTrimmed() })
    }

    val needed = kind.colorCount
    // N 个颜色在渐变上首尾对齐,只有 N-1 个自由度 —— 所以间距输入框是 needed-1 个
    val gapCount = (needed - 1).coerceAtLeast(0)
    val parsedColors = colorInputs.map { parseHexColor(it) }
    val colorsValid = (0 until needed).all { parsedColors[it] != null }
    val gaps = weightInputs.take(gapCount).map { it.toFloatOrNull() }
    val gapsValid = !kind.isDiagonal || gaps.all { it != null && it > 0f }
    val angleValid = !kind.isDiagonal || (angleInput.toFloatOrNull()?.let { it in 0f..360f } == true)
    val canSave = name.isNotBlank() && colorsValid && gapsValid && angleValid

    val draft = CustomTheme(
        // 新建时把 id 记住,不要每次重组都换一个 UUID
        id = initial?.id ?: newId,
        name = name.ifBlank { "未命名" },
        kind = kind,
        colors = List(needed) { parsedColors[it] ?: Color.White },
        angleDegrees = angleInput.toFloatOrNull() ?: 45f,
        weights = if (kind.isDiagonal) {
            List(gapCount) { gaps.getOrNull(it) ?: 1f }
        } else {
            emptyList()
        },
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.glassConvexOverlay(24.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                text = if (isEditing) "编辑主题" else "自定义主题",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 430.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── 效果预览 ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .themePreviewBackground(draft.toTokens())
                        .border(BorderStroke(1.dp, GlassAccent.edge), RoundedCornerShape(16.dp))
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("主题名字") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = glassFieldColors()
                )

                // ── 渐变方式 ──
                Text(
                    text = "渐变方式",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = tokens.textSecondary
                )
                GradientKind.entries.chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { option ->
                            KindChip(
                                label = option.label,
                                selected = option == kind,
                                onClick = { kind = option },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // ── 色值 ──
                Text(
                    text = if (kind == GradientKind.QUAD) "四个角的色值" else "色值（$needed 个）",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = tokens.textSecondary
                )
                repeat(needed) { index ->
                    ColorInputRow(
                        label = if (kind == GradientKind.QUAD) QUAD_SLOTS[index] else "颜色 ${index + 1}",
                        value = colorInputs[index],
                        parsed = parsedColors[index],
                        onValueChange = { next ->
                            colorInputs = colorInputs.toMutableList().also { it[index] = next }
                        }
                    )
                }

                // ── 对角专属:角度 + 每段比例 ──
                if (kind.isDiagonal) {
                    Text(
                        text = "角度（0° 从上往下，顺时针为正）",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = tokens.textSecondary
                    )
                    OutlinedTextField(
                        value = angleInput,
                        onValueChange = { angleInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("角度 0 ~ 360") },
                        singleLine = true,
                        isError = !angleValid,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = glassFieldColors()
                    )

                    if (gapCount > 0) {
                        Text(
                            text = "色值之间的距离（相对值，会自动归一化）",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = tokens.textSecondary
                        )
                        repeat(gapCount) { index ->
                            OutlinedTextField(
                                value = weightInputs[index],
                                onValueChange = { next ->
                                    weightInputs = weightInputs.toMutableList().also { it[index] = next }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("颜色 ${index + 1} → ${index + 2} 的距离") },
                                singleLine = true,
                                isError = gaps.getOrNull(index)?.let { it <= 0f } ?: true,
                                shape = RoundedCornerShape(16.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = glassFieldColors()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(draft) },
                enabled = canSave,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = tokens.textSecondary)
            }
        }
    )
}

@Composable
private fun KindChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = modifier
            .noRipplePressable(pressedScale = 0.95f, onClick = onClick)
            .then(
                if (selected) Modifier.glassConvexOverlay(14.dp)
                else Modifier.glassConcaveOverlay(14.dp)
            )
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) GlassAccent.primary else tokens.textPrimary,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun ColorInputRow(
    label: String,
    value: String,
    parsed: Color?,
    onValueChange: (String) -> Unit,
) {
    val tokens = LocalGlassTokens.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(parsed ?: Color.Transparent)
                .border(BorderStroke(1.dp, GlassAccent.edge), CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (parsed == null) "$label（#RRGGBB）" else label) },
            singleLine = true,
            isError = parsed == null,
            shape = RoundedCornerShape(16.dp),
            supportingText = if (parsed == null) {
                { Text("色值格式不对，要 #RRGGBB", color = GlassAccent.error, style = MaterialTheme.typography.bodySmall) }
            } else {
                null
            },
            colors = glassFieldColors()
        )
    }
}

@Composable
private fun glassFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedBorderColor = GlassAccent.primary,
    focusedLabelColor = GlassAccent.primary,
    cursorColor = GlassAccent.primary,
)

private fun Float.toTrimmed(): String =
    if (this == toInt().toFloat()) toInt().toString() else toString()

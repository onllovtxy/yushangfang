package com.xixikitchen.jetpack.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xixikitchen.jetpack.data.CoupleMood
import com.xixikitchen.jetpack.ui.DiscoverUiState
import com.xixikitchen.jetpack.ui.noRipplePressable
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import java.util.Calendar
import java.util.TimeZone

/**
 * 情侣每日心情。
 *
 * 一个日期格里上下两枚表情：上=他（方形底），下=她（圆形底）。
 * 点某天弹出编辑面板，选心情 + 写一句话，保存后覆盖当天的记录。
 */
enum class MoodFace(
    val code: String,
    val emoji: String,
    val label: String,
    val color: Long
) {
    HAPPY("happy", "🥳", "开心", 0xFFF6C453),
    CALM("calm", "😌", "平静", 0xFF8CCF7E),
    LOVE("love", "😍", "心动", 0xFFF2839C),
    EXCITED("excited", "🤩", "兴奋", 0xFFF5B942),
    SAD("sad", "🥲", "伤心", 0xFF9AA0A6),
    ANGRY("angry", "😡", "生气", 0xFFE4572E),
    TIRED("tired", "😮‍💨", "心累", 0xFF7FB2E5),
    ANXIOUS("anxious", "😰", "烦躁", 0xFFA87CD1);

    companion object {
        fun fromCode(code: String?): MoodFace =
            entries.firstOrNull { it.code == code } ?: CALM
    }
}

@Composable
internal fun MoodsContent(
    state: DiscoverUiState,
    currentUserId: Long,
    partnerId: Long?,
    onLoadMonth: (year: Int, month: Int) -> Unit,
    onSaveMood: (date: String, code: String, note: String) -> Unit
) {
    val tokens = LocalGlassTokens.current
    val now = remember { Calendar.getInstance() }
    var year by remember { mutableStateOf(now.get(Calendar.YEAR)) }
    var month by remember { mutableStateOf(now.get(Calendar.MONTH)) }
    var editingDate by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(year, month) { onLoadMonth(year, month) }

    val mine = remember(state.moods, currentUserId) {
        state.moods.filter { it.userId == currentUserId }.associateBy { it.moodDate }
    }
    val theirs = remember(state.moods, partnerId, currentUserId) {
        state.moods.filter { it.userId != currentUserId }.associateBy { it.moodDate }
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 月份导航 + 图例
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .noRipplePressable(pressedScale = 0.9f) {
                                if (month == 0) {
                                    month = 11; year -= 1
                                } else month -= 1
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ChevronLeft, "上个月",
                            tint = GlassAccent.primary, modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "${year}年${month + 1}月",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = tokens.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .noRipplePressable(pressedScale = 0.9f) {
                                if (month == 11) {
                                    month = 0; year += 1
                                } else month += 1
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ChevronRight, "下个月",
                            tint = GlassAccent.primary, modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 图例：方的是我、圆的是 TA
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(GlassAccent.primary.copy(alpha = 0.25f))
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "方的是我", style = MaterialTheme.typography.labelSmall,
                        color = tokens.textSecondary
                    )
                    Spacer(Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(GlassAccent.primary.copy(alpha = 0.25f))
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "圆的是 TA", style = MaterialTheme.typography.labelSmall,
                        color = tokens.textSecondary
                    )
                }
            }
        }

        // 日历网格
        item {
            MoodMonthGrid(
                year = year,
                month = month,
                mine = mine,
                theirs = theirs,
                onPickDate = { editingDate = it }
            )
        }

        item { Spacer(Modifier.height(24.dp)) }
    }

    editingDate?.let { date ->
        val existingMine = mine[date]
        MoodEditorDialog(
            date = date,
            initial = existingMine,
            partnerMood = theirs[date],
            onDismiss = { editingDate = null },
            onSave = { code, note ->
                editingDate = null
                onSaveMood(date, code, note)
            }
        )
    }
}

@Composable
private fun MoodMonthGrid(
    year: Int,
    month: Int,
    mine: Map<String, CoupleMood>,
    theirs: Map<String, CoupleMood>,
    onPickDate: (String) -> Unit
) {
    val tokens = LocalGlassTokens.current
    val today = remember {
        Calendar.getInstance().let {
            "%04d-%02d-%02d".format(
                it.get(Calendar.YEAR), it.get(Calendar.MONTH) + 1, it.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
    val cells = remember(year, month) { buildMoodCells(year, month) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glassConvex(20.dp)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary
                )
            }
        }

        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                week.forEach { day ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (day == null) {
                            Spacer(Modifier.height(64.dp))
                        } else {
                            val dateKey = "%04d-%02d-%02d".format(year, month + 1, day)
                            MoodDayCell(
                                day = day,
                                isToday = dateKey == today,
                                myMood = mine[dateKey],
                                partnerMood = theirs[dateKey],
                                onClick = { onPickDate(dateKey) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodDayCell(
    day: Int,
    isToday: Boolean,
    myMood: CoupleMood?,
    partnerMood: CoupleMood?,
    onClick: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .noRipplePressable(pressedScale = 0.94f, onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal
            ),
            color = if (isToday) GlassAccent.primary else tokens.textSecondary
        )

        // 上：我（方形）
        MoodFaceBadge(
            mood = myMood,
            shape = MoodShape.SQUARE,
            sizeDp = 24
        )
        // 下：TA（圆形）
        MoodFaceBadge(
            mood = partnerMood,
            shape = MoodShape.CIRCLE,
            sizeDp = 24
        )
    }
}

private enum class MoodShape { SQUARE, CIRCLE }

@Composable
private fun MoodFaceBadge(
    mood: CoupleMood?,
    shape: MoodShape,
    sizeDp: Int
) {
    val face = MoodFace.fromCode(mood?.moodCode)
    val bg = Color(face.color).copy(alpha = if (mood == null) 0.06f else 0.22f)
    val clipShape = if (shape == MoodShape.SQUARE) RoundedCornerShape(6.dp) else CircleShape
    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .clip(clipShape)
            .background(bg)
            .border(1.dp, Color(face.color).copy(alpha = if (mood == null) 0.15f else 0.5f), clipShape),
        contentAlignment = Alignment.Center
    ) {
        if (mood != null) {
            Text(text = face.emoji, fontSize = (sizeDp * 0.58f).sp)
        }
    }
}

@Composable
private fun MoodEditorDialog(
    date: String,
    initial: CoupleMood?,
    partnerMood: CoupleMood?,
    onDismiss: () -> Unit,
    onSave: (code: String, note: String) -> Unit
) {
    val tokens = LocalGlassTokens.current
    var selected by remember { mutableStateOf(MoodFace.fromCode(initial?.moodCode)) }
    var note by remember { mutableStateOf(initial?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(320.dp)
            .glassConvexOverlay(24.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "记录下你的心情吧~",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassAccent.primaryDark
                )
                Text(
                    date,
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = selected.emoji,
                    fontSize = 46.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // 3 列 x 3 行情绪网格
                MoodFace.entries.chunked(4).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { face ->
                            val active = face == selected
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .noRipplePressable(pressedScale = 0.94f) { selected = face }
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (active) Color(face.color).copy(alpha = 0.28f)
                                        else Color(face.color).copy(alpha = 0.10f)
                                    )
                                    .border(
                                        1.dp,
                                        if (active) Color(face.color) else Color.Transparent,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(face.emoji, fontSize = 22.sp)
                                Text(
                                    face.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Normal
                                    ),
                                    color = tokens.textPrimary
                                )
                            }
                        }
                        // 不足一行时补空位，保持列宽一致
                        repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 500) note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("输入当下经历与心情") },
                    shape = RoundedCornerShape(14.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )

                if (partnerMood != null) {
                    val partnerFace = MoodFace.fromCode(partnerMood.moodCode)
                    Text(
                        text = "TA 这天是 ${partnerFace.emoji} ${partnerFace.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.textSecondary
                    )
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .noRipplePressable(pressedScale = 0.97f) { onSave(selected.code, note.trim()) }
                    .glassConvexOverlay(16.dp)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "确定",
                    color = GlassAccent.primaryDark,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = tokens.textSecondary)
            }
        }
    )
}

/** 生成当月格子，周一为每周第一天，前后补 null 对齐 7 列。 */
private fun buildMoodCells(year: Int, month: Int): List<Int?> {
    val cal = Calendar.getInstance()
    cal.clear()
    cal.set(year, month, 1)
    val offset = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val days = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val cells = MutableList<Int?>(offset) { null }
    for (d in 1..days) cells.add(d)
    while (cells.size % 7 != 0) cells.add(null)
    return cells
}

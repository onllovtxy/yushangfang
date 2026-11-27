package com.xixikitchen.jetpack.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.xixikitchen.jetpack.data.CoupleMeetup
import com.xixikitchen.jetpack.data.CoupleMeetupSaveRequest
import com.xixikitchen.jetpack.ui.DiscoverUiState
import com.xixikitchen.jetpack.ui.DiscoverViewModel
import com.xixikitchen.jetpack.ui.components.realImageUrl
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcaveOverlay
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.noRippleCombinedPressable
import com.xixikitchen.jetpack.ui.noRipplePressable
import com.xixikitchen.jetpack.ui.readAndCompressImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
internal fun MeetupsContent(
    state: DiscoverUiState,
    onMeetupActionRequest: (CoupleMeetup) -> Unit,
    onPhotoClick: (imageUrl: String, caption: String?) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(state.meetups, key = { "meetup_${it.id}" }) { item ->
            MeetupSketchCard(
                item = item,
                onLongClick = { onMeetupActionRequest(item) },
                onPhotoClick = onPhotoClick
            )
        }

        item { Spacer(Modifier.height(96.dp)) }
    }
}

/**
 * 严格遵循手稿的「上文字 · 分割线 · 下照片（双图拍立得并排 / 单图大图）」见面纪念卡片。
 */
@Composable
private fun MeetupSketchCard(
    item: CoupleMeetup,
    onLongClick: () -> Unit,
    onPhotoClick: (imageUrl: String, caption: String?) -> Unit
) {
    val tokens = LocalGlassTokens.current
    val photos = remember(item.photoUrl1, item.photoUrl2) {
        listOf(item.photoUrl1, item.photoUrl2).filter { it.isNotBlank() }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleCombinedPressable(
                pressedScale = 0.985f,
                onLongClick = onLongClick,
                onClick = {}
            )
            .glassConvex(22.dp)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── 上半区：日期 + 小记 ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassConcave(16.dp)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = item.arriveDate.replace('-', '.'),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = tokens.textPrimary
                        )
                        if (item.city.isNotBlank()) {
                            Text(
                                text = "📍 ${item.city}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GlassAccent.primaryDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(GlassAccent.washStrong, RoundedCornerShape(999.dp))
                                .padding(horizontal = 9.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "相伴 ${item.stayDays} 天",
                                color = GlassAccent.primaryDark,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold)
                            )
                        }
                        val dashColor = GlassAccent.primary
                        Canvas(
                            modifier = Modifier
                                .width(52.dp)
                                .height(2.dp)
                        ) {
                            drawLine(
                                color = dashColor,
                                start = Offset(0f, size.height / 2f),
                                end = Offset(size.width, size.height / 2f),
                                strokeWidth = size.height,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 7f), 0f)
                            )
                        }
                    }

                    Text(
                        text = item.leaveDate.replace('-', '.'),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = tokens.textPrimary
                    )
                }
            }

            // 文字小记
            if (item.note.isNotBlank()) {
                Text(
                    text = item.note,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = tokens.textPrimary,
                    lineHeight = 21.sp
                )
            }

            // ── 中间分割线 + 下半区照片（1 张大图 / 2 张拍立得同框并排） ──
            if (photos.isNotEmpty()) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = GlassAccent.edge
                )

                val photoCaption = buildString {
                    append("${item.arriveDate} ~ ${item.leaveDate}")
                    if (item.city.isNotBlank()) append(" · ${item.city}")
                    if (item.note.isNotBlank()) append(" · ${item.note}")
                }

                if (photos.size == 1) {
                    MeetupPolaroidPhoto(
                        imageUrl = photos[0],
                        badge = null,
                        aspectRatio = 4f / 3f,
                        onClick = { onPhotoClick(photos[0], photoCaption) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        photos.take(2).forEach { url ->
                            MeetupPolaroidPhoto(
                                imageUrl = url,
                                badge = null,
                                aspectRatio = 3f / 4f,
                                onClick = { onPhotoClick(url, photoCaption) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MeetupPolaroidPhoto(
    imageUrl: String,
    badge: String?,
    aspectRatio: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resolvedUrl = realImageUrl(imageUrl) ?: imageUrl
    val frameShape = RoundedCornerShape(16.dp)
    val innerShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .noRipplePressable(pressedScale = 0.98f, onClick = onClick)
            .clip(frameShape)
            .background(Color.White.copy(alpha = 0.86f))
            .border(BorderStroke(1.dp, Color.White), frameShape)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .clip(innerShape)
                .background(GlassAccent.wash)
        ) {
            AsyncImage(
                model = resolvedUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            if (!badge.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.48f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MeetupEditorDialog(
    initial: CoupleMeetup?,
    vm: DiscoverViewModel,
    onDismiss: () -> Unit,
    onSave: (CoupleMeetupSaveRequest) -> Unit
) {
    val tokens = LocalGlassTokens.current
    val todayStr = remember { formatMeetupDateString(Date()) }
    var arriveDate by remember(initial) {
        mutableStateOf(initial?.arriveDate?.takeIf { it.isNotBlank() } ?: todayStr)
    }
    var leaveDate by remember(initial) {
        mutableStateOf(initial?.leaveDate?.takeIf { it.isNotBlank() } ?: todayStr)
    }
    var city by remember(initial) { mutableStateOf(initial?.city ?: "") }
    var note by remember(initial) { mutableStateOf(initial?.note ?: "") }
    var photoUrl1 by remember(initial) { mutableStateOf(initial?.photoUrl1 ?: "") }
    var photoUrl2 by remember(initial) { mutableStateOf(initial?.photoUrl2 ?: "") }
    var uploading1 by remember { mutableStateOf(false) }
    var uploading2 by remember { mutableStateOf(false) }

    var showRangePicker by remember { mutableStateOf(false) }

    val dateInvalid = remember(arriveDate, leaveDate) {
        leaveDate < arriveDate
    }
    val stayDays = remember(arriveDate, leaveDate) {
        val start = parseMeetupUtcMillis(arriveDate)
        val end = parseMeetupUtcMillis(leaveDate)
        if (start == null || end == null || end < start) 1L else (end - start) / 86_400_000L + 1L
    }

    AlertDialog(
        onDismissRequest = {
            if (!uploading1 && !uploading2) onDismiss()
        },
        modifier = Modifier
            .width(280.dp)
            .glassConvexOverlay(24.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                text = if (initial == null) "记一次见面" else "编辑见面记录",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                MeetupDateRangeBox(
                    arriveDate = arriveDate,
                    leaveDate = leaveDate,
                    stayDays = stayDays,
                    onClick = { showRangePicker = true }
                )

                if (dateInvalid) {
                    Text(
                        text = "分别日期不能早于见面日期哦",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = GlassAccent.error
                    )
                }

                OutlinedTextField(
                    value = city,
                    onValueChange = { if (it.length <= 60) city = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("见面城市 / 地点") },
                    placeholder = { Text("例如：杭州 ⇄ 上海") },
                    shape = RoundedCornerShape(14.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 500) note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("文字记录") },
                    placeholder = { Text("写下那天的心情...") },
                    shape = RoundedCornerShape(14.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )

                Text(
                    text = "见面照片",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = tokens.textSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MeetupPhotoUploadSlot(
                        value = photoUrl1,
                        label = "照片 1",
                        uploading = uploading1,
                        vm = vm,
                        onUploadingChange = { uploading1 = it },
                        onValueChange = { photoUrl1 = it },
                        modifier = Modifier.weight(1f)
                    )
                    MeetupPhotoUploadSlot(
                        value = photoUrl2,
                        label = "照片 2",
                        uploading = uploading2,
                        vm = vm,
                        onUploadingChange = { uploading2 = it },
                        onValueChange = { photoUrl2 = it },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        CoupleMeetupSaveRequest(
                            arriveDate = arriveDate,
                            leaveDate = leaveDate,
                            city = city.trim(),
                            note = note.trim(),
                            photoUrl1 = photoUrl1.trim(),
                            photoUrl2 = photoUrl2.trim()
                        )
                    )
                },
                enabled = !dateInvalid && !uploading1 && !uploading2,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !uploading1 && !uploading2
            ) {
                Text("取消", color = tokens.textSecondary)
            }
        }
    )

    if (showRangePicker) {
        MeetupRangePickerDialog(
            initialArrive = arriveDate,
            initialLeave = leaveDate,
            onDismiss = { showRangePicker = false },
            onConfirm = { start, end ->
                arriveDate = start
                leaveDate = end
                showRangePicker = false
            }
        )
    }
}

/** 紧凑版区间日历：只有月份导航 + 星期行 + 6 行日期，整体比系统 DateRangePicker 矮一截。 */
@Composable
private fun MeetupRangePickerDialog(
    initialArrive: String,
    initialLeave: String,
    onDismiss: () -> Unit,
    onConfirm: (arrive: String, leave: String) -> Unit
) {
    val tokens = LocalGlassTokens.current
    val initialStart = remember { parseMeetupUtcMillis(initialArrive) }
    val initialEnd = remember { parseMeetupUtcMillis(initialLeave) }
    var startMillis by remember { mutableStateOf(initialStart) }
    var endMillis by remember { mutableStateOf(initialEnd) }

    val anchor = initialStart ?: System.currentTimeMillis()
    var year by remember { mutableStateOf(meetupUtcYear(anchor)) }
    var month by remember { mutableStateOf(meetupUtcMonth(anchor)) }

    val todayMillis = remember {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        meetupUtcMillisOf(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
    }
    val cells = remember(year, month) { buildMeetupMonthCells(year, month) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .width(304.dp)
            .glassConvexOverlay(24.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                text = "选择见面区间",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // 月份导航
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .noRipplePressable(pressedScale = 0.9f) {
                                if (month == 0) {
                                    month = 11
                                    year -= 1
                                } else {
                                    month -= 1
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "上个月",
                            tint = GlassAccent.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "${year}年${month + 1}月",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = tokens.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .noRipplePressable(pressedScale = 0.9f) {
                                if (month == 11) {
                                    month = 0
                                    year += 1
                                } else {
                                    month += 1
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "下个月",
                            tint = GlassAccent.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // 星期表头（周一开头）
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

                // 日期网格
                cells.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        week.forEach { cell ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (cell != null) {
                                    val isStart = startMillis == cell.millis
                                    val isEnd = endMillis == cell.millis
                                    val inRange = startMillis != null && endMillis != null &&
                                        cell.millis > startMillis!! && cell.millis < endMillis!!
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .then(
                                                when {
                                                    isStart || isEnd -> Modifier.background(GlassAccent.primary)
                                                    inRange -> Modifier.background(GlassAccent.washStrong)
                                                    else -> Modifier
                                                }
                                            )
                                            .noRipplePressable(pressedScale = 0.9f) {
                                                val s = startMillis
                                                val e = endMillis
                                                when {
                                                    s == null || e != null -> {
                                                        startMillis = cell.millis
                                                        endMillis = null
                                                    }
                                                    cell.millis < s -> startMillis = cell.millis
                                                    else -> endMillis = cell.millis
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cell.day.toString(),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = if (isStart || isEnd) FontWeight.ExtraBold else FontWeight.Medium
                                            ),
                                            color = when {
                                                isStart || isEnd -> Color.White
                                                inRange -> GlassAccent.primaryDark
                                                cell.millis == todayMillis -> GlassAccent.primary
                                                else -> tokens.textPrimary
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 已选区间摘要
                val s = startMillis
                val e = endMillis
                Text(
                    text = if (s == null) {
                        "点选开始与结束日期"
                    } else {
                        val days = ((e ?: s) - s) / 86_400_000L + 1L
                        "${formatMeetupUtcMillis(s)} → ${formatMeetupUtcMillis(e ?: s)} · $days 天"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (s == null) tokens.textSecondary else GlassAccent.primaryDark
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = startMillis ?: return@Button
                    onConfirm(formatMeetupUtcMillis(s), formatMeetupUtcMillis(endMillis ?: s))
                },
                enabled = startMillis != null,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("确定", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = tokens.textSecondary)
            }
        }
    )
}

private data class MeetupDayCell(val millis: Long, val day: Int)

private fun meetupUtcMillisOf(year: Int, month: Int, day: Int): Long {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.clear()
    cal.set(year, month, day)
    return cal.timeInMillis
}

private fun meetupUtcYear(millis: Long): Int {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = millis
    return cal.get(Calendar.YEAR)
}

private fun meetupUtcMonth(millis: Long): Int {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.timeInMillis = millis
    return cal.get(Calendar.MONTH)
}

/** 生成当月格子，周一为每周第一天，前后补 null 对齐 7 列。 */
private fun buildMeetupMonthCells(year: Int, month: Int): List<MeetupDayCell?> {
    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.clear()
    cal.set(year, month, 1)
    // Calendar.DAY_OF_WEEK: 周日=1 … 周六=7，转成 周一=0 … 周日=6
    val offset = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val cells = MutableList<MeetupDayCell?>(offset) { null }
    for (day in 1..daysInMonth) {
        cells.add(MeetupDayCell(meetupUtcMillisOf(year, month, day), day))
    }
    while (cells.size % 7 != 0) cells.add(null)
    return cells
}

@Composable
private fun MeetupDateRangeBox(
    arriveDate: String,
    leaveDate: String,
    stayDays: Long,
    onClick: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .noRipplePressable(pressedScale = 0.98f, onClick = onClick)
            .glassConcaveOverlay(16.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "见面 ~ 分别",
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary
                )
                Text(
                    text = "${arriveDate.replace('-', '.')} → ${leaveDate.replace('-', '.')}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassAccent.primary
                )
            }
            Box(
                modifier = Modifier
                    .background(GlassAccent.washStrong, RoundedCornerShape(999.dp))
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "$stayDays 天",
                    color = GlassAccent.primaryDark,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}

@Composable
private fun MeetupPhotoUploadSlot(
    value: String,
    label: String,
    uploading: Boolean,
    vm: DiscoverViewModel,
    onUploadingChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        onUploadingChange(true)
        scope.launch {
            try {
                val processed = withContext(Dispatchers.IO) { readAndCompressImage(context, uri) }
                if (processed == null) {
                    onUploadingChange(false)
                    return@launch
                }
                val uploadedUrl = vm.uploadImageBlocking(
                    bytes = processed.bytes,
                    fileName = "meetup_${System.currentTimeMillis()}.${processed.extension}"
                )
                onValueChange(uploadedUrl)
            } catch (e: Exception) {
                vm.reportError(e)
            } finally {
                onUploadingChange(false)
            }
        }
    }

    val shape = RoundedCornerShape(16.dp)
    val resolvedUrl = realImageUrl(value)

    Box(
        modifier = modifier
            .height(84.dp)
            .noRipplePressable(enabled = !uploading, pressedScale = 0.97f) {
                launcher.launch("image/*")
            }
            .clip(shape)
            .background(GlassAccent.wash)
            .border(BorderStroke(1.dp, GlassAccent.stroke), shape),
        contentAlignment = Alignment.Center
    ) {
        if (!resolvedUrl.isNullOrBlank()) {
            AsyncImage(
                model = resolvedUrl,
                contentDescription = label,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "更换照片",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            // 右上角移除按钮
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .noRipplePressable(pressedScale = 0.9f) { onValueChange("") },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "移除照片",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = label,
                    tint = GlassAccent.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = label,
                    color = GlassAccent.primary,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        if (uploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.78f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = GlassAccent.primary,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun formatMeetupDateString(date: Date): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(date)
}

private fun parseMeetupUtcMillis(dateStr: String): Long? {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return runCatching { sdf.parse(dateStr)?.time }.getOrNull()
}

private fun formatMeetupUtcMillis(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return sdf.format(Date(millis))
}

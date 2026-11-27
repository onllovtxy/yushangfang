package com.xixikitchen.jetpack.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.tween
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xixikitchen.jetpack.data.CoupleMeetup
import com.xixikitchen.jetpack.data.CoupleSpace
import com.xixikitchen.jetpack.ui.DiscoverUiState
import com.xixikitchen.jetpack.ui.DiscoverViewModel
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.rememberCoroutineScope
import com.xixikitchen.jetpack.ui.components.CardImageExporter
import com.xixikitchen.jetpack.ui.components.ConfirmDeleteDialog
import com.xixikitchen.jetpack.ui.components.PhotoLightboxDialog
import com.xixikitchen.jetpack.ui.components.SuperAdminSpaceBanner
import com.xixikitchen.jetpack.ui.components.rememberPullRefreshState
import com.xixikitchen.jetpack.ui.designsystem.motion.AppDuration
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcaveOverlay
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.noRipplePressable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

enum class DiaryTab(val label: String) {
    Meetup("见面"),
    Mood("每日心情")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetupDiaryScreen(
    state: DiscoverUiState,
    vm: DiscoverViewModel,
    isSuperAdmin: Boolean = false,
    currentSpace: CoupleSpace? = null,
    allSpaces: List<CoupleSpace> = emptyList(),
    onSelectSpace: (Long) -> Unit = {},
    fabSignal: Int = 0
) {
    val tokens = LocalGlassTokens.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val (isPullRefreshing, startPullRefresh) = rememberPullRefreshState(state.loading)

    val onSaveImage: (CoupleMeetup) -> Unit = remember(context, state.meetups) {
        { meetup ->
            scope.launch {
                val photos = withContext(Dispatchers.IO) {
                    listOf(meetup.photoUrl1, meetup.photoUrl2)
                        .filter { it.isNotBlank() }
                        .mapNotNull { loadBitmap(it) }
                }
                val bitmap = withContext(Dispatchers.Default) {
                    CardImageExporter.renderMeetup(context, meetup, photos)
                }
                val saved = withContext(Dispatchers.IO) {
                    CardImageExporter.saveToGallery(
                        context,
                        bitmap,
                        "见面_第${meetup.meetIndex}次_${meetup.arriveDate}"
                    )
                }
                photos.forEach { it.recycle() }
                bitmap.recycle()
                android.widget.Toast.makeText(
                    context,
                    if (saved) "已保存到相册" else "保存失败，请检查存储空间",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    var subTab by rememberSaveable { mutableStateOf(DiaryTab.Meetup) }
    var showCreateMeetup by remember { mutableStateOf(false) }
    var editingMeetup by remember { mutableStateOf<CoupleMeetup?>(null) }
    var meetupActionTarget by remember { mutableStateOf<CoupleMeetup?>(null) }
    var deleteMeetupTarget by remember { mutableStateOf<CoupleMeetup?>(null) }
    var meetupLightbox by remember { mutableStateOf<Pair<String, String?>?>(null) }

    LaunchedEffect(subTab) {
        when (subTab) {
            DiaryTab.Meetup -> vm.refreshMeetups()
            DiaryTab.Mood -> Unit // 由 MoodsContent 自己按年月拉取
        }
    }

    // 新建按钮：进入页面 / 点击日记 Tab 时出现，5 秒无操作自动隐藏
    var showAddFab by remember { mutableStateOf(true) }
    LaunchedEffect(fabSignal) {
        if (fabSignal > 0) showAddFab = true
    }
    LaunchedEffect(showAddFab) {
        if (showAddFab) {
            kotlinx.coroutines.delay(5_000)
            showAddFab = false
        }
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = Color.Transparent
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isPullRefreshing,
            onRefresh = {
                startPullRefresh()
                vm.refreshMeetups()
            },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                if (isSuperAdmin) {
                    SuperAdminSpaceBanner(
                        isSuperAdmin = true,
                        currentSpace = currentSpace,
                        spaces = allSpaces,
                        onSelectSpace = onSelectSpace,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                // 顶部两个切换按钮：见面 / 每日心情
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DiaryTab.entries.forEach { tab ->
                        val active = subTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .noRipplePressable(pressedScale = 0.96f) { subTab = tab }
                                .then(
                                    if (active) Modifier.glassConvex(16.dp)
                                    else Modifier.glassConcave(16.dp)
                                )
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.label,
                                color = if (active) GlassAccent.primary else tokens.textSecondary,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp)
                ) {
                    when (subTab) {
                        DiaryTab.Meetup -> MeetupsContent(
                            state = state,
                            onMeetupActionRequest = { meetupActionTarget = it },
                            onPhotoClick = { url, caption -> meetupLightbox = url to caption }
                        )
                        DiaryTab.Mood -> MoodsContent(
                            state = state,
                            currentUserId = state.currentUser?.id ?: 0L,
                            partnerId = state.currentUser?.partnerId,
                            onLoadMonth = { y, m -> vm.refreshMoods(y, m) },
                            onSaveMood = { date, code, note ->
                                vm.saveMood(date, code, note)
                            }
                        )
                    }

                    androidx.compose.animation.AnimatedVisibility(
                        visible = showAddFab && subTab == DiaryTab.Meetup,
                        enter = fadeIn(tween(AppDuration.quick)) + scaleIn(
                            animationSpec = tween(AppDuration.quick),
                            initialScale = 0.85f
                        ),
                        exit = fadeOut(tween(AppDuration.exit)) + scaleOut(
                            animationSpec = tween(AppDuration.exit),
                            targetScale = 0.85f
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 28.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(GlassAccent.primary, CircleShape)
                                .noRipplePressable(pressedScale = 0.92f) {
                                    showAddFab = false
                                    showCreateMeetup = true
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "记一次见面",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreateMeetup || editingMeetup != null) {
        MeetupEditorDialog(
            initial = editingMeetup,
            vm = vm,
            onDismiss = {
                showCreateMeetup = false
                editingMeetup = null
            },
            onSave = { req ->
                val targetId = editingMeetup?.id
                vm.saveMeetup(targetId, req) {
                    showCreateMeetup = false
                    editingMeetup = null
                }
            }
        )
    }

    meetupActionTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { meetupActionTarget = null },
            modifier = Modifier.glassConvexOverlay(24.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.Transparent,
            title = {
                Text(
                    "第 ${target.meetIndex} 次见面",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassAccent.primaryDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${target.arriveDate} ~ ${target.leaveDate}" +
                            (if (target.city.isNotBlank()) " · ${target.city}" else ""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.textSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .noRipplePressable(pressedScale = 0.96f) {
                                    editingMeetup = target
                                    meetupActionTarget = null
                                }
                                .glassConcaveOverlay(14.dp)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "编辑",
                                color = tokens.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .noRipplePressable(pressedScale = 0.96f) {
                                    deleteMeetupTarget = target
                                    meetupActionTarget = null
                                }
                                .glassConcaveOverlay(14.dp)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "删除",
                                color = GlassAccent.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .noRipplePressable(pressedScale = 0.96f) {
                                meetupActionTarget = null
                                onSaveImage(target)
                            }
                            .glassConcaveOverlay(14.dp)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "保存图片到相册",
                            color = GlassAccent.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { meetupActionTarget = null }) {
                    Text("关闭", color = tokens.textSecondary)
                }
            }
        )
    }

    deleteMeetupTarget?.let { target ->
        ConfirmDeleteDialog(
            message = "确定要删除第 ${target.meetIndex} 次见面记录（${target.arriveDate} ~ ${target.leaveDate}）吗？",
            onConfirm = {
                deleteMeetupTarget = null
                vm.deleteMeetup(target.id)
            },
            onDismiss = { deleteMeetupTarget = null }
        )
    }

    meetupLightbox?.let { (url, caption) ->
        PhotoLightboxDialog(
            imageUrl = url,
            caption = caption,
            onDismiss = { meetupLightbox = null }
        )
    }
}

/** 下载卡片里的照片用于导出；失败直接跳过，不阻塞保存。 */
private fun loadBitmap(url: String): Bitmap? {
    val resolved = com.xixikitchen.jetpack.ui.components.realImageUrl(url) ?: return null
    return runCatching {
        val conn = (URL(resolved).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 15_000
            instanceFollowRedirects = true
        }
        try {
            conn.inputStream.use { BitmapFactory.decodeStream(it) }
        } finally {
            conn.disconnect()
        }
    }.getOrNull()
}

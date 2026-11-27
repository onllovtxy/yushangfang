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
import com.xixikitchen.jetpack.ui.components.ConfirmDeleteDialog
import com.xixikitchen.jetpack.ui.components.PhotoLightboxDialog
import com.xixikitchen.jetpack.ui.components.SuperAdminSpaceBanner
import com.xixikitchen.jetpack.ui.components.rememberPullRefreshState
import com.xixikitchen.jetpack.ui.designsystem.motion.AppDuration
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcaveOverlay
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.noRipplePressable
import com.xixikitchen.jetpack.widget.WidgetPinner
import com.xixikitchen.jetpack.widget.WidgetPublish
import com.xixikitchen.jetpack.widget.WidgetType
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

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

    var showCreateMeetup by remember { mutableStateOf(false) }
    var editingMeetup by remember { mutableStateOf<CoupleMeetup?>(null) }
    var meetupActionTarget by remember { mutableStateOf<CoupleMeetup?>(null) }
    var deleteMeetupTarget by remember { mutableStateOf<CoupleMeetup?>(null) }
    var meetupLightbox by remember { mutableStateOf<Pair<String, String?>?>(null) }

    LaunchedEffect(Unit) { vm.refreshMeetups() }

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
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp)
                ) {
                    MeetupsContent(
                        state = state,
                        onMeetupActionRequest = { meetupActionTarget = it },
                        onPhotoClick = { url, caption -> meetupLightbox = url to caption }
                    )

                    androidx.compose.animation.AnimatedVisibility(
                        visible = showAddFab,
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
                                val picked = target
                                meetupActionTarget = null
                                scope.launch {
                                    WidgetPublish.publishMeetups(context, state.meetups)
                                    WidgetPinner.requestPin(context, WidgetType.Meetup, picked.id)
                                }
                            }
                            .glassConcaveOverlay(14.dp)
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "添加到桌面",
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

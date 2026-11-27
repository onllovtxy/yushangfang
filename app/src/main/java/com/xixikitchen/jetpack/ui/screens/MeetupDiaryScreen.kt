package com.xixikitchen.jetpack.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.xixikitchen.jetpack.data.CoupleMeetup
import com.xixikitchen.jetpack.data.CoupleSpace
import com.xixikitchen.jetpack.ui.DiscoverUiState
import com.xixikitchen.jetpack.ui.DiscoverViewModel
import com.xixikitchen.jetpack.ui.components.ConfirmDeleteDialog
import com.xixikitchen.jetpack.ui.components.PhotoLightboxDialog
import com.xixikitchen.jetpack.ui.components.SuperAdminSpaceBanner
import com.xixikitchen.jetpack.ui.components.rememberPullRefreshState
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcaveOverlay
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.noRipplePressable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetupDiaryScreen(
    state: DiscoverUiState,
    vm: DiscoverViewModel,
    isSuperAdmin: Boolean = false,
    currentSpace: CoupleSpace? = null,
    allSpaces: List<CoupleSpace> = emptyList(),
    onSelectSpace: (Long) -> Unit = {}
) {
    val tokens = LocalGlassTokens.current
    val (isPullRefreshing, startPullRefresh) = rememberPullRefreshState(state.loading)

    var showCreateMeetup by remember { mutableStateOf(false) }
    var editingMeetup by remember { mutableStateOf<CoupleMeetup?>(null) }
    var meetupActionTarget by remember { mutableStateOf<CoupleMeetup?>(null) }
    var deleteMeetupTarget by remember { mutableStateOf<CoupleMeetup?>(null) }
    var meetupLightbox by remember { mutableStateOf<Pair<String, String?>?>(null) }

    LaunchedEffect(Unit) { vm.refreshMeetups() }

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
                Box(Modifier.fillMaxSize().padding(top = 12.dp)) {
                    MeetupsContent(
                        state = state,
                        onAddMeetup = { showCreateMeetup = true },
                        onMeetupActionRequest = { meetupActionTarget = it },
                        onPhotoClick = { url, caption -> meetupLightbox = url to caption }
                    )
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

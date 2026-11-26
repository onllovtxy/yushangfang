package com.xixikitchen.jetpack.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xixikitchen.jetpack.data.Anniversary
import com.xixikitchen.jetpack.data.AnniversarySaveRequest
import com.xixikitchen.jetpack.data.CoupleNote
import com.xixikitchen.jetpack.data.CoupleSpace
import com.xixikitchen.jetpack.data.FoodDiary
import com.xixikitchen.jetpack.data.FoodDiaryComment
import com.xixikitchen.jetpack.ui.DiscoverUiState
import com.xixikitchen.jetpack.ui.DiscoverViewModel
import com.xixikitchen.jetpack.ui.components.ConfirmDeleteDialog
import com.xixikitchen.jetpack.ui.components.CropAspectRatio
import com.xixikitchen.jetpack.ui.components.ImageCropperDialog
import com.xixikitchen.jetpack.ui.components.PhotoLightboxDialog
import com.xixikitchen.jetpack.ui.components.StatusFilterChip
import com.xixikitchen.jetpack.ui.components.SuperAdminSpaceBanner
import com.xixikitchen.jetpack.ui.components.SwipeRevealBox
import com.xixikitchen.jetpack.ui.components.TicketStubCard
import com.xixikitchen.jetpack.ui.components.UserMiniAvatar
import com.xixikitchen.jetpack.ui.components.decodeBitmapWithExif
import com.xixikitchen.jetpack.ui.components.rememberPullRefreshState
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcaveOverlay
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.noRippleCombinedPressable
import com.xixikitchen.jetpack.ui.noRipplePressable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private enum class DiscoverSubTab(val label: String) {
    Announcements("公告栏"),
    Notes("小纸条"),
    Diary("美食日记")
}

private data class TicketCreatorDraft(
    val rawBitmap: Bitmap,
    val croppedBitmap: Bitmap,
    val aspectRatio: CropAspectRatio
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    state: DiscoverUiState,
    vm: DiscoverViewModel,
    isSuperAdmin: Boolean = false,
    currentSpace: CoupleSpace? = null,
    allSpaces: List<CoupleSpace> = emptyList(),
    onSelectSpace: (Long) -> Unit = {}
) {
    val tokens = LocalGlassTokens.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var subTab by rememberSaveable { mutableStateOf(DiscoverSubTab.Announcements) }
    val (isPullRefreshing, startPullRefresh) = rememberPullRefreshState(state.loading)

    var showCreateNote by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<CoupleNote?>(null) }
    var noteActionTarget by remember { mutableStateOf<CoupleNote?>(null) }
    var deleteNoteTarget by remember { mutableStateOf<CoupleNote?>(null) }

    var showCreateAnniversary by remember { mutableStateOf(false) }
    var editingAnniversary by remember { mutableStateOf<Anniversary?>(null) }
    var anniversaryActionTarget by remember { mutableStateOf<Anniversary?>(null) }
    var deleteAnniversaryTarget by remember { mutableStateOf<Anniversary?>(null) }

    // 票根制作 / 裁切 / 编辑 / 评论状态
    var pendingCropBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var creatorDraft by remember { mutableStateOf<TicketCreatorDraft?>(null) }
    var editingDiary by remember { mutableStateOf<FoodDiary?>(null) }
    var diaryActionTarget by remember { mutableStateOf<FoodDiary?>(null) }
    var deleteDiaryTarget by remember { mutableStateOf<FoodDiary?>(null) }
    var lightboxDiary by remember { mutableStateOf<FoodDiary?>(null) }
    var commentTarget by remember { mutableStateOf<Pair<FoodDiary, FoodDiaryComment?>?>(null) }
    var commentActionTarget by remember { mutableStateOf<Pair<FoodDiary, FoodDiaryComment>?>(null) }

    val ticketPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val decoded = withContext(Dispatchers.IO) { decodeBitmapWithExif(context, uri) }
            if (decoded != null) {
                pendingCropBitmap = decoded
            }
        }
    }

    LaunchedEffect(subTab) {
        when (subTab) {
            DiscoverSubTab.Announcements -> if (!state.announcementsLoaded) vm.refreshAnnouncements()
            DiscoverSubTab.Notes -> if (!state.notesLoaded) vm.refreshNotesBoard()
            DiscoverSubTab.Diary -> if (!state.diaryLoaded) vm.refreshDiary()
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
                when (subTab) {
                    DiscoverSubTab.Announcements -> vm.refreshAnnouncements()
                    DiscoverSubTab.Notes -> vm.refreshNotesBoard()
                    DiscoverSubTab.Diary -> vm.refreshDiary()
                }
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
                if (isSuperAdmin && subTab != DiscoverSubTab.Announcements) {
                    SuperAdminSpaceBanner(
                        isSuperAdmin = true,
                        currentSpace = currentSpace,
                        spaces = allSpaces,
                        onSelectSpace = onSelectSpace,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    DiscoverSubTab.entries.forEach { tab ->
                        StatusFilterChip(
                            active = subTab == tab,
                            onClick = { subTab = tab },
                            label = tab.label,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                when (subTab) {
                    DiscoverSubTab.Announcements -> AnnouncementsContent(state = state)

                    DiscoverSubTab.Notes -> NotesBoardContent(
                        state = state,
                        onAddAnniversary = { showCreateAnniversary = true },
                        onAnniversaryClick = { anniversaryActionTarget = it },
                        onAnniversaryDeleteRequest = { deleteAnniversaryTarget = it },
                        onAddNote = { showCreateNote = true },
                        onNoteLongClick = { noteActionTarget = it },
                        onNoteDeleteRequest = { deleteNoteTarget = it }
                    )

                    DiscoverSubTab.Diary -> DiaryContent(
                        state = state,
                        onCreateTicket = { ticketPicker.launch("image/*") },
                        onImageClick = { lightboxDiary = it },
                        onLongClick = { diaryActionTarget = it },
                        onCommentClick = { diary, replyComment ->
                            if (replyComment == null) {
                                commentTarget = diary to null
                            } else {
                                commentActionTarget = diary to replyComment
                            }
                        },
                        onLoadMore = vm::loadMoreDiary
                    )
                }
            }
        }
    }

    // 选图后立即弹出交互式裁切器
    pendingCropBitmap?.let { raw ->
        ImageCropperDialog(
            sourceBitmap = raw,
            initialRatio = CropAspectRatio.RATIO_4_3,
            onDismiss = { pendingCropBitmap = null },
            onCropConfirmed = { cropped, chosenRatio ->
                pendingCropBitmap = null
                creatorDraft = TicketCreatorDraft(
                    rawBitmap = raw,
                    croppedBitmap = cropped,
                    aspectRatio = chosenRatio
                )
            }
        )
    }

    // 新建票根工作台
    creatorDraft?.let { draft ->
        TicketCreatorDialog(
            initialDiary = null,
            initialRawBitmap = draft.rawBitmap,
            initialCroppedBitmap = draft.croppedBitmap,
            initialRatio = draft.aspectRatio,
            authorName = state.currentUser?.nickname,
            vm = vm,
            onDismiss = { creatorDraft = null },
            onSubmit = { req ->
                vm.createDiary(req) {
                    creatorDraft = null
                }
            }
        )
    }

    // 编辑已有票根工作台
    editingDiary?.let { target ->
        TicketCreatorDialog(
            initialDiary = target,
            authorName = target.authorName ?: state.currentUser?.nickname,
            vm = vm,
            onDismiss = { editingDiary = null },
            onSubmit = { req ->
                vm.updateDiary(target.id, req) {
                    editingDiary = null
                }
            }
        )
    }

    if (showCreateNote || editingNote != null) {
        NoteEditorDialog(
            initial = editingNote,
            onDismiss = {
                showCreateNote = false
                editingNote = null
            },
            onSave = { content, pinned ->
                val targetId = editingNote?.id
                showCreateNote = false
                editingNote = null
                vm.saveNote(targetId, content, pinned)
            }
        )
    }

    noteActionTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { noteActionTarget = null },
            modifier = Modifier.glassConvexOverlay(24.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.Transparent,
            title = {
                Text(
                    "小纸条操作",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassAccent.primaryDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = target.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = tokens.textSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .noRipplePressable(pressedScale = 0.96f) {
                                    noteActionTarget = null
                                    vm.togglePinNote(target)
                                }
                                .glassConcaveOverlay(14.dp)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (target.pinned) "取消置顶" else "置顶",
                                color = GlassAccent.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .noRipplePressable(pressedScale = 0.96f) {
                                    editingNote = target
                                    noteActionTarget = null
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
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteNoteTarget = target
                        noteActionTarget = null
                    }
                ) {
                    Text("删除", color = GlassAccent.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteActionTarget = null }) {
                    Text("关闭", color = tokens.textSecondary)
                }
            }
        )
    }

    deleteNoteTarget?.let { target ->
        ConfirmDeleteDialog(
            message = "确定要撕掉这张小纸条吗？",
            onConfirm = {
                deleteNoteTarget = null
                vm.deleteNote(target.id)
            },
            onDismiss = { deleteNoteTarget = null }
        )
    }

    if (showCreateAnniversary || editingAnniversary != null) {
        AnniversaryEditorDialog(
            initial = editingAnniversary,
            onDismiss = {
                showCreateAnniversary = false
                editingAnniversary = null
            },
            onSave = { req ->
                val targetId = editingAnniversary?.id
                showCreateAnniversary = false
                editingAnniversary = null
                vm.saveAnniversary(targetId, req)
            }
        )
    }

    anniversaryActionTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { anniversaryActionTarget = null },
            modifier = Modifier.glassConvexOverlay(24.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.Transparent,
            title = {
                Text(
                    target.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassAccent.primaryDark
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "${target.eventDate}${if (target.note.isNotBlank()) " · ${target.note}" else ""}",
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
                                    anniversaryActionTarget = null
                                    vm.togglePinAnniversary(target)
                                }
                                .glassConcaveOverlay(14.dp)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (target.pinned) "取消置顶" else "置顶",
                                color = GlassAccent.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .noRipplePressable(pressedScale = 0.96f) {
                                    editingAnniversary = target
                                    anniversaryActionTarget = null
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
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteAnniversaryTarget = target
                        anniversaryActionTarget = null
                    }
                ) {
                    Text("删除", color = GlassAccent.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { anniversaryActionTarget = null }) {
                    Text("关闭", color = tokens.textSecondary)
                }
            }
        )
    }

    deleteAnniversaryTarget?.let { target ->
        ConfirmDeleteDialog(
            message = "确定要删除纪念日 \"${target.title}\" 吗？",
            onConfirm = {
                deleteAnniversaryTarget = null
                vm.deleteAnniversary(target.id)
            },
            onDismiss = { deleteAnniversaryTarget = null }
        )
    }

    diaryActionTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { diaryActionTarget = null },
            modifier = Modifier.glassConvexOverlay(24.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.Transparent,
            title = {
                Text(
                    target.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassAccent.primaryDark
                )
            },
            text = {
                Text(
                    "想要编辑或删除这张美食票根吗？",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingDiary = target
                        diaryActionTarget = null
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
                ) {
                    Text("编辑票根", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { diaryActionTarget = null }) {
                        Text("取消", color = tokens.textSecondary)
                    }
                    TextButton(
                        onClick = {
                            deleteDiaryTarget = target
                            diaryActionTarget = null
                        }
                    ) {
                        Text("删除", color = GlassAccent.error)
                    }
                }
            }
        )
    }

    deleteDiaryTarget?.let { target ->
        ConfirmDeleteDialog(
            message = "确定要删除美食票根 \"${target.title}\" 吗？相关评论也会一并清除。",
            onConfirm = {
                deleteDiaryTarget = null
                vm.deleteDiary(target)
            },
            onDismiss = { deleteDiaryTarget = null }
        )
    }

    commentTarget?.let { (diary, replyTo) ->
        DiaryCommentDialog(
            diaryTitle = diary.title,
            replyToName = replyTo?.authorName,
            onDismiss = { commentTarget = null },
            onSend = { text ->
                commentTarget = null
                vm.addComment(diary.id, text, replyTo?.userId)
            }
        )
    }

    commentActionTarget?.let { (diary, comment) ->
        AlertDialog(
            onDismissRequest = { commentActionTarget = null },
            modifier = Modifier.glassConvexOverlay(24.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.Transparent,
            title = {
                Text(
                    text = comment.authorName ?: "评论操作",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassAccent.primaryDark
                )
            },
            text = {
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textPrimary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        commentActionTarget = null
                        commentTarget = diary to comment
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
                ) {
                    Text("回复", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { commentActionTarget = null }) {
                        Text("取消", color = tokens.textSecondary)
                    }
                    TextButton(
                        onClick = {
                            commentActionTarget = null
                            vm.deleteComment(diary.id, comment.id)
                        }
                    ) {
                        Text("删除", color = GlassAccent.error)
                    }
                }
            }
        )
    }

    lightboxDiary?.let { target ->
        PhotoLightboxDialog(
            imageUrl = target.imageUrl,
            caption = listOfNotNull(
                target.title.takeIf { it.isNotBlank() },
                target.content.takeIf { it.isNotBlank() }
            ).joinToString(" · ").takeIf { it.isNotBlank() },
            onDismiss = { lightboxDiary = null }
        )
    }
}

@Composable
private fun AnnouncementsContent(state: DiscoverUiState) {
    val tokens = LocalGlassTokens.current
    if (state.announcements.isEmpty() && !state.loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassConcave(20.dp)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无公告",
                    color = tokens.textSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.announcements, key = { "ann_${it.id}" }) { ann ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (ann.pinned) {
                            Modifier.border(BorderStroke(1.dp, GlassAccent.edge), RoundedCornerShape(20.dp))
                        } else {
                            Modifier
                        }
                    )
                    .glassConvex(20.dp)
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .glassConcave(12.dp)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "公告",
                                    color = GlassAccent.primary,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            if (ann.pinned) {
                                Box(
                                    modifier = Modifier
                                        .background(GlassAccent.wash, RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "置顶",
                                        color = GlassAccent.primary,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                        Text(
                            text = (ann.updatedAt ?: ann.createdAt)?.take(10).orEmpty(),
                            style = MaterialTheme.typography.labelSmall,
                            color = tokens.textSecondary
                        )
                    }
                    Text(
                        text = ann.content,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        color = tokens.textPrimary,
                        lineHeight = 22.sp
                    )
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun NotesBoardContent(
    state: DiscoverUiState,
    onAddAnniversary: () -> Unit,
    onAnniversaryClick: (Anniversary) -> Unit,
    onAnniversaryDeleteRequest: (Anniversary) -> Unit,
    onAddNote: () -> Unit,
    onNoteLongClick: (CoupleNote) -> Unit,
    onNoteDeleteRequest: (CoupleNote) -> Unit
) {
    val tokens = LocalGlassTokens.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionActionHeader(
                title = "纪念日",
                actionLabel = "添加",
                onAction = onAddAnniversary
            )
        }

        if (state.anniversaries.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRipplePressable(pressedScale = 0.98f, onClick = onAddAnniversary)
                        .glassConcave(18.dp)
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = GlassAccent.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "添加你们的第一个纪念日 / 倒计时",
                            color = GlassAccent.primary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        } else {
            items(state.anniversaries, key = { "anniv_${it.id}" }) { item ->
                SwipeRevealBox(
                    swipeEnabled = true,
                    onDeleteClick = { onAnniversaryDeleteRequest(item) }
                ) {
                    AnniversaryCard(
                        item = item,
                        onClick = { onAnniversaryClick(item) }
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            SectionActionHeader(
                title = "小纸条",
                actionLabel = "写纸条",
                onAction = onAddNote
            )
        }

        if (state.notes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRipplePressable(pressedScale = 0.98f, onClick = onAddNote)
                        .glassConcave(20.dp)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "冰箱门还空着，贴上第一张小纸条吧~",
                        color = tokens.textSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(state.notes, key = { "note_${it.id}" }) { note ->
                SwipeRevealBox(
                    swipeEnabled = true,
                    onDeleteClick = { onNoteDeleteRequest(note) }
                ) {
                    FridgeNoteCard(
                        note = note,
                        onLongClick = { onNoteLongClick(note) }
                    )
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun SectionActionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = tokens.textPrimary
        )
        Box(
            modifier = Modifier
                .noRipplePressable(pressedScale = 0.94f, onClick = onAction)
                .glassConvex(14.dp)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = GlassAccent.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = actionLabel,
                    color = GlassAccent.primary,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold)
                )
            }
        }
    }
}

@Composable
private fun AnniversaryCard(
    item: Anniversary,
    onClick: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    val badgeText = when {
        item.isToday -> "就是今天"
        item.type == "countup" && item.isPast -> "即将到来"
        item.type == "countup" -> "已在一起"
        item.isPast -> "已过去"
        else -> "倒计时"
    }
    val dayPrefix = when {
        item.type == "countup" && item.isPast -> "还有"
        item.type == "countup" -> "已经"
        item.isPast -> "已过"
        else -> "还有"
    }
    val footerText = when {
        item.nextAnniversaryDays == 0L -> "${item.eventDate} · 周年纪念 🎉"
        item.nextAnniversaryDays != null -> "${item.eventDate} · 周年还有 ${item.nextAnniversaryDays} 天"
        item.note.isNotBlank() -> "${item.eventDate} · ${item.note}"
        else -> item.eventDate
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleCombinedPressable(
                pressedScale = 0.985f,
                onLongClick = onClick,
                onClick = onClick
            )
            .then(
                if (item.pinned) {
                    Modifier.border(BorderStroke(1.dp, GlassAccent.edge), RoundedCornerShape(20.dp))
                } else {
                    Modifier
                }
            )
            .glassConvex(20.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (item.pinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "已置顶",
                            tint = GlassAccent.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = tokens.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Box(
                        modifier = Modifier
                            .background(GlassAccent.wash, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = GlassAccent.primary,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Text(
                    text = footerText,
                    style = MaterialTheme.typography.bodySmall,
                    color = tokens.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(12.dp))

            if (item.isToday) {
                Text(
                    text = "今天 🎉",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = GlassAccent.primaryDark,
                    maxLines = 1
                )
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = dayPrefix,
                        style = MaterialTheme.typography.labelSmall,
                        color = tokens.textSecondary,
                        modifier = Modifier.padding(bottom = 4.dp, end = 4.dp)
                    )
                    Text(
                        text = "${item.daysCount}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = GlassAccent.primary,
                        maxLines = 1
                    )
                    Text(
                        text = "天",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = GlassAccent.primary,
                        modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FridgeNoteCard(
    note: CoupleNote,
    onLongClick: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleCombinedPressable(
                pressedScale = 0.985f,
                onLongClick = onLongClick,
                onClick = {}
            )
            .then(
                if (note.pinned) {
                    Modifier.border(BorderStroke(1.dp, GlassAccent.edge), RoundedCornerShape(20.dp))
                } else {
                    Modifier
                }
            )
            .glassConvex(20.dp)
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserMiniAvatar(note.authorAvatar, size = 22.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = note.authorName ?: "小厨房成员",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = tokens.textPrimary
                    )
                    if (note.pinned) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .background(GlassAccent.wash, RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "置顶",
                                color = GlassAccent.primary,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
                Text(
                    text = (note.updatedAt ?: note.createdAt)?.take(10).orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary
                )
            }

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = tokens.textPrimary,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun DiaryContent(
    state: DiscoverUiState,
    onCreateTicket: () -> Unit,
    onImageClick: (FoodDiary) -> Unit,
    onLongClick: (FoodDiary) -> Unit,
    onCommentClick: (FoodDiary, FoodDiaryComment?) -> Unit,
    onLoadMore: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    val currentUserId = state.currentUser?.id ?: -1L

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SectionActionHeader(
                title = "美食票根",
                actionLabel = "制作票根",
                onAction = onCreateTicket
            )
        }

        if (state.diaryEntries.isEmpty() && !state.loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassConvex(24.dp)
                        .padding(24.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "还没有美味票根",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = tokens.textPrimary
                        )
                        Text(
                            text = "上传一张美食照片，裁切画面并套用旅行票根模板，制作你们的专属美食手帐。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.textSecondary
                        )
                        Button(
                            onClick = onCreateTicket,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
                        ) {
                            Text("制作第一张票根", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(state.diaryEntries, key = { "diary_${it.id}" }) { entry ->
                DiaryTicketItem(
                    entry = entry,
                    canManage = true,
                    onImageClick = { onImageClick(entry) },
                    onLongClick = { onLongClick(entry) },
                    onCommentClick = { replyComment -> onCommentClick(entry, replyComment) }
                )
            }

            if (state.diaryEntries.size < state.diaryTotal) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .noRipplePressable(
                                enabled = !state.loadingMoreDiary,
                                pressedScale = 0.97f,
                                onClick = onLoadMore
                            )
                            .glassConcave(18.dp)
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.loadingMoreDiary) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = GlassAccent.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "加载更多",
                                color = GlassAccent.primary,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun DiaryTicketItem(
    entry: FoodDiary,
    canManage: Boolean,
    onImageClick: () -> Unit,
    onLongClick: () -> Unit,
    onCommentClick: (FoodDiaryComment?) -> Unit
) {
    val tokens = LocalGlassTokens.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TicketStubCard(
            diary = entry,
            onImageClick = onImageClick,
            onLongClick = if (canManage) onLongClick else null
        )

        // 票根底部互动条（发布者信息 + 评论按钮）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserMiniAvatar(entry.authorAvatar, size = 20.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = entry.authorName ?: "成员",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = tokens.textPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = entry.createdAt?.take(16).orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    color = tokens.textSecondary
                )
            }

            Box(
                modifier = Modifier
                    .noRipplePressable(pressedScale = 0.94f) { onCommentClick(null) }
                    .glassConvex(14.dp)
                    .padding(horizontal = 12.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "评论",
                        tint = GlassAccent.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (entry.comments.isEmpty()) "评论" else "评论 ${entry.comments.size}",
                        color = GlassAccent.primary,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        // 评论列表
        if (entry.comments.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassConcave(16.dp)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                entry.comments.forEach { comment ->
                    val lineText = buildAnnotatedString {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = GlassAccent.primaryDark)) {
                            append(comment.authorName ?: "成员")
                        }
                        if (!comment.replyToName.isNullOrBlank()) {
                            withStyle(SpanStyle(color = tokens.textSecondary)) {
                                append(" 回复 ")
                            }
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = GlassAccent.primaryDark)) {
                                append("@${comment.replyToName}")
                            }
                        }
                        withStyle(SpanStyle(color = tokens.textPrimary)) {
                            append("：${comment.content}")
                        }
                    }
                    Text(
                        text = lineText,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .noRipplePressable(pressedScale = 0.99f) { onCommentClick(comment) }
                            .padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DiaryCommentDialog(
    diaryTitle: String,
    replyToName: String?,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    val tokens = LocalGlassTokens.current
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.glassConvexOverlay(24.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                text = if (!replyToName.isNullOrBlank()) "回复 @$replyToName" else "评论「$diaryTitle」",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 300) text = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("说点什么（最多300字）") },
                shape = RoundedCornerShape(16.dp),
                minLines = 2,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlassAccent.primary,
                    focusedLabelColor = GlassAccent.primary
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onSend(text.trim()) },
                enabled = text.trim().isNotEmpty(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("发送", fontWeight = FontWeight.Bold)
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
private fun NoteEditorDialog(
    initial: CoupleNote?,
    onDismiss: () -> Unit,
    onSave: (String, Boolean) -> Unit
) {
    val tokens = LocalGlassTokens.current
    var content by remember { mutableStateOf(initial?.content ?: "") }
    var pinned by remember { mutableStateOf(initial?.pinned ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.glassConvexOverlay(24.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                if (initial == null) "贴一张小纸条" else "编辑小纸条",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { if (it.length <= 500) content = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("写点什么（最多500字）") },
                    shape = RoundedCornerShape(16.dp),
                    minLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )
                TogglePillRow(
                    label = "置顶到顶部",
                    checked = pinned,
                    onToggle = { pinned = !pinned }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(content.trim(), pinned) },
                enabled = content.trim().isNotEmpty(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnniversaryEditorDialog(
    initial: Anniversary?,
    onDismiss: () -> Unit,
    onSave: (AnniversarySaveRequest) -> Unit
) {
    val tokens = LocalGlassTokens.current
    val initialDate = remember(initial) {
        initial?.eventDate?.takeIf { it.isNotBlank() } ?: formatLocalDateString(Date())
    }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var type by remember { mutableStateOf(initial?.type ?: "countdown") }
    var eventDate by remember { mutableStateOf(initialDate) }
    var repeatYearly by remember { mutableStateOf(initial?.repeatYearly ?: true) }
    var pinned by remember { mutableStateOf(initial?.pinned ?: false) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.glassConvexOverlay(24.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                if (initial == null) "新增纪念日" else "编辑纪念日",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 60) title = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("纪念日名称") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isCountup = type == "countup"
                    val isCountdown = type == "countdown"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .noRipplePressable(pressedScale = 0.96f) { type = "countup" }
                            .background(if (isCountup) GlassAccent.primary else GlassAccent.wash, RoundedCornerShape(14.dp))
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "正数纪念日",
                            color = if (isCountup) Color.White else tokens.textPrimary,
                            fontWeight = if (isCountup) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .noRipplePressable(pressedScale = 0.96f) { type = "countdown" }
                            .background(if (isCountdown) GlassAccent.primary else GlassAccent.wash, RoundedCornerShape(14.dp))
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "倒计时",
                            color = if (isCountdown) Color.White else tokens.textPrimary,
                            fontWeight = if (isCountdown) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .noRipplePressable(pressedScale = 0.98f) { showDatePicker = true }
                        .glassConcaveOverlay(16.dp)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "纪念日期",
                            style = MaterialTheme.typography.bodySmall,
                            color = tokens.textSecondary
                        )
                        Text(
                            text = eventDate,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = GlassAccent.primary
                        )
                    }
                }

                TogglePillRow(
                    label = "每年循环",
                    checked = repeatYearly,
                    onToggle = { repeatYearly = !repeatYearly }
                )

                TogglePillRow(
                    label = "置顶展示",
                    checked = pinned,
                    onToggle = { pinned = !pinned }
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { if (it.length <= 200) note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("备注（可选）") },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        AnniversarySaveRequest(
                            title = title.trim(),
                            eventDate = eventDate,
                            type = type,
                            repeatYearly = repeatYearly,
                            pinned = pinned,
                            note = note.trim()
                        )
                    )
                },
                enabled = title.trim().isNotEmpty(),
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = parseUtcDateMillis(eventDate)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            shape = RoundedCornerShape(24.dp),
            colors = DatePickerDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.glassConvexOverlay(24.dp),
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            eventDate = formatUtcDateMillis(millis)
                        }
                        showDatePicker = false
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
                ) {
                    Text("确定", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消", color = tokens.textSecondary)
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(containerColor = Color.Transparent)
            )
        }
    }
}

@Composable
private fun TogglePillRow(
    label: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRipplePressable(pressedScale = 0.98f, onClick = onToggle)
            .glassConcaveOverlay(16.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = tokens.textPrimary
        )
        Box(
            modifier = Modifier
                .background(
                    if (checked) GlassAccent.primary else GlassAccent.wash,
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (checked) "已开启" else "未开启",
                color = if (checked) Color.White else tokens.textSecondary,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

private fun formatLocalDateString(date: Date): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    return sdf.format(date)
}

private fun parseUtcDateMillis(dateStr: String): Long? {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return runCatching { sdf.parse(dateStr)?.time }.getOrNull()
}

private fun formatUtcDateMillis(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return sdf.format(Date(millis))
}

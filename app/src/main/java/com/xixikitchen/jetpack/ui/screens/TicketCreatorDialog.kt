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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xixikitchen.jetpack.data.FoodDiary
import com.xixikitchen.jetpack.data.FoodDiarySaveRequest
import com.xixikitchen.jetpack.ui.DiscoverViewModel
import com.xixikitchen.jetpack.ui.components.CropAspectRatio
import com.xixikitchen.jetpack.ui.components.ImageCropperDialog
import com.xixikitchen.jetpack.ui.components.TicketStubCard
import com.xixikitchen.jetpack.ui.components.TicketTemplate
import com.xixikitchen.jetpack.ui.components.compressBitmapToJpegBytes
import com.xixikitchen.jetpack.ui.components.decodeBitmapWithExif
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassMeshBackground
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.noRipplePressable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val MOOD_PRESETS = listOf(
    "超满足",
    "光盘行动",
    "大厨封神",
    "纪念味道",
    "深夜放毒",
    "浪漫双人"
)

@Composable
fun TicketCreatorDialog(
    initialDiary: FoodDiary? = null,
    initialRawBitmap: Bitmap? = null,
    initialCroppedBitmap: Bitmap? = null,
    initialRatio: CropAspectRatio = CropAspectRatio.RATIO_4_3,
    authorName: String?,
    vm: DiscoverViewModel,
    onDismiss: () -> Unit,
    onSubmit: (FoodDiarySaveRequest) -> Unit
) {
    val tokens = LocalGlassTokens.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var rawBitmap by remember(initialRawBitmap) { mutableStateOf(initialRawBitmap) }
    var croppedBitmap by remember(initialCroppedBitmap) { mutableStateOf(initialCroppedBitmap) }
    var cropForReEdit by remember { mutableStateOf<Bitmap?>(null) }

    var template by remember(initialDiary) {
        mutableStateOf(TicketTemplate.fromCode(initialDiary?.templateCode))
    }
    var aspectRatio by remember(initialDiary, initialRatio) {
        mutableStateOf(
            if (initialDiary != null) CropAspectRatio.fromCode(initialDiary.aspectRatio)
            else initialRatio
        )
    }
    var title by remember(initialDiary) { mutableStateOf(initialDiary?.title ?: "") }
    var location by remember(initialDiary) { mutableStateOf(initialDiary?.location ?: "") }
    var moodTag by remember(initialDiary) { mutableStateOf(initialDiary?.moodTag ?: "超满足") }
    var content by remember(initialDiary) { mutableStateOf(initialDiary?.content ?: "") }

    var uploadedImageUrl by remember(initialDiary) { mutableStateOf(initialDiary?.imageUrl ?: "") }
    var uploading by remember { mutableStateOf(false) }

    fun uploadCroppedBitmap(bmp: Bitmap) {
        uploading = true
        scope.launch {
            runCatching {
                val bytes = withContext(Dispatchers.IO) { compressBitmapToJpegBytes(bmp) }
                vm.uploadImageBlocking(bytes, "ticket_${System.currentTimeMillis()}.jpg")
            }.onSuccess { url ->
                uploadedImageUrl = url
                uploading = false
            }.onFailure { e ->
                uploading = false
                vm.reportError(e)
            }
        }
    }

    // 新建时刚裁切完进入工作台即后台自动压缩上传
    LaunchedEffect(initialCroppedBitmap) {
        if (initialDiary == null && initialCroppedBitmap != null && uploadedImageUrl.isBlank()) {
            uploadCroppedBitmap(initialCroppedBitmap)
        }
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val decoded = withContext(Dispatchers.IO) { decodeBitmapWithExif(context, uri) }
            if (decoded != null) {
                rawBitmap = decoded
                cropForReEdit = decoded
            }
        }
    }

    val previewDiary = remember(
        initialDiary,
        uploadedImageUrl,
        template,
        aspectRatio,
        title,
        content,
        location,
        moodTag,
        authorName
    ) {
        FoodDiary(
            id = initialDiary?.id ?: 0L,
            authorName = initialDiary?.authorName ?: authorName ?: "我",
            imageUrl = uploadedImageUrl,
            templateCode = template.code,
            aspectRatio = aspectRatio.code,
            title = title.ifBlank { "今日份美味" },
            content = content,
            location = location.ifBlank { "熙熙的小厨房" },
            moodTag = moodTag.ifBlank { "超满足" },
            ticketNo = initialDiary?.ticketNo?.takeIf { it.isNotBlank() } ?: "NO.2026-PREVIEW",
            createdAt = initialDiary?.createdAt ?: "TODAY"
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x22FFFFFF))
        ) {
            GlassMeshBackground(tokens)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // 顶部栏
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .glassConvex(22.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "取消",
                                    tint = GlassAccent.primary
                                )
                            }
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (initialDiary == null) "制作美味票根" else "编辑票根",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = tokens.textPrimary
                            )
                        }

                        Button(
                            onClick = {
                                onSubmit(
                                    FoodDiarySaveRequest(
                                        imageUrl = uploadedImageUrl,
                                        templateCode = template.code,
                                        aspectRatio = aspectRatio.code,
                                        title = title.trim().ifEmpty { "今日份美味" },
                                        content = content.trim(),
                                        location = location.trim().ifEmpty { "熙熙的小厨房" },
                                        moodTag = moodTag.trim().ifEmpty { "超满足" }
                                    )
                                )
                            },
                            enabled = !uploading && uploadedImageUrl.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            if (uploading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("上传中", fontWeight = FontWeight.Bold)
                            } else {
                                Text(
                                    text = if (initialDiary == null) "生成票根" else "保存",
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 实时票根预览
                    TicketStubCard(
                        diary = previewDiary,
                        previewBitmap = croppedBitmap
                    )

                    // 图片操作按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (rawBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .noRipplePressable(enabled = !uploading, pressedScale = 0.96f) {
                                        cropForReEdit = rawBitmap
                                    }
                                    .glassConvex(14.dp)
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✂️ 重新裁切",
                                    color = GlassAccent.primary,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .noRipplePressable(enabled = !uploading, pressedScale = 0.96f) {
                                    pickerLauncher.launch("image/*")
                                }
                                .glassConvex(14.dp)
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🖼️ 换张图片",
                                color = tokens.textPrimary,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // 5 款票根模板选择
                    Text(
                        text = "选择票根模板",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = tokens.textPrimary
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(TicketTemplate.entries) { item ->
                            val selected = item == template
                            Column(
                                modifier = Modifier
                                    .width(114.dp)
                                    .noRipplePressable(pressedScale = 0.95f) { template = item }
                                    .then(
                                        if (selected) {
                                            Modifier.border(
                                                BorderStroke(1.5.dp, GlassAccent.primary),
                                                RoundedCornerShape(16.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .then(
                                        if (selected) Modifier.glassConvex(16.dp) else Modifier.glassConcave(16.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = item.label,
                                    color = if (selected) GlassAccent.primary else tokens.textPrimary,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.ExtraBold),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // 票根文案填写
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassConvex(20.dp)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { if (it.length <= 60) title = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("票根 / 美食标题") },
                                placeholder = { Text("今日份美味") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GlassAccent.primary,
                                    focusedLabelColor = GlassAccent.primary
                                )
                            )

                            OutlinedTextField(
                                value = location,
                                onValueChange = { if (it.length <= 60) location = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("打卡地点 / 站台") },
                                placeholder = { Text("熙熙的小厨房") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GlassAccent.primary,
                                    focusedLabelColor = GlassAccent.primary
                                )
                            )

                            Text(
                                text = "心情印章",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = tokens.textSecondary
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(MOOD_PRESETS) { preset ->
                                    val active = moodTag == preset
                                    Box(
                                        modifier = Modifier
                                            .noRipplePressable(pressedScale = 0.95f) { moodTag = preset }
                                            .background(
                                                if (active) GlassAccent.primary else GlassAccent.wash,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = preset,
                                            color = if (active) Color.White else GlassAccent.primaryDark,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = content,
                                onValueChange = { if (it.length <= 500) content = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("票根小记") },
                                shape = RoundedCornerShape(14.dp),
                                minLines = 3,
                                maxLines = 5,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GlassAccent.primary,
                                    focusedLabelColor = GlassAccent.primary
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    cropForReEdit?.let { source ->
        ImageCropperDialog(
            sourceBitmap = source,
            initialRatio = aspectRatio,
            onDismiss = { cropForReEdit = null },
            onCropConfirmed = { nextCropped, nextRatio ->
                cropForReEdit = null
                croppedBitmap = nextCropped
                aspectRatio = nextRatio
                uploadCroppedBitmap(nextCropped)
            }
        )
    }
}

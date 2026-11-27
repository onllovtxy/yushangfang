package com.xixikitchen.jetpack.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.snapTo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.xixikitchen.jetpack.data.BackendConfig
import com.xixikitchen.jetpack.data.CoupleSpace
import com.xixikitchen.jetpack.data.User
import com.xixikitchen.jetpack.ui.FallbackFoodAvatar
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.noRipplePressable
import com.xixikitchen.jetpack.ui.readAndCompressImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

fun realImageUrl(url: String?): String? = BackendConfig.resolveUrl(url)

fun statusText(status: Int): String = when (status) {
    0 -> "待接单"
    1 -> "已接单"
    2 -> "已完成"
    3 -> "已拒绝"
    else -> "未知"
}

@Composable
fun rememberPullRefreshState(globalLoading: Boolean): Pair<Boolean, () -> Unit> {
    var pullRefreshing by remember { mutableStateOf(false) }
    var pullRefreshStarted by remember { mutableStateOf(false) }
    LaunchedEffect(globalLoading, pullRefreshing) {
        if (pullRefreshing && globalLoading) {
            pullRefreshStarted = true
        }
        if (pullRefreshing && pullRefreshStarted && !globalLoading) {
            pullRefreshing = false
            pullRefreshStarted = false
        }
    }
    return pullRefreshing to {
        if (!pullRefreshing) {
            pullRefreshing = true
            pullRefreshStarted = false
        }
    }
}

@Composable
fun ErrorGlassDialog(
    message: String,
    onDismiss: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    AlertDialog(
        modifier = Modifier.glassConvexOverlay(24.dp),
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        text = {
            Text(
                message,
                color = tokens.textPrimary,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("知道了", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun ConfirmDeleteDialog(
    title: String = "确认删除",
    message: String,
    confirmText: String = "确认删除",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.glassConvexOverlay(24.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = tokens.textPrimary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.error)
            ) {
                Text(confirmText, fontWeight = FontWeight.Bold, color = Color.White)
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
fun CustomSnackbar(message: String) {
    val isSuccess = !message.contains("失败") &&
        !message.contains("错误") &&
        !message.contains("不匹配") &&
        !message.contains("无")
    val icon = if (isSuccess) "✨" else "⚠️"
    val textColor = if (isSuccess) GlassAccent.primaryDark else GlassAccent.errorDeep

    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 20.dp)
            .glassConvex(18.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 18.sp,
                modifier = Modifier.padding(end = 10.dp)
            )
            Text(
                text = message,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** 睁眼图标（密文可见时显示），Lucide "eye"，描边风格 */
private val EyeOpenIcon: ImageVector = ImageVector.Builder(
    name = "EyeOpen",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(2.062f, 12.348f)
        arcToRelative(1f, 1f, 0f, false, true, 0f, -0.696f)
        arcToRelative(10.75f, 10.75f, 0f, false, true, 19.876f, 0f)
        arcToRelative(1f, 1f, 0f, false, true, 0f, 0.696f)
        arcToRelative(10.75f, 10.75f, 0f, false, true, -19.876f, 0f)
        close()
    }
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(12f, 9f)
        arcToRelative(3f, 3f, 0f, false, true, 0f, 6f)
        arcToRelative(3f, 3f, 0f, false, true, 0f, -6f)
        close()
    }
}.build()

/** 闭眼图标（密文隐藏时显示），Lucide "eye-closed"，睫毛下垂描边风格 */
private val EyeClosedIcon: ImageVector = ImageVector.Builder(
    name = "EyeClosed",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(2f, 8f)
        arcToRelative(10.645f, 10.645f, 0f, false, false, 20f, 0f)
    }
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(15f, 18f)
        lineToRelative(-0.722f, -3.25f)
    }
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(20f, 15f)
        lineToRelative(-1.726f, -2.05f)
    }
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(4f, 15f)
        lineToRelative(1.726f, -2.05f)
    }
    path(
        fill = SolidColor(Color.Transparent),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(9f, 18f)
        lineToRelative(0.722f, -3.25f)
    }
}.build()

@Composable
fun GlassPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    defaultColors: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = leadingIcon,
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) EyeOpenIcon else EyeClosedIcon,
                    contentDescription = if (visible) "隐藏密码" else "显示密码",
                    tint = GlassAccent.primary
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        isError = isError,
        supportingText = supportingText,
        shape = shape,
        colors = if (defaultColors) {
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GlassAccent.primary,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedLabelColor = GlassAccent.primary,
                cursorColor = GlassAccent.primary
            )
        } else {
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GlassAccent.primary,
                focusedLabelColor = GlassAccent.primary,
                cursorColor = GlassAccent.primary
            )
        }
    )
}

@Composable
fun DishThumbnail(
    dishId: Long,
    dishName: String?,
    imageUrl: String?,
    size: Dp,
    emojiSize: TextUnit,
    cornerRadius: Dp
) {
    val model = realImageUrl(imageUrl)
    var failed by remember(model) { mutableStateOf(false) }
    if (model.isNullOrBlank() || failed) {
        FallbackFoodAvatar(
            dishId = dishId,
            dishName = dishName,
            size = size,
            emojiSize = emojiSize,
            cornerRadius = cornerRadius
        )
    } else {
        AsyncImage(
            model = model,
            contentDescription = dishName,
            onError = { failed = true },
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius))
                .background(GlassAccent.wash),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun UserMiniAvatar(
    avatarUrl: String?,
    size: Dp = 20.dp
) {
    val model = realImageUrl(avatarUrl)
    if (!model.isNullOrBlank()) {
        AsyncImage(
            model = model,
            contentDescription = null,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Color.White),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(GlassAccent.wash),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = GlassAccent.primary,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

@Composable
fun UserMiniAvatar(user: User?, size: Dp = 20.dp) {
    UserMiniAvatar(avatarUrl = user?.avatarUrl, size = size)
}

@Composable
fun StatusFilterChip(
    active: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = modifier
            .noRipplePressable(pressedScale = 0.96f, onClick = onClick)
            .then(
                if (active) Modifier.glassConvex(16.dp)
                else Modifier.glassConcave(16.dp)
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (active) GlassAccent.primary else tokens.textSecondary,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (active) FontWeight.ExtraBold else FontWeight.Medium
            )
        )
    }
}

@Composable
fun ListAction(
    title: String,
    icon: ImageVector,
    value: String? = null,
    onClick: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .noRipplePressable(pressedScale = 0.985f, onClick = onClick)
            .glassConvex(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(GlassAccent.wash, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = GlassAccent.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = tokens.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.textSecondary
                )
                Spacer(Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = tokens.textSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** 左滑揭示的两种停靠状态：闭合 / 展开(露出删除按钮) */
private enum class RevealValue { Closed, Open }

/**
 * iOS 风格左滑组件：向左滑动一段距离后停住，露出右侧删除按钮，
 * 点击按钮才执行删除；右滑或点击按钮后收起。
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeRevealBox(
    swipeEnabled: Boolean = true,
    onDeleteClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val revealWidth = screenWidthDp / 6f
    val density = LocalDensity.current
    val revealPx = with(density) { revealWidth.toPx() }
    val scope = rememberCoroutineScope()
    val state = remember(revealPx) {
        AnchoredDraggableState(
            initialValue = RevealValue.Closed,
            anchors = DraggableAnchors {
                RevealValue.Closed at 0f
                RevealValue.Open at -revealPx
            },
            confirmValueChange = { true }
        )
    }

    LaunchedEffect(swipeEnabled) {
        if (!swipeEnabled && state.currentValue != RevealValue.Closed) {
            state.snapTo(RevealValue.Closed)
        }
    }

    Box(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(start = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .width(revealWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(GlassAccent.error)
                    .clickable {
                        scope.launch { state.animateTo(RevealValue.Closed) }
                        onDeleteClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("删除", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset {
                    val off = state.requireOffset()
                    IntOffset(if (off.isNaN()) 0 else off.roundToInt(), 0)
                }
                .then(
                    if (swipeEnabled) {
                        Modifier.anchoredDraggable(state, Orientation.Horizontal)
                    } else {
                        Modifier
                    }
                )
        ) {
            content()
        }
    }
}

@Composable
fun DirectImageUploadField(
    value: String,
    label: String,
    emptyText: String,
    uploading: Boolean,
    filePrefix: String,
    onUpload: (bytes: ByteArray, fileName: String, onSuccess: (String) -> Unit, onFailure: () -> Unit) -> Unit,
    onUploadingChange: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
    circular: Boolean = false,
    previewHeight: Dp = 132.dp
) {
    val tokens = LocalGlassTokens.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        onUploadingChange(true)
        scope.launch(Dispatchers.IO) {
            try {
                val processed = readAndCompressImage(context, uri)
                if (processed == null) {
                    withContext(Dispatchers.Main) {
                        onUploadingChange(false)
                    }
                    return@launch
                }
                withContext(Dispatchers.Main) {
                    onUpload(
                        processed.bytes,
                        "${filePrefix}_${System.currentTimeMillis()}.${processed.extension}",
                        { url ->
                            onValueChange(url)
                            onUploadingChange(false)
                        },
                        {
                            onUploadingChange(false)
                        }
                    )
                }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    onUploadingChange(false)
                }
            }
        }
    }
    val shape = if (circular) CircleShape else RoundedCornerShape(18.dp)
    val imageUrl = realImageUrl(value)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (circular) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = tokens.textSecondary
        )
        Box(
            modifier = Modifier
                .then(if (circular) Modifier.size(92.dp) else Modifier.fillMaxWidth().height(previewHeight))
                .noRipplePressable(enabled = !uploading, pressedScale = 0.98f) { launcher.launch("image/*") }
                .clip(shape)
                .background(GlassAccent.wash)
                .border(BorderStroke(1.dp, GlassAccent.stroke), shape),
            contentAlignment = Alignment.Center
        ) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = label,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "更换图片",
                        tint = Color.White,
                        modifier = Modifier.size(if (circular) 24.dp else 28.dp)
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = label,
                        tint = GlassAccent.primary,
                        modifier = Modifier.size(if (circular) 26.dp else 32.dp)
                    )
                    Text(
                        emptyText,
                        color = GlassAccent.primary,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
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
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        if (uploading) {
            Text(
                text = "正在上传...",
                style = MaterialTheme.typography.bodySmall,
                color = GlassAccent.primary
            )
        }
    }
}

@Composable
fun PhotoLightboxDialog(
    imageUrl: String,
    caption: String? = null,
    onDismiss: () -> Unit
) {
    val resolved = realImageUrl(imageUrl) ?: imageUrl
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.90f))
                .clickable(onClick = onDismiss)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = resolved,
                    contentDescription = caption ?: "成品照片",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Fit
                )
                if (!caption.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = caption,
                        color = Color.White.copy(alpha = 0.92f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.16f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭预览",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun SuperAdminSpaceBanner(
    isSuperAdmin: Boolean,
    currentSpace: CoupleSpace?,
    spaces: List<CoupleSpace>,
    onSelectSpace: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isSuperAdmin) return
    val tokens = LocalGlassTokens.current
    var showPicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .noRipplePressable(pressedScale = 0.985f) { showPicker = true }
            .glassConvex(18.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currentSpace?.name?.takeIf { it.isNotBlank() } ?: "加载中...",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = tokens.textPrimary,
                maxLines = 1
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .glassConcave(12.dp)
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "切换空间",
                tint = GlassAccent.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "切换",
                color = GlassAccent.primary,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            modifier = Modifier.glassConvexOverlay(24.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.Transparent,
            title = {
                Text(
                    "切换情侣空间",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassAccent.primaryDark
                )
            },
            text = {
                if (spaces.isEmpty()) {
                    Text("暂无可切换的情侣空间", color = tokens.textSecondary)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(spaces, key = { it.id }) { space ->
                            val selected = currentSpace?.id == space.id
                            val memberNames = space.members
                                .mapNotNull { it.nickname?.takeIf { n -> n.isNotBlank() } }
                                .joinToString(" & ")
                                .ifBlank { "暂无成员" }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .noRipplePressable(pressedScale = 0.97f) {
                                        showPicker = false
                                        onSelectSpace(space.id)
                                    }
                                    .then(
                                        if (selected) Modifier.glassConvexOverlay(16.dp)
                                        else Modifier.glassConcave(16.dp)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = space.name.ifBlank { "空间 #${space.id}" },
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (selected) GlassAccent.primaryDark else tokens.textPrimary
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "成员: $memberNames",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = tokens.textSecondary
                                    )
                                }
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "当前选中",
                                        tint = GlassAccent.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("关闭", color = tokens.textSecondary)
                }
            }
        )
    }
}

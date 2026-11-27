package com.xixikitchen.jetpack.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.xixikitchen.jetpack.data.OrderUserInfo
import com.xixikitchen.jetpack.ui.components.ListAction
import com.xixikitchen.jetpack.ui.components.realImageUrl
import com.xixikitchen.jetpack.ui.designsystem.theme.AppTheme
import com.xixikitchen.jetpack.ui.designsystem.theme.CustomTheme
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.LocalGlassTokens
import com.xixikitchen.jetpack.ui.designsystem.theme.ResolvedTheme
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConcave
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvex
import com.xixikitchen.jetpack.ui.designsystem.theme.glassConvexOverlay
import com.xixikitchen.jetpack.ui.designsystem.theme.themePreviewBackground
import com.xixikitchen.jetpack.ui.designsystem.theme.tokens

/**
 * 设置页。放置伴侣配对卡片、管理入口与主题入口。
 */
@Composable
fun SettingsScreen(
    state: KitchenUiState,
    vm: KitchenViewModel,
    current: ResolvedTheme,
    onOpenAdmin: () -> Unit,
    onOpenThemePicker: () -> Unit,
    onBack: () -> Unit
) {
    val tokens = LocalGlassTokens.current
    var showPairDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.refreshCoupleSpace()
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsHeader(title = "设置", onBack = onBack)

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!state.isSuperAdmin) {
                    val space = state.coupleSpace
                    val partner = space?.partner ?: state.recipients.firstOrNull()?.let {
                        OrderUserInfo(it.id, it.nickname, it.avatarUrl)
                    }
                    val isPaired = space?.paired == true || partner != null
                    val partnerName = partner?.nickname?.takeIf { it.isNotBlank() }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassConvex(22.dp)
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = GlassAccent.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isPaired && partnerName != null) "伴侣：$partnerName" else "伴侣：无",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = tokens.textPrimary
                                )
                            }

                            if (isPaired && partner != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .glassConcave(16.dp)
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val avatarUrl = partner.avatarUrl
                                    if (!avatarUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = realImageUrl(avatarUrl),
                                            contentDescription = "Partner Avatar",
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .glassConvex(22.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(CircleShape)
                                                .glassConvex(22.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = GlassAccent.primary,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = partnerName ?: "伴侣",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = tokens.textPrimary
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .noRipplePressable(pressedScale = 0.98f) { showPairDialog = true }
                                        .glassConcave(16.dp)
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = GlassAccent.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "去绑定",
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                        color = GlassAccent.primaryDark
                                    )
                                }
                            }
                        }
                    }
                }

                ListAction(
                    title = if (state.isSuperAdmin) "超管控制台" else "厨房与菜单管理",
                    icon = Icons.Default.AdminPanelSettings,
                    onClick = onOpenAdmin
                )

                ListAction(
                    title = "切换主题",
                    icon = Icons.Default.Palette,
                    value = current.label,
                    onClick = onOpenThemePicker
                )
            }

            Spacer(Modifier.height(28.dp))
        }
    }

    if (showPairDialog) {
        PairByCodeDialog(
            myPairCode = state.coupleSpace?.pairCode.orEmpty(),
            loading = state.loading,
            onDismiss = { showPairDialog = false },
            onConfirm = { code ->
                vm.bindPartnerByCode(code) {
                    showPairDialog = false
                }
            }
        )
    }
}

private fun copyPairCodeToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText("配对码", text))
    Toast.makeText(context, "已复制: $text", Toast.LENGTH_SHORT).show()
}

@Composable
private fun PairByCodeDialog(
    myPairCode: String,
    loading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val tokens = LocalGlassTokens.current
    val context = LocalContext.current
    var pairCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.glassConvexOverlay(24.dp),
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.Transparent,
        title = {
            Text(
                "绑定伴侣",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (myPairCode.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .noRipplePressable(pressedScale = 0.98f) {
                                copyPairCodeToClipboard(context, myPairCode)
                            }
                            .glassConcave(14.dp)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "我的配对码：$myPairCode",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = GlassAccent.primaryDark
                        )
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "复制",
                            tint = GlassAccent.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                OutlinedTextField(
                    value = pairCode,
                    onValueChange = { pairCode = it.uppercase().take(16) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("对方配对码") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassAccent.primary,
                        focusedLabelColor = GlassAccent.primary
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(pairCode.trim()) },
                enabled = !loading && pairCode.trim().length >= 4,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
            ) {
                Text("确认绑定", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !loading) {
                Text("取消", color = tokens.textSecondary)
            }
        }
    )
}

/** 主题选择页。内置六套 + 自定义若干套,点哪套立刻全局生效。 */
@Composable
fun ThemePickerScreen(
    currentId: String,
    customs: List<CustomTheme>,
    onSelectTheme: (String) -> Unit,
    onSaveCustom: (CustomTheme) -> Unit,
    onDeleteCustom: (String) -> Unit,
    onBack: () -> Unit,
) {
    var creating by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<CustomTheme?>(null) }
    var menuFor by remember { mutableStateOf<CustomTheme?>(null) }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsHeader(title = "切换主题", onBack = onBack)

            SectionLabel("内置主题")
            ThemeGrid(
                entries = AppTheme.entries.map {
                    ThemeEntry(it.id, it.label, it.tokens())
                },
                currentId = currentId,
                onSelect = onSelectTheme,
                onLongPress = null,
            )

            SectionLabel("我的主题")
            ThemeGrid(
                entries = customs.map {
                    ThemeEntry(it.id, it.name, it.toTokens())
                },
                currentId = currentId,
                onSelect = onSelectTheme,
                onLongPress = { entry -> menuFor = customs.firstOrNull { it.id == entry.id } },
                trailing = { CreateThemeCard(onClick = { creating = true }) },
            )

            Spacer(Modifier.height(28.dp))
        }
    }

    if (creating || editing != null) {
        CustomThemeDialog(
            initial = editing,
            onDismiss = {
                creating = false
                editing = null
            },
            onSave = { saved ->
                onSaveCustom(saved)
                onSelectTheme(saved.id)
                creating = false
                editing = null
            }
        )
    }

    menuFor?.let { target ->
        AlertDialog(
            onDismissRequest = { menuFor = null },
            modifier = Modifier.glassConvexOverlay(24.dp),
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.Transparent,
            title = {
                Text(
                    text = target.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = GlassAccent.primaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        editing = target
                        menuFor = null
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GlassAccent.primary)
                ) {
                    Text("编辑", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { menuFor = null }) {
                        Text("取消", color = LocalGlassTokens.current.textSecondary)
                    }
                    TextButton(
                        onClick = {
                            onDeleteCustom(target.id)
                            menuFor = null
                        }
                    ) {
                        Text("删除", color = GlassAccent.error)
                    }
                }
            }
        )
    }
}

private data class ThemeEntry(
    val id: String,
    val label: String,
    val tokens: GlassTokens,
)

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = LocalGlassTokens.current.textSecondary,
        modifier = Modifier.padding(start = 22.dp, top = 16.dp, bottom = 10.dp)
    )
}

/** 两列网格。自定义那一段末尾会补一张"新建"卡。 */
@Composable
private fun ThemeGrid(
    entries: List<ThemeEntry>,
    currentId: String,
    onSelect: (String) -> Unit,
    onLongPress: ((ThemeEntry) -> Unit)?,
    trailing: (@Composable () -> Unit)? = null,
) {
    // 自定义那一段末尾补一个空槽,让"自定义主题"卡跟在最后一张卡后面
    val cells: List<ThemeEntry?> =
        if (trailing != null) entries + listOf<ThemeEntry?>(null) else entries
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        cells.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { entry ->
                    if (entry != null) {
                        ThemeCard(
                            label = entry.label,
                            previewTokens = entry.tokens,
                            selected = entry.id == currentId,
                            onClick = { onSelect(entry.id) },
                            onLongClick = onLongPress?.let { handler -> { handler(entry) } },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Box(Modifier.weight(1f)) { trailing?.invoke() }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CreateThemeCard(onClick: () -> Unit) {
    val tokens = LocalGlassTokens.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .noRipplePressable(pressedScale = 0.97f, onClick = onClick)
            .glassConcave(20.dp)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(BorderStroke(1.dp, GlassAccent.stroke), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = GlassAccent.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(9.dp))
        Text(
            text = "自定义主题",
            color = tokens.textPrimary,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            maxLines = 1
        )
        Spacer(Modifier.height(2.dp))
    }
}

@Composable
private fun SettingsHeader(title: String, onBack: () -> Unit) {
    val tokens = LocalGlassTokens.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .glassConvex(24.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    tint = GlassAccent.primary
                )
            }
            Spacer(Modifier.width(6.dp))
            Text(
                text = title,
                color = tokens.textPrimary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
            )
        }
    }
}

/**
 * 一张主题卡。上面那条色块是**真实的主题背景**本身(用 themePreviewBackground
 * 直接跑主题的绘制逻辑),不是另画一张示意图 —— 预览和切过去之后看到的必须一致。
 *
 * 只有自定义主题会传 [onLongClick] —— 内置六套长按没有反应,它们不可删也不可改。
 */
@Composable
private fun ThemeCard(
    label: String,
    previewTokens: GlassTokens,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalGlassTokens.current
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .then(
                if (onLongClick != null) {
                    Modifier.noRippleCombinedPressable(
                        pressedScale = 0.97f,
                        onLongClick = onLongClick,
                        onClick = onClick
                    )
                } else {
                    Modifier.noRipplePressable(pressedScale = 0.97f, onClick = onClick)
                }
            )
            .glassConvex(20.dp)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(shape)
                .themePreviewBackground(previewTokens)
                .border(
                    BorderStroke(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) tokens.primary else tokens.borderLo
                    ),
                    shape
                ),
            contentAlignment = Alignment.BottomEnd
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(22.dp)
                        .background(tokens.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "已选中",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(9.dp))
        Text(
            text = label,
            color = if (selected) tokens.primary else tokens.textPrimary,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
            ),
            maxLines = 1
        )
        Spacer(Modifier.height(2.dp))
    }
}

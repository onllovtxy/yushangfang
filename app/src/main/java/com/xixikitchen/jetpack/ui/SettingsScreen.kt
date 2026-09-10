package com.xixikitchen.jetpack.ui

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
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
 * 设置页。这里只放"入口",具体的选项进下一级再选 ——
 * iOS 的设置也是这个层级关系:一屏是分类,点进去才是具体选项。
 */
@Composable
fun SettingsScreen(
    current: ResolvedTheme,
    onOpenThemePicker: () -> Unit,
    onBack: () -> Unit
) {
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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

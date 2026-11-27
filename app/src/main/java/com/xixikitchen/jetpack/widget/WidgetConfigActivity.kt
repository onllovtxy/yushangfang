package com.xixikitchen.jetpack.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.xixikitchen.jetpack.data.Anniversary
import com.xixikitchen.jetpack.data.ApiResponse
import com.xixikitchen.jetpack.data.ApiService
import com.xixikitchen.jetpack.data.BackendConfig
import com.xixikitchen.jetpack.data.FoodDiary
import com.xixikitchen.jetpack.data.SessionStore
import com.xixikitchen.jetpack.widget.WidgetRefresh
import com.xixikitchen.jetpack.ui.components.realImageUrl
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassAccent
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassMeshBackground
import com.xixikitchen.jetpack.ui.designsystem.theme.GlassTheme
import com.xixikitchen.jetpack.ui.designsystem.theme.resolveTheme
import com.xixikitchen.jetpack.ui.designsystem.theme.ThemeStore
import com.xixikitchen.jetpack.ui.noRipplePressable

/**
 * 添加 widget 时的"选哪张卡片"页面。
 *
 * 由 `android:configure` 声明在 widget-provider XML 里，系统在放置 widget 前拉起。
 * 选完把 appWidgetId → itemId 写进 WidgetStore，然后返回。
 */
class WidgetConfigActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var type: WidgetType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 系统约定：默认结果是取消，用户选完才改成 OK，否则桌面不会放置 widget
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        type = widgetTypeOf(intent?.component?.className)

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // 缓存可能还是空的（用户从桌面直接添加，App 里从没打开过对应页），
        // 这里先异步拉一次，成功后用 system 广播让界面重组
        WidgetRefresh.refreshNow(this, type)

        val themeStore = ThemeStore(this)
        val resolved = resolveTheme(themeStore.loadSelection(), themeStore.loadCustoms())

        setContent {
            GlassTheme(tokens = resolved.tokens) {
                Box(Modifier.fillMaxSize()) {
                    GlassMeshBackground(resolved.tokens, Modifier.fillMaxSize())
                    WidgetPickerScreen(
                        type = type,
                        onPick = { itemId -> finishWith(itemId) },
                        onCancel = { finish() }
                    )
                }
            }
        }
    }

    private fun finishWith(itemId: Long) {
        WidgetStore.saveSelected(this, type, appWidgetId, itemId)
        WidgetRefresher.refreshAll(this, type)
        val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(RESULT_OK, result)
        finish()
    }

    private fun widgetTypeOf(className: String?): WidgetType = when (className) {
        AnniversaryWidgetProvider::class.java.name -> WidgetType.Anniversary
        DiaryWidgetProvider::class.java.name -> WidgetType.Diary
        else -> WidgetType.Diary
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetPickerScreen(
    type: WidgetType,
    onPick: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val title = if (type == WidgetType.Anniversary) "选择要显示的纪念日" else "选择要显示的票根"

    val anniversaries: List<Anniversary> =
        if (type == WidgetType.Anniversary) {
            WidgetStore.loadItems(context, WidgetType.Anniversary, Anniversary::class.java)
        } else emptyList()

    val diaries: List<FoodDiary> =
        if (type == WidgetType.Diary) {
            WidgetStore.loadItems(context, WidgetType.Diary, FoodDiary::class.java)
        } else emptyList()

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "选一张放到桌面，之后可以在 App 里重新指定",
                style = MaterialTheme.typography.bodySmall,
                color = GlassAccent.primary.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(16.dp))

            val empty = anniversaries.isEmpty() && diaries.isEmpty()
            if (empty) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "还没有内容，先去 App 里添加一条吧",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GlassAccent.primary.copy(alpha = 0.7f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(anniversaries, key = { "ann_${it.id}" }) { item ->
                        PickerRow(
                            title = item.title,
                            subtitle = item.eventDate,
                            imageUrl = null,
                            badge = if (item.isToday) "就是今天" else "还有 ${item.daysCount} 天",
                            onClick = { onPick(item.id) }
                        )
                    }
                    items(diaries, key = { "diary_${it.id}" }) { entry ->
                        PickerRow(
                            title = entry.title.ifBlank { "今日份美味" },
                            subtitle = entry.location.ifBlank { "熙熙的小厨房" },
                            imageUrl = realImageUrl(entry.imageUrl),
                            badge = entry.moodTag,
                            onClick = { onPick(entry.id) }
                        )
                    }
                }
            }

            TextButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text("取消", color = GlassAccent.primary.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun PickerRow(
    title: String,
    subtitle: String,
    imageUrl: String?,
    badge: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRipplePressable(pressedScale = 0.98f, onClick = onClick)
            .background(Color.White.copy(alpha = 0.75f), RoundedCornerShape(18.dp))
            .border(1.dp, GlassAccent.primary.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (imageUrl != null) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(54.dp)
                    .background(GlassAccent.primary.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = GlassAccent.primaryDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = GlassAccent.primary.copy(alpha = 0.65f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (badge.isNotBlank()) {
            Box(
                modifier = Modifier
                    .background(GlassAccent.primary.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = GlassAccent.primaryDark
                )
            }
        }
        Spacer(Modifier.width(2.dp))
    }
}

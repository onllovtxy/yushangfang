package com.xixikitchen.jetpack.widget

import android.content.Context
import com.xixikitchen.jetpack.data.Anniversary
import com.xixikitchen.jetpack.data.CoupleMeetup
import com.xixikitchen.jetpack.data.FoodDiary
import com.xixikitchen.jetpack.ui.components.realImageUrl

/** 桌面卡片要展示的纯数据，与 Compose 解耦，widget 进程可直接渲染。 */
data class WidgetCard(
    // 纪念日
    val title: String = "",
    val prefix: String = "",
    val bigNumber: String = "",
    val unit: String = "天",
    // 票根
    val imageUrl: String? = null,
    val ticketNo: String = "",
    val location: String = "",
    val moodTag: String = "",
    val dateLabel: String = "",
    val content: String = ""
)

object WidgetCardRenderer {
    fun render(context: Context, type: WidgetType, appWidgetId: Int): WidgetCard {
        val selectedId = WidgetStore.selected(context, type, appWidgetId)
        return when (type) {
            WidgetType.Anniversary -> renderAnniversary(context, selectedId)
            WidgetType.Diary -> renderDiary(context, selectedId)
        }
    }

    private fun renderAnniversary(context: Context, selectedId: Long): WidgetCard {
        val items: List<Anniversary> = WidgetStore.loadItems(context, WidgetType.Anniversary, Anniversary::class.java)
        if (items.isEmpty()) {
            return WidgetCard(title = "", prefix = "", bigNumber = "--", unit = "天")
        }
        val item = items.firstOrNull { it.id == selectedId } ?: items.first()
        // daysCount：正数=未来还有 N 天；负数=已过去 N 天
        val days = item.daysCount
        val isFuture = days >= 0
        val absDays = kotlin.math.abs(days)
        val prefix = when {
            item.isToday -> "就是今天"
            item.type == "countup" && !isFuture -> "已经在一起"
            item.type == "countup" -> "还有"
            isFuture -> "还有"
            else -> "已经过去"
        }
        return WidgetCard(
            title = item.title,
            prefix = prefix,
            bigNumber = absDays.toString(),
            unit = "天"
        )
    }

    private fun renderDiary(context: Context, selectedId: Long): WidgetCard {
        val items: List<FoodDiary> = WidgetStore.loadItems(context, WidgetType.Diary, FoodDiary::class.java)
        if (items.isEmpty()) {
            return WidgetCard(ticketNo = "NO.TICKET", title = "还没有票根")
        }
        val entry = items.firstOrNull { it.id == selectedId } ?: items.first()
        return WidgetCard(
            title = entry.title.ifBlank { "今日份美味" },
            imageUrl = realImageUrl(entry.imageUrl),
            ticketNo = entry.ticketNo.ifBlank { "NO.2026-0520" },
            location = entry.location.ifBlank { "熙熙的小厨房" },
            moodTag = entry.moodTag.ifBlank { "超满足" },
            dateLabel = entry.createdAt?.take(10)?.replace('-', '.') ?: "",
            content = entry.content
        )
    }
}

object WidgetStoreCodec {
    fun encodeCard(card: WidgetCard): String = com.google.gson.Gson().toJson(card)
}

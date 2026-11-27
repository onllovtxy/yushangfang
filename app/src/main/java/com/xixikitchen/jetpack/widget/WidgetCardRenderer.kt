package com.xixikitchen.jetpack.widget

import android.content.Context
import com.google.gson.Gson
import com.xixikitchen.jetpack.data.Anniversary
import com.xixikitchen.jetpack.data.CoupleMeetup
import com.xixikitchen.jetpack.data.FoodDiary
import com.xixikitchen.jetpack.ui.components.realImageUrl

/** 桌面卡片要展示的纯数据，与 Compose 解耦，widget 进程可直接渲染。 */
data class WidgetCard(
    val title: String,
    val subtitle: String,
    val badge: String = "",
    val date: String = "",
    val imageUrl: String? = null,
    val accent: Int = 0xD97724
)

object WidgetCardRenderer {
    fun render(context: Context, type: WidgetType, appWidgetId: Int): WidgetCard {
        val selectedId = WidgetStore.selected(context, type, appWidgetId)
        return when (type) {
            WidgetType.Meetup -> renderMeetup(context, selectedId)
            WidgetType.Anniversary -> renderAnniversary(context, selectedId)
            WidgetType.Diary -> renderDiary(context, selectedId)
        }
    }

    private fun renderMeetup(context: Context, selectedId: Long): WidgetCard {
        val items: List<CoupleMeetup> = WidgetStore.loadItems(context, WidgetType.Meetup, CoupleMeetup::class.java)
        if (items.isEmpty()) return emptyCard("见面日记", "打开 App 记录第一次见面")
        val meetup = items.firstOrNull { it.id == selectedId } ?: items.first()
        val range = meetup.arriveDate.replace('-', '.') + " → " + meetup.leaveDate.replace('-', '.')
        val badge = when (meetup.status) {
            "ongoing" -> "正在见面中"
            "upcoming" -> "还有 ${meetup.daysUntilMeet ?: 0} 天见面"
            else -> "第 ${meetup.meetIndex} 次见面"
        }
        return WidgetCard(
            title = "第 ${meetup.meetIndex} 次见面",
            subtitle = if (meetup.city.isNotBlank()) meetup.city else meetup.note.ifBlank { range },
            badge = badge,
            date = range,
            imageUrl = firstPhoto(meetup.photoUrl1, meetup.photoUrl2)
        )
    }

    private fun renderAnniversary(context: Context, selectedId: Long): WidgetCard {
        val items: List<Anniversary> = WidgetStore.loadItems(context, WidgetType.Anniversary, Anniversary::class.java)
        if (items.isEmpty()) return emptyCard("纪念日", "打开 App 添加纪念日")
        val item = items.firstOrNull { it.id == selectedId } ?: items.first()
        val badge = when {
            item.isToday -> "就是今天"
            item.daysCount > 0 -> "还有 ${item.daysCount} 天"
            else -> "已过 ${-item.daysCount} 天"
        }
        return WidgetCard(
            title = item.title,
            subtitle = item.note.ifBlank { item.eventDate },
            badge = badge,
            date = item.eventDate
        )
    }

    private fun renderDiary(context: Context, selectedId: Long): WidgetCard {
        val items: List<FoodDiary> = WidgetStore.loadItems(context, WidgetType.Diary, FoodDiary::class.java)
        if (items.isEmpty()) return emptyCard("美食票根", "打开 App 制作第一张票根")
        val entry = items.firstOrNull { it.id == selectedId } ?: items.first()
        return WidgetCard(
            title = entry.title,
            subtitle = entry.content.ifBlank { entry.location },
            badge = entry.moodTag,
            date = entry.createdAt?.take(10)?.replace('-', '.') ?: "",
            imageUrl = realImageUrl(entry.imageUrl)
        )
    }

    private fun firstPhoto(a: String, b: String): String? =
        realImageUrl(a.takeIf { it.isNotBlank() } ?: b.takeIf { it.isNotBlank() })

    private fun emptyCard(title: String, hint: String) =
        WidgetCard(title = title, subtitle = hint, badge = "还没有内容")
}

object WidgetStoreCodec {
    fun encodeCard(card: WidgetCard): String = Gson().toJson(card)
}

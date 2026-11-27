package com.xixikitchen.jetpack.widget

import android.content.Context
import com.xixikitchen.jetpack.data.Anniversary
import com.xixikitchen.jetpack.data.CoupleMeetup
import com.xixikitchen.jetpack.data.FoodDiary

/**
 * App 内「长按卡片 → 添加到桌面」的统一入口：
 * 缓存整份列表 + 记录选中的卡片 + 下载封面 + 刷新桌面 widget。
 */
object WidgetPublish {

    /** [downloadImages] = false 时只落数据不拉封面，用于 App 内常规数据同步。 */
    suspend fun publishMeetups(
        context: Context,
        items: List<CoupleMeetup>,
        downloadImages: Boolean = true
    ) {
        WidgetStore.saveItems(context, WidgetType.Meetup, items)
        if (downloadImages) {
            WidgetImageDownloader.cacheImages(
                context,
                WidgetType.Meetup,
                items.flatMap { listOf(it.photoUrl1, it.photoUrl2) }
            )
        }
        WidgetRefresher.refreshAll(context, WidgetType.Meetup)
    }

    suspend fun publishAnniversaries(context: Context, items: List<Anniversary>) {
        WidgetStore.saveItems(context, WidgetType.Anniversary, items)
        WidgetRefresher.refreshAll(context, WidgetType.Anniversary)
    }

    suspend fun publishDiaries(
        context: Context,
        items: List<FoodDiary>,
        downloadImages: Boolean = true
    ) {
        WidgetStore.saveItems(context, WidgetType.Diary, items)
        if (downloadImages) {
            WidgetImageDownloader.cacheImages(context, WidgetType.Diary, items.map { it.imageUrl })
        }
        WidgetRefresher.refreshAll(context, WidgetType.Diary)
    }

}

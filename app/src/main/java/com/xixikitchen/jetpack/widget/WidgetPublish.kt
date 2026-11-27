package com.xixikitchen.jetpack.widget

import android.content.Context
import com.xixikitchen.jetpack.data.Anniversary
import com.xixikitchen.jetpack.data.FoodDiary

/**
 * App 内「长按卡片 → 添加到桌面」的统一入口：
 * 缓存整份列表 + 记录选中的卡片 + 下载封面 + 刷新桌面 widget。
 */
object WidgetPublish {

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

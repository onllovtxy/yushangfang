package com.xixikitchen.jetpack.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

/**
 * 接收系统「已把 widget 放到桌面」的结果，把用户长按选中的那张卡片绑定到新 widget 上。
 *
 * 这是 requestPinAppWidget 的回调，比"猜最近新增的 widget"可靠。
 */
class WidgetPinResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = WidgetType.entries.firstOrNull { it.key == intent.getStringExtra(WidgetPinner.EXTRA_TYPE) }
            ?: return
        val itemId = intent.getLongExtra(WidgetPinner.EXTRA_ITEM_ID, -1L)
        val newId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )
        if (itemId < 0 || newId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        WidgetStore.saveSelected(context, type, newId, itemId)

        val manager = AppWidgetManager.getInstance(context)
        val provider = ComponentName(context, WidgetPinner.providerClass(type))
        manager.updateAppWidget(
            newId,
            WidgetRefresher.buildViews(
                context,
                when (type) {
                    WidgetType.Meetup -> MeetupWidgetProvider()
                    WidgetType.Anniversary -> AnniversaryWidgetProvider()
                    WidgetType.Diary -> DiaryWidgetProvider()
                },
                type,
                newId
            )
        )
        // 顺带找一下桌面上的其它同类 widget，让它们也拿到最新数据
        WidgetRefresher.refreshAll(context, type)
        manager.getAppWidgetIds(provider) // 触发一次系统查询，保证 provider 已注册
    }
}

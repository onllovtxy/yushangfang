package com.xixikitchen.jetpack.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews

/**
 * 三个小组件共用的基类：所有 provider 只是换一个 [WidgetType] 和布局。
 *
 * 每个卡片对应的数据（见面/纪念日/票根）在 App 内「长按卡片 → 添加到桌面」时，
 * 由 [WidgetPublish] 写入 [WidgetStore] 并刷新所有已放置的 widget。
 */
abstract class BaseCardWidgetProvider : AppWidgetProvider() {

    abstract val type: WidgetType

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            appWidgetManager.updateAppWidget(id, WidgetRefresher.buildViews(context, this, type, id))
        }
    }

    /** 桌面拉伸/缩放时系统会回调这里，按新的宽高重画。 */
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        appWidgetManager.updateAppWidget(
            appWidgetId,
            WidgetRefresher.buildViews(context, this, type, appWidgetId, newOptions)
        )
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { WidgetStore.clearSelected(context, type, it) }
    }
}

class MeetupWidgetProvider : BaseCardWidgetProvider() {
    override val type = WidgetType.Meetup
}

class AnniversaryWidgetProvider : BaseCardWidgetProvider() {
    override val type = WidgetType.Anniversary
}

class DiaryWidgetProvider : BaseCardWidgetProvider() {
    override val type = WidgetType.Diary
}

/** 统一构造 RemoteViews：整块点击打开 App 的「日记」页。 */
object WidgetRefresher {
    fun buildViews(
        context: Context,
        provider: BaseCardWidgetProvider,
        type: WidgetType,
        appWidgetId: Int,
        options: Bundle? = null
    ): RemoteViews {
        val views = RemoteViews(context.packageName, WidgetLayouts.layoutFor(type))
        val card = WidgetCardRenderer.render(context, type, appWidgetId)
        val manager = AppWidgetManager.getInstance(context)
        val opts = options ?: manager.getAppWidgetOptions(appWidgetId)
        val (wPx, hPx) = pixelSize(context, opts)
        views.setImageViewBitmap(
            WidgetLayouts.IMAGE_ID,
            CardBitmapFactory.create(context, type, card, wPx, hPx)
        )
        views.setContentDescription(
            WidgetLayouts.ROOT_ID,
            card.title + " " + card.subtitle
        )
        views.setOnClickPendingIntent(
            WidgetLayouts.ROOT_ID,
            WidgetIntents.openApp(context, type)
        )
        return views
    }

    private fun pixelSize(context: Context, options: Bundle?): Pair<Int, Int> {
        if (options == null) {
            return WidgetSize.fallbackWidth(context) to WidgetSize.fallbackHeight(context)
        }
        val minW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
        val maxW = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, 0)
        val minH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
        val maxH = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0)
        val widthDp = maxOf(minW, maxW, 110)
        val heightDp = maxOf(minH, maxH, 110)
        val metrics = context.resources.displayMetrics
        val w = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthDp.toFloat(), metrics).toInt()
        val h = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp.toFloat(), metrics).toInt()
        return w to h
    }

    /** 刷新所有已添加的本类 widget（App 内数据变化 / 用户切换卡片时调用）。 */
    fun refreshAll(context: Context, type: WidgetType) {
        val manager = AppWidgetManager.getInstance(context)
        val cls = when (type) {
            WidgetType.Meetup -> MeetupWidgetProvider::class.java
            WidgetType.Anniversary -> AnniversaryWidgetProvider::class.java
            WidgetType.Diary -> DiaryWidgetProvider::class.java
        }
        val ids = manager.getAppWidgetIds(ComponentName(context, cls))
        ids.forEach { id ->
            val provider = when (type) {
                WidgetType.Meetup -> MeetupWidgetProvider()
                WidgetType.Anniversary -> AnniversaryWidgetProvider()
                WidgetType.Diary -> DiaryWidgetProvider()
            }
            manager.updateAppWidget(id, buildViews(context, provider, type, id))
        }
    }
}

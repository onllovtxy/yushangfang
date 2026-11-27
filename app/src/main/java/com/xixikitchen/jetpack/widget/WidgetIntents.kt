package com.xixikitchen.jetpack.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.xixikitchen.jetpack.MainActivity
import com.xixikitchen.jetpack.R

object WidgetLayouts {
    val layoutAnniversary = R.layout.widget_card_anniversary
    val layoutDiary = R.layout.widget_card_diary

    val ROOT_ID = R.id.widget_root
    val IMAGE_ID = R.id.widget_image

    fun layoutFor(type: WidgetType): Int = when (type) {
        WidgetType.Anniversary -> layoutAnniversary
        WidgetType.Diary -> layoutDiary
    }
}

object WidgetIntents {
    /** 专用 action：保证每次点击都是一次新 Intent，能可靠走到 onNewIntent。 */
    const val ACTION_OPEN = "com.xixikitchen.jetpack.OPEN_FROM_WIDGET"

    fun openApp(context: Context, type: WidgetType): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            // 不能用 ACTION_MAIN + CATEGORY_LAUNCHER：系统会当成"回到原任务"，
            // App 已在后台时不触发 onNewIntent，extras 直接丢失，于是不跳转。
            action = ACTION_OPEN
            putExtra(MainActivity.EXTRA_ROUTE, routeFor(type))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            // 每个 widget 类型固定 requestCode，配合 UPDATE_CURRENT 刷新 extras
            type.ordinal + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** 票根在「发现」页的「美食日记」，纪念日也在「发现」页。 */
    private fun routeFor(type: WidgetType): String = "discover"
}

/**
 * 把 widget 绑定到某一张卡片：
 *
 * 用户有多张票根/纪念日时，桌面上的 widget 需要一个"这张要显示哪一条"的选择。
 * 目前策略是取列表第一条；后续可在 App 内长按卡片指定（见 WidgetStore.saveSelected）。
 */
object WidgetPinner {
    const val ACTION_PIN_RESULT = "com.xixikitchen.jetpack.WIDGET_PIN_RESULT"
    const val EXTRA_TYPE = "widget_type"
    const val EXTRA_ITEM_ID = "widget_item_id"

    fun requestPin(context: Context, type: WidgetType, itemId: Long): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(context, "系统版本过低，请在桌面长按空白处添加小部件", Toast.LENGTH_LONG).show()
            return false
        }
        val manager = AppWidgetManager.getInstance(context)
        if (!manager.isRequestPinAppWidgetSupported) {
            Toast.makeText(context, "当前桌面不支持一键添加，请在桌面长按空白处选择小部件", Toast.LENGTH_LONG).show()
            return false
        }
        val provider = ComponentName(context, providerClass(type))
        val callback = PendingIntent.getBroadcast(
            context,
            type.ordinal + 2000,
            Intent(context, WidgetPinResultReceiver::class.java).apply {
                action = ACTION_PIN_RESULT
                putExtra(EXTRA_TYPE, type.key)
                putExtra(EXTRA_ITEM_ID, itemId)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val success = manager.requestPinAppWidget(provider, null, callback)
        if (!success) {
            Toast.makeText(context, "添加失败，请稍后重试或从桌面小部件列表添加", Toast.LENGTH_SHORT).show()
        }
        return success
    }

    fun providerClass(type: WidgetType): Class<*> = when (type) {
        WidgetType.Anniversary -> AnniversaryWidgetProvider::class.java
        WidgetType.Diary -> DiaryWidgetProvider::class.java
    }
}

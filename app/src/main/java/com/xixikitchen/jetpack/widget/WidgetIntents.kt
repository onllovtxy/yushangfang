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
    fun openApp(context: Context, type: WidgetType): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
            putExtra("open_route", routeFor(type))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            type.ordinal + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun routeFor(type: WidgetType): String = "discover"
}

/**
 * 「添加到桌面」：优先用系统固定请求（Android 8.0+），旧版本提示去桌面长按添加。
 *
 * 成功时系统会通过 [WidgetPinResultReceiver] 回传新增的 appWidgetId，
 * 我们在那里把这张卡片绑定到该 widget 上并立即刷新。
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

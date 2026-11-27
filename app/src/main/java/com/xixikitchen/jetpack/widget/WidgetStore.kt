package com.xixikitchen.jetpack.widget

import android.content.Context
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 小组件的轻量缓存：widget 进程被系统随时回收，且不能依赖 Hilt / Compose，
 * 所以这里用普通 SharedPreferences 保存「上次拉取到的卡片列表」和每个 widget 选中的卡片 id。
 */
object WidgetStore {
    private const val PREFS = "xixi_widget_cache"
    private const val KEY_ITEMS_PREFIX = "items_"
    private const val KEY_SELECTED_PREFIX = "selected_"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun <T> saveItems(context: Context, type: WidgetType, items: List<T>) {
        prefs(context).edit {
            putString(KEY_ITEMS_PREFIX + type.key, Gson().toJson(items))
        }
    }

    fun <T> loadItems(context: Context, type: WidgetType, clazz: Class<T>): List<T> {
        val json = prefs(context).getString(KEY_ITEMS_PREFIX + type.key, null) ?: return emptyList()
        val listType = TypeToken.getParameterized(List::class.java, clazz).type
        return runCatching {
            Gson().fromJson<List<T>>(json, listType)
        }.getOrDefault(emptyList())
    }

    fun saveSelected(context: Context, type: WidgetType, appWidgetId: Int, itemId: Long) {
        prefs(context).edit {
            putLong(KEY_SELECTED_PREFIX + type.key + "_" + appWidgetId, itemId)
        }
    }

    fun selected(context: Context, type: WidgetType, appWidgetId: Int): Long =
        prefs(context).getLong(KEY_SELECTED_PREFIX + type.key + "_" + appWidgetId, -1L)

    fun clearSelected(context: Context, type: WidgetType, appWidgetId: Int) {
        prefs(context).edit {
            remove(KEY_SELECTED_PREFIX + type.key + "_" + appWidgetId)
        }
    }
}

enum class WidgetType(val key: String, val label: String) {
    Anniversary("anniversary", "纪念日"),
    Diary("diary", "美食票根")
}

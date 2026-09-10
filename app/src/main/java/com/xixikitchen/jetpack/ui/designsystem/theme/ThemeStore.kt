package com.xixikitchen.jetpack.ui.designsystem.theme

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 主题选择的落盘:当前选中的主题 id + 用户自建的主题列表。
 *
 * 用 SharedPreferences 同步读:第一帧就能拿到用户选的主题,
 * 不会先闪一下默认配色再切过去(异步存储必然会有这一下)。
 */
class ThemeStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("xixi_theme", Context.MODE_PRIVATE)
    private val gson = Gson()

    /** 返回主题 id。可能是内置主题的 id,也可能是某个自定义主题的 id。 */
    fun loadSelection(): String =
        prefs.getString(KEY_SELECTED, null) ?: AppTheme.DEFAULT.id

    fun saveSelection(themeId: String) {
        prefs.edit().putString(KEY_SELECTED, themeId).apply()
    }

    fun loadCustoms(): List<CustomTheme> = runCatching {
        val json = prefs.getString(KEY_CUSTOMS, null) ?: return emptyList()
        val type = object : TypeToken<List<CustomThemeDto>>() {}.type
        gson.fromJson<List<CustomThemeDto>>(json, type)?.map { it.toModel() } ?: emptyList()
    }.getOrDefault(emptyList())

    fun saveCustoms(themes: List<CustomTheme>) {
        val json = gson.toJson(themes.map { it.toDto() })
        prefs.edit().putString(KEY_CUSTOMS, json).apply()
    }

    private companion object {
        const val KEY_SELECTED = "selected_theme"
        const val KEY_CUSTOMS = "custom_themes"
    }
}

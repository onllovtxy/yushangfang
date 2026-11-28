package com.xixikitchen.jetpack.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.util.Log
import com.xixikitchen.jetpack.data.ApiClient
import com.xixikitchen.jetpack.data.BackendConfig
import com.xixikitchen.jetpack.data.FoodDiary
import com.xixikitchen.jetpack.data.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * widget 侧的数据拉取。
 *
 * widget 进程没有 Hilt 注入，所以直接 new SessionStore / ApiClient；
 * 两者都只依赖 Context，构造成本很低。
 */
object WidgetRefresh {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun refreshNow(
        context: Context,
        type: WidgetType,
        onComplete: (() -> Unit)? = null
    ) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                runCatching {
                    val sessionStore = SessionStore(appContext)
                    val session = sessionStore.sessionSnapshot()
                    if (!session.loggedIn) return@runCatching
                    // 沿用 App 里配置的服务器地址
                    BackendConfig.setCurrentBaseUrl(
                        runCatching { sessionStore.backendBaseUrlSnapshot() }
                            .getOrDefault(BackendConfig.DEFAULT_BASE_URL)
                    )
                    val api = ApiClient(sessionStore).service()
                    val token = "Bearer ${session.token}"
                    when (type) {
                        WidgetType.Anniversary -> {
                            val resp = api.anniversaries(token)
                            resp.data?.let { list ->
                                WidgetStore.saveItems(appContext, WidgetType.Anniversary, list)
                            }
                        }
                        WidgetType.Diary -> {
                            val resp = api.diaries(token, page = 1, pageSize = 30)
                            resp.data?.list?.let { list ->
                                WidgetStore.saveItems(appContext, WidgetType.Diary, list)
                                cacheActiveDiaryImages(appContext, list)
                            }
                        }
                    }
                    WidgetRefresher.refreshAll(appContext, type)
                }.onFailure { Log.w("WidgetRefresh", "refresh failed", it) }
            } finally {
                if (onComplete != null) {
                    withContext(Dispatchers.Main) { onComplete() }
                }
            }
        }
    }

    /** 选定某张票根后在后台补全封面图缓存并刷新桌面展示。 */
    fun ensureDiaryImageCached(context: Context, itemId: Long) {
        val appContext = context.applicationContext
        scope.launch {
            runCatching {
                val sessionStore = SessionStore(appContext)
                BackendConfig.setCurrentBaseUrl(
                    runCatching { sessionStore.backendBaseUrlSnapshot() }
                        .getOrDefault(BackendConfig.DEFAULT_BASE_URL)
                )
                val items = WidgetStore.loadItems(appContext, WidgetType.Diary, FoodDiary::class.java)
                val target = items.firstOrNull { it.id == itemId } ?: items.firstOrNull() ?: return@runCatching
                WidgetImageDownloader.cacheImages(appContext, WidgetType.Diary, listOf(target.imageUrl))
                WidgetRefresher.refreshAll(appContext, WidgetType.Diary)
            }.onFailure { Log.w("WidgetRefresh", "ensureDiaryImageCached failed", it) }
        }
    }

    private suspend fun cacheActiveDiaryImages(context: Context, list: List<FoodDiary>) {
        if (list.isEmpty()) return
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, DiaryWidgetProvider::class.java))
        val selectedIds = ids.map { WidgetStore.selected(context, WidgetType.Diary, it) }.toSet()
        val urls = buildList {
            list.filter { it.id in selectedIds }.forEach { add(it.imageUrl) }
            if (isEmpty()) add(list.first().imageUrl)
        }
        WidgetImageDownloader.cacheImages(context, WidgetType.Diary, urls)
    }
}

package com.xixikitchen.jetpack.widget

import android.content.Context
import android.util.Log
import com.xixikitchen.jetpack.data.Anniversary
import com.xixikitchen.jetpack.data.ApiClient
import com.xixikitchen.jetpack.data.BackendConfig
import com.xixikitchen.jetpack.data.FoodDiary
import com.xixikitchen.jetpack.data.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * widget 侧的数据拉取。
 *
 * widget 进程没有 Hilt 注入，所以直接 new SessionStore / ApiClient；
 * 两者都只依赖 Context，构造成本很低。
 */
object WidgetRefresh {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun refreshNow(context: Context, type: WidgetType) {
        val appContext = context.applicationContext
        scope.launch {
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
                        }
                    }
                }
                WidgetRefresher.refreshAll(appContext, type)
            }.onFailure { Log.w("WidgetRefresh", "refresh failed", it) }
        }
    }
}

package com.xixikitchen.jetpack

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.xixikitchen.jetpack.ui.KitchenViewModel
import com.xixikitchen.jetpack.ui.XixiKitchenApp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * widget 点击要打开的顶层路由。
     *
     * 用 StateFlow 而不是普通字段：App 已在后台时点 widget 走 [onNewIntent]，
     * 只改字段 Compose 收不到通知；用 Flow 才能让界面重新导航。
     * 每次处理完由界面层调用 [consumeWidgetRoute] 清空，避免重复跳转。
     */
    private val _widgetRoute = MutableStateFlow<String?>(null)
    val widgetRoute: StateFlow<String?> = _widgetRoute.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        // 显式开启 edge-to-edge。targetSdk 36 在 Android 15+ 上被系统强制开启,
        // 但 API 24~34 不会 —— 不显式调用的话同一个界面在新旧机器上的
        // 状态栏处理会不一致(而且 statusBarsPadding() 会重复内缩)。
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        _widgetRoute.value = intent?.getStringExtra(EXTRA_ROUTE)
        setContent {
            val vm: KitchenViewModel = hiltViewModel()
            XixiKitchenApp(vm, widgetRoute = widgetRoute, onWidgetRouteConsumed = ::consumeWidgetRoute)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // App 已在运行时的 widget 点击也会走到这里，同样要触发导航
        intent.getStringExtra(EXTRA_ROUTE)?.let { _widgetRoute.value = it }
    }

    fun consumeWidgetRoute() {
        _widgetRoute.value = null
    }

    companion object {
        const val EXTRA_ROUTE = "open_route"
    }
}

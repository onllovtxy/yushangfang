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
import java.util.concurrent.atomic.AtomicLong

data class WidgetNavigationTarget(
    val route: String,
    val subTab: String? = null,
    val requestId: Long = 0L
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * widget 点击要打开的目标路由与子标签。
     *
     * 用 StateFlow + 递增 requestId：App 已在后台或已停留在目标顶层页时点 widget，
     * 走 [onNewIntent] 仍会发出新事件，保证每次点击都能可靠切换到对应页面和子标签；
     * 每次处理完由界面层调用 [consumeWidgetTarget] 清空，避免重复跳转。
     */
    private val widgetRequestSeq = AtomicLong(0L)
    private val _widgetTarget = MutableStateFlow<WidgetNavigationTarget?>(null)
    val widgetTarget: StateFlow<WidgetNavigationTarget?> = _widgetTarget.asStateFlow()

    override fun onCreate(savedInstanceState: Bundle?) {
        // 显式开启 edge-to-edge。targetSdk 36 在 Android 15+ 上被系统强制开启,
        // 但 API 24~34 不会 —— 不显式调用的话同一个界面在新旧机器上的
        // 状态栏处理会不一致(而且 statusBarsPadding() 会重复内缩)。
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            handleWidgetIntent(intent)
        }
        setContent {
            val vm: KitchenViewModel = hiltViewModel()
            XixiKitchenApp(
                vm = vm,
                widgetTarget = widgetTarget,
                onWidgetTargetConsumed = ::consumeWidgetTarget
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // App 已在运行时的 widget 点击也会走到这里，同样要触发导航
        handleWidgetIntent(intent)
    }

    private fun handleWidgetIntent(intent: Intent?) {
        val extracted = extractWidgetTarget(intent) ?: return
        _widgetTarget.value = extracted.copy(requestId = widgetRequestSeq.incrementAndGet())
        // 消费掉 Intent 上的一次性跳转标记，避免旋转屏幕或从最近任务恢复时重复跳转
        intent?.removeExtra(EXTRA_ROUTE)
        intent?.removeExtra(EXTRA_SUB_TAB)
        if (intent?.data?.scheme == WIDGET_URI_SCHEME && intent.data?.host == WIDGET_URI_HOST) {
            intent.data = null
        }
    }

    fun consumeWidgetTarget(requestId: Long) {
        if (_widgetTarget.value?.requestId == requestId) {
            _widgetTarget.value = null
        }
    }

    companion object {
        const val EXTRA_ROUTE = "open_route"
        const val EXTRA_SUB_TAB = "open_sub_tab"
        const val WIDGET_URI_SCHEME = "xixikitchen"
        const val WIDGET_URI_HOST = "widget"

        fun extractWidgetTarget(intent: Intent?): WidgetNavigationTarget? {
            if (intent == null) return null
            val extraRoute = intent.getStringExtra(EXTRA_ROUTE)?.takeIf { it.isNotBlank() }
            val extraSubTab = intent.getStringExtra(EXTRA_SUB_TAB)?.takeIf { it.isNotBlank() }
            if (extraRoute != null) {
                return WidgetNavigationTarget(route = extraRoute, subTab = extraSubTab)
            }
            val uri = intent.data
            if (uri != null && uri.scheme == WIDGET_URI_SCHEME && uri.host == WIDGET_URI_HOST) {
                return when (uri.lastPathSegment) {
                    "anniversary" -> WidgetNavigationTarget(route = "discover", subTab = "anniversaries")
                    "diary" -> WidgetNavigationTarget(route = "discover", subTab = "diary")
                    else -> null
                }
            }
            return null
        }
    }
}

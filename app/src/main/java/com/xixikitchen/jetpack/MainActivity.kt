package com.xixikitchen.jetpack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.xixikitchen.jetpack.ui.KitchenViewModel
import com.xixikitchen.jetpack.ui.XixiKitchenApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /** widget 点击要打开的顶层路由（kitchen / orders / meetupDiary / discover / mine），null 表示普通启动。 */
    var widgetRoute: String? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        // 显式开启 edge-to-edge。targetSdk 36 在 Android 15+ 上被系统强制开启,
        // 但 API 24~34 不会 —— 不显式调用的话同一个界面在新旧机器上的
        // 状态栏处理会不一致(而且 statusBarsPadding() 会重复内缩)。
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // 从桌面 widget 点进来时携带目标页面，App 启动后由导航层消费
        widgetRoute = intent?.getStringExtra("open_route")
        setContent {
            val vm: KitchenViewModel = hiltViewModel()
            XixiKitchenApp(vm, initialRoute = widgetRoute)
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

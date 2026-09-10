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
    override fun onCreate(savedInstanceState: Bundle?) {
        // 显式开启 edge-to-edge。targetSdk 36 在 Android 15+ 上被系统强制开启,
        // 但 API 24~34 不会 —— 不显式调用的话同一个界面在新旧机器上的
        // 状态栏处理会不一致(而且 statusBarsPadding() 会重复内缩)。
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val vm: KitchenViewModel = hiltViewModel()
            XixiKitchenApp(vm)
        }
    }
}

package com.xixikitchen.jetpack

import android.app.Application
import cn.jiguang.api.JCoreManager
import cn.jiguang.api.utils.JCollectionAuth
import cn.jpush.android.api.JPushInterface
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class XixiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Enable JPush functionality (required to receive push notifications)
        JCollectionAuth.setAuth(this, true)
        
        // Disable background auto-wakeup/mutual start (heavily reduces background package scanning)
        JCollectionAuth.enableAutoWakeup(this, false)
        
        // Disable location tracking (LBS)
        JCoreManager.setLBSEnable(this, false)

        JPushInterface.setDebugMode(BuildConfig.DEBUG)
        JPushInterface.init(this)
    }
}

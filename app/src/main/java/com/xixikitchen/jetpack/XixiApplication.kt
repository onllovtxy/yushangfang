package com.xixikitchen.jetpack

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class XixiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

package com.xixikitchen.jetpack.push

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import cn.jpush.android.api.JPushInterface
import com.xixikitchen.jetpack.data.KitchenRepository
import com.xixikitchen.jetpack.data.PushTokenRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushTokenRegistrar @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: KitchenRepository
) {
    suspend fun registerCurrentDevice(authToken: String) {
        if (authToken.isBlank()) return

        runCatching {
            Log.i(TAG, "Starting JPush registrationId sync")
            val registrationId = waitForRegistrationId()
            if (registrationId.isBlank()) {
                Log.w(TAG, "JPush registrationId is still not ready after waiting")
                return
            }
            repo.registerPushToken(
                authToken,
                PushTokenRequest(
                    token = registrationId,
                    platform = "jpush_android",
                    appVersion = appVersion()
                )
            )
            Log.i(TAG, "JPush registrationId registered: ${registrationId.take(8)}")
        }.onFailure {
            Log.w(TAG, "Push token registration skipped: ${it.message}")
        }
    }

    suspend fun registerToken(authToken: String, registrationId: String) {
        if (authToken.isBlank() || registrationId.isBlank()) return

        runCatching {
            repo.registerPushToken(
                authToken,
                PushTokenRequest(
                    token = registrationId,
                    platform = "jpush_android",
                    appVersion = appVersion()
                )
            )
        }.onFailure {
            Log.w(TAG, "Push token refresh registration failed: ${it.message}")
        }
    }

    private fun appVersion(): String? {
        return runCatching {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        }.getOrNull()
    }

    private suspend fun waitForRegistrationId(): String {
        repeat(REGISTRATION_RETRY_COUNT) {
            val registrationId = JPushInterface.getRegistrationID(context).orEmpty()
            if (registrationId.isNotBlank()) return registrationId
            Log.i(TAG, "Waiting for JPush registrationId: attempt=${it + 1}")
            delay(REGISTRATION_RETRY_DELAY_MS)
        }
        return JPushInterface.getRegistrationID(context).orEmpty()
    }

    private companion object {
        const val TAG = "PushTokenRegistrar"
        const val REGISTRATION_RETRY_COUNT = 15
        const val REGISTRATION_RETRY_DELAY_MS = 2_000L
    }
}

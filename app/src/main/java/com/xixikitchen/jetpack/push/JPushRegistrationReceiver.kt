package com.xixikitchen.jetpack.push

import android.content.Context
import android.util.Log
import cn.jpush.android.service.JPushMessageReceiver
import com.xixikitchen.jetpack.data.KitchenRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class JPushRegistrationReceiver : JPushMessageReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ReceiverEntryPoint {
        fun kitchenRepository(): KitchenRepository
        fun pushTokenRegistrar(): PushTokenRegistrar
    }

    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onRegister(context: Context, registrationId: String) {
        Log.i("JPushReceiver", "onRegister: registrationId=$registrationId")
        if (registrationId.isBlank()) return

        // Resolve dependencies via Hilt EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ReceiverEntryPoint::class.java
        )
        val repo = entryPoint.kitchenRepository()
        val pushTokenRegistrar = entryPoint.pushTokenRegistrar()

        receiverScope.launch {
            val session = repo.session.first()
            pushTokenRegistrar.registerToken(session.token, registrationId)
        }
    }
}

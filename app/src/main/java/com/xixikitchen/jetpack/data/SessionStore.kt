package com.xixikitchen.jetpack.data

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "xixi_secure_session"
private const val KEY_TOKEN = "token"
private const val KEY_USER = "user"
private const val KEY_BACKEND_BASE_URL = "backend_base_url"

data class Session(val token: String = "", val user: User? = null) {
    val loggedIn: Boolean get() = token.isNotBlank() && user != null
}

class SessionStore(private val context: Context) {
    private val gson = Gson()
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val sessionState = MutableStateFlow(readSession())
    private val backendBaseUrlState = MutableStateFlow(readBackendBaseUrl())

    // 登录态被服务端判定失效时的一次性通知。用 SharedFlow 而不是 StateFlow：
    // 这是"发生了一次过期事件"，不是一种持续状态，重复收集不该重复触发。
    private val expiredEvents = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val session: Flow<Session> = sessionState.asStateFlow()
    val backendBaseUrl: Flow<String> = backendBaseUrlState.asStateFlow()
    val sessionExpired: SharedFlow<Unit> = expiredEvents.asSharedFlow()

    fun sessionSnapshot(): Session = sessionState.value

    suspend fun save(token: String, user: User) {
        prefs.edit(commit = true) {
            putString(KEY_TOKEN, token)
            putString(KEY_USER, gson.toJson(user))
        }
        sessionState.value = Session(token, user)
    }

    suspend fun updateUser(user: User) {
        prefs.edit(commit = true) {
            putString(KEY_USER, gson.toJson(user))
        }
        sessionState.value = sessionState.value.copy(user = user)
    }

    suspend fun clear() {
        prefs.edit(commit = true) {
            remove(KEY_TOKEN)
            remove(KEY_USER)
        }
        sessionState.value = Session()
    }

    /**
     * 服务端返回 401 时调用：清掉本地登录态并通知界面回到登录页。
     *
     * [requestToken] 是触发这次 401 的请求所用的 token。用户重新登录后，
     * 旧请求迟到的 401 不应该把新会话清掉，所以 token 对不上时直接忽略。
     *
     * 由 OkHttp 拦截器在后台线程调用，所以这里同步写盘；用 @Synchronized
     * 加上登录态判断，保证一批并发的 401 只清理一次、只通知一次。
     */
    @Synchronized
    fun expireSession(requestToken: String? = null) {
        val current = sessionState.value
        if (!current.loggedIn) return
        if (requestToken != null && requestToken != current.token) return
        prefs.edit(commit = true) {
            remove(KEY_TOKEN)
            remove(KEY_USER)
        }
        sessionState.value = Session()
        expiredEvents.tryEmit(Unit)
    }

    private fun readSession(): Session {
        val token = prefs.getString(KEY_TOKEN, "").orEmpty()
        val userJson = prefs.getString(KEY_USER, "").orEmpty()
        val user = runCatching { gson.fromJson(userJson, User::class.java) }.getOrNull()
        return Session(token, user)
    }

    private fun readBackendBaseUrl(): String {
        val saved = prefs.getString(KEY_BACKEND_BASE_URL, null)
        return sanitizeBackendBaseUrl(saved)
    }

    private fun sanitizeBackendBaseUrl(saved: String?): String {
        return if (saved == null || saved.contains("10.0.2.2") || saved.contains("localhost")) {
            BackendConfig.DEFAULT_BASE_URL
        } else {
            BackendConfig.normalizeBaseUrl(saved)
        }
    }
}

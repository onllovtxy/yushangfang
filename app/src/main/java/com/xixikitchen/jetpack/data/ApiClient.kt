package com.xixikitchen.jetpack.data

import com.google.gson.GsonBuilder
import com.xixikitchen.jetpack.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor(
    private val sessionStore: SessionStore
) {
    private val gson = GsonBuilder().create()
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.ENABLE_HTTP_LOGGING) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    // 任何需要登录的接口返回 401，都说明 token 已过期/失效。
    // 放在 OkHttp 层是为了覆盖全部接口（包括以后新增的），
    // 而不是在每个 ViewModel 的 catch 里各写一遍。
    private val sessionExpiryInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 401) {
            // 带上这次请求用的 token：如果用户已经重新登录，
            // 旧请求迟到的 401 不能把新会话也踢掉。
            val requestToken = request.header("Authorization")?.removePrefix("Bearer ")?.trim()
            // 存储层出问题也不能吞掉原始响应，否则界面只会看到一个无关的网络异常。
            runCatching { sessionStore.expireSession(requestToken) }
        }
        response
    }

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(sessionExpiryInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    @Volatile
    private var serviceBaseUrl: String = ""

    @Volatile
    private var service: ApiService? = null

    fun baseUrl(): String = BackendConfig.currentBaseUrl()

    fun service(): ApiService {
        val baseUrl = BackendConfig.currentBaseUrl()
        val cached = service
        if (cached != null && serviceBaseUrl == baseUrl) {
            return cached
        }

        synchronized(this) {
            val freshBaseUrl = BackendConfig.currentBaseUrl()
            val freshCached = service
            if (freshCached != null && serviceBaseUrl == freshBaseUrl) {
                return freshCached
            }
            val created = Retrofit.Builder()
                .baseUrl(freshBaseUrl)
                .client(okHttp)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(ApiService::class.java)
            serviceBaseUrl = freshBaseUrl
            service = created
            return created
        }
    }
}

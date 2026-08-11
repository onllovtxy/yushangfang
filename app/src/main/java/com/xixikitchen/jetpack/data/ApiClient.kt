package com.xixikitchen.jetpack.data

import com.google.gson.GsonBuilder
import com.xixikitchen.jetpack.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor() {
    private val gson = GsonBuilder().create()
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.ENABLE_HTTP_LOGGING) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    @Volatile
    private var serviceBaseUrl: String = ""

    @Volatile
    private var service: ApiService? = null

    fun setBaseUrl(value: String) {
        BackendConfig.setCurrentBaseUrl(value)
    }

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

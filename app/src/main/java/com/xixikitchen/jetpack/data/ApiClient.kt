package com.xixikitchen.jetpack.data

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiClient @Inject constructor() {
    private val gson = GsonBuilder().create()
    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
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

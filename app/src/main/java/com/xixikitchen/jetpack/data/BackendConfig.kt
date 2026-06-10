package com.xixikitchen.jetpack.data

object BackendConfig {
    const val DEFAULT_BASE_URL = "http://114.132.245.245/"

    @Volatile
    private var currentBaseUrl: String = DEFAULT_BASE_URL

    fun currentBaseUrl(): String = currentBaseUrl

    fun setCurrentBaseUrl(value: String) {
        currentBaseUrl = normalizeBaseUrl(value)
    }

    fun normalizeBaseUrl(value: String): String {
        var normalized = value.trim()
        if (normalized.isBlank()) {
            normalized = DEFAULT_BASE_URL
        }
        if (!normalized.startsWith("http://") && !normalized.startsWith("https://")) {
            normalized = "http://$normalized"
        }
        return normalized.trimEnd('/') + "/"
    }

    fun resolveUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        if (url.startsWith("http://") || url.startsWith("https://")) return url
        val base = currentBaseUrl.trimEnd('/')
        return base + if (url.startsWith("/")) url else "/$url"
    }
}

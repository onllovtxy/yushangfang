package com.xixikitchen.jetpack.widget

import android.content.Context
import com.xixikitchen.jetpack.ui.components.realImageUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 桌面 widget 不能联网，所以刷新或选卡时先把封面图下载到 filesDir 缓存，
 * 之后 widget 渲染直接读文件。
 */
object WidgetImageDownloader {

    suspend fun cacheImages(context: Context, type: WidgetType, urls: List<String?>) =
        withContext(Dispatchers.IO) {
            urls.mapNotNull { realImageUrl(it) }
                .filter { it.isNotBlank() }
                .distinct()
                .forEach { resolvedUrl ->
                    val target = WidgetImageCache.file(context, type, resolvedUrl) ?: return@forEach
                    if (target.exists() && target.length() > 0) return@forEach
                    runCatching { download(resolvedUrl, target) }
                }
        }

    private fun download(url: String, target: File) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 15_000
            instanceFollowRedirects = true
        }
        try {
            connection.inputStream.use { input ->
                FileOutputStream(target).use { output ->
                    input.copyTo(output, bufferSize = 32 * 1024)
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}

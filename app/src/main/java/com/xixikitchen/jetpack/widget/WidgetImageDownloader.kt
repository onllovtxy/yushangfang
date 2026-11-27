package com.xixikitchen.jetpack.widget

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * 桌面 widget 不能联网，所以「长按 → 添加到桌面」时先把封面图下载到 filesDir 缓存，
 * 之后 widget 刷新直接读文件。
 */
object WidgetImageDownloader {

    suspend fun cacheImages(context: Context, type: WidgetType, urls: List<String?>) =
        withContext(Dispatchers.IO) {
            urls.filterNotNull().filter { it.isNotBlank() }.distinct().forEach { url ->
                val target = WidgetImageCache.file(context, type, url) ?: return@forEach
                if (target.exists() && target.length() > 0) return@forEach
                runCatching { download(url, target) }
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

package com.xixikitchen.jetpack.ui

import com.google.gson.Gson
import com.xixikitchen.jetpack.data.ApiResponse
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

private val errorGson = Gson()

fun resolveErrorMessage(e: Throwable): String {
    if (e is HttpException && e.code() == 401) {
        // 登录态失效由 OkHttp 拦截器统一处理（清 session + 回登录页），
        // 这里不再把后端的原始提示弹出来。
        return "登录已过期，请重新登录"
    }
    if (e is HttpException) {
        try {
            val bodyString = e.response()?.errorBody()?.string()
            if (!bodyString.isNullOrBlank()) {
                try {
                    val apiResponse = errorGson.fromJson(bodyString, ApiResponse::class.java)
                    if (!apiResponse.msg.isNullOrBlank()) {
                        return apiResponse.msg
                    }
                } catch (_: Exception) {
                }

                if (bodyString.contains("<html>") || bodyString.contains("<title>")) {
                    val titleRegex = "<title>(.*?)</title>".toRegex(RegexOption.IGNORE_CASE)
                    val matchResult = titleRegex.find(bodyString)
                    if (matchResult != null) {
                        return "服务器错误 (${e.code()}): ${matchResult.groupValues[1]}"
                    }
                }
                return if (bodyString.contains("系统内部错误，请稍后重试") && bodyString.contains("\"code\":-1")) {
                    "接口异常：服务端返回 500，请检查服务端日志或接口实现。"
                } else {
                    "请求错误 (${e.code()}): ${bodyString.take(120)}"
                }
            }
        } catch (_: Exception) {
        }
        return "请求错误: ${e.code()}"
    }
    if (e is IllegalArgumentException) {
        return e.message ?: "输入或安全校验失败"
    }
    if (e is ConnectException || e is UnknownHostException) {
        return "网络连接失败，请检查后端地址或网络设置"
    }
    if (e is SocketTimeoutException) {
        return "请求超时，请稍后再试"
    }
    return e.message ?: "操作失败"
}

fun resolveLoginErrorMessage(e: Throwable): String {
    if (e is HttpException) {
        val code = e.code()
        val bodyString = runCatching { e.response()?.errorBody()?.string() }.getOrNull().orEmpty()
        if (bodyString.isNotBlank()) {
            val apiResponse = runCatching {
                errorGson.fromJson(bodyString, ApiResponse::class.java)
            }.getOrNull()
            if (!apiResponse?.msg.isNullOrBlank()) {
                return apiResponse!!.msg!!
            }
            return when (code) {
                502, 503 -> "服务器暂时不可用，请稍后再试"
                404 -> "接口不存在，请检查后端地址设置"
                else -> "登录失败 (HTTP $code)，请稍后重试"
            }
        }
        return "登录失败: ${e.code()}"
    }

    val msg = e.message.orEmpty()
    if (msg.contains("用户名或密码错误")) return msg
    if (msg.contains("Connection reset", ignoreCase = true)) {
        return "连接被服务器重置。请稍后重试，或联系我继续排查服务端。"
    }
    return resolveErrorMessage(e)
}

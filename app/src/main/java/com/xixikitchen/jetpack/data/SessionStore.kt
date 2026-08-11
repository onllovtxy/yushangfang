package com.xixikitchen.jetpack.data

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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

    val session: Flow<Session> = sessionState.asStateFlow()
    val backendBaseUrl: Flow<String> = backendBaseUrlState.asStateFlow()

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

    suspend fun saveBackendBaseUrl(baseUrl: String) {
        val normalized = BackendConfig.normalizeBaseUrl(baseUrl)
        prefs.edit(commit = true) {
            putString(KEY_BACKEND_BASE_URL, normalized)
        }
        backendBaseUrlState.value = sanitizeBackendBaseUrl(normalized)
    }

    suspend fun clear() {
        prefs.edit(commit = true) {
            remove(KEY_TOKEN)
            remove(KEY_USER)
        }
        sessionState.value = Session()
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

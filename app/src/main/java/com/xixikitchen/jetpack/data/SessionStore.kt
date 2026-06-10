package com.xixikitchen.jetpack.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.sessionDataStore by preferencesDataStore("xixi_session")

data class Session(val token: String = "", val user: User? = null) {
    val loggedIn: Boolean get() = token.isNotBlank() && user != null
}

class SessionStore(private val context: Context) {
    private val gson = Gson()

    val session: Flow<Session> = context.sessionDataStore.data.map { prefs ->
        val token = prefs[TOKEN] ?: ""
        val userJson = prefs[USER] ?: ""
        val user = runCatching { gson.fromJson(userJson, User::class.java) }.getOrNull()
        Session(token, user)
    }

    val backendBaseUrl: Flow<String> = context.sessionDataStore.data.map { prefs ->
        val saved = prefs[BACKEND_BASE_URL]
        if (saved == null || saved.contains("10.0.2.2") || saved.contains("localhost")) {
            BackendConfig.DEFAULT_BASE_URL
        } else {
            BackendConfig.normalizeBaseUrl(saved)
        }
    }

    suspend fun save(token: String, user: User) {
        context.sessionDataStore.edit { prefs ->
            prefs[TOKEN] = token
            prefs[USER] = gson.toJson(user)
        }
    }

    suspend fun updateUser(user: User) {
        context.sessionDataStore.edit { prefs ->
            prefs[USER] = gson.toJson(user)
        }
    }

    suspend fun saveBackendBaseUrl(baseUrl: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[BACKEND_BASE_URL] = BackendConfig.normalizeBaseUrl(baseUrl)
        }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(TOKEN)
            prefs.remove(USER)
        }
    }

    private companion object {
        val TOKEN = stringPreferencesKey("token")
        val USER = stringPreferencesKey("user")
        val BACKEND_BASE_URL = stringPreferencesKey("backend_base_url")
    }
}

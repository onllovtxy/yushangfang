package com.xixikitchen.jetpack.data

import javax.inject.Inject

class KitchenRepository @Inject constructor(
    private val apiClient: ApiClient,
    private val sessionStore: SessionStore
) {
    val session = sessionStore.session
    val backendBaseUrl = sessionStore.backendBaseUrl

    suspend fun login(username: String, password: String): Session {
        val payload = api().login(LoginRequest(username, password)).unwrap()
        val user = payload.userInfo ?: error("登录响应缺少用户信息")
        sessionStore.save(payload.token, user)
        return Session(payload.token, user)
    }

    suspend fun resetPassword(resetKey: String, username: String, newPassword: String) =
        api().resetPassword(PasswordResetRequest(resetKey, username, newPassword)).unwrap()

    suspend fun saveBackendBaseUrl(baseUrl: String): String {
        val normalized = BackendConfig.normalizeBaseUrl(baseUrl)
        sessionStore.saveBackendBaseUrl(normalized)
        apiClient.setBaseUrl(normalized)
        return normalized
    }

    suspend fun logout() {
        runCatching { api().logout(auth(sessionStore.sessionSnapshot())) }
        sessionStore.clear()
    }
    suspend fun updateCachedUser(user: User) = sessionStore.updateUser(user)

    suspend fun categories(token: String) = api().categories(auth(token)).unwrap()
    suspend fun addCategory(token: String, category: Category) = api().addCategory(auth(token), category).unwrap()
    suspend fun updateCategory(token: String, category: Category) = api().updateCategory(auth(token), category).unwrap()
    suspend fun deleteCategory(token: String, id: Long) = api().deleteCategory(auth(token), id).unwrap()

    suspend fun dishes(token: String, categoryId: Long? = null) = api().dishes(auth(token), categoryId).unwrap()
    suspend fun searchDishes(token: String, keyword: String) = api().searchDishes(auth(token), keyword).unwrap()
    suspend fun randomDishes(token: String) = api().randomDishes(auth(token)).unwrap()
    suspend fun addDish(token: String, dish: Dish) = api().addDish(auth(token), dish).unwrap()
    suspend fun updateDish(token: String, dish: Dish) = api().updateDish(auth(token), dish).unwrap()
    suspend fun deleteDish(token: String, id: Long) = api().deleteDish(auth(token), id).unwrap()

    suspend fun recipients(token: String) = api().recipients(auth(token)).unwrap()
    suspend fun updateProfile(token: String, request: ProfileUpdateRequest) = api().updateProfile(auth(token), request).unwrap()
    suspend fun uploadFile(token: String, file: okhttp3.MultipartBody.Part) = api().uploadFile(auth(token), file).unwrap()
    suspend fun registerPushToken(token: String, request: PushTokenRequest) =
        api().registerPushToken(auth(token), request).unwrap()

    suspend fun createOrder(token: String, request: OrderCreateRequest) = api().createOrder(auth(token), request).unwrap()
    suspend fun orders(token: String, status: Int? = null) = api().orders(auth(token), status).unwrap()
    suspend fun orderDetail(token: String, id: Long) = api().orderDetail(auth(token), id).unwrap()
    suspend fun acceptOrder(token: String, id: Long) = api().acceptOrder(auth(token), id).unwrap()
    suspend fun rejectOrder(token: String, id: Long) = api().rejectOrder(auth(token), id).unwrap()
    suspend fun completeOrder(token: String, id: Long) = api().completeOrder(auth(token), id).unwrap()
    suspend fun rateDish(token: String, orderId: Long, dishId: Long, rating: Int) =
        api().rateDish(auth(token), RateDishRequest(orderId, dishId, rating)).unwrap()
    suspend fun orderStats(token: String) = api().orderStats(auth(token)).unwrap()

    suspend fun announcements(token: String) = api().announcements(auth(token)).unwrap()
    suspend fun addAnnouncement(token: String, content: String) =
        api().addAnnouncement(auth(token), mapOf("content" to content)).unwrap()
    suspend fun updateAnnouncement(token: String, id: Long, content: String) =
        api().updateAnnouncement(auth(token), mapOf("id" to id, "content" to content)).unwrap()
    suspend fun deleteAnnouncement(token: String, id: Long) = api().deleteAnnouncement(auth(token), id).unwrap()

    suspend fun adminUsers(token: String) = api().adminUsers(auth(token)).unwrap()
    suspend fun addUser(token: String, user: UserEditorPayload) = api().addUser(auth(token), user).unwrap()
    suspend fun updateUser(token: String, id: Long, user: UserEditorPayload) =
        api().updateUser(auth(token), id, user).unwrap()
    suspend fun updateUserRole(token: String, id: Long, role: String) =
        api().updateUserRole(auth(token), id, RolePayload(role)).unwrap()
    suspend fun updateUserPartner(token: String, id: Long, partnerId: Long?) =
        api().updateUserPartner(auth(token), id, PartnerPayload(partnerId)).unwrap()
    suspend fun deleteUser(token: String, id: Long) = api().deleteUser(auth(token), id).unwrap()
    suspend fun adminOrders(token: String, status: Int? = null) = api().adminOrders(auth(token), status).unwrap()
    suspend fun deleteOrder(token: String, id: Long) = api().deleteOrder(auth(token), id).unwrap()

    private fun auth(token: String) = "Bearer $token"
    private fun api(): ApiService = apiClient.service()

    private fun <T> ApiResponse<T>.unwrap(): T {
        if (code == 0 && data != null) return data
        if (code == 0 && data == null) {
            @Suppress("UNCHECKED_CAST")
            return Unit as T
        }
        error(msg ?: "请求失败")
    }
}

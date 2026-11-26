package com.xixikitchen.jetpack.data

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class KitchenRepository @Inject constructor(
    private val apiClient: ApiClient,
    private val sessionStore: SessionStore
) {
    val session = sessionStore.session
    val backendBaseUrl = sessionStore.backendBaseUrl
    val selectedCoupleId = sessionStore.selectedCoupleId
    val sessionExpired = sessionStore.sessionExpired
    val spaceRefreshEvents = sessionStore.spaceRefreshEvents

    fun selectCoupleSpace(coupleId: Long?) = sessionStore.setSelectedCoupleId(coupleId)
    fun notifySpaceChanged() = sessionStore.notifySpaceChanged()

    suspend fun login(username: String, password: String): Session {
        val payload = api().login(LoginRequest(username, password)).unwrap()
        val user = payload.userInfo ?: error("登录响应缺少用户信息")
        sessionStore.save(payload.token, user)
        return Session(payload.token, user)
    }

    suspend fun register(username: String, password: String, inviteCode: String): Session {
        val payload = api().register(RegisterRequest(username, password, inviteCode)).unwrap()
        val user = payload.userInfo ?: error("注册响应缺少用户信息")
        sessionStore.save(payload.token, user)
        return Session(payload.token, user)
    }

    suspend fun resetPassword(resetKey: String, username: String, newPassword: String) =
        api().resetPassword(PasswordResetRequest(resetKey, username, newPassword)).unwrap()

    suspend fun logout() {
        // 先清本地再通知服务端：退出登录立刻生效，不会因为网络失败卡在已登录状态；
        // 也避免这次请求的 401 反过来触发一条"登录已过期"的提示。
        val token = sessionStore.sessionSnapshot().token
        sessionStore.clear()
        if (token.isNotBlank()) {
            runCatching { api().logout(auth(token)) }
        }
    }
    suspend fun updateCachedUser(user: User) = sessionStore.updateUser(user)

    suspend fun categories(token: String) = api().categories(auth(token)).unwrap()
    suspend fun addCategory(token: String, category: Category) = api().addCategory(auth(token), category).unwrap()
    suspend fun updateCategory(token: String, category: Category) = api().updateCategory(auth(token), category).unwrap()
    suspend fun deleteCategory(token: String, id: Long) = api().deleteCategory(auth(token), id).unwrap()

    suspend fun dishes(token: String, categoryId: Long? = null) = api().dishes(auth(token), categoryId).unwrap()
    suspend fun searchDishes(token: String, keyword: String) = api().searchDishes(auth(token), keyword).unwrap()
    suspend fun addDish(token: String, dish: Dish) = api().addDish(auth(token), dish).unwrap()
    suspend fun updateDish(token: String, dish: Dish) = api().updateDish(auth(token), dish).unwrap()
    suspend fun deleteDish(token: String, id: Long) = api().deleteDish(auth(token), id).unwrap()

    suspend fun recipients(token: String) = api().recipients(auth(token)).unwrap()
    suspend fun updateProfile(token: String, request: ProfileUpdateRequest) = api().updateProfile(auth(token), request).unwrap()
    suspend fun coupleSpace(token: String) = api().coupleSpace(auth(token)).unwrap()
    suspend fun coupleSpaces(token: String) = api().coupleSpaces(auth(token)).unwrap()
    suspend fun pairByCode(token: String, pairCode: String) = api().pairByCode(auth(token), PairRequest(pairCode)).unwrap()
    suspend fun uploadFile(token: String, file: okhttp3.MultipartBody.Part) = api().uploadFile(auth(token), file).unwrap()

    suspend fun createOrder(token: String, request: OrderCreateRequest) = api().createOrder(auth(token), request).unwrap()
    suspend fun orders(token: String, status: Int? = null) = api().orders(auth(token), status).unwrap()
    suspend fun orderDetail(token: String, id: Long) = api().orderDetail(auth(token), id).unwrap()
    suspend fun acceptOrder(token: String, id: Long) = api().acceptOrder(auth(token), id).unwrap()
    suspend fun rejectOrder(token: String, id: Long) = api().rejectOrder(auth(token), id).unwrap()
    suspend fun completeOrder(token: String, id: Long) = api().completeOrder(auth(token), id).unwrap()

    suspend fun announcements(token: String) = api().announcements(auth(token)).unwrap()
    suspend fun addAnnouncement(token: String, content: String, pinned: Boolean = false) =
        api().addAnnouncement(auth(token), mapOf("content" to content, "pinned" to pinned)).unwrap()
    suspend fun updateAnnouncement(token: String, id: Long, content: String, pinned: Boolean? = null): String {
        val body = buildMap<String, Any> {
            put("id", id)
            put("content", content)
            if (pinned != null) put("pinned", pinned)
        }
        return api().updateAnnouncement(auth(token), body).unwrap()
    }
    suspend fun pinAnnouncement(token: String, id: Long, pinned: Boolean) =
        api().pinAnnouncement(auth(token), id, PinPayload(pinned)).unwrap()
    suspend fun deleteAnnouncement(token: String, id: Long) = api().deleteAnnouncement(auth(token), id).unwrap()

    suspend fun notes(token: String) = api().notes(auth(token)).unwrap()
    suspend fun addNote(token: String, req: CoupleNoteSaveRequest) = api().addNote(auth(token), req).unwrap()
    suspend fun updateNote(token: String, id: Long, req: CoupleNoteSaveRequest) =
        api().updateNote(auth(token), id, req).unwrap()
    suspend fun pinNote(token: String, id: Long, pinned: Boolean) =
        api().pinNote(auth(token), id, PinPayload(pinned)).unwrap()
    suspend fun deleteNote(token: String, id: Long) = api().deleteNote(auth(token), id).unwrap()

    suspend fun anniversaries(token: String) = api().anniversaries(auth(token)).unwrap()
    suspend fun addAnniversary(token: String, req: AnniversarySaveRequest) =
        api().addAnniversary(auth(token), req).unwrap()
    suspend fun updateAnniversary(token: String, id: Long, req: AnniversarySaveRequest) =
        api().updateAnniversary(auth(token), id, req).unwrap()
    suspend fun pinAnniversary(token: String, id: Long, pinned: Boolean) =
        api().pinAnniversary(auth(token), id, PinPayload(pinned)).unwrap()
    suspend fun deleteAnniversary(token: String, id: Long) = api().deleteAnniversary(auth(token), id).unwrap()

    suspend fun diaries(token: String, page: Int = 1, pageSize: Int = 20) =
        api().diaries(auth(token), page, pageSize).unwrap()
    suspend fun createDiary(token: String, req: FoodDiarySaveRequest) =
        api().createDiary(auth(token), req).unwrap()
    suspend fun updateDiary(token: String, id: Long, req: FoodDiarySaveRequest) =
        api().updateDiary(auth(token), id, req).unwrap()
    suspend fun deleteDiary(token: String, id: Long) =
        api().deleteDiary(auth(token), id).unwrap()
    suspend fun addDiaryComment(token: String, diaryId: Long, req: FoodDiaryCommentCreateRequest) =
        api().addDiaryComment(auth(token), diaryId, req).unwrap()
    suspend fun deleteDiaryComment(token: String, commentId: Long) =
        api().deleteDiaryComment(auth(token), commentId).unwrap()

    suspend fun uploadImageBytes(token: String, bytes: ByteArray, fileName: String): String {
        val lowerName = fileName.lowercase()
        val allowedExtension = lowerName.endsWith(".jpg") ||
            lowerName.endsWith(".jpeg") ||
            lowerName.endsWith(".png") ||
            lowerName.endsWith(".gif") ||
            lowerName.endsWith(".webp")
        if (!allowedExtension) {
            throw IllegalArgumentException("仅支持 jpg、jpeg、png、gif、webp 图片")
        }
        if (bytes.isEmpty()) {
            throw IllegalArgumentException("上传文件不能为空")
        }
        val maxSizeBytes = 5 * 1024 * 1024
        if (bytes.size > maxSizeBytes) {
            throw IllegalArgumentException("图片不能超过 5MB")
        }

        val mimeType = when {
            fileName.endsWith(".png", ignoreCase = true) -> "image/png"
            fileName.endsWith(".gif", ignoreCase = true) -> "image/gif"
            fileName.endsWith(".webp", ignoreCase = true) -> "image/webp"
            else -> "image/jpeg"
        }
        val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", fileName, requestFile)
        return uploadFile(token, part).url
    }

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

    suspend fun inviteCodes(token: String) = api().inviteCodes(auth(token)).unwrap()
    suspend fun createInviteCode(token: String, req: InviteCodeCreateRequest) =
        api().createInviteCode(auth(token), req).unwrap()
    suspend fun toggleInviteCode(token: String, id: Long, enabled: Boolean) =
        api().toggleInviteCode(auth(token), id, EnabledPayload(enabled)).unwrap()
    suspend fun deleteInviteCode(token: String, id: Long) = api().deleteInviteCode(auth(token), id).unwrap()

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

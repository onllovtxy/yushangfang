package com.xixikitchen.jetpack.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.xixikitchen.jetpack.data.Announcement
import com.xixikitchen.jetpack.data.BackendConfig
import com.xixikitchen.jetpack.data.CartLine
import com.xixikitchen.jetpack.data.Category
import com.xixikitchen.jetpack.data.Dish
import com.xixikitchen.jetpack.data.KitchenRepository
import com.xixikitchen.jetpack.data.Order
import com.xixikitchen.jetpack.data.OrderCreateItem
import com.xixikitchen.jetpack.data.OrderCreateRequest
import com.xixikitchen.jetpack.data.OrderStats
import com.xixikitchen.jetpack.data.ProfileUpdateRequest
import com.xixikitchen.jetpack.data.Session
import com.xixikitchen.jetpack.data.User
import com.xixikitchen.jetpack.data.UserEditorPayload
import com.xixikitchen.jetpack.push.PushTokenRegistrar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

data class KitchenUiState(
    val session: Session = Session(),
    val loading: Boolean = false,
    val message: String? = null,
    val errorDialogMessage: String? = null,
    val categories: List<Category> = emptyList(),
    val activeCategoryId: Long? = null,
    val dishes: List<Dish> = emptyList(),
    val searchKeyword: String = "",
    val searchResults: List<Dish> = emptyList(),
    val cart: List<CartLine> = emptyList(),
    val recipients: List<User> = emptyList(),
    val orders: List<Order> = emptyList(),
    val orderStatus: Int? = null,
    val selectedOrder: Order? = null,
    val announcements: List<Announcement> = emptyList(),
    val stats: OrderStats = OrderStats(),
    val adminUsers: List<User> = emptyList(),
    val adminOrders: List<Order> = emptyList(),
    val backendBaseUrl: String = BackendConfig.DEFAULT_BASE_URL
) {
    val isLoggedIn: Boolean get() = session.loggedIn
    val isAdmin: Boolean get() = session.user?.role == "admin" || session.user?.role == "super_admin"
    val token: String get() = session.token
    val user: User? get() = session.user
    val cartCount: Int get() = cart.sumOf { it.quantity }
}

@HiltViewModel
class KitchenViewModel @Inject constructor(
    private val repo: KitchenRepository,
    private val pushTokenRegistrar: PushTokenRegistrar
) : ViewModel() {
    private val _state = MutableStateFlow(KitchenUiState())
    val state: StateFlow<KitchenUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repo.backendBaseUrl.collect { baseUrl ->
                BackendConfig.setCurrentBaseUrl(baseUrl)
                _state.update { it.copy(backendBaseUrl = baseUrl) }
            }
        }

        viewModelScope.launch {
            repo.session.collect { session ->
                _state.update { it.copy(session = session) }
                if (session.loggedIn && _state.value.categories.isEmpty()) {
                    registerPushToken(session.token)
                    refreshHome()
                    refreshOrders()
                    refreshAnnouncements()
                    refreshStats()
                }
            }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
    fun consumeErrorDialog() = _state.update { it.copy(errorDialogMessage = null) }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null, errorDialogMessage = null) }

            val trimmedUsername = username.trim()
            val trimmedPassword = password
            if (trimmedUsername.isBlank() || trimmedPassword.isBlank()) {
                notifyError("请输入账号和密码")
                _state.update { it.copy(loading = false) }
                return@launch
            }

            runCatching {
                repo.login(trimmedUsername, trimmedPassword)
            }.onSuccess {
                notify("登录成功")
            }.onFailure { e ->
                notifyError(resolveLoginErrorMessage(e))
            }

            _state.update { it.copy(loading = false) }
        }
    }

    fun resetPassword(resetKey: String, username: String, newPassword: String) = runAction {
        repo.resetPassword(resetKey.trim(), username.trim(), newPassword)
        notify("密码重置成功")
    }

    fun saveBackendBaseUrl(baseUrl: String) = runAction {
        val normalized = repo.saveBackendBaseUrl(baseUrl)
        BackendConfig.setCurrentBaseUrl(normalized)
        _state.update {
            it.copy(
                backendBaseUrl = normalized,
                categories = emptyList(),
                dishes = emptyList(),
                searchResults = emptyList(),
                recipients = emptyList(),
                orders = emptyList(),
                selectedOrder = null,
                announcements = emptyList(),
                adminUsers = emptyList(),
                adminOrders = emptyList()
            )
        }
        notify("后端地址已保存")
    }

    fun logout() = runAction {
        repo.logout()
        _state.update { KitchenUiState(backendBaseUrl = it.backendBaseUrl) }
    }

    fun refreshHome() = runAction {
        val categories = repo.categories(token())
        val active = _state.value.activeCategoryId ?: categories.firstOrNull()?.id
        val dishes = active?.let { repo.dishes(token(), it) } ?: emptyList()
        val recipients = repo.recipients(token())
        _state.update { it.copy(categories = categories, activeCategoryId = active, dishes = dishes, recipients = recipients) }
    }

    fun selectCategory(id: Long) {
        _state.update { it.copy(activeCategoryId = id, searchKeyword = "", searchResults = emptyList()) }
        viewModelScope.launch {
            runCatching {
                val dishes = repo.dishes(token(), id)
                _state.update { it.copy(dishes = dishes) }
            }.onFailure { e ->
                notify(resolveErrorMessage(e))
            }
        }
    }

    fun search(keyword: String) = runAction {
        val trimmed = keyword.trim()
        _state.update { it.copy(searchKeyword = keyword, searchResults = if (trimmed.isEmpty()) emptyList() else repo.searchDishes(token(), trimmed)) }
    }

    fun addRandomDish() = runAction {
        val dish = repo.randomDishes(token()).firstOrNull()
        if (dish != null) {
            addToCart(dish)
            notify("已随机加入一道菜")
        } else {
            notify("暂无菜品")
        }
    }

    fun addToCart(dish: Dish) {
        _state.update { state ->
            val existing = state.cart.firstOrNull { it.dish.id == dish.id }
            val cart = if (existing == null) {
                state.cart + CartLine(dish, 1)
            } else {
                state.cart.map { if (it.dish.id == dish.id) it.copy(quantity = it.quantity + 1) else it }
            }
            state.copy(cart = cart)
        }
    }

    fun changeCartQuantity(dishId: Long, delta: Int) {
        _state.update { state ->
            state.copy(cart = state.cart.mapNotNull {
                if (it.dish.id == dishId) {
                    val next = it.quantity + delta
                    if (next <= 0) null else it.copy(quantity = next)
                } else it
            })
        }
    }

    fun clearCart() = _state.update { it.copy(cart = emptyList()) }

    fun submitOrder(remark: String, toUserId: Long?) = runAction {
        val cart = _state.value.cart
        if (cart.isEmpty()) {
            notify("请先选择菜品")
            return@runAction
        }
        repo.createOrder(
            token(),
            OrderCreateRequest(
                items = cart.map { OrderCreateItem(it.dish.id, it.quantity) },
                remark = remark,
                toUserId = toUserId
            )
        )
        _state.update { it.copy(cart = emptyList()) }
        refreshOrders()
        notify("下单成功")
    }

    fun refreshOrders(status: Int? = _state.value.orderStatus) = runAction {
        val page = repo.orders(token(), status)
        _state.update { it.copy(orders = page.list, orderStatus = status) }
    }

    fun loadOrderDetail(id: Long) = runAction {
        _state.update { it.copy(selectedOrder = repo.orderDetail(token(), id)) }
    }

    fun orderAction(id: Long, action: String) = runAction {
        when (action) {
            "accept" -> repo.acceptOrder(token(), id)
            "reject" -> repo.rejectOrder(token(), id)
            "complete" -> repo.completeOrder(token(), id)
        }
        loadOrderDetail(id)
        refreshOrders()
        notify("操作成功")
    }

    fun rateDish(orderId: Long, dishId: Long, rating: Int) = runAction {
        repo.rateDish(token(), orderId, dishId, rating)
        notify("评分成功")
    }

    fun refreshAnnouncements() = runAction {
        _state.update { it.copy(announcements = repo.announcements(token())) }
    }

    fun refreshStats() = runAction {
        _state.update { it.copy(stats = repo.orderStats(token())) }
    }

    fun updateProfile(nickname: String?, avatarUrl: String?) = runAction {
        repo.updateProfile(token(), ProfileUpdateRequest(nickname, avatarUrl))
        val current = _state.value.user
        if (current != null) {
            val updated = current.copy(
                nickname = nickname?.takeIf { it.isNotBlank() } ?: current.nickname,
                avatarUrl = avatarUrl ?: current.avatarUrl
            )
            repo.updateCachedUser(updated)
            _state.update { it.copy(session = it.session.copy(user = updated)) }
        }
        notify("保存成功")
    }

    fun uploadFile(
        bytes: ByteArray,
        fileName: String,
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit
    ) = runAction {
        try {
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
            val part = okhttp3.MultipartBody.Part.createFormData("file", fileName, requestFile)
            val response = repo.uploadFile(token(), part)
            onSuccess(response.url)
        } catch (e: Exception) {
            onFailure()
            throw e
        }
    }

    fun loadAdminData() = runAction {
        val users = repo.adminUsers(token())
        val orders = repo.adminOrders(token()).list
        _state.update { it.copy(adminUsers = users, adminOrders = orders) }
    }

    fun saveCategory(category: Category) = runAction {
        if (category.id == 0L) repo.addCategory(token(), category) else repo.updateCategory(token(), category)
        refreshHome()
        notify("分类已保存")
    }

    fun deleteCategory(id: Long) = runAction {
        repo.deleteCategory(token(), id)
        refreshHome()
        notify("分类已删除")
    }

    fun saveDish(dish: Dish) = runAction {
        if (dish.id == 0L) repo.addDish(token(), dish) else repo.updateDish(token(), dish)
        refreshHome()
        notify("菜品已保存")
    }

    fun deleteDish(id: Long) = runAction {
        repo.deleteDish(token(), id)
        refreshHome()
        notify("菜品已删除")
    }

    fun saveAnnouncement(id: Long?, content: String) = runAction {
        if (id == null || id == 0L) repo.addAnnouncement(token(), content) else repo.updateAnnouncement(token(), id, content)
        refreshAnnouncements()
        notify("公告已保存")
    }

    fun deleteAnnouncement(id: Long) = runAction {
        repo.deleteAnnouncement(token(), id)
        refreshAnnouncements()
        notify("公告已删除")
    }

    fun saveUser(id: Long?, payload: UserEditorPayload) = runAction {
        if (id == null || id == 0L) repo.addUser(token(), payload) else repo.updateUser(token(), id, payload)
        loadAdminData()
        notify("用户已保存")
    }

    fun toggleRole(user: User) = runAction {
        repo.updateUserRole(token(), user.id, if (user.role == "admin") "user" else "admin")
        loadAdminData()
        notify("角色已更新")
    }

    fun updatePartner(userId: Long, partnerId: Long?) = runAction {
        repo.updateUserPartner(token(), userId, partnerId)
        loadAdminData()
        notify("接单人已更新")
    }

    fun deleteUser(id: Long) = runAction {
        repo.deleteUser(token(), id)
        loadAdminData()
        notify("用户已删除")
    }

    fun deleteAdminOrder(id: Long) = runAction {
        repo.deleteOrder(token(), id)
        loadAdminData()
        notify("订单已删除")
    }

    private fun runAction(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, message = null, errorDialogMessage = null) }
            runCatching { block() }
                .onFailure { e -> notifyError(resolveErrorMessage(e)) }
            _state.update { it.copy(loading = false) }
        }
    }

    private fun resolveErrorMessage(e: Throwable): String {
        if (e is retrofit2.HttpException) {
            try {
                val bodyString = e.response()?.errorBody()?.string()
                if (!bodyString.isNullOrBlank()) {
                    try {
                        val apiResponse = com.google.gson.Gson().fromJson(bodyString, com.xixikitchen.jetpack.data.ApiResponse::class.java)
                        if (!apiResponse.msg.isNullOrBlank()) {
                            return apiResponse.msg
                        }
                    } catch (ignored: Exception) {}
                    
                    if (bodyString.contains("<html>") || bodyString.contains("<title>")) {
                        val titleRegex = "<title>(.*?)</title>".toRegex(RegexOption.IGNORE_CASE)
                        val matchResult = titleRegex.find(bodyString)
                        if (matchResult != null) {
                            return "服务器错误 (${e.code()}): ${matchResult.groupValues[1]}"
                        }
                    }
                    return if (bodyString.contains("系统内部错误，请稍后重试") && bodyString.contains("\"code\":-1")) {
                        "登录接口异常：服务端 /api/user/password-login 返回 500。请检查后端登录实现或接口路径是否正确。"
                    } else {
                        "请求错误 (${e.code()}): ${bodyString.take(120)}"
                    }
                }
            } catch (ignored: Exception) {
                // ignore
            }
            return "请求错误: ${e.code()}"
        }
        if (e is IllegalArgumentException) {
            return e.message ?: "输入或安全校验失败"
        }
        if (e is java.net.ConnectException || e is java.net.UnknownHostException) {
            return "网络连接失败，请检查后端地址或网络设置"
        }
        if (e is java.net.SocketTimeoutException) {
            return "请求超时，请稍后再试"
        }
        return e.message ?: "操作失败"
    }

    private fun resolveLoginErrorMessage(e: Throwable): String {
        if (e is retrofit2.HttpException) {
            val code = e.code()
            val bodyString = runCatching { e.response()?.errorBody()?.string() }.getOrNull().orEmpty()
            if (bodyString.isNotBlank()) {
                // 后端标准 JSON 响应：直接展示业务 msg
                val apiResponse = runCatching {
                    com.google.gson.Gson().fromJson(bodyString, com.xixikitchen.jetpack.data.ApiResponse::class.java)
                }.getOrNull()
                if (!apiResponse?.msg.isNullOrBlank()) {
                    return apiResponse!!.msg!!
                }
                // 非 JSON（网关 HTML 错误页等）：给出友好提示，不显示原始 HTML
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

    private fun token(): String = _state.value.token

    private fun notify(message: String) {
        _state.update { it.copy(message = message) }
    }

    private fun notifyError(message: String) {
        _state.update { it.copy(message = message, errorDialogMessage = message) }
    }

    private fun registerPushToken(authToken: String) {
        viewModelScope.launch {
            pushTokenRegistrar.registerCurrentDevice(authToken)
        }
    }
}

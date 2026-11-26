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
import com.xixikitchen.jetpack.data.ProfileUpdateRequest
import com.xixikitchen.jetpack.data.Session
import com.xixikitchen.jetpack.data.User
import com.xixikitchen.jetpack.data.UserEditorPayload
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class KitchenUiState(
    val session: Session = Session(),
    val activeRequests: Int = 0,
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
    val adminUsers: List<User> = emptyList(),
    val adminOrders: List<Order> = emptyList(),
    val backendBaseUrl: String = BackendConfig.DEFAULT_BASE_URL
) {
    val loading: Boolean get() = activeRequests > 0
    val isLoggedIn: Boolean get() = session.loggedIn
    val isAdmin: Boolean get() = session.user?.role == "admin" || session.user?.role == "super_admin"
    val token: String get() = session.token
    val user: User? get() = session.user
    val cartCount: Int get() = cart.sumOf { it.quantity }
}

@HiltViewModel
class KitchenViewModel @Inject constructor(
    private val repo: KitchenRepository
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
                // 这里只同步登录态，不主动拉数据：首页/订单/公告各自在进入时
                // 用 LaunchedEffect 触发刷新。两边都拉会让冷启动把同一批请求发两遍。
                _state.update { it.copy(session = session) }
            }
        }

        // token 过期时后端返回 401，拦截器已经清掉本地登录态；
        // 这里要把界面残留的数据一并清空并提示用户重新登录，
        // 否则会话虽然没了，页面还停在旧内容上动不了。
        viewModelScope.launch {
            repo.sessionExpired.collect {
                _state.update {
                    KitchenUiState(
                        backendBaseUrl = it.backendBaseUrl,
                        errorDialogMessage = "登录已过期，请重新登录"
                    )
                }
            }
        }
    }

    fun consumeMessage() = _state.update { it.copy(message = null) }
    fun consumeErrorDialog() = _state.update { it.copy(errorDialogMessage = null) }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(activeRequests = it.activeRequests + 1, message = null, errorDialogMessage = null) }
            try {
                val trimmedUsername = username.trim()
                val trimmedPassword = password
                if (trimmedUsername.isBlank() || trimmedPassword.isBlank()) {
                    notifyError("请输入账号和密码")
                    return@launch
                }

                runCatching {
                    repo.login(trimmedUsername, trimmedPassword)
                }.onSuccess {
                    // 登录成功后不弹任何提示（页面切换本身就是反馈），
                    // 并清掉可能残留的错误消息，进入主页保持干净
                    _state.update { it.copy(message = null, errorDialogMessage = null) }
                }.onFailure { e ->
                    notifyError(resolveLoginErrorMessage(e))
                }
            } finally {
                _state.update { it.copy(activeRequests = (it.activeRequests - 1).coerceAtLeast(0)) }
            }
        }
    }

    fun register(username: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _state.update { it.copy(activeRequests = it.activeRequests + 1, message = null, errorDialogMessage = null) }
            try {
                val trimmedUsername = username.trim()
                val trimmedPassword = password
                if (trimmedUsername.isBlank() || trimmedPassword.isBlank()) {
                    notifyError("请输入账号和密码")
                    return@launch
                }

                runCatching {
                    val session = repo.register(trimmedUsername, trimmedPassword)
                    // 注册成功即已登录（后端返回 token），清理状态后进入主页
                    _state.update {
                        it.copy(
                            session = session,
                            message = null,
                            errorDialogMessage = null,
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
                    onSuccess()
                }.onFailure { e ->
                    notifyError(resolveErrorMessage(e))
                }
            } finally {
                _state.update { it.copy(activeRequests = (it.activeRequests - 1).coerceAtLeast(0)) }
            }
        }
    }

    fun resetPassword(resetKey: String, username: String, newPassword: String) = runAction {
        repo.resetPassword(resetKey.trim(), username.trim(), newPassword)
        notify("密码重置成功")
    }

    fun logout() = runAction {
        repo.logout()
        _state.update { KitchenUiState(backendBaseUrl = it.backendBaseUrl) }
    }

    fun refreshHome() = runAction {
        coroutineScope {
            val cachedActive = _state.value.activeCategoryId
            val categoriesDeferred = async { repo.categories(token()) }
            val recipientsDeferred = async { repo.recipients(token()) }
            val cachedDishesDeferred = cachedActive?.let { id ->
                async { repo.dishes(token(), id) }
            }

            val categories = categoriesDeferred.await()
            val active = cachedActive?.takeIf { cached -> categories.any { it.id == cached } }
                ?: categories.firstOrNull()?.id
            val dishes = if (cachedActive != null && active == cachedActive) {
                cachedDishesDeferred?.await() ?: emptyList()
            } else {
                active?.let { repo.dishes(token(), it) } ?: emptyList()
            }
            val recipients = recipientsDeferred.await()

            _state.update {
                it.copy(categories = categories, activeCategoryId = active, dishes = dishes, recipients = recipients)
            }
        }
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
        _state.update {
            it.copy(
                searchKeyword = trimmed,
                searchResults = if (trimmed.isEmpty()) emptyList() else repo.searchDishes(token(), trimmed)
            )
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

    fun refreshAnnouncements() = runAction {
        _state.update { it.copy(announcements = repo.announcements(token())) }
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
            val url = repo.uploadImageBytes(token(), bytes, fileName)
            onSuccess(url)
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
            _state.update { it.copy(activeRequests = it.activeRequests + 1, message = null, errorDialogMessage = null) }
            try {
                runCatching { block() }
                    .onFailure { e -> notifyError(resolveErrorMessage(e)) }
            } finally {
                _state.update { it.copy(activeRequests = (it.activeRequests - 1).coerceAtLeast(0)) }
            }
        }
    }

    private fun token(): String = _state.value.token

    private fun notify(message: String) {
        _state.update { it.copy(message = message) }
    }

    private fun notifyError(message: String) {
        _state.update { it.copy(errorDialogMessage = message) }
    }
}

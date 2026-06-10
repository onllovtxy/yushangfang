package com.xixikitchen.jetpack.data

data class ApiResponse<T>(
    val code: Int = -1,
    val msg: String? = null,
    val data: T? = null
)

data class LoginRequest(val username: String, val password: String)

data class PasswordResetRequest(
    val resetKey: String,
    val username: String,
    val newPassword: String
)

data class LoginPayload(
    val token: String = "",
    val userInfo: User? = null
)

data class User(
    val id: Long = 0,
    val username: String? = null,
    val openid: String? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val role: String = "user",
    val partnerId: Long? = null
)

data class Category(
    val id: Long = 0,
    val name: String = "",
    val icon: String? = null,
    val sortOrder: Int? = null
)

data class Dish(
    val id: Long = 0,
    val categoryId: Long = 0,
    val name: String = "",
    val imageUrl: String? = null,
    val rating: Double? = null,
    val monthlySales: Int? = null,
    val sortOrder: Int? = null
)

data class Announcement(
    val id: Long = 0,
    val content: String = "",
    val updatedAt: String? = null
)

data class OrderCreateRequest(
    val items: List<OrderCreateItem>,
    val remark: String? = null,
    val toUserId: Long? = null
)

data class OrderCreateItem(val dishId: Long, val quantity: Int)

data class PushTokenRequest(
    val token: String,
    val platform: String = "android",
    val appVersion: String? = null
)

data class OrderPage(
    val total: Long = 0,
    val list: List<Order> = emptyList()
)

data class Order(
    val id: Long = 0,
    val orderNo: String = "",
    val status: Int = 0,
    val remark: String? = null,
    val totalCount: Int = 0,
    val fromUserId: Long = 0,
    val toUserId: Long = 0,
    val createdAt: String? = null,
    val items: List<OrderItem> = emptyList()
)

data class OrderItem(
    val dishId: Long = 0,
    val dishName: String? = null,
    val dishImage: String? = null,
    val quantity: Int = 1
)

data class OrderStats(
    val total: Int = 0,
    val pending: Int = 0,
    val completed: Int = 0,
    val monthOrders: Int = 0,
    val monthReceived: Int = 0,
    val totalOrders: Int = 0
)

data class RateDishRequest(val orderId: Long, val dishId: Long, val rating: Int)

data class ProfileUpdateRequest(val nickname: String? = null, val avatarUrl: String? = null)

data class UserEditorPayload(
    val nickname: String,
    val avatarUrl: String? = null,
    val password: String? = null,
    val role: String = "user",
    val partnerId: Long? = null
)

data class RolePayload(val role: String)
data class PartnerPayload(val partnerId: Long?)

data class CartLine(val dish: Dish, val quantity: Int)

data class UploadResponse(val url: String)

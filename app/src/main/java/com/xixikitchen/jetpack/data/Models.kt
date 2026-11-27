package com.xixikitchen.jetpack.data

data class ApiResponse<T>(
    val code: Int = -1,
    val msg: String? = null,
    val data: T? = null
)

data class LoginRequest(val username: String, val password: String)

data class RegisterRequest(
    val username: String,
    val password: String,
    val inviteCode: String
)

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
    val partnerId: Long? = null,
    val coupleId: Long? = null
)

data class CoupleSpace(
    val id: Long = 0,
    val name: String = "",
    val pairCode: String = "",
    val paired: Boolean = false,
    val partner: OrderUserInfo? = null,
    val members: List<OrderUserInfo> = emptyList()
)

data class PairRequest(val pairCode: String)

data class InviteCodeItem(
    val id: Long = 0,
    val code: String = "",
    val note: String = "",
    val maxUses: Int = 0,
    val usedCount: Int = 0,
    val enabled: Boolean = true,
    val createdAt: String? = null
)

data class InviteCodeCreateRequest(
    val code: String? = null,
    val note: String? = null,
    val maxUses: Int = 0
)

data class EnabledPayload(val enabled: Boolean)

data class Category(
    val id: Long = 0,
    val name: String = "",
    val sortOrder: Int? = null
)

data class Dish(
    val id: Long = 0,
    val categoryId: Long = 0,
    val name: String = "",
    val imageUrl: String? = null,
    val monthlySales: Int? = null,
    val sortOrder: Int? = null
)

data class Announcement(
    val id: Long = 0,
    val content: String = "",
    val pinned: Boolean = false,
    val pinnedAt: String? = null,
    val createdBy: Long? = null,
    val updatedBy: Long? = null,
    val authorName: String? = null,
    val authorAvatar: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CoupleNote(
    val id: Long = 0,
    val content: String = "",
    val pinned: Boolean = false,
    val pinnedAt: String? = null,
    val createdBy: Long? = null,
    val authorName: String? = null,
    val authorAvatar: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CoupleNoteSaveRequest(
    val content: String,
    val pinned: Boolean
)

data class PinPayload(val pinned: Boolean)

data class Anniversary(
    val id: Long = 0,
    val title: String = "",
    val eventDate: String = "",
    val type: String = "countdown",
    val repeatYearly: Boolean = true,
    val pinned: Boolean = false,
    val note: String = "",
    val createdBy: Long? = null,
    val creatorName: String? = null,
    val daysCount: Long = 0,
    val isToday: Boolean = false,
    val isPast: Boolean = false,
    val nextAnniversaryDays: Long? = null
)

data class AnniversarySaveRequest(
    val title: String,
    val eventDate: String,
    val type: String,
    val repeatYearly: Boolean,
    val pinned: Boolean,
    val note: String
)

data class CoupleMeetup(
    val id: Long = 0,
    val arriveDate: String = "",
    val leaveDate: String = "",
    val city: String = "",
    val note: String = "",
    val photoUrl1: String = "",
    val photoUrl2: String = "",
    val meetIndex: Int = 1,
    val farewellIndex: Int? = null,
    val stayDays: Long = 1,
    val gapDaysFromPrev: Long? = null,
    val status: String = "past",
    val daysUntilMeet: Long? = null,
    val createdBy: Long? = null,
    val authorName: String? = null,
    val authorAvatar: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class CoupleMeetupSaveRequest(
    val arriveDate: String,
    val leaveDate: String,
    val city: String,
    val note: String,
    val photoUrl1: String,
    val photoUrl2: String
)


data class FoodDiaryComment(
    val id: Long = 0,
    val diaryId: Long = 0,
    val userId: Long = 0,
    val authorName: String? = null,
    val authorAvatar: String? = null,
    val replyToUserId: Long? = null,
    val replyToName: String? = null,
    val content: String = "",
    val createdAt: String? = null
)

data class FoodDiary(
    val id: Long = 0,
    val userId: Long = 0,
    val authorName: String? = null,
    val authorAvatar: String? = null,
    val imageUrl: String = "",
    val templateCode: String = "boarding_pass",
    val aspectRatio: String = "4:3",
    val title: String = "今日份美味",
    val content: String = "",
    val location: String = "熙熙的小厨房",
    val moodTag: String = "超满足",
    val ticketNo: String = "",
    val comments: List<FoodDiaryComment> = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class FoodDiaryPage(
    val total: Long = 0,
    val list: List<FoodDiary> = emptyList()
)

data class FoodDiarySaveRequest(
    val imageUrl: String,
    val templateCode: String,
    val aspectRatio: String,
    val title: String,
    val content: String,
    val location: String,
    val moodTag: String
)

data class FoodDiaryCommentCreateRequest(
    val content: String,
    val replyToUserId: Long? = null
)

data class OrderCreateRequest(
    val items: List<OrderCreateItem>,
    val remark: String? = null,
    val toUserId: Long? = null
)

data class OrderCreateItem(val dishId: Long, val quantity: Int)

data class OrderPage(
    val total: Long = 0,
    val list: List<Order> = emptyList()
)

data class OrderUserInfo(
    val id: Long = 0,
    val nickname: String? = null,
    val avatarUrl: String? = null
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
    val acceptedAt: String? = null,
    val completedAt: String? = null,
    val fromUser: OrderUserInfo? = null,
    val toUser: OrderUserInfo? = null,
    val items: List<OrderItem> = emptyList()
)

data class OrderItem(
    val dishId: Long = 0,
    val dishName: String? = null,
    val dishImage: String? = null,
    val quantity: Int = 1
)

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

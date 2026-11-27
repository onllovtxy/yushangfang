package com.xixikitchen.jetpack.data

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("/api/user/password-login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginPayload>

    @POST("/api/user/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<LoginPayload>

    @POST("/api/user/logout")
    suspend fun logout(@Header("Authorization") token: String): ApiResponse<String>

    @POST("/api/admin/reset-password")
    suspend fun resetPassword(@Body request: PasswordResetRequest): ApiResponse<String>

    @GET("/api/category/list")
    suspend fun categories(@Header("Authorization") token: String): ApiResponse<List<Category>>

    @POST("/api/category/add")
    suspend fun addCategory(@Header("Authorization") token: String, @Body category: Category): ApiResponse<String>

    @PUT("/api/category/update")
    suspend fun updateCategory(@Header("Authorization") token: String, @Body category: Category): ApiResponse<String>

    @DELETE("/api/category/delete/{id}")
    suspend fun deleteCategory(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>

    @GET("/api/dish/list")
    suspend fun dishes(@Header("Authorization") token: String, @Query("categoryId") categoryId: Long? = null): ApiResponse<List<Dish>>

    @GET("/api/dish/search")
    suspend fun searchDishes(@Header("Authorization") token: String, @Query("keyword") keyword: String): ApiResponse<List<Dish>>

    @POST("/api/dish/add")
    suspend fun addDish(@Header("Authorization") token: String, @Body dish: Dish): ApiResponse<String>

    @PUT("/api/dish/update")
    suspend fun updateDish(@Header("Authorization") token: String, @Body dish: Dish): ApiResponse<String>

    @DELETE("/api/dish/delete/{id}")
    suspend fun deleteDish(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>

    @GET("/api/user/list")
    suspend fun recipients(@Header("Authorization") token: String): ApiResponse<List<User>>

    @PUT("/api/user/update")
    suspend fun updateProfile(@Header("Authorization") token: String, @Body request: ProfileUpdateRequest): ApiResponse<String>

    @GET("/api/couple/me")
    suspend fun coupleSpace(@Header("Authorization") token: String): ApiResponse<CoupleSpace>

    @GET("/api/couple/spaces")
    suspend fun coupleSpaces(@Header("Authorization") token: String): ApiResponse<List<CoupleSpace>>

    @POST("/api/couple/pair")
    suspend fun pairByCode(@Header("Authorization") token: String, @Body request: PairRequest): ApiResponse<CoupleSpace>

    @POST("/api/order/create")
    suspend fun createOrder(@Header("Authorization") token: String, @Body request: OrderCreateRequest): ApiResponse<Any>

    @GET("/api/order/list")
    suspend fun orders(
        @Header("Authorization") token: String,
        @Query("status") status: Int? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 30
    ): ApiResponse<OrderPage>

    @GET("/api/order/detail/{id}")
    suspend fun orderDetail(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<Order>

    @PUT("/api/order/accept/{id}")
    suspend fun acceptOrder(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>

    @PUT("/api/order/reject/{id}")
    suspend fun rejectOrder(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>

    @PUT("/api/order/complete/{id}")
    suspend fun completeOrder(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>

    @GET("/api/announcement/list")
    suspend fun announcements(@Header("Authorization") token: String): ApiResponse<List<Announcement>>

    @POST("/api/announcement/add")
    suspend fun addAnnouncement(
        @Header("Authorization") token: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): ApiResponse<String>

    @PUT("/api/announcement/update")
    suspend fun updateAnnouncement(
        @Header("Authorization") token: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): ApiResponse<String>

    @PUT("/api/announcement/pin/{id}")
    suspend fun pinAnnouncement(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: PinPayload
    ): ApiResponse<String>

    @DELETE("/api/announcement/delete/{id}")
    suspend fun deleteAnnouncement(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>

    @GET("/api/note/list")
    suspend fun notes(@Header("Authorization") token: String): ApiResponse<List<CoupleNote>>

    @POST("/api/note/add")
    suspend fun addNote(
        @Header("Authorization") token: String,
        @Body body: CoupleNoteSaveRequest
    ): ApiResponse<CoupleNote>

    @PUT("/api/note/update/{id}")
    suspend fun updateNote(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: CoupleNoteSaveRequest
    ): ApiResponse<CoupleNote>

    @PUT("/api/note/pin/{id}")
    suspend fun pinNote(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: PinPayload
    ): ApiResponse<String>

    @DELETE("/api/note/delete/{id}")
    suspend fun deleteNote(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>

    @GET("/api/anniversary/list")
    suspend fun anniversaries(@Header("Authorization") token: String): ApiResponse<List<Anniversary>>

    @POST("/api/anniversary/add")
    suspend fun addAnniversary(
        @Header("Authorization") token: String,
        @Body body: AnniversarySaveRequest
    ): ApiResponse<Anniversary>

    @PUT("/api/anniversary/update/{id}")
    suspend fun updateAnniversary(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: AnniversarySaveRequest
    ): ApiResponse<Anniversary>

    @PUT("/api/anniversary/pin/{id}")
    suspend fun pinAnniversary(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: PinPayload
    ): ApiResponse<String>

    @DELETE("/api/anniversary/delete/{id}")
    suspend fun deleteAnniversary(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>

    @GET("/api/meetup/list")
    suspend fun meetups(@Header("Authorization") token: String): ApiResponse<List<CoupleMeetup>>

    @POST("/api/meetup/add")
    suspend fun addMeetup(
        @Header("Authorization") token: String,
        @Body body: CoupleMeetupSaveRequest
    ): ApiResponse<CoupleMeetup>

    @PUT("/api/meetup/update/{id}")
    suspend fun updateMeetup(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: CoupleMeetupSaveRequest
    ): ApiResponse<CoupleMeetup>

    @DELETE("/api/meetup/delete/{id}")
    suspend fun deleteMeetup(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>


    @GET("/api/diary/list")
    suspend fun diaries(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ApiResponse<FoodDiaryPage>

    @POST("/api/diary/add")
    suspend fun createDiary(
        @Header("Authorization") token: String,
        @Body body: FoodDiarySaveRequest
    ): ApiResponse<FoodDiary>

    @PUT("/api/diary/update/{id}")
    suspend fun updateDiary(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: FoodDiarySaveRequest
    ): ApiResponse<FoodDiary>

    @DELETE("/api/diary/delete/{id}")
    suspend fun deleteDiary(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>

    @POST("/api/diary/{diaryId}/comment")
    suspend fun addDiaryComment(
        @Header("Authorization") token: String,
        @Path("diaryId") diaryId: Long,
        @Body body: FoodDiaryCommentCreateRequest
    ): ApiResponse<FoodDiaryComment>

    @DELETE("/api/diary/comment/{commentId}")
    suspend fun deleteDiaryComment(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Long
    ): ApiResponse<String>

    @GET("/api/admin/users")
    suspend fun adminUsers(@Header("Authorization") token: String): ApiResponse<List<User>>

    @POST("/api/admin/users")
    suspend fun addUser(@Header("Authorization") token: String, @Body user: UserEditorPayload): ApiResponse<String>

    @PUT("/api/admin/users/{id}")
    suspend fun updateUser(@Header("Authorization") token: String, @Path("id") id: Long, @Body user: UserEditorPayload): ApiResponse<String>

    @PUT("/api/admin/users/{id}/role")
    suspend fun updateUserRole(@Header("Authorization") token: String, @Path("id") id: Long, @Body role: RolePayload): ApiResponse<String>

    @PUT("/api/admin/users/{id}/partner")
    suspend fun updateUserPartner(@Header("Authorization") token: String, @Path("id") id: Long, @Body partner: PartnerPayload): ApiResponse<String>

    @DELETE("/api/admin/users/{id}")
    suspend fun deleteUser(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>

    @GET("/api/admin/orders")
    suspend fun adminOrders(
        @Header("Authorization") token: String,
        @Query("status") status: Int? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50
    ): ApiResponse<OrderPage>

    @DELETE("/api/admin/orders/{id}")
    suspend fun deleteOrder(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>

    @GET("/api/admin/invite-codes")
    suspend fun inviteCodes(@Header("Authorization") token: String): ApiResponse<List<InviteCodeItem>>

    @POST("/api/admin/invite-codes")
    suspend fun createInviteCode(
        @Header("Authorization") token: String,
        @Body request: InviteCodeCreateRequest
    ): ApiResponse<InviteCodeItem>

    @PUT("/api/admin/invite-codes/{id}/toggle")
    suspend fun toggleInviteCode(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body body: EnabledPayload
    ): ApiResponse<String>

    @DELETE("/api/admin/invite-codes/{id}")
    suspend fun deleteInviteCode(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>

    @Multipart
    @POST("/api/file/upload")
    suspend fun uploadFile(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): ApiResponse<UploadResponse>
}

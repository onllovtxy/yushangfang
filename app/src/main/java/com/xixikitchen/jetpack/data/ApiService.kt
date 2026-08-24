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

    @GET("/api/dish/detail/{id}")
    suspend fun dishDetail(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<Dish>

    @GET("/api/dish/search")
    suspend fun searchDishes(@Header("Authorization") token: String, @Query("keyword") keyword: String): ApiResponse<List<Dish>>

    @GET("/api/dish/random")
    suspend fun randomDishes(@Header("Authorization") token: String, @Query("count") count: Int = 1): ApiResponse<List<Dish>>

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

    @POST("/api/order/rate")
    suspend fun rateDish(@Header("Authorization") token: String, @Body request: RateDishRequest): ApiResponse<String>

    @GET("/api/order/stats")
    suspend fun orderStats(@Header("Authorization") token: String): ApiResponse<OrderStats>

    @GET("/api/announcement/list")
    suspend fun announcements(@Header("Authorization") token: String): ApiResponse<List<Announcement>>

    @POST("/api/announcement/add")
    suspend fun addAnnouncement(@Header("Authorization") token: String, @Body body: Map<String, String>): ApiResponse<String>

    @PUT("/api/announcement/update")
    suspend fun updateAnnouncement(@Header("Authorization") token: String, @Body body: Map<String, Any>): ApiResponse<String>

    @DELETE("/api/announcement/delete/{id}")
    suspend fun deleteAnnouncement(@Header("Authorization") token: String, @Path("id") id: Long): ApiResponse<String>

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

    @Multipart
    @POST("/api/file/upload")
    suspend fun uploadFile(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): ApiResponse<UploadResponse>
}

package com.stocksync.app.data.api

import retrofit2.http.*

data class LoginRequest(val googleIdToken: String)
data class LoginResponse(val jwtToken: String, val userId: String, val expiresAt: String)
data class RefreshRequest(val googleIdToken: String)
data class RefreshResponse(val jwtToken: String, val expiresAt: String)

data class ProductDto(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val category: String?,
    val barcode: String?,
    val photoUrl: String?,
    val lowStockThreshold: Int,
    val isActive: Boolean,
    val updatedAt: String
)

data class CreateProductDto(
    val name: String,
    val price: Double,
    val quantity: Int,
    val category: String?,
    val barcode: String?,
    val photoBase64: String?
)

data class UpdateProductDto(
    val name: String,
    val price: Double,
    val category: String?,
    val barcode: String?,
    val photoBase64: String?
)

data class SaleDto(
    val clientSaleId: String,
    val productId: String,
    val quantity: Int,
    val salePrice: Double,
    val paymentMethod: String,
    val timestamp: String
)

data class SaleResponseDto(
    val serverSaleId: String,
    val clientSaleId: String,
    val status: String,
    val productQuantityAfter: Int
)

data class StockAdjustmentDto(
    val clientAdjustmentId: String,
    val productId: String,
    val quantityChange: Int,
    val note: String?,
    val timestamp: String
)

data class StockAdjustmentResponseDto(
    val serverAdjustmentId: String,
    val clientAdjustmentId: String,
    val status: String,
    val productQuantityAfter: Int
)

data class SyncBatchDto(
    val sales: List<SaleDto>,
    val adjustments: List<StockAdjustmentDto>,
    val productUpdates: List<ProductUpdateDto>
)

data class ProductUpdateDto(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val category: String?,
    val barcode: String?,
    val updatedAt: String
)

data class SyncResultDto(
    val results: List<SyncResultItemDto>,
    val serverProducts: List<ProductDto>
)

data class SyncResultItemDto(
    val clientRecordId: String,
    val status: String,
    val serverId: String?
)

data class DashboardDto(
    val totalSales: Double,
    val transactionCount: Int,
    val lowStockCount: Int,
    val topProducts: List<TopProductDto>
)

data class TopProductDto(
    val name: String,
    val quantitySold: Int
)

data class SettingsDto(
    val language: String,
    val currency: String,
    val lowStockThreshold: Int,
    val notifyLowStock: Boolean,
    val notifyDailySummary: Boolean,
    val notifySyncComplete: Boolean,
    val biometricLock: Boolean
)

data class DeviceRegistrationDto(val fcmToken: String)

interface StockSyncApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): RefreshResponse

    @GET("api/products")
    suspend fun getProducts(@Header("Authorization") token: String): List<ProductDto>

    @GET("api/products/{id}")
    suspend fun getProduct(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ProductDto

    @POST("api/products")
    suspend fun createProduct(
        @Header("Authorization") token: String,
        @Body dto: CreateProductDto
    ): ProductDto

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body dto: UpdateProductDto
    ): ProductDto

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Header("Authorization") token: String,
        @Path("id") id: String
    )

    @POST("api/sales")
    suspend fun logSale(
        @Header("Authorization") token: String,
        @Body dto: SaleDto
    ): SaleResponseDto

    @POST("api/stock-adjustments")
    suspend fun adjustStock(
        @Header("Authorization") token: String,
        @Body dto: StockAdjustmentDto
    ): StockAdjustmentResponseDto

    @POST("api/sync/batch")
    suspend fun syncBatch(
        @Header("Authorization") token: String,
        @Body dto: SyncBatchDto
    ): SyncResultDto

    @GET("api/dashboard/today")
    suspend fun getDashboard(@Header("Authorization") token: String): DashboardDto

    @GET("api/settings")
    suspend fun getSettings(@Header("Authorization") token: String): SettingsDto

    @PUT("api/settings")
    suspend fun updateSettings(
        @Header("Authorization") token: String,
        @Body dto: SettingsDto
    ): SettingsDto

    @POST("api/devices/register")
    suspend fun registerDevice(
        @Header("Authorization") token: String,
        @Body dto: DeviceRegistrationDto
    ): Map<String, Boolean>
}

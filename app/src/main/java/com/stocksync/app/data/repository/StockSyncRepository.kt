package com.stocksync.app.data.repository

import android.util.Log
import com.google.gson.Gson
import com.stocksync.app.data.api.*
import com.stocksync.app.data.db.*
import com.stocksync.app.data.firestore.FirestoreManager
import com.stocksync.app.data.preferences.TokenManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class StockSyncRepository(
    private val api: StockSyncApi,
    private val db: StockSyncDatabase,
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "StockSyncRepository"
    }

    private val productDao = db.productDao()
    private val saleDao = db.saleDao()
    private val adjustmentDao = db.stockAdjustmentDao()
    private val pendingSyncDao = db.pendingSyncDao()
    private val firestoreManager = FirestoreManager()
    private val gson = Gson()

    // Auth
    suspend fun login(googleIdToken: String): Result<LoginResponse> {
        return try {
            Log.d(TAG, "Attempting login with Google token")
            val response = api.login(LoginRequest(googleIdToken))
            tokenManager.saveJwtToken(response.jwtToken)
            tokenManager.saveUserId(response.userId)
            tokenManager.saveGoogleIdToken(googleIdToken)
            tokenManager.saveTokenExpiry(response.expiresAt)
            Log.d(TAG, "Login successful, userId: ${response.userId}")
            Result.success(response)
        } catch (e: Exception) {
            Log.e(TAG, "Login failed", e)
            Result.failure(e)
        }
    }

    fun getAuthToken(): String? = tokenManager.getJwtToken()
    fun getUserId(): String? = tokenManager.getUserId()
    fun isLoggedIn(): Boolean = tokenManager.isLoggedIn()

    fun logout() {
        Log.d(TAG, "Logging out, clearing tokens")
        tokenManager.clearAll()
    }

    // Products
    fun getAllProducts() = productDao.getAllProducts()
    fun getLowStockProducts(threshold: Int) = productDao.getLowStockProducts(threshold)
    fun getProductCount() = productDao.getProductCount()

    suspend fun addProduct(
        name: String,
        price: Double,
        quantity: Int,
        category: String?,
        barcode: String?,
        photoBlob: ByteArray? = null
    ) {
        val productId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val product = ProductEntity(
            id = productId,
            userId = tokenManager.getUserId() ?: "",
            name = name,
            price = price,
            quantity = quantity,
            category = category,
            barcode = barcode,
            photoUrl = null,
            photoBlob = photoBlob,
            lowStockThreshold = 5,
            isActive = true,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING
        )
        productDao.upsertProduct(product)

        val payload = gson.toJson(mapOf(
            "id" to productId,
            "name" to name,
            "price" to price,
            "quantity" to quantity,
            "category" to (category ?: ""),
            "barcode" to (barcode ?: "")
        ))
        pendingSyncDao.insertPending(PendingSyncEntity(
            id = UUID.randomUUID().toString(),
            recordType = "PRODUCT_CREATE",
            recordId = productId,
            payload = payload,
            createdAt = now
        ))
        Log.d(TAG, "Product added locally: $name, pending sync")

        firestoreManager.storeProduct(
            productId = productId,
            userId = tokenManager.getUserId() ?: "",
            name = name,
            price = price,
            quantity = quantity,
            category = category,
            barcode = barcode
        )
    }

    suspend fun updateProduct(product: ProductEntity) {
        val now = System.currentTimeMillis()
        productDao.upsertProduct(product.copy(syncStatus = SyncStatus.PENDING, updatedAt = now))

        val payload = gson.toJson(mapOf(
            "id" to product.id,
            "name" to product.name,
            "price" to product.price,
            "quantity" to product.quantity,
            "category" to (product.category ?: ""),
            "barcode" to (product.barcode ?: ""),
            "updatedAt" to now.toString()
        ))
        pendingSyncDao.insertPending(PendingSyncEntity(
            id = UUID.randomUUID().toString(),
            recordType = "PRODUCT_UPDATE",
            recordId = product.id,
            payload = payload,
            createdAt = now
        ))
        Log.d(TAG, "Product updated locally: ${product.name}, pending sync")
    }

    suspend fun deleteProduct(productId: String) {
        val now = System.currentTimeMillis()
        productDao.softDelete(productId, now)

        val payload = gson.toJson(mapOf(
            "id" to productId,
            "deletedAt" to now.toString()
        ))
        pendingSyncDao.insertPending(PendingSyncEntity(
            id = UUID.randomUUID().toString(),
            recordType = "PRODUCT_DELETE",
            recordId = productId,
            payload = payload,
            createdAt = now
        ))
        Log.d(TAG, "Product soft-deleted locally: $productId, pending sync")
    }

    suspend fun adjustStock(productId: String, quantityChange: Int, note: String?) {
        val product = productDao.getProductById(productId) ?: return
        val newQty = maxOf(0, product.quantity + quantityChange)
        productDao.updateQuantity(productId, newQty, System.currentTimeMillis())

        val adjustmentId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val adjustment = StockAdjustmentEntity(
            id = UUID.randomUUID().toString(),
            clientAdjustmentId = adjustmentId,
            userId = tokenManager.getUserId() ?: "",
            productId = productId,
            quantityChange = quantityChange,
            note = note,
            timestamp = now,
            syncStatus = SyncStatus.PENDING
        )
        adjustmentDao.insertAdjustment(adjustment)

        val payload = gson.toJson(mapOf(
            "clientAdjustmentId" to adjustmentId,
            "productId" to productId,
            "quantityChange" to quantityChange,
            "note" to (note ?: ""),
            "timestamp" to now.toString()
        ))
        pendingSyncDao.insertPending(PendingSyncEntity(
            id = UUID.randomUUID().toString(),
            recordType = "STOCK_ADJUSTMENT",
            recordId = adjustmentId,
            payload = payload,
            createdAt = now
        ))
        Log.d(TAG, "Stock adjusted for product $productId by $quantityChange, pending sync")
    }

    // Sales
    fun getAllSales() = saleDao.getAllSales()
    fun getTodaySalesTotal(startOfDay: Long) = saleDao.getTodaySalesTotal(startOfDay)
    fun getTodayTransactionCount(startOfDay: Long) = saleDao.getTodayTransactionCount(startOfDay)
    fun getTopSellingProducts(startOfDay: Long) = saleDao.getTopSellingProducts(startOfDay)

    suspend fun logSale(productId: String, quantity: Int, salePrice: Double, paymentMethod: String) {
        val product = productDao.getProductById(productId) ?: return
        val newQty = maxOf(0, product.quantity - quantity)
        productDao.updateQuantity(productId, newQty, System.currentTimeMillis())

        val clientSaleId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val sale = SaleEntity(
            id = UUID.randomUUID().toString(),
            clientSaleId = clientSaleId,
            userId = tokenManager.getUserId() ?: "",
            productId = productId,
            quantity = quantity,
            salePrice = salePrice,
            paymentMethod = paymentMethod,
            timestamp = now,
            syncStatus = SyncStatus.PENDING
        )
        saleDao.insertSale(sale)

        val payload = gson.toJson(mapOf(
            "clientSaleId" to clientSaleId,
            "productId" to productId,
            "quantity" to quantity,
            "salePrice" to salePrice,
            "paymentMethod" to paymentMethod,
            "timestamp" to now.toString()
        ))
        pendingSyncDao.insertPending(PendingSyncEntity(
            id = UUID.randomUUID().toString(),
            recordType = "SALE",
            recordId = clientSaleId,
            payload = payload,
            createdAt = now
        ))
        Log.d(TAG, "Sale logged for product $productId, qty $quantity, pending sync")

        firestoreManager.storeSale(
            saleId = clientSaleId,
            userId = tokenManager.getUserId() ?: "",
            productId = productId,
            productName = product.name,
            quantity = quantity,
            salePrice = salePrice,
            paymentMethod = paymentMethod,
            timestamp = now
        )
    }

    suspend fun readSalesFromFirestore(limit: Long = 10): Result<List<Map<String, Any?>>> {
        val userId = tokenManager.getUserId() ?: return Result.failure(Exception("Not authenticated"))
        return firestoreManager.readRecentSales(userId, limit)
    }

    suspend fun readProductsFromFirestore(): Result<List<Map<String, Any?>>> {
        val userId = tokenManager.getUserId() ?: return Result.failure(Exception("Not authenticated"))
        return firestoreManager.readProducts(userId)
    }

    // Sync
    fun getPendingCount() = pendingSyncDao.getPendingCount()

    suspend fun syncPending(): Result<SyncResultDto?> {
        val token = tokenManager.getJwtToken() ?: return Result.failure(Exception("Not authenticated"))
        val pending = pendingSyncDao.getPendingRecords()
        if (pending.isEmpty()) {
            Log.d(TAG, "No pending records to sync")
            return Result.success(null)
        }

        return try {
            Log.d(TAG, "Syncing ${pending.size} pending records")
            pending.forEach { pendingSyncDao.updateStatus(it.id, SyncStatus.SYNCING, System.currentTimeMillis()) }

            val sales = mutableListOf<SaleDto>()
            val adjustments = mutableListOf<StockAdjustmentDto>()
            val productUpdates = mutableListOf<ProductUpdateDto>()
            val productCreates = mutableListOf<ProductUpdateDto>()

            for (record in pending) {
                try {
                    when (record.recordType) {
                        "SALE" -> {
                            val data = gson.fromJson(record.payload, Map::class.java)
                            sales.add(SaleDto(
                                clientSaleId = data["clientSaleId"] as String,
                                productId = data["productId"] as String,
                                quantity = (data["quantity"] as Number).toInt(),
                                salePrice = (data["salePrice"] as Number).toDouble(),
                                paymentMethod = data["paymentMethod"] as String,
                                timestamp = millisToIso(data["timestamp"] as String)
                            ))
                        }
                        "STOCK_ADJUSTMENT" -> {
                            val data = gson.fromJson(record.payload, Map::class.java)
                            adjustments.add(StockAdjustmentDto(
                                clientAdjustmentId = data["clientAdjustmentId"] as String,
                                productId = data["productId"] as String,
                                quantityChange = (data["quantityChange"] as Number).toInt(),
                                note = data["note"] as String?,
                                timestamp = millisToIso(data["timestamp"] as String)
                            ))
                        }
                        "PRODUCT_CREATE" -> {
                            val data = gson.fromJson(record.payload, Map::class.java)
                            val dto = ProductUpdateDto(
                                id = data["id"] as String,
                                name = data["name"] as String,
                                price = (data["price"] as Number).toDouble(),
                                quantity = (data["quantity"] as Number).toInt(),
                                category = data["category"] as String?,
                                barcode = data["barcode"] as String?,
                                updatedAt = millisToIso(data.get("updatedAt") as? String ?: System.currentTimeMillis().toString())
                            )
                            productCreates.add(dto)
                        }
                        "PRODUCT_UPDATE" -> {
                            val data = gson.fromJson(record.payload, Map::class.java)
                            productUpdates.add(ProductUpdateDto(
                                id = data["id"] as String,
                                name = data["name"] as String,
                                price = (data["price"] as Number).toDouble(),
                                quantity = (data["quantity"] as Number).toInt(),
                                category = data["category"] as String?,
                                barcode = data["barcode"] as String?,
                                updatedAt = millisToIso(data["updatedAt"] as String)
                            ))
                        }
                        "PRODUCT_DELETE" -> {
                            val data = gson.fromJson(record.payload, Map::class.java)
                            productUpdates.add(ProductUpdateDto(
                                id = data["id"] as String,
                                name = "__DELETED__",
                                price = 0.0,
                                quantity = 0,
                                category = null,
                                barcode = null,
                                updatedAt = millisToIso(data["deletedAt"] as String)
                            ))
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse pending record ${record.id}", e)
                }
            }

            val batch = SyncBatchDto(
                sales = sales,
                adjustments = adjustments,
                productUpdates = productCreates + productUpdates
            )
            val result = api.syncBatch("Bearer $token", batch)

            pending.forEach { record ->
                pendingSyncDao.updateStatus(record.id, SyncStatus.SYNCED, System.currentTimeMillis())
                when (record.recordType) {
                    "SALE" -> saleDao.updateSyncStatus(record.recordId, SyncStatus.SYNCED)
                    "STOCK_ADJUSTMENT" -> adjustmentDao.updateSyncStatus(record.recordId, SyncStatus.SYNCED)
                    "PRODUCT_CREATE", "PRODUCT_UPDATE", "PRODUCT_DELETE" -> {
                        productDao.updateSyncStatus(record.recordId, SyncStatus.SYNCED)
                    }
                }
            }
            pendingSyncDao.deleteSynced()

            Log.d(TAG, "Sync complete: ${result.results.size} results")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed", e)
            pending.forEach { pendingSyncDao.updateStatus(it.id, SyncStatus.PENDING, System.currentTimeMillis()) }
            Result.failure(e)
        }
    }

    // Dashboard
    suspend fun fetchProduct(productId: String): Result<ProductDto> {
        val token = tokenManager.getJwtToken() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val product = api.getProduct("Bearer $token", productId)
            Result.success(product)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch product $productId", e)
            Result.failure(e)
        }
    }

    suspend fun fetchDashboard(): Result<DashboardDto> {
        val token = tokenManager.getJwtToken() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val dashboard = api.getDashboard("Bearer $token")
            Result.success(dashboard)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch dashboard", e)
            Result.failure(e)
        }
    }

    // Settings
    suspend fun fetchSettings(): Result<SettingsDto> {
        val token = tokenManager.getJwtToken() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val settings = api.getSettings("Bearer $token")
            Result.success(settings)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch settings", e)
            Result.failure(e)
        }
    }

    suspend fun pushSettings(settings: SettingsDto): Result<SettingsDto> {
        val token = tokenManager.getJwtToken() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val result = api.updateSettings("Bearer $token", settings)
            Log.d(TAG, "Settings pushed to server")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push settings", e)
            Result.failure(e)
        }
    }

    // Device registration
    suspend fun registerDevice(fcmToken: String): Result<Map<String, Boolean>> {
        val token = tokenManager.getJwtToken() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val result = api.registerDevice("Bearer $token", DeviceRegistrationDto(fcmToken))
            Log.d(TAG, "FCM token registered with server")
            Result.success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register FCM token", e)
            Result.failure(e)
        }
    }

    private fun millisToIso(millis: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.format(Date(millis.toLong()))
        } catch (e: Exception) {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())
        }
    }
}

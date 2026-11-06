package com.stocksync.app.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE quantity < :threshold AND isActive = 1")
    fun getLowStockProducts(threshold: Int): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    fun getProductCount(): Flow<Int>

    @Query("SELECT * FROM products WHERE syncStatus = 'PENDING'")
    suspend fun getPendingProducts(): List<ProductEntity>

    @Upsert
    suspend fun upsertProduct(product: ProductEntity)

    @Query("UPDATE products SET quantity = :quantity, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: Int, updatedAt: Long)

    @Query("UPDATE products SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("UPDATE products SET isActive = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun softDelete(id: String, updatedAt: Long)

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSales(): List<SaleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity)

    @Query("UPDATE sales SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("SELECT COALESCE(SUM(salePrice * quantity), 0.0) FROM sales WHERE timestamp >= :startOfDay")
    fun getTodaySalesTotal(startOfDay: Long): Flow<Double>

    @Query("SELECT COUNT(*) FROM sales WHERE timestamp >= :startOfDay")
    fun getTodayTransactionCount(startOfDay: Long): Flow<Int>

    @Query("SELECT productId, SUM(quantity) as totalQty FROM sales WHERE timestamp >= :startOfDay GROUP BY productId ORDER BY totalQty DESC LIMIT 3")
    fun getTopSellingProducts(startOfDay: Long): Flow<List<ProductQuantityResult>>

    @Query("DELETE FROM sales")
    suspend fun deleteAll()
}

data class ProductQuantityResult(
    val productId: String,
    val totalQty: Int
)

@Dao
interface StockAdjustmentDao {
    @Query("SELECT * FROM stock_adjustments WHERE syncStatus = 'PENDING'")
    suspend fun getPendingAdjustments(): List<StockAdjustmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdjustment(adjustment: StockAdjustmentEntity)

    @Query("UPDATE stock_adjustments SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)

    @Query("DELETE FROM stock_adjustments")
    suspend fun deleteAll()
}

@Dao
interface PendingSyncDao {
    @Query("SELECT * FROM pending_sync WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingRecords(): List<PendingSyncEntity>

    @Query("SELECT COUNT(*) FROM pending_sync WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPending(record: PendingSyncEntity)

    @Query("UPDATE pending_sync SET status = :status, lastAttemptAt = :timestamp WHERE id = :id")
    suspend fun updateStatus(id: String, status: SyncStatus, timestamp: Long)

    @Query("DELETE FROM pending_sync WHERE status = 'SYNCED'")
    suspend fun deleteSynced()

    @Query("DELETE FROM pending_sync")
    suspend fun deleteAll()
}

package com.stocksync.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val category: String?,
    val barcode: String?,
    val photoUrl: String?,
    @androidx.room.TypeConverters(ByteArrayConverter::class)
    val photoBlob: ByteArray? = null,
    val lowStockThreshold: Int = 5,
    val isActive: Boolean = true,
    val updatedAt: Long,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

@Entity(tableName = "sales", indices = [Index("clientSaleId", unique = true)])
data class SaleEntity(
    @PrimaryKey val id: String,
    val clientSaleId: String,
    val userId: String,
    val productId: String,
    val quantity: Int,
    val salePrice: Double,
    val paymentMethod: String,
    val timestamp: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

@Entity(tableName = "stock_adjustments", indices = [Index("clientAdjustmentId", unique = true)])
data class StockAdjustmentEntity(
    @PrimaryKey val id: String,
    val clientAdjustmentId: String,
    val userId: String,
    val productId: String,
    val quantityChange: Int,
    val note: String?,
    val timestamp: Long,
    val syncStatus: SyncStatus = SyncStatus.PENDING
)

@Entity(tableName = "pending_sync")
data class PendingSyncEntity(
    @PrimaryKey val id: String,
    val recordType: String,
    val recordId: String,
    val payload: String,
    val status: SyncStatus = SyncStatus.PENDING,
    val retryCount: Int = 0,
    val createdAt: Long,
    val lastAttemptAt: Long? = null
)

enum class SyncStatus {
    PENDING, SYNCING, SYNCED, FAILED
}

/**
 * Type converter for storing product photo BLOBs (byte arrays) in RoomDB.
 * Enables BLOB storage of image data directly in the SQLite database.
 */
class ByteArrayConverter {
    @androidx.room.TypeConverter
    fun fromByteArray(bytes: ByteArray?): String? {
        if (bytes == null) return null
        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    }

    @androidx.room.TypeConverter
    fun toByteArray(value: String?): ByteArray? {
        if (value == null) return null
        return android.util.Base64.decode(value, android.util.Base64.NO_WRAP)
    }
}

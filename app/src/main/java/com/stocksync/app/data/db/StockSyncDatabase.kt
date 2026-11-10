package com.stocksync.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        StockAdjustmentEntity::class,
        PendingSyncEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(ByteArrayConverter::class)
abstract class StockSyncDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun stockAdjustmentDao(): StockAdjustmentDao
    abstract fun pendingSyncDao(): PendingSyncDao

    companion object {
        @Volatile
        private var INSTANCE: StockSyncDatabase? = null

        fun getDatabase(context: Context): StockSyncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StockSyncDatabase::class.java,
                    "stocksync.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

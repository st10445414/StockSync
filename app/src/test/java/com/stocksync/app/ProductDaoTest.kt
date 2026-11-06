package com.stocksync.app

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.stocksync.app.data.db.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ProductDaoTest {

    private lateinit var db: StockSyncDatabase
    private lateinit var productDao: ProductDao
    private lateinit var saleDao: SaleDao
    private lateinit var pendingSyncDao: PendingSyncDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, StockSyncDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        productDao = db.productDao()
        saleDao = db.saleDao()
        pendingSyncDao = db.pendingSyncDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun insertProduct_andRetrieve() = runBlocking {
        val product = ProductEntity(
            id = "test-1",
            userId = "user-1",
            name = "Coca-Cola 500ml",
            price = 8.0,
            quantity = 24,
            category = "Beverages",
            barcode = "6001234567890",
            photoUrl = null,
            lowStockThreshold = 5,
            isActive = true,
            updatedAt = System.currentTimeMillis()
        )
        productDao.upsertProduct(product)

        val retrieved = productDao.getProductById("test-1")
        assertNotNull(retrieved)
        assertEquals("Coca-Cola 500ml", retrieved!!.name)
        assertEquals(8.0, retrieved.price, 0.01)
        assertEquals(24, retrieved.quantity)
    }

    @Test
    fun getAllProducts_returnsOnlyActive() = runBlocking {
        productDao.upsertProduct(ProductEntity(
            id = "1", userId = "u", name = "Active", price = 10.0, quantity = 5,
            category = null, barcode = null, photoUrl = null, lowStockThreshold = 5,
            isActive = true, updatedAt = System.currentTimeMillis()
        ))
        productDao.upsertProduct(ProductEntity(
            id = "2", userId = "u", name = "Inactive", price = 10.0, quantity = 5,
            category = null, barcode = null, photoUrl = null, lowStockThreshold = 5,
            isActive = false, updatedAt = System.currentTimeMillis()
        ))

        val products = productDao.getAllProducts().first()
        assertEquals(1, products.size)
        assertEquals("Active", products[0].name)
    }

    @Test
    fun softDelete_setsInactive() = runBlocking {
        productDao.upsertProduct(ProductEntity(
            id = "1", userId = "u", name = "Test", price = 10.0, quantity = 5,
            category = null, barcode = null, photoUrl = null, lowStockThreshold = 5,
            isActive = true, updatedAt = System.currentTimeMillis()
        ))
        productDao.softDelete("1", System.currentTimeMillis())

        val products = productDao.getAllProducts().first()
        assertTrue(products.isEmpty())
    }

    @Test
    fun updateQuantity_updatesCorrectly() = runBlocking {
        productDao.upsertProduct(ProductEntity(
            id = "1", userId = "u", name = "Test", price = 10.0, quantity = 10,
            category = null, barcode = null, photoUrl = null, lowStockThreshold = 5,
            isActive = true, updatedAt = System.currentTimeMillis()
        ))
        productDao.updateQuantity("1", 7, System.currentTimeMillis())

        val product = productDao.getProductById("1")
        assertEquals(7, product!!.quantity)
    }

    @Test
    fun getLowStockProducts_returnsOnlyBelowThreshold() = runBlocking {
        productDao.upsertProduct(ProductEntity(
            id = "1", userId = "u", name = "Low", price = 10.0, quantity = 2,
            category = null, barcode = null, photoUrl = null, lowStockThreshold = 5,
            isActive = true, updatedAt = System.currentTimeMillis()
        ))
        productDao.upsertProduct(ProductEntity(
            id = "2", userId = "u", name = "OK", price = 10.0, quantity = 20,
            category = null, barcode = null, photoUrl = null, lowStockThreshold = 5,
            isActive = true, updatedAt = System.currentTimeMillis()
        ))

        val lowStock = productDao.getLowStockProducts(5).first()
        assertEquals(1, lowStock.size)
        assertEquals("Low", lowStock[0].name)
    }

    @Test
    fun insertSale_andRetrievePending() = runBlocking {
        val sale = SaleEntity(
            id = "sale-1",
            clientSaleId = "client-1",
            userId = "u",
            productId = "p1",
            quantity = 2,
            salePrice = 10.0,
            paymentMethod = "CASH",
            timestamp = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
        saleDao.insertSale(sale)

        val pending = saleDao.getPendingSales()
        assertEquals(1, pending.size)
        assertEquals("client-1", pending[0].clientSaleId)
    }

    @Test
    fun insertPendingSync_andRetrieve() = runBlocking {
        val record = PendingSyncEntity(
            id = "ps-1",
            recordType = "SALE",
            recordId = "sale-1",
            payload = "{}",
            status = SyncStatus.PENDING,
            createdAt = System.currentTimeMillis()
        )
        pendingSyncDao.insertPending(record)

        val pending = pendingSyncDao.getPendingRecords()
        assertEquals(1, pending.size)
        assertEquals("SALE", pending[0].recordType)
    }

    @Test
    fun updateSyncStatus_changesStatus() = runBlocking {
        productDao.upsertProduct(ProductEntity(
            id = "1", userId = "u", name = "Test", price = 10.0, quantity = 5,
            category = null, barcode = null, photoUrl = null, lowStockThreshold = 5,
            isActive = true, updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        ))
        productDao.updateSyncStatus("1", SyncStatus.SYNCED)

        val product = productDao.getProductById("1")
        assertEquals(SyncStatus.SYNCED, product!!.syncStatus)
    }
}

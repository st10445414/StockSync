package com.stocksync.app.data.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

/**
 * FirestoreManager handles NoSQL database operations using Firebase Firestore.
 *
 * This demonstrates reading from and writing to a NoSQL document database.
 * Sales data is synced to Firestore collections for cloud-based analytics,
 * complementing the RoomDB (SQLite) local storage and the REST API.
 */
class FirestoreManager {

    companion object {
        private const val TAG = "FirestoreManager"
        private const val COLLECTION_SALES = "sales"
        private const val COLLECTION_PRODUCTS = "products"
    }

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    /**
     * Stores a sale record in Firestore (NoSQL write operation).
     * Called after a sale is logged locally in RoomDB.
     */
    suspend fun storeSale(
        saleId: String,
        userId: String,
        productId: String,
        productName: String,
        quantity: Int,
        salePrice: Double,
        paymentMethod: String,
        timestamp: Long
    ): Result<Unit> {
        return try {
            val saleData = hashMapOf(
                "saleId" to saleId,
                "userId" to userId,
                "productId" to productId,
                "productName" to productName,
                "quantity" to quantity,
                "salePrice" to salePrice,
                "paymentMethod" to paymentMethod,
                "timestamp" to timestamp,
                "createdAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_SALES)
                .document(saleId)
                .set(saleData)
                .await()
            Log.d(TAG, "Sale stored in Firestore: $saleId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store sale in Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Reads recent sales from Firestore (NoSQL read operation).
     * Returns the latest N sales documents ordered by timestamp descending.
     */
    suspend fun readRecentSales(userId: String, limit: Long = 10): Result<List<Map<String, Any?>>> {
        return try {
            val snapshot = db.collection(COLLECTION_SALES)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            val sales = snapshot.documents.map { doc ->
                doc.data ?: emptyMap()
            }
            Log.d(TAG, "Read ${sales.size} sales from Firestore for user $userId")
            Result.success(sales)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read sales from Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Stores a product record in Firestore (NoSQL write operation).
     */
    suspend fun storeProduct(
        productId: String,
        userId: String,
        name: String,
        price: Double,
        quantity: Int,
        category: String?,
        barcode: String?
    ): Result<Unit> {
        return try {
            val productData = hashMapOf(
                "productId" to productId,
                "userId" to userId,
                "name" to name,
                "price" to price,
                "quantity" to quantity,
                "category" to category,
                "barcode" to barcode,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection(COLLECTION_PRODUCTS)
                .document(productId)
                .set(productData)
                .await()
            Log.d(TAG, "Product stored in Firestore: $productId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to store product in Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Reads all products from Firestore for a user (NoSQL read operation).
     */
    suspend fun readProducts(userId: String): Result<List<Map<String, Any?>>> {
        return try {
            val snapshot = db.collection(COLLECTION_PRODUCTS)
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val products = snapshot.documents.map { doc ->
                doc.data ?: emptyMap()
            }
            Log.d(TAG, "Read ${products.size} products from Firestore for user $userId")
            Result.success(products)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read products from Firestore", e)
            Result.failure(e)
        }
    }
}

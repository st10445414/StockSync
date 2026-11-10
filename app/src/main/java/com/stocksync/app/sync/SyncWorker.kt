package com.stocksync.app.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.stocksync.app.data.api.ApiClient
import com.stocksync.app.data.db.StockSyncDatabase
import com.stocksync.app.data.preferences.TokenManager
import com.stocksync.app.data.repository.StockSyncRepository

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"
        const val WORK_NAME = "stocksync_sync"
    }

    override suspend fun doWork(): Result {
        Log.d(TAG, "Background sync started")

        val context = applicationContext
        val tokenManager = TokenManager(context)

        if (!tokenManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, skipping sync")
            return Result.success()
        }

        val db = StockSyncDatabase.getDatabase(context)
        val repository = StockSyncRepository(ApiClient.api, db, tokenManager)

        return try {
            val result = repository.syncPending()
            if (result.isSuccess) {
                Log.d(TAG, "Background sync completed successfully")
                Result.success()
            } else {
                val error = result.exceptionOrNull()
                Log.e(TAG, "Background sync failed", error)
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Background sync exception", e)
            Result.retry()
        }
    }
}

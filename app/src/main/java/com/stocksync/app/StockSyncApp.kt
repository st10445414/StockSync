package com.stocksync.app

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.stocksync.app.sync.SyncWorker
import java.util.concurrent.TimeUnit

class StockSyncApp : Application(), Configuration.Provider {

    companion object {
        private const val TAG = "StockSyncApp"
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application created, initializing WorkManager sync")

        // Schedule periodic sync every 15 minutes when network is available
        val syncConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(syncConstraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncWork
        )

        Log.d(TAG, "Periodic sync work scheduled every 15 minutes")
    }
}

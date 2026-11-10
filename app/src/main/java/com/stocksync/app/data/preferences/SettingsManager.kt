package com.stocksync.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class UserSettings(
    val language: String = "en",
    val currency: String = "ZAR",
    val lowStockThreshold: Int = 5,
    val notifyLowStock: Boolean = true,
    val notifyDailySummary: Boolean = true,
    val notifySyncComplete: Boolean = false,
    val biometricLock: Boolean = true
)

class SettingsManager(private val context: Context) {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language")
        val CURRENCY = stringPreferencesKey("currency")
        val LOW_STOCK_THRESHOLD = intPreferencesKey("low_stock_threshold")
        val NOTIFY_LOW_STOCK = booleanPreferencesKey("notify_low_stock")
        val NOTIFY_DAILY_SUMMARY = booleanPreferencesKey("notify_daily_summary")
        val NOTIFY_SYNC_COMPLETE = booleanPreferencesKey("notify_sync_complete")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
    }

    val settings: Flow<UserSettings> = context.settingsDataStore.data.map { prefs ->
        UserSettings(
            language = prefs[Keys.LANGUAGE] ?: "en",
            currency = prefs[Keys.CURRENCY] ?: "ZAR",
            lowStockThreshold = prefs[Keys.LOW_STOCK_THRESHOLD] ?: 5,
            notifyLowStock = prefs[Keys.NOTIFY_LOW_STOCK] ?: true,
            notifyDailySummary = prefs[Keys.NOTIFY_DAILY_SUMMARY] ?: true,
            notifySyncComplete = prefs[Keys.NOTIFY_SYNC_COMPLETE] ?: false,
            biometricLock = prefs[Keys.BIOMETRIC_LOCK] ?: true
        )
    }

    suspend fun updateLanguage(language: String) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = language }
    }

    suspend fun updateCurrency(currency: String) {
        context.settingsDataStore.edit { it[Keys.CURRENCY] = currency }
    }

    suspend fun updateLowStockThreshold(threshold: Int) {
        context.settingsDataStore.edit { it[Keys.LOW_STOCK_THRESHOLD] = threshold }
    }

    suspend fun updateNotifyLowStock(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.NOTIFY_LOW_STOCK] = enabled }
    }

    suspend fun updateNotifyDailySummary(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.NOTIFY_DAILY_SUMMARY] = enabled }
    }

    suspend fun updateNotifySyncComplete(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.NOTIFY_SYNC_COMPLETE] = enabled }
    }

    suspend fun updateBiometricLock(enabled: Boolean) {
        context.settingsDataStore.edit { it[Keys.BIOMETRIC_LOCK] = enabled }
    }

    suspend fun updateAll(settings: UserSettings) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.LANGUAGE] = settings.language
            prefs[Keys.CURRENCY] = settings.currency
            prefs[Keys.LOW_STOCK_THRESHOLD] = settings.lowStockThreshold
            prefs[Keys.NOTIFY_LOW_STOCK] = settings.notifyLowStock
            prefs[Keys.NOTIFY_DAILY_SUMMARY] = settings.notifyDailySummary
            prefs[Keys.NOTIFY_SYNC_COMPLETE] = settings.notifySyncComplete
            prefs[Keys.BIOMETRIC_LOCK] = settings.biometricLock
        }
    }
}

package com.stocksync.app.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stocksync.app.data.api.SettingsDto
import com.stocksync.app.data.preferences.SettingsManager
import com.stocksync.app.data.preferences.UserSettings
import com.stocksync.app.data.repository.StockSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val repository: StockSyncRepository
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
    }

    private val _settings = MutableStateFlow(UserSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    private val _syncResult = MutableStateFlow<String?>(null)
    val syncResult: StateFlow<String?> = _syncResult.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.settings.collect { _settings.value = it }
        }
    }

    fun updateLanguage(language: String) {
        viewModelScope.launch {
            settingsManager.updateLanguage(language)
            pushSettingsToServer()
            Log.d(TAG, "Language updated to $language")
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            settingsManager.updateCurrency(currency)
            pushSettingsToServer()
            Log.d(TAG, "Currency updated to $currency")
        }
    }

    fun updateLowStockThreshold(threshold: Int) {
        viewModelScope.launch {
            settingsManager.updateLowStockThreshold(threshold)
            pushSettingsToServer()
            Log.d(TAG, "Low-stock threshold updated to $threshold")
        }
    }

    fun updateNotifyLowStock(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateNotifyLowStock(enabled)
            pushSettingsToServer()
        }
    }

    fun updateNotifyDailySummary(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateNotifyDailySummary(enabled)
            pushSettingsToServer()
        }
    }

    fun updateNotifySyncComplete(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateNotifySyncComplete(enabled)
            pushSettingsToServer()
        }
    }

    fun updateBiometricLock(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateBiometricLock(enabled)
            pushSettingsToServer()
        }
    }

    private fun pushSettingsToServer() {
        viewModelScope.launch {
            val current = _settings.value
            val dto = SettingsDto(
                language = current.language,
                currency = current.currency,
                lowStockThreshold = current.lowStockThreshold,
                notifyLowStock = current.notifyLowStock,
                notifyDailySummary = current.notifyDailySummary,
                notifySyncComplete = current.notifySyncComplete,
                biometricLock = current.biometricLock
            )
            val result = repository.pushSettings(dto)
            result.onSuccess { _syncResult.value = "Settings synced" }
                .onFailure { _syncResult.value = "Settings saved locally" }
        }
    }

    fun clearSyncResult() { _syncResult.value = null }

    fun signOut() {
        repository.logout()
        Log.d(TAG, "User signed out")
    }
}

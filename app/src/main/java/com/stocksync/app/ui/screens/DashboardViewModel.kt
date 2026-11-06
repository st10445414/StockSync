package com.stocksync.app.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stocksync.app.data.db.ProductDao
import com.stocksync.app.data.db.SaleDao
import com.stocksync.app.data.db.PendingSyncDao
import com.stocksync.app.data.preferences.SettingsManager
import com.stocksync.app.data.repository.StockSyncRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class DashboardViewModel(
    private val repository: StockSyncRepository,
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
    private val pendingSyncDao: PendingSyncDao,
    private val settingsManager: SettingsManager
) : ViewModel() {

    companion object {
        private const val TAG = "DashboardViewModel"
    }

    private val _todaySalesTotal = MutableStateFlow(0.0)
    val todaySalesTotal: StateFlow<Double> = _todaySalesTotal.asStateFlow()

    private val _transactionCount = MutableStateFlow(0)
    val transactionCount: StateFlow<Int> = _transactionCount.asStateFlow()

    private val _lowStockCount = MutableStateFlow(0)
    val lowStockCount: StateFlow<Int> = _lowStockCount.asStateFlow()

    private val _productCount = MutableStateFlow(0)
    val productCount: StateFlow<Int> = _productCount.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount: StateFlow<Int> = _pendingCount.asStateFlow()

    private val _topProducts = MutableStateFlow<List<Pair<String, Int>>>(emptyList())
    val topProducts: StateFlow<List<Pair<String, Int>>> = _topProducts.asStateFlow()

    private val _lowStockThreshold = MutableStateFlow(5)
    val lowStockThreshold: StateFlow<Int> = _lowStockThreshold.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis

        viewModelScope.launch {
            saleDao.getTodaySalesTotal(startOfDay).collect { _todaySalesTotal.value = it }
        }
        viewModelScope.launch {
            saleDao.getTodayTransactionCount(startOfDay).collect { _transactionCount.value = it }
        }
        viewModelScope.launch {
            settingsManager.settings
                .flatMapLatest { settings ->
                    _lowStockThreshold.value = settings.lowStockThreshold
                    productDao.getLowStockProducts(settings.lowStockThreshold)
                }
                .collect { products ->
                    _lowStockCount.value = products.size
                }
        }
        viewModelScope.launch {
            productDao.getProductCount().collect { _productCount.value = it }
        }
        viewModelScope.launch {
            pendingSyncDao.getPendingCount().collect { _pendingCount.value = it }
        }
        viewModelScope.launch {
            saleDao.getTopSellingProducts(startOfDay).collect { results ->
                val top = results.map { r ->
                    val product = productDao.getProductById(r.productId)
                    (product?.name ?: "Unknown") to r.totalQty
                }
                _topProducts.value = top
            }
        }
        Log.d(TAG, "Dashboard loaded")
    }
}

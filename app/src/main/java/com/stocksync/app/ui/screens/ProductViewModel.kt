package com.stocksync.app.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stocksync.app.data.db.ProductDao
import com.stocksync.app.data.db.ProductEntity
import com.stocksync.app.data.db.SyncStatus
import com.stocksync.app.data.repository.StockSyncRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductViewModel(
    private val repository: StockSyncRepository,
    private val productDao: ProductDao
) : ViewModel() {

    companion object {
        private const val TAG = "ProductViewModel"
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterCategory = MutableStateFlow<String?>(null)
    val filterCategory: StateFlow<String?> = _filterCategory.asStateFlow()

    val products: StateFlow<List<ProductEntity>> = combine(
        productDao.getAllProducts(),
        _searchQuery,
        _filterCategory
    ) { allProducts, query, category ->
        allProducts.filter { product ->
            (query.isEmpty() || product.name.contains(query, ignoreCase = true)) &&
            (category == null || product.category == category)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilterCategory(category: String?) {
        _filterCategory.value = category
    }

    fun addProduct(name: String, price: Double, quantity: Int, category: String?, barcode: String?, photoBlob: ByteArray? = null) {
        viewModelScope.launch {
            repository.addProduct(name, price, quantity, category, barcode, photoBlob)
            Log.d(TAG, "Product added: $name")
        }
    }

    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            repository.updateProduct(product)
            Log.d(TAG, "Product updated: ${product.name}")
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            Log.d(TAG, "Product deleted: $productId")
        }
    }

    fun adjustStock(productId: String, quantityChange: Int, note: String?) {
        viewModelScope.launch {
            repository.adjustStock(productId, quantityChange, note)
            Log.d(TAG, "Stock adjusted for $productId")
        }
    }

    fun logSale(productId: String, quantity: Int, salePrice: Double, paymentMethod: String) {
        viewModelScope.launch {
            repository.logSale(productId, quantity, salePrice, paymentMethod)
            Log.d(TAG, "Sale logged for $productId")
        }
    }
}

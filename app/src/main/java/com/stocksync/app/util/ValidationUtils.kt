package com.stocksync.app.util

import android.util.Log

object ValidationUtils {

    private const val TAG = "ValidationUtils"

    fun validateProductName(name: String): Boolean {
        val valid = name.isNotBlank() && name.length <= 100
        if (!valid) Log.d(TAG, "Product name validation failed: '$name'")
        return valid
    }

    fun validatePrice(price: String): Boolean {
        val doubleValue = price.toDoubleOrNull()
        val valid = doubleValue != null && doubleValue >= 0
        if (!valid) Log.d(TAG, "Price validation failed: '$price'")
        return valid
    }

    fun validateQuantity(quantity: String): Boolean {
        val intValue = quantity.toIntOrNull()
        val valid = intValue != null && intValue >= 0
        if (!valid) Log.d(TAG, "Quantity validation failed: '$quantity'")
        return valid
    }

    fun validateBarcode(barcode: String?): Boolean {
        if (barcode.isNullOrBlank()) return true
        val valid = barcode.length <= 20
        if (!valid) Log.d(TAG, "Barcode validation failed: '$barcode'")
        return valid
    }

    fun validateCategory(category: String?): Boolean {
        if (category.isNullOrBlank()) return true
        val valid = category.length <= 50
        if (!valid) Log.d(TAG, "Category validation failed: '$category'")
        return valid
    }
}

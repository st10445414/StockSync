package com.stocksync.app

import com.stocksync.app.util.ValidationUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun validateProductName_validName_returnsTrue() {
        assertTrue(ValidationUtils.validateProductName("Coca-Cola 500ml"))
    }

    @Test
    fun validateProductName_emptyName_returnsFalse() {
        assertFalse(ValidationUtils.validateProductName(""))
    }

    @Test
    fun validateProductName_blankName_returnsFalse() {
        assertFalse(ValidationUtils.validateProductName("   "))
    }

    @Test
    fun validateProductName_tooLongName_returnsFalse() {
        val longName = "a".repeat(101)
        assertFalse(ValidationUtils.validateProductName(longName))
    }

    @Test
    fun validatePrice_validPrice_returnsTrue() {
        assertTrue(ValidationUtils.validatePrice("8.50"))
    }

    @Test
    fun validatePrice_zeroPrice_returnsTrue() {
        assertTrue(ValidationUtils.validatePrice("0"))
    }

    @Test
    fun validatePrice_negativePrice_returnsFalse() {
        assertFalse(ValidationUtils.validatePrice("-5.0"))
    }

    @Test
    fun validatePrice_nonNumericPrice_returnsFalse() {
        assertFalse(ValidationUtils.validatePrice("abc"))
    }

    @Test
    fun validatePrice_emptyPrice_returnsFalse() {
        assertFalse(ValidationUtils.validatePrice(""))
    }

    @Test
    fun validateQuantity_validQuantity_returnsTrue() {
        assertTrue(ValidationUtils.validateQuantity("24"))
    }

    @Test
    fun validateQuantity_zeroQuantity_returnsTrue() {
        assertTrue(ValidationUtils.validateQuantity("0"))
    }

    @Test
    fun validateQuantity_negativeQuantity_returnsFalse() {
        assertFalse(ValidationUtils.validateQuantity("-1"))
    }

    @Test
    fun validateQuantity_nonNumericQuantity_returnsFalse() {
        assertFalse(ValidationUtils.validateQuantity("abc"))
    }

    @Test
    fun validateBarcode_nullBarcode_returnsTrue() {
        assertTrue(ValidationUtils.validateBarcode(null))
    }

    @Test
    fun validateBarcode_emptyBarcode_returnsTrue() {
        assertTrue(ValidationUtils.validateBarcode(""))
    }

    @Test
    fun validateBarcode_validBarcode_returnsTrue() {
        assertTrue(ValidationUtils.validateBarcode("6001234567890"))
    }

    @Test
    fun validateBarcode_tooLongBarcode_returnsFalse() {
        val longBarcode = "1".repeat(21)
        assertFalse(ValidationUtils.validateBarcode(longBarcode))
    }

    @Test
    fun validateCategory_nullCategory_returnsTrue() {
        assertTrue(ValidationUtils.validateCategory(null))
    }

    @Test
    fun validateCategory_validCategory_returnsTrue() {
        assertTrue(ValidationUtils.validateCategory("Beverages"))
    }

    @Test
    fun validateCategory_tooLongCategory_returnsFalse() {
        val longCategory = "a".repeat(51)
        assertFalse(ValidationUtils.validateCategory(longCategory))
    }
}

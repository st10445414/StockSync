package com.stocksync.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class TokenManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveJwtToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getJwtToken(): String? = prefs.getString("jwt_token", null)

    fun saveUserId(userId: String) {
        prefs.edit().putString("user_id", userId).apply()
    }

    fun getUserId(): String? = prefs.getString("user_id", null)

    fun saveGoogleIdToken(token: String) {
        prefs.edit().putString("google_id_token", token).apply()
    }

    fun getGoogleIdToken(): String? = prefs.getString("google_id_token", null)

    fun saveTokenExpiry(expiresAt: String) {
        prefs.edit().putString("token_expiry", expiresAt).apply()
    }

    fun getTokenExpiry(): String? = prefs.getString("token_expiry", null)

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getJwtToken() != null && getUserId() != null
}

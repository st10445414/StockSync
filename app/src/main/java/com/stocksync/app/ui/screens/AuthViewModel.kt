package com.stocksync.app.ui.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stocksync.app.data.repository.StockSyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: StockSyncRepository) : ViewModel() {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun loginWithGoogle(googleIdToken: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            val result = repository.login(googleIdToken)
            result.onSuccess {
                Log.d(TAG, "Login successful")
                _loginState.value = LoginState.Success
            }.onFailure { e ->
                Log.e(TAG, "Login failed", e)
                _loginState.value = LoginState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun showError(message: String) {
        _loginState.value = LoginState.Error(message)
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}

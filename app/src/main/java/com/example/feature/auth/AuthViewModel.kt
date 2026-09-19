package com.example.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.repository.ThinkTankRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: ThinkTankRepository) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _authSuccess = MutableStateFlow(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    private val _forgotSuccess = MutableStateFlow(false)
    val forgotSuccess: StateFlow<Boolean> = _forgotSuccess.asStateFlow()

    fun clearState() {
        _error.value = null
        _authSuccess.value = false
        _forgotSuccess.value = false
        _loading.value = false
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _error.value = "All fields are required"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = repository.login(email, password)
            _loading.value = false
            if (result.success) {
                _authSuccess.value = true
            } else {
                _error.value = result.error?.message ?: "Login failed"
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _error.value = "All fields are required"
            return
        }
        if (password.length < 6) {
            _error.value = "Password must be at least 6 characters"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = repository.register(name, email, password)
            _loading.value = false
            if (result.success) {
                _authSuccess.value = true
            } else {
                _error.value = result.error?.message ?: "Registration failed"
            }
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _error.value = "Email is required"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = repository.forgotPassword(email)
            _loading.value = false
            if (result.success) {
                _forgotSuccess.value = true
            } else {
                _error.value = result.error?.message ?: "Failed to request password reset"
            }
        }
    }
}

package com.example.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.UserDto
import com.example.core.repository.ThinkTankRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: ThinkTankRepository) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _user = MutableStateFlow<UserDto?>(null)
    val user: StateFlow<UserDto?> = _user.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _passwordSuccess = MutableStateFlow(false)
    val passwordSuccess: StateFlow<Boolean> = _passwordSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.getProfile()
            _loading.value = false
            if (result.success) {
                _user.value = result.data
            }
        }
    }

    fun updateProfile(name: String, avatarUrl: String) {
        if (name.isBlank()) {
            _error.value = "Name cannot be empty"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = repository.updateProfile(name, avatarUrl)
            _loading.value = false
            if (result.success) {
                _user.value = result.data
                _updateSuccess.value = true
            } else {
                _error.value = result.error?.message ?: "Failed to update profile"
            }
        }
    }

    fun changePassword(current: String, new: String) {
        if (current.isBlank() || new.isBlank()) {
            _error.value = "All fields are required"
            return
        }
        if (new.length < 6) {
            _error.value = "New password must be at least 6 characters"
            return
        }
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            val result = repository.changePassword(current, new)
            _loading.value = false
            if (result.success) {
                _passwordSuccess.value = true
            } else {
                _error.value = result.error?.message ?: "Failed to change password"
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onComplete()
        }
    }

    fun clearState() {
        _updateSuccess.value = false
        _passwordSuccess.value = false
        _error.value = null
    }
}

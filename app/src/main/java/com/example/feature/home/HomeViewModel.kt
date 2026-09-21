package com.example.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.DashboardDto
import com.example.core.network.NotificationDto
import com.example.core.repository.ThinkTankRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: ThinkTankRepository) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _dashboard = MutableStateFlow<DashboardDto?>(null)
    val dashboard: StateFlow<DashboardDto?> = _dashboard.asStateFlow()

    val userName: StateFlow<String> = repository.sessionManager.userName
        .map { it ?: "Academic Thinker" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Academic Thinker")

    val userAvatar: StateFlow<String> = repository.sessionManager.userAvatar
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val unreadNotificationsCount: StateFlow<Int> = repository.unreadNotificationsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val notifications = repository.localNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadDashboardData()
        syncData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.getDashboard()
            _loading.value = false
            if (result.success) {
                _dashboard.value = result.data
            }
        }
    }

    private fun syncData() {
        viewModelScope.launch {
            repository.syncNotifications()
            repository.syncBookmarks()
        }
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }
}

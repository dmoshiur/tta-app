package com.example.feature.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.ExploreDto
import com.example.core.repository.ThinkTankRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExploreViewModel(private val repository: ThinkTankRepository) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _exploreItems = MutableStateFlow<List<ExploreDto>>(emptyList())
    val exploreItems: StateFlow<List<ExploreDto>> = _exploreItems.asStateFlow()

    private val _selectedItem = MutableStateFlow<ExploreDto?>(null)
    val selectedItem: StateFlow<ExploreDto?> = _selectedItem.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedType = MutableStateFlow("ARTICLE") // ARTICLE, BOOK, KNOWLEDGE, WORLD, HUMANITY, SOCIETY
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    val bookmarks = repository.localBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadExploreItems()
    }

    fun loadExploreItems() {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.getExplore(
                type = _selectedType.value,
                search = _searchQuery.value.takeIf { it.isNotBlank() }
            )
            _loading.value = false
            if (result.success && result.data != null) {
                _exploreItems.value = result.data
            }
        }
    }

    fun selectType(type: String) {
        _selectedType.value = type
        _searchQuery.value = ""
        loadExploreItems()
    }

    fun searchExplore(query: String) {
        _searchQuery.value = query
        loadExploreItems()
    }

    fun loadExploreDetail(itemId: String) {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.getExplore(type = _selectedType.value)
            _loading.value = false
            if (result.success && result.data != null) {
                _selectedItem.value = result.data.find { it.id == itemId }
            }
        }
    }

    fun toggleBookmark(item: ExploreDto) {
        viewModelScope.launch {
            repository.toggleLocalBookmark(
                id = item.id,
                type = item.type,
                title = item.title,
                subtitle = item.author,
                imageUrl = item.coverUrl ?: ""
            )
            // Refresh detailed item state to keep local bookmark synchronised
            _selectedItem.value = _selectedItem.value?.let {
                val isCurrentlyBookmarked = bookmarks.value.any { b -> b.id == item.id }
                it.copy(bookmarked = !isCurrentlyBookmarked)
            }
        }
    }
}

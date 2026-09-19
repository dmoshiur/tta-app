package com.example.feature.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.CourseDto
import com.example.core.repository.ThinkTankRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CoursesViewModel(private val repository: ThinkTankRepository) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _courses = MutableStateFlow<List<CourseDto>>(emptyList())
    val courses: StateFlow<List<CourseDto>> = _courses.asStateFlow()

    private val _courseDetails = MutableStateFlow<CourseDto?>(null)
    val courseDetails: StateFlow<CourseDto?> = _courseDetails.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val bookmarks = repository.localBookmarks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadCourses()
    }

    fun loadCourses() {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.getCourses(
                search = _searchQuery.value.takeIf { it.isNotBlank() },
                category = _selectedCategory.value
            )
            _loading.value = false
            if (result.success && result.data != null) {
                _courses.value = result.data
            }
        }
    }

    fun searchCourses(query: String) {
        _searchQuery.value = query
        loadCourses()
    }

    fun filterCategory(category: String?) {
        _selectedCategory.value = category
        loadCourses()
    }

    fun loadCourseDetails(courseId: String) {
        viewModelScope.launch {
            _loading.value = true
            _courseDetails.value = null
            val result = repository.getCourseDetails(courseId)
            _loading.value = false
            if (result.success) {
                _courseDetails.value = result.data
            }
        }
    }

    fun enrollInCourse(courseId: String) {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.enrollCourse(courseId)
            if (result.success) {
                loadCourseDetails(courseId)
                loadCourses()
            }
            _loading.value = false
        }
    }

    fun toggleBookmark(id: String, title: String, category: String) {
        viewModelScope.launch {
            repository.toggleLocalBookmark(
                id = id,
                type = "COURSE",
                title = title,
                subtitle = category,
                imageUrl = ""
            )
        }
    }
}

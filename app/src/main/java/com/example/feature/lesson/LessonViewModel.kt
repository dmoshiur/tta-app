package com.example.feature.lesson

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.LessonDto
import com.example.core.repository.ThinkTankRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LessonViewModel(private val repository: ThinkTankRepository) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _lesson = MutableStateFlow<LessonDto?>(null)
    val lesson: StateFlow<LessonDto?> = _lesson.asStateFlow()

    fun loadLesson(lessonId: String) {
        viewModelScope.launch {
            _loading.value = true
            val result = repository.getLessonDetails(lessonId)
            _loading.value = false
            if (result.success) {
                _lesson.value = result.data
            }
        }
    }

    fun completeLesson(lessonId: String) {
        viewModelScope.launch {
            val result = repository.completeLesson(lessonId)
            if (result.success) {
                loadLesson(lessonId)
            }
        }
    }
}

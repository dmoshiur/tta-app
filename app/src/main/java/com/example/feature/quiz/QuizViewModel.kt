package com.example.feature.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.network.QuizDto
import com.example.core.network.QuizResponseDto
import com.example.core.repository.ThinkTankRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class QuizViewModel(private val repository: ThinkTankRepository) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _quizzes = MutableStateFlow<List<QuizDto>>(emptyList())
    val quizzes: StateFlow<List<QuizDto>> = _quizzes.asStateFlow()

    private val _quizzesSearchQuery = MutableStateFlow("")
    val quizzesSearchQuery: StateFlow<String> = _quizzesSearchQuery.asStateFlow()

    private val _quiz = MutableStateFlow<QuizDto?>(null)
    val quiz: StateFlow<QuizDto?> = _quiz.asStateFlow()

    private val _selectedAnswers = MutableStateFlow<Map<String, String>>(emptyMap())
    val selectedAnswers: StateFlow<Map<String, String>> = _selectedAnswers.asStateFlow()

    private val _timeRemainingSeconds = MutableStateFlow(0)
    val timeRemainingSeconds: StateFlow<Int> = _timeRemainingSeconds.asStateFlow()

    private val _quizResult = MutableStateFlow<QuizResponseDto?>(null)
    val quizResult: StateFlow<QuizResponseDto?> = _quizResult.asStateFlow()

    private var timerJob: Job? = null

    init {
        loadQuizzes()
    }

    fun loadQuizzes() {
        viewModelScope.launch {
            _loading.value = true
            val query = _quizzesSearchQuery.value.takeIf { it.isNotBlank() }
            val result = repository.getQuizzes(search = query)
            _loading.value = false
            if (result.success && result.data != null) {
                _quizzes.value = result.data
            }
        }
    }

    fun searchQuizzes(query: String) {
        _quizzesSearchQuery.value = query
        loadQuizzes()
    }

    fun loadQuiz(quizId: String) {
        viewModelScope.launch {
            _loading.value = true
            _quizResult.value = null
            _selectedAnswers.value = emptyMap()
            val result = repository.getQuiz(quizId)
            _loading.value = false
            if (result.success && result.data != null) {
                _quiz.value = result.data
                startTimer(result.data.durationMinutes * 60)
            }
        }
    }

    private fun startTimer(durationSeconds: Int) {
        timerJob?.cancel()
        _timeRemainingSeconds.value = durationSeconds
        timerJob = viewModelScope.launch {
            while (_timeRemainingSeconds.value > 0) {
                delay(1000)
                _timeRemainingSeconds.value -= 1
            }
            // Timer expired, submit automatically
            submitQuiz()
        }
    }

    fun selectAnswer(questionId: String, option: String) {
        val current = _selectedAnswers.value.toMutableMap()
        current[questionId] = option
        _selectedAnswers.value = current
    }

    fun submitQuiz() {
        timerJob?.cancel()
        val currentQuiz = _quiz.value ?: return
        viewModelScope.launch {
            _loading.value = true
            val result = repository.submitQuiz(currentQuiz.id, _selectedAnswers.value)
            _loading.value = false
            if (result.success) {
                _quizResult.value = result.data
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

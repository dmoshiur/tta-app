package com.example.core.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.database.AppDatabase
import com.example.core.datastore.SessionManager
import com.example.core.network.RetrofitClient
import com.example.core.repository.ThinkTankRepository
import com.example.feature.auth.AuthViewModel
import com.example.feature.courses.CoursesViewModel
import com.example.feature.explore.ExploreViewModel
import com.example.feature.home.HomeViewModel
import com.example.feature.lesson.LessonViewModel
import com.example.feature.profile.ProfileViewModel
import com.example.feature.quiz.QuizViewModel

class ViewModelFactory(private val repository: ThinkTankRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(repository) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(CoursesViewModel::class.java) -> {
                CoursesViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ExploreViewModel::class.java) -> {
                ExploreViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LessonViewModel::class.java) -> {
                LessonViewModel(repository) as T
            }
            modelClass.isAssignableFrom(QuizViewModel::class.java) -> {
                QuizViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun createFactory(context: Context): ViewModelProvider.Factory {
            val appCtx = context.applicationContext
            val apiService = RetrofitClient.getInstance(appCtx).apiService
            val appDao = AppDatabase.getInstance(appCtx).appDao()
            val sessionManager = SessionManager.getInstance(appCtx)
            val moshi = RetrofitClient.getInstance(appCtx).moshi
            val repository = ThinkTankRepository(apiService, appDao, sessionManager, moshi)
            return ViewModelFactory(repository)
        }
    }
}

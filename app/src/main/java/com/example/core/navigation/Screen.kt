package com.example.core.navigation

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    
    object Home : Screen("home")
    object Courses : Screen("courses")
    object Quizzes : Screen("quizzes")
    object Search : Screen("search")
    object Explore : Screen("explore")
    object MyLearning : Screen("my_learning")
    object Profile : Screen("profile")
    
    object CourseDetails : Screen("course_details/{courseId}") {
        fun createRoute(courseId: String) = "course_details/$courseId"
    }
    
    object Lesson : Screen("lesson/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson/$lessonId"
    }
    
    object Quiz : Screen("quiz/{quizId}") {
        fun createRoute(quizId: String) = "quiz/$quizId"
    }
    
    object Bookmarks : Screen("bookmarks")
    object Notifications : Screen("notifications")
    object Settings : Screen("settings")
}

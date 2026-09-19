package com.example.core.network

import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/register")
    suspend fun register(
        @Body body: Map<String, String>
    ): Envelope<AuthResponseDto>

    @POST("auth/login")
    suspend fun login(
        @Body body: Map<String, String>
    ): Envelope<AuthResponseDto>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(
        @Body body: Map<String, String>
    ): Envelope<Unit>

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body body: Map<String, String>
    ): Envelope<Unit>

    // Profile & Users
    @GET("users/me")
    suspend fun getProfile(): Envelope<UserDto>

    @PUT("users/me")
    suspend fun updateProfile(
        @Body body: Map<String, String>
    ): Envelope<UserDto>

    @POST("users/change-password")
    suspend fun changePassword(
        @Body body: Map<String, String>
    ): Envelope<Unit>

    // Dashboard
    @GET("dashboard")
    suspend fun getDashboard(): Envelope<DashboardDto>

    // Courses
    @GET("courses")
    suspend fun getCourses(
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("page") page: Int? = null
    ): Envelope<List<CourseDto>>

    @GET("courses/{id}")
    suspend fun getCourseDetails(
        @Path("id") id: String
    ): Envelope<CourseDto>

    @POST("courses/{id}/enroll")
    suspend fun enrollInCourse(
        @Path("id") id: String
    ): Envelope<Unit>

    // Lessons
    @GET("lessons/{id}")
    suspend fun getLessonDetails(
        @Path("id") id: String
    ): Envelope<LessonDto>

    @POST("lessons/{id}/complete")
    suspend fun completeLesson(
        @Path("id") id: String
    ): Envelope<Unit>

    // Quizzes
    @GET("quizzes/{id}")
    suspend fun getQuiz(
        @Path("id") id: String
    ): Envelope<QuizDto>

    @POST("quizzes/{id}/submit")
    suspend fun submitQuiz(
        @Path("id") id: String,
        @Body body: QuizSubmissionDto
    ): Envelope<QuizResponseDto>

    // Explore Content
    @GET("explore")
    suspend fun getExplore(
        @Query("type") type: String? = null,
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): Envelope<List<ExploreDto>>

    // Bookmarks
    @GET("bookmarks")
    suspend fun getBookmarks(): Envelope<List<ExploreDto>>

    @POST("bookmarks/{id}")
    suspend fun toggleBookmark(
        @Path("id") id: String
    ): Envelope<Unit>

    // Notifications
    @GET("notifications")
    suspend fun getNotifications(): Envelope<List<NotificationDto>>

    @POST("notifications/{id}/read")
    suspend fun readNotification(
        @Path("id") id: String
    ): Envelope<Unit>
}

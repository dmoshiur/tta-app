package com.example.core.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Envelope<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: T? = null,
    @Json(name = "error") val error: ErrorDetail? = null
)

@JsonClass(generateAdapter = true)
data class ErrorDetail(
    @Json(name = "code") val code: String? = null,
    @Json(name = "message") val message: String? = null
)

// Auth DTOs
@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "avatar_url") val avatarUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponseDto(
    @Json(name = "token") val token: String,
    @Json(name = "user") val user: UserDto
)

// Dashboard DTOs
@JsonClass(generateAdapter = true)
data class DashboardProgressDto(
    @Json(name = "course_percentage") val coursePercentage: Int,
    @Json(name = "completed_lessons") val completedLessons: Int,
    @Json(name = "remaining_lessons") val remainingLessons: Int,
    @Json(name = "completed_courses") val completedCourses: Int
)

@JsonClass(generateAdapter = true)
data class DashboardDto(
    @Json(name = "learning_progress") val learningProgress: DashboardProgressDto,
    @Json(name = "recent_activity") val recentActivity: List<String> = emptyList(),
    @Json(name = "recommendations") val recommendations: List<String> = emptyList()
)

// Course DTOs
@JsonClass(generateAdapter = true)
data class CourseDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "instructor") val instructor: String,
    @Json(name = "category") val category: String,
    @Json(name = "difficulty") val difficulty: String,
    @Json(name = "duration") val duration: String,
    @Json(name = "enrolled") val enrolled: Boolean = false,
    @Json(name = "progress") val progress: Int = 0,
    @Json(name = "lesson_count") val lessonCount: Int = 0,
    @Json(name = "modules") val modules: List<ModuleDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ModuleDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "lessons") val lessons: List<LessonMinimalDto> = emptyList(),
    @Json(name = "quizzes") val quizzes: List<QuizMinimalDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LessonMinimalDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "duration") val duration: String,
    @Json(name = "completed") val completed: Boolean = false
)

@JsonClass(generateAdapter = true)
data class QuizMinimalDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "duration_minutes") val durationMinutes: Int = 10
)

// Lesson Full DTO
@JsonClass(generateAdapter = true)
data class LessonDto(
    @Json(name = "id") val id: String,
    @Json(name = "course_id") val courseId: String,
    @Json(name = "title") val title: String,
    @Json(name = "content") val content: String,
    @Json(name = "completed") val completed: Boolean = false,
    @Json(name = "resources") val resources: List<ResourceDto> = emptyList(),
    @Json(name = "previous_lesson_id") val previousLessonId: String? = null,
    @Json(name = "next_lesson_id") val nextLessonId: String? = null
)

@JsonClass(generateAdapter = true)
data class ResourceDto(
    @Json(name = "title") val title: String,
    @Json(name = "url") val url: String
)

// Quiz Full DTOs
@JsonClass(generateAdapter = true)
data class QuizDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "duration_minutes") val durationMinutes: Int = 10,
    @Json(name = "questions") val questions: List<QuestionDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuestionDto(
    @Json(name = "id") val id: String,
    @Json(name = "text") val text: String,
    @Json(name = "options") val options: List<String>
)

@JsonClass(generateAdapter = true)
data class QuizSubmissionDto(
    @Json(name = "answers") val answers: Map<String, String>
)

@JsonClass(generateAdapter = true)
data class QuizResponseDto(
    @Json(name = "score_percentage") val scorePercentage: Int,
    @Json(name = "total_questions") val totalQuestions: Int,
    @Json(name = "correct_answers") val correctAnswers: Int,
    @Json(name = "review") val review: List<QuizReviewDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class QuizReviewDto(
    @Json(name = "question_id") val questionId: String,
    @Json(name = "question_text") val questionText: String,
    @Json(name = "user_answer") val userAnswer: String,
    @Json(name = "correct_answer") val correctAnswer: String,
    @Json(name = "is_correct") val isCorrect: Boolean,
    @Json(name = "explanation") val explanation: String? = null
)

// Explore Content DTOs
@JsonClass(generateAdapter = true)
data class ExploreDto(
    @Json(name = "id") val id: String,
    @Json(name = "type") val type: String, // ARTICLE, BOOK, KNOWLEDGE, WORLD, HUMANITY, SOCIETY
    @Json(name = "title") val title: String,
    @Json(name = "author") val author: String,
    @Json(name = "date") val date: String,
    @Json(name = "reading_time") val readingTime: String,
    @Json(name = "cover_url") val coverUrl: String? = null,
    @Json(name = "summary") val summary: String,
    @Json(name = "content") val content: String,
    @Json(name = "sources") val sources: List<String> = emptyList(),
    @Json(name = "bookmarked") val bookmarked: Boolean = false,
    @Json(name = "details") val details: ExploreDetailsDto? = null
)

@JsonClass(generateAdapter = true)
data class ExploreDetailsDto(
    @Json(name = "key_ideas") val keyIdeas: List<String> = emptyList(),
    @Json(name = "important_lessons") val importantLessons: List<String> = emptyList()
)

// Notification DTO
@JsonClass(generateAdapter = true)
data class NotificationDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String,
    @Json(name = "read") val read: Boolean = false,
    @Json(name = "timestamp") val timestamp: Long
)

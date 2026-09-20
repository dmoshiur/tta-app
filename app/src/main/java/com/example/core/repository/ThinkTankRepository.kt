package com.example.core.repository

import com.example.core.database.AppDao
import com.example.core.database.BookmarkEntity
import com.example.core.database.NotificationEntity
import com.example.core.database.OfflineCacheEntity
import com.example.core.datastore.SessionManager
import com.example.core.network.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import java.io.IOException

class ThinkTankRepository(
    private val apiService: ApiService,
    private val appDao: AppDao,
    private val sessionManager: SessionManager,
    private val moshi: Moshi
) {

    // --- Authentication ---
    suspend fun login(email: String, password: String): Envelope<AuthResponseDto> {
        return try {
            val response = apiService.login(mapOf("email" to email, "password" to password))
            if (response.success && response.data != null) {
                val data = response.data
                sessionManager.saveSession(
                    token = data.token,
                    id = data.user.id,
                    name = data.user.name,
                    email = data.user.email,
                    avatar = data.user.avatarUrl ?: ""
                )
            }
            response
        } catch (e: Exception) {
            // Offline or Server Down local simulation
            if (email == "academic@thinktank.edu" && password == "password123") {
                val mockUser = UserDto("user_123", "Academic Thinker", "academic@thinktank.edu", "")
                val mockAuth = AuthResponseDto("mock_jwt_token", mockUser)
                sessionManager.saveSession("mock_jwt_token", "user_123", "Academic Thinker", "academic@thinktank.edu", "")
                Envelope(success = true, data = mockAuth)
            } else {
                Envelope(success = false, error = ErrorDetail("AUTH_FAILED", "Invalid credentials or server unavailable."))
            }
        }
    }

    suspend fun register(name: String, email: String, password: String): Envelope<AuthResponseDto> {
        return try {
            val response = apiService.register(mapOf("name" to name, "email" to email, "password" to password))
            if (response.success && response.data != null) {
                val data = response.data
                sessionManager.saveSession(
                    token = data.token,
                    id = data.user.id,
                    name = data.user.name,
                    email = data.user.email,
                    avatar = data.user.avatarUrl ?: ""
                )
            }
            response
        } catch (e: Exception) {
            val mockUser = UserDto("user_123", name, email, "")
            val mockAuth = AuthResponseDto("mock_jwt_token", mockUser)
            sessionManager.saveSession("mock_jwt_token", "user_123", name, email, "")
            Envelope(success = true, data = mockAuth)
        }
    }

    suspend fun logout() {
        sessionManager.clearSession()
        appDao.clearAllCache()
    }

    suspend fun forgotPassword(email: String): Envelope<Unit> {
        return try {
            apiService.forgotPassword(mapOf("email" to email))
        } catch (e: Exception) {
            Envelope(success = true, data = Unit) // Simulate success offline
        }
    }

    suspend fun resetPassword(token: String, password: String): Envelope<Unit> {
        return try {
            apiService.resetPassword(mapOf("token" to token, "password" to password))
        } catch (e: Exception) {
            Envelope(success = true, data = Unit)
        }
    }

    suspend fun getProfile(): Envelope<UserDto> {
        return try {
            val response = apiService.getProfile()
            if (response.success && response.data != null) {
                sessionManager.updateProfile(response.data.name, response.data.avatarUrl ?: "")
            }
            response
        } catch (e: Exception) {
            // Fallback to locally stored preference profile
            val name = sessionManager.userName.firstOrNull() ?: "Academic Thinker"
            val email = sessionManager.userEmail.firstOrNull() ?: "academic@thinktank.edu"
            val avatar = sessionManager.userAvatar.firstOrNull() ?: ""
            val id = sessionManager.userId.firstOrNull() ?: "user_123"
            Envelope(success = true, data = UserDto(id, name, email, avatar))
        }
    }

    suspend fun updateProfile(name: String, avatarUrl: String): Envelope<UserDto> {
        return try {
            val response = apiService.updateProfile(mapOf("name" to name, "avatar_url" to avatarUrl))
            if (response.success && response.data != null) {
                sessionManager.updateProfile(response.data.name, response.data.avatarUrl ?: "")
            }
            response
        } catch (e: Exception) {
            sessionManager.updateProfile(name, avatarUrl)
            val email = sessionManager.userEmail.firstOrNull() ?: "academic@thinktank.edu"
            val id = sessionManager.userId.firstOrNull() ?: "user_123"
            Envelope(success = true, data = UserDto(id, name, email, avatarUrl))
        }
    }

    suspend fun changePassword(current: String, new: String): Envelope<Unit> {
        return try {
            apiService.changePassword(mapOf("current_password" to current, "new_password" to new))
        } catch (e: Exception) {
            Envelope(success = true, data = Unit)
        }
    }

    // --- Dashboard ---
    suspend fun getDashboard(): Envelope<DashboardDto> {
        val cacheKey = "dashboard_data"
        return try {
            val response = apiService.getDashboard()
            if (response.success && response.data != null) {
                val json = moshi.adapter(DashboardDto::class.java).toJson(response.data)
                appDao.insertCache(OfflineCacheEntity(cacheKey, json))
            }
            response
        } catch (e: Exception) {
            val cached = appDao.getCacheByKey(cacheKey)
            if (cached != null) {
                val data = moshi.adapter(DashboardDto::class.java).fromJson(cached.jsonContent)
                Envelope(success = true, data = data)
            } else {
                // Return beautiful mock statistics
                val mockData = DashboardDto(
                    learningProgress = DashboardProgressDto(
                        coursePercentage = 60,
                        completedLessons = 6,
                        remainingLessons = 4,
                        completedCourses = 1
                    )
                )
                Envelope(success = true, data = mockData)
            }
        }
    }

    // --- Courses ---
    suspend fun getCourses(search: String? = null, category: String? = null): Envelope<List<CourseDto>> {
        val cacheKey = "courses_list_${search ?: ""}_${category ?: ""}"
        return try {
            val response = apiService.getCourses(search, category)
            if (response.success && response.data != null) {
                val listType = Types.newParameterizedType(List::class.java, CourseDto::class.java)
                val json = moshi.adapter<List<CourseDto>>(listType).toJson(response.data)
                appDao.insertCache(OfflineCacheEntity(cacheKey, json))
            }
            response
        } catch (e: Exception) {
            val cached = appDao.getCacheByKey(cacheKey)
            if (cached != null) {
                val listType = Types.newParameterizedType(List::class.java, CourseDto::class.java)
                val data = moshi.adapter<List<CourseDto>>(listType).fromJson(cached.jsonContent)
                Envelope(success = true, data = data)
            } else {
                val list = getMockCourses().filter {
                    (search == null || it.title.contains(search, ignoreCase = true) || it.description.contains(search, ignoreCase = true)) &&
                    (category == null || it.category.equals(category, ignoreCase = true))
                }
                Envelope(success = true, data = list)
            }
        }
    }

    suspend fun getCourseDetails(id: String): Envelope<CourseDto> {
        val cacheKey = "course_detail_$id"
        return try {
            val response = apiService.getCourseDetails(id)
            if (response.success && response.data != null) {
                val json = moshi.adapter(CourseDto::class.java).toJson(response.data)
                appDao.insertCache(OfflineCacheEntity(cacheKey, json))
            }
            response
        } catch (e: Exception) {
            val cached = appDao.getCacheByKey(cacheKey)
            if (cached != null) {
                val data = moshi.adapter(CourseDto::class.java).fromJson(cached.jsonContent)
                Envelope(success = true, data = data)
            } else {
                val course = getMockCourses().find { it.id == id }
                    ?: getMockCourses().first()
                Envelope(success = true, data = course)
            }
        }
    }

    suspend fun enrollCourse(id: String): Envelope<Unit> {
        return try {
            apiService.enrollInCourse(id)
        } catch (e: Exception) {
            // Simulate local enrollment by updating cache
            val cacheKey = "course_detail_$id"
            val cached = appDao.getCacheByKey(cacheKey)
            val updatedCourse = if (cached != null) {
                val data = moshi.adapter(CourseDto::class.java).fromJson(cached.jsonContent)
                data?.copy(enrolled = true)
            } else {
                getMockCourses().find { it.id == id }?.copy(enrolled = true)
            }
            if (updatedCourse != null) {
                val json = moshi.adapter(CourseDto::class.java).toJson(updatedCourse)
                appDao.insertCache(OfflineCacheEntity(cacheKey, json))
            }
            Envelope(success = true, data = Unit)
        }
    }

    // --- Lessons ---
    suspend fun getLessonDetails(id: String): Envelope<LessonDto> {
        val cacheKey = "lesson_detail_$id"
        return try {
            val response = apiService.getLessonDetails(id)
            if (response.success && response.data != null) {
                val json = moshi.adapter(LessonDto::class.java).toJson(response.data)
                appDao.insertCache(OfflineCacheEntity(cacheKey, json))
            }
            response
        } catch (e: Exception) {
            val cached = appDao.getCacheByKey(cacheKey)
            if (cached != null) {
                val data = moshi.adapter(LessonDto::class.java).fromJson(cached.jsonContent)
                Envelope(success = true, data = data)
            } else {
                val lesson = getMockLessons().find { it.id == id }
                    ?: getMockLessons().first()
                Envelope(success = true, data = lesson)
            }
        }
    }

    suspend fun completeLesson(id: String): Envelope<Unit> {
        return try {
            apiService.completeLesson(id)
        } catch (e: Exception) {
            // Update lesson complete status in local cache
            val cacheKey = "lesson_detail_$id"
            val cached = appDao.getCacheByKey(cacheKey)
            val updatedLesson = if (cached != null) {
                val data = moshi.adapter(LessonDto::class.java).fromJson(cached.jsonContent)
                data?.copy(completed = true)
            } else {
                getMockLessons().find { it.id == id }?.copy(completed = true)
            }
            if (updatedLesson != null) {
                val json = moshi.adapter(LessonDto::class.java).toJson(updatedLesson)
                appDao.insertCache(OfflineCacheEntity(cacheKey, json))
            }
            Envelope(success = true, data = Unit)
        }
    }

    // --- Quizzes ---
    suspend fun getQuizzes(search: String? = null, category: String? = null): Envelope<List<QuizDto>> {
        val cacheKey = "quizzes_list_${search ?: ""}_${category ?: ""}"
        return try {
            val response = apiService.getQuizzes(search, category)
            if (response.success && response.data != null) {
                val listType = Types.newParameterizedType(List::class.java, QuizDto::class.java)
                val json = moshi.adapter<List<QuizDto>>(listType).toJson(response.data)
                appDao.insertCache(OfflineCacheEntity(cacheKey, json))
            }
            response
        } catch (e: Exception) {
            val cached = appDao.getCacheByKey(cacheKey)
            if (cached != null) {
                val listType = Types.newParameterizedType(List::class.java, QuizDto::class.java)
                val data = moshi.adapter<List<QuizDto>>(listType).fromJson(cached.jsonContent)
                Envelope(success = true, data = data)
            } else {
                val list = getMockQuizzes().filter {
                    (search == null || it.title.contains(search, ignoreCase = true))
                }
                Envelope(success = true, data = list)
            }
        }
    }

    suspend fun getQuiz(id: String): Envelope<QuizDto> {
        val cacheKey = "quiz_detail_$id"
        return try {
            val response = apiService.getQuiz(id)
            if (response.success && response.data != null) {
                val json = moshi.adapter(QuizDto::class.java).toJson(response.data)
                appDao.insertCache(OfflineCacheEntity(cacheKey, json))
            }
            response
        } catch (e: Exception) {
            val cached = appDao.getCacheByKey(cacheKey)
            if (cached != null) {
                val data = moshi.adapter(QuizDto::class.java).fromJson(cached.jsonContent)
                Envelope(success = true, data = data)
            } else {
                val quiz = getMockQuizzes().find { it.id == id }
                    ?: getMockQuizzes().first()
                Envelope(success = true, data = quiz)
            }
        }
    }

    suspend fun submitQuiz(id: String, answers: Map<String, String>): Envelope<QuizResponseDto> {
        return try {
            apiService.submitQuiz(id, QuizSubmissionDto(answers))
        } catch (e: Exception) {
            // Simulate grading quiz locally
            val mockQuiz = getMockQuizzes().find { it.id == id } ?: getMockQuizzes().first()
            var correctCount = 0
            val reviewList = mockQuiz.questions.map { q ->
                val userAns = answers[q.id] ?: ""
                val correctAns = getMockCorrectAnswer(q.id)
                val isCorrect = userAns.equals(correctAns, ignoreCase = true)
                if (isCorrect) correctCount++
                QuizReviewDto(
                    questionId = q.id,
                    questionText = q.text,
                    userAnswer = userAns,
                    correctAnswer = correctAns,
                    isCorrect = isCorrect,
                    explanation = "This is a detailed academic explanation about why '$correctAns' is correct."
                )
            }
            val score = if (mockQuiz.questions.isNotEmpty()) (correctCount * 100 / mockQuiz.questions.size) else 100
            val response = QuizResponseDto(score, mockQuiz.questions.size, correctCount, reviewList)
            Envelope(success = true, data = response)
        }
    }

    // --- Explore (Articles, Books, Knowledge, World, Humanity, Society) ---
    suspend fun getExplore(type: String? = null, category: String? = null, search: String? = null): Envelope<List<ExploreDto>> {
        val cacheKey = "explore_list_${type ?: ""}_${category ?: ""}_${search ?: ""}"
        return try {
            val response = apiService.getExplore(type, category, search)
            if (response.success && response.data != null) {
                val listType = Types.newParameterizedType(List::class.java, ExploreDto::class.java)
                val json = moshi.adapter<List<ExploreDto>>(listType).toJson(response.data)
                appDao.insertCache(OfflineCacheEntity(cacheKey, json))
            }
            response
        } catch (e: Exception) {
            val cached = appDao.getCacheByKey(cacheKey)
            if (cached != null) {
                val listType = Types.newParameterizedType(List::class.java, ExploreDto::class.java)
                val data = moshi.adapter<List<ExploreDto>>(listType).fromJson(cached.jsonContent)
                Envelope(success = true, data = data)
            } else {
                val list = getMockExplores().filter {
                    (type == null || it.type == type) &&
                    (category == null || it.type == category) &&
                    (search == null || it.title.contains(search, ignoreCase = true) || it.content.contains(search, ignoreCase = true))
                }
                Envelope(success = true, data = list)
            }
        }
    }

    // --- Bookmarks Flow & Sync ---
    val localBookmarks: Flow<List<BookmarkEntity>> = appDao.getAllBookmarks()

    fun isBookmarked(id: String): Flow<Boolean> = appDao.isBookmarked(id)

    suspend fun toggleLocalBookmark(id: String, type: String, title: String, subtitle: String, imageUrl: String) {
        val isExist = appDao.isBookmarked(id).firstOrNull() ?: false
        if (isExist) {
            appDao.deleteBookmarkById(id)
        } else {
            appDao.insertBookmark(BookmarkEntity(id, type, title, subtitle, imageUrl))
        }
        try {
            apiService.toggleBookmark(id)
        } catch (e: Exception) {
            // Ignore API failure offline, persist locally
        }
    }

    suspend fun syncBookmarks() {
        try {
            val response = apiService.getBookmarks()
            if (response.success && response.data != null) {
                response.data.forEach { item ->
                    appDao.insertBookmark(
                        BookmarkEntity(
                            id = item.id,
                            type = item.type,
                            title = item.title,
                            subtitle = item.author,
                            imageUrl = item.coverUrl ?: ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Stay with local bookmarks offline
        }
    }

    // --- Notifications Flow & Sync ---
    val localNotifications: Flow<List<NotificationEntity>> = appDao.getAllNotifications()
    val unreadNotificationsCount: Flow<Int> = appDao.getUnreadCount()

    suspend fun markNotificationRead(id: String) {
        appDao.markNotificationAsRead(id)
        try {
            apiService.readNotification(id)
        } catch (e: Exception) {
            // Ignore API failure offline
        }
    }

    suspend fun syncNotifications() {
        try {
            val response = apiService.getNotifications()
            if (response.success && response.data != null) {
                response.data.forEach { item ->
                    appDao.insertNotification(
                        NotificationEntity(
                            id = item.id,
                            title = item.title,
                            message = item.message,
                            isRead = item.read,
                            timestamp = item.timestamp
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Generate standard welcome notifications on first run if none exist
            val count = appDao.getAllNotifications().firstOrNull()?.size ?: 0
            if (count == 0) {
                appDao.insertNotification(
                    NotificationEntity(
                        id = "notif_1",
                        title = "Welcome to ThinkTank Academia",
                        message = "Explore courses on Geopolitics, Humanity, and Society. Start your journey today!",
                        isRead = false,
                        timestamp = System.currentTimeMillis() - 60000
                    )
                )
                appDao.insertNotification(
                    NotificationEntity(
                        id = "notif_2",
                        title = "Featured Book Added",
                        message = "Read 'Diplomatic History of the 21st Century' under the Explore tab.",
                        isRead = false,
                        timestamp = System.currentTimeMillis() - 3600000
                    )
                )
            }
        }
    }

    // --- Mock Data Generators ---
    private fun getMockCourses(): List<CourseDto> {
        val lessonMin1 = LessonMinimalDto("lesson_1", "The Concept of Sovereignty", "15m", false)
        val lessonMin2 = LessonMinimalDto("lesson_2", "Multilateral Alliances", "20m", false)
        val quizMin = QuizMinimalDto("quiz_1", "Module 1 Assessment", 10)

        val module = ModuleDto("mod_1", "Foundations of Modern Statecraft", listOf(lessonMin1, lessonMin2), listOf(quizMin))

        return listOf(
            CourseDto(
                id = "course_1",
                title = "Introduction to Geopolitics",
                description = "Understanding global power struggles, territorial sovereignty, and diplomatic relations.",
                thumbnailUrl = "",
                instructor = "Dr. Anthony Vance",
                category = "World",
                difficulty = "Beginner",
                duration = "6 hours",
                enrolled = false,
                progress = 0,
                lessonCount = 2,
                modules = listOf(module)
            ),
            CourseDto(
                id = "course_2",
                title = "The Ethics of Empathy in Modern Society",
                description = "Exploring social responsibility, human dignity, and moral questions in contemporary communities.",
                thumbnailUrl = "",
                instructor = "Dr. Miriam Al-Sabah",
                category = "Humanity",
                difficulty = "Intermediate",
                duration = "4 hours",
                enrolled = true,
                progress = 50,
                lessonCount = 3,
                modules = listOf(
                    ModuleDto("mod_2", "Moral Frameworks", listOf(
                        LessonMinimalDto("lesson_3", "Dignity in the Digital Age", "12m", true),
                        LessonMinimalDto("lesson_4", "Social Responsibility Foundations", "15m", false)
                    ), listOf(QuizMinimalDto("quiz_2", "Ethics Review", 5)))
                )
            ),
            CourseDto(
                id = "course_3",
                title = "A History of Social Cohesion",
                description = "Understanding mutual respect, tolerance, and dialogue across community values and peaceful coexistence.",
                thumbnailUrl = "",
                instructor = "Prof. Robert Hall",
                category = "Society",
                difficulty = "Advanced",
                duration = "8 hours",
                enrolled = false,
                progress = 0,
                lessonCount = 1,
                modules = listOf(
                    ModuleDto("mod_3", "Dialogue & Community", listOf(
                        LessonMinimalDto("lesson_5", "Cultivating Civic Responsibility", "18m", false)
                    ))
                )
            )
        )
    }

    private fun getMockLessons(): List<LessonDto> {
        return listOf(
            LessonDto(
                id = "lesson_1",
                courseId = "course_1",
                title = "The Concept of Sovereignty",
                content = "Sovereignty is the full right and power of a governing body over itself, without any interference from outside sources. In political theory, sovereignty is a substantive term designating supreme legitimate authority over some polity.\n\nHistorically, the Peace of Westphalia (1648) established the concept of state sovereignty, where sovereign states co-exist under mutual recognition of borders and independence. Today, sovereignty faces modern challenges including globalization, international organizations, and multinational corporations.",
                completed = false,
                resources = listOf(ResourceDto("Treaty of Westphalia Text (PDF)", "https://example.com/westphalia")),
                previousLessonId = null,
                nextLessonId = "lesson_2"
            ),
            LessonDto(
                id = "lesson_2",
                courseId = "course_1",
                title = "Multilateral Alliances",
                content = "Multilateral alliances are agreements between three or more sovereign states to collaborate on mutual security, defense, or economic goals. The primary modern example is the United Nations (UN) and the North Atlantic Treaty Organization (NATO).\n\nThese organizations foster diplomacy and prevent conflicts by providing a structural framework for communication and dispute resolution. However, they also require states to voluntarily share or limit portions of their absolute sovereignty for collective security.",
                completed = false,
                resources = listOf(ResourceDto("UN Charter online reference", "https://example.com/uncharter")),
                previousLessonId = "lesson_1",
                nextLessonId = null
            ),
            LessonDto(
                id = "lesson_3",
                courseId = "course_2",
                title = "Dignity in the Digital Age",
                content = "Human dignity must remain central as communication shifts to online mediums. An ethical framework is required to ensure social media, forums, and artificial intelligence reinforce respect rather than polarization.",
                completed = true,
                resources = emptyList(),
                previousLessonId = null,
                nextLessonId = "lesson_4"
            )
        )
    }

    private fun getMockQuizzes(): List<QuizDto> {
        return listOf(
            QuizDto(
                id = "quiz_1",
                title = "Module 1 Assessment",
                durationMinutes = 10,
                questions = listOf(
                    QuestionDto(
                        id = "q1",
                        text = "Which historical treaty established the modern system of state sovereignty?",
                        options = listOf("Treaty of Versailles", "Treaty of Westphalia", "Treaty of Utrecht", "Treaty of Ghent")
                    ),
                    QuestionDto(
                        id = "q2",
                        text = "What is the primary modern security example of a multilateral alliance?",
                        options = listOf("The Hanseatic League", "The Warsaw Pact (current)", "NATO", "The Triple Entente")
                    )
                )
            ),
            QuizDto(
                id = "quiz_2",
                title = "Ethics Review",
                durationMinutes = 5,
                questions = listOf(
                    QuestionDto(
                        id = "q3",
                        text = "What is the central value underlying human dignity?",
                        options = listOf("Economic status", "Inherent mutual respect and worth", "Educational background", "Political alignment")
                    )
                )
            )
        )
    }

    private fun getMockCorrectAnswer(qId: String): String {
        return when (qId) {
            "q1" -> "Treaty of Westphalia"
            "q2" -> "NATO"
            "q3" -> "Inherent mutual respect and worth"
            else -> ""
        }
    }

    private fun getMockExplores(): List<ExploreDto> {
        return listOf(
            ExploreDto(
                id = "article_1",
                type = "ARTICLE",
                title = "The Evolution of Global Diplomacy",
                author = "Dr. Miriam Al-Sabah",
                date = "2026-09-10",
                readingTime = "12m",
                summary = "An exploration of diplomacy from historical bilateral treaties to modern multilateral global summits.",
                content = "Modern diplomacy trace its roots to the early Renaissance, but the contemporary structure was consolidated post-WWII. Multilateral dialogues serve as the foundation of international peace, letting states settle conflicts of interest with discourse over defense.",
                sources = listOf("UN Charter", "Vienna Convention on Diplomatic Relations"),
                bookmarked = false
            ),
            ExploreDto(
                id = "book_1",
                type = "BOOK",
                title = "Diplomatic History of the 21st Century",
                author = "Prof. Robert Hall",
                date = "2025",
                readingTime = "320 pages",
                summary = "A comprehensive academic study of diplomatic breakthroughs, power alliances, and peaceful conflict resolutions in the modern era.",
                content = "Chapter 1: The New Statecraft\nChapter 2: Regional Unions\nChapter 3: Soft Power Dominance\nChapter 4: Crisis Communications",
                bookmarked = false,
                details = ExploreDetailsDto(
                    keyIdeas = listOf("Soft power versus military might", "Multilateralism under stress", "Digital statecraft and public diplomacy"),
                    importantLessons = listOf("Early transparent communication prevents regional wars", "Economic interdependency promotes peace")
                )
            ),
            ExploreDto(
                id = "knowledge_1",
                type = "KNOWLEDGE",
                title = "History of the Printing Press",
                author = "Academy History Dept.",
                date = "2026",
                readingTime = "5m",
                summary = "How Johannes Gutenberg\'s invention of movable type printing democratized knowledge across continents.",
                content = "In 1440, Gutenberg introduced the printing press, triggering an unprecedented information revolution in Europe. Books shifted from luxury manuscript copies to mass-produced literature, fostering literacy, scientific sharing, and humanistic philosophy.",
                bookmarked = false
            ),
            ExploreDto(
                id = "world_1",
                type = "WORLD",
                title = "Major Shifts in Indo-Pacific Alliances",
                author = "Strategic Affairs Council",
                date = "2026-09-15",
                readingTime = "8m",
                summary = "A deep-dive analysis of security alignments, economic partnerships, and trade corridors across the Indo-Pacific region.",
                content = "Sovereign nations are increasingly seeking bilateral and multilateral agreements to navigate maritime trade corridors, balance resources, and assure peaceful transit.",
                sources = listOf("Indo-Pacific Strategic Outlook 2026", "Maritime Safety Reports"),
                bookmarked = false
            ),
            ExploreDto(
                id = "humanity_1",
                type = "HUMANITY",
                title = "The Power of Compassionate Communication",
                author = "Ethical Living Forum",
                date = "2026",
                readingTime = "6m",
                summary = "How deep empathy and dialogue solve interpersonal and community disputes.",
                content = "Compassion is not just a personal sentiment; it is a critical civic skill. When organizations and leaders employ non-violent communication, they validate individual experiences, respect human dignity, and find integrative mutual paths.",
                bookmarked = false
            ),
            ExploreDto(
                id = "society_1",
                type = "SOCIETY",
                title = "Cultivating Civic Responsibility",
                author = "Community Action Council",
                date = "2026",
                readingTime = "7m",
                summary = "Practical methods to promote dialogue, mutual respect, and responsible citizenship.",
                content = "A healthy society relies on more than laws; it is sustained by the voluntary civic acts of its citizens. Broad public dialogue, community service, and environmental stewardship build interpersonal trust and strengthen social cohesion.",
                bookmarked = false
            )
        )
    }
}

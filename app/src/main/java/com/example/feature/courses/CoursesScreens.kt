package com.example.feature.courses

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.network.CourseDto
import com.example.feature.home.CourseCard
import com.example.feature.lesson.LessonViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoursesScreen(
    viewModel: CoursesViewModel,
    onCourseClick: (String) -> Unit
) {
    val courses by viewModel.courses.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val categories = listOf("All", "World", "Humanity", "Society")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.nav_courses),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.searchCourses(it) },
            placeholder = { Text(stringResource(id = R.string.search_hint)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("course_search_bar"),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Tabs
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory ?: "All").coerceAtLeast(0),
            edgePadding = 0.dp,
            divider = {},
            containerColor = androidx.compose.ui.graphics.Color.Transparent
        ) {
            categories.forEach { cat ->
                val isSelected = (selectedCategory ?: "All") == cat
                Tab(
                    selected = isSelected,
                    onClick = { viewModel.filterCategory(if (cat == "All") null else cat) },
                    text = {
                        Text(
                            text = cat,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (loading) {
            Box(modifier = Modifier.fillWeight(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            if (courses.isEmpty()) {
                Box(modifier = Modifier.fillWeight(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(id = R.string.empty_courses),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(courses) { course ->
                        CourseCard(course = course, onClick = { onCourseClick(course.id) })
                    }
                }
            }
        }
    }
}

private fun Modifier.fillWeight() = this.fillMaxWidth().fillMaxHeight()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailsScreen(
    viewModel: CoursesViewModel,
    courseId: String,
    onBackClick: () -> Unit,
    onLessonClick: (String) -> Unit,
    onQuizClick: (String) -> Unit
) {
    val courseDetails by viewModel.courseDetails.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    LaunchedEffect(courseId) {
        viewModel.loadCourseDetails(courseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Course Overview") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    courseDetails?.let { details ->
                        val isBookmarked = bookmarks.any { it.id == details.id }
                        IconButton(onClick = { viewModel.toggleBookmark(details.id, details.title, details.category) }) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            courseDetails?.let { details ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = details.category.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = details.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Led by ${details.instructor}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = details.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Enrollment Status Execution
                    if (!details.enrolled) {
                        Button(
                            onClick = { viewModel.enrollInCourse(details.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("course_enroll_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(id = R.string.enroll_now), style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "You are active in this course.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${details.progress}% Complete",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Course Syllabus",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    details.modules.forEach { module ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = module.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                // Syllabus lessons
                                module.lessons.forEach { lesson ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = details.enrolled) { onLessonClick(lesson.id) }
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (lesson.completed) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = if (lesson.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = lesson.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (details.enrolled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                        Text(
                                            text = lesson.duration,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }

                                // Syllabus Quizzes
                                module.quizzes.forEach { quiz ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = details.enrolled) { onQuizClick(quiz.id) }
                                            .padding(vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Assignment,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = quiz.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (details.enrolled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                        Text(
                                            text = "${quiz.durationMinutes} mins",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    viewModel: LessonViewModel,
    lessonId: String,
    onBackClick: () -> Unit
) {
    val lesson by viewModel.lesson.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(lessonId) {
        viewModel.loadLesson(lessonId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Lesson") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            lesson?.let { data ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = data.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 26.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Resources
                    if (data.resources.isNotEmpty()) {
                        Text(
                            text = "Reference Resources",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        data.resources.forEach { res ->
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(res.url))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(res.title, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                    Icon(Icons.Default.Link, contentDescription = "Open resource", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    if (!data.completed) {
                        Button(
                            onClick = { viewModel.completeLesson(data.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("lesson_complete_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(stringResource(id = R.string.mark_completed), style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Lesson Completed", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

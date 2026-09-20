package com.example.feature.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.core.navigation.Screen
import com.example.core.ui.ViewModelFactory
import com.example.feature.courses.CourseDetailsScreen
import com.example.feature.courses.CoursesScreen
import com.example.feature.courses.CoursesViewModel
import com.example.feature.courses.LessonScreen
import com.example.feature.explore.ExploreDetailScreen
import com.example.feature.explore.ExploreScreen
import com.example.feature.explore.ExploreViewModel
import com.example.feature.home.HomeScreen
import com.example.feature.home.HomeViewModel
import com.example.feature.home.MyLearningScreen
import com.example.feature.lesson.LessonViewModel
import com.example.feature.profile.*
import com.example.feature.quiz.QuizScreen
import com.example.feature.quiz.QuizViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // ViewModels generated cleanly using factory
    val factory = remember { ViewModelFactory.createFactory(context) as ViewModelFactory }
    val repository = factory.repository
    
    val homeViewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val coursesViewModel: CoursesViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val exploreViewModel: ExploreViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val lessonViewModel: LessonViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val quizViewModel: QuizViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
    val profileViewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        NavigationItem(Screen.Home.route, stringResource(id = R.string.nav_home), Icons.Filled.Home, Icons.Outlined.Home),
        NavigationItem(Screen.Courses.route, stringResource(id = R.string.nav_courses), Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        NavigationItem(Screen.Quizzes.route, stringResource(id = R.string.nav_quizzes), Icons.Filled.Quiz, Icons.Outlined.Quiz),
        NavigationItem(Screen.Search.route, stringResource(id = R.string.nav_search), Icons.Filled.Search, Icons.Outlined.Search),
        NavigationItem(Screen.Profile.route, stringResource(id = R.string.nav_dashboard), Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
    )

    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        topBar = {
            if (showBottomBar) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.app_name),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Notifications.route) }) {
                            val count by homeViewModel.unreadNotificationsCount.collectAsState()
                            BadgedBox(
                                badge = {
                                    if (count > 0) {
                                        Badge { Text(count.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Outlined.Notifications, contentDescription = stringResource(id = R.string.nav_notifications))
                            }
                        }
                        IconButton(onClick = { navController.navigate(Screen.Bookmarks.route) }) {
                            Icon(Icons.Outlined.Bookmarks, contentDescription = stringResource(id = R.string.nav_bookmarks))
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.height(56.dp)
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .background(MaterialTheme.colorScheme.secondary, shape = CircleShape)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    } else {
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.tertiary,
                                indicatorColor = androidx.compose.ui.graphics.Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    coursesViewModel = coursesViewModel,
                    onCourseClick = { courseId ->
                        navController.navigate(Screen.CourseDetails.createRoute(courseId))
                    }
                )
            }

            // Courses
            composable(Screen.Courses.route) {
                CoursesScreen(
                    viewModel = coursesViewModel,
                    onCourseClick = { courseId ->
                        navController.navigate(Screen.CourseDetails.createRoute(courseId))
                    }
                )
            }

            // Quizzes List
            composable(Screen.Quizzes.route) {
                com.example.feature.quiz.QuizzesListScreen(
                    viewModel = quizViewModel,
                    onQuizClick = { quizId ->
                        navController.navigate(Screen.Quiz.createRoute(quizId))
                    }
                )
            }

            // Search
            composable(Screen.Search.route) {
                com.example.feature.search.SearchScreen(
                    repository = repository,
                    onCourseClick = { courseId ->
                        navController.navigate(Screen.CourseDetails.createRoute(courseId))
                    },
                    onExploreClick = { itemId, type ->
                        if (type == "COURSE") {
                            navController.navigate(Screen.CourseDetails.createRoute(itemId))
                        } else {
                            exploreViewModel.selectType(type)
                            navController.navigate("explore_detail/$itemId")
                        }
                    }
                )
            }

            // Explore
            composable(Screen.Explore.route) {
                ExploreScreen(
                    viewModel = exploreViewModel,
                    onItemClick = { itemId ->
                        navController.navigate("explore_detail/$itemId")
                    }
                )
            }

            // Explore Detail
            composable(
                route = "explore_detail/{itemId}",
                arguments = listOf(navArgument("itemId") { type = NavType.StringType })
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
                ExploreDetailScreen(
                    viewModel = exploreViewModel,
                    itemId = itemId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // My Learning
            composable(Screen.MyLearning.route) {
                MyLearningScreen(
                    viewModel = coursesViewModel,
                    onCourseClick = { courseId ->
                        navController.navigate(Screen.CourseDetails.createRoute(courseId))
                    }
                )
            }

            // Profile
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = profileViewModel,
                    onEditProfileClick = { navController.navigate("edit_profile") },
                    onChangePasswordClick = { navController.navigate("change_password") },
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    onLogoutClick = {
                        profileViewModel.logout {
                            onLogout()
                        }
                    }
                )
            }

            // Edit Profile
            composable("edit_profile") {
                EditProfileScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Change Password
            composable("change_password") {
                ChangePasswordScreen(
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Settings
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Course Details
            composable(
                route = Screen.CourseDetails.route,
                arguments = listOf(navArgument("courseId") { type = NavType.StringType })
            ) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
                CourseDetailsScreen(
                    viewModel = coursesViewModel,
                    courseId = courseId,
                    onBackClick = { navController.popBackStack() },
                    onLessonClick = { lessonId ->
                        navController.navigate(Screen.Lesson.createRoute(lessonId))
                    },
                    onQuizClick = { quizId ->
                        navController.navigate(Screen.Quiz.createRoute(quizId))
                    }
                )
            }

            // Lesson
            composable(
                route = Screen.Lesson.route,
                arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
            ) { backStackEntry ->
                val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
                LessonScreen(
                    viewModel = lessonViewModel,
                    lessonId = lessonId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Quiz
            composable(
                route = Screen.Quiz.route,
                arguments = listOf(navArgument("quizId") { type = NavType.StringType })
            ) { backStackEntry ->
                val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
                QuizScreen(
                    viewModel = quizViewModel,
                    quizId = quizId,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Bookmarks
            composable(Screen.Bookmarks.route) {
                BookmarksScreen(
                    viewModel = exploreViewModel,
                    coursesViewModel = coursesViewModel,
                    onBackClick = { navController.popBackStack() },
                    onItemClick = { itemId, type ->
                        if (type == "COURSE") {
                            navController.navigate(Screen.CourseDetails.createRoute(itemId))
                        } else {
                            exploreViewModel.selectType(type)
                            navController.navigate("explore_detail/$itemId")
                        }
                    }
                )
            }

            // Notifications
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    viewModel = homeViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

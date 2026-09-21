package com.example.feature.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

data class BottomBarTab(
    val route: String,
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
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

    // Modern 5-Item Structure
    val bottomNavItems = listOf(
        BottomBarTab(Screen.Home.route, stringResource(id = R.string.nav_home), Icons.Filled.Home, Icons.Outlined.Home),
        BottomBarTab(Screen.Courses.route, stringResource(id = R.string.nav_courses), Icons.Filled.MenuBook, Icons.Outlined.MenuBook),
        BottomBarTab(Screen.Search.route, stringResource(id = R.string.nav_search), Icons.Filled.Search, Icons.Outlined.Search),
        BottomBarTab(Screen.MyLearning.route, "Learning", Icons.Filled.School, Icons.Outlined.School),
        BottomBarTab("more", "More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
    )

    val showBottomBar = bottomNavItems.any { it.route == currentRoute } || currentRoute == "more"
    var showMoreSheet by remember { mutableStateOf(false) }

    val userName by homeViewModel.userName.collectAsState()
    val userAvatar by homeViewModel.userAvatar.collectAsState()
    val unreadCount by homeViewModel.unreadNotificationsCount.collectAsState()

    Scaffold(
        topBar = {
            if (showBottomBar) {
                // Compact Premium Header (Logo | Bell Badge + Avatar)
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Official Logo scaled beautifully
                        Image(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = "ThinkTank Academia Logo",
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        // Rightside Icons
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            IconButton(
                                onClick = { navController.navigate(Screen.Notifications.route) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadCount > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.secondary,
                                                contentColor = Color.White
                                            ) {
                                                Text(unreadCount.toString(), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Notifications,
                                        contentDescription = "Notifications",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            // Profile Avatar Initials
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                                    .clickable { navController.navigate(Screen.Profile.route) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                        modifier = Modifier.navigationBarsPadding().height(64.dp)
                    ) {
                        bottomNavItems.forEach { item ->
                            val isSelected = if (item.route == "more") showMoreSheet else (currentRoute == item.route)
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (item.route == "more") {
                                        showMoreSheet = true
                                    } else {
                                        showMoreSheet = false
                                        if (currentRoute != item.route) {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                )
                            )
                        }
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

    // Beautiful Premium "More" Menu Modal Bottom Sheet
    if (showMoreSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 36.dp, top = 8.dp)
            ) {
                // Header Details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "ThinkTank Scholar",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Academy Features
                Text(
                    text = "ACADEMIC PORTAL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                MoreMenuItem(
                    icon = Icons.Outlined.Assignment,
                    title = "Academic Quizzes",
                    subtitle = "Verify and track your course retention",
                    onClick = {
                        showMoreSheet = false
                        navController.navigate(Screen.Quizzes.route)
                    }
                )

                MoreMenuItem(
                    icon = Icons.Outlined.Bookmarks,
                    title = "Saved Bookmarks",
                    subtitle = "Review flagged reference documents & resources",
                    onClick = {
                        showMoreSheet = false
                        navController.navigate(Screen.Bookmarks.route)
                    }
                )

                MoreMenuItem(
                    icon = Icons.Outlined.Notifications,
                    title = "Platform Notifications",
                    subtitle = "Stay updated on recent announcements",
                    onClick = {
                        showMoreSheet = false
                        navController.navigate(Screen.Notifications.route)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Section 2: Account & System
                Text(
                    text = "ACCOUNT SERVICES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                MoreMenuItem(
                    icon = Icons.Outlined.AccountCircle,
                    title = "Academic Profile & Dashboard",
                    subtitle = "View enrolled courses and global stats",
                    onClick = {
                        showMoreSheet = false
                        navController.navigate(Screen.Profile.route)
                    }
                )

                MoreMenuItem(
                    icon = Icons.Outlined.Settings,
                    title = "App Preferences & Settings",
                    subtitle = "Customize application language & options",
                    onClick = {
                        showMoreSheet = false
                        navController.navigate(Screen.Settings.route)
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                        .clickable {
                            showMoreSheet = false
                            profileViewModel.logout {
                                onLogout()
                            }
                        }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ExitToApp,
                        contentDescription = "Logout",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out Account",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun MoreMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
            modifier = Modifier.size(18.dp)
        )
    }
}

package com.yourstudio.hskstroke.bishun.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yourstudio.hskstroke.bishun.ui.account.AccountScreen
import com.yourstudio.hskstroke.bishun.ui.character.CharacterRoute
import com.yourstudio.hskstroke.bishun.ui.character.CharacterViewModel
import com.yourstudio.hskstroke.bishun.ui.courses.CoursesScreen
import com.yourstudio.hskstroke.bishun.ui.library.LibraryScreen
import com.yourstudio.hskstroke.bishun.ui.library.LibraryViewModel
import com.yourstudio.hskstroke.bishun.ui.progress.ProgressScreen

private sealed class MainDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Practice : MainDestination("practice", "练习", Icons.Outlined.Create)
    data object Courses : MainDestination("courses", "课程", Icons.Outlined.AutoStories)
    data object Progress : MainDestination("progress", "进度", Icons.Outlined.Timeline)
    data object Library : MainDestination("library", "字典", Icons.Outlined.MenuBook)
    data object Account : MainDestination("account", "账户", Icons.Outlined.Person)

    companion object {
        val items: List<MainDestination> = listOf(
            Practice,
            Courses,
            Progress,
            Library,
            Account,
        )
    }
}

@Composable
fun BishunApp() {
    val navController = rememberNavController()
    val destinations = remember { MainDestination.items }
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route ?: MainDestination.Practice.route
    val context = LocalContext.current
    val sharedCharacterViewModel: CharacterViewModel = viewModel(
        factory = CharacterViewModel.factory(context),
    )
    val libraryViewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModel.factory(context),
    )
    val userPreferences by sharedCharacterViewModel.userPreferences.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                destinations.forEach { destination ->
                    val selected = destination.route == currentRoute
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.label,
                            )
                        },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainDestination.Practice.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(MainDestination.Practice.route) {
                CharacterRoute(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = sharedCharacterViewModel,
                )
            }
            composable(MainDestination.Courses.route) {
                CoursesScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = sharedCharacterViewModel,
                    onNavigateToPractice = {
                        navController.navigate(MainDestination.Practice.route) {
                            launchSingleTop = true
                        }
                    },
                    languageOverride = userPreferences.languageOverride,
                )
            }
            composable(MainDestination.Progress.route) {
                ProgressScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = sharedCharacterViewModel,
                    onJumpToPractice = {
                        navController.navigate(MainDestination.Practice.route) {
                            launchSingleTop = true
                        }
                    },
                    onJumpToCharacter = { symbol ->
                        sharedCharacterViewModel.jumpToCharacter(symbol)
                        navController.navigate(MainDestination.Practice.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToCourses = {
                        navController.navigate(MainDestination.Courses.route) {
                            launchSingleTop = true
                        }
                    },
                    languageOverride = userPreferences.languageOverride,
                )
            }
            composable(MainDestination.Library.route) {
                LibraryScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = libraryViewModel,
                    onLoadInPractice = { symbol ->
                        sharedCharacterViewModel.jumpToCharacter(symbol)
                        navController.navigate(MainDestination.Practice.route) {
                            launchSingleTop = true
                        }
                    },
                    languageOverride = userPreferences.languageOverride,
                )
            }
            composable(MainDestination.Account.route) {
                AccountScreen(
                    modifier = Modifier.fillMaxSize(),
                    onClearLocalData = sharedCharacterViewModel::clearLocalData,
                    languageOverride = userPreferences.languageOverride,
                )
            }
        }
    }
}

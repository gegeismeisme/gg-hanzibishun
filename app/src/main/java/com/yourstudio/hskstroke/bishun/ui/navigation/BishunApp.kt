package com.yourstudio.hskstroke.bishun.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
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
import com.yourstudio.hskstroke.bishun.ui.learn.LearnScreen
import com.yourstudio.hskstroke.bishun.ui.learn.LearnTab
import com.yourstudio.hskstroke.bishun.ui.library.LibraryScreen
import com.yourstudio.hskstroke.bishun.ui.library.LibraryViewModel

private sealed class MainDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Home : MainDestination("home", "首页", Icons.Outlined.Home)
    data object Learn : MainDestination("learn", "学习", Icons.Outlined.AutoStories)
    data object Library : MainDestination("library", "字典", Icons.AutoMirrored.Outlined.MenuBook)
    data object Account : MainDestination("account", "我的", Icons.Outlined.Person)

    companion object {
        val items: List<MainDestination> = listOf(
            Home,
            Learn,
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
    val rawRoute = currentBackStack?.destination?.route
    val currentRoute = when (rawRoute) {
        LEGACY_COURSES_ROUTE,
        LEGACY_PROGRESS_ROUTE,
        -> MainDestination.Learn.route
        else -> rawRoute
    } ?: MainDestination.Home.route
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
                            if (selected) return@NavigationBarItem
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
            startDestination = MainDestination.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(MainDestination.Home.route) {
                CharacterRoute(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = sharedCharacterViewModel,
                )
            }
            composable(MainDestination.Learn.route) {
                LearnScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = sharedCharacterViewModel,
                    onNavigateToPractice = {
                        navController.navigate(MainDestination.Home.route) {
                            launchSingleTop = true
                        }
                    },
                    languageOverride = userPreferences.languageOverride,
                )
            }
            composable(LEGACY_COURSES_ROUTE) {
                LearnScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = sharedCharacterViewModel,
                    onNavigateToPractice = {
                        navController.navigate(MainDestination.Home.route) {
                            launchSingleTop = true
                        }
                    },
                    languageOverride = userPreferences.languageOverride,
                    initialTab = LearnTab.Courses,
                )
            }
            composable(LEGACY_PROGRESS_ROUTE) {
                LearnScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = sharedCharacterViewModel,
                    onNavigateToPractice = {
                        navController.navigate(MainDestination.Home.route) {
                            launchSingleTop = true
                        }
                    },
                    languageOverride = userPreferences.languageOverride,
                    initialTab = LearnTab.Progress,
                )
            }
            composable(MainDestination.Library.route) {
                LibraryScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = libraryViewModel,
                    onLoadInPractice = { symbol ->
                        sharedCharacterViewModel.jumpToCharacter(symbol)
                        navController.navigate(MainDestination.Home.route) {
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

private const val LEGACY_COURSES_ROUTE = "courses"
private const val LEGACY_PROGRESS_ROUTE = "progress"

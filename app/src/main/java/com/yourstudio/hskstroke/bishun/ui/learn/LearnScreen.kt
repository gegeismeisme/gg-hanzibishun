package com.yourstudio.hskstroke.bishun.ui.learn

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.yourstudio.hskstroke.bishun.ui.character.CharacterViewModel
import com.yourstudio.hskstroke.bishun.ui.character.rememberLocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.courses.CoursesScreen
import com.yourstudio.hskstroke.bishun.ui.progress.ProgressScreen

enum class LearnTab {
    Courses,
    Progress,
}

@Composable
fun LearnScreen(
    modifier: Modifier = Modifier,
    viewModel: CharacterViewModel,
    onNavigateToPractice: () -> Unit,
    languageOverride: String? = null,
    initialTab: LearnTab = LearnTab.Courses,
) {
    var selectedTabKey by rememberSaveable { mutableStateOf(initialTab.name) }
    val selectedTab = remember(selectedTabKey) { LearnTab.valueOf(selectedTabKey) }
    val strings = rememberLocalizedStrings(languageOverride)
    val tabs = remember(strings) {
        listOf(
            LearnTab.Courses to strings.courses.title,
            LearnTab.Progress to strings.progress.title,
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            tabs.forEach { (tab, label) ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTabKey = tab.name },
                    text = { Text(label) },
                )
            }
        }
        when (selectedTab) {
            LearnTab.Courses -> {
                CoursesScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onNavigateToPractice = onNavigateToPractice,
                    languageOverride = languageOverride,
                )
            }

            LearnTab.Progress -> {
                ProgressScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                    onJumpToPractice = onNavigateToPractice,
                    onJumpToCharacter = { symbol ->
                        viewModel.jumpToCharacter(symbol)
                        onNavigateToPractice()
                    },
                    onNavigateToCourses = { selectedTabKey = LearnTab.Courses.name },
                    languageOverride = languageOverride,
                )
            }
        }
    }
}

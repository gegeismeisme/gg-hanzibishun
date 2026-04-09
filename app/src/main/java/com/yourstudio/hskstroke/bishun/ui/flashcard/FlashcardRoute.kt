package com.yourstudio.hskstroke.bishun.ui.flashcard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yourstudio.hskstroke.bishun.ui.character.LocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.character.rememberLocalizedStrings

@Composable
fun FlashcardRoute(
    modifier: Modifier = Modifier,
    languageOverride: String?,
    isPro: Boolean,
    onNavigateToPractice: (String) -> Unit,
    onBuyPro: () -> Unit = {},
) {
    val viewModel: FlashcardViewModel = viewModel(factory = FlashcardViewModel.factory(
        androidx.compose.ui.platform.LocalContext.current
    ))
    val state by viewModel.state.collectAsState()
    val strings = rememberLocalizedStrings(languageOverride)

    FlashcardScreen(
        modifier = modifier,
        state = state,
        strings = strings,
        isPro = isPro,
        onSelectLevel = { viewModel.selectLevel(it) },
        onReveal = { viewModel.revealCard() },
        onRate = { viewModel.submitRating(it) },
        onNavigateToPractice = onNavigateToPractice,
        onBuyPro = onBuyPro,
    )
}

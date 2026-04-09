package com.yourstudio.hskstroke.bishun.ui.flashcard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yourstudio.hskstroke.bishun.data.flashcard.StudyCard
import com.yourstudio.hskstroke.bishun.data.flashcard.StudyRating
import com.yourstudio.hskstroke.bishun.ui.character.LocalizedStrings

@Composable
fun FlashcardScreen(
    modifier: Modifier = Modifier,
    state: FlashcardUiState,
    strings: LocalizedStrings,
    isPro: Boolean,
    onSelectLevel: (Int) -> Unit,
    onReveal: () -> Unit,
    onRate: (StudyRating) -> Unit,
    onNavigateToPractice: (String) -> Unit,
    onBuyPro: () -> Unit,
) {
    when (state) {
        is FlashcardUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(strings.flashcard.seedingMessage, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        is FlashcardUiState.Done -> {
            DoneContent(
                modifier = modifier,
                state = state,
                strings = strings,
                onSelectLevel = onSelectLevel,
            )
        }

        is FlashcardUiState.Ready -> {
            ReadyContent(
                modifier = modifier,
                state = state,
                strings = strings,
                isPro = isPro,
                onSelectLevel = onSelectLevel,
                onReveal = onReveal,
                onRate = onRate,
                onNavigateToPractice = onNavigateToPractice,
                onBuyPro = onBuyPro,
            )
        }
    }
}

@Composable
private fun ReadyContent(
    modifier: Modifier,
    state: FlashcardUiState.Ready,
    strings: LocalizedStrings,
    isPro: Boolean,
    onSelectLevel: (Int) -> Unit,
    onReveal: () -> Unit,
    onRate: (StudyRating) -> Unit,
    onNavigateToPractice: (String) -> Unit,
    onBuyPro: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Level selector
        LevelSelector(
            selectedLevel = state.selectedLevel,
            isPro = isPro,
            strings = strings,
            onSelectLevel = onSelectLevel,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Stats
        StatsRow(state = state, strings = strings)

        Spacer(modifier = Modifier.height(16.dp))

        // Pro gate overlay for non-Pro HSK 2+ selection
        val showProGate = !isPro && state.selectedLevel > 1

        if (showProGate) {
            ProGateCard(
                modifier = Modifier.weight(1f),
                strings = strings,
                onBuyPro = onBuyPro,
            )
        } else if (state.card != null) {
            // Flashcard
            FlashcardContent(
                card = state.card,
                isRevealed = state.isRevealed,
                strings = strings,
                modifier = Modifier.weight(1f),
                onReveal = onReveal,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rating buttons (visible only after reveal)
            AnimatedVisibility(
                visible = state.isRevealed,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                RatingButtons(onRate = onRate, strings = strings)
            }
        } else {
            // No card to review
            EmptyContent(
                modifier = Modifier.weight(1f),
                strings = strings,
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Composable
private fun DoneContent(
    modifier: Modifier,
    state: FlashcardUiState.Done,
    strings: LocalizedStrings,
    onSelectLevel: (Int) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        LevelSelector(
            selectedLevel = 0,
            isPro = true,
            strings = strings,
            onSelectLevel = onSelectLevel,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = strings.flashcard.emptyTitle,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = strings.flashcard.reviewedFormat.format(state.sessionReviewed),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = strings.flashcard.emptyDescription,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun LevelSelector(
    selectedLevel: Int,
    isPro: Boolean,
    strings: LocalizedStrings,
    onSelectLevel: (Int) -> Unit,
) {
    val levels = listOf(0, 1, 2, 3, 4, 5, 6, 7)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        levels.forEach { level ->
            val isLocked = level > 1 && !isPro
            FilterChip(
                selected = selectedLevel == level,
                onClick = {
                    if (!isLocked) onSelectLevel(level)
                },
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isLocked) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = null,
                                modifier = Modifier.height(14.dp),
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                        }
                        Text(
                            text = if (level == 0) strings.flashcard.levelAll
                            else strings.flashcard.levelFormat.format(level),
                            fontSize = 12.sp,
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}

@Composable
private fun StatsRow(
    state: FlashcardUiState.Ready,
    strings: LocalizedStrings,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatItem(label = strings.flashcard.statDue, value = state.stats.dueCount)
        StatItem(label = strings.flashcard.statWeak, value = state.stats.weakCount)
        StatItem(label = strings.flashcard.statMastered, value = state.stats.masteredCount)
    }
}

@Composable
private fun StatItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FlashcardContent(
    card: StudyCard,
    isRevealed: Boolean,
    strings: LocalizedStrings,
    modifier: Modifier = Modifier,
    onReveal: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isRevealed) { onReveal() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Hanzi character (always visible)
                Text(
                    text = card.hanzi,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(8.dp))

                AnimatedContent(
                    targetState = isRevealed,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "card_flip",
                ) { revealed ->
                    if (revealed) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = card.pinyin,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = card.english,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            if (card.example.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = card.example,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    } else {
                        Text(
                            text = strings.flashcard.cardFrontHint,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingButtons(
    onRate: (StudyRating) -> Unit,
    strings: LocalizedStrings,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = { onRate(StudyRating.Again) },
            modifier = Modifier.weight(1f),
        ) {
            Text(strings.flashcard.ratingAgain)
        }
        OutlinedButton(
            onClick = { onRate(StudyRating.Hard) },
            modifier = Modifier.weight(1f),
        ) {
            Text(strings.flashcard.ratingHard)
        }
        Button(
            onClick = { onRate(StudyRating.Good) },
            modifier = Modifier.weight(1f),
        ) {
            Text(strings.flashcard.ratingGood)
        }
        OutlinedButton(
            onClick = { onRate(StudyRating.Easy) },
            modifier = Modifier.weight(1f),
        ) {
            Text(strings.flashcard.ratingEasy)
        }
    }
}

@Composable
private fun ProGateCard(
    modifier: Modifier = Modifier,
    strings: LocalizedStrings,
    onBuyPro: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = strings.flashcard.proGateTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.flashcard.proGateDescription,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onBuyPro) {
                Text(strings.flashcard.proGateButton)
            }
        }
    }
}

@Composable
private fun EmptyContent(
    modifier: Modifier = Modifier,
    strings: LocalizedStrings,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = strings.flashcard.emptyTitle,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = strings.flashcard.emptyDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

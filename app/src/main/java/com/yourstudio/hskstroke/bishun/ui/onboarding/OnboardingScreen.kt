package com.yourstudio.hskstroke.bishun.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourstudio.hskstroke.bishun.ui.character.rememberLocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.testing.TestTags

@Composable
fun OnboardingScreen(
    modifier: Modifier = Modifier,
    onFinish: () -> Unit,
    onSkip: () -> Unit,
    languageOverride: String? = null,
) {
    val strings = rememberLocalizedStrings(languageOverride)
    val onboardingStrings = strings.onboarding
    val pages = remember(onboardingStrings) {
        onboardingIcons.zip(onboardingStrings.pages).map { (icon, text) ->
            OnboardingPage(
                icon = icon,
                title = text.title,
                body = text.body,
            )
        }
    }
    if (pages.isEmpty()) return
    var index by rememberSaveable { mutableIntStateOf(0) }
    val clampedIndex = index.coerceIn(0, pages.lastIndex)
    val page = pages[clampedIndex]
    val isLastPage = clampedIndex == pages.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = onboardingStrings.appTitle,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onSkip, modifier = Modifier.testTag(TestTags.ONBOARDING_SKIP)) {
                Text(onboardingStrings.skipLabel)
            }
        }

        Text(
            text = "${clampedIndex + 1}/${pages.size}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                )
                Text(
                    text = page.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { index = (clampedIndex - 1).coerceAtLeast(0) },
                enabled = clampedIndex > 0,
                modifier = Modifier.testTag(TestTags.ONBOARDING_BACK),
            ) {
                Text(onboardingStrings.backLabel)
            }
            Button(
                onClick = {
                    if (isLastPage) {
                        onFinish()
                    } else {
                        index = (clampedIndex + 1).coerceAtMost(pages.lastIndex)
                    }
                },
                modifier = Modifier.testTag(TestTags.ONBOARDING_NEXT),
            ) {
                Text(if (isLastPage) onboardingStrings.startLabel else onboardingStrings.nextLabel)
            }
        }
    }
}

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String,
)

private val onboardingIcons = listOf(
    Icons.Outlined.Search,
    Icons.Outlined.Create,
    Icons.AutoMirrored.Outlined.MenuBook,
    Icons.AutoMirrored.Outlined.VolumeUp,
)

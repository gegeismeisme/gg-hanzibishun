package com.yourstudio.hskstroke.bishun.ui.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
fun PracticeBottomBar(
    primaryLabel: String,
    onPrimaryClick: () -> Unit,
    primaryEnabled: Boolean,
    onPlayPronunciation: () -> Unit,
    pronunciationEnabled: Boolean,
    onShowDictionary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompactIconButton(
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                )
            },
            description = "Play pronunciation",
            enabled = pronunciationEnabled,
            onClick = onPlayPronunciation,
        )
        Button(
            onClick = onPrimaryClick,
            enabled = primaryEnabled,
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .height(48.dp)
                .weight(1f),
        ) {
            Text(text = primaryLabel, style = MaterialTheme.typography.labelLarge)
        }
        CompactIconButton(
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                    contentDescription = null,
                )
            },
            description = "Show dictionary entry",
            enabled = true,
            onClick = onShowDictionary,
        )
    }
}

@Composable
private fun CompactIconButton(
    icon: @Composable () -> Unit,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .size(44.dp)
            .semantics { contentDescription = description },
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
            icon()
        }
    }
}


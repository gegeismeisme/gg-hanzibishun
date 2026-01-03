package com.yourstudio.hskstroke.bishun.ui.audio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun VolumeSafetyDialog(
    currentVolumePercent: Int,
    thresholdPercent: Int,
    lowerToPercent: Int,
    onCheckVolume: () -> Unit,
    onLowerAndPlay: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Volume is high") },
        text = {
            Text(
                text = "Media volume is at $currentVolumePercent% (threshold: $thresholdPercent%).",
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onLowerAndPlay) {
                Text("Lower to $lowerToPercent% and play")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onCheckVolume) {
                    Text("Check")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
    )
}

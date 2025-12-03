package com.example.bishun.ui.support

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bishun.data.settings.UserPreferences
import com.example.bishun.ui.character.HelpSectionText
import com.example.bishun.ui.character.LocalizedStrings
import com.example.bishun.ui.character.PolicySectionText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val SUPPORT_EMAIL = "qq260316514@gmail.com"

@Composable
fun HelpDialog(
    strings: LocalizedStrings,
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val sections = strings.helpSections
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.helpTitle) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                sections.forEach { section ->
                    HelpSectionCard(section = section)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(strings.helpConfirm) }
        },
    )
}

@Composable
private fun HelpSectionCard(section: HelpSectionText) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(section.title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = section.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            section.bullets.forEach { bullet ->
                Text(
                    text = "- $bullet",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun PrivacyDialog(
    prefs: UserPreferences,
    onAnalyticsChange: (Boolean) -> Unit,
    onCrashChange: (Boolean) -> Unit,
    onPrefetchChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    strings: LocalizedStrings,
) {
    val context = LocalContext.current
    val contactEmail = SUPPORT_EMAIL
    val summaryPoints = strings.privacySummaryRows
    var showFullPolicy by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.privacyTitle) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = strings.privacyIntro,
                    style = MaterialTheme.typography.bodySmall,
                )
                PrivacyToggleRow(
                    title = "Usage analytics",
                    description = "Anonymous counters for upcoming features.",
                    checked = prefs.analyticsOptIn,
                    onCheckedChange = onAnalyticsChange,
                )
                PrivacyToggleRow(
                    title = "Crash reports",
                    description = "Share lightweight logs if rendering fails.",
                    checked = prefs.crashReportsOptIn,
                    onCheckedChange = onCrashChange,
                )
                PrivacyToggleRow(
                    title = "Network prefetch",
                    description = "Allow Wi-Fi downloads of future packs.",
                    checked = prefs.networkPrefetchEnabled,
                    onCheckedChange = onPrefetchChange,
                )
                HorizontalDivider()
                Text(
                    text = strings.dataSafetyHeading,
                    style = MaterialTheme.typography.labelLarge,
                )
                summaryPoints.forEach { point ->
                    PrivacySummaryCard(PrivacySummaryRow(point.title, point.detail))
                }
                Text(
                    text = String.format(strings.locale, strings.contactSupportLabel, contactEmail),
                    style = MaterialTheme.typography.bodySmall,
                )
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:$contactEmail")
                            putExtra(Intent.EXTRA_SUBJECT, "Hanzi Stroke Order - Privacy question")
                        }
                        val chooser = Intent.createChooser(intent, strings.emailSupportButton)
                        if (intent.resolveActivity(context.packageManager) != null) {
                            runCatching { context.startActivity(chooser) }
                        }
                    },
                ) {
                    Text(strings.emailSupportButton)
                }
                TextButton(onClick = { showFullPolicy = true }) {
                    Text(strings.viewPolicyButton)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
    if (showFullPolicy) {
        FullPrivacyPolicyDialog(strings = strings, onDismiss = { showFullPolicy = false })
    }
}

@Composable
private fun PrivacyToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun FeedbackDialog(
    prefs: UserPreferences,
    lastSubmittedAt: Long?,
    onShareLog: () -> Unit,
    onDraftChange: (String, String, String) -> Unit,
    onSubmit: (String, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var topic by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    val canSubmit = message.trim().length >= 6
    val scrollState = rememberScrollState()

    LaunchedEffect(prefs.feedbackTopic, prefs.feedbackMessage, prefs.feedbackContact) {
        if (!submitted) {
            topic = prefs.feedbackTopic
            message = prefs.feedbackMessage
            contact = prefs.feedbackContact
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (submitted) "Thanks!" else "Send feedback") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (lastSubmittedAt != null) {
                    Text(
                        text = "Last sent: ${formatHistoryTimestamp(lastSubmittedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (submitted) {
                    Text(
                        text = "We stored your note locally. The next release will bundle it with diagnostic exports so nothing gets lost.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    SupportTextField(
                        value = topic,
                        onValueChange = {
                            val value = it.take(60)
                            topic = value
                            onDraftChange(value, message, contact)
                        },
                        label = "Topic",
                        placeholder = "Feature request, bug...",
                        singleLine = true,
                    )
                    SupportTextField(
                        value = message,
                        onValueChange = {
                            val value = it.take(600)
                            message = value
                            onDraftChange(topic, value, contact)
                        },
                        label = "Details",
                        placeholder = "Describe the idea or issue",
                        minLines = 4,
                    )
                    SupportTextField(
                        value = contact,
                        onValueChange = {
                            val value = it.take(80)
                            contact = value
                            onDraftChange(topic, message, value)
                        },
                        label = "Contact (optional)",
                        placeholder = "Email or handle",
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            if (submitted) {
                TextButton(onClick = onDismiss) { Text("Close") }
            } else {
                TextButton(
                    onClick = {
                        onSubmit(topic, message, contact)
                        submitted = true
                    },
                    enabled = canSubmit,
                ) {
                    Text("Send")
                }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (lastSubmittedAt != null) {
                    TextButton(onClick = onShareLog) { Text("Share log") }
                }
                if (!submitted) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                }
            }
        },
    )
}

@Composable
private fun SupportTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean = false,
    minLines: Int = 1,
) {
    androidx.compose.material3.OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = singleLine,
        minLines = minLines,
    )
}

@Composable
private fun FullPrivacyPolicyDialog(
    strings: LocalizedStrings,
    onDismiss: () -> Unit,
) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.fullPolicyTitle) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                strings.fullPolicySections.forEach { section ->
                    PolicySectionCard(section)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
    )
}

@Composable
private fun PolicySectionCard(section: PolicySectionText) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(section.title, style = MaterialTheme.typography.titleSmall)
            section.bullets.forEach { bullet ->
                Text(
                    text = "- $bullet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private data class PrivacySummaryRow(val title: String, val details: String)

@Composable
private fun PrivacySummaryCard(row: PrivacySummaryRow) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(row.title, style = MaterialTheme.typography.labelLarge)
            Text(
                text = row.details,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

fun formatHistoryTimestamp(timestamp: Long): String {
    return try {
        val formatter = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
        formatter.format(Date(timestamp))
    } catch (_: Exception) {
        "-"
    }
}

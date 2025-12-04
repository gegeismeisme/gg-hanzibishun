package com.example.bishun.ui.account

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bishun.data.settings.UserPreferences
import com.example.bishun.ui.character.AccountStrings
import com.example.bishun.ui.character.FeedbackSubmission
import com.example.bishun.ui.character.LocalizedStrings
import com.example.bishun.ui.character.rememberLocalizedStrings
import com.example.bishun.ui.support.FeedbackDialog
import com.example.bishun.ui.support.HelpDialog
import com.example.bishun.ui.support.PrivacyDialog
import com.example.bishun.ui.support.SUPPORT_EMAIL
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel,
    userPreferences: UserPreferences,
    lastFeedbackTimestamp: Long?,
    feedbackSubmission: FeedbackSubmission?,
    onAnalyticsChange: (Boolean) -> Unit,
    onCrashChange: (Boolean) -> Unit,
    onPrefetchChange: (Boolean) -> Unit,
    onFeedbackDraftChange: (String, String, String) -> Unit,
    onFeedbackSubmit: (String, String, String) -> Unit,
    onFeedbackHandled: () -> Unit,
    onLoadFeedbackLog: suspend () -> String,
    languageOverride: String?,
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoggedIn = uiState.isSignedIn
    val hasUnlockedPremium = uiState.hasPremiumAccess
    var showLoginDialog by rememberSaveable { mutableStateOf(false) }
    var showPurchaseDialog by rememberSaveable { mutableStateOf(false) }
    var showHelpDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacyDialog by rememberSaveable { mutableStateOf(false) }
    var showFeedbackDialog by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val strings = rememberLocalizedStrings(languageOverride)
    val accountStrings = strings.account
    val supportStrings = strings.support
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val shareFeedbackLog = remember(onLoadFeedbackLog, context) {
        {
            coroutineScope.launch {
                val logText = onLoadFeedbackLog().takeIf { it.isNotBlank() }
                if (logText.isNullOrBlank()) {
                    Toast.makeText(context, supportStrings.feedbackLogEmpty, Toast.LENGTH_LONG).show()
                    return@launch
                }
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Hanzi feedback log")
                    putExtra(Intent.EXTRA_TEXT, logText)
                }
                val chooser = Intent.createChooser(shareIntent, "Share feedback log")
                val canHandle = shareIntent.resolveActivity(context.packageManager) != null
                if (canHandle) {
                    runCatching { context.startActivity(chooser) }
                        .onFailure {
                            Toast.makeText(context, supportStrings.feedbackLogShareError, Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(context, supportStrings.feedbackLogNoApps, Toast.LENGTH_LONG).show()
                }
            }
            Unit
        }
    }

    LaunchedEffect(feedbackSubmission) {
        val submission = feedbackSubmission ?: return@LaunchedEffect
        val subject = if (submission.topic.isNotBlank()) {
            "Hanzi Stroke Order feedback: ${submission.topic}"
        } else {
            "Hanzi Stroke Order feedback"
        }
        val contactLine = submission.contact.takeIf { it.isNotBlank() }?.let { "\n\nContact: $it" } ?: ""
        val body = submission.message.ifBlank { "(No message)" } + contactLine
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        val chooser = Intent.createChooser(intent, "Send feedback")
        val canHandle = intent.resolveActivity(context.packageManager) != null
        if (canHandle) {
            runCatching { context.startActivity(chooser) }
                .onFailure {
                    Toast.makeText(context, supportStrings.feedbackEmailError, Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(context, supportStrings.feedbackEmailNoApp, Toast.LENGTH_LONG).show()
        }
        onFeedbackHandled()
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(text = accountStrings.title, style = MaterialTheme.typography.headlineSmall)
        AccountCard(
            title = accountStrings.signInCardTitle,
            description = if (isLoggedIn) {
                accountStrings.signInDescriptionSignedIn
            } else {
                accountStrings.signInDescriptionSignedOut
            },
            buttonLabel = if (isLoggedIn) accountStrings.signInButtonSignedIn else accountStrings.signInButtonSignedOut,
            onClick = {
                if (isLoggedIn) {
                    viewModel.signOut()
                } else {
                    showLoginDialog = true
                }
            },
        )
        AccountCard(
            title = accountStrings.courseCardTitle,
            description = when {
                !isLoggedIn -> accountStrings.courseDescriptionSignedOut
                hasUnlockedPremium -> accountStrings.courseDescriptionUnlocked
                else -> accountStrings.courseDescriptionLocked
            },
            buttonLabel = when {
                !isLoggedIn -> accountStrings.courseButtonSignedOut
                hasUnlockedPremium -> accountStrings.courseButtonUnlocked
                else -> accountStrings.courseButtonLocked
            },
            onClick = {
                if (!isLoggedIn) {
                    showLoginDialog = true
                } else if (!hasUnlockedPremium) {
                    showPurchaseDialog = true
                }
            },
            enabled = isLoggedIn && !hasUnlockedPremium,
        )
        SupportCard(
            accountStrings = accountStrings,
            strings = strings,
            onHelpClick = { showHelpDialog = true },
            onPrivacyClick = { showPrivacyDialog = true },
            onFeedbackClick = { showFeedbackDialog = true },
        )
    }

    if (showLoginDialog) {
        ConsentDialog(
            title = accountStrings.consentSignInTitle,
            bulletPoints = accountStrings.consentSignInBullets,
            confirmLabel = accountStrings.consentSignInConfirm,
            checkboxLabel = accountStrings.consentCheckboxLabel,
            cancelLabel = accountStrings.cancelLabel,
            closeLabel = accountStrings.closeLabel,
            onConfirm = {
                viewModel.signIn()
                showLoginDialog = false
            },
            onDismiss = { showLoginDialog = false },
        )
    }
    if (showPurchaseDialog) {
        ConsentDialog(
            title = accountStrings.consentUnlockTitle,
            bulletPoints = accountStrings.consentUnlockBullets,
            confirmLabel = accountStrings.consentUnlockConfirm,
            checkboxLabel = accountStrings.consentCheckboxLabel,
            cancelLabel = accountStrings.cancelLabel,
            closeLabel = accountStrings.closeLabel,
            onConfirm = {
                viewModel.unlockPremiumLevels()
                showPurchaseDialog = false
            },
            onDismiss = { showPurchaseDialog = false },
        )
    }
    if (showHelpDialog) {
        HelpDialog(strings = strings, onDismiss = { showHelpDialog = false })
    }
    if (showPrivacyDialog) {
        PrivacyDialog(
            prefs = userPreferences,
            onAnalyticsChange = onAnalyticsChange,
            onCrashChange = onCrashChange,
            onPrefetchChange = onPrefetchChange,
            onDismiss = { showPrivacyDialog = false },
            strings = strings,
        )
    }
    if (showFeedbackDialog) {
        FeedbackDialog(
            prefs = userPreferences,
            lastSubmittedAt = lastFeedbackTimestamp,
            strings = supportStrings,
            onShareLog = shareFeedbackLog,
            onDraftChange = onFeedbackDraftChange,
            onSubmit = onFeedbackSubmit,
            onDismiss = { showFeedbackDialog = false },
        )
    }
}

@Composable
private fun AccountCard(
    title: String,
    description: String,
    buttonLabel: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                onClick = onClick,
                enabled = enabled,
            ) {
                Text(buttonLabel)
            }
        }
    }
}

@Composable
private fun SupportCard(
    accountStrings: AccountStrings,
    onHelpClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    strings: LocalizedStrings,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = accountStrings.supportTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = accountStrings.supportDescription,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onHelpClick) {
                Text(strings.helpTitle)
            }
            Button(onClick = onPrivacyClick) {
                Text(strings.privacyTitle)
            }
            OutlinedButton(onClick = onFeedbackClick) {
                Text(accountStrings.supportFeedbackButton)
            }
        }
    }
}

@Composable
private fun ConsentDialog(
    title: String,
    bulletPoints: List<String>,
    confirmLabel: String,
    checkboxLabel: String,
    cancelLabel: String,
    closeLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    var accepted by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                bulletPoints.forEach { entry ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(text = "•", modifier = Modifier.padding(end = 8.dp))
                        Text(
                            text = entry,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = accepted,
                        onCheckedChange = { accepted = it },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = checkboxLabel, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    accepted = false
                },
                enabled = accepted,
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    accepted = false
                    onDismiss()
                },
            ) {
                Text(cancelLabel)
            }
        },
    )
}

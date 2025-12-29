package com.yourstudio.hskstroke.bishun.ui.account
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferences
import com.yourstudio.hskstroke.bishun.ui.character.AccountStrings
import com.yourstudio.hskstroke.bishun.ui.character.LocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.character.rememberLocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.support.HelpDialog
import com.yourstudio.hskstroke.bishun.ui.support.PrivacyDialog

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    onClearLocalData: () -> Unit,
    languageOverride: String?,
) {
    var showHelpDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacyDialog by rememberSaveable { mutableStateOf(false) }
    var showClearDataDialog by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val strings = rememberLocalizedStrings(languageOverride)
    val accountStrings = strings.account

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(text = accountStrings.title, style = MaterialTheme.typography.headlineSmall)
        SupportCard(
            accountStrings = accountStrings,
            strings = strings,
            onHelpClick = { showHelpDialog = true },
            onPrivacyClick = { showPrivacyDialog = true },
        )
        AccountCard(
            title = accountStrings.clearDataTitle,
            description = accountStrings.clearDataDescription,
            buttonLabel = accountStrings.clearDataButton,
            onClick = { showClearDataDialog = true },
        )
    }
    if (showHelpDialog) {
        HelpDialog(strings = strings, onDismiss = { showHelpDialog = false })
    }
    if (showPrivacyDialog) {
        PrivacyDialog(onDismiss = { showPrivacyDialog = false }, strings = strings)
    }
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text(accountStrings.clearDataDialogTitle) },
            text = { Text(accountStrings.clearDataDialogBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearLocalData()
                        showClearDataDialog = false
                    },
                ) {
                    Text(accountStrings.clearDataButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text(accountStrings.cancelLabel)
                }
            },
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

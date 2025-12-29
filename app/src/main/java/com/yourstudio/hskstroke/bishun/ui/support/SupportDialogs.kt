package com.yourstudio.hskstroke.bishun.ui.support

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yourstudio.hskstroke.bishun.ui.character.HelpSectionText
import com.yourstudio.hskstroke.bishun.ui.character.LocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.character.PolicySectionText
import com.yourstudio.hskstroke.bishun.ui.character.SupportStrings

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
    onDismiss: () -> Unit,
    strings: LocalizedStrings,
) {
    val contactEmail = SUPPORT_EMAIL
    val summaryPoints = strings.privacySummaryRows
    val supportStrings = strings.support
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
                TextButton(onClick = { showFullPolicy = true }) {
                    Text(strings.viewPolicyButton)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(supportStrings.privacyCloseLabel) }
        },
    )
    if (showFullPolicy) {
        FullPrivacyPolicyDialog(strings = strings, supportStrings = supportStrings, onDismiss = { showFullPolicy = false })
    }
}

@Composable
private fun FullPrivacyPolicyDialog(
    strings: LocalizedStrings,
    supportStrings: SupportStrings,
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
            TextButton(onClick = onDismiss) { Text(supportStrings.privacyCloseLabel) }
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


package com.example.bishun.ui.account

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val isLoggedIn = uiState.isSignedIn
    val hasUnlockedPremium = uiState.hasPremiumAccess
    var showLoginDialog by rememberSaveable { mutableStateOf(false) }
    var showPurchaseDialog by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "账户与购买",
            style = MaterialTheme.typography.headlineSmall,
        )
        AccountCard(
            title = "登录状态",
            description = if (isLoggedIn) {
                "已登录，课程进度可与购买凭证绑定。"
            } else {
                "当前为游客模式。登录仅在购买课程时使用，不影响本地练习功能。"
            },
            buttonLabel = if (isLoggedIn) "退出登录" else "登录并同步",
            onClick = {
                if (isLoggedIn) {
                    viewModel.signOut()
                } else {
                    showLoginDialog = true
                }
            },
        )
        AccountCard(
            title = "课程解锁",
            description = when {
                !isLoggedIn -> "请先登录后再解锁课程。未登录时课程以预览模式显示。"
                hasUnlockedPremium -> "已解锁全部课程。进入“课程”页即可继续学习。"
                else -> "购买后即可离线访问所有课程列表，包含未来更新内容。"
            },
            buttonLabel = when {
                !isLoggedIn -> "登录后解锁"
                hasUnlockedPremium -> "查看课程"
                else -> "解锁课程"
            },
            onClick = {
                if (!isLoggedIn) {
                    showLoginDialog = true
                } else if (!hasUnlockedPremium) {
                    showPurchaseDialog = true
                }
            },
            enabled = isLoggedIn,
        )
        AccountCard(
            title = "支持与合规",
            description = "帮助、隐私、反馈入口将迁移至此。登录/购买前会再次展示完整隐私条款，确保满足 Google Play 合规要求。",
            buttonLabel = "查看计划",
            onClick = { showLoginDialog = true },
        )
    }

    if (showLoginDialog) {
        ConsentDialog(
            title = "登录前须知",
            bulletPoints = listOf(
                "登录仅用于绑定购买信息，练习数据仍保存在本地。",
                "登录意味着您已阅读并同意《隐私政策》（docs/privacy-policy.md）。",
                "如不同意，可继续使用游客模式，但无法解锁付费课程。",
            ),
            confirmLabel = "同意并登录",
            onConfirm = {
                viewModel.signIn()
                showLoginDialog = false
            },
            onDismiss = { showLoginDialog = false },
        )
    }
    if (showPurchaseDialog) {
        ConsentDialog(
            title = "解锁课程",
            bulletPoints = listOf(
                "付款完成后课程授权将缓存在本地，并可离线使用。",
                "购买记录会写入本地凭证，同时显示在“课程”页中。",
                "购买流程会再次展示隐私/条款，确保符合商店合规要求。",
            ),
            confirmLabel = "确认解锁",
            onConfirm = {
                viewModel.unlockPremiumLevels()
                showPurchaseDialog = false
            },
            onDismiss = { showPurchaseDialog = false },
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
private fun ConsentDialog(
    title: String,
    bulletPoints: List<String>,
    confirmLabel: String,
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
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = accepted,
                        onCheckedChange = { accepted = it },
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "我已阅读并同意隐私条款",
                        style = MaterialTheme.typography.bodyMedium,
                    )
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
                Text("取消")
            }
        },
    )
}

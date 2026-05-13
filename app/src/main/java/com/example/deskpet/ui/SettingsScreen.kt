package com.example.deskpet.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deskpet.ui.components.ScreenTopBar

private enum class DangerAction {
    ClearDiary,
    ResetPet,
    ClearImages
}

@Composable
fun SettingsScreen(
    backendUrl: String,
    onBack: () -> Unit,
    onTestBackend: () -> Unit,
    onClearDiary: () -> Unit,
    onResetPet: () -> Unit,
    onClearImageCache: () -> Unit
) {
    var pendingAction by remember { mutableStateOf<DangerAction?>(null) }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = "设置",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsCard(
                title = "后端地址",
                body = backendUrl
            ) {
                Button(onClick = onTestBackend, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "测试后端连接")
                }
            }

            SettingsCard(
                title = "数据管理",
                body = "可以清空日记、重置宠物，或者清理本地缓存图片。"
            ) {
                Button(
                    onClick = { pendingAction = DangerAction.ClearDiary },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "清空全部日记")
                }
                Button(
                    onClick = { pendingAction = DangerAction.ResetPet },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "重置宠物")
                }
                Button(
                    onClick = { pendingAction = DangerAction.ClearImages },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "清理本地图片缓存")
                }
            }

            SettingsCard(
                title = "关于本应用",
                body = "当前是 DeskPet 的 MVP。宠物回复定位于情绪陪伴和记录，不提供医疗或心理治疗建议。日记默认保存在本机。"
            )
        }
    }

    val currentAction = pendingAction
    if (currentAction != null) {
        val (title, message, confirmAction) = when (currentAction) {
            DangerAction.ClearDiary -> Triple("清空全部日记？", "这会删除当前本机中的所有日记记录。", onClearDiary)
            DangerAction.ResetPet -> Triple("重置宠物？", "这会恢复默认宠物资料和状态。", onResetPet)
            DangerAction.ClearImages -> Triple("清理本地图片缓存？", "这会移除缓存的宠物图片，宠物会回到默认占位图。", onClearImageCache)
        }
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmAction()
                        pendingAction = null
                    }
                ) {
                    Text(text = "确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) {
                    Text(text = "取消")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    body: String,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (content != null) {
                content()
            }
        }
    }
}

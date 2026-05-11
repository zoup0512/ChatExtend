package com.zoup.android.chatextend.ui.history

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zoup.android.chatextend.MainActivity
import com.zoup.android.chatextend.R
import com.zoup.android.chatextend.data.api.model.Message
import com.zoup.android.chatextend.data.database.entity.ChatMessageEntity
import com.zoup.android.chatextend.ui.chat.ChatViewModel
import com.zoup.android.chatextend.utils.MessageIdManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: ChatViewModel
) {
    val history by viewModel.getAllHistoryMessages().collectAsState(initial = emptyList())
    val context = LocalContext.current
    val activity = context as? MainActivity ?: return // 确保是 MainActivity 上下文

    val navController = remember {
        activity.findNavController(R.id.nav_host_fragment_content_main)
    }

    var searchQuery by remember { mutableStateOf("") }

    // 过滤历史记录
    val filteredHistory = remember(history, searchQuery) {
        if (searchQuery.isEmpty()) {
            history
        } else {
            history.filter { message ->
                val type = object : TypeToken<List<Message>>() {}.type
                val jsonContent = message.content
                if (jsonContent.isNotEmpty()) {
                    val messages = Gson().fromJson<List<Message>>(jsonContent, type)
                    messages.any { it.content.contains(searchQuery, ignoreCase = true) }
                } else {
                    false
                }
            }
        }
    }

    // 分组逻辑
    val now = System.currentTimeMillis()
    val groupedHistory = filteredHistory.groupBy { message ->
        val diffDays = (now - message.timestamp) / (24 * 60 * 60 * 1000)
        when {
            diffDays < 1 -> "今天"
            diffDays < 2 -> "昨天"
            diffDays < 7 -> "最近7天"
            diffDays < 30 -> "最近30天"
            diffDays < 365 -> "一年内"
            else -> "更早"
        }
    }

    val groupOrder = listOf("今天", "昨天", "最近7天", "最近30天", "一年内", "更早")
    val sortedGroups = groupedHistory.entries.sortedBy { groupOrder.indexOf(it.key) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("搜索对话内容...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
    ) { padding ->
        if (filteredHistory.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (searchQuery.isEmpty()) "暂无历史记录" else "未找到匹配的对话",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                for ((group, messages) in sortedGroups) {
                    item {
                        Text(
                            text = "$group (${messages.size})",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    items(messages) { message ->
                        HistoryItem(
                            message = message,
                            onClick = {
                                MessageIdManager.currentMessageId = message.id
                                navController.navigate(R.id.nav_chat)
                            },
                            onRename = { newTitle ->
                                viewModel.renameConversation(message.id, newTitle)
                            },
                            onDelete = {
                                viewModel.deleteConversation(message.id)
                            },
                            onShare = {
                                showShareDialog(context, message)
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun showShareDialog(context: android.content.Context, message: ChatMessageEntity) {
    val options = arrayOf("分享为文本", "分享为Markdown", "分享为图片")
    
    androidx.appcompat.app.AlertDialog.Builder(context)
        .setTitle("选择分享方式")
        .setItems(options) { _, which ->
            when (which) {
                0 -> com.zoup.android.chatextend.utils.ShareUtils.shareAsText(context, message)
                1 -> com.zoup.android.chatextend.utils.ShareUtils.shareAsMarkdown(context, message)
                2 -> com.zoup.android.chatextend.utils.ShareUtils.shareAsImage(context, message)
            }
        }
        .show()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryItem(
    message: ChatMessageEntity,
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                val type = object : TypeToken<List<Message>>() {}.type
                val jsonContent = message.content
                if (jsonContent.isNotEmpty()) {
                    val messages = Gson().fromJson<List<Message>>(jsonContent, type)
                    if (messages.isNotEmpty()) {
                        // 显示标题(优先使用自定义标题)
                        val title = if (message.title.isNotEmpty()) {
                            message.title
                        } else {
                            val userMessage = messages.firstOrNull { it.role == "user" }
                            userMessage?.content ?: "无标题"
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 显示消息数量和时间
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${messages.size} 条消息",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = formatTimestamp(message.timestamp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("重命名") },
                onClick = {
                    showMenu = false
                    showRenameDialog = true
                },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = "重命名")
                }
            )
            DropdownMenuItem(
                text = { Text("分享") },
                onClick = {
                    showMenu = false
                    onShare()
                },
                leadingIcon = {
                    Icon(Icons.Default.Share, contentDescription = "分享")
                }
            )
            DropdownMenuItem(
                text = { Text("删除") },
                onClick = {
                    showMenu = false
                    showDeleteDialog = true
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            )
        }
    }

    // 重命名对话框
    if (showRenameDialog) {
        var newTitle by remember { mutableStateOf(message.title) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("重命名对话") },
            text = {
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("对话标题") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            onRename(newTitle)
                            Toast.makeText(context, "已重命名", Toast.LENGTH_SHORT).show()
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除对话") },
            text = { Text("确定要删除这个对话吗?此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
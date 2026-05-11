package com.zoup.android.chatextend.ui.chat

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.zoup.android.chatextend.data.repository.bean.ChatMessage
import com.zoup.android.chatextend.ui.chat.components.MarkdownText
import io.noties.markwon.Markwon
import org.koin.androidx.compose.getViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = getViewModel(),
    modifier: Modifier = Modifier
) {
    val chatState by viewModel.chatStateFlow.collectAsState()
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current
    val markwon = remember { Markwon.create(context) }
    var userInput by remember { mutableStateOf("") }

    // 显示错误提示
    LaunchedEffect(chatState.error) {
        chatState.error?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    // 自动滚动到最后一条消息
    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(chatState.messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier,
//        topBar = {
//            TopAppBar(
//                title = { Text("新对话") }
//            )
//        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 消息列表
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(chatState.messages) { message ->
                    when (message) {
                        is ChatMessage.UserMessage -> UserMessageItem(
                            message = message,
                            onCopy = { viewModel.copyMessage(it) },
                            onDelete = { viewModel.deleteMessage(it) }
                        )
                        is ChatMessage.AssistantMessage -> AssistantMessageItem(
                            message = message,
                            markwon = markwon,
                            onCopy = { viewModel.copyMessage(it) },
                            onRegenerate = { viewModel.regenerateMessage(it) }
                        )
                        else -> {
                            // 可选占位内容或日志上报
                            Text(text = "未知消息类型")
                        }
                    }
                }
            }

            // 输入框
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 新增：开始新会话按钮
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.secondary)
                        .clickable(
                            onClick = {
                                viewModel.startNewConversation() // 调用 ViewModel 方法
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh, // 使用 Refresh 图标表示新会话
                        contentDescription = "New Conversation",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(0.dp)),
                    placeholder = { Text("给DeepSeek发送消息") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (userInput.isNotBlank()) {
                                viewModel.sendMessage(userInput)
                                userInput = ""
                            }
                        }
                    ),
                    singleLine = false,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(
                            enabled = userInput.isNotBlank(),
                            onClick = {
                                if (userInput.isNotBlank()) {
                                    viewModel.sendMessage(userInput)
                                    userInput = ""
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserMessageItem(
    message: ChatMessage.UserMessage,
    onCopy: (String) -> Unit,
    onDelete: (ChatMessage.UserMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp),
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = { showMenu = true }
                )
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("复制") },
                    onClick = {
                        onCopy(message.content)
                        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                        showMenu = false
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
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除消息") },
            text = { Text("确定要删除这条消息吗?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(message)
                        showDeleteDialog = false
                        Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("删除")
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AssistantMessageItem(
    message: ChatMessage.AssistantMessage,
    markwon: Markwon,
    onCopy: (String) -> Unit,
    onRegenerate: (ChatMessage.AssistantMessage) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp),
                modifier = Modifier.combinedClickable(
                    onClick = {},
                    onLongClick = { 
                        if (!message.isPending) {
                            showMenu = true 
                        }
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (message.isPending && message.content.isEmpty()) {
                        // 初始加载状态
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Text("思考中...")
                        }
                    } else {
                        // 流式内容或完整内容
                        ComposeMarkdownText(
                            markdown = message.content,
                            markwon = markwon
                        )

                        if (message.isPending) {
                            Spacer(modifier = Modifier.height(8.dp))
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("复制") },
                    onClick = {
                        onCopy(message.content)
                        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("重新生成") },
                    onClick = {
                        onRegenerate(message)
                        Toast.makeText(context, "正在重新生成...", Toast.LENGTH_SHORT).show()
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Refresh, contentDescription = "重新生成")
                    }
                )
            }
        }
    }
}

@Composable
fun ComposeMarkdownText(
    markdown: String,
    markwon: Markwon,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MarkdownText(context).apply {
                setMarkdown(markdown)
            }
        },
        update = { view ->
            view.setMarkdown(markdown)
        }
    )
}

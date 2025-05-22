package com.zoup.android.chatextend.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.findNavController
import com.zoup.android.chatextend.MainActivity
import com.zoup.android.chatextend.R
import com.zoup.android.chatextend.data.database.chatmessage.ChatMessageEntity
import com.zoup.android.chatextend.ui.chat.ChatViewModel
import com.zoup.android.chatextend.utils.MessageIdManager

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

    Scaffold(topBar = {
        TopAppBar(title = { Text("历史记录") })
    }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(history) { message ->
                HistoryItem(message = message, onClick = {
                    // 点击后跳转到 ChatScreen 并恢复聊天
                    MessageIdManager.currentMessageId = message.id
                    navController.navigate(R.id.nav_home)
                })
            }
        }
    }
}

@Composable
fun HistoryItem(
    message: ChatMessageEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "${message.content.take(50)}...")
        }
    }
}
package com.zoup.android.chatextend.ui.history

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.zoup.android.chatextend.data.database.ChatMessageDao
import com.zoup.android.chatextend.data.database.ChatMessageEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(chatMessageDao: ChatMessageDao) {
    // 从 Flow 收集数据并转换为 Compose 可观察的 State
    val messages = chatMessageDao.getAllMessages()
        .collectAsState(initial = emptyList<ChatMessageEntity>())

    Scaffold(
        topBar = { TopAppBar(title = { Text("Chat History") }) }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
//            items(messages) { message ->
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp),
//                    colors = CardDefaults.cardColors(
//                        containerColor = if (message.isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
//                    )
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text(text = message.content)
//                    }
//                }
//            }
        }
    }
}

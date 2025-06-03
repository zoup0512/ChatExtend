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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zoup.android.chatextend.MainActivity
import com.zoup.android.chatextend.R
import com.zoup.android.chatextend.data.api.model.Message
import com.zoup.android.chatextend.data.database.entity.ChatMessageEntity
import com.zoup.android.chatextend.ui.chat.ChatViewModel
import com.zoup.android.chatextend.utils.MessageIdManager
import java.util.*

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

    // 分组逻辑
    val now = System.currentTimeMillis()
    val groupedHistory = history.groupBy { message ->
        val diffDays = (now - message.timestamp) / (24 * 60 * 60 * 1000)
        when {
            diffDays < 1 -> "1天内"
            diffDays < 7 -> "7天内"
            diffDays < 30 -> "30天内"
            diffDays < 365 -> "一年内"
            else -> "更早"
        }
    }

    val groupOrder = listOf("1天内", "7天内", "30天内", "一年内", "更早")
    val sortedGroups = groupedHistory.entries.sortedBy { groupOrder.indexOf(it.key) }

    Scaffold(topBar = {
        TopAppBar(title = { Text("历史记录") })
    }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            for ((group, messages) in sortedGroups) {
                item {
                    Text(
                        text = group,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(messages) { message ->
                    HistoryItem(message = message, onClick = {
                        MessageIdManager.currentMessageId = message.id
                        navController.navigate(R.id.nav_chat)
                    })
                }
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
            val type = object : TypeToken<List<Message>>() {}.type
            val jsonContent = message.content
            if (jsonContent.isNotEmpty()) {
                val messages = Gson().fromJson<List<Message>>(jsonContent, type)
                if (messages.isNotEmpty()) {
                    val userMessage = messages.firstOrNull { it.role == "user" }
                    val title = userMessage?.content
                    Text(text = "${title?.take(50)}...")
                }
            }
        }
    }
}

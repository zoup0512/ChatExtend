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
import com.zoup.android.chatextend.data.database.chatmessage.ChatMessageEntity
import com.zoup.android.chatextend.ui.chat.ChatViewModel
import com.zoup.android.chatextend.utils.MessageIdManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: ChatViewModel) {
    val history by viewModel.getAllHistoryMessages().collectAsState(initial = emptyList())
    val groupedHistory by remember(history) {
        mutableStateOf(groupHistoryMessages(history))
    }

    val context = LocalContext.current
    val activity = context as? MainActivity ?: return

    val navController = remember {
        activity.findNavController(R.id.nav_host_fragment_content_main)
    }

    Scaffold(topBar = {
        TopAppBar(title = { Text("历史记录") })
    }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            groupedHistory.forEach { (timeRange, messages) ->
                item {
                    Text(
                        text = timeRange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(Color.LightGray),
                        fontWeight = FontWeight.Bold
                    )
                }
                items(messages) { message ->
                    HistoryItem(message = message, onClick = {
                        MessageIdManager.currentMessageId = message.id
                        navController.navigate(R.id.nav_home)
                    })
                }
            }
        }
    }
}

private fun groupHistoryMessages(messages: List<ChatMessageEntity>): Map<String, List<ChatMessageEntity>> {
    val now = System.currentTimeMillis()
    return messages.groupBy {
        val diffDays = (now - it.timestamp) / (1000 * 60 * 60 * 24)
        when {
            diffDays <= 1 -> "1天内"
            diffDays <= 7 -> "7天内"
            diffDays <= 30 -> "30天内"
            diffDays <= 365 -> "1年内"
            else -> "更早"
        }
    }
}

package com.zoup.android.chatextend.data.repository.bean

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false
)
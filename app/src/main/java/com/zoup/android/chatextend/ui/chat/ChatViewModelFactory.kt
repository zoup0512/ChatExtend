package com.zoup.android.chatextend.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.zoup.android.chatextend.data.repository.ChatMessageRepository

class ChatViewModelFactory(
    private val chatMessageRepository: ChatMessageRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(chatMessageRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
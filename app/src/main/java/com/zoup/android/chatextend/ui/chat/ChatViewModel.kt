package com.zoup.android.chatextend.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoup.android.chatextend.data.database.chatmessage.ChatMessageEntity
import com.zoup.android.chatextend.data.repository.ChatRepository
import com.zoup.android.chatextend.data.repository.bean.ChatState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    // 聊天消息状态
    private var _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    fun sendMessage(userInput: String) {
        viewModelScope.launch {
            _chatState = chatRepository.sendMessage(userInput, _chatState)
        }
    }

    /**
     * 清空当前聊天界面及数据库中的历史记录
     */
    fun clearChatHistory() {
        viewModelScope.launch {
            chatRepository.clearChat(_chatState)
        }
    }

    /**
     * 获取所有历史消息（用于在 HistoryScreen 中展示）
     */
    fun getAllHistoryMessages() = chatRepository.getAllHistoryMessages()
}
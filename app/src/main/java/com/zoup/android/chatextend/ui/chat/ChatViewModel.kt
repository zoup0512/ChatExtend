package com.zoup.android.chatextend.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoup.android.chatextend.data.database.ChatMessageEntity
import com.zoup.android.chatextend.data.repository.ChatRepository
import com.zoup.android.chatextend.data.repository.ChatRepository.ChatState
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
     * 从历史记录中恢复对话（点击某条历史消息后调用）
     */
    fun resumeChatFromHistory(historyMessage: ChatMessageEntity) {
        viewModelScope.launch {
            val restoredState = chatRepository.resumeChatFromHistory(historyMessage)
            _chatState = restoredState
        }
    }

    fun resumeChatFromSession(sessionId: String) {
        viewModelScope.launch {
            _chatState = chatRepository.resumeChatFromSession(sessionId)
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
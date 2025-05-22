package com.zoup.android.chatextend.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoup.android.chatextend.data.repository.ChatRepository
import com.zoup.android.chatextend.data.repository.bean.ChatState
import com.zoup.android.chatextend.utils.MessageIdManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {

    // 聊天消息状态
    private var _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    fun initViews(messageId: String?) {
        viewModelScope.launch {
            _chatState = chatRepository.initViews(messageId,_chatState)
        }
    }
    fun sendMessage(userInput: String) {
        viewModelScope.launch {
            _chatState = chatRepository.sendMessage(userInput, _chatState)
        }
    }

    fun startNewConversation() {
        MessageIdManager.currentMessageId = null
        _chatState.value = ChatState() // 重置为初始状态
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
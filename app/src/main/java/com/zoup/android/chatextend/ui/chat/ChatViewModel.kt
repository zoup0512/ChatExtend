package com.zoup.android.chatextend.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoup.android.chatextend.data.database.entity.ChatMessageEntity
import com.zoup.android.chatextend.data.repository.ChatMessageRepository
import com.zoup.android.chatextend.data.repository.bean.ChatState
import com.zoup.android.chatextend.utils.MessageIdManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class ChatViewModel(private val chatMessageRepository: ChatMessageRepository) : ViewModel() {

    // 聊天消息状态
    private var _chatStateFlow = MutableStateFlow(ChatState())
    val chatStateFlow: StateFlow<ChatState> = _chatStateFlow.asStateFlow()

    fun initViews(messageId: String?) {
        viewModelScope.launch {
            chatMessageRepository.initViews(messageId, _chatStateFlow).collect {
                _chatStateFlow.value = it
            }
        }
    }

    fun sendMessage(userInput: String) {
        viewModelScope.launch {
            _chatStateFlow = chatMessageRepository.sendMessage(userInput, _chatStateFlow)
        }
    }

    fun startNewConversation() {
        MessageIdManager.currentMessageId = null
        _chatStateFlow.value = ChatState() // 重置为初始状态
    }


    /**
     * 清空当前聊天界面及数据库中的历史记录
     */
    fun clearChatHistory() {
        viewModelScope.launch {
            chatMessageRepository.clearChat(_chatStateFlow)
        }
    }

    /**
     * 获取所有历史消息（用于在 HistoryScreen 中展示）
     */
    fun getAllHistoryMessages() = chatMessageRepository.getAllHistoryMessages()

    /**
     * 监听收藏状态
     */
    fun collectChatMessages(categoryId: Int) {
        viewModelScope.launch {
            chatMessageRepository.collectChatMessages(categoryId, _chatStateFlow).collect {
                _chatStateFlow.value = it
            }
        }
    }

    fun groupMessagesByTime(messages: List<ChatMessageEntity>): Map<String, List<ChatMessageEntity>> {
        val grouped = mutableMapOf<String, List<ChatMessageEntity>>()

        val now = System.currentTimeMillis()

        // 今天（24小时）
        val todayStart = now - (now % 86400000) // 一天的毫秒数
        // 昨天（前24小时）
        val yesterdayStart = todayStart - 86400000
        // 最近7天
        val last7DaysStart = now - 7 * 86400000
        // 最近30天
        val last30DaysStart = now - 30 * 86400000
        // 最近一年
        val lastYearStart = now - 365 * 86400000

        grouped["今天"] = messages.filter { it.timestamp >= todayStart }
        grouped["昨天"] = messages.filter { it.timestamp in yesterdayStart until todayStart }
        grouped["最近7天"] = messages.filter { it.timestamp in last7DaysStart until yesterdayStart }
        grouped["最近30天"] = messages.filter { it.timestamp in last30DaysStart until last7DaysStart }
        grouped["一年内"] = messages.filter { it.timestamp in lastYearStart until last30DaysStart }

        return grouped
    }

}
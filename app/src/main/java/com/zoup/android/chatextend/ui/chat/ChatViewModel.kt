package com.zoup.android.chatextend.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zoup.android.chatextend.ChatApplication
import com.zoup.android.chatextend.data.database.entity.ChatMessageEntity
import com.zoup.android.chatextend.data.repository.ChatMessageRepository
import com.zoup.android.chatextend.data.repository.bean.ChatMessage
import com.zoup.android.chatextend.data.repository.bean.ChatState
import com.zoup.android.chatextend.utils.MessageIdManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

    /**
     * 复制消息到剪贴板
     */
    fun copyMessage(content: String) {
        val context = ChatApplication.AppSingleton.application
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("message", content)
        clipboard.setPrimaryClip(clip)
    }

    /**
     * 删除用户消息
     */
    fun deleteMessage(message: ChatMessage.UserMessage) {
        viewModelScope.launch {
            _chatStateFlow.update { state ->
                val updatedMessages = state.messages.toMutableList()
                val index = updatedMessages.indexOf(message)
                if (index != -1) {
                    // 删除用户消息
                    updatedMessages.removeAt(index)
                    // 如果后面紧跟着助手消息,也删除
                    if (index < updatedMessages.size && updatedMessages[index] is ChatMessage.AssistantMessage) {
                        updatedMessages.removeAt(index)
                    }
                }
                state.copy(messages = updatedMessages)
            }
            // 更新数据库
            chatMessageRepository.updateChatState(_chatStateFlow)
        }
    }

    /**
     * 重新生成助手消息
     */
    fun regenerateMessage(message: ChatMessage.AssistantMessage) {
        viewModelScope.launch {
            // 找到这条消息之前的最后一条用户消息
            val messages = _chatStateFlow.value.messages
            val index = messages.indexOf(message)
            if (index > 0) {
                val previousMessage = messages[index - 1]
                if (previousMessage is ChatMessage.UserMessage) {
                    // 删除当前助手消息
                    _chatStateFlow.update { state ->
                        state.copy(
                            messages = state.messages.filterNot { it == message }
                        )
                    }
                    // 重新发送用户消息
                    sendMessage(previousMessage.content)
                }
            }
        }
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _chatStateFlow.update { it.copy(error = null) }
    }

}
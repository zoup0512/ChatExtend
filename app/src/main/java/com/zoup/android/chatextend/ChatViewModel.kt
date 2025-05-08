package com.zoup.android.chatextend

import ChatMessage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel : ViewModel() {
    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    private val apiService = ApiClient.deepSeekApiService
    private val apiKey = "sk-fbcbc2bfaec74a71b948a5e6ad6cfc4a" // 替换为你的实际API密钥

    fun sendMessage(message: String) {
        val userMessageId = UUID.randomUUID().toString()
        val assistantMessageId = UUID.randomUUID().toString()

        // 添加用户消息
        _chatState.update { state ->
            state.copy(
                messages = state.messages + ChatMessage.UserMessage(
                    id = userMessageId,
                    content = message
                )
            )
        }

        // 添加占位符助手消息
        _chatState.update { state ->
            state.copy(
                messages = state.messages + ChatMessage.AssistantMessage(
                    id = assistantMessageId,
                    content = "",
                    isPending = true
                )
            )
        }

        viewModelScope.launch {
            try {
                val messagesForApi = _chatState.value.messages
                    .filterNot { it is ChatMessage.AssistantMessage && it.isPending }
                    .map {
                        Message(
                            role = when (it) {
                                is ChatMessage.UserMessage -> "user"
                                is ChatMessage.AssistantMessage -> "assistant"
                                else -> "unknown"
                            },
                            content = it.content
                        )
                    } + Message(role = "user", content = message)

                val response = apiService.createChatCompletion(
                    authorization = "Bearer $apiKey",
                    request = DeepSeekRequest(
                        messages = messagesForApi,
                        stream = false
                    )
                )

                if (response.isSuccessful) {
                    val assistantResponse = response.body()?.choices?.firstOrNull()?.message?.content ?: ""

                    _chatState.update { state ->
                        val updatedMessages = state.messages.map {
                            if (it.id == assistantMessageId) {
                                (it as ChatMessage.AssistantMessage).copy(
                                    content = assistantResponse,
                                    isPending = false
                                )
                            } else {
                                it
                            }
                        }
                        state.copy(messages = updatedMessages)
                    }
                } else {
                    // 处理错误
                    _chatState.update { state ->
                        val updatedMessages = state.messages.map {
                            if (it.id == assistantMessageId) {
                                (it as ChatMessage.AssistantMessage).copy(
                                    content = "Error: ${response.message()}",
                                    isPending = false
                                )
                            } else {
                                it
                            }
                        }
                        state.copy(messages = updatedMessages)
                    }
                }
            } catch (e: Exception) {
                _chatState.update { state ->
                    val updatedMessages = state.messages.map {
                        if (it.id == assistantMessageId) {
                            (it as ChatMessage.AssistantMessage).copy(
                                content = "Error: ${e.localizedMessage}",
                                isPending = false
                            )
                        } else {
                            it
                        }
                    }
                    state.copy(messages = updatedMessages)
                }
            }
        }
    }

    data class ChatState(
        val messages: List<ChatMessage> = emptyList(),
        val isLoading: Boolean = false
    )
}
package com.zoup.android.chatextend.data.repository

// 导入必要的模块和类
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zoup.android.chatextend.data.api.DeepSeekApiService
import com.zoup.android.chatextend.data.api.model.DeepSeekRequest
import com.zoup.android.chatextend.data.api.model.DeepSeekStreamResponse
import com.zoup.android.chatextend.data.api.model.Message
import com.zoup.android.chatextend.data.database.chatmessage.ChatMessageDao
import com.zoup.android.chatextend.data.database.chatmessage.ChatMessageEntity
import com.zoup.android.chatextend.data.repository.bean.ChatMessage
import com.zoup.android.chatextend.data.repository.bean.ChatMessage.AssistantMessage
import com.zoup.android.chatextend.data.repository.bean.ChatState
import com.zoup.android.chatextend.utils.Constants
import com.zoup.android.chatextend.utils.MessageIdManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.util.UUID

/**
 * ChatRepository 类用于处理与聊天相关的业务逻辑，包括消息的发送、接收、重试机制等。
 * 它通过依赖注入的方式获取网络服务（DeepSeekApiService）、数据库访问对象（ChatMessageDao）以及 API 密钥。
 */
class ChatRepository(private val chatMessageDao: ChatMessageDao) {

    /**
     * 初始化聊天视图
     */
    suspend fun initViews(
        messageId: String?,
        chatState: MutableStateFlow<ChatState>
    ): MutableStateFlow<ChatState> {
        var currentMessageId: String? = null
        currentMessageId = if (messageId == null || messageId.isEmpty()) {
            return chatState
        } else {
            messageId
        }
        val flowContent = chatMessageDao.getMessageContentById(currentMessageId)
        val content = flowContent.first().toString()
        Log.d("ChatRepository-content", content)
        //TODO 为什么要这样判空??(content!="null")
        if (content != "null" && content.isNotEmpty()) {
            val type = object : TypeToken<List<Message>>() {}.type
            val messages: List<Message> = Gson().fromJson(content, type)
            chatState.update { state ->
                state.copy(
                    messages = messages.map { message ->
                        when (message.role) {
                            "user" -> ChatMessage.UserMessage(
                                id = currentMessageId,
                                content = message.content
                            )

                            "assistant" -> ChatMessage.AssistantMessage(
                                id = currentMessageId,
                                content = message.content,
                                isPending = false
                            )

                            else -> throw IllegalArgumentException("Unknown message role: ${message.role}")
                        }
                    },
                    isLoading = false
                )

            }
        }

        return chatState
    }
    /**
     * 发送消息到 DeepSeek API
     */
    suspend fun sendMessage(
        userInput: String,
        chatState: MutableStateFlow<ChatState>
    ): MutableStateFlow<ChatState> {
        var messageId = MessageIdManager.currentMessageId
        val newMessages = buildMessagesListWithInput(userInput, chatState)
        if (messageId.isNullOrEmpty()) {
            val newMessageId = UUID.randomUUID().toString()
            messageId = newMessageId
            MessageIdManager.currentMessageId=  newMessageId
            chatMessageDao.insertMessage(
                ChatMessageEntity(
                    id = messageId,
                    content = Gson().toJson(newMessages).toString()
                )
            )
        } else {
            chatMessageDao.updateMessage(
                ChatMessageEntity(
                    id = messageId,
                    content = Gson().toJson(newMessages).toString()
                )
            )
        }
        // 更新状态：添加用户消息和占位符助手消息
        chatState.update { state ->
            state.copy(
                messages = state.messages + listOf(
                    ChatMessage.UserMessage(
                        id = messageId,
                        content = userInput
                    ),
                    ChatMessage.AssistantMessage(
                        id = messageId,
                        content = "",
                        isPending = true
                    )
                ),
                isLoading = true
            )
        }

        withContext(Dispatchers.IO) {
            try {
                // 构建 API 请求
                val request = DeepSeekRequest(
                    model = "deepseek-chat",
                    messages = newMessages,
                    stream = true
                )
                // 发起流式请求
                val apiService = DeepSeekApiService.create()
                val response = apiService.createChatCompletion(
                    authorization = "Bearer " + Constants.DEEPSEEK_API_KEY,
                    request = request
                )

                if (!response.isSuccessful) {
                    throw HttpException(response)
                }

                // 处理流式响应
                processStreamResponse(
                    body = response.body()!!,
                    messageId = messageId,
                    chatState = chatState
                )
            } catch (e: Exception) {
                handleError(e, messageId, chatState)
            } finally {
                chatState.update { it.copy(isLoading = false) }
            }
        }

        return chatState
    }

    /**
     * 构建消息历史列表
     */
    private fun buildMessagesListWithInput(
        newMessage: String,
        chatState: StateFlow<ChatState>
    ): List<Message> {
        return chatState.value.messages
            .filterNot { it is ChatMessage.AssistantMessage && it.isPending }
            .map {
                Message(
                    role = when (it) {
                        is ChatMessage.UserMessage -> "user"
                        is ChatMessage.AssistantMessage -> "assistant"
                    },
                    content = it.content
                )
            } + Message(role = "user", content = newMessage)
    }

    /**
     * 构建消息历史列表
     */
    private fun buildMessagesListWithChatState(
        chatState: StateFlow<ChatState>
    ): List<Message> {
        return chatState.value.messages
            .filterNot { it is ChatMessage.AssistantMessage && it.isPending }
            .map {
                Message(
                    role = when (it) {
                        is ChatMessage.UserMessage -> "user"
                        is ChatMessage.AssistantMessage -> "assistant"
                    },
                    content = it.content
                )
            }
    }

    /**
     * 处理流式响应
     */
    private suspend fun processStreamResponse(
        body: ResponseBody,
        messageId: String,
        chatState: MutableStateFlow<ChatState>
    ) = withContext(Dispatchers.IO) {
        val reader = body.byteStream().bufferedReader()
        var accumulatedContent = ""

        try {
            reader.useLines { lines ->
                lines.forEach { line ->
                    if (line.startsWith("data:") && !line.contains("[DONE]")) {
                        val json = line.substringAfter("data:").trim()
                        if (json.isNotEmpty()) {
                            val content = parseStreamChunk(json)
                            if (content.isNotEmpty()) {
                                accumulatedContent += content
                                updateAssistantMessage(
                                    messageId = messageId,
                                    content = accumulatedContent,
                                    isPending = true,
                                    chatState = chatState
                                )
                            }
                        }
                    }
                }
            }

            // 流式接收完成
            updateAssistantMessage(
                messageId = messageId,
                content = accumulatedContent,
                isPending = false,
                chatState = chatState
            )
        } catch (e: Exception) {
            updateAssistantMessage(
                messageId = messageId,
                content = "Error: ${e.localizedMessage}",
                isPending = false,
                chatState = chatState
            )
        } finally {
            body.close()
        }
    }

    /**
     * 解析流式数据块
     */
    private fun parseStreamChunk(json: String): String {
        return try {
            val response = Json.Default.decodeFromString<DeepSeekStreamResponse>(json)
            response.choices.firstOrNull()?.delta?.content ?: ""
        } catch (e: Exception) {
            Log.e("ChatViewModel", e.toString())
            ""
        }
    }

    /**
     * 更新助手消息
     */
    private suspend fun updateAssistantMessage(
        messageId: String,
        content: String,
        isPending: Boolean,
        chatState: MutableStateFlow<ChatState>
    ) {
        chatState.update { state ->
            // only update the last assistant message
            val lastMessage = state.messages.findLast {
                it.id == messageId && it is AssistantMessage
            }
            val updatedMessages = state.messages.map {
                if (it == lastMessage && it is AssistantMessage) {
                    it.copy(
                        content = content,
                        isPending = isPending
                    )
                } else {
                    it
                }
            }
            state.copy(messages = updatedMessages)
        }

        // 更新数据库中的助手消息
        chatMessageDao.updateMessage(
            ChatMessageEntity(
                id=messageId,
                content = buildContentWithChatState(chatState)
            )
        )
    }

    /**
     * 错误处理
     */
    private suspend fun handleError(
        e: Exception,
        messageId: String,
        chatState: MutableStateFlow<ChatState>
    ) {
        val errorMessage = when (e) {
            is HttpException -> "API Error: ${e.code()} - ${e.response()?.errorBody()?.string()}"
            else -> "Network Error: ${e.localizedMessage}"
        }

        updateAssistantMessage(
            messageId = messageId,
            content = errorMessage,
            isPending = false,
            chatState = chatState
        )
    }

    /**
     * 清空聊天
     */
    fun clearChat(chatState: MutableStateFlow<ChatState>) {
        chatState.update { ChatState() }
    }

    /**
     * 加载所有历史消息（用于展示）
     */
    fun getAllHistoryMessages(): Flow<List<ChatMessageEntity>> {
        return chatMessageDao.getAllMessages()
    }

    private fun buildContentWithChatState(
        chatState: StateFlow<ChatState>
    ): String {
        val messages =buildMessagesListWithChatState(chatState)
        return Gson().toJson(messages).toString()
    }

}

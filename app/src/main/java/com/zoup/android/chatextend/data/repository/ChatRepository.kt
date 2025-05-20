package com.zoup.android.chatextend.data.repository

// 导入必要的模块和类
import android.util.Log
import com.zoup.android.chatextend.BuildConfig
import com.zoup.android.chatextend.data.api.DeepSeekApiService
import com.zoup.android.chatextend.data.api.model.DeepSeekRequest
import com.zoup.android.chatextend.data.api.model.DeepSeekStreamResponse
import com.zoup.android.chatextend.data.api.model.Message
import com.zoup.android.chatextend.data.database.ChatMessage
import com.zoup.android.chatextend.data.database.ChatMessageDao
import com.zoup.android.chatextend.data.database.ChatMessageEntity
import com.zoup.android.chatextend.utils.formatTimestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
     * 发送消息到 DeepSeek API
     */
    suspend fun sendMessage(
        userInput: String,
        chatState: MutableStateFlow<ChatState>
    ): MutableStateFlow<ChatState> {
        val userMessageId = UUID.randomUUID().toString()
        val assistantMessageId = UUID.randomUUID().toString()

        // 更新状态：添加用户消息和占位符助手消息
        chatState.update { state ->
            state.copy(
                messages = state.messages + listOf(
                    ChatMessage.UserMessage(
                        id = userMessageId,
                        content = userInput
                    ),
                    ChatMessage.AssistantMessage(
                        id = assistantMessageId,
                        content = "",
                        isPending = true
                    )
                ),
                isLoading = true
            )
        }
        val sessionId=generateSessionId()
        // 保存用户消息到数据库
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                role = "user",
                content = userInput,
                sessionId = sessionId
            )
        )

        chatMessageDao.insertMessage(
            ChatMessageEntity(
                role = "assistant",
                content = "",
                isPending = true,
                sessionId = sessionId
            )
        )
        withContext (Dispatchers.IO) {
            try {
                // 构建 API 请求
                val request = DeepSeekRequest(
                    model = "deepseek-chat",
                    messages = buildMessagesList(userInput, chatState),
                    stream = true
                )
                // 发起流式请求
                val apiService = DeepSeekApiService.create()
                val response = apiService.createChatCompletion(
                    authorization = "Bearer ${BuildConfig.DEEPSEEK_API_KEY}",
                    request = request
                )

                if (!response.isSuccessful) {
                    throw HttpException(response)
                }

                // 处理流式响应
                processStreamResponse(
                    body = response.body()!!,
                    messageId = assistantMessageId,
                    chatState = chatState
                )
            } catch (e: Exception) {
                handleError(e, assistantMessageId, chatState)
            } finally {
                chatState.update { it.copy(isLoading = false) }
            }
        }

        return chatState
    }

    /**
     * 构建消息历史列表
     */
    private fun buildMessagesList(
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
            val updatedMessages = state.messages.map {
                if (it.id == messageId && it is ChatMessage.AssistantMessage) {
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
                role = "assistant",
                content = content,
                isPending = isPending,
                sessionId = messageId
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

    /**
     * 从某条历史消息出发，加载整个会话
     */
    suspend fun resumeChatFromHistory(historyMessage: ChatMessageEntity): MutableStateFlow<ChatState> {
        // 从数据库中查询该 sessionId 下的所有消息（假设未来添加了 sessionId）
        val sessionMessages = chatMessageDao.getMessagesBySessionId(historyMessage.sessionId)

        val messages = sessionMessages.map { msg ->
            if (msg.role == "user") {
                ChatMessage.UserMessage(msg.id, msg.content)
            } else {
                ChatMessage.AssistantMessage(msg.id, msg.content, isPending = false)
            }
        }

        return MutableStateFlow(ChatState(messages = messages))
    }

    // 获取每个会话的首条用户消息
    fun getFirstUserMessages() = chatMessageDao.getFirstUserMessagesBySession()

    // 根据 sessionId 获取完整对话记录
    suspend fun resumeChatFromSession(sessionId: String): MutableStateFlow<ChatState> {
        val messages = chatMessageDao.getMessagesBySessionId(sessionId)
        val state = ChatState(
            messages = messages.map {
                if (it.role == "user") {
                    ChatMessage.UserMessage(it.id, it.content)
                } else {
                    ChatMessage.AssistantMessage(it.id, it.content, isPending = false)
                }
            }
        )
        return MutableStateFlow(state)
    }


    // 数据模型
    data class ChatState(
        val messages: List<ChatMessage> = emptyList(),
        val isLoading: Boolean = false
    )

    fun generateSessionId(): String {
        val randomSuffix = UUID.randomUUID().toString()
        return randomSuffix
    }
}

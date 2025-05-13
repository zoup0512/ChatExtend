package com.zoup.android.chatextend.data.repository

import com.zoup.android.chatextend.data.api.DeepSeekApiService
import com.zoup.android.chatextend.data.db.ChatMessageDao
import com.zoup.android.chatextend.data.db.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

class ChatRepository(
    private val apiService: DeepSeekApiService,
    private val chatMessageDao: ChatMessageDao,
    private val apiKey: String
)
{
//    fun getAllMessages(): Flow<List<ChatMessageEntity>> {
//        return chatMessageDao.getAllMessages()
//    }
//
//    suspend fun sendMessage(content: String, retryCount: Int = 3): Flow<ApiResponse<String>> = flow {
//        // Save user message
//        val userMessage = ChatMessageEntity(
//            role = "user",
//            content = content,
//            isPending = false
//        )
//        chatMessageDao.insertMessage(userMessage)
//
//        // Create assistant pending message
//        val assistantMessageId = UUID.randomUUID().toString()
//        val assistantMessage = ChatMessageEntity(
//            id = assistantMessageId,
//            role = "assistant",
//            content = "",
//            isPending = true
//        )
//        chatMessageDao.insertMessage(assistantMessage)
//
//        var currentRetry = 0
//        var lastError: Exception? = null
//
//        while (currentRetry < retryCount) {
//            try {
//                val request = ChatRequest(
//                    messages = listOf(
//                        ChatMessage(role = "user", content = content)
//                    )
//                )
//
//                apiService.sendMessageStream(apiKey, request).collect { responseBody ->
//                    val buffer = StringBuilder()
//                    responseBody.byteStream().bufferedReader().use { reader ->
//                        var line: String?
//                        while (reader.readLine().also { line = it } != null) {
//                            if (line?.startsWith("data:") == true) {
//                                val json = line!!.substring(5).trim()
//                                if (json != "[DONE]") {
//                                    try {
//                                        val chatResponse = parseStreamResponse(json)
//                                        val deltaContent = chatResponse.choices[0].delta?.content ?: ""
//                                        buffer.append(deltaContent)
//
//                                        // Update message in DB
//                                        chatMessageDao.updateMessage(
//                                            assistantMessage.copy(content = buffer.toString())
//                                        )
//                                    } catch (e: Exception) {
//                                        // Log parsing error but continue
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    // Final update when stream is complete
//                    chatMessageDao.updateMessage(
//                        assistantMessage.copy(
//                            content = buffer.toString(),
//                            isPending = false
//                        )
//                    )
//                    emit(ApiResponse.Success(buffer.toString()))
//                }
//
//                // If we get here, the request was successful
//                return@flow
//            } catch (e: Exception) {
//                lastError = e
//                currentRetry++
//
//                if (currentRetry < retryCount) {
//                    // Wait before retrying
//                    kotlinx.coroutines.delay(1000L * currentRetry)
//                }
//            }
//        }
//
//        // If we get here, all retries failed
//        val errorMessage = when (lastError) {
//            is HttpException -> "HTTP error: ${lastError.code()}"
//            is IOException -> "Network error: ${lastError.message}"
//            else -> "Error: ${lastError?.message ?: "Unknown error"}"
//        }
//
//        // Update message with error
//        chatMessageDao.updateMessage(
//            assistantMessage.copy(
//                isPending = false,
//                error = errorMessage
//            )
//        )
//
//        emit(ApiResponse.Error(errorMessage))
//    }
//
//    suspend fun clearChat() {
//        chatMessageDao.clearAllMessages()
//    }
//
//    private fun parseStreamResponse(json: String): ChatResponse {
//        // Implement JSON parsing for stream response
//        // This is a simplified version - you should use a proper JSON parser
//        // and handle all fields properly
//        return ChatResponse(
//            id = "",
//            choices = listOf(Choice(delta = ChatMessage(role = "assistant", content = json), index = 0, finish_reason = null)),
//            created = 0
//        )
//    }
}
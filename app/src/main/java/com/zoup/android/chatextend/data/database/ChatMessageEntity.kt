package com.zoup.android.chatextend.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = Date().time,
    val isPending: Boolean = false,
    val error: String? = null,
    // 新增字段：用于标识所属会话
    val sessionId: String,
    // 新增字段：对应AI消息的messageId
    var messageId: String? = null,
    //  新增字段：用于标识AI消息对应的User提出问题的消息id
    var questionId: String? = null
)

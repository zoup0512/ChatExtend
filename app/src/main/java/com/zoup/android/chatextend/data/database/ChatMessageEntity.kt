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
    val sessionId: String = UUID.randomUUID().toString() // 可以在新会话开始时生成
)

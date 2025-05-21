package com.zoup.android.chatextend.data.database.chatmessage

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "chat_message")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val content: String,
    val timestamp: Long = Date().time
)

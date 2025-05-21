package com.zoup.android.chatextend.data.database.chatsession

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat_session")
data class ChatSessionEntity(
    @PrimaryKey val sessionId: String = UUID.randomUUID().toString(),
    //  新增字段：用于标识User提出问题的消息id
    var questionCount: Int = 0,
    //  新增字段：用于标识AI回答问题的消息id
    var answerCount: Int = 0
)
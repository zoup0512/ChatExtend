package com.zoup.android.chatextend.data.db

import androidx.compose.runtime.Immutable

@Immutable
sealed class ChatMessage {
    abstract val id: String
    abstract val content: String

    @Immutable
    data class UserMessage(
        override val id: String,
        override val content: String
    ) : ChatMessage()

    @Immutable
    data class AssistantMessage(
        override val id: String,
        override val content: String,
        val isPending: Boolean = false
    ) : ChatMessage()
}
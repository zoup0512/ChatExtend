package com.zoup.android.chatextend.data.repository

import com.zoup.android.chatextend.data.database.dao.ChatMessageDao
import com.zoup.android.chatextend.data.database.dao.MessageCategoryDao
import com.zoup.android.chatextend.data.database.entity.ChatMessageEntity
import com.zoup.android.chatextend.data.database.entity.MessageCategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map


class NotesRepository(
    private val chatMessageDao: ChatMessageDao,
    private val messageCategoryDao: MessageCategoryDao
) {

    fun getMergedCategoriesAndMessages(): Flow<List<MessageCategoryEntity>> {
        val allCategories: Flow<List<MessageCategoryEntity>> =
            messageCategoryDao.getAllMessageCategories()

        val allMessages: Flow<List<MessageCategoryEntity>> =
            mapChatMessagesToCategories(chatMessageDao.getAllMessages())

        return combine(allCategories, allMessages) { categories, messages ->
            (categories + messages).sortedByDescending { it.timestamp }
        }
    }

    fun mapChatMessagesToCategories(
        chatMessagesFlow: Flow<List<ChatMessageEntity>>
    ): Flow<List<MessageCategoryEntity>> = chatMessagesFlow.map { chatMessages ->
        chatMessages.map { message ->
            MessageCategoryEntity(
                id = message.id.hashCode(),
                messageId = message.id,
                name = message.title ?: "Untitled",
                parentCategoryId = message.categoryId,
                timestamp = message.timestamp
            )
        }
    }

}
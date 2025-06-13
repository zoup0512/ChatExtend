package com.zoup.android.chatextend.data.repository

import com.zoup.android.chatextend.data.database.dao.MessageCategoryDao
import com.zoup.android.chatextend.data.database.entity.MessageCategoryEntity
import kotlinx.coroutines.flow.Flow

class MessageCategoryRepository(private val messageCategoryDao: MessageCategoryDao) {

    fun getAllMessageCategories(): Flow<List<MessageCategoryEntity>> {
        val messageCategories = messageCategoryDao.getAllMessageCategories()
        return messageCategories
    }

    suspend fun deleteMessageCategoryById(id: Int): Flow<List<MessageCategoryEntity>> {
        messageCategoryDao.deleteMessageCategoryById(id)
        val messageCategories = messageCategoryDao.getAllMessageCategories()
        return messageCategories
    }

    suspend fun addMessageCategory(messageCategory: MessageCategoryEntity): Flow<List<MessageCategoryEntity>> {
        messageCategoryDao.insertMessageCategory(messageCategory)
        val messageCategories = messageCategoryDao.getAllMessageCategories()
        return messageCategories
    }
}
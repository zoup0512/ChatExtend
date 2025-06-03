package com.zoup.android.chatextend.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zoup.android.chatextend.data.database.entity.MessageCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageCategoryDao {

    @Query("SELECT * FROM message_category WHERE id = :id")
    fun getMessageCategoryById(id: Int): Flow<MessageCategoryEntity?>

    // 插入单条消息
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    fun insertMessageCategory(messageCategory: MessageCategoryEntity): Long

    @Delete
    fun deleteMessageCategory(messageCategory: MessageCategoryEntity)

    @Query("SELECT * FROM message_category WHERE parentCategoryId = :id")
    fun getAllSubMessageCategories(id: Int): Flow<List<MessageCategoryEntity>>

}
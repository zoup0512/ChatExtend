package com.zoup.android.chatextend.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zoup.android.chatextend.data.database.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_message WHERE id = :id LIMIT 1")
    suspend fun getMessageByIdSync(id: String): ChatMessageEntity?

    @Query("SELECT content FROM chat_message WHERE id = :id LIMIT 1")
    suspend fun getMessageContentByIdSync(id: String): String?

    // 获取全部消息
    @Query("SELECT * FROM chat_message ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    // 插入单条消息
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    // 更新消息
    @Update
    suspend fun updateMessage(message: ChatMessageEntity)


    // 清空数据库
    @Query("DELETE FROM chat_message")
    suspend fun clearAllMessages()

    // 获取最新消息的 id
    @Query("SELECT id FROM chat_message ORDER BY timestamp DESC LIMIT 1")
    fun getLatestMessageId(): Flow<String?>

}
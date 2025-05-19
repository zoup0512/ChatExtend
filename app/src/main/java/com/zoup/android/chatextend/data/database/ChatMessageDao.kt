package com.zoup.android.chatextend.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    // 获取全部消息（保留原有功能）
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    // 获取每个 session 的第一条用户消息（用于 HistoryScreen）
    @Query("""
        SELECT * FROM chat_messages 
        WHERE role = 'user' AND id IN (
            SELECT MIN(id) FROM chat_messages 
            WHERE role = 'user' 
            GROUP BY sessionId
        )
        ORDER BY timestamp DESC
    """)
    fun getFirstUserMessagesBySession(): Flow<List<ChatMessageEntity>>

    // 根据 sessionId 获取完整对话
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesBySessionId(sessionId: String): List<ChatMessageEntity>

    // 插入单条消息
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    // 更新消息
    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    // 清空数据库
    @Query("DELETE FROM chat_messages")
    suspend fun clearAllMessages()
}

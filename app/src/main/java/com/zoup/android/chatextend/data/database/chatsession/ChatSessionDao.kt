package com.zoup.android.chatextend.data.database.chatsession

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ChatSessionDao {

    @Query("SELECT * FROM chat_session")
    fun getAllSessions(): List<ChatSessionEntity>
}
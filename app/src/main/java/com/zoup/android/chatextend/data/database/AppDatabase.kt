package com.zoup.android.chatextend.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.zoup.android.chatextend.data.database.chatmessage.ChatMessageDao
import com.zoup.android.chatextend.data.database.chatmessage.ChatMessageEntity
import com.zoup.android.chatextend.data.database.chatsession.ChatSessionDao

@Database(entities = [ChatMessageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatMessageDao(): ChatMessageDao

    abstract fun chatSessionDao(): ChatSessionDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chat_db"
                ).build().also { instance = it }
            }
        }
    }
}
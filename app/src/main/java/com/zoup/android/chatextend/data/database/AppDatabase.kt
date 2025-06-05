package com.zoup.android.chatextend.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zoup.android.chatextend.data.database.dao.ChatMessageDao
import com.zoup.android.chatextend.data.database.dao.MessageCategoryDao
import com.zoup.android.chatextend.data.database.entity.ChatMessageEntity
import com.zoup.android.chatextend.data.database.entity.MessageCategoryEntity
import java.util.concurrent.Executors

@Database(entities = [ChatMessageEntity::class, MessageCategoryEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun messageCategoryDao(): MessageCategoryDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "chat_db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        insertDefaultCategories(db)
                    }
                }).setQueryCallback({ sqlQuery, bindArgs ->
                    Log.d("ROOM_SQL", "SQL: $sqlQuery")
                    Log.d("ROOM_ARGS", "Args: $bindArgs")
                }, Executors.newSingleThreadExecutor())
                    .build().also { instance = it }
            }
        }

        fun insertDefaultCategories(db: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()
            db.execSQL("INSERT INTO message_category (id,name,parentCategoryId,timestamp) VALUES (1,'知识查询',0,$now)")
            db.execSQL("INSERT INTO message_category (id,name,parentCategoryId,timestamp) VALUES (2,'学习辅助',0,$now)")
            db.execSQL("INSERT INTO message_category (id,name,parentCategoryId,timestamp) VALUES (3,'智能编码',0,$now)")
            db.execSQL("INSERT INTO message_category (id,name,parentCategoryId,timestamp) VALUES (4,'创意生成',0,$now)")
            db.execSQL("INSERT INTO message_category (id,name,parentCategoryId,timestamp) VALUES (5,'其它类别',0,$now)")

            db.execSQL("INSERT INTO message_category (id,name,parentCategoryId,timestamp) VALUES (6,'知识百科',1,$now)")
            db.execSQL("INSERT INTO message_category (id,name,parentCategoryId,timestamp) VALUES (7,'AI面试',2,$now)")
            db.execSQL("INSERT INTO message_category (id,name,parentCategoryId,timestamp) VALUES (8,'Android开发',7,$now)")
            db.execSQL("INSERT INTO message_category (id,name,parentCategoryId,timestamp) VALUES (9,'Kotlin基础',7,$now)")
        }
    }
}
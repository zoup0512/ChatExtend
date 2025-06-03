package com.zoup.android.chatextend.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.zoup.android.chatextend.data.database.dao.ChatMessageDao
import com.zoup.android.chatextend.data.database.dao.MessageCategoryDao
import com.zoup.android.chatextend.data.database.entity.ChatMessageEntity
import com.zoup.android.chatextend.data.database.entity.MessageCategoryEntity

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
                })
                    .build().also { instance = it }
            }
        }

        fun insertDefaultCategories(db: SupportSQLiteDatabase) {
            db.execSQL("INSERT INTO message_category (name,parentCategoryId,categoryId) VALUES ('知识查询',-1,1)")
            db.execSQL("INSERT INTO message_category (name,parentCategoryId,categoryId) VALUES ('学习辅助',-1,2)")
            db.execSQL("INSERT INTO message_category (name,parentCategoryId,categoryId) VALUES ('智能编码',-1,3)")
            db.execSQL("INSERT INTO message_category (name,parentCategoryId,categoryId) VALUES ('创意生成',-1,4)")
            db.execSQL("INSERT INTO message_category (name,parentCategoryId,categoryId) VALUES ('其它类别',-1,5)")
        }
    }
}
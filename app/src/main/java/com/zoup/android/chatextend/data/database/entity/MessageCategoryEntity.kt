package com.zoup.android.chatextend.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "message_category")
data class MessageCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val messageId: String? = null,
    val name: String,
    val parentCategoryId: Int,
    val timestamp: Long = Date().time
)
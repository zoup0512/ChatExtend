// 文件路径：com/zoup/android/chatextend/utils/SessionManager.kt
package com.zoup.android.chatextend.utils

import java.util.UUID

object SessionManager {
    var currentSessionId: String? = null
    /**
     * 生成一个新的 sessionId，并更新全局变量
     */
    fun generateCurrentSessionId(): String {
        if(currentSessionId == null){
            val newId = UUID.randomUUID().toString()
            currentSessionId = newId
        }
        return currentSessionId!!
    }
}

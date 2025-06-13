package com.zoup.android.chatextend.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun encryptString(input: String, visibleCharsAtStart: Int = 4, visibleCharsAtEnd: Int = 4): String {
    if (input.length <= visibleCharsAtStart + visibleCharsAtEnd) {
        return input // 如果字符串长度小于等于保留的首尾字符数，则直接返回原字符串
    }

    val encryptedPart = "*".repeat(input.length - visibleCharsAtStart - visibleCharsAtEnd)
    return input.take(visibleCharsAtStart) + encryptedPart + input.takeLast(visibleCharsAtEnd)
}

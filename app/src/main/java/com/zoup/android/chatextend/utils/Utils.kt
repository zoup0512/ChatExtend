package com.zoup.android.chatextend.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
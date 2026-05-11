package com.zoup.android.chatextend.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zoup.android.chatextend.data.api.model.Message
import com.zoup.android.chatextend.data.database.entity.ChatMessageEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ShareUtils {
    
    /**
     * 分享对话为文本
     */
    fun shareAsText(context: Context, message: ChatMessageEntity) {
        val text = formatMessageAsText(message)
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, message.title)
        }
        
        context.startActivity(Intent.createChooser(intent, "分享对话"))
    }
    
    /**
     * 分享对话为Markdown文件
     */
    fun shareAsMarkdown(context: Context, message: ChatMessageEntity) {
        try {
            val markdown = formatMessageAsMarkdown(message)
            val file = createTempFile(context, "${message.title}.md", markdown)
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/markdown"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, message.title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "分享Markdown文件"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 分享对话为图片
     */
    fun shareAsImage(context: Context, message: ChatMessageEntity) {
        try {
            val bitmap = createConversationImage(message)
            val file = saveBitmapToFile(context, bitmap, "${message.title}.png")
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "分享图片"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 格式化消息为纯文本
     */
    private fun formatMessageAsText(message: ChatMessageEntity): String {
        val sb = StringBuilder()
        sb.append("=== ${message.title} ===\n\n")
        
        val type = object : TypeToken<List<Message>>() {}.type
        val messages = Gson().fromJson<List<Message>>(message.content, type)
        
        messages.forEach { msg ->
            when (msg.role) {
                "user" -> sb.append("👤 用户:\n${msg.content}\n\n")
                "assistant" -> sb.append("🤖 AI:\n${msg.content}\n\n")
            }
        }
        
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sb.append("---\n生成时间: ${sdf.format(Date(message.timestamp))}")
        
        return sb.toString()
    }
    
    /**
     * 格式化消息为Markdown
     */
    private fun formatMessageAsMarkdown(message: ChatMessageEntity): String {
        val sb = StringBuilder()
        sb.append("# ${message.title}\n\n")
        
        val type = object : TypeToken<List<Message>>() {}.type
        val messages = Gson().fromJson<List<Message>>(message.content, type)
        
        messages.forEach { msg ->
            when (msg.role) {
                "user" -> {
                    sb.append("## 👤 用户\n\n")
                    sb.append("${msg.content}\n\n")
                }
                "assistant" -> {
                    sb.append("## 🤖 AI助手\n\n")
                    sb.append("${msg.content}\n\n")
                }
            }
            sb.append("---\n\n")
        }
        
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sb.append("*生成时间: ${sdf.format(Date(message.timestamp))}*\n")
        
        return sb.toString()
    }
    
    /**
     * 创建对话图片
     */
    private fun createConversationImage(message: ChatMessageEntity): Bitmap {
        val width = 1080
        val padding = 60
        val lineHeight = 80
        val titleHeight = 120
        
        val type = object : TypeToken<List<Message>>() {}.type
        val messages = Gson().fromJson<List<Message>>(message.content, type)
        
        // 计算所需高度
        val paint = Paint().apply {
            textSize = 40f
            isAntiAlias = true
        }
        
        var totalHeight = titleHeight + padding * 2
        messages.forEach { msg ->
            val lines = msg.content.split("\n")
            totalHeight += lines.size * lineHeight + padding
        }
        
        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // 背景
        canvas.drawColor(Color.WHITE)
        
        // 标题
        paint.textSize = 60f
        paint.color = Color.BLACK
        paint.isFakeBoldText = true
        canvas.drawText(message.title, padding.toFloat(), (padding + 60).toFloat(), paint)
        
        var y = titleHeight + padding * 2
        
        // 绘制消息
        paint.textSize = 40f
        paint.isFakeBoldText = false
        
        messages.forEach { msg ->
            // 角色标签
            paint.color = if (msg.role == "user") Color.parseColor("#2196F3") else Color.parseColor("#4CAF50")
            paint.isFakeBoldText = true
            val label = if (msg.role == "user") "👤 用户" else "🤖 AI"
            canvas.drawText(label, padding.toFloat(), y.toFloat(), paint)
            y += lineHeight
            
            // 消息内容
            paint.color = Color.DKGRAY
            paint.isFakeBoldText = false
            val lines = msg.content.split("\n")
            lines.forEach { line ->
                // 文本换行处理
                val maxWidth = width - padding * 2
                val words = line.split(" ")
                var currentLine = ""
                
                words.forEach { word ->
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    val bounds = Rect()
                    paint.getTextBounds(testLine, 0, testLine.length, bounds)
                    
                    if (bounds.width() > maxWidth && currentLine.isNotEmpty()) {
                        canvas.drawText(currentLine, padding.toFloat(), y.toFloat(), paint)
                        y += lineHeight
                        currentLine = word
                    } else {
                        currentLine = testLine
                    }
                }
                
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, padding.toFloat(), y.toFloat(), paint)
                    y += lineHeight
                }
            }
            
            y += padding / 2
        }
        
        return bitmap
    }
    
    /**
     * 保存Bitmap到文件
     */
    private fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String): File {
        val file = File(context.cacheDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }
    
    /**
     * 创建临时文件
     */
    private fun createTempFile(context: Context, filename: String, content: String): File {
        val file = File(context.cacheDir, filename)
        file.writeText(content)
        return file
    }
}

package com.zoup.android.chatextend.utils

import android.content.Context
import io.noties.markwon.Markwon
import java.io.File
import java.io.FileOutputStream

object MarkdownFileUtils {

    /**
     * 保存 Markdown 文本到应用内部存储
     * @param context 上下文
     * @param markdownText Markdown 文本内容
     * @param fileName 文件名（自动添加 .md 后缀）
     * @return 保存成功返回 true
     */
    fun saveMarkdownToInternal(context: Context, markdownText: String, fileName: String): Boolean {
        val validFileName = if (fileName.endsWith(".md")) fileName else "$fileName.md"

        return try {
            val file = File(context.filesDir, validFileName)
            FileOutputStream(file).use { fos ->
                fos.write(markdownText.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 从内部存储读取 Markdown 文件
     * @param context 上下文
     * @param fileName 文件名（带或不带 .md 后缀）
     * @return 文件内容或 null
     */
    fun readMarkdownFromInternal(context: Context, fileName: String): String? {
        val validFileName = if (fileName.endsWith(".md")) fileName else "$fileName.md"

        return try {
            val file = File(context.filesDir, validFileName)
            if (!file.exists()) return null
            file.readText()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 保存到外部公共目录（如 Downloads 需要权限）
     * @param dirType Environment.DIRECTORY_XXX 类型
     */
    fun saveMarkdownToExternal(
        context: Context,
        markdownText: String,
        fileName: String,
        dirType: String
    ): Boolean {
        val validFileName = if (fileName.endsWith(".md")) fileName else "$fileName.md"

        return try {
            val dir = context.getExternalFilesDir(dirType) ?: return false
            val file = File(dir, validFileName)
            FileOutputStream(file).use { fos ->
                fos.write(markdownText.toByteArray())
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
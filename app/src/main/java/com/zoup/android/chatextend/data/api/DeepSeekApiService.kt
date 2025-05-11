package com.zoup.android.chatextend.data.api

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Streaming

interface DeepSeekApiService {
    @POST("v1/chat/completions")
    @Streaming  // 必须添加此注解处理流式响应
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Body request: DeepSeekRequest  // 使用定义好的请求体
    ): retrofit2.Response<ResponseBody>  // 返回原始响应体
}
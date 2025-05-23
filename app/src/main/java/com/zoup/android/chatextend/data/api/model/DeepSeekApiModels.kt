package com.zoup.android.chatextend.data.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeepSeekRequest(
    @SerialName("model") val model: String = "deepseek-chat",
    @SerialName("messages") val messages: List<Message>,
    @SerialName("temperature") val temperature: Double = 0.7,
    @SerialName("max_tokens") val maxTokens: Int? = null,
    @SerialName("stream") val stream: Boolean = true,// 新增必要字段
//    @SerialName("top_p") val topP: Double = 1.0,
//    @SerialName("frequency_penalty") val frequencyPenalty: Double = 0.0,
//    @SerialName("presence_penalty") val presencePenalty: Double = 0.0

)

@Serializable
data class Message(
    @SerialName("role") val role: String, // "user" 或 "assistant"
    @SerialName("content") val content: String
)

@Serializable
data class DeepSeekStreamResponse(
    @SerialName("id") val id: String,
    @SerialName("object") val obj: String,
    @SerialName("created") val created: Long,
    @SerialName("model") val model: String,
    @SerialName("system_fingerprint") val systemFingerprint: String,
    @SerialName("choices") val choices: List<StreamChoice>,
    @SerialName("usage") val usage: Usage? = null
)

@Serializable
data class StreamChoice(
    @SerialName("delta") val delta: MessageDelta,
    @SerialName("index") val index: Int,
    @SerialName("finish_reason") val finishReason: String?,
    @SerialName("logprobs") val logprobs: String?,
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens") val promptTokens: Int,
    @SerialName("completion_tokens") val completionTokens: Int,
    @SerialName("total_tokens") val totalTokens: Int,
    @SerialName("prompt_tokens_details") val promptTokensDetails: PromptTokensDetails?,
    @SerialName("prompt_cache_hit_tokens") val promptCacheHitTokens: Int,
    @SerialName("prompt_cache_miss_tokens") val promptCacheMissTokens: Int,
)

@Serializable
data class PromptTokensDetails(
    @SerialName("cached_tokens") val cachedTokens: Int,
)

@Serializable
data class MessageDelta(
    @SerialName("role") val role: String? = null,
    @SerialName("content") val content: String? = null
)
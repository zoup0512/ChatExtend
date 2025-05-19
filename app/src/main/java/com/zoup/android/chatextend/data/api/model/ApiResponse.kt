package com.zoup.android.chatextend.data.api.model

import retrofit2.Response

/**
 * 用于统一包装 API 请求响应结果的密封类。
 *
 * 支持三种状态：
 * - Success：表示请求成功
 * - Error：表示请求发生错误
 * - Exception：表示请求过程中抛出异常
 */
sealed class ApiResponse<out T> {
    /**
     * 表示成功的 API 响应。
     *
     * @property data 成功返回的数据对象
     */
    data class Success<out T>(val data: T) : ApiResponse<T>()

    /**
     * 表示失败的 API 响应，包含错误信息和可选的响应体。
     *
     * @property message 错误描述信息
     * @property code HTTP 状态码
     * @property errorBody 错误响应体（可选）
     */
    data class Error(
        val message: String,
        val code: Int,
        val errorBody: Response<*>? = null
    ) : ApiResponse<Nothing>()

    /**
     * 表示在请求过程中抛出的异常。
     *
     * @property exception 异常对象
     */
    data class Exception(val exception: Throwable) : ApiResponse<Nothing>()
}
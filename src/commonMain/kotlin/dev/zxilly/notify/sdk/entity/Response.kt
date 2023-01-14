package dev.zxilly.notify.sdk.entity

import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    val code: Int,
    val body: T
)

@Serializable
data class ErrorResponse(
    val code: Int,
    val body: String
)
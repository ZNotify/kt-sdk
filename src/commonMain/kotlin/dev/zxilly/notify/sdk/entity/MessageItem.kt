package dev.zxilly.notify.sdk.entity

import kotlinx.serialization.Serializable

@Serializable
data class MessageItem(
    val id: String,
    val user_id: String,
    val content: String,
    val title: String,
    val long: String,
    val created_at: String
)
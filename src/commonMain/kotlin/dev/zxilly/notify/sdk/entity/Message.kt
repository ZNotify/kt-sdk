package dev.zxilly.notify.sdk.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: String,
    val content: String,
    val title: String,
    val long: String,
    val priority: Priority,

    @SerialName("created_at")
    val createdAt: String
)
package entity

import kotlinx.serialization.Serializable

@Serializable
data class SendResult(
    val id: String,
    val user_id: String,
    val content: String,
    val title: String,
    val long: String,
    val created_at: String
)
package entity

import kotlinx.serialization.Serializable


@Serializable
data class Message(
    val content: String,
    val title: String?,
    val long: String?
)
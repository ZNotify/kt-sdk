package entity

import kotlinx.serialization.Serializable


@Serializable
data class Message(
    val content: String,
    val title: String?,
    val long: String?
) {
    constructor(content: String) : this(content, null, null)
    constructor(content: String, title: String) : this(content, title, null)
}
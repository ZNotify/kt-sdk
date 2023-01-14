package dev.zxilly.notify.sdk.entity

data class MessageOption(
    val content: String,
    val title: String = "Notification",
    val long: String = "",
    val priority: Priority = Priority.Normal
)
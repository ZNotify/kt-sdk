package dev.zxilly.notify.sdk

import dev.zxilly.notify.sdk.entity.MessageOption
import dev.zxilly.notify.sdk.entity.Message

private suspend fun getClient(userID: String): Result<Client> {
    return Client.create(userID)
}

suspend fun send(userID: String, message: MessageOption): Result<Message> {
    return getClient(userID).getOrElse {
        return Result.failure(it)
    }.send(message)
}

suspend fun send(userID: String, content: String): Result<Message> {
    return send(userID, MessageOption(content))
}

@Suppress("unused")
suspend fun send(userID: String, content: String, title: String): Result<Message> {
    return send(userID, MessageOption(content, title))
}

@Suppress("unused")
suspend fun send(userID: String, content: String, title: String, long: String): Result<Message> {
    return send(userID, MessageOption(content, title, long))
}

@Suppress("unused")
suspend fun send(userID: String, block: MessageOption.() -> Unit): Result<Message> {
    val msg = MessageOption("").apply(block)
    return send(userID, msg)
}
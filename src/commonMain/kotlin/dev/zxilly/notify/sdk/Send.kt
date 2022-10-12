package dev.zxilly.notify.sdk

import dev.zxilly.notify.sdk.entity.MessagePayload
import dev.zxilly.notify.sdk.entity.MessageItem

private val clients = LRUCache<String, Client>(8)

private suspend fun getClient(userID: String): Result<Client> {
    val cacheClient = clients[userID]
    return if (cacheClient != null) {
        Result.success(cacheClient)
    } else {
        Client.create(userID)
    }
}

suspend fun send(userID: String, message: MessagePayload): Result<MessageItem> {
    return getClient(userID).getOrElse {
        return Result.failure(it)
    }.send(message)
}

suspend fun send(userID: String, content: String): Result<MessageItem> {
    return send(userID, MessagePayload(content))
}

@Suppress("unused")
suspend fun send(userID: String, content: String, title: String): Result<MessageItem> {
    return send(userID, MessagePayload(content, title))
}

@Suppress("unused")
suspend fun send(userID: String, content: String, title: String, long: String): Result<MessageItem> {
    return send(userID, MessagePayload(content, title, long))
}

@Suppress("unused")
suspend fun send(userID: String, block: MessagePayload.() -> Unit): Result<MessageItem> {
    val msg = MessagePayload("", null, null).apply(block)
    return send(userID, msg)
}
package dev.zxilly.notify.sdk

import dev.zxilly.notify.sdk.entity.MessageOption
import dev.zxilly.notify.sdk.entity.Message

private suspend fun getClient(userID: String, endpoint: String): Result<Client> {
    return Client.create(userID, endpoint)
}

suspend fun send(
    userID: String,
    message: MessageOption,
    endpoint: String = defaultEndpoint
): Result<Message> {
    return getClient(userID, endpoint).getOrElse {
        return Result.failure(it)
    }.send(message)
}

suspend fun send(
    userID: String,
    content: String,
    endpoint: String = defaultEndpoint
): Result<Message> {
    return send(userID, MessageOption(content), endpoint)
}

suspend fun send(
    userID: String,
    content: String,
    title: String,
    endpoint: String = defaultEndpoint
): Result<Message> {
    return send(userID, MessageOption(content, title), endpoint)
}


suspend fun send(
    userID: String,
    content: String,
    title: String,
    long: String,
    endpoint: String = defaultEndpoint
): Result<Message> {
    return send(userID, MessageOption(content, title, long), endpoint)
}

suspend fun send(
    userID: String,
    block: MessageOption.() -> Unit,
    endpoint: String = defaultEndpoint
): Result<Message> {
    val msg = MessageOption("").apply(block)
    return send(userID, msg, endpoint)
}
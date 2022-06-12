import entity.*

private val clients = LRUCache<String, Client>(8)

private suspend fun getClient(userID: String): Result<Client> {
    val cacheClient = clients[userID]
    return if (cacheClient != null) {
        Result.success(cacheClient)
    } else {
        Client.create(userID)
    }
}

suspend fun send(userID: String, message: Message): Result<MessageItem> {
    return getClient(userID).getOrElse {
        return Result.failure(it)
    }.send(message)
}

suspend fun send(userID: String, content: String): Result<MessageItem> {
    return send(userID, Message(content, null, null))
}

suspend fun send(userID: String, content: String, title: String): Result<MessageItem> {
    return send(userID, Message(content, title, null))
}

suspend fun send(userID: String, content: String, title: String, long: String): Result<MessageItem> {
    return send(userID, Message(content, title, long))
}

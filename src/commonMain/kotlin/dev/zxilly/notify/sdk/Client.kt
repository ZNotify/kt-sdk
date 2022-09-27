package dev.zxilly.notify.sdk

import dev.zxilly.notify.sdk.entity.Message
import dev.zxilly.notify.sdk.entity.MessageItem
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Client private constructor(
    private val userID: String,
    private val endpoint: String
) {
    private val client = HttpClient()

    private suspend fun check() {
        val resp = client.get("$endpoint/check") {
            parameter("user_id", userID)
        }
        if (resp.bodyAsText() != "true") {
            throw Error("User ID not valid")
        }
    }

    suspend fun send(msg: Message) = wrap {
        if (msg.content.isBlank()) {
            throw emptyContentError
        }
        val resp = client.submitForm(
            url = "$endpoint/$userID/send",
            formParameters = Parameters.build {
                append("content", msg.content)
                msg.title?.let { append("title", it) }
                msg.long?.let { append("long", it) }
            }
        )
        if (!resp.ok()) {
            throw Error("Send failed\n${resp.bodyAsText()}")
        }
        Json.decodeFromString(MessageItem.serializer(), resp.bodyAsText())
    }

    suspend fun send(content: String) = wrap {
        val msg = Message(content, null, null)
        send(msg)
    }

    suspend fun send(content: String, title: String) = wrap {
        val msg = Message(content, title, null)
        send(msg)
    }

    suspend fun send(content: String, title: String, long: String) = wrap {
        val msg = Message(content, title, long)
        send(msg)
    }

    suspend fun send(block: Message.() -> Unit) = wrap {
        val msg = Message("", null, null).apply(block)
        send(msg)
    }

    suspend fun delete(id: String) = wrap {
        val resp = client.delete("$endpoint/$userID/$id")
        if (!resp.ok()) {
            throw Error("Delete failed\n${resp.bodyAsText()}")
        }
    }

    suspend fun reportFCMToken(token: String) = wrap {
        val resp = client.put("$endpoint/$userID/fcm/token") {
            setBody(token)
        }
        if (!resp.ok()) {
            throw Error("Report failed\n${resp.bodyAsText()}")
        }
    }

    suspend fun <T> fetchMessage(postProcessor: List<MessageItem>.() -> List<T>) = wrap {
        val resp = client.get("$endpoint/$userID/record")
        if (!resp.ok()) {
            throw Error("Fetch failed\n${resp.bodyAsText()}")
        }
        val messages = Json.decodeFromString(ListSerializer(MessageItem.serializer()), resp.bodyAsText())
        postProcessor(messages)
    }

    companion object {
        suspend fun create(userID: String, endpoint: String = defaultEndpoint) =
            wrap {
                val client = Client(userID, endpoint)
                client.check()
                client
            }

        suspend fun check(userID: String, endpoint: String = defaultEndpoint): Boolean {
            val ret = runCatching {
                val client = Client(userID, endpoint)
                client.check()
            }
            return ret.isSuccess
        }


        private suspend fun <T> wrap(block: suspend () -> T): Result<T> {
            return runCatching {
                block.invoke()
            }
        }
    }
}
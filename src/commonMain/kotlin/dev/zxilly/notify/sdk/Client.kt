package dev.zxilly.notify.sdk

import dev.zxilly.notify.sdk.entity.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Client private constructor(
    private val userID: String,
    private val endpoint: String
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        // set useragent
        install(UserAgent) {
            agent = "znotify-kt-sdk/${BuildKonfig.VERSION}"
        }
    }

    private suspend inline fun check() {
        val resp = client.get("$endpoint/check") {
            parameter("user_id", userID)
        }
        val ret: Response<Boolean> = resp.body()
        if (!ret.body) {
            throw Exception("User ID $userID not valid")
        }
    }

    suspend fun send(msg: MessagePayload) = wrap {
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
            val err: ErrorResponse = resp.body()
            throw Exception("Error code ${err.code}: ${err.body}")
        }
        (resp.body() as Response<MessageItem>).body
    }

    suspend fun send(content: String) = wrap {
        val msg = MessagePayload(content, null, null)
        send(msg)
    }

    suspend fun send(content: String, title: String) = wrap {
        val msg = MessagePayload(content, title, null)
        send(msg)
    }

    suspend fun send(content: String, title: String, long: String) = wrap {
        val msg = MessagePayload(content, title, long)
        send(msg)
    }

    suspend fun send(block: MessagePayload.() -> Unit) = wrap {
        val msg = MessagePayload("", null, null).apply(block)
        send(msg)
    }

    suspend fun delete(id: String) = wrap {
        val resp = client.delete("$endpoint/$userID/$id")
        if (!resp.ok()) {
            throw Error("Delete failed\n${resp.bodyAsText()}")
        }
    }

    suspend fun register(channel: Channel, token: String, deviceID: String) = wrap {
        if (!isUUID(deviceID)) {
            throw Error("Device ID is not a valid UUID")
        }

        val resp = client.put {
            url("$endpoint/$userID/token/$deviceID")
            setBody(
                Parameters.build {
                    append("channel", channel.value)
                    append("token", token)
                }.formUrlEncode()
            )
            header("Content-Type", "application/x-www-form-urlencoded")
        }
        if (!resp.ok()) {
            val err: ErrorResponse = resp.body()
            throw Exception("Error code ${err.code}: ${err.body}")
        }
        (resp.body() as Response<Boolean>).body
    }

    suspend fun unregister(deviceID: String) = wrap {
        if (!isUUID(deviceID)) {
            throw Error("Device ID is not a valid UUID")
        }

        val resp = client.delete("$endpoint/$userID/token/$deviceID")
        if (!resp.ok()) {
            val err: ErrorResponse = resp.body()
            throw Exception("Error code ${err.code}: ${err.body}")
        }
        (resp.body() as Response<Boolean>).body
    }

    suspend fun <T> fetchMessage(postProcessor: List<MessageItem>.() -> List<T>) = wrap {
        val resp = client.get("$endpoint/$userID/record")
        if (!resp.ok()) {
            throw Error("Fetch failed\n${resp.bodyAsText()}")
        }
        val messages = resp.body() as Response<List<MessageItem>>
        postProcessor(messages.body)
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
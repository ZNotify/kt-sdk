package dev.zxilly.notify.sdk

import dev.zxilly.notify.sdk.entity.*
import dev.zxilly.notify.sdk.resource.Check
import dev.zxilly.notify.sdk.resource.User
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@Suppress("MemberVisibilityCanBePrivate", "unused")
class Client private constructor(
    private val userSecret: String,
    private val endpoint: String
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }

        install(Resources)

        defaultRequest {
            url(endpoint.removeSuffixSlash())
        }

        // set useragent
        install(UserAgent) {
            agent = "znotify-kt-sdk/${BuildKonfig.VERSION}"
        }
    }

    private suspend inline fun check() {
        val resp = client.get(Check(userSecret))
        val ret: Response<Boolean> = resp.body()
        if (!ret.body) {
            throw Exception("User secret not valid")
        }
    }

    suspend fun send(msg: MessageOption) = wrap {
        if (msg.content.isBlank()) {
            throw emptyContentError
        }

        val resp = client.post(User.Send(User(userSecret))) {
            form {
                set("content", msg.content)
                set("title", msg.title)
                set("long", msg.long)
                set("priority", msg.priority.value)
            }
        }
        if (!resp.ok()) {
            val err: ErrorResponse = resp.body()
            throw Exception("Error code ${err.code}: ${err.body}")
        }
        (resp.body() as Response<Message>).body
    }

    suspend fun send(content: String) = wrap {
        val msg = MessageOption(content)
        send(msg)
    }

    suspend fun send(content: String, title: String) = wrap {
        val msg = MessageOption(content, title = title)
        send(msg)
    }

    suspend fun send(content: String, title: String, long: String) = wrap {
        val msg = MessageOption(content, title = title, long = long)
        send(msg)
    }

    suspend fun send(block: MessageOption.() -> Unit) = wrap {
        val msg = MessageOption("").apply(block)
        send(msg)
    }

    suspend fun getMessage(id: String) = wrap {
        val resp = client.get(User.Message(User(userSecret), id))
        if (!resp.ok()) {
            if (resp.status == HttpStatusCode.NotFound) {
                return@wrap null
            } else {
                val err: ErrorResponse = resp.body()
                throw Exception("Error code ${err.code}: ${err.body}")
            }
        } else {
            (resp.body() as Response<Message>).body
        }
    }

    suspend fun deleteMessage(id: String) = wrap {
        val resp = client.delete(User.Message(User(userSecret), id))
        if (!resp.ok()) {
            throw Error("Delete failed\n${resp.bodyAsText()}")
        }
    }

    suspend fun createDevice(
        channel: Channel,
        token: String,
        deviceID: String,
        deviceName: String = "",
        deviceMeta: String = ""
    ) = wrap {
        if (!isUUID(deviceID)) {
            throw Error("Device ID is not a valid UUID")
        }

        val resp = client.put(User.Device(User(userSecret), deviceID)) {
            form {
                set("channel", channel.value)
                set("token", token)
                set("device_name", deviceName)
                set("device_meta", deviceMeta)
            }
        }
        if (!resp.ok()) {
            val err: ErrorResponse = resp.body()
            throw Exception("Error code ${err.code}: ${err.body}")
        }
        (resp.body() as Response<Boolean>).body
    }

    suspend fun deleteDevice(deviceID: String) = wrap {
        if (!isUUID(deviceID)) {
            throw Error("Device ID is not a valid UUID")
        }

        val resp = client.delete(User.Device(User(userSecret), deviceID))
        if (!resp.ok()) {
            val err: ErrorResponse = resp.body()
            throw Exception("Error code ${err.code}: ${err.body}")
        }
        (resp.body() as Response<Boolean>).body
    }

    suspend fun <T> getMessages() = wrap {
        val resp = client.get(User.Messages(User(userSecret)))
        if (!resp.ok()) {
            throw Error("Fetch failed\n${resp.bodyAsText()}")
        }
        val messages = resp.body() as Response<List<Message>>
        return@wrap messages.body
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

        private fun HttpRequestBuilder.form(block: MutableMap<String, String>.() -> Unit) {
            val map = mutableMapOf<String, String>()
            map.block()
            setBody(FormDataContent(Parameters.build {
                map.forEach { (k, v) ->
                    append(k, v)
                }
            }))
        }
    }
}
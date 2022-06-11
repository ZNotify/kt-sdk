import entity.*
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
    private val endpoint: String = "https://push.learningman.top"
) {
    private val client = HttpClient()

    private suspend fun check() {
        val resp = client.get("$endpoint/$userID/check")
        if (resp.bodyAsText() != "true") {
            throw Error("User ID not valid")
        }
    }

    suspend fun send(msg: Message) = wrap {
        val resp = client.submitForm(
            url = "https://httpbin.org/post",
            formParameters = Parameters.build {
                append("content", msg.content)
                msg.title?.let { append("title", it) }
                msg.long?.let { append("long", it) }
            }
        )
        if (!resp.ok()) {
            throw Error("Send failed\n${resp.bodyAsText()}")
        }
        Json.decodeFromString(SendResult.serializer(), resp.bodyAsText())
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
        if (msg.content.isEmpty()) {
            throw Error("Content is empty")
        }
        send(msg)
    }

    suspend fun delete(id: String) = wrap {
        val resp = client.delete("$endpoint/$userID/$id")
        if (!resp.ok()) {
            throw Error("Delete failed\n${resp.bodyAsText()}")
        }
    }

    suspend fun reportFCMToken(token: String) = wrap {
        val resp = client.put("$endpoint/$userID/fcm/$token") {
            setBody(token)
        }
        if (!resp.ok()) {
            throw Error("Report failed\n${resp.bodyAsText()}")
        }
    }

    suspend fun <T> fetchMessage(postProcessor: List<Message>.() -> List<T>) = wrap {
        val resp = client.get("$endpoint/$userID/record")
        if (!resp.ok()) {
            throw Error("Fetch failed\n${resp.bodyAsText()}")
        }
        val messages = Json.decodeFromString(ListSerializer(Message.serializer()), resp.bodyAsText())
        postProcessor(messages)
    }

    companion object {
        suspend fun create(userID: String, endpoint: String = "https://push.learningman.top") =
            wrap {
                val client = Client(userID, endpoint)
                client.check()
                client
            }

        private suspend fun <T> wrap(block: suspend () -> T): Result<T> {
            return runCatching {
                block.invoke()
            }
        }
    }
}
package dev.zxilly.notify.sdk

import dev.zxilly.notify.sdk.entity.ErrorResponse
import dev.zxilly.notify.sdk.entity.Message
import dev.zxilly.notify.sdk.entity.Priority
import dev.zxilly.notify.sdk.entity.Response
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class SerializationTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun messageResponseRoundTripsWithWireNames() {
        val response = Response(
            200,
            Message("message-1", "Hello 世界", "Title", "Long body", Priority.High, "2026-10-07T00:00:00Z")
        )

        val encoded = json.encodeToString(response)
        assertTrue(encoded.contains("\"created_at\""))
        assertTrue(encoded.contains("\"priority\":\"high\""))
        assertEquals(response, json.decodeFromString<Response<Message>>(encoded))
    }

    @Test
    fun responseAllowsUnknownFields() {
        val encoded = """{"code":200,"body":{"id":"message-1","content":"Hello","title":"Title","long":"","priority":"normal","created_at":"2026-10-07T00:00:00Z","extra":true},"extra":"ignored"}"""
        val response = json.decodeFromString<Response<Message>>(encoded)

        assertEquals(200, response.code)
        assertEquals(Priority.Normal, response.body.priority)
        assertEquals("2026-10-07T00:00:00Z", response.body.createdAt)
    }

    @Test
    fun booleanListAndErrorResponsesDecode() {
        assertEquals(Response(200, true), json.decodeFromString<Response<Boolean>>("""{"code":200,"body":true}"""))
        assertEquals(emptyList(), json.decodeFromString<Response<List<Message>>>("""{"code":200,"body":[]}""").body)
        assertEquals(ErrorResponse(400, "Invalid input"), json.decodeFromString<ErrorResponse>("""{"code":400,"body":"Invalid input"}"""))
    }

    @Test
    fun unsupportedPriorityIsRejected() {
        assertFailsWith<SerializationException> {
            json.decodeFromString<Priority>("\"urgent\"")
        }
    }
}

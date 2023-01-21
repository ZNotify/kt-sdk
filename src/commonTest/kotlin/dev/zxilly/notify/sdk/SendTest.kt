package dev.zxilly.notify.sdk

import dev.zxilly.notify.sdk.entity.Message
import dev.zxilly.notify.sdk.entity.MessageOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue



@OptIn(ExperimentalCoroutinesApi::class)
internal class SendTest {
    @Test
    fun testCreateFailed() = runTest {
        val ret = send("error", "test", testEndpoint)
        assertTrue { ret.isFailure }
        assertTrue { ret.exceptionOrNull() is Throwable }
    }

    @Test
    fun checkSendMessage() = runTest {
        val ret = send("test", MessageOption("test"), testEndpoint)
        assertTrue { ret.isSuccess }
        assertTrue { ret.getOrNull() is Message }
        assertEquals(ret.getOrNull()!!.content, "test")
    }

    @Test
    fun checkSendMessage2() = runTest {
        val ret = send("test", MessageOption("test", "test_title"), testEndpoint)
        assertTrue { ret.isSuccess }
        assertTrue { ret.getOrNull() is Message }
        assertEquals(ret.getOrNull()!!.content, "test")
        assertEquals(ret.getOrNull()!!.title, "test_title")
    }

    @Test
    fun checkSendMessageFailed() = runTest {
        val ret = send("test", MessageOption(""), testEndpoint)
        assertTrue { ret.isFailure }
        assertTrue { ret.exceptionOrNull() is Throwable }
    }
}
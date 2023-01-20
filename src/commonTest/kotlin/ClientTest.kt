import dev.zxilly.notify.sdk.Client
import dev.zxilly.notify.sdk.entity.Channel
import dev.zxilly.notify.sdk.entity.Message
import dev.zxilly.notify.sdk.entity.MessageOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.expect
import kotlin.test.fail

@ExperimentalCoroutinesApi
class ClientTest {
    @Test
    fun testCreate() = runTest {
        val ret = Client.create("test")
        assertTrue { ret.isSuccess }
        assertTrue { ret.getOrNull() is Client }
    }

    @Test
    fun testCreateFailed() = runTest {
        val ret = Client.create("error")
        assertTrue { ret.isFailure }
        assertTrue { ret.exceptionOrNull() is Throwable }
    }

    @Test
    fun checkSendMessage() = runTest {
        val client = Client.create("test").getOrElse {
            fail(it.stackTraceToString())
        }
        val ret = client.send(MessageOption("test"))
        assertTrue(ret.exceptionOrNull()?.stackTraceToString()) { ret.isSuccess }
        assertTrue { ret.getOrNull() is Message }
        assertEquals(ret.getOrNull()!!.content, "test")
    }

    @Test
    fun checkSendMessage2() = runTest {
        val client = Client.create("test").getOrElse {
            fail(it.stackTraceToString())
        }
        val ret = client.send(MessageOption("test", "test_title"))
        assertTrue(ret.exceptionOrNull()?.stackTraceToString()) { ret.isSuccess }
        assertTrue { ret.getOrNull() is Message }
        assertEquals(ret.getOrNull()!!.content, "test")
        assertEquals(ret.getOrNull()!!.title, "test_title")
    }

    @Test
    fun checkSendMessageFailed() = runTest {
        val client = Client.create("test").getOrElse {
            fail(it.stackTraceToString())
        }
        val ret = client.send(MessageOption(""))
        assertTrue { ret.isFailure }
        assertTrue { ret.exceptionOrNull() is Throwable }

    }

    @Test
    fun checkRegister() = runTest {
        val client = Client.create("test").getOrElse {
            fail(it.stackTraceToString())
        }
        val ret = client.createDevice(Channel.FCM, "test", "test")
        assertTrue { ret.isFailure }
        assertTrue { ret.exceptionOrNull() is Throwable }
        expect("Device ID is not a valid UUID") {
            ret.exceptionOrNull()!!.message
        }

        val uuid = "00000000-0000-0000-0000-000000000000"
        val ret2 = client.createDevice(Channel.WebSocket, "", uuid)
        assertTrue(ret2.exceptionOrNull()?.stackTraceToString()) { ret2.isSuccess }
        assertTrue { ret2.getOrNull() is Boolean }
        assertTrue { ret2.getOrNull()!! }

        val ret3 = client.deleteDevice(uuid)
        assertTrue(ret3.exceptionOrNull()?.stackTraceToString()) { ret3.isSuccess }
        assertTrue { ret3.getOrNull() is Boolean }
        assertTrue { ret3.getOrNull()!! }
    }
}
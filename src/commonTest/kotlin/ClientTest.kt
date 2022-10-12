import dev.zxilly.notify.sdk.Client
import dev.zxilly.notify.sdk.entity.Channel
import dev.zxilly.notify.sdk.entity.MessagePayload
import dev.zxilly.notify.sdk.entity.MessageItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlin.test.*

@ExperimentalCoroutinesApi
class ClientTest {
    @BeforeTest
    fun testServer() = runTest {
        TestUtils.check()
    }

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
        val ret = client.send(MessagePayload("test"))
        assertTrue { ret.isSuccess }
        assertTrue { ret.getOrNull() is MessageItem }
        assertEquals(ret.getOrNull()!!.content, "test")
    }

    @Test
    fun checkSendMessage2() = runTest {
        val client = Client.create("test").getOrElse {
            fail(it.stackTraceToString())
        }
        val ret = client.send(MessagePayload("test", "test_title"))
        assertTrue { ret.isSuccess }
        assertTrue { ret.getOrNull() is MessageItem }
        assertEquals(ret.getOrNull()!!.content, "test")
        assertEquals(ret.getOrNull()!!.title, "test_title")
    }

    @Test
    fun checkSendMessageFailed() = runTest {
        val client = Client.create("test").getOrElse {
            fail(it.stackTraceToString())
        }
        val ret = client.send(MessagePayload(""))
        assertTrue { ret.isFailure }
        assertTrue { ret.exceptionOrNull() is Throwable }

    }

    @Test
    fun checkRegister() = runTest {
        val client = Client.create("test").getOrElse {
            fail(it.stackTraceToString())
        }
        val ret = client.register(Channel.FCM, "test", "test")
        assertTrue { ret.isFailure }
        assertTrue { ret.exceptionOrNull() is Throwable }
        expect("Device ID is not a valid UUID") {
            ret.exceptionOrNull()!!.message
        }

        val uuid = "00000000-0000-0000-0000-000000000000"
        val ret2 = client.register(Channel.WebSocket, "test", uuid)
        assertTrue { ret2.isSuccess }
        assertTrue { ret2.getOrNull() is Boolean }
        assertTrue { ret2.getOrNull()!! }
    }
}